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

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfApiClient.executeCleanupCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfApiClient.executeDeleteCnfIdentifierOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfApiClient.executeTerminateCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfVerify.verifyCleanupVnfResponse;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfVerify.verifyDeleteCnfIdentifierResponse;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfVerify.verifyTerminateCnfResponse;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.INSTANTIATED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.TERMINATE;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class TerminateCnfSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateCnfSteps.class);

    private TerminateCnfSteps() {
    }

    /**
     * Single step that combines the individual steps of a CNF
     * - Terminating a Cnf
     * - Deleting the Cnf identifier after the Cnf has been terminated
     */
    public static void performTerminateAndDeleteIdentifierStep(EvnfmCnf evnfmCnfToTerminate, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToTerminate, user);

        ResponseEntity<Void> terminateResponse = executeTerminateCnfOperationRequest(evnfmCnfToTerminate, user);
        verifyTerminateCnfResponse(terminateResponse);
        final String terminateOperationLink = terminateResponse.getHeaders().get(HttpHeaders.LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, terminateOperationLink);

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                evnfmCnfToTerminate, user, terminateOperationLink, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, TERMINATE);

        performDeleteCnfIdentifierStep(evnfmCnfToTerminate, user);
    }

    /**
     * Delete the Cnf identifier (after Cnf has been terminated)
     *
     * @param evnfmCnfToTerminate Cnf termination details
     */
    public static void performDeleteCnfIdentifierStep(final EvnfmCnf evnfmCnfToTerminate, User user) {
        ResponseEntity<Void> deleteCnfIdentifierResponse = executeDeleteCnfIdentifierOperationRequest(evnfmCnfToTerminate, user);
        verifyDeleteCnfIdentifierResponse(deleteCnfIdentifierResponse);
    }

    public static void performCleanupCnfStep(EvnfmCnf evnfmCnfToCleanup, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToCleanup, user);

        ResponseEntity<Void> cleanupResponse = executeCleanupCnfOperationRequest(evnfmCnfToCleanup, user);
        verifyCleanupVnfResponse(cleanupResponse);
        final String cleanupOperationLink = Objects.requireNonNull(cleanupResponse.getHeaders().get(HttpHeaders.LOCATION)).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, cleanupOperationLink);

        // When cleanup is performed, VNF instance and its operations are always deleted
        try {
            final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                    evnfmCnfToCleanup, user, cleanupOperationLink, null);
            verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, TERMINATE); // cleanup not added to enum
        } catch (HttpClientErrorException.NotFound e) {
            assertThat(e.getResponseBodyAsString()).contains("does not exist");
        }
    }

    public static void performTerminateOrCleanupCnfStepIfNecessary(EvnfmCnf cnfToTerminate, User user) {
        final VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                 cnfToTerminate.getVnfInstanceName(),
                                                                                 user);

        if (vnfInstanceByRelease == null) {
            return;
        }

        cnfToTerminate.setCleanUpResources(true);

        if (Objects.equals(vnfInstanceByRelease.getInstantiationState(), INSTANTIATED)) {
            performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);
        } else {
            performCleanupCnfStep(cnfToTerminate, user);
        }
    }

    public static void performTerminateCnfStepExpectingError(EvnfmCnf evnfmCnfToTerminate, User user) {
        try {
            performTerminateAndDeleteIdentifierStep(evnfmCnfToTerminate, user);
        } catch (HttpClientErrorException exception) {
            verifyExpectedHttpError(exception, evnfmCnfToTerminate.getExpectedError());
        }
    }
}
