# Application Manager Onboarding & Common WF Services

[TOC]

## Overview
This chart installs both the Application Manager Onboarding and Common Workflow Services all in one go. To read more information on these services, please inspect the README.md files for each service. These files are located in their respective sub-directories in the charts/ folder.

## Configuration and Deployment of the Onboarding and Common WF Services
The default values.yaml for the Onboarding Service invokes the deployment of ADP-provided registries for helm charts
and Docker images (eric-lcm-helm-chart-registry and eric-lcm-container-registry respectively) during a helm install of this integration chart.

**Note:** For this release of Application Manager Onboarding Service, the attached helm and Docker registries are the only ones currently
supported. Their deployment is controlled by the `deploy-docker-registry.enabled` and `deploy-helm-registry.enabled` values, both of which must remain
true.

To specify helm chart values used in the deployment of the Onboarding & Common Workflow Services to a Kubernetes cluster, use the
following syntax structure:

```
helm install <integrationChart> --set key=value,key=value
```

## Onboarding Service Requirements

### Onboarding Service Ingress Values
* **eric-am-onboarding-service.ingress.host** must be specified with the ingress URI for the Onboarding Service for deployment.
* **eric-am-onboarding-service.ingress.enabled** is true by default. It is not recommended to disable the ingress for the Onboarding Service.

### Onboarding Service Registry Ingress Values
In order for the default registries of the Onboarding Service to be accessible from outside the Kubernetes cluster, it is necessary for ingress to
be enabled.
By default, the ingress for Docker registry is set to enabled, as instantiation happens on worker nodes which can see internal Docker registry
address.
The ingress for the helm registry is set to disabled as all interaction with helm registry is internal to the cluster

### Onboarding Service Docker Registry Ingress Values
* ***eric-lcm-container-registry.ingress.host*** must be set to the desired host name of the Docker registry service which includes cluster
details.
     e.g. test.clusterIngressHostAddress

### Onboarding Service Docker Registry Ingress TLS secret name
* ***eric-lcm-container-registry.ingress.tls.secretName*** must be set to the secret name that has the container registry certificate.
A secret with the correct tls cert and tls key is required. It is advised to use a proper signed certificate with a valid CA.
* If user does not have a certificate signed by proper CA, a self-signed certificate can be used. Follow the note section to create a self-signed certificate.

**Note:** It is not advisable to use self-signed certificate.

Steps to create self-signed certificate for container registry.

Execute following commands to generate root CA certificate and certificate key:
```
 openssl genrsa -out ca-key.pem 4096
 openssl req -new -x509 -key ca-key.pem -days 10950 -out ca.pem -subj "/C=CH/ST=SH/L=SUSE/O=CBC/CN=eric.com"
```

Execute following commands to generate container registry certificate signing request and certificate key:
```
 openssl genrsa -out server-key.pem 4096
 openssl req -new -key server-key.pem -out server.csr -days 10950 -subj "/C=CH/ST=SH/L=SUSE/O=CBC/CN=<container-registry-ingress-hostname"
```

Execute below command to sign the server certificate with the created CA
```
 openssl x509 -req -in server.csr -CA ca.pem -CAkey ca-key.pem -CAcreateserial -days 10950 -out ca.crt
```


### Steps to create the secrets for the container registry
* Certificate used for creation of secret should use the common name value as the ingress name of the container registry.
* User can create the secret with tls cert and tls key using the command:
***kubectl create secret tls <SECRET_NAME> --key <key-file> --cert <cert-file> --namespace <namespace_name>***

```
kubectl create secret tls container-registry-secret --key server-key.pem --cert ca.crt --namespace <namespace_name>
```
* User has to copy this certificate manually to all the worker nodes

### Steps to copy the container registry certificate to all worker nodes
* Login to all worker nodes and create a directory with name
```
/etc/docker/certs.d/<ingress_host_name_container_registry>
```
* Copy the cert to /etc/docker/certs.d/<ingress_host_name_container_registry> directory

## Common WFS Requirements

### Prerequisites
Successful deployment of Application Manager Common Workflow Service requires that both Helm and kubectl are installed on the worker nodes of the Kubernetes cluster.
It is recommended to have the same version of Helm for both the client and the server (tiller).

### Common WFS Service Account Requirements
In order for the Common Workflow Service to be deployed, Helm and kubectl require the .kube configuration file in order to communicate to Kubernetes. Once the Common Workflow Service container is deployed, it will then configure Kubernetes.
This requires a Service Account to be set up with the correct privileges. A Cluster Role Binding grants these privileges cluster-wide to a role - which references the Service Account.

**Note:** A Service Account and Cluster Role Binding are required for the Common Workflow Service to be deployed correctly.

### Common WFS Helm Location Values
The location of the Helm binary on the worker nodes must be specified. The default location of the Helm binary is set to /usr/local/bin/helm .
If Helm is located elsewhere on the nodes:
* **eric-am-common-wfs.helm.location** must be set to the target location.

### Common WFS Kubectl Location Values
The location of kubectl on the worker nodes must be specified. The default location of kubectl is set to /usr/local/bin/kubectl .
If kubectl is located elsewhere on the nodes:
* **eric-am-common-wfs.kube.location** must be set to the target location.

### Common WFS Service Account Values
The Service Account name must be provided at installation time and it must be within the same namespace as where the application will be deployed.
* **eric-am-common-wfs.service.account** must be set to the desired name of the Service Account.

### Common WFS Ingress Values
* **eric-am-common-wfs.ingress.host** must be specified with the ingress URI for the Common Workflow Service for deployment.
* **eric-am-common-wfs.ingress.enabled** is true by default. It is not recommended to disable the ingress for the Common Workflow Service.

## Example Helm Install Command
This integration chart deploys both eric-am-onboarding-service and eric-am-common-wfs. The recommendation when deploying application manager using this integration chart is to set autoConfigureDocker.enabled and autoConfigureHelm.enabled for eric-am-common-wfs to true.
By default autoConfigureDocker.enabled and autoConfigureHelm.enabled are set to true for eric-eo-evnfm in the values.yaml file. By setting autoConfigureDocker.enabled and autoConfigureHelm.enabled, eric-am-onboarding-service will use the helm and docker registries deployed by eric-am-onboarding-service.

Please see below example Helm install command with all --set values required for this helm chart:

```
helm install eric-eo-evnfm-0.0.1.tgz --namespace default --name eric-am-onboarding-and-common-workflow-services --set eric-am-onboarding-service.ingress.host=${OnboardingServiceAddress},eric-lcm-container-registry.ingress.host=${ContainerRegistryAddress},
eric-am-common-wfs.ingress.host=${CommonWorkflowServiceAddress},eric-am-common-wfs.service.account=${ServiceAccountName},eric-lcm-container-registry.ingress.tls.secretName=${ContainerRegistryTLSSecretName}
```

If you wish to override the default value for autoConfigureDocker.enabled and autoConfigureHelm.enabled parameters you can either:
1. Update the values.yaml file.
2. Append ',eric-am-common-wfs.autoConfigureDocker.enabled={booleanValue}, eric-am-common-wfs.autoConfigureHelm.enabled={booleanValue}' to the above helm command.

***Note:*** When deploying application manager using this helm chart it is not advised to set autoConfigureDocker.enabled or autoConfigureHelm.enabled to false.
