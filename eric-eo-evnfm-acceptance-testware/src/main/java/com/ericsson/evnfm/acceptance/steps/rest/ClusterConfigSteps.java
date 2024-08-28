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
package com.ericsson.evnfm.acceptance.steps.rest;

import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CLUSTER_UPLOAD_URI;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getFileResource;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getGatewayUrl;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class ClusterConfigSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConfigSteps.class);

    /**
     * @param clusterConfigFile
     *         the cluster file to upload
     */
    public static void uploadClusterConfig(final String clusterConfigFile) {
        FileSystemResource clusterConfig = getFileResource(clusterConfigFile);
        final String uploadClusterConfigUrl = getGatewayUrl() + CLUSTER_UPLOAD_URI;
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("clusterConfig", clusterConfig);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        LOGGER.info("Executing upload cluster config request {} to host: {}", requestEntity, uploadClusterConfigUrl);
        ResponseEntity<String> responseEntity = getRestRetryTemplate().execute(context -> getRestTemplate()
                .postForEntity(uploadClusterConfigUrl, requestEntity, String.class));
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(201);
        LOGGER.info("ClusterFile uploaded successfully");
    }
}
