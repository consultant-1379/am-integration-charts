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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.delay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigMapVerify;
import com.ericsson.evnfm.acceptance.models.configuration.Configuration;
import com.ericsson.evnfm.acceptance.models.configuration.CustomResourceInfo;
import com.ericsson.vnfm.orchestrator.model.ComputeResource;
import com.ericsson.vnfm.orchestrator.model.InstantiatedVnfInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfcResourceInfo;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;

public class KubernetesAPISteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesAPISteps.class);

    private KubernetesAPISteps() {
    }

    public static List<ConfigMapVerify> verifyConfigMaps(List<Configuration> configurations,
                                                         Map<String, Object> additionalAttributes,
                                                         String namespace, String clusterConfig) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig);
        List<ConfigMapVerify> configMapVerifies = new ArrayList<>();
        if (configurations != null) {
            KubernetesAPIClient finalClient = client;
            configurations.forEach(configuration -> {
                LOGGER.info("Fetching configMap {} to verify data", configuration.getName());
                ConfigMap configMap = finalClient.getConfigmap(namespace, configuration.getName());
                LOGGER.info("ConfigMap is: {} ", configMap);
                Optional<Map<String, String>> configData = Optional.ofNullable(configMap.getData());
                if (configData.isPresent()) {
                    configuration.getVerify().forEach(data -> {
                        String configMapFieldData = configData.get().get(data.getKey());
                        String attributeData = additionalAttributes.get(data.getAttributeKey()).toString();
                        configMapVerifies.add(new ConfigMapVerify(attributeData, configMapFieldData));
                    });
                } else {
                    throw new RuntimeException(String.format("No data found in ConfigMap %s in namespace %s",
                                                             configMap.getMetadata().getName(),
                                                             namespace));
                }
            });
        }
        return configMapVerifies;
    }

    public static void verifyAnnotations(VnfInstanceResponse vnfInstance, String namespace, String clusterConfig) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig);
        Optional<InstantiatedVnfInfo> instantiatedVnfInfo = Optional.ofNullable(vnfInstance.getInstantiatedVnfInfo());
        if (instantiatedVnfInfo.isPresent()) {
            List<VnfcResourceInfo> vnfcResourcesInfo = instantiatedVnfInfo.get().getVnfcResourceInfo();
            vnfcResourcesInfo.forEach(resourceInfo -> {
                ComputeResource computeResource = resourceInfo.getComputeResource();
                String podName = computeResource.getResourceId();
                JSONObject additionalResourceInfo = new JSONObject((Map) computeResource.getVimLevelAdditionalResourceInfo());
                Optional<Pod> pod = Optional.ofNullable(client.getPod(additionalResourceInfo.getString("namespace"), podName));
                if (pod.isPresent()) {
                    LOGGER.info("Fetching annotations for pod: {} to verify data.", podName);
                    Optional<ObjectMeta> podMetadata = Optional.ofNullable(pod.get().getMetadata());
                    if (podMetadata.isPresent()) {
                        Map<String, String> podAnnotations = podMetadata.get().getAnnotations();
                        Map<String, String> resourceAnnotations = additionalResourceInfo.getJSONObject("annotations")
                                .toMap()
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getValue() instanceof String)
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
                        assertThat(resourceAnnotations).containsAllEntriesOf(podAnnotations);
                    } else {
                        throw new RuntimeException(String.format("No metadata found in pod: {} in namespace: {}", podName, namespace));
                    }
                } else {
                    throw new RuntimeException(String.format("No pod found with name: %s in namespace : %s", podName, namespace));
                }
            });
        }
    }

    public static void verifyPodsCountByStatus(String clusterConfig, String timeout, String namespace, String status, Map<String, String> labels,
                                               int expectedCount) {
        int applicationTimeOut = timeout == null ? 600 : Integer.parseInt(timeout);
        StopWatch stopwatch = StopWatch.createStarted();
        int podsCount;
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig);
        LOGGER.info("Performing query of pods in namespace {} with labels {} status {}\n", namespace, labels, status);
        while (stopwatch.getTime(TimeUnit.SECONDS) < applicationTimeOut) {
            List<Pod> pods = client.getPodsByLabelsAndStatus(namespace, labels, status);
            podsCount = pods.size();
            LOGGER.info("Pods are running in status {}: actual {}, expected {}", status, podsCount, expectedCount);
            if (podsCount == expectedCount) {
                return;
            }
            delay(5000);
        }
        fail(String.format("Pods in status %s failed to reach expected count %d within timeout %d seconds", status, expectedCount,
                           applicationTimeOut));
    }

    public static CustomResourceDefinition createOrReplaceCustomResourceDefinition(String clusterConfig, CustomResourceInfo customResourceInfo) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig);
        return client.createOrReplaceCustomResourceDefinition(customResourceInfo.getCrdYamlPath());
    }

    public static void createCustomResource(String clusterConfig, String namespace, CustomResourceDefinition crd,
                                            CustomResourceInfo customResourceInfo,
                                            VnfInstanceResponse vir) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig, namespace);
        client.createCustomResource(crd, customResourceInfo, vir);
    }

    public static GenericKubernetesResource getCustomResource(String clusterConfig, String namespace, CustomResourceDefinition crd,
                                                        CustomResourceInfo customResourceInfo) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig, namespace);
        return client.getCustomResource(crd, customResourceInfo);
    }

    public static void deleteCustomResource(String clusterConfig, String namespace, CustomResourceDefinition crd,
                                            CustomResourceInfo customResourceInfo) {
        KubernetesAPIClient client = new KubernetesAPIClient(clusterConfig, namespace);
        client.deleteCustomResource(crd, customResourceInfo);
    }
}
