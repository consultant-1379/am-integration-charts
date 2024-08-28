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
package com.ericsson.evnfm.acceptance.steps.backup.rest;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupAPIClient.executeBackupLCMOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupAPIClient.waitingForBackupCompletion;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBackupCnfResponse;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBackupDetails;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.getSftpCredentials;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.getSftpServer;

import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.BackupExportParams;
import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.SftpUsersSecret;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.BackupsResponseDto;

public class BackupSteps {

    public static void performBackupCnfStep(BackupRequest payload, User user) {

        ResponseEntity<Void> response = executeBackupLCMOperationRequest(payload, user);
        verifyBackupCnfResponse(response);

        ResponseEntity<BackupsResponseDto[]> backupDetails = waitingForBackupCompletion(payload, user);
        verifyBackupDetails(backupDetails, payload);
    }

    public static void performExportBackupStep(BackupRequest payload, User user) {
        SftpUsersSecret.SftpUser sftpUser = getSftpCredentials(payload.getCluster().getLocalPath());
        BackupExportParams backupExportParams = new BackupExportParams();
        backupExportParams.setPassword(sftpUser.getPassword());
        backupExportParams.setHost(getSftpServer(payload.getCluster().getLocalPath(), payload.getVnfInstanceName()));
        payload.getAdditionalParams().setRemote(backupExportParams);
        ResponseEntity<Void> response = executeBackupLCMOperationRequest(payload, user);
        verifyBackupCnfResponse(response);
    }
}