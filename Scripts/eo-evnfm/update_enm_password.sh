#!/usr/bin/env bash

[[ -z "$1" ]] && echo "ERROR: namespace must be provided as argument\nExample:\n\$ $0 eo-namespace" && exit 1

NAMESPACE="$1"

kubectl get namespace "$NAMESPACE" >/dev/null 2>/dev/null
[[ "$?" -ne 0 ]] && echo -e "ERROR: cannot get namespace from cluster. Please check if namespace name is correct and kubectl has access to the cluster.\nCommand:\n\$ kubectl get namespace '$NAMESPACE'" && exit 2

ORCHESTRATOR_PODS="$( kubectl get pods --namespace "$NAMESPACE" -l app=eric-vnfm-orchestrator-service -o jsonpath='{range .items[*]}{.metadata.name}{" "}{end}' 2>/dev/null )"
[[ -z "$ORCHESTRATOR_PODS" ]] && echo -e "ERROR: namespace '$NAMESPACE' does not contain CVNFM.\nCommand to check:\n\$ kubectl get pods --namespace '$NAMESPACE' -l app=eric-vnfm-orchestrator-service'" && exit 3

echo "Creating/updating integration between CVNFM and ENM"

echo -n "Enter ENM user name: "
read ENM_USERNAME

echo -n "Enter user '$ENM_USERNAME' password: "
read -s ENM_PASSWORD
echo

echo -n "Enter ENM secret name: "
read ENM_SECRET

echo "Updating secret..."
echo "Patch password..."
kubectl patch secret "$ENM_SECRET"  --patch="{\"data\": { \"enm-scripting-password\": \"$(echo -n $ENM_PASSWORD |base64 -w0)\" }}" -n "$NAMESPACE"
if [[ "$?" -ne 0 ]]; then
    exit 1
  else
    new_password=$(kubectl get secret "$ENM_SECRET" -n "$NAMESPACE" --template='{{index .data "enm-scripting-password"}}' | base64 -d)
fi

echo "Patch User..."
kubectl patch secret "$ENM_SECRET"  --patch="{\"data\": { \"enm-scripting-username\": \"$(echo -n $ENM_USERNAME |base64 -w0)\" }}" -n "$NAMESPACE"
if [[ "$?" -ne 0 ]]; then
    exit 1
  else
    new_user=$(kubectl get secret "$ENM_SECRET" -n "$NAMESPACE" --template='{{index .data "enm-scripting-username"}}' | base64 -d)
fi

[[ "$new_password" == "$ENM_PASSWORD" && "$new_user" == "$ENM_USERNAME" ]] && echo "Secret has been updated successfully"

echo "Restarting pods..."
kubectl rollout restart deployment/eric-vnfm-orchestrator-service -n "$NAMESPACE"
echo "Done"