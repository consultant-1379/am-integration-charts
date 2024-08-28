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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.LcmWithNonScalableVdusTestDataProviders.loadScaleRequestConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.LcmWithNonScalableVdusTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class LcmWithNonScalableVdusTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LcmWithNonScalableVdusTest.class);

    @Test(description = "EVNFM_LCM_non_scalable_vdus: Instantiate REST test",
            dataProvider = "getInstancesToInstantiate",
            dataProviderClass = LcmWithNonScalableVdusTestDataProviders.class,
            priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("LcmWithNonScalableVdusTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());

        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyNumberOfTargets(cnfToInstantiate);

        cnfs.add(cnfToInstantiate);

        LOGGER.info("LcmWithNonScalableVdusTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_non_scalable_vdus: Scale REST test with non-scalable vdus negative",
            dataProvider = "getInstancesToScale",
            dataProviderClass = LcmWithNonScalableVdusTestDataProviders.class,
            priority = 2)
    public void testScaleNegative(EvnfmCnf cnfToScale, ITestContext iTestContext) throws IOException {
        LOGGER.info("LcmWithNonScalableVdusTest : starts Scale a CNF : {}", cnfToScale.getVnfInstanceName());

        final ScaleVnfRequest scaleVnfRequest = loadScaleRequestConfigData(iTestContext);

        performScaleCnfStepExpectingError(cnfToScale, scaleVnfRequest, user);

        LOGGER.info("LcmWithNonScalableVdusTest : Scale a CNF : {} was completed with failure as expected", cnfToScale.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_non_scalable_vdus: Upgrade REST test",
            dataProvider = "getInstancesToUpgrade",
            dataProviderClass = LcmWithNonScalableVdusTestDataProviders.class,
            priority = 3)
    public void testUpgrade(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("LcmWithNonScalableVdusTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());

        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyNumberOfTargets(cnfToUpgrade);

        LOGGER.info("LcmWithNonScalableVdusTest : Upgrade a CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("LcmWithNonScalableVdusTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("LcmWithNonScalableVdusTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("LcmWithNonScalableVdusTest : cleanup step completed successfully");
    }
}
