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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.upgrade.skipMergingPreviousValues;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithManualRollbackDataProvider.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithManualRollbackDataProvider.loadRollbackConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithManualRollbackDataProvider.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackSteps.performRollbackStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class UpgradeWithManualRollbackTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeWithManualRollbackTest.class);

    @BeforeClass
    public void setup(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithManualRollbackTest : starts setup step");

        LOGGER.info("UpgradeWithManualRollbackTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeWithManualRollbackTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeWithManualRollbackTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);
        LOGGER.info("UpgradeWithManualRollbackTest : setup test was completed successfully");
    }

    @Test
    public void testRollbackAtFailure(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithManualRollbackTest : load upgrade data");
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);

        LOGGER.info("UpgradeWithManualRollbackTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user, VnfLcmOpOcc.OperationStateEnum.FAILED_TEMP);
        LOGGER.info("UpgradeWithManualRollbackTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("UpgradeWithManualRollbackTest : load rollback data");
        final EvnfmCnf cnfToRollback = loadRollbackConfigData(iTestContext);

        LOGGER.info("UpgradeWithManualRollbackTest : starts Rollback a CNF : {}", cnfToRollback.getVnfInstanceName());
        performRollbackStep(cnfToRollback, user);
        LOGGER.info("UpgradeWithManualRollbackTest : Rollback a CNF : {} was completed successfully", cnfToRollback.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeWithManualRollbackTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeWithManualRollbackTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeWithManualRollbackTest : cleanup step completed successfully");
    }
}
