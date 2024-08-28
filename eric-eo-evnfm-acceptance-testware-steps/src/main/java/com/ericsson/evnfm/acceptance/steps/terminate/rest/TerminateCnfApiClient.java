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
package com.ericsson.evnfm.acceptance.steps.terminate.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCommonAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCleanupAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLEANUP;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLEANUP_URL;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_IDENTIFIER_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.vnfm.orchestrator.model.TerminateVnfRequest.TerminationTypeEnum.FORCEFUL;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.CleanupVnfRequest;
import com.ericsson.vnfm.orchestrator.model.TerminateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class TerminateCnfApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateCnfApiClient.class);

    private TerminateCnfApiClient() {
    }

    /**
     * Method to terminate a Cnf.
     *
     * @param evnfmCnfToTerminate   Cnf termination details
     * @return ResponseEntity<Void> terminate response
     */
    public static ResponseEntity<Void> executeTerminateCnfOperationRequest(final EvnfmCnf evnfmCnfToTerminate, User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.TERMINATE, evnfmCnfToTerminate.getVnfInstanceName());

        TerminateVnfRequest terminateVnfRequestBody = buildTerminateCnfRequestBody(evnfmCnfToTerminate);
        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.TERMINATE, terminateVnfRequestBody);
        HttpEntity<TerminateVnfRequest> request = new HttpEntity<>(terminateVnfRequestBody, createHeaders(user));

        final String terminateUri = evnfmCnfToTerminate.getVnfInstanceResponseLinks().getTerminate().getHref();
        long startTime = System.currentTimeMillis();
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().postForEntity(terminateUri, request, Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds", terminateUri,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 15);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.TERMINATE, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    /**
     * Delete the Cnf identifier (after Cnf has been terminated)
     *
     * @param evnfmCnfToTerminate Cnf termination details
     */
    public static ResponseEntity<Void> executeDeleteCnfIdentifierOperationRequest(final EvnfmCnf evnfmCnfToTerminate, User user) {
        String self = evnfmCnfToTerminate.getVnfInstanceResponseLinks().getSelf().getHref();
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, DELETE_IDENTIFIER_LIFECYCLE_OPERATION, evnfmCnfToTerminate.getVnfInstanceName());
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(self, HttpMethod.DELETE, new HttpEntity<>(createHeaders(user)), Void.class));
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, DELETE_IDENTIFIER_LIFECYCLE_OPERATION, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    public static TerminateVnfRequest buildTerminateCnfRequestBody(final EvnfmCnf evnfmCnfToTerminate) {
        Map<String, Object> additionalMap = buildCommonAdditionalParamsMap(evnfmCnfToTerminate);
        return new TerminateVnfRequest().terminationType(FORCEFUL).additionalParams(additionalMap);
    }

    /**
     * Method to cleanup a Cnf.
     *
     * @param evnfmCnfToCleanup Cnf cleanup details
     * @return ResponseEntity<Void> cleanup response
     */
    public static ResponseEntity<Void> executeCleanupCnfOperationRequest(final EvnfmCnf evnfmCnfToCleanup, User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, CLEANUP, evnfmCnfToCleanup.getVnfInstanceName());

        CleanupVnfRequest cleanupVnfRequestBody = buildCleanupCnfRequestBody(evnfmCnfToCleanup);
        LOGGER.info(REQUEST_BODY_LOG, CLEANUP, cleanupVnfRequestBody);
        HttpEntity<CleanupVnfRequest> request = new HttpEntity<>(cleanupVnfRequestBody, createHeaders(user));

        final String cleanupUri = evnfmCnfToCleanup.getVnfInstanceResponseLinks().getSelf().getHref() + CLEANUP_URL;
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().postForEntity(cleanupUri, request, Void.class));

        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, CLEANUP, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    public static CleanupVnfRequest buildCleanupCnfRequestBody(final EvnfmCnf evnfmCnfToCleanup) {
        Map<String, Object> additionalMap = buildCleanupAdditionalParamsMap(evnfmCnfToCleanup);
        return new CleanupVnfRequest().additionalParams(additionalMap);
    }
}
