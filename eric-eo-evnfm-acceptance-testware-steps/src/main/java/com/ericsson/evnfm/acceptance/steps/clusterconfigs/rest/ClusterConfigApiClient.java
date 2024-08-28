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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;

import static com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.utils.Constants.CISM_CLUSTER_PREFIX;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLUSTER_CONFIGS_URI;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static org.junit.Assert.assertTrue;

public class ClusterConfigApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConfigApiClient.class);

    private static final String CLUSTER_FILE_PARAM_KEY = "clusterConfig";
    private static final String DESCRIPTION_PARAM_KEY = "description";
    private static final String SKIP_SAME_CLUSTER_VERIFICATION_PARAM_KEY = "skipSameClusterVerification";
    private static final String IS_DEFAULT_PARAM_KEY = "isDefault";

    public static ResponseEntity<String> getClusterConfigs(User requestingUser) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken(requestingUser);
        String clustersUrl = EVNFM_INSTANCE.getEvnfmUrl() + CLUSTER_CONFIGS_URI;
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Querying for registered cluster configs: {}", clustersUrl);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> clustersList = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(clustersUrl, HttpMethod.GET, requestEntity, String.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        LOGGER.info("Response {}", clustersList);
        return clustersList;
    }

    public static ResponseEntity<String> registerClusterConfig(final ClusterConfig clusterConfigFile, User requestingUser) {
        String uploadClusterConfigUrl = EVNFM_INSTANCE.getEvnfmUrl() + CLUSTER_CONFIGS_URI;
        HttpEntity<MultiValueMap<String, Object>> requestEntity = buildClusterRequestEntity(clusterConfigFile, requestingUser, false);
        LOGGER.info("Registering cluster config file {} to : {}", clusterConfigFile, uploadClusterConfigUrl);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response =  getRestRetryTemplate().execute(
                context -> getRestTemplate().postForEntity(uploadClusterConfigUrl, requestEntity, String.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        return response;
    }

    private static HttpEntity<MultiValueMap<String, Object>> buildClusterRequestEntity(ClusterConfig clusterConfigFile, User requestingUser,
                                                                                       Boolean skipSameClusterVerification) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken(requestingUser);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(CLUSTER_FILE_PARAM_KEY, new FileSystemResource(new File(clusterConfigFile.getLocalPath())));
        body.add(DESCRIPTION_PARAM_KEY, clusterConfigFile.getDescription());
        body.add(SKIP_SAME_CLUSTER_VERIFICATION_PARAM_KEY, Boolean.TRUE.equals(skipSameClusterVerification));
        LOGGER.info("{} Set isDefault param to Cluster Request {} with path {} and default status {}",
                CISM_CLUSTER_PREFIX, clusterConfigFile.getName(), clusterConfigFile.getLocalPath(), clusterConfigFile.isDefault());
        body.add(IS_DEFAULT_PARAM_KEY, clusterConfigFile.isDefault());
        return new HttpEntity<>(body, httpHeaders);
    }

    private static HttpEntity<String> buildPartialUpdateRequestEntity(String clusterConfigUpdateFields,
                                                                      User requestingUser)
     {
        HttpHeaders httpHeaders = getHttpHeadersWithToken(requestingUser);

        MediaType APPLICATION_MERGE_PATCH_JSON = new MediaType("application", "merge-patch+json");

        httpHeaders.setContentType(APPLICATION_MERGE_PATCH_JSON);

        return new HttpEntity<>(clusterConfigUpdateFields, httpHeaders);
    }

    public static ResponseEntity<String> deregisterClusterConfig(final ClusterConfig clusterConfig, User requestingUser) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken(requestingUser);
        String clustersUrl = EVNFM_INSTANCE.getEvnfmUrl() + CLUSTER_CONFIGS_URI + "/" + clusterConfig.getName();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Querying for registered cluster configs: {}", clustersUrl);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> deregisterClusterRequest = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(clustersUrl, HttpMethod.DELETE, requestEntity, String.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        LOGGER.info("Response {}", deregisterClusterRequest);
        return deregisterClusterRequest;
    }

    public static ResponseEntity<String> updateClusterConfig(final ClusterConfig clusterConfig,
                                                             User requestingUser,
                                                             String clusterConfigName,
                                                             Boolean skipSameClusterVerification) {
        String updateClusterConfigUrl = EVNFM_INSTANCE.getEvnfmUrl() + CLUSTER_CONFIGS_URI + "/" + clusterConfigName;
        HttpEntity<MultiValueMap<String, Object>> requestEntity = buildClusterRequestEntity(clusterConfig,
                                                                                            requestingUser,
                                                                                            skipSameClusterVerification);
        LOGGER.info("Update cluster config file {} to : {}", clusterConfig, updateClusterConfigUrl);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response =  getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(updateClusterConfigUrl, HttpMethod.PUT, requestEntity, String.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        return response;
    }

    public static ResponseEntity<String> modifyClusterConfig(String clusterConfigModifyFields,
                                                             User requestingUser,
                                                             String clusterConfigName,
                                                             Boolean skipSameClusterVerification){
        String modifyClusterConfigUrl = EVNFM_INSTANCE.getEvnfmUrl() + CLUSTER_CONFIGS_URI + "/" + clusterConfigName +
                "?skipSameClusterVerification=" + skipSameClusterVerification ;

        HttpEntity<String> requestEntity = buildPartialUpdateRequestEntity(clusterConfigModifyFields,
                                                                           requestingUser
                                                                           );
        LOGGER.info("Partial update cluster config file {} to : {}", clusterConfigModifyFields, modifyClusterConfigUrl);
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response =  getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(modifyClusterConfigUrl, HttpMethod.PATCH, requestEntity, String.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        return response;
    }
}
