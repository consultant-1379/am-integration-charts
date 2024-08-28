#!/usr/bin/env bash

ALARM_LIST=$(
cat <<-END
curl -X GET http://eric-fh-alarm-handler:5005/alarm-handling/v1beta1/alarms
echo
END
)

STRUCTURE_REGEXP='"[^"]*" *(: *([0-9]*|"[^"]*")[^{}\["]*|,)?|[^"\]\[\}\{]*|\{|\},?|\[|\],?|[0-9 ]*,?'
ALIGNMENT_REGEX='{if ($0 ~ /^[}\]]/ ) offset-=4; printf "%*c%s\n", offset, " ", $0; if ($0 ~ /^[{\[]/) offset+=4}'
GREP_ID='"id":"\K[^"]*'
GREP_FUNCTIONAL='{[^{}]*"serviceName":"eric-eo-cvnfm"[^{}]*}'

function showHelp() {
  echo '                         Key Value        Description'
  echo 'Flag/Configuration:'
  echo '                         -n  String       Namespace scope | Mandatory'
  echo 'Flags/Actions:'
  echo '                         -h  Not expect   Show Help'
  echo '                         -l  Not expect   List of the Alarms'
  echo '                         -c  Not expect   Clear all Alarms'
  echo '                         -r  Not expect   Clear all functional Alarms'
  echo '                         -d  String       Clear Alarm by id'
}

if [[ $# -eq 0 ]]; then
  showHelp
  echo "No arguments specified"
  exit 1
fi

declare -A arguments

while [[ $# -gt 0 ]]; do
  if [[ $1 == -* ]]; then
    key=$1
    if ! [[ $2 == -* ]]; then
      value=$2
      shift
    else
      value=""
    fi
  fi
  arguments["$key"]="$value"
  shift
done

if [[ ${arguments["-h"]+exists} ]]; then
    showHelp
    exit 1
fi

if ! [[ ${arguments["-n"]+exists} ]]; then
    showHelp
    echo "The -n key is missing"
    exit 1
fi

if ! [[ -n "${arguments['-n']}" ]]; then
  showHelp
  echo "The -n value is missing"
  exit 1
fi

ERIC_FH_ALARM_HANDLER=$(kubectl get pods --namespace="${arguments['-n']}" --no-headers -o custom-columns=":metadata.name" | grep -m 1 "^eric-eo-fh-event-to-alarm-adapter-")

if [[ -z "$ERIC_FH_ALARM_HANDLER" ]]; then
     echo "eric-eo-fh-event-to-alarm-adapter pod in ${arguments['-n']} namespace not found"
     exit 1
fi

if [[ ${arguments["-l"]+exists} ]]; then
  kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$ALARM_LIST" | grep -Eo "$STRUCTURE_REGEXP" | awk "$ALIGNMENT_REGEX"
  exit 0
fi

if [[ ${arguments["-c"]+exists} ]]; then
  ids=($(kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$ALARM_LIST" | grep -Po "$GREP_ID"))
  for id in "${ids[@]}"; do
    echo "Start clearance alarm with id = $id"
    command="curl -X POST -d '{\"alarmId\": \"$id\", \"clearanceReason\":\"REST based clearance\"}' http://eric-fh-alarm-handler:5005/alarm-handling/v1beta1/alarm-clearances"
    kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$command" | grep -Eo "$STRUCTURE_REGEXP" | awk "$ALIGNMENT_REGEX"
  done
  exit 0
fi

if [[ ${arguments["-r"]+exists} ]]; then
  ids=($(kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$ALARM_LIST" | grep -Po "$GREP_FUNCTIONAL" | grep -Po "$GREP_ID"))
  for id in "${ids[@]}"; do
    echo "Start clearance alarm with id = $id"
    command="curl -X POST -d '{\"alarmId\": \"$id\", \"clearanceReason\":\"REST based clearance\"}' http://eric-fh-alarm-handler:5005/alarm-handling/v1beta1/alarm-clearances"
    kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$command" | grep -Eo "$STRUCTURE_REGEXP" | awk "$ALIGNMENT_REGEX"
  done
  exit 0
fi

if [[ ${arguments["-d"]+exists} ]]; then
  if ! [[ -n "${arguments['-d']}" ]]; then
  showHelp
    echo "The -d value is missing in the passed arguments"
    exit 1
  fi

  command="curl -X POST -d '{\"alarmId\": \"${arguments['-d']}\", \"clearanceReason\":\"REST based clearance\"}' http://eric-fh-alarm-handler:5005/alarm-handling/v1beta1/alarm-clearances"
  kubectl exec -it --namespace=${arguments['-n']} $ERIC_FH_ALARM_HANDLER --container eric-eo-fh-event-to-alarm-adapter -- /bin/sh -c "$command" | grep -Eo "$STRUCTURE_REGEXP" | awk "$ALIGNMENT_REGEX"
  exit 0
fi

showHelp
echo "Specify the action flag"