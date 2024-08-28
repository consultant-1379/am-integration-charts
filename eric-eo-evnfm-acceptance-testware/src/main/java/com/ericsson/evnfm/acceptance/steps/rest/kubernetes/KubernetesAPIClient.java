/*******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.evnfm.acceptance.steps.rest.kubernetes;

import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getFileResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import io.fabric8.kubernetes.api.model.GenericKubernetesResourceBuilder;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;

import com.ericsson.evnfm.acceptance.models.configuration.CustomResourceInfo;
import com.ericsson.evnfm.acceptance.utils.FileUtilities;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionSpec;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinitionVersion;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.ReplicaSet;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;

public class KubernetesAPIClient {

    private static final Logger LOGGER = getLogger(KubernetesAPIClient.class);
    private KubernetesClient client;
    private String namespace;

    public KubernetesAPIClient(String kubeConfig) {
        if (kubeConfig != null) {
            try {
                FileSystemResource fileResource = getFileResource(kubeConfig);
                String kubeConfigContent = FileUtilities.getFileContentFromInputStream(fileResource.getInputStream());
                Config config = Config.fromKubeconfig(kubeConfigContent);
                client = new KubernetesClientBuilder().withConfig(config).build();
            } catch (IOException e) {
                LOGGER.error("Failed to create Kubernetes client with config file. Default configuration is used. :: {}", e.getMessage());
                client = new KubernetesClientBuilder().build();
            }
        } else {
            client = new KubernetesClientBuilder().build();
        }
    }

    public KubernetesAPIClient(String kubeConfig, String namespace) {
        this(kubeConfig);
        this.namespace = namespace;
    }

    public ConfigMap getConfigmap(String namespace, String name) {
        return client.configMaps().inNamespace(namespace).withName(name).get();
    }

    public Pod getPod(String namespace, String name) {
        return client.pods().inNamespace(namespace).withName(name).get();
    }

    public Deployment getDeployment(String name) {
        return client.apps().deployments().inNamespace(namespace).withName(name).get();
    }

    public StatefulSet getStatefulSet(String name) {
        return client.apps().statefulSets().inNamespace(namespace).withName(name).get();
    }

    public ReplicaSet getReplicaSet(String name) {
        return client.apps().replicaSets().inNamespace(namespace).withName(name).get();
    }

    public Integer getReplicaCount(Deployment deployment) {
        return deployment.getSpec().getReplicas();
    }

    public Integer getReplicaCount(StatefulSet statefulSett) {
        return statefulSett.getSpec().getReplicas();
    }

    public Integer getReplicaCount(ReplicaSet replicaSet) {
        return replicaSet.getSpec().getReplicas();
    }

    public List<Pod> getPodsByLabelsAndStatus(String namespace, Map<String, String> labels, String status) {
        PodList pods = client.pods().inNamespace(namespace).withLabels(labels).list();
        if(status != null && !status.isEmpty()) {
            return pods.getItems().stream()
                       .filter(pod -> status.equals(pod.getStatus().getPhase()))
                       .collect(Collectors.toList());
        }
        return pods.getItems();
    }

    public CustomResourceDefinition createOrReplaceCustomResourceDefinition(String path) {
        CustomResourceDefinition customResourceDefinition = loadCustomResourceDefinitionYaml(path);
        client.apiextensions().v1().customResourceDefinitions().resource(customResourceDefinition).createOrReplace();
        return customResourceDefinition;
    }

    public <T> void createCustomResource(CustomResourceDefinition customResourceDefinition, CustomResourceInfo customResourceInfo,
                                                 T resource) {
        CustomResourceDefinitionSpec spec = customResourceDefinition.getSpec();
        CustomResourceDefinitionContext crdContext = initCustomResourceDefinitionContext(customResourceDefinition);
        Map<String, Object> crMetadata = customResourceInfo.getCrMetadata();
        GenericKubernetesResource cr = new GenericKubernetesResourceBuilder()
                .withNewMetadata()
                .withName((String) crMetadata.get("name"))
                .endMetadata()
                .withKind(spec.getNames().getKind())
                .withApiVersion(spec.getGroup() + "/" + crdContext.getVersion())
                .withAdditionalProperties(Map.of("spec", resource))
                .build();

        client.genericKubernetesResources(crdContext).inNamespace(namespace).resource(cr).createOrReplace();
    }

    public GenericKubernetesResource getCustomResource(CustomResourceDefinition customResourceDefinition, CustomResourceInfo customResourceInfo) {
        CustomResourceDefinitionContext crdContext = initCustomResourceDefinitionContext(customResourceDefinition);
        String crName = customResourceInfo.getCrMetadata().get("name").toString();
        return client.genericKubernetesResources(crdContext).inNamespace(namespace).withName(crName).get();
    }

    public void deleteCustomResource(CustomResourceDefinition customResourceDefinition, CustomResourceInfo customResourceInfo) {
        CustomResourceDefinitionContext crdContext = initCustomResourceDefinitionContext(customResourceDefinition);
        String crName = customResourceInfo.getCrMetadata().get("name").toString();

        client.genericKubernetesResources(crdContext).inNamespace(namespace).withName(crName).delete();
    }

    public CustomResourceDefinition loadCustomResourceDefinitionYaml(String path) {
        return client.apiextensions().v1().customResourceDefinitions().load(getFileResource(path).getFile()).get();
    }

    private CustomResourceDefinitionContext initCustomResourceDefinitionContext(CustomResourceDefinition customResourceDefinition) {
        CustomResourceDefinitionSpec spec = customResourceDefinition.getSpec();
        CustomResourceDefinitionVersion crdVersionObj = spec.getVersions().stream().findFirst().orElseThrow(
                                                                        () -> new RuntimeException("CRD versions not found."));
        return new CustomResourceDefinitionContext.Builder()
                .withVersion(crdVersionObj.getName())
                .withScope(spec.getScope())
                .withGroup(spec.getGroup())
                .withPlural(spec.getNames().getPlural())
                .build();
    }
}
