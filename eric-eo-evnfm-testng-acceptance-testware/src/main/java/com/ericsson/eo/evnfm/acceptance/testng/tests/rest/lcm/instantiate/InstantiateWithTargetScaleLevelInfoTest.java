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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.instantiate;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.InstantiateWithTargetScaleLevelInfoTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class InstantiateWithTargetScaleLevelInfoTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateWithTargetScaleLevelInfoTest.class);

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_instantiate_targetScaleLevelInfo/version/0.1
     */
    @Test(description = "EVNFM_LCM_instantiate_targetScaleLevelInfo: Instantiate REST test with targetScaleLevelInfo", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = InstantiateWithTargetScaleLevelInfoTestDataProviders.class, priority = 1)
    public void instantiateWithTargetScaleLevelInfo(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("InstantiateWithTargetScaleLevelInfoTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("InstantiateWithTargetScaleLevelInfoTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("InstantiateWithTargetScaleLevelInfoTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("InstantiateWithTargetScaleLevelInfoTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("InstantiateWithTargetScaleLevelInfoTest : cleanup step completed successfully");
    }
}
