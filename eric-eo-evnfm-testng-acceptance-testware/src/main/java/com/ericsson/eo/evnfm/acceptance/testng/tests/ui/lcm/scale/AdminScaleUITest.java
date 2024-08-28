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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AdminScaleDataProviders.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AdminScaleDataProviders.loadRollbackConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AdminScaleDataProviders.loadScaleConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AdminScaleDataProviders.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.ui.CommonSteps.verifyComponents;
import static com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps.verifyResourceInstantiated;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfUITestStep;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.AdminScaleDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.UiBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.steps.common.ui.AuthenticationStep;
import com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps;
import com.ericsson.evnfm.acceptance.steps.rollback.ui.RollbackUISteps;
import com.ericsson.evnfm.acceptance.steps.scale.ui.ScaleUISteps;
import com.ericsson.evnfm.acceptance.steps.terminate.UI.TerminateUISteps;
import com.ericsson.evnfm.acceptance.steps.upgrade.ui.UpgradeUISteps;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class AdminScaleUITest extends UiBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminScaleUITest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("Starts preparation for AdminScaleTest test: load instantiate data");

        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());

        LOGGER.info("Continue preparation for AdminScaleTest test: perform UI Instantiate operation for instance : {}",
                    cnfToInstantiate.getVnfInstanceName());

        InstantiateUISteps instantiateUISteps = new InstantiateUISteps();
        ResourceInfo resourceInfo = instantiateUISteps.instantiateResourceStepsUI(driver, cnfToInstantiate);
        cnfs.add(cnfToInstantiate);

        LOGGER.info("Continue preparation for AdminScaleTest test: verify Resource Were Instantiated with ID : {}",
                    resourceInfo.getVnfInstanceId());
        verifyResourceInstantiated(resourceInfo, cnfToInstantiate);
        LOGGER.info("Completed preparation for AdminScaleTest test for instance : {}", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "Perform Admin Scale tests", dataProviderClass = AdminScaleDataProviders.class)
    public void testPerformAdminScaleSuccessTest(ITestContext iTestContext) throws IOException {
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        final VnfInstanceLegacyResponse actualVnfInstance = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                    cnfToInstantiate.getVnfInstanceName(), user);

        LOGGER.info("AdminScaleTest test: perform UI Scale operation for instance : {}",
                    cnfToInstantiate.getVnfInstanceName());
        performScaleCnfUITestStep(cnfToInstantiate, actualVnfInstance);
        verifyComponents(driver, cnfToInstantiate);

        LOGGER.info("AdminScaleTest : starts execution Scale a CNF with an instantiation level and extensions");
        final EvnfmCnf evnfmCnf = loadScaleConfigData(iTestContext);
        testScaleWithInstantiationLevels(evnfmCnf);
        LOGGER.info("AdminScaleTest : operation 'Scale a CNF with an instantiation level and extensions' completed successfully");

        LOGGER.info("AdminScaleTest: starts execution Upgrade a CNF setting instantiationLevel and extensions");
        final EvnfmCnf upgradeData = loadUpgradeConfigData(iTestContext);
        testUpgradeWithInstantiationLevels(upgradeData);
        LOGGER.info("AdminScaleTest : operation 'Upgrade a CNF setting instantiationLevel and extensions' completed successfully");

        LOGGER.info("AdminScaleTest : starts operation Rollback a CNF with instantiationLevel and extensions");
        final EvnfmCnf rollbackData = loadRollbackConfigData(iTestContext);
        testRollback(rollbackData);
        LOGGER.info("AdminScaleTest : operation 'Rollback a CNF with instantiationLevel and extensions' completed successfully");
    }

    public void testScaleWithInstantiationLevels(EvnfmCnf cnfToScale) {
        ScaleUISteps scaleUISteps = new ScaleUISteps();
        scaleUISteps.scaleResourceStepsUI(driver, cnfToScale);

        final VnfInstanceLegacyResponse actualVnfInstance = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                    cnfToScale.getVnfInstanceName(), user);

        performScaleCnfUITestStep(cnfToScale, actualVnfInstance);
        verifyComponents(driver, cnfToScale);
    }

    public void testUpgradeWithInstantiationLevels(EvnfmCnf evnfmCnfToUpgrade) {
        UpgradeUISteps upgradeUISteps = new UpgradeUISteps();
        ResourceInfo resourceInfo = upgradeUISteps.upgradeResourceStepsUI(driver, evnfmCnfToUpgrade);
        UpgradeUISteps.verifyResourceUpgraded(resourceInfo, evnfmCnfToUpgrade);

        String packageName = "Ericsson." + resourceInfo.getType() + "." + resourceInfo.getSoftwareVersion() + "." + resourceInfo.getPackageVersion();
        Assertions.assertThat(packageName).isEqualTo(evnfmCnfToUpgrade.getPackageName());
        Assertions.assertThat(resourceInfo.getLastOperation()).isEqualTo("Change_vnfpkg");

        final VnfInstanceLegacyResponse actualVnfInstance = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                    evnfmCnfToUpgrade.getVnfInstanceName(), user);
        performScaleCnfUITestStep(evnfmCnfToUpgrade, actualVnfInstance);
        verifyComponents(driver, evnfmCnfToUpgrade);
    }

    public void testRollback(EvnfmCnf cnfToRollback) {
        RollbackUISteps rollbackUISteps = new RollbackUISteps();
        ResourceInfo resourceInfo = rollbackUISteps.rollbackResourceStepsUI(driver, cnfToRollback);

        String packageName =
                "Ericsson." + resourceInfo.getType() + "." + resourceInfo.getSoftwareVersion() + "." + resourceInfo.getPackageVersion();
        Assertions.assertThat(packageName).isEqualTo(cnfToRollback.getPackageName());
        Assertions.assertThat(resourceInfo.getLastOperation()).isEqualTo("Change_vnfpkg");

        verifyComponents(driver, cnfToRollback);

        final VnfInstanceLegacyResponse actualVnfInstance = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                    cnfToRollback.getVnfInstanceName(), user);
        performScaleCnfUITestStep(cnfToRollback, actualVnfInstance);
    }

    @AfterClass
    public void shutdown(ITestContext iTestContext) throws IOException {
        LOGGER.info("AdminScaleTest : starts shutdown step");
        final EvnfmCnf evnfmCnf = loadInstantiateConfigData(iTestContext);
        final VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                 evnfmCnf.getVnfInstanceName(), user);

        if (vnfInstanceByRelease != null) {
            LOGGER.info("AdminScaleTest : starts Terminate a CNF : {}", evnfmCnf.getVnfInstanceName());
            TerminateUISteps terminateUISteps = new TerminateUISteps();
            terminateUISteps.terminateResourceStepsUI(driver, evnfmCnf);
            LOGGER.info("AdminScaleTest : Terminate a CNF : {} was completed successfully", evnfmCnf.getVnfInstanceName());
        }

        LOGGER.info("AdminScaleTest : completed successfully");
    }
}

