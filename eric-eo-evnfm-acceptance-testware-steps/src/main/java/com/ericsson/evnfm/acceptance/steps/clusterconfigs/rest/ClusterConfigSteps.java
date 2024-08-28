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
package com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigApiClient.deregisterClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigApiClient.getClusterConfigs;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigApiClient.modifyClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigApiClient.registerClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigApiClient.updateClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigModifySuccess;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigRegistrationSuccess;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigUpdateSuccess;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyFailedRequest;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyFailedRequestRegex;
import static com.ericsson.evnfm.acceptance.utils.Constants.CISM_CLUSTER_PREFIX;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.slf4j.LoggerFactory.getLogger;

public class ClusterConfigSteps {
    private static final Logger LOGGER = getLogger(ClusterConfigSteps.class);

    private ClusterConfigSteps() {
    }

    public static void registerCluster(ClusterConfig clusterConfigFile, User user) {
        ResponseEntity<String> registerClusterConfigResponse = registerClusterConfig(clusterConfigFile, user);
        verifyClusterConfigRegistrationSuccess(registerClusterConfigResponse, clusterConfigFile);
    }

    public static void registerClusterExpectingError(ClusterConfig clusterConfigFile, ProblemDetails expectedError, User user)
    throws JsonProcessingException {
        try {
            LOGGER.info("registerClusterExpectingError");
            registerClusterConfig(clusterConfigFile, user);
        } catch (HttpClientErrorException httpClientErrorException) {
            ProblemDetails errorMessageReceived = getObjectMapper().readValue(httpClientErrorException.getResponseBodyAsString(),
                                                                              ProblemDetails.class);
            LOGGER.info("Error message received: {}", errorMessageReceived);
            verifyFailedRequest(errorMessageReceived, expectedError);
            return;
        }
        fail("Exception was expected, but none received");
    }

    public static ClusterConfig searchForClusterWithName(String name, User user) {
        Optional<ClusterConfig> clusterConfig = getClustersList(user).stream().filter(c -> name.equals(c.getName())).findFirst();
        if (clusterConfig.isEmpty()) {
            fail("Cluster config not found");
        }
        return clusterConfig.get();
    }

    public static List<ClusterConfig> getClustersList(User user) {
        ResponseEntity<String> getClustersResponse = getClusterConfigs(user);
        try {
            final ObjectMapper mapper = getObjectMapper();
            String responseBody = Objects.requireNonNull(getClustersResponse.getBody());
            JsonNode clusterConfigsListNode = mapper.readTree(responseBody).get("items");
            return mapper.convertValue(clusterConfigsListNode, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert cluster configs to ClusterConfig type", e);
        }
    }

    public static ClusterConfig getCluster(User user, String clusterName) {
        List<ClusterConfig> clusterConfigs = getClustersList(user);

        return clusterConfigs.stream()
                .filter(clusterConfig -> clusterName.equals(clusterConfig.getName()))
                .findAny()
                .orElse(null);
    }

    public static void deregisterCluster(ClusterConfig clusterConfigName, User user) {
        LOGGER.info("{} Clean up cluster config {}", CISM_CLUSTER_PREFIX, clusterConfigName);
        ResponseEntity<String> deregisterClusterConfigResponse = deregisterClusterConfig(clusterConfigName, user);
        assertThat(deregisterClusterConfigResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    public static void updateCluster(ClusterConfig clusterConfigFile, User user, Boolean skipSameClusterVerification) {
        ResponseEntity<String> updateClusterConfigResponse = updateClusterConfig(clusterConfigFile,
                                                                                 user,
                                                                                 clusterConfigFile.getName(),
                                                                                 skipSameClusterVerification);
        verifyClusterConfigUpdateSuccess(updateClusterConfigResponse, clusterConfigFile);
    }

    public static void updateClusterExpectingError(ClusterConfig clusterConfigFile, ProblemDetails expectedError, User user,
                                                   String clusterConfigName, Boolean skipSameClusterVerification)
    throws JsonProcessingException {
        try {
            LOGGER.info("registerClusterExpectingError");
            updateClusterConfig(clusterConfigFile, user, clusterConfigName, skipSameClusterVerification);
        } catch (HttpClientErrorException httpClientErrorException) {
            ProblemDetails errorMessageReceived = getObjectMapper().readValue(httpClientErrorException.getResponseBodyAsString(),
                                                                              ProblemDetails.class);
            LOGGER.info("Error message received: {}", errorMessageReceived);
            LOGGER.info("Error message expected: {}", expectedError);
            verifyFailedRequestRegex(errorMessageReceived, expectedError);
            return;
        }
        fail("Exception was expected, but none received");
    }

    public static void modifyCluster(String patchFields, ClusterConfig clusterConfigFile, User user, Boolean skipSameClusterVerification) {
        LOGGER.info("{} Update cluster config {} with path {} with default status {}",
                CISM_CLUSTER_PREFIX, clusterConfigFile.getName(), clusterConfigFile.getLocalPath(),  clusterConfigFile.isDefault());
        LOGGER.info("{} Update cluster config {} with patchFields {}", CISM_CLUSTER_PREFIX, clusterConfigFile.getName(), patchFields);
        ResponseEntity<String> modifyClusterConfigResponse = modifyClusterConfig(patchFields,
                                                                                 user,
                                                                                 clusterConfigFile.getName(),
                                                                                 skipSameClusterVerification);

        ClusterConfig modifiedClusterConfig = getCluster(user, clusterConfigFile.getName());
        verifyClusterConfigModifySuccess(modifyClusterConfigResponse, modifiedClusterConfig, patchFields);
    }

    public static void modifyClusterExpectingError(String patchFields,
                                                   String clusterName,
                                                   User user,
                                                   ProblemDetails expectedError,
                                                   Boolean skipSameClusterVerification)
    throws JsonProcessingException {
        try {
            modifyClusterConfig(patchFields, user, clusterName, skipSameClusterVerification);
        } catch (HttpClientErrorException httpClientErrorException) {
            ProblemDetails errorMessageReceived = getObjectMapper().readValue(httpClientErrorException.getResponseBodyAsString(),
                                                                              ProblemDetails.class);
            LOGGER.info("Error message received: {}", errorMessageReceived);
            verifyFailedRequestRegex(errorMessageReceived, expectedError);
        }
    }

    public static void deregisterClusterExpectingError(ClusterConfig clusterConfig, User user, ProblemDetails expectedError) throws
            JsonProcessingException {
        try {
            LOGGER.info("deregisterClusterExpectingError");
            deregisterClusterConfig(clusterConfig, user);
        } catch (HttpClientErrorException httpClientErrorException){
            ProblemDetails errorMessageReceived = getObjectMapper().readValue(httpClientErrorException.getResponseBodyAsString(), ProblemDetails.class);
            LOGGER.info("Error message received: {}", errorMessageReceived);
            verifyFailedRequest(errorMessageReceived, expectedError);
            return;
        }
        fail("Exception was expected, but none received");
    }

}
