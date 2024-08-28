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
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackVerify.verifyRollback;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performRollbackCnfAfterSuccessfulUpgradeStep;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.RollbackAtSuccessDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class RollbackAtSuccessTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackAtSuccessTest.class);

    @Test(description = "EVNFM_LCM_Rollback_at_success: Instantiate REST test", dataProvider =
            "loadInstantiateConfigData",
            dataProviderClass = RollbackAtSuccessDataProviders.class, priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("RollbackAtSuccessTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("RollbackAtSuccessTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Rollback_at_success:: Upgrade REST test", dataProvider =
            "loadUpgradeConfigData",
            dataProviderClass = RollbackAtSuccessDataProviders.class, priority = 2)
    public void testUpgrade(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("RollbackAtSuccessTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        LOGGER.info("RollbackAtSuccessTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Rollback_at_success:: Rollback REST test", dataProvider =
            "loadRollbackConfigData",
            dataProviderClass = RollbackAtSuccessDataProviders.class, priority = 3)
    public void testRollbackAtSuccess(EvnfmCnf cnfToRollback) {
        LOGGER.info("RollbackAtSuccessTest : starts Rollback After Successful Upgrade a CNF : {}", cnfToRollback.getVnfInstanceName());
        performRollbackCnfAfterSuccessfulUpgradeStep(cnfToRollback, cnfToRollback.getSourceVnfdId(), user);
        verifyRollback(cnfToRollback, user);
        LOGGER.info("RollbackAtSuccessTest : Rollback After Successful Upgrade a CNF : {} completed successfully",
                    cnfToRollback.getVnfInstanceName());
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
