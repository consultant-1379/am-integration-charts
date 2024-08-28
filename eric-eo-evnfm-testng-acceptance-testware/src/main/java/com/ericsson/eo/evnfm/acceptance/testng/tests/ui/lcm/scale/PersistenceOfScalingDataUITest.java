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
package com.ericsson.eo.evnfm.acceptance.testng.tests.ui.lcm.scale;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.SCALE_CONFIG_ASPECT1_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingUIDataProviders.loadConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingUIDataProviders.loadScaleVnfRequest;
import static com.ericsson.evnfm.acceptance.steps.common.ui.CommonSteps.verifyComponents;
import static com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps.verifyResourceInstantiated;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingUIDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.UiBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.steps.common.ui.AuthenticationStep;
import com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps;
import com.ericsson.evnfm.acceptance.steps.scale.ui.ScaleUISteps;
import com.ericsson.evnfm.acceptance.steps.terminate.UI.TerminateUISteps;
import com.ericsson.evnfm.acceptance.steps.upgrade.ui.UpgradeUISteps;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PersistenceOfScalingDataUITest extends UiBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceOfScalingDataUITest.class);

    private EvnfmCnf cnfToInstantiate;
    private VnfInstanceResponse vnfInstanceByRelease;

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {

        LOGGER.info("PersistenceOfScalingDataUITest: load instantiate data");

        cnfToInstantiate = loadConfigData(iTestContext, CNF_TO_INSTANTIATE);
        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());

        LOGGER.info("PersistenceOfScalingDataUITest: perform UI Instantiate operation for instance : {}",
                    cnfToInstantiate.getVnfInstanceName());

        InstantiateUISteps instantiateUISteps = new InstantiateUISteps();
        ResourceInfo resourceInfo = instantiateUISteps.instantiateResourceStepsUI(driver, cnfToInstantiate);
        cnfs.add(cnfToInstantiate);

        LOGGER.info("PersistenceOfScalingDataUITest: verify Resource Were Instantiated with ID : {}",
                    resourceInfo.getVnfInstanceId());
        verifyResourceInstantiated(resourceInfo, cnfToInstantiate);
        verifyComponents(driver, cnfToInstantiate);

        LOGGER.info("Completed preparation for PersistenceOfScalingDataUITest for instance : {}", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "performing scale to check scale info persistence after upgrade",
            dataProviderClass = PersistenceOfScalingUIDataProviders.class, testName = "UIPersistenceOfScalingData")
    public void endToEndUITestPersistenceOfScalingData(ITestContext iTestContext) throws IOException {
        LOGGER.info("RestTestPersistenceOfScalingData : Testing persistence of scaling");

        LOGGER.info("PersistenceOfScalingDataUITest: prepare data");

        ScaleVnfRequest cnfToScaleOutAspect1 = loadScaleVnfRequest(iTestContext, SCALE_CONFIG_ASPECT1_OUT_REQUEST);

        EvnfmCnf cnfToUpgrade = loadConfigData(iTestContext, CNF_TO_UPGRADE);

        LOGGER.info("PersistenceOfScalingDataUITest: perform UI Scale operation for instance : {}",
                    cnfToScaleOutAspect1.toString());

        ScaleUISteps scaleUISteps = new ScaleUISteps();
        scaleUISteps.scaleResourceStepsUI(driver, cnfToInstantiate, cnfToScaleOutAspect1);

        LOGGER.info("PersistenceOfScalingDataUITest: perform UI Upgrade operation for instance : {}",
                    cnfToUpgrade.toString());

        UpgradeUISteps upgradeUISteps = new UpgradeUISteps();
        ResourceInfo resourceInfo = upgradeUISteps.upgradeResourceStepsUI(driver, cnfToUpgrade);
        UpgradeUISteps.verifyResourceUpgraded(resourceInfo, cnfToUpgrade);
        verifyComponents(driver, cnfToUpgrade);

        TerminateUISteps terminateUISteps = new TerminateUISteps();
        terminateUISteps.terminateResourceStepsUI(driver, cnfToInstantiate);
    }

    @AfterClass
    public void shutdown(ITestContext iTestContext) throws IOException {
        LOGGER.info("PersistenceOfScalingDataUITest : starts shutdown step");

        if (vnfInstanceByRelease != null) {
            TerminateUISteps terminateUISteps = new TerminateUISteps();
            terminateUISteps.terminateResourceStepsUI(driver, cnfToInstantiate);
        }

        AuthenticationStep.logout(driver);
        LOGGER.info("PersistenceOfScalingDataUITest : test completed successfully");
    }
}