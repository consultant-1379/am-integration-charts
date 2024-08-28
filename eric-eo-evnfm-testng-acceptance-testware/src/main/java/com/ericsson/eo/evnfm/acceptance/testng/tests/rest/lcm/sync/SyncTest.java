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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.sync;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.executeUpgradeReleaseForScaleCnfStep;
import static com.ericsson.evnfm.acceptance.steps.sync.rest.SyncSteps.performSync;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.SyncDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class SyncTest extends RestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(SyncTest.class);

    @Test(description = "EVNFM_LCM_Sync: Instantiate REST test", dataProvider =
            "loadInstantiateConfigData",
            dataProviderClass = SyncDataProviders.class, priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("SyncTest : starts instantiation step");

        LOGGER.info("SyncTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyNumberOfTargets(cnfToInstantiate);
        LOGGER.info("SyncTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : setup test was completed successfully");
    }

    @Test(description = "EVNFM_LCM_Sync: Sync REST test", dataProvider =
            "getValidSyncCnaData",
            dataProviderClass = SyncDataProviders.class, priority = 2)
    public void testSync(EvnfmCnf cnaToScale) {
        LOGGER.info("SyncTest : Starts perform MANUAL scale");

        LOGGER.info("ScaleSuccessTest : execute Upgrade Release step");
        executeUpgradeReleaseForScaleCnfStep(cnaToScale);
        LOGGER.info("SyncTest : Finish manual scale performing");

        LOGGER.info("SyncTest : Verify number of targets");
        verifyNumberOfTargets(cnaToScale);
        LOGGER.info("SyncTest : Number of targets verification is finished successfully");

        LOGGER.info("SyncTest : Start performing sync operation");
        performSync(cnaToScale, user);

        LOGGER.info("SyncTest : Sync operation is performed successfully");

        LOGGER.info("SyncTest : Test 'Replica count in database for aspect' is finished successfully");
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("ScaleSuccessTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("ScaleSuccessTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("ScaleSuccessTest : cleanup step completed successfully");
    }
}
