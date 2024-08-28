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
package com.ericsson.eo.evnfm.acceptance.testng.tests.ui.lcm.modifyVnfInfo;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ModifyVnfInfoUIDataProviders.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ModifyVnfInfoUIDataProviders.loadModifyVnfInfoConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps.verifyResourceInstantiated;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.UiBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.steps.common.ui.AuthenticationStep;
import com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps;
import com.ericsson.evnfm.acceptance.steps.modifyVnfInfo.ui.ModifyVnfInfoUISteps;
import com.ericsson.evnfm.acceptance.steps.terminate.UI.TerminateUISteps;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class ModifyVnfInfoUITest extends UiBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInfoUITest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("ModifyVnfInfoUITest : starts setup step");

        LOGGER.info("ModifyVnfInfoUITest : load instantiate data");
        EvnfmCnf cnfToInstantiateThroughUI = loadInstantiateConfigData(iTestContext);

        LOGGER.info("ModifyVnfInfoUITest : starts Instantiate a CNF : {}", cnfToInstantiateThroughUI.getVnfInstanceName());
        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());

        InstantiateUISteps instantiateUISteps = new InstantiateUISteps();
        ResourceInfo resourceInfo = instantiateUISteps.instantiateResourceStepsUI(driver, cnfToInstantiateThroughUI);
        verifyResourceInstantiated(resourceInfo, cnfToInstantiateThroughUI);
        LOGGER.info("ModifyVnfInfoUITest : Instantiate a CNF : {} was completed successfully", cnfToInstantiateThroughUI.getVnfInstanceName());

        cnfs.add(cnfToInstantiateThroughUI);
        LOGGER.info("ModifyVnfInfoUITest : setup test was completed successfully");
    }

    @Test
    public void testUIModifyVnfInfo(ITestContext iTestContext) throws IOException {
        LOGGER.info("ModifyVnfInfoUITest : load modify data");
        EvnfmCnf cnfToModifyVnfInfoThroughUI = loadModifyVnfInfoConfigData(iTestContext);

        LOGGER.info("ModifyVnfInfoUITest : starts Modify VNF Info for CNF : {}", cnfToModifyVnfInfoThroughUI.getVnfInstanceName());
        ModifyVnfInfoUISteps modifyVnfInfoUISteps = new ModifyVnfInfoUISteps();
        modifyVnfInfoUISteps.modifyVnfInfoUI(driver, cnfToModifyVnfInfoThroughUI, user);
        LOGGER.info("ModifyVnfInfoUITest : Modify VNF Info for CNF : {} was completed successfully",
                    cnfToModifyVnfInfoThroughUI.getVnfInstanceName());
    }

    @AfterClass
    public void shutdown(ITestContext iTestContext) throws IOException {
        LOGGER.info("ModifyVnfInfoUITest : starts shutdown step");
        final EvnfmCnf evnfmCnf = loadInstantiateConfigData(iTestContext);
        final VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                 evnfmCnf.getVnfInstanceName(), user);

        if (vnfInstanceByRelease != null) {
            LOGGER.info("ModifyVnfInfoUITest : starts Terminate a CNF : {}", evnfmCnf.getVnfInstanceName());
            TerminateUISteps terminateUISteps = new TerminateUISteps();
            terminateUISteps.terminateResourceStepsUI(driver, evnfmCnf);
            AuthenticationStep.logout(driver);
            LOGGER.info("ModifyVnfInfoUITest : Terminate a CNF : {} was completed successfully", evnfmCnf.getVnfInstanceName());
        }

        LOGGER.info("ModifyVnfInfoUITest : completed successfully");
    }
}
