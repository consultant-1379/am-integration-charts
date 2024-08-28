#!/bin/bash
#
# COPYRIGHT Ericsson 2024
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#


usage()
{
  echo "Usage: $0 -n <Kubernetes Namespace> -c <Chart/Path> -r <Release> [-v values] [-b backup/path] [-t Timeout] [-p Number of PODS] [-s extra parameters for upgrade]"
  echo ""
  echo "             -n         Kubernetes namespace used for the deployment"
  echo "             -c         Path to helm chart used"
  echo "             -r         Release name"
  echo "             -f         Rollback Revision or set to \"skip\". Used to set the revision to rollback to stabilise the system before restoring the Data"
  echo "  (optional) -v         Path to values yaml file [default \"--reuse-values\", reuse the last release's values]"
  echo "  (optional) -b         Backup path  [default: /mnt/pg_backup/pg_data_1]"
  echo "  (optional) -t         Time out for Upgrade/Rollback to wait [default: 420]"
  echo "  (optional) -p         Number of Backup DB's (postgres & idam database) Expected  [default: '4']"
  echo "  (optional) -s         Used to add extra parameters to the upgrade command, same as using --set in helm"
  exit 1
}

while getopts n:c:v:b:r:t:p:f:s: option; do
  case "${option}"
    in
      n)
        NAMESPACE=${OPTARG}
        ;;
      c)
        CHART=${OPTARG}
        ;;
      v)
        PATH_TO_VALUES=${OPTARG}
        ;;
      b)
        BACKUP_PATH=${OPTARG}
        ;;
      r)
        RELEASE=${OPTARG}
        ;;
      t)
        TIMEOUTS=${OPTARG}
        ;;
      p)
        EXPECTED_DBS=${OPTARG}
        ;;
      f)
        ROLLBACK_REVISION=${OPTARG}
        ;;
      s)
        EXTRA_PARAMS=${OPTARG}
        ;;
      h)
        usage
        ;;
      *)
        echo "Invalid option: -$OPTARG" >&2
        usage
  esac
done

KUBE_CMD=kubectl
HELM_CMD=helm
DATABASE_STRING="postgres|database"
DATA_PVC_PREFIX=pg-data
DATA_PVC_BACKUP_PREFIX=backup-data
EXPECTED_DBS=${EXPECTED_DBS:-4}
TIMEOUTS=${TIMEOUTS:-420}

if [ -z ${ROLLBACK_REVISION+x} ]; then
    echo "Rollback Revision needs to be specified or set to \"skip\"."
    usage
fi
ROLLBACK_REVISION=$( echo ${ROLLBACK_REVISION} | tr '[:upper:]' '[:lower:]' )

if [ -z ${NAMESPACE+x} ]; then
    echo "Namespace required"
    usage
fi

if [ -z ${PATH_TO_VALUES+x} ]; then
  PATH_TO_VALUES="--reuse-values"
else
  PATH_TO_VALUES="--values ${PATH_TO_VALUES}"
fi

# Add the deployment values to one variable
if [ -z ${EXTRA_PARAMS+x} ]; then
  VALUES="$PATH_TO_VALUES"
else
  VALUES="$PATH_TO_VALUES --set ${EXTRA_PARAMS}"
fi

if [ -z ${BACKUP_PATH+x} ]; then
    BACKUP_PATH=/mnt/pg_backup/pg_data_1
fi

