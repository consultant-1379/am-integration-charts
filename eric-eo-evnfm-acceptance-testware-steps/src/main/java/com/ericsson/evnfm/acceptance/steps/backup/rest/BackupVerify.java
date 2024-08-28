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

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupAPIClient.getBackupScopesLCMOperation;
import static com.ericsson.evnfm.acceptance.utils.Constants.HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.runCommand;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Arrays;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpServerErrorException;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.BroActionsResponse;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.BackupsResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class BackupVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupVerify.class);

    private BackupVerify() {
    }

    public static void verifyBackupCnfResponse(ResponseEntity<Void> response) {
        LOGGER.info("Starting Backup verification of status " + HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getHeaders()).isNotNull();

        final HttpHeaders headers = response.getHeaders();

        assertThat(CollectionUtils.isEmpty(headers)).withFailMessage(HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE).isFalse();
        LOGGER.info("Backup verification is completed successfully");
    }

    public static void verifyBroAgentIsUp(BackupRequest backupRequest, User user) {
        await().atMost(120, SECONDS).pollInterval(5, SECONDS).until(() -> scopesExist(backupRequest, user));
        LOGGER.info("Bro agent is up for vnf instance name {} in namespace {}", backupRequest.getVnfInstanceName(), backupRequest.getNamespace());
    }

    private static boolean scopesExist(BackupRequest backupRequest, User user) {
        ResponseEntity<String[]> broScopesResponse;
        try {
            broScopesResponse = getBackupScopesLCMOperation(backupRequest, user);
        } catch (HttpServerErrorException ex) {
            LOGGER.warn("Bro agent is not available yet: {}", ex.getMessage());
            return false;
        }
        return broScopesResponse.getStatusCode().is2xxSuccessful()
                && Arrays.asList(Objects.requireNonNull(broScopesResponse.getBody())).contains(backupRequest.getAdditionalParams().getScope());
    }

    public static void verifyBackupDetails(ResponseEntity<BackupsResponseDto[]> response, BackupRequest request) {

        Arrays.stream(Objects.requireNonNull(response.getBody())).forEach(value -> {
            assertThat(value.getStatus()).isEqualTo(BackupsResponseDto.StatusEnum.COMPLETE);
            assertThat(value.getScope()).isEqualTo(request.getAdditionalParams().getScope());
            assertThat(value.getName()).isEqualTo(request.getAdditionalParams().getBackupName());
            assertThat(value.getId()).isEqualTo(request.getAdditionalParams().getBackupName());
            assertThat(value.getCreationTime()).isNotNull();
        });

        LOGGER.info("Backup response verification is completed successfully");
    }

    public static void verifyBackupWasCreated(BackupRequest payload) {
        await().atMost(120, SECONDS).until(() -> verifyBackupAction(payload.getCluster().getLocalPath(),
                                                                    payload.getNamespace(),
                                                                    payload.getAdditionalParams().getScope(),
                                                                    "CREATE_BACKUP"));
    }

    public static void verifyBackupWasExported(BackupRequest payload) {
        await().atMost(90, SECONDS).until(() -> verifyBackupAction(payload.getCluster().getLocalPath(),
                                                                   payload.getNamespace(), payload.getAdditionalParams().getScope(), "EXPORT"));
    }

    public static boolean verifyBackupAction(String clusterConfigPath, String cnfNamespace, String scope, String actionName) {
        String broEndpoint = String.format("http://eric-ctrl-bro.%s:7001", cnfNamespace);
        String checkBackupExportCommand = String.format("kubectl --kubeconfig %s -n %s exec -it "
                                                                + "$(kubectl --kubeconfig %1$s -n %2$s get pods | grep orchestrator | awk '{print $1}') "
                                                                + " -- /bin/bash -c 'curl -s %s/v1/backup-manager/%s/action'",
                                                        clusterConfigPath,
                                                        EVNFM_INSTANCE.getNamespace(),
                                                        broEndpoint,
                                                        scope);
        String commandResult = runCommand(checkBackupExportCommand, 30, false);
        try {
            JsonNode jsonNode = getObjectMapper().readTree(commandResult);
            ArrayNode actions = jsonNode.withArray("actions");
            for (JsonNode action : actions) {
                String actionAsString = getObjectMapper().writeValueAsString(action);
                BroActionsResponse broActionsResponse = getObjectMapper().readValue(actionAsString, BroActionsResponse.class);
                if (!actionName.equals(broActionsResponse.getName())) {
                    continue;
                }
                if ("SUCCESS".equals(broActionsResponse.getResult())) {
                    LOGGER.info("Backup action : {}  with id {} was successful in namespace {}.", actionName,
                                broActionsResponse.getId(), cnfNamespace);
                    return true;
                }
                assertThat(broActionsResponse.getResult())
                        .withFailMessage(String.format("Backup action: %s with id %s has failed in namespace %s.", actionName,
                                                       broActionsResponse.getId(), cnfNamespace))
                        .isNotEqualTo("FAILURE");
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException occurred when verifying backup action: {}", e.getMessage());
            return false;
        }
        return false;
    }
}