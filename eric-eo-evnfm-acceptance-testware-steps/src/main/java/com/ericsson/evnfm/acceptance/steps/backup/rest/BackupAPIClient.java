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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.delay;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.utils.Constants.BACKUP_URL;
import static com.ericsson.evnfm.acceptance.utils.Constants.GET_BACKUP_SCOPES_URL;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.BackupsResponseDto;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class BackupAPIClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupAPIClient.class);

    private BackupAPIClient() {
    }

    public static String queryVnfInstanceId(String vnfInstanceName, User user) {
        Optional<VnfInstanceResponse> name = Optional.ofNullable(getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                         vnfInstanceName, user));
        return name.get().getId();
    }

    public static ResponseEntity<Void> executeBackupLCMOperationRequest(BackupRequest backupsRequest, User user) {

        String vnfInstanceId = queryVnfInstanceId(backupsRequest.getVnfInstanceName(), user);
        LOGGER.info("Start backup operation for instance {}", vnfInstanceId);

        String backupUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(BACKUP_URL, vnfInstanceId);
        final HttpEntity<BackupRequest> entity = new HttpEntity<>(backupsRequest, createHeaders(user));
        long startTime = System.currentTimeMillis();
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(context -> getRestTemplate()
                .postForEntity(backupUrl, entity, Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds", backupsRequest,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 15);

        LOGGER.info("Backup is completed with status {}", getReasonPhrase(response.getStatusCode()));
        return response;
    }

    public static ResponseEntity<BackupsResponseDto[]> getBackupLCMOperation(BackupRequest backupsRequest,
                                                                             User user) {
        var vnfInstanceId = queryVnfInstanceId(backupsRequest.getVnfInstanceName(), user);

        String backupUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(BACKUP_URL, vnfInstanceId);
        final HttpEntity<BackupRequest> entity = new HttpEntity<>(createHeaders(user));

        final ResponseEntity<BackupsResponseDto[]> response = executeOperationWithLogs(backupUrl, HttpMethod.GET, entity, BackupsResponseDto[].class);
        LOGGER.info("Request is completed with status {}", getReasonPhrase(response.getStatusCode()));

        return response;
    }

    public static ResponseEntity<String[]> getBackupScopesLCMOperation(BackupRequest backupsRequest,
                                                                       User user) {
        var vnfInstanceId = queryVnfInstanceId(backupsRequest.getVnfInstanceName(), user);

        String backupUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(GET_BACKUP_SCOPES_URL, vnfInstanceId);
        final HttpEntity<BackupRequest> entity = new HttpEntity<>(createHeaders(user));

        final ResponseEntity<String[]> response = executeOperationWithLogs(backupUrl, HttpMethod.GET, entity, String[].class);
        LOGGER.info("Request is completed with status {}", getReasonPhrase(response.getStatusCode()));

        return response;
    }

    public static ResponseEntity<BackupsResponseDto[]> waitingForBackupCompletion(BackupRequest backupsRequest,
                                                                                  User user) {

        StopWatch stopwatch = StopWatch.createStarted();
        BackupsResponseDto.StatusEnum currentBackupState = null;
        int timeOut = 60;
        ResponseEntity<BackupsResponseDto[]> response = null;

        while (stopwatch.getTime(TimeUnit.SECONDS) < timeOut) {
            delay(7000);
            response = getBackupLCMOperation(backupsRequest, user);
            if(response.getBody() == null || response.getBody().length == 0) {
                continue;
            }
            currentBackupState = (Objects.requireNonNull(response.getBody()))[0].getStatus();

            if (currentBackupState.equals(BackupsResponseDto.StatusEnum.COMPLETE)) {
                LOGGER.info("Backup is completed successfully {}", (Object) response.getBody());
                break;
            }
        }

        assertThat(currentBackupState).isEqualTo(BackupsResponseDto.StatusEnum.COMPLETE);
        return response;
    }
}
