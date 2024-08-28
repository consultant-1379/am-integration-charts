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
package com.ericsson.evnfm.acceptance.steps.common.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import static com.ericsson.evnfm.acceptance.utils.Constants.RESPONSE_INFO_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.TOKEN_URI_RESOURCE;
import static com.ericsson.evnfm.acceptance.utils.Constants.X_LOGIN;
import static com.ericsson.evnfm.acceptance.utils.Constants.X_PASSWORD;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

public class LoginRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginRequest.class);

    private LoginRequest() {
    }

    public static ResponseEntity<String> jsessionidTokenRequest(String username, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(X_LOGIN, username);
        httpHeaders.set(X_PASSWORD, password);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final String loginUrl = EVNFM_INSTANCE.getEvnfmUrl() + TOKEN_URI_RESOURCE;
        LOGGER.debug("Retrieving Session token from {}\n", loginUrl);
        ResponseEntity<String> tokenResponse = getRestRetryTemplate()
                .execute(context -> getRestTemplate().postForEntity(loginUrl, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, tokenResponse.getBody());
        return tokenResponse;
    }
}
