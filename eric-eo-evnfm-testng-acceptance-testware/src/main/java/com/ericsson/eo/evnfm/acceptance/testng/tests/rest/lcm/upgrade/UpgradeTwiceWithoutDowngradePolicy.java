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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.upgrade;

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

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeTwiceWithoutDowngradePolicyDataProvider;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class UpgradeTwiceWithoutDowngradePolicy extends RestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeTwiceWithoutDowngradePolicy.class);

    @Test(description = "EVNFM_LCM_instantiate: Instantiate REST test", dataProvider =
            "getInstanceToInstantiate",
            dataProviderClass = UpgradeTwiceWithoutDowngradePolicyDataProvider.class, priority = 1)
    public void instantiateTest(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : starts setup step");

        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);
    }

    @Test(description = "EVNFM_LCM_upgrade: Upgrade REST test", dataProvider = "getInstanceToUpgrade",
            dataProviderClass = UpgradeTwiceWithoutDowngradePolicyDataProvider.class, priority = 2, dependsOnMethods = "instantiateTest")
    public void upgradeTest(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : starts first Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : First Upgrade a CNF : {} was completed successfully",
                    cnfToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_upgrade: Upgrade REST test", dataProvider = "getInstanceToSecondUpgrade",
            dataProviderClass = UpgradeTwiceWithoutDowngradePolicyDataProvider.class, priority = 3, dependsOnMethods = "upgradeTest")
    public void secondUpgradeTest(EvnfmCnf cnfToSecondUpgrade) {
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : starts second Upgrade a CNF : {}", cnfToSecondUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToSecondUpgrade, user);
        verifyHelmHistory(cnfToSecondUpgrade);
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : Second Upgrade a CNF : {} was completed successfully",
                    cnfToSecondUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeTwiceWithoutDowngradePolicyTest : cleanup step completed successfully");
    }
}
