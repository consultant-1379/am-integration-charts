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

import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.utils.FileUtilities.getFileFromFileSystem;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getK8sRetryTemplate;

import java.io.IOException;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.slf4j.Logger;
import org.springframework.core.io.FileSystemResource;

import com.ericsson.evnfm.acceptance.exception.KubernetesClientException;
import com.ericsson.evnfm.acceptance.utils.FileUtilities;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaimList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;

public class KubernetesApiClient {
    private static final Logger LOGGER = getLogger(KubernetesApiClient.class);

    private final KubernetesClient client;

    public KubernetesApiClient() {
        LOGGER.info("Creating Kubernetes client with default config file");
        client = new KubernetesClientBuilder().build();
        LOGGER.info("Created Kubernetes client with default config file.");
    }

    public KubernetesApiClient(String kubeConfig) {
        LOGGER.info("Creating Kubernetes client with cluster config file {}", kubeConfig);
        if (kubeConfig == null) {
            throw new RuntimeException("Failed to create kubernetes client");
        }
        try {
            FileSystemResource fileResource = getFileFromFileSystem(kubeConfig);
            String kubeConfigContent = FileUtilities.getFileContentFromInputStream(fileResource.getInputStream());
            Config config = Config.fromKubeconfig(kubeConfigContent);
            client = new KubernetesClientBuilder().withConfig(config).build();
        } catch (IOException e) {
            throw new KubernetesClientException(String.format(
                    "Failed to create kubernetes client using config file : %s , reason : %s", kubeConfig, e));
        }
    }

    public void createNamespace(String namespace) {
        LOGGER.info("Creating namespace {}", namespace);
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).endMetadata().build();
        client.namespaces().resource(ns).create();
    }

    public boolean deleteNamespace(String namespace) {
        LOGGER.info("Deleting namespace {}", namespace);
        return getK8sRetryTemplate().execute(
                context -> !client.namespaces().withName(namespace).delete().isEmpty());
    }

    public Namespace getNamespace(String namespace) {
        return getK8sRetryTemplate().execute(
                context -> client.namespaces().withName(namespace).get());
    }

    public List<Namespace> getNamespaces() {
        return getK8sRetryTemplate().execute(
                context -> client.namespaces().list().getItems());
    }

    public List<Pod> getPodsInNamespace(String namespace) {
        return getK8sRetryTemplate().execute(
                context -> client.pods().inNamespace(namespace).list().getItems());
    }

    public List<Pod> getPodsInNamespaceWithStringInName(String namespace, String prefix) {
        return getK8sRetryTemplate().execute(
                context -> client.pods().inNamespace(namespace)
                        .list().getItems().stream()
                        .filter(pod -> pod.getMetadata().getName().contains(prefix))
                        .collect(Collectors.toList()));
    }

    public Secret getSecret(String namespace, String secretName) {
        return getK8sRetryTemplate().execute(
                context -> client.secrets().inNamespace(namespace).withName(secretName).get());
    }

    public ConfigMap getConfigMap(String namespace, String configMapName) {
        return getK8sRetryTemplate().execute(
                context -> client.configMaps().inNamespace(namespace).withName(configMapName).get());
    }

    public ConfigMap patchConfigMap(String namespace, String configMapName, UnaryOperator<ConfigMap> function) {
        return getK8sRetryTemplate().execute(
                context -> client.configMaps().inNamespace(namespace).withName(configMapName).edit(function));
    }

    public boolean deletePVCs(String namespace) {
        return getK8sRetryTemplate().execute(
                context -> !client.persistentVolumeClaims().inNamespace(namespace).delete().isEmpty());
    }

    public PersistentVolumeClaimList getPVCs(String namespace) {
        return getK8sRetryTemplate().execute(
                context -> client.persistentVolumeClaims().inNamespace(namespace).list());
    }

    public List<StatefulSet> getStatefulSets(String namespace) {
        return client.apps().statefulSets().inNamespace(namespace).list().getItems();
    }

    public List<Deployment> getDeployments(String namespace) {
        return client.apps().deployments().inNamespace(namespace).list().getItems();
    }

    public Pod getPod(String namespace, String name) {
        return client.pods().inNamespace(namespace).withName(name).get();
    }
}
