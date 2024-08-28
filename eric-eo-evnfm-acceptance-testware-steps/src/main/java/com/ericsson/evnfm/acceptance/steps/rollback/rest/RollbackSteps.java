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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getLcmOccurrencesByVnfInstanceName;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackApiClient.executeRollbackLifecycleOperationByIdOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackVerify.verifyRollbackAtFailure;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackVerify.verifyRollbackResponse;
import static com.ericsson.evnfm.acceptance.utils.Constants.FAILED_TO_FIND_LCM_OP_FAILED_TEMP;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLED_BACK;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.utils.Constants;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class RollbackSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackSteps.class);

    private RollbackSteps() {
    }

    public static void performRollbackStep(EvnfmCnf evnfmCnfToRollback, User user) {

        Optional<List<VnfLcmOpOcc>> vnfLcmOpOccOptionalList = getLcmOccurrencesByVnfInstanceName(
                EVNFM_INSTANCE.getEvnfmUrl(), evnfmCnfToRollback.getVnfInstanceName(), user);

        String lifecycleOperationId = vnfLcmOpOccOptionalList
                .flatMap(vnfLcmOpOccs -> vnfLcmOpOccs.stream()
                        .filter(vnfLcmOpOcc -> vnfLcmOpOcc.getOperationState().equals(VnfLcmOpOcc.OperationStateEnum.FAILED_TEMP))
                        .findFirst())
                .map(VnfLcmOpOcc::getId)
                .orElseThrow(() -> new RuntimeException(String.format(FAILED_TO_FIND_LCM_OP_FAILED_TEMP, evnfmCnfToRollback.getVnfInstanceName())));

        ResponseEntity<Void> rollbackResponse = executeRollbackLifecycleOperationByIdOperationRequest(lifecycleOperationId, user);
        verifyRollbackResponse(rollbackResponse);
        final String operationLink = EVNFM_INSTANCE.getEvnfmUrl() + Constants.ETSI_LIFECYCLE_OCC_URI + "/" + lifecycleOperationId;
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationLink);

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                evnfmCnfToRollback, user, operationLink, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, CHANGE_VNFPKG, ROLLED_BACK);
        verifyRollbackAtFailure(evnfmCnfToRollback, user);
    }
}
