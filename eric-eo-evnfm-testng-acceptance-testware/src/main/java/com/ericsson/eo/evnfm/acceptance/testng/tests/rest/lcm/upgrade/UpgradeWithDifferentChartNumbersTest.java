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
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyFirstChartName;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfHelmReleasesChartNames;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeWithDifferentChartNumbersTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class UpgradeWithDifferentChartNumbersTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeWithDifferentChartNumbersTest.class);

    @Test(description = "EVNFM_LCM_instantiate: Instantiate REST test", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = UpgradeWithDifferentChartNumbersTestDataProviders.class, priority = 1)
    public void instantiateTest(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("UpgradeWithDifferentChartNumbersTest : starts Instantiate a CNF : {} for further upgrade " +
                            "to a package with a different number of charts", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyFirstChartName(cnfToInstantiate);
        verifyHelmHistory(cnfToInstantiate);
        verifyNumberOfHelmReleasesChartNames(cnfToInstantiate);
        verifyNumberOfTargets(cnfToInstantiate);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("UpgradeWithDifferentChartNumbersTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_upgrade: Upgrade REST test", dataProvider = "getInstancesToUpgrade",
            dataProviderClass = UpgradeWithDifferentChartNumbersTestDataProviders.class, priority = 2, dependsOnMethods = "instantiateTest")
    public void upgradeTest(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("UpgradeWithDifferentChartNumbersTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmHistory(cnfToUpgrade);
        verifyFirstChartName(cnfToUpgrade);
        LOGGER.info("UpgradeWithDifferentChartNumbersTest : Upgrade a CNF : {} was completed  successfully",
                    cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeWithDifferentChartNumbersTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeWithDifferentChartNumbersTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeWithDifferentChartNumbersTest : cleanup step completed successfully");
    }
}
