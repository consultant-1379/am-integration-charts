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
package com.ericsson.evnfm.acceptance.steps.modify.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVnfInstanceByLink;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExtensionsAreEqual;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoApiClient.executeModifyVnfInfoOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoVerify.verifyModifiedInfoOfVnfInstance;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoVerify.verifyModifyVnfInfoResponseSuccess;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.MODIFY_INFO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class ModifyVnfInstanceInfoSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInstanceInfoSteps.class);

    private ModifyVnfInstanceInfoSteps() {
    }

    public static void performModifyVnfInstanceInfoStep(EvnfmCnf evnfmCnfToModifyVnfInfo, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToModifyVnfInfo, user);

        final ResponseEntity<Void> response = executeModifyVnfInfoOperationRequest(evnfmCnfToModifyVnfInfo, user);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = checkResponseAndWaitForOperationCompletion(evnfmCnfToModifyVnfInfo,
                                                                                                                 user,
                                                                                                                 response);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, MODIFY_INFO);

        VnfInstanceResponse actualVnfInstance = getVnfInstanceByLink(evnfmCnfToModifyVnfInfo.getVnfInstanceResponseLinks().getSelf().getHref(), user);

        verifyExtensionsAreEqual(evnfmCnfToModifyVnfInfo, actualVnfInstance);
        verifyModifiedInfoOfVnfInstance(evnfmCnfToModifyVnfInfo, actualVnfInstance);
    }

    public static void performModifyVnfInstanceInfoStepExpectingFailure(EvnfmCnf evnfmCnfToModifyVnfInfo, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToModifyVnfInfo, user);

        final ResponseEntity<Void> response = executeModifyVnfInfoOperationRequest(evnfmCnfToModifyVnfInfo, user);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = checkResponseAndWaitForOperationCompletion(evnfmCnfToModifyVnfInfo,
                                                                                                                 user,
                                                                                                                 response);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, MODIFY_INFO, evnfmCnfToModifyVnfInfo.getExpectedOperationState());
    }


    public static void performModifyVnfInstanceInfoUITestStep(EvnfmCnf evnfmCnfToModifyVnfInfo, User user) {
        final VnfInstanceResponse actualVnfInstance = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                              evnfmCnfToModifyVnfInfo.getVnfInstanceName(),
                                                                              user);

        verifyExtensionsAreEqual(evnfmCnfToModifyVnfInfo, actualVnfInstance);
        verifyModifiedInfoOfVnfInstance(evnfmCnfToModifyVnfInfo, actualVnfInstance);
    }

    private static ResponseEntity<VnfLcmOpOcc> checkResponseAndWaitForOperationCompletion(final EvnfmCnf evnfmCnfToModifyVnfInfo,
                                                                                          final User user,
                                                                                          final ResponseEntity<Void> response) {

        verifyModifyVnfInfoResponseSuccess(response);
        final String operationLink = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationLink);

        return pollingVnfLcmOperationOccurrence(evnfmCnfToModifyVnfInfo, user, operationLink, null);
    }
}
