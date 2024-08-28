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
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNotContainHelmValues;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfHelmReleasesChartNames;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
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

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UpgradeUsingPatternTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class UpgradeUsingPatternTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeUsingPatternTest.class);

    @Test(description = "EVNFM LCM instantiate: Instantiate REST test with upgrade pattern", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = UpgradeUsingPatternTestDataProviders.class, priority = 1)
    public void instantiateWithUpgradePattern(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("UpgradeUsingPatternTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyHelmHistory(cnfToInstantiate);
        verifyNumberOfHelmReleasesChartNames(cnfToInstantiate);
        verifyNumberOfTargets(cnfToInstantiate);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("UpgradeUsingPatternTest : Instantiate a CNF : {} was completed successfully",
                cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM LCM upgrade using pattern: Upgrade REST test with upgrade pattern", dataProvider = "getCombinedData",
            dataProviderClass = UpgradeUsingPatternTestDataProviders.class, priority = 2, dependsOnMethods = "instantiateWithUpgradePattern")
    public void upgradeRollbackWithUpgradePattern(EvnfmCnf cnfToUpgrade, EvnfmCnf cnfToRollback) {
        LOGGER.info("UpgradeUsingPatternTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmHistory(cnfToUpgrade);
        LOGGER.info("UpgradeUsingPatternTest : Upgrade a CNF : {} was completed  successfully",
                cnfToUpgrade.getVnfInstanceName());


        LOGGER.info("UpgradeUsingPatternTest : starts Rollback After Successful Upgrade a CNF : {}", cnfToRollback.getVnfInstanceName());
        performRollbackCnfAfterSuccessfulUpgradeStep(cnfToRollback, cnfToUpgrade.getVnfdId(), user);
        verifyRollback(cnfToRollback, user);
        verifyNotContainHelmValues(cnfToUpgrade, cnfToRollback);
        LOGGER.info("UpgradeUsingPatternTest : Rollback After Successful Upgrade a CNF : {} completed successfully",
                cnfToRollback.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("UpgradeUsingPatternTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("UpgradeUsingPatternTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("UpgradeUsingPatternTest : cleanup step completed successfully");
    }
}
