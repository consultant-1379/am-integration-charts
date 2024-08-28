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
package com.ericsson.evnfm.acceptance.steps.kubernetes.rest;

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigDay0;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.ComputeResource;
import com.ericsson.vnfm.orchestrator.model.InstantiatedVnfInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfcResourceInfo;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;

public class KubernetesSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(KubernetesSteps.class);

    private KubernetesSteps() {
    }

    public static void createNamespaceIfNotExists(String clusterConfig, String namespace) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        if (k8sClient.getNamespace(namespace) == null) {
            k8sClient.createNamespace(namespace);
        }
    }

    public static boolean deleteNamespace(String clusterConfig, String namespace) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        return k8sClient.deleteNamespace(namespace);
    }

    public static boolean deletePVCs(String clusterConfig, String namespace) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        return k8sClient.deletePVCs(namespace);
    }

    public static Namespace getNamespace(String clusterConfig, String namespace) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        return k8sClient.getNamespace(namespace);
    }

    public static PersistentVolumeClaimList getPVCs(String clusterConfig, String namespace) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        return k8sClient.getPVCs(namespace);
    }

    public static List<Namespace> getNamespaces(String clusterConfig) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        return k8sClient.getNamespaces();
    }

    public static List<String> getPodsNamesInNamespaceWithStringInName(String clusterConfig, String namespace, String partialPodName) {
        var k8sClient = new KubernetesApiClient(clusterConfig);
        List<Pod> pods = k8sClient.getPodsInNamespaceWithStringInName(namespace, partialPodName);
        return pods.stream().map(p -> p.getMetadata().getName()).collect(Collectors.toList());
    }

    public static boolean verifySecretsPresentInNamespace(Day0SecretVerificationInfo day0Info) {
        LOGGER.info("Verifying day0 secrets are created in namespace {}", day0Info.getNamespace());
        List<Optional<Secret>> retrievedSecrets = getSecretsFromCluster(day0Info.getConfigDay0(),
                                                                        day0Info.getNamespace(),
                                                                        day0Info.getClusterConfig());
        boolean allSecretsRetrieved = retrievedSecrets.stream().allMatch(Optional::isPresent);
        if (!allSecretsRetrieved) {
            LOGGER.info("Not all secrets were retrieved from the cluster");
            return false;
        }
        boolean secretsVerified = true;
        for (int i = 0; i < retrievedSecrets.size() && secretsVerified; ++i) {
            ConfigDay0.SecretData day0Secret = day0Info.getConfigDay0().getSecrets().get(i);
            Secret retrievedSecret = retrievedSecrets.get(i).get();
            assertThat(day0Secret.getName())
                    .withFailMessage("Names of the secret from the cluster ({}) and in day0 configuration ({}) do not match",
                                     retrievedSecret.getMetadata().getName(), day0Secret.getName())
                    .isEqualTo(retrievedSecret.getMetadata().getName());

            retrievedSecret.getData()
                    .forEach((key, value) -> retrievedSecret.getData().put(key, new String(Base64.getDecoder().decode(value)).trim()));
            Map<String, String> retrievedSecretData = retrievedSecret.getData();

            secretsVerified = day0Secret
                    .getData()
                    .entrySet()
                    .stream()
                    .allMatch(e -> e.getValue().equals(retrievedSecretData.get(e.getKey())));
        }
        return secretsVerified;
    }

    public static void verifySecretsNotPresentInNamespace(final Day0SecretVerificationInfo day0Info) {
        LOGGER.info("Verifying day0 secrets are removed from namespace {}", day0Info.getNamespace());
        for (ConfigDay0.SecretData secretData : day0Info.getConfigDay0().getSecrets()) {
            verifySecretNotPresentInNamespace(secretData, day0Info.getNamespace(), day0Info.getClusterConfig());
        }
    }

    public static void verifySecretNotPresentInNamespace(ConfigDay0.SecretData secret, String namespace, String clusterConfig) {
        KubernetesApiClient client = new KubernetesApiClient(clusterConfig);
        String secretName = secret.getName();
        Optional<Secret> retrievedSecret = getSecretFromCluster(secret, namespace, client);
        assertThat(retrievedSecret)
                .withFailMessage("Secret %s is still present in the namespace %s", secretName, namespace)
                .isNotPresent();
    }

    private static List<Optional<Secret>> getSecretsFromCluster(ConfigDay0 day0Configuration, String namespace, String clusterConfig) {
        LOGGER.info("Retrieving secrets from cluster");
        KubernetesApiClient client = new KubernetesApiClient(clusterConfig);
        return day0Configuration.getSecrets().stream()
                .map(secret -> getSecretFromCluster(secret, namespace, client))
                .collect(Collectors.toList());
    }

    private static Optional<Secret> getSecretFromCluster(ConfigDay0.SecretData secret, String namespace, KubernetesApiClient client) {
        LOGGER.info("Retrieving secret {}", secret.getName());
        Secret retrievedSecret = client.getSecret(namespace, secret.getName());
        if (retrievedSecret == null) {
            LOGGER.info("Secret {} was not retrieved", secret.getName());
        } else {
            LOGGER.info("Secret {} retrieved successfully", secret.getName());
        }
        return Optional.ofNullable(retrievedSecret);
    }

    public static void verifyAnnotations(VnfInstanceResponse vnfInstance, String clusterConfig) {
        KubernetesApiClient client = new KubernetesApiClient(clusterConfig);
        InstantiatedVnfInfo instantiatedVnfInfo = vnfInstance.getInstantiatedVnfInfo();

        assertThat(instantiatedVnfInfo).isNotNull();
        List<VnfcResourceInfo> vnfcResourcesInfo = instantiatedVnfInfo.getVnfcResourceInfo();
        assertThat(vnfcResourcesInfo).isNotEmpty();

        vnfcResourcesInfo.forEach(resourceInfo -> {
            ComputeResource computeResource = resourceInfo.getComputeResource();
            String podName = computeResource.getResourceId();

            JSONObject additionalResourceInfo = new JSONObject((Map) computeResource.getVimLevelAdditionalResourceInfo());

            final String namespace = additionalResourceInfo.getString("namespace");

            await().atMost(180, SECONDS).until(podIsCreated(client, namespace, podName));
            Pod pod = client.getPod(namespace, podName);
            LOGGER.info("Fetching annotations for pod: {} to verify data.", podName);
            ObjectMeta podMetadata = pod.getMetadata();
            Map<String, String> podAnnotations = podMetadata.getAnnotations();
            Map<String, String> resourceAnnotations = additionalResourceInfo.getJSONObject("annotations")
                    .toMap()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() instanceof String)
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> (String) e.getValue()));
            if (podAnnotations == null) {
                assertThat(resourceAnnotations).isNull();
            } else {
                assertThat(resourceAnnotations).containsAllEntriesOf(podAnnotations);
            }
        });
    }

    private static Callable<Boolean> podIsCreated(KubernetesApiClient client, String namespace, String podName) {
        return () -> client.getPod(namespace, podName) != null;
    }
}
