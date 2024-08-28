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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_ROLLBACK;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.SCALE_CONFIG_ASPECT1_IN_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.SCALE_CONFIG_ASPECT1_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.SCALE_CONFIG_ASPECT5_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.loadConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.MultipleScaleOperationDataProviders.loadScaleVnfRequest;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.ui.CommonSteps.verifyComponents;
import static com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps.verifyResourceInstantiated;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.verifyAnnotations;
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
import com.ericsson.evnfm.acceptance.steps.rollback.ui.RollbackUISteps;
import com.ericsson.evnfm.acceptance.steps.scale.ui.ScaleUISteps;
import com.ericsson.evnfm.acceptance.steps.terminate.UI.TerminateUISteps;
import com.ericsson.evnfm.acceptance.steps.upgrade.ui.UpgradeUISteps;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class MultipleScaleOperationTest extends UiBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleScaleOperationTest.class);

    private EvnfmCnf cnfToInstantiate;
    private VnfInstanceResponse vnfInstanceByRelease;

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {

        LOGGER.info("MultipleScaleOperationTest: load instantiate data");

        cnfToInstantiate = loadConfigData(iTestContext, CNF_TO_INSTANTIATE);
        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());

        LOGGER.info("MultipleScaleOperationTest: perform UI Instantiate operation for instance : {}",
                    cnfToInstantiate.getVnfInstanceName());

        InstantiateUISteps instantiateUISteps = new InstantiateUISteps();
        ResourceInfo resourceInfo = instantiateUISteps.instantiateResourceStepsUI(driver, cnfToInstantiate);
        cnfs.add(cnfToInstantiate);

        LOGGER.info("MultipleScaleOperationTest: verify Resource Were Instantiated with ID : {}",
                    resourceInfo.getVnfInstanceId());
        verifyResourceInstantiated(resourceInfo, cnfToInstantiate);
        LOGGER.info("Completed preparation for MultipleScaleOperationTest for instance : {}", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "Perform Admin Scale tests")
    public void testMultipleScaleOperationWithAnnotationVerification(ITestContext iTestContext) throws IOException {

        LOGGER.info("MultipleScaleOperationTest: prepare data");

        ScaleVnfRequest cnfToScaleOutAspect1 = loadScaleVnfRequest(iTestContext, SCALE_CONFIG_ASPECT1_OUT_REQUEST);
        ScaleVnfRequest cnfToScaleInAspect1 = loadScaleVnfRequest(iTestContext, SCALE_CONFIG_ASPECT1_IN_REQUEST);
        ScaleVnfRequest cnfToScaleOutAspect5 = loadScaleVnfRequest(iTestContext, SCALE_CONFIG_ASPECT5_OUT_REQUEST);

        EvnfmCnf evnfmCnfToUpgrade = loadConfigData(iTestContext, CNF_TO_UPGRADE);

        LOGGER.info("MultipleScaleOperationTest: perform UI Scale operation for instance : {}",
                    cnfToScaleOutAspect1.toString());

        ScaleUISteps scaleUISteps = new ScaleUISteps();
        scaleUISteps.scaleResourceStepsUI(driver, cnfToInstantiate, cnfToScaleOutAspect1);

        LOGGER.info("MultipleScaleOperationTest: perform UI Scale operation for instance : {}",
                    cnfToScaleInAspect1.toString());

        scaleUISteps.scaleResourceStepsUI(driver, cnfToInstantiate, cnfToScaleInAspect1);

        LOGGER.info("MultipleScaleOperationTest: perform UI Scale operation for instance : {}",
                    cnfToScaleOutAspect5.toString());

        scaleUISteps.scaleResourceStepsUI(driver, cnfToInstantiate, cnfToScaleOutAspect5);

        LOGGER.info("MultipleScaleOperationTest: perform UI Upgrade operation for instance : {}",
                    evnfmCnfToUpgrade.toString());

        UpgradeUISteps upgradeUISteps = new UpgradeUISteps();
        ResourceInfo resourceInfo = upgradeUISteps.upgradeResourceStepsUI(driver, evnfmCnfToUpgrade);
        UpgradeUISteps.verifyResourceUpgraded(resourceInfo, evnfmCnfToUpgrade);

        LOGGER.info("MultipleScaleOperationTest: perform verification");
        vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(), evnfmCnfToUpgrade.getVnfInstanceName(), user);
        verifyAnnotations(vnfInstanceByRelease, evnfmCnfToUpgrade.getCluster().getLocalPath());

        LOGGER.info("MultipleScaleOperationTest: Rollback a CNF with instantiationLevel and extensions");
        EvnfmCnf cnfToRollback = loadConfigData(iTestContext, CNF_TO_ROLLBACK);
        RollbackUISteps rollbackUISteps = new RollbackUISteps();
        rollbackUISteps.rollbackResourceStepsUI(driver, cnfToRollback);
        verifyComponents(driver, cnfToRollback);
    }

    @AfterClass
    public void shutdown(ITestContext iTestContext) throws IOException {
        LOGGER.info("MultipleScaleOperationTest : starts shutdown step");

        if (vnfInstanceByRelease != null) {
            TerminateUISteps terminateUISteps = new TerminateUISteps();
            terminateUISteps.terminateResourceStepsUI(driver, cnfToInstantiate);
        }

        AuthenticationStep.logout(driver);
        LOGGER.info("MultipleScaleOperationTest : test completed successfully");
    }
}

