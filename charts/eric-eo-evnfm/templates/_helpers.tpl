{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-eo-evnfm.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create release name used for cluster role.
*/}}
{{- define "eric-eo-evnfm.release.name" -}}
{{- default .Release.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}


{{/*
Create Ericsson product app.kubernetes.io info
*/}}
{{- define "eric-eo-evnfm.kubernetes-io-info" -}}
{{- include "eric-eo-evnfm-library-chart.kubernetes-io-info" . -}}
{{- end -}}

{{/*
Create Ericsson Product Info
*/}}
{{- define "eric-eo-evnfm.helm-annotations" -}}
{{- include "eric-eo-evnfm-library-chart.helm-annotations" . -}}
{{- end}}


{{/*
Create keycloak-client image pull secrets
*/}}
{{- define "eric-eo-evnfm.keycloak-client.pullSecrets" -}}
  {{- include "eric-eo-evnfm-library-chart.pullSecrets" . -}}
{{- end -}}

{{/*
Create keycloak-client image registry url
*/}}
{{- define "eric-eo-evnfm.keycloak-client.registryUrl" -}}
  {{- include "eric-eo-evnfm-library-chart.mainImagePath" (dict "ctx" . "svcRegistryName" "keycloakClient") -}}
{{- end -}}


{{- define "eric-eo-evnfm.nodeSelector" -}}
  {{- include "eric-eo-evnfm-library-chart.nodeSelector" . -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Kubernetes labels
*/}}
{{- define "eric-eo-evnfm.kubernetes-labels" -}}
app.kubernetes.io/name: {{ include "eric-eo-evnfm.name" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ include "eric-eo-evnfm.version" . }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-eo-evnfm.labels" -}}
  {{- $kubernetesLabels := include "eric-eo-evnfm.kubernetes-labels" . | fromYaml -}}
  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $kubernetesLabels $globalLabels $serviceLabels)) }}
{{- end -}}

{{/*
Merged labels for extended defaults
*/}}
{{- define "eric-eo-evnfm.labels.extended-defaults" -}}
  {{- $extendedLabels := dict -}}
  {{- $_ := set $extendedLabels "app" (include "eric-eo-evnfm.name" .) -}}
  {{- $_ := set $extendedLabels "chart" (include "eric-eo-evnfm.chart" .) -}}
  {{- $_ := set $extendedLabels "release" (.Release.Name) -}}
  {{- $_ := set $extendedLabels "heritage" (.Release.Service) -}}
  {{- $commonLabels := include "eric-eo-evnfm.labels" . | fromYaml -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $commonLabels $extendedLabels)) | trim }}
{{- end -}}


{{/*
Create Ericsson product specific annotations
*/}}
{{- define "eric-eo-evnfm.helm-annotations_product_name" -}}
{{- include "eric-eo-evnfm-library-chart.helm-annotations_product_name" . -}}
{{- end -}}
{{- define "eric-eo-evnfm.helm-annotations_product_number" -}}
{{- include "eric-eo-evnfm-library-chart.helm-annotations_product_number" . -}}
{{- end -}}
{{- define "eric-eo-evnfm.helm-annotations_product_revision" -}}
{{- include "eric-eo-evnfm-library-chart.helm-annotations_product_revision" . -}}
{{- end -}}

{{/*
Create a dict of annotations for the product information (DR-D1121-064, DR-D1121-067).
*/}}
{{- define "eric-eo-evnfm.product-info" }}
ericsson.com/product-name: {{ template "eric-eo-evnfm.helm-annotations_product_name" . }}
ericsson.com/product-number: {{ template "eric-eo-evnfm.helm-annotations_product_number" . }}
ericsson.com/product-revision: {{ template "eric-eo-evnfm.helm-annotations_product_revision" . }}
{{- end }}

{{/*
Common annotations
*/}}
{{- define "eric-eo-evnfm.annotations" -}}
  {{- $productInfo := include "eric-eo-evnfm.product-info" . | fromYaml -}}
  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- include "eric-eo-evnfm-library-chart.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $globalAnn $serviceAnn)) | trim }}
{{- end -}}

{{/*
Create global container registry (Geo-Red)
*/}}
{{- define "eric-eo-evnfm.global-container-registry.enabled" -}}
  {{- $crEnabledKeyExists := false -}}
  {{- if hasKey .Values "eric-lcm-container-registry" -}}
    {{- if hasKey (index .Values "eric-lcm-container-registry") "enabled" -}}
      {{- index .Values "eric-lcm-container-registry" "enabled" -}}
      {{- $crEnabledKeyExists = true -}}
    {{- end -}}
  {{- end -}}
  {{- if not $crEnabledKeyExists -}}
    {{- if hasKey .Values "services" -}}
      {{- if hasKey .Values.services "onboarding" -}}
        {{- if hasKey .Values.services.onboarding "enabled" -}}
          {{- index .Values "services" "onboarding" "enabled" -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}


{{/*
DR-D470217-007-AD This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-eo-evnfm.service-mesh-enabled" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-enabled" . -}}
{{- end -}}


{{/*
DR-D470217-011 This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-eo-evnfm.service-mesh-inject" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-inject" . -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-eo-evnfm.service-mesh-version" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-version" . -}}
{{- end -}}

{{/*
Create Service Mesh Ingress enabling option
*/}}
{{- define "eric-eo-evnfm.service-mesh-ingress-enabled" -}}
{{ if .Values.global.serviceMesh }}
  {{ if .Values.global.serviceMesh.ingress }}
    {{ if .Values.global.serviceMesh.ingress.enabled }}
  {{- print "true" -}}
    {{ else }}
  {{- print "false" -}}
    {{- end -}}
  {{ else }}
  {{- print "false" -}}
  {{- end -}}
{{ else }}
  {{- print "false" -}}
{{ end }}
{{- end}}