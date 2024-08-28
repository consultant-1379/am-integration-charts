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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.modify;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoSteps.performModifyVnfInstanceInfoStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ModifyVnfInfoRestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class ModifyVnfInfoSuccessTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInfoSuccessTest.class);

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Create a VNF test", dataProvider =
            "loadCreateVnfConfigData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 1)
    public void testCreateVnf(EvnfmCnf cnfToCreate) {
        LOGGER.info("ModifyVnfInfoSuccessTest : Create a VNF Identifier: {}", cnfToCreate.getVnfInstanceName());
        performCreateCnfIdentifierStep(cnfToCreate, user);
        LOGGER.info("ModifyVnfInfoSuccessTest : Create a VNF Identifier : {} was completed successfully", cnfToCreate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Modify a CNF before instantiate", dataProvider =
            "loadModifyInfoBeforeInstantiateData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 2)
    public void testModifyVnfInfoAfterCreateVnf(EvnfmCnf cnfToModify) {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts Modify Info a CNF : {} before instantiate with new name {}",
                    cnfToModify.getVnfInstanceName(), cnfToModify.getVnfInstanceNameToModify());
        performModifyVnfInstanceInfoStep(cnfToModify, user);
        LOGGER.info("ModifyVnfInfoSuccessTest : Modify Info a a CNF : {} before instantiate with new name {} completed successfully",
                    cnfToModify.getVnfInstanceName(), cnfToModify.getVnfInstanceNameToModify());
    }

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Instantiate REST test", dataProvider =
            "loadInstantiateConfigData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 3)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) throws IOException {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("ModifyVnfInfoSuccessTest : Instantiate a VNF: {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Modify all aspects", dataProvider =
            "loadModifyAllAspectsAfterInstantiateData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 4)
    public void testModifyAllAspectsAfterInstantiate(EvnfmCnf cnfToModify) {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts Modify all aspects : {} after instantiate", cnfToModify.getVnfInstanceName());
        performModifyVnfInstanceInfoStep(cnfToModify, user);
        verifyHelmHistory(cnfToModify);
        LOGGER.info("ModifyVnfInfoSuccessTest : Modify VNF Info for CNF : {} was completed successfully",
                    cnfToModify.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Modify one aspect", dataProvider =
            "loadModifyOneAspectAfterInstantiateData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 5)
    public void testModifyOneAspectAfterInstantiate(EvnfmCnf cnfToModify) {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts Modify one aspect : {} after instantiate", cnfToModify.getVnfInstanceName());
        performModifyVnfInstanceInfoStep(cnfToModify, user);
        verifyHelmHistory(cnfToModify);
        LOGGER.info("ModifyVnfInfoSuccessTest : Modify VNF Info for CNF : {} was completed successfully",
                    cnfToModify.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Modify_vnf_info: Modify description", dataProvider =
            "loadModifyDescriptionAfterInstantiateData",
            dataProviderClass = ModifyVnfInfoRestDataProviders.class, priority = 6)
    public void testModifyDescriptionAfterInstantiate(EvnfmCnf cnfToModify) {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts Modify description : {} after instantiate", cnfToModify.getVnfInstanceName());
        performModifyVnfInstanceInfoStep(cnfToModify, user);
        verifyHelmHistory(cnfToModify);
        LOGGER.info("ModifyVnfInfoSuccessTest : Modify VNF Info for CNF : {} was completed successfully",
                    cnfToModify.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("ModifyVnfInfoSuccessTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("ModifyVnfInfoSuccessTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("ModifyVnfInfoSuccessTest : cleanup step completed successfully");
    }
}
