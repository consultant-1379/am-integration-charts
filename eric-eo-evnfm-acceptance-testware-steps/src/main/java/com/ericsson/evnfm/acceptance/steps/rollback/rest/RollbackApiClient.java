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
package com.ericsson.evnfm.acceptance.steps.rollback.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLBACK_FROM_FAILURE_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLBACK_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_LCM_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.User;

public class RollbackApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackApiClient.class);

    private RollbackApiClient() {
    }

    public static ResponseEntity<Void> executeRollbackLifecycleOperationByIdOperationRequest(final String lifecycleOperationId, User user) {
        LOGGER.info(STARTS_PERFORMING_LCM_OPERATION_LOG, ROLLBACK_LIFECYCLE_OPERATION, lifecycleOperationId);
        String rollbackUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ROLLBACK_FROM_FAILURE_URI, lifecycleOperationId);
        long startTime = System.currentTimeMillis();
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(context -> getRestTemplate()
                .postForEntity(rollbackUrl, new HttpEntity<>(createHeaders(user)), Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds", rollbackUrl,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 15);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, ROLLBACK_LIFECYCLE_OPERATION, getReasonPhrase(response.getStatusCode()));
        return response;
    }
}
