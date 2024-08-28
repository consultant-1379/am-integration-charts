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
package com.ericsson.evnfm.acceptance.steps.upgrade.rest;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.failLcmOperation;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.getPackageByVnfdIdentifier;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfApiClient.executeRollbackCnfOperation;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfApiClient.executeUpgradeCnfOperation;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfApiClient.getOperationDetailsForVnfPkgId;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfVerify.verifyUpgradeCnfResponseSuccess;
import static com.ericsson.evnfm.acceptance.utils.Constants.FAILED;
import static com.ericsson.evnfm.acceptance.utils.Constants.FAILED_TEMP;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLED_BACK;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class UpgradeCnfSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeCnfSteps.class);

    private UpgradeCnfSteps() {
    }

    public static void performRollbackCnfStep(EvnfmCnf evnfmCnfToRollback, User user,
                                              VnfLcmOpOcc.OperationStateEnum operationStateEnum) {

        collectCnfInstanceLinksIfNeed(evnfmCnfToRollback, user);

        final ResponseEntity<Void> response = executeRollbackCnfOperation(evnfmCnfToRollback, user);
        verifyUpgradeCnfResponseSuccess(response);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, response.getHeaders().get(HttpHeaders.LOCATION).get(0));

        String operationUrl = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                evnfmCnfToRollback, user, operationUrl, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, CHANGE_VNFPKG, operationStateEnum.name());
    }

    public static void performUpgradeCnfStep(EvnfmCnf evnfmCnfToUpgrade, User user, VnfLcmOpOcc.OperationStateEnum operationStateEnum) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToUpgrade, user);

        final ResponseEntity<Void> response = executeUpgradeCnfOperation(evnfmCnfToUpgrade, user);
        verifyUpgradeCnfResponseSuccess(response);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, response.getHeaders().get(HttpHeaders.LOCATION).get(0));

        String operationUrl = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                evnfmCnfToUpgrade, user, operationUrl, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, CHANGE_VNFPKG, operationStateEnum.name());
        if(evnfmCnfToUpgrade.getExpectedHelmValues() != null) {
            verifyHelmValues(evnfmCnfToUpgrade);
        }
    }

    public static void performUpgradeCnfStep(EvnfmCnf evnfmCnfToUpgrade, User user) {
        performUpgradeCnfStep(evnfmCnfToUpgrade, user, VnfLcmOpOcc.OperationStateEnum.COMPLETED);
    }

    public static void performSuccessfulRollbackCnfStep(EvnfmCnf evnfmCnfToRollback, User user) {
        performRollbackCnfStep(evnfmCnfToRollback, user, VnfLcmOpOcc.OperationStateEnum.COMPLETED);
    }

    public static void performRollbackCnfAfterSuccessfulUpgradeStep(EvnfmCnf cnfToRollback,
                                                                    String sourceVnfdId, User user) {
        List<OperationDetail> supportedOperationsAfterUpgrade = getSupportedOperations(sourceVnfdId, user);
        performSuccessfulRollbackCnfStep(cnfToRollback, user);
        List<OperationDetail> supportedOperationsAfterRollback = getSupportedOperations(cnfToRollback.getVnfdId(), user);
        assertThat(supportedOperationsAfterUpgrade.containsAll(supportedOperationsAfterRollback)).isEqualTo(false);
        if (cnfToRollback.getExpectedHelmValues() != null) {
            verifyHelmValues(cnfToRollback);
        }
    }

    public static void performUpgradeCnfFailureStep(EvnfmCnf evnfmCnfToUpgrade, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToUpgrade, user);

        final ResponseEntity<Void> response = executeUpgradeCnfOperation(evnfmCnfToUpgrade, user);
        final String responseHeaderUrl = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, responseHeaderUrl);

        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, responseHeaderUrl);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(evnfmCnfToUpgrade,
                                                                                                       user,
                                                                                                       responseHeaderUrl,
                                                                                                       null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, CHANGE_VNFPKG, FAILED_TEMP);

        // call the 'fail' api to fail the operation and certify FAILED state
        failLcmOperation(responseHeaderUrl, user);

        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, responseHeaderUrl);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseFailedResult = pollingVnfLcmOperationOccurrence(evnfmCnfToUpgrade,
                                                                                                             user,
                                                                                                             responseHeaderUrl,
                                                                                                             null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseFailedResult, CHANGE_VNFPKG, FAILED);
    }

    public static void performUpgradeCnfAutoRollbackStep(EvnfmCnf evnfmCnfToUpgrade, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToUpgrade, user);

        final ResponseEntity<Void> response = executeUpgradeCnfOperation(evnfmCnfToUpgrade, user);
        final String operationId = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationId);

        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationId);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(evnfmCnfToUpgrade,
                                                                                                       user,
                                                                                                       operationId,
                                                                                                       null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, CHANGE_VNFPKG, ROLLED_BACK);

        LOGGER.info("Operation {} failed as expected", operationId);
    }

    public static void performUpgradeCnfStepExpectingError(EvnfmCnf evnfmCnfToUpgrade, User user) {
        try {
            performUpgradeCnfStep(evnfmCnfToUpgrade, user);
        } catch (HttpClientErrorException exception) {
            verifyExpectedHttpError(exception, evnfmCnfToUpgrade.getExpectedError());
        }
    }

    private static List<OperationDetail> getSupportedOperations(String vnfdId, User user) {
        Optional<VnfPkgInfo> vnfPkgInfo = getPackageByVnfdIdentifier(vnfdId, user);
        return vnfPkgInfo.map(pkgInfo -> getOperationDetailsForVnfPkgId(pkgInfo.getId(), user)).orElse(null);
    }
}
