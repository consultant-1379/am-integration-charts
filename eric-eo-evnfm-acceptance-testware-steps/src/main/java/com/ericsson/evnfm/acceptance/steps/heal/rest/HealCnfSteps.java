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
package com.ericsson.evnfm.acceptance.steps.heal.rest;

import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.getSftpCredentials;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.getSftpServer;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectDay0VerificationInfo;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealApiClient.executeHealCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfVerify.verifyCnfWasHealed;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfVerify.verifyHealCnfResponse;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.HEAL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class HealCnfSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealCnfSteps.class);

    private static final String RESTORE_CREDENTIALS_PLACEHOLDER = "<restore-credentials>";
    private static final String RESTORE_URI_PLACEHOLDER = "<restore-uri>";

    private HealCnfSteps() {
    }

    public static void performHealCnfStep(EvnfmCnf cnfToHeal, User user) {
        collectCnfInstanceLinksIfNeed(cnfToHeal, user);
        appendRestoreDay0Secrets(cnfToHeal);
        ResponseEntity<Void> restoreResponse = executeHealCnfOperationRequest(cnfToHeal.getVnfInstanceResponseLinks().getSelf(), cnfToHeal, user);
        verifyHealCnfResponse(restoreResponse);

        String operationUrl = restoreResponse.getHeaders().get(LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationUrl);

        Day0SecretVerificationInfo verificationInfo = collectDay0VerificationInfo(cnfToHeal);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                cnfToHeal, user, operationUrl, verificationInfo);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, HEAL);
        verifyCnfWasHealed(cnfToHeal);
    }

    private static void appendRestoreDay0Secrets(EvnfmCnf cnfToHeal) {
        cnfToHeal.getAdditionalParams()
                .computeIfPresent("day0.configuration.param1.value",
                                  (key, value) -> replaceRestoreCredentials(value, cnfToHeal.getCluster().getLocalPath()));
        cnfToHeal.getAdditionalParams()
                .computeIfPresent("day0.configuration.param2.value", (key, value) -> replaceRestoreUri(value, cnfToHeal));
    }

    private static Object replaceRestoreUri(final Object value, EvnfmCnf cnfToHeal) {
        final String valueString = value.toString();
        if (!valueString.contains(RESTORE_URI_PLACEHOLDER)) {
            return value;
        }

        final String sftpServer = getSftpServer(cnfToHeal.getCluster().getLocalPath(), cnfToHeal.getVnfInstanceName());

        return valueString.replace(RESTORE_URI_PLACEHOLDER, sftpServer);
    }

    private static Object replaceRestoreCredentials(final Object value, String clusterConfigPath) {
        final String valueString = value.toString();
        if (!valueString.contains(RESTORE_CREDENTIALS_PLACEHOLDER)) {
            return value;
        }
        String sftpPassword = getSftpCredentials(clusterConfigPath).getPassword();
        return valueString.replace(RESTORE_CREDENTIALS_PLACEHOLDER, sftpPassword);
    }

    public static void performHealCnfStepExpectingError(EvnfmCnf cnfToHeal, User user) {
        try {
            performHealCnfStep(cnfToHeal, user);
        } catch (HttpClientErrorException exception) {
            verifyExpectedHttpError(exception, cnfToHeal.getExpectedError());
        }
    }
}
