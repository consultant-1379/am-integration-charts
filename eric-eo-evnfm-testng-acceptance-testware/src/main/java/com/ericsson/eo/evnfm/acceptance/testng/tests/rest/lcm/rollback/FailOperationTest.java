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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.FailOperationDataProviders.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.FailOperationDataProviders.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfFailureStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class FailOperationTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FailOperationTest.class);

    @BeforeClass
    public void setup(ITestContext iTestContext) throws IOException {
        // Instantiate cnf
        LOGGER.info("FailOperationTest : starts setup step");

        LOGGER.info("FailOperationTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("FailOperationTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("FailOperationTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);

        LOGGER.info("FailOperationTest : setup test was completed successfully");
    }

    @Test
    public void testFailOperationAfterFailedUpgrade(ITestContext iTestContext) throws IOException {
        LOGGER.info("FailOperationTest : load modifyVnfInfo and instantiate data");
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);

        LOGGER.info("FailOperationTest : starts failure upgrade CNF {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfFailureStep(cnfToUpgrade, user);
        LOGGER.info("FailOperationTest : failure upgrade CNF {} completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("FailOperationTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("FailOperationTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("FailOperationTest : cleanup step completed successfully");
    }
}
