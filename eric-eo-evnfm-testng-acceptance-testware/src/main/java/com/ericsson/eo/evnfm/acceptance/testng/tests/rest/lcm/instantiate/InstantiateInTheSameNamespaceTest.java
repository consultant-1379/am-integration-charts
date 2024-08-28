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
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfHelmReleasesChartNames;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateAndDeleteIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.InstantiateInTheSameNamespaceTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class InstantiateInTheSameNamespaceTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateInTheSameNamespaceTest.class);


    @Test(description = "EVNFM_LCM_instantiate_same_namespace: Instantiate REST test in the same namespace", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = InstantiateInTheSameNamespaceTestDataProviders.class, priority = 1)
    public void testInstantiateInTheSameNamespace(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("InstantiateInTheSameNamespaceTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("InstantiateInTheSameNamespaceTest : Instantiate a CNF : {} was completed successfully",
                cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_instantiate_same_namespace: Terminate REST test first CNF. Verify second CNF still exists", dataProvider =
            "getInstancesToTerminate",
            dataProviderClass = InstantiateInTheSameNamespaceTestDataProviders.class, priority = 2)
    public void testTerminateFirstCnf(EvnfmCnf cnfToTerminate) {
        LOGGER.info("InstantiateInTheSameNamespaceTest : starts Terminate first CNF : {}", cnfToTerminate.getVnfInstanceName());
        performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);
        LOGGER.info("InstantiateInTheSameNamespaceTest : Terminate first CNF : {} was completed successfully",
                cnfToTerminate.getVnfInstanceName());
        LOGGER.info("InstantiateInTheSameNamespaceTest : Verify second CNF exists in namespace : {}", cnfToTerminate.getNamespace());
        verifyHelmHistory(cnfToTerminate);
        verifyNumberOfHelmReleasesChartNames(cnfToTerminate);
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("InstantiateInTheSameNamespaceTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("InstantiateInTheSameNamespaceTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("InstantiateInTheSameNamespaceTest : cleanup step completed successfully");
    }
}
