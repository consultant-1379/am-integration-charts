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

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.LoginRequest.jsessionidTokenRequest;
import static com.ericsson.evnfm.acceptance.utils.Constants.COOKIE;
import static com.ericsson.evnfm.acceptance.utils.Constants.JSESSIONID;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import com.ericsson.evnfm.acceptance.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class LoginSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginSteps.class);

    private LoginSteps() {
    }

    public static HttpHeaders getHttpHeadersWithToken(User user) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(COOKIE, JSESSIONID + retrieveToken(user.getUsername(), user.getPassword(), true));
        return httpHeaders;
    }

    /**
     * Method to retrieve a JSESSIONID token.
     *
     * @param username Username matching keycloak secret username
     * @param password Password matching keycloak secret password
     * @param retry    Retry if the first attempt fails
     * @return JSESSIONID in String format
     */
    public static String retrieveToken(String username, String password, boolean retry) {
        try {
            final ResponseEntity<String> responseEntity = jsessionidTokenRequest(username, password);
            assertThat(responseEntity.getStatusCode().value())
                    .withFailMessage("Token could not be retrieved successfully from: %s\nDetail: %s",
                                     EVNFM_INSTANCE.getEvnfmUrl(), responseEntity.getBody())
                    .isEqualTo(200);
            LOGGER.debug("Successfully retrieved session token from {}\n", EVNFM_INSTANCE.getEvnfmUrl());
            return responseEntity.getBody();
        } catch (Exception e) {
            if (retry) {
                LOGGER.warn("Token could not be retrieved successfully. Logging error and retrying", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    LOGGER.warn("Sleep was interrupted: ", ie);
                }
                return retrieveToken(username, password, false);
            } else {
                throw e;
            }
        }
    }
}
