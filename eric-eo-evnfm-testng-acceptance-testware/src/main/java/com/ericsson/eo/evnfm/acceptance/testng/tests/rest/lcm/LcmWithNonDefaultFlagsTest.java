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
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.LcmWithNonDefaultFlagsTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class LcmWithNonDefaultFlagsTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LcmWithNonDefaultFlagsTest.class);

    @Test(description = "EVNFM_LCM_non_default_flags: Instantiate REST test with non-default flags",
            dataProvider = "getInstancesToInstantiate",
            dataProviderClass = LcmWithNonDefaultFlagsTestDataProviders.class,
            priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("LcmWithNonDefaultFlagsTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("LcmWithNonDefaultFlagsTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_non_default_flags: Upgrade REST test with non-default flags",
            dataProvider = "getInstancesToUpgrade",
            dataProviderClass = LcmWithNonDefaultFlagsTestDataProviders.class,
            priority = 2)
    public void testUpgrade(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("LcmWithNonDefaultFlagsTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        LOGGER.info("LcmWithNonDefaultFlagsTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("LcmWithNonDefaultFlagsTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("LcmWithNonDefaultFlagsTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("LcmWithNonDefaultFlagsTest : cleanup step completed successfully");
    }
}
