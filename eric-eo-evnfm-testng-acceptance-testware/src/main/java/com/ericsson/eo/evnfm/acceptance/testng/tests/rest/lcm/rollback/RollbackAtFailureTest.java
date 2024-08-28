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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.rollback;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackSteps.performRollbackStep;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackVerify.verifyRollback;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.RollbackAtFailureDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class RollbackAtFailureTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackAtFailureTest.class);

    @Test(description = "EVNFM_LCM_Rollback_at_failure: Instantiate REST test", dataProvider =
            "loadInstantiateConfigData",
            dataProviderClass = RollbackAtFailureDataProviders.class, priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("RollbackAtFailureTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("RollbackAtFailureTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Rollback_at_failure: Upgrade REST test failure", dataProvider =
            "loadUpgradeConfigData",
            dataProviderClass = RollbackAtFailureDataProviders.class, priority = 2)
    public void testUpgradeFails(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("RollbackAtFailureTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user, VnfLcmOpOcc.OperationStateEnum.FAILED_TEMP);
        LOGGER.info("RollbackAtFailureTest : Upgrade a CNF : {} with state FAILED_TEMP was completed successfully",
                    cnfToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Rollback_at_failure: Rollback REST test", dataProvider =
            "loadRollbackConfigData",
            dataProviderClass = RollbackAtFailureDataProviders.class, priority = 3)
    public void testRollbackAtFailure(EvnfmCnf cnfToRollback) {
        LOGGER.info("RollbackAtFailureTest : starts Rollback after failed Upgrade a CNF : {}", cnfToRollback.getVnfInstanceName());
        performRollbackStep(cnfToRollback, user);
        verifyRollback(cnfToRollback, user);
        LOGGER.info("RollbackAtFailureTest : Rollback after failed Upgrade for CNF : {} completed successfully", cnfToRollback.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("RollbackAtFailureTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("RollbackAtFailureTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("RollbackAtFailureTest : cleanup step completed successfully");
    }
}