DB_PVC_BACKUP_DATA=$($KUBE_CMD get pvc -n $NAMESPACE -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' | egrep "$DATABASE_STRING" | grep "${DATA_PVC_BACKUP_PREFIX}")
DB_STATEFULSETS=$($KUBE_CMD get statefulsets -n $NAMESPACE -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' | egrep "$DATABASE_STRING")
DB_PVC_DATA=$($KUBE_CMD get pvc -n $NAMESPACE -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' | grep "$DATA_PVC_PREFIX")
DATA_PVCS=""
RESTORE_VALUES=""
STATEFULSET_INLINE=""

for DB in $DB_PVC_BACKUP_DATA; do
  DB_POD_SHORT=$( echo ${DB} | sed 's/'"${DATA_PVC_BACKUP_PREFIX}-"'//' )
  RESTORE_VALUES=" ${RESTORE_VALUES} --set ${DB_POD_SHORT%??}.persistence.backup.existingClaim=${DB} --set ${DB_POD_SHORT%??}.restore.backupDataDir=${BACKUP_PATH} "
done

helm_rollback()
{
  echo "------------------------------------------------------------------------------------------"
  echo "STEP 1: Helm Rollback"

  if [[ ${ROLLBACK_REVISION} != "skip" ]]; then
    if [ $( helm history ${RELEASE} | wc -l ) -ge 3 ]; then
      echo "${HELM_CMD} rollback ${RELEASE} ${ROLLBACK_REVISION} --wait --recreate-pods --timeout ${TIMEOUTS}"
      ${HELM_CMD} rollback ${RELEASE} ${ROLLBACK_REVISION} --wait --recreate-pods --timeout ${TIMEOUTS}
      if [ $? -eq 1 ]; then
        echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
        echo "Failed: Helm rollback For Release, \"${RELEASE}\""
        echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
        exit 1
      fi
    else
      echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
      echo "WARNING Helm rollback failed - No Pre Existing Release"
      echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    fi
  else
    echo "Skipping Stage, Rollback Revision set to ${ROLLBACK_REVISION}"
  fi
  echo "-------------------------------------------------------------------------------------------"
}

check_number_backup_dbs()
{
  # Check to ensure the returned PODs are the number expected
  if [ $( echo "$DB_PVC_BACKUP_DATA" | wc -l ) != ${EXPECTED_DBS} ]; then
    printf "\nThe number of BACKUP DB's Returned, $( echo "$DB_PVC_BACKUP_DATA" | wc -l ), is not the number expected, ${EXPECTED_DBS}.\n"
    printf "Please Investigate, search for PODS with string, \"$DATABASE_STRING\".\n"
    printf "There should be ${EXPECTED_DBS} PODS in total returned.\n\n"
    printf "EXITING!!!\n\n"
    exit 1
  fi
}

delete_statefulsets()
{
  echo "------------------------------------------------------------------------------------------"

  echo "STEP 2: Delete DB statefulsets"

  for DB_STATEFULSET in $DB_STATEFULSETS
  do
    STATEFULSET_INLINE="${STATEFULSET_INLINE} ${DB_STATEFULSET}"
  done
  if [ ! -z ${STATEFULSET_INLINE+x} ]; then
    ${KUBE_CMD} delete statefulset $STATEFULSET_INLINE -n $NAMESPACE
    echo "Statefulsets ${STATEFULSET_INLINE} deleted"
  else
    echo "NO Statefulsets Found to be deleted"
  fi
  echo "--------------------------------------------------------------------------------------------"
}

delete_pvcs()
{
  echo "------------------------------------------------------------------------------------------"
  echo "STEP 3: Delete Data PVCs"

  for PVC in $DB_PVC_DATA; do
    DATA_PVCS="${DATA_PVCS} ${PVC}"
  done
  if [ ! -z ${DATA_PVCS+x} ]; then
    ${KUBE_CMD} delete pvc $DATA_PVCS -n $NAMESPACE
    echo "PVCs ${DATA_PVCS} deleted"
  else
    echo "NO DATA PVCs to deleted"
  fi
  echo "----------------------------------------------------------------------------------------------"
}

restore()
{
  echo "------------------------------------------------------------------------------------------"
  echo "STEP 4: Restore"

  echo "${HELM_CMD} upgrade --install --debug --wait
        --timeout ${TIMEOUTS}
        --force
        --namespace $NAMESPACE
        $VALUES
        $RELEASE
        $CHART
        $RESTORE_VALUES"

  ${HELM_CMD} \
      upgrade \
      --install \
      --debug \
      --wait \
      --timeout ${TIMEOUTS} \
      --force \
      --namespace $NAMESPACE \
      $VALUES \
      $RELEASE \
      $CHART \
      $RESTORE_VALUES

  if [ $? -eq 1 ]; then
    echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo "Restore failed"
    echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
  else
    DEPLOYED_CHART=$(${HELM_CMD} list ${RELEASE} --output json |  grep -Po '"Chart":.*?[^\\]",'| awk -F':' '{print $2}' | tr -d '"' | tr -d ',')
    echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
    echo "Restored to Chart version ${DEPLOYED_CHART}"
    echo "+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
  fi

  echo "----------------------------------------------------------------------------------------------"
}

check_number_backup_dbs
helm_rollback
delete_statefulsets
delete_pvcs
restore
