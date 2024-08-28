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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithRollbackDataProvider.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithRollbackDataProvider.loadRollbackConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithRollbackDataProvider.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNotContainHelmValues;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.rollback.rest.RollbackVerify.verifyRollback;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performRollbackCnfAfterSuccessfulUpgradeStep;
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

public class UpgradeWithRollbackAtSuccessTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeWithRollbackAtSuccessTest.class);

    @BeforeClass
    public void setup(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : starts setup step");

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : setup test was completed successfully");
    }

    @Test
    public void testRollbackAtSuccess(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : load upgrade data");
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmValues(cnfToInstantiate, cnfToUpgrade);
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : load rollback data");
        final EvnfmCnf cnfToRollback = loadRollbackConfigData(iTestContext);

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : starts Rollback After Successful Upgrade a CNF : {}", cnfToRollback.getVnfInstanceName());
        performRollbackCnfAfterSuccessfulUpgradeStep(cnfToRollback, cnfToUpgrade.getVnfdId(), user);
        verifyRollback(cnfToRollback, user);
        verifyNotContainHelmValues(cnfToUpgrade, cnfToRollback);
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : Rollback After Successful Upgrade a CNF : {} completed successfully",
                    cnfToRollback.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeWithRollbackAtSuccessTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeWithRollbackAtSuccessTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeWithRollbackAtSuccessTest : cleanup step completed successfully");
    }
}
