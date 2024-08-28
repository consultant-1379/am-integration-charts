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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.enm;

import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.scaleResource;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.enm.EnmApiClient.executeAddNodeToEnmRequest;
import static com.ericsson.evnfm.acceptance.steps.enm.EnmApiClient.executeDeleteNodeFromEnmRequest;
import static com.ericsson.evnfm.acceptance.steps.enm.EnmVerify.verifyAddedToOss;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AddDeleteNodeDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class AddDeleteNodeTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddDeleteNodeTest.class);

    @Test(description = "EVNFM_LCM_Add_delete_node: Instantiate REST test with add node to ENM", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = AddDeleteNodeDataProviders.class, priority = 1)
    public void testInstantiateWithAddNodeToEnm(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("AddDeleteNodeTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        verifyAddedToOss(cnfToInstantiate, user, true);
        LOGGER.info("AddDeleteNodeTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Add_delete_node: Delete node from ENM REST test", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = AddDeleteNodeDataProviders.class, priority = 2)
    public void testDeleteNodeFromEnm(EvnfmCnf cnfToAddNodeToEnm) {
        LOGGER.info("AddDeleteNodeTest : starts deleting node from ENM for CNF : {}", cnfToAddNodeToEnm.getVnfInstanceName());
        executeDeleteNodeFromEnmRequest(cnfToAddNodeToEnm, user);
        verifyAddedToOss(cnfToAddNodeToEnm, user, false);
        LOGGER.info("AddDeleteNodeTest : deleting node for {} was completed successfully",
                    cnfToAddNodeToEnm.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Add_delete_node: Delete node second time from ENM REST test", dataProvider =
            "getInstancesToDeleteFromEnmSecondTime",
            dataProviderClass = AddDeleteNodeDataProviders.class, priority = 3)
    public void testSecondDeleteNodeFromEnmFail(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("AddDeleteNodeTest : starts deleting node from ENM for CNF second time : {}", cnfToInstantiate.getVnfInstanceName());
        executeDeleteNodeFromEnmRequest(cnfToInstantiate, user);
        verifyAddedToOss(cnfToInstantiate, user, false);
        LOGGER.info("AddDeleteNodeTest :  deleting node for {} was completed has failed",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Add_delete_node: Add node to ENM REST test", dataProvider =
            "getInstancesToAddToEnm",
            dataProviderClass = AddDeleteNodeDataProviders.class, priority = 4)
    public void testAddNodeToEnm(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("AddDeleteNodeTest : starts adding node to ENM for CNF : {}", cnfToInstantiate.getVnfInstanceName());
        executeAddNodeToEnmRequest(cnfToInstantiate, user);
        verifyAddedToOss(cnfToInstantiate, user, true);
        LOGGER.info("AddDeleteNodeTest : adding node to ENM for {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Add_delete_node: Delete node from ENM REST test when ENM is not accessible", dataProvider =
            "getInstancesToDeleteFromEnmNotAccessible",
            dataProviderClass = AddDeleteNodeDataProviders.class, priority = 5)
    public void testDeleteNodeFailsWhenENMisNotAccessible(EvnfmCnf cnfToInstantiate) throws InterruptedException {
        LOGGER.info("AddDeleteNodeTest : starts deleting node from ENM for CNF : {} when ENM is not available",
                    cnfToInstantiate.getVnfInstanceName());

        scaleResource(cnfToInstantiate.getCluster().getLocalPath(), EVNFM_INSTANCE.getNamespace(), "cvnfm-enm-cli-stub", "0");
        LOGGER.info("AddDeleteNodeTest : Waiting for ENM stub to shut down");
        TimeUnit.SECONDS.sleep(80);

        try {
            executeDeleteNodeFromEnmRequest(cnfToInstantiate, user);
            verifyAddedToOss(cnfToInstantiate, user, true);
            LOGGER.info("AddDeleteNodeTest : deleting node for {} has failed as expected", cnfToInstantiate.getVnfInstanceName());
        } finally {
            scaleResource(cnfToInstantiate.getCluster().getLocalPath(), EVNFM_INSTANCE.getNamespace(), "cvnfm-enm-cli-stub", "1");
            LOGGER.info("AddDeleteNodeTest : Waiting for ENM stub to start");
            TimeUnit.SECONDS.sleep(40);
        }
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("AddDeleteNodeTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("AddDeleteNodeTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("AddDeleteNodeTest : cleanup step completed successfully");
    }
}
