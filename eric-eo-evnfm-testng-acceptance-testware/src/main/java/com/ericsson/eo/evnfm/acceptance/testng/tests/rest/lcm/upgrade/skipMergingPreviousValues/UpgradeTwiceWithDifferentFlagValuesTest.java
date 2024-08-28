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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeTwiceWithDifferentFlagValuesDataProviders.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeTwiceWithDifferentFlagValuesDataProviders.loadSecondUpgradeConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeTwiceWithDifferentFlagValuesDataProviders.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
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

public class UpgradeTwiceWithDifferentFlagValuesTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTwiceWithDifferentFlagValuesTest.class);

    @BeforeClass
    public void instantiate(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : starts setup step");

        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);
    }

    @Test
    public void upgradeTest(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : load first upgrade data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : starts first Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmValues(cnfToInstantiate, cnfToUpgrade);
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : First Upgrade a CNF : {} was completed successfully",
                    cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : load second upgrade data");
        EvnfmCnf secondCnfToUpgrade = loadSecondUpgradeConfigData(iTestContext);
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : starts second Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(secondCnfToUpgrade, user);
        verifyHelmValues(secondCnfToUpgrade);
        verifyHelmHistory(secondCnfToUpgrade);
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : Second Upgrade a CNF : {} was completed successfully",
                    secondCnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeTwiceWithDifferentFlagValuesTest : cleanup step completed successfully");
    }
}
