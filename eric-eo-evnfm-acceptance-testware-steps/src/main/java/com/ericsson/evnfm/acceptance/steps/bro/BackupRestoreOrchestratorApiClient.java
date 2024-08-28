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
package com.ericsson.evnfm.acceptance.steps.bro;

import static com.ericsson.evnfm.acceptance.utils.Constants.BRO_STATUS_ENDPOINT;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.BackupRestoreOrchestratorStatusResponse;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps;

public class BackupRestoreOrchestratorApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRestoreOrchestratorApiClient.class);

    private BackupRestoreOrchestratorApiClient() {
    }

    public static ResponseEntity<BackupRestoreOrchestratorStatusResponse> retrieveAgentsRegisteredInBRO(final User user) {
        final String broStatusUrl = EVNFM_INSTANCE.getEvnfmUrl() + BRO_STATUS_ENDPOINT;

        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<BackupRestoreOrchestratorStatusResponse> statusResponse = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(broStatusUrl, HttpMethod.GET, httpEntity, BackupRestoreOrchestratorStatusResponse.class));
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, "Get BRO status", statusResponse.getStatusCode());

        return statusResponse;
    }
}
