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
package com.ericsson.evnfm.acceptance.utils;

import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.utils.HelmCommand.OutputTypeEnum.JSON;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

public final class HelmUtils {

    private static final Logger LOGGER = getLogger(HelmUtils.class);

    private HelmUtils() {
    }

    // For long running commands inheritIO should be set to true
    public static String runCommand(final String command, final int timeout, final boolean inheritIO) {
        ProcessExecutor executor = new ProcessExecutor();
        ProcessExecutorResponse response;
        try {
            response = executor.executeProcess(command, timeout, inheritIO);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to execute command:: {}, Reason:: {}", command, e.getMessage());
            throw new RuntimeException(String.format("Failed to execute command:: %s, Reason:: %s", command, e.getMessage()));
        }
        if (Objects.requireNonNull(response).getExitValue() != 0) {
            LOGGER.error("ProcessExecutor response :: {}", response.getCommandOutput());
            throw new RuntimeException(String.format("ProcessExecutor response :: %s", response.getCommandOutput()));
        }
        return response.getCommandOutput();
    }

    public static List<Map<String, String>> getHelmReleases(String namespace, String cluster) {
        String command = new HelmCommand().list().namespace(namespace).kubeconfig(cluster).output(JSON).build();
        String response = runCommand(command, 300, false);
        try {
            return getObjectMapper().readValue(response, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to read helm release values :: %s", response));
        }
    }

    public static List<Map<String, String>> getHelmHistoryForRelease(String releaseName, String namespace, String cluster) {
        String command = new HelmCommand()
                .history(releaseName)
                .namespace(namespace)
                .kubeconfig(cluster)
                .output(JSON)
                .build();
        String response = runCommand(command, 300, false);
        try {
            return getObjectMapper().readValue(response, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to read values from :: %s", response), e.getCause());
        }
    }

    public static List<Map<String, Object>> getReleaseValues(EvnfmCnf evnfmCnf) {
        return getHelmReleasesAndCheckNotEmpty(evnfmCnf.getCluster(), evnfmCnf.getNamespace())
                .stream()
                .map(releaseName -> new HelmCommand()
                        .getValues(releaseName)
                        .namespace(evnfmCnf.getNamespace())
                        .kubeconfig(evnfmCnf.getCluster().getLocalPath())
                        .output(JSON)
                        .allValues()
                        .build())
                .map(helmCommand -> runCommand(helmCommand, 300, false))
                .map(HelmUtils::readJsonToMap)
                .collect(Collectors.toList());
    }

    public static List<Map<String, Object>> getReleaseManifests(ClusterConfig clusterConfig, String namespace) {
        return getHelmReleasesAndCheckNotEmpty(clusterConfig, namespace)
                .stream()
                .map(releaseName -> new HelmCommand()
                        .getManifest(releaseName)
                        .namespace(namespace)
                        .kubeconfig(clusterConfig.getLocalPath())
                        .build())
                .map(helmCommand -> runCommand(helmCommand, 300, false))
                .flatMap(HelmUtils::readYamlsToMapsStream)
                .collect(Collectors.toList());
    }

    private static List<String> getHelmReleasesAndCheckNotEmpty(final ClusterConfig clusterConfig, final String namespace) {
        List<String> releases = getHelmReleases(namespace, clusterConfig.getLocalPath()).stream()
                .map(release -> release.get("name"))
                .collect(Collectors.toList());

        if (releases.isEmpty()) {
            throw new RuntimeException(String.format("No releases found in the namespace: %s, cluster: %s.", namespace, clusterConfig.getName()));
        }

        return releases;
    }

    private static Map<String, Object> readJsonToMap(final String commandResponse) {
        try {
            return getObjectMapper().readValue(commandResponse, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to read JSON response as map from :: %s", commandResponse));
        }
    }

    @SuppressWarnings("unchecked")
    private static Stream<Map<String, Object>> readYamlsToMapsStream(final String commandResponse) {
        final Iterable<Object> documents = getYamlParser().loadAll(commandResponse);

        return StreamSupport.stream(documents.spliterator(), false)
                .filter(Objects::nonNull)
                .map(object -> (Map<String, Object>) object);
    }
}
