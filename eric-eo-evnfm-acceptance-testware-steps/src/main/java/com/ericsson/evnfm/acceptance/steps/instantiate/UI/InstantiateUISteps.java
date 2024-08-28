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
package com.ericsson.evnfm.acceptance.steps.instantiate.UI;

import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_OPERATION_STATES_INSTANTIATE;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.commonUIActions.CommonActions;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.UI.page.OperationStartedDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.AdditionalAttributesWizardPage;
import com.ericsson.evnfm.acceptance.models.UI.page.InstantiateWizardGeneralAttributesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.InstantiateWizardInfrastructurePage;
import com.ericsson.evnfm.acceptance.models.UI.page.PackageSelectionPageWizard;
import com.ericsson.evnfm.acceptance.models.UI.page.SummaryWizardPage;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;

public class InstantiateUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateUISteps.class);

    private ResourcesPage resourcesPage;
    private PackageSelectionPageWizard packageSelectionPageWizard;
    private InstantiateWizardInfrastructurePage instantiateWizardInfrastructurePage;
    private InstantiateWizardGeneralAttributesPage instantiateWizardGeneralAttributesPage;
    private AdditionalAttributesWizardPage instantiateWizardAdditionalAttributesPage;
    private SummaryWizardPage instantiateWizardSummaryPage;
    private OperationStartedDialog instantiateOperationStartedDialog;

    public ResourceInfo instantiateResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnfToInstantiate){
        LOGGER.info("Opening Instantiate wizard from the Resources page");
        resourcesPage = new ResourcesPage();
        resourcesPage.verifyResourcesPageIsOpened(driver);
        packageSelectionPageWizard = resourcesPage.openInstantiateWizard(driver);

        LOGGER.info("Instantiate Step 1 - Package Selection");
        packageSelectionPageWizard.selectPackage(driver, evnfmCnfToInstantiate.getPackageName());
        instantiateWizardInfrastructurePage = packageSelectionPageWizard.goToInstantiateWizardInfrastructurePage(driver);

        LOGGER.info("Instantiate Step 2 - Infrastructure");
        LOGGER.info("Selecting the cluster");
        LOGGER.info("Entering the namespace");
        instantiateWizardInfrastructurePage.setClusterName(driver, evnfmCnfToInstantiate.getCluster().getUIName());
        instantiateWizardInfrastructurePage.setNamespace(driver, evnfmCnfToInstantiate.getNamespace());
        instantiateWizardGeneralAttributesPage = instantiateWizardInfrastructurePage.goToInstantiateWizardGeneralAttributesPage(driver);

        LOGGER.info("Instantiate Step 3 - General attributes");
        LOGGER.info("Entering the resource instance to confirm that it causes the appropriate error");
        instantiateWizardGeneralAttributesPage.setResourceInstanceName(driver, evnfmCnfToInstantiate.getVnfInstanceName());
        instantiateWizardGeneralAttributesPage.setDescription(driver, evnfmCnfToInstantiate.getVnfInstanceDescription());
        instantiateWizardGeneralAttributesPage.setApplicationTimeout(driver, evnfmCnfToInstantiate.getApplicationTimeout());
        CommonActions.setExtensions(driver, evnfmCnfToInstantiate.getExtensions());
        instantiateWizardGeneralAttributesPage.setInstantiationLevel(driver, evnfmCnfToInstantiate.getInstantiationLevel());
        instantiateWizardAdditionalAttributesPage = instantiateWizardGeneralAttributesPage.goToInstantiateWizardAdditionalAttributesPage(driver);

        LOGGER.info("Instantiate Step 4 - Additional attributes");
        instantiateWizardAdditionalAttributesPage.setAdditionalAttributes(driver, evnfmCnfToInstantiate.getAdditionalParams());
        instantiateWizardSummaryPage = instantiateWizardAdditionalAttributesPage.goToInstantiateWizardSummaryPage(driver);

        LOGGER.info("Instantiate Step 5 - Summary");
        instantiateOperationStartedDialog = instantiateWizardSummaryPage.goToOperationStartedDialog(driver);

        LOGGER.info("Waiting for operation to be completed");
        resourcesPage = instantiateOperationStartedDialog.openResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        return resourcesPage.verifyResourceState(driver, evnfmCnfToInstantiate, INVALID_OPERATION_STATES_INSTANTIATE);
    }

    public static void verifyResourceInstantiated(final ResourceInfo resourceInfo, final EvnfmCnf evnfmCnf) {
        Assertions.assertThat(resourceInfo.getOperationState()).isEqualToIgnoringCase("COMPLETED");
        Assertions.assertThat(resourceInfo.getLastOperation()).isEqualToIgnoringCase("INSTANTIATE");
        Assertions.assertThat(resourceInfo.getCluster()).isEqualToIgnoringCase(evnfmCnf.getCluster().getUIName());
        Assertions.assertThat(resourceInfo.getResourceInstanceName()).isEqualToIgnoringCase(evnfmCnf.getVnfInstanceName());
    }
}
