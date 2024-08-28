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
package com.ericsson.eo.evnfm.acceptance.testng.tests.postdeployment;

import static com.ericsson.evnfm.acceptance.steps.bro.BackupRestoreOrchestratorSteps.findBRAgentsInEvnfmNamespace;
import static com.ericsson.evnfm.acceptance.steps.bro.BackupRestoreOrchestratorVerify.verifyAllAgentsRegisteredInBRO;

import java.util.Set;

import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BackupRestoreOrchestratorDataProvider;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.Base;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;

public class BackupRestoreOrchestratorTest extends Base {

    @Test(description = "Verify all backup/restore agents successfully registered in BRO",
            dataProvider = "testAllBackupRestoreAgentsRegisteredData", dataProviderClass = BackupRestoreOrchestratorDataProvider.class)
    public void testAllBackupRestoreAgentsRegistered(final ClusterConfig config) {
        final Set<String> agentsInEvnfmNamespace = findBRAgentsInEvnfmNamespace(config);

        verifyAllAgentsRegisteredInBRO(agentsInEvnfmNamespace, user);
    }
}
