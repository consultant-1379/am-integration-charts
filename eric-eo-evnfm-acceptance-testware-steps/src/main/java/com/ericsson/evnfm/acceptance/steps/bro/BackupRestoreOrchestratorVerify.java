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
package com.ericsson.evnfm.acceptance.steps.bro;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.bro.BackupRestoreOrchestratorApiClient.retrieveAgentsRegisteredInBRO;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.BackupRestoreOrchestratorStatusResponse;
import com.ericsson.evnfm.acceptance.models.User;

public class BackupRestoreOrchestratorVerify {

    private BackupRestoreOrchestratorVerify() {
    }

    public static void verifyAllAgentsRegisteredInBRO(final Set<String> agentsInEvnfmNamespace, final User user) {
        final ResponseEntity<BackupRestoreOrchestratorStatusResponse> response = retrieveAgentsRegisteredInBRO(user);

        assertThat(response.getStatusCode())
                .withFailMessage(String.format("Expected BRO status response to be 200 OK status code, actual code is %s",
                                               response.getStatusCode()))
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .withFailMessage("BRO status response body should not be null")
                .isNotNull();

        assertThat(response.getBody().getRegisteredAgents())
                .withFailMessage(String.format("Registered agents in BRO status response does not match expected, actual is %s",
                                               response.getBody().getRegisteredAgents()))
                .isEqualTo(agentsInEvnfmNamespace);
    }
}
