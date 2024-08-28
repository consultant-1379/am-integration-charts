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

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBackupAction;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.utils.Constants.HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class HealCnfVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealCnfSteps.class);

    private HealCnfVerify() {
    }

    public static void verifyCnfWasHealed(EvnfmCnf cnfToHeal) {
        verifyHelmHistory(cnfToHeal);
        if (cnfToHeal.getExpectedHelmValues() != null) {
            verifyHelmValues(cnfToHeal);
        }
    }

    public static void verifyCnfWasRestoredFromBackup(EvnfmCnf cnfToHeal) {
        await().atMost(100, SECONDS).until(() -> verifyBackupAction(cnfToHeal.getCluster().getLocalPath(),
                                                                    cnfToHeal.getNamespace(),
                                                                    (String) cnfToHeal.getAdditionalParams().get("restore.scope"), "IMPORT"));
        await().atMost(150, SECONDS).until(() -> verifyBackupAction(cnfToHeal.getCluster().getLocalPath(),
                                                                   cnfToHeal.getNamespace(),
                                                                   (String) cnfToHeal.getAdditionalParams().get("restore.scope"), "RESTORE"));
    }

    public static void verifyHealCnfResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.HEAL, HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getHeaders()).isNotNull();

        final List<String> headers = response.getHeaders().get(LOCATION);
        assertThat(CollectionUtils.isEmpty(headers)).withFailMessage(HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE).isFalse();
        assertThat(headers.get(0)).isNotNull();
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.HEAL);
    }
}
