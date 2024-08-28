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
  echo "Usage: $0 -n <Kubernetes Namespace> [-b backup/path] [-s IDAM/Keycloak Secret Name] [-p Number of PODS]"
  echo ""
  echo "             -n          Kubernetes Namespace used for the deployment"
  echo "  (optional) -b          Backup path, location to store the postgres DB Backup  [default: '/mnt/pg_backup/pg_data_1']"
  echo "  (optional) -s          IDAM/Keycloak Secret name set during initial installation  [default: 'eric-sec-access-mgmt-creds']"
  echo "  (optional) -p          Number of DB's & Postgres PODS Expected  [default: '4']"
  echo ""
  exit 1
}

while getopts ":n:b:s:p:h:" option; do
  case "${option}" in
    n)
      NAMESPACE=${OPTARG}
      ;;
    b)
      BACKUP_PATH=${OPTARG}
      ;;
    s)
      SECRET_NAME=${OPTARG}
      ;;
    p)
      EXPECTED_PODS=${OPTARG}
      ;;
    h)
      usage
      ;;
    *)
      echo "Invalid option: -$OPTARG" >&2
      usage
      ;;
  esac
done
shift $((OPTIND-1))

if [ -z ${NAMESPACE+x} ]; then
    echo "Invalid option: Namespace required"
    usage
fi

BACKUP_PATH=${BACKUP_PATH:-/mnt/pg_backup/pg_data_1}
SECRET_NAME=${SECRET_NAME:-eric-sec-access-mgmt-creds}
EXPECTED_PODS=${EXPECTED_PODS:-4}
KUBE_CMD=kubectl
DATABASE_STRING="postgres|database"
PG_PASS="*:*:*:replica:replica"

echo "......."
${KUBE_CMD} get pods -n $NAMESPACE

PODS=$($KUBE_CMD get pods -n $NAMESPACE -o go-template --template '{{range .items}}{{.metadata.name}}{{"\n"}}{{end}}' | egrep "$DATABASE_STRING")
# Check to ensure the returned PODs are the number expected
if [ $( echo "$PODS" | wc -l ) != ${EXPECTED_PODS} ]; then
  printf "\nThe number of PODS returned, $( echo "$PODS" | wc -l ), is not the number expected, ${EXPECTED_PODS}.\n"
  printf "Please investigate, search for PODS with string, \"$DATABASE_STRING\".\n"
  printf "There should be ${EXPECTED_PODS} PODS in total returned.\n\n"
  printf "EXITING!!!\n\n"
  exit 1
fi
echo "......."
echo "Backing up the following DB's"
printf "$PODS\n\n"

for POD in $PODS
do
    RETRY_COUNT=0
    DB_CONTAINER=${POD%??}
    while [ $RETRY_COUNT -lt 5 ]
    do
      if [[ ${POD} == *"database"* ]]; then
        PG_PASS="*:*:*:admin:$( kubectl get secret ${SECRET_NAME} -n $NAMESPACE -o go-template='{{.data.pgpasswd | base64decode}}' )"
      else
        PG_PASS="*:*:*:replica:replica"
      fi
      if [ $? != 0 ]; then
        echo "Issue with the execution of the backup, unable to retrieve secret for POD \"${POD}\" using secret name, \"${SECRET_NAME}\". Please investigate..."
        echo "EXITING!!!\n\n"
        exit 1
      fi
      echo "------ Backing Up PG data in container $DB_CONTAINER for pod: $POD---------------"
      ${KUBE_CMD} exec -it $POD -c ${DB_CONTAINER} --namespace $NAMESPACE  -- bash -c "echo ${PG_PASS} > ~/.pgpass && \
      chmod 0600 ~/.pgpass && \
      if [ -d ${BACKUP_PATH} ]; then \
        echo 'Backing up old Backup to ${BACKUP_PATH}_bck'; \
        mv ${BACKUP_PATH} ${BACKUP_PATH}_bck; \
        rm -rf ${BACKUP_PATH}; \
      fi; \
      /var/lib/postgresql/scripts/data_backup.sh -D ${BACKUP_PATH};  \
      if [ $? -eq 1 ]; then \
      echo '+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'; \
      echo 'Backup failed for ${POD}'; \
      rm -rf ~/.pgpass; \
      echo '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'; \
      exit 1; \
      else echo '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'; \
      echo 'backup success for ${POD}'; \
      if [ -d ${BACKUP_PATH}_bck ]; then \
        echo 'Removing Old Backup ${BACKUP_PATH}_bck'; \
        rm -rf ${BACKUP_PATH}_bck; \
      fi; \
      rm -rf ~/.pgpass; \
      echo '++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++'; \
      fi   "
      if [ $? -eq 1 ]; then
        RETRY_COUNT=$(( RETRY_COUNT + 1 ))
        echo "---           ----            ----          ----      -----"
        echo "Retrying backup for ${POD} ---- ${RETRY_COUNT} of 5"
        echo "---           ----            ----          ----      -----"
      else
        break
      fi
    done
done
echo "-------------------------------------------------------------"
echo "Finished"
echo "-------------------------------------------------------------"

