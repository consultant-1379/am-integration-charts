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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLCMDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class BasicLCMTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicLCMTest.class);

    @Test(description = "Instantiate REST test", dataProvider = "getInstancesToInstantiate",
            dataProviderClass = BasicLCMDataProviders.class, priority = 1)
    public void testInstantiateREST(EvnfmCnf cnfToInstantiate) {

        LOGGER.info("BasicLCMTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyHelmHistory(cnfToInstantiate);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("BasicLCMTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "Upgrade REST test", dataProvider = "getInstancesToUpgradeSuccess",
            dataProviderClass = BasicLCMDataProviders.class, priority = 2, dependsOnMethods = "testInstantiateREST")
    public void testUpgradeREST(EvnfmCnf cnfToSuccessfulUpgrade) {
        LOGGER.info("BasicLCMTest : starts Upgrade a CNF : {}", cnfToSuccessfulUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToSuccessfulUpgrade, user);
        verifyHelmHistory(cnfToSuccessfulUpgrade);
        LOGGER.info("BasicLCMTest : Upgrade a CNF : {} was completed  successfully",
                    cnfToSuccessfulUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("BasicLCMTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("BasicLCMTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("BasicLCMTest : cleanup step completed successfully");
    }
}