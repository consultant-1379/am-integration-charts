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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithHealDataProvider.loadHealConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithHealDataProvider.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithHealDataProvider.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfSteps.performHealCnfStep;
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

public class UpgradeWithHealTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTwiceWithDifferentFlagValuesTest.class);

    @BeforeClass
    public void instantiate(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithHealTest : starts setup step");

        LOGGER.info("UpgradeWithHealTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("UpgradeWithHealTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeWithHealTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);
    }

    @Test
    public void upgradeTest(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeWithHealTest : load upgrade data");
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);
        LOGGER.info("UpgradeWithHealTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmValues(cnfToInstantiate, cnfToUpgrade);
        LOGGER.info("UpgradeWithHealTest : Upgrade a CNF : {} was completed successfully",
                    cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("UpgradeWithHealTest : load heal data");
        EvnfmCnf cnfToHeal = loadHealConfigData(iTestContext);
        LOGGER.info("UpgradeWithHealTest : starts Heal a CNF : {}", cnfToHeal.getVnfInstanceName());
        performHealCnfStep(cnfToHeal, user);
        verifyHelmValues(cnfToInstantiate, cnfToHeal);
        LOGGER.info("UpgradeWithHealTest : Heal a CNF : {} was completed successfully", cnfToHeal.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeWithHealTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeWithHealTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeWithHealTest : cleanup step completed successfully");
    }
}
