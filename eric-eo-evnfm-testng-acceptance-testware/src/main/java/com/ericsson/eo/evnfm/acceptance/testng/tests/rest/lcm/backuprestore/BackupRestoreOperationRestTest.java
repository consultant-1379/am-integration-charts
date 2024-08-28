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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.backuprestore;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupSteps.performBackupCnfStep;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupSteps.performExportBackupStep;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBackupWasCreated;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBackupWasExported;
import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupVerify.verifyBroAgentIsUp;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.addFileToStatefulPod;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.verifyFileContent;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfSteps.performHealCnfStep;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfVerify.verifyCnfWasRestoredFromBackup;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BackupRestoreDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class BackupRestoreOperationRestTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRestoreOperationRestTest.class);

    @Test(description = "EVNFM_LCM_backup_restore: Instantiate CNF with bro agent", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = BackupRestoreDataProviders.class, priority = 1)
    public void instantiate(EvnfmCnf cnfToInstantiate) throws IOException {
        LOGGER.info("BackupRestoreOperationRestTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("BackupRestoreOperationRestTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_backup_restore: Backup CNF", dataProvider =
            "getBackupConfigData",
            dataProviderClass = BackupRestoreDataProviders.class, priority = 2)
    public void testBackupCNF(BackupRequest backupRequest) {
        LOGGER.info("BackupRestoreOperationRestTest : starts verifying bro agent is up and running");
        verifyBroAgentIsUp(backupRequest, user);
        LOGGER.info("BackupRestoreOperationRestTest : starts Backup CNF operation for CNF : {}", backupRequest.getVnfInstanceName());
        addFileToStatefulPod(backupRequest, "text.txt", "before backup");
        verifyFileContent(backupRequest.getNamespace(), backupRequest.getCluster().getLocalPath(), "text.txt", "before backup");
        performBackupCnfStep(backupRequest, user);
        verifyBackupWasCreated(backupRequest);

        addFileToStatefulPod(backupRequest, "text.txt", "after backup");
        verifyFileContent(backupRequest.getNamespace(), backupRequest.getCluster().getLocalPath(), "text.txt", "after backup");
        LOGGER.info("BackupRestoreOperationRestTest : Backup CNF operation for CNF : {} was completed successfully", backupRequest.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_backup_restore: Export backup", dataProvider =
            "getBackupConfigData",
            dataProviderClass = BackupRestoreDataProviders.class, priority = 3)
    public void testExportBackup(BackupRequest backupRequest) {
        LOGGER.info("BackupRestoreOperationRestTest : starts Backup CNF operation for CNF : {}", backupRequest.getVnfInstanceName());
        performExportBackupStep(backupRequest, user);
        verifyBackupWasExported(backupRequest);
        LOGGER.info("BackupRestoreOperationRestTest : Backup CNF operation for CNF : {} was completed successfully", backupRequest.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_backup_restore: Restore CNF from backup", dataProvider = "getInstancesToHeal", dataProviderClass =
            BackupRestoreDataProviders.class,
            priority = 4)
    public void testHealCnf(EvnfmCnf cnfToHeal) {
        LOGGER.info("BackupRestoreOperationRestTest : starts Heal a CNF : {}", cnfToHeal.getVnfInstanceName());
        performHealCnfStep(cnfToHeal, user);
        verifyCnfWasRestoredFromBackup(cnfToHeal);
        verifyFileContent(cnfToHeal.getNamespace(), cnfToHeal.getCluster().getLocalPath(), "text.txt", "before backup");
        LOGGER.info("BackupRestoreOperationRestTest : Heal a CNF : {} was completed successfully", cnfToHeal.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("BackupRestoreOperationRestTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("BackupRestoreOperationRestTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("BackupRestoreOperationRestTest : cleanup step completed successfully");
    }
}
