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

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TOKEN_URI_RESOURCE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.X_LOGIN;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.X_PASSWORD;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;

public class CommonSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSteps.class);

    /**
     * Method to retrieve a JSESSIONID token.
     *
     * @param keycloakUsername Username matching keycloak secret username
     * @param keycloakPassword Password matching keycloak secret password
     * @return JSESSIONID in String format
     */
    public static String retrieveToken(String keycloakUsername, String keycloakPassword) {
        String gatewayUrl = ConfigurationProvider.getGeneralConfig().getApiGatewayHost();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(X_LOGIN, keycloakUsername);
        httpHeaders.set(X_PASSWORD, keycloakPassword);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        LOGGER.debug("Retrieving Session token from {}\n", gatewayUrl);
        final ResponseEntity<String> responseEntity = getRestRetryTemplate().execute(context ->
                getRestTemplate().postForEntity(gatewayUrl + TOKEN_URI_RESOURCE, requestEntity, String.class));
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Token could not be retrieved successfully from: %s\nDetail: %s", gatewayUrl,
                        responseEntity.getBody()).isEqualTo(200);
        LOGGER.debug("Successfully retrieved session token from {}\n", gatewayUrl);
        return responseEntity.getBody();
    }
}
