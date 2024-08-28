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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

import static com.ericsson.evnfm.acceptance.utils.Constants.CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterConfigVerify {

    private ClusterConfigVerify() {
    }

    public static ClusterConfig verifyClusterConfigRegistrationSuccess(ResponseEntity<String> responseEntity,
                                                                       ClusterConfig clusterConfig) {
        assertThat(responseEntity.getStatusCode())
                .withFailMessage("Unable to register cluster: %s, status code is %s",
                                 clusterConfig, responseEntity.getStatusCode().value())
                .isEqualTo(HttpStatus.CREATED);
        assertThat(clusterConfig)
                .withFailMessage("Provided clusterConfig is null")
                .isNotNull();
        assertThat(responseEntity.getBody())
                .withFailMessage("Response body is null")
                .isNotNull();
        try {
            ClusterConfig clusterFromResponse = getObjectMapper().readValue(responseEntity.getBody(), ClusterConfig.class);
            assertThat(clusterFromResponse.getId())
                    .withFailMessage(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Id")
                    .isNotEmpty();
            assertThat(clusterConfig).isEqualTo(clusterFromResponse);
            return clusterFromResponse;
        } catch (JsonProcessingException e) {
            fail("Unable to convert response body to ClusterConfig type");
            return null;
        }
    }

    public static void verifyClusterConfigListContainsCluster(List<ClusterConfig> clusterConfigList, ClusterConfig clusterConfig) {
        assertThat(clusterConfigList).containsOnlyOnce(clusterConfig);
    }

    public static void verifyClusterConfigListDoesNotContainCluster(List<ClusterConfig> clusterConfigList, ClusterConfig clusterConfig) {
        assertThat(clusterConfigList).doesNotContain(clusterConfig);
    }

    public static void verifyFailedRequest(ProblemDetails actual,
                                           ProblemDetails expected) {

        assertThat(expected)
                .withFailMessage("Expected and received error messages did not match")
                .isEqualTo(actual);
    }

    public static void verifyFailedRequestRegex(ProblemDetails actual,
                                                ProblemDetails expected) {
        Predicate<ProblemDetails> expectedPredicate = actualPredicate -> actualPredicate.getType().matches(expected.getType())
                && actualPredicate.getTitle().matches(expected.getTitle())
                && actualPredicate.getStatus().equals(expected.getStatus())
                && actualPredicate.getDetail().matches(expected.getDetail())
                && actualPredicate.getInstance().equals(expected.getInstance());

        assertThat(actual)
                .withFailMessage("Expected and received error messages did not match")
                .matches(expectedPredicate);
    }

    public static ClusterConfig verifyClusterConfigUpdateSuccess(ResponseEntity<String> responseEntity,
                                                                 ClusterConfig clusterConfig) {
        assertThat(responseEntity.getStatusCode())
                .withFailMessage("Unable to update cluster: %s, status code is %s",
                                 clusterConfig, responseEntity.getStatusCode().value())
                .isEqualTo(HttpStatus.OK);

        assertThat(clusterConfig)
                .withFailMessage("Provided clusterConfig is null")
                .isNotNull();

        assertThat(responseEntity.getBody())
                .withFailMessage("Response body is null")
                .isNotNull();
        try {
            ClusterConfig clusterFromResponse = getObjectMapper().readValue(responseEntity.getBody(), ClusterConfig.class);
            assertThat(clusterFromResponse.getId())
                    .withFailMessage(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Id")
                    .isNotEmpty();
            assertThat(clusterConfig).isEqualTo(clusterFromResponse);
            return clusterFromResponse;
        } catch (JsonProcessingException e) {
            fail("Unable to convert response body to ClusterConfig type");
            return null;
        }
    }

    public static ClusterConfig verifyClusterConfigModifySuccess(ResponseEntity<String> responseEntity,
                                                                 ClusterConfig clusterConfig,
                                                                 String patchFields) {
        assertThat(responseEntity.getStatusCode())
                .withFailMessage("Unable to modify cluster: %s, status code is %s",
                                 clusterConfig, responseEntity.getStatusCode().value())
                .isEqualTo(HttpStatus.OK);

        assertThat(clusterConfig)
                .withFailMessage("Provided clusterConfig is null")
                .isNotNull();

        assertThat(responseEntity.getBody())
                .withFailMessage("Response body is null")
                .isNotNull();

        try {
            ClusterConfig clusterFromResponse = getObjectMapper().readValue(responseEntity.getBody(), ClusterConfig.class);
            assertThat(clusterFromResponse.getId())
                    .withFailMessage(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Id")
                    .isNotEmpty();
            ObjectMapper patchFieldsObjectMapper = getObjectMapper();
            Map<String,String>  patchFieldsMap = patchFieldsObjectMapper.readValue(patchFields,Map.class);

            if (patchFieldsMap.containsKey("description")) {
                assertThat(clusterFromResponse.getDescription())
                        .isEqualTo(patchFieldsMap.get("description"));
            }

            if (patchFieldsMap.containsKey("isDefault")) {
                assertThat(clusterFromResponse.isDefault())
                        .isEqualTo(patchFieldsMap.get("isDefault"));
            }
            assertThat(clusterConfig).isEqualTo(clusterFromResponse);
            return clusterFromResponse;
        } catch (JsonProcessingException e) {
            fail("Unable to convert response body to ClusterConfig type");
            return null;
        }
    }
}
