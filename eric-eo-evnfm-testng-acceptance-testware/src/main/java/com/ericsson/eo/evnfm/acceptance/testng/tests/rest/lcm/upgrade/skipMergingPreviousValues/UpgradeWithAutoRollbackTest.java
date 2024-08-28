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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithAutoRollbackDataProvider.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithAutoRollbackDataProvider.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfAutoRollbackStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class UpgradeWithAutoRollbackTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeWithAutoRollbackTest.class);

    @BeforeClass
    public void setup(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithAutoRollbackTest : starts setup step");

        LOGGER.info("UpgradeWithAutoRollbackTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeWithAutoRollbackTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeWithAutoRollbackTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);
    }

    @Test
    public void testAutoRollbackAtFailure(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithAutoRollbackTest : load upgrade data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);

        LOGGER.info("UpgradeWithAutoRollbackTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfAutoRollbackStep(cnfToUpgrade, user);
        verifyHelmValues(cnfToInstantiate);
        LOGGER.info("UpgradeWithAutoRollbackTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeWithAutoRollbackTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeWithAutoRollbackTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeWithAutoRollbackTest : cleanup step completed successfully");
    }
}
