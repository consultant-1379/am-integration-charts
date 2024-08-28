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
package com.ericsson.evnfm.acceptance.steps.upgrade.ui;

import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_OPERATION_STATES_INSTANTIATE;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.commonUIActions.CommonActions;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.UI.page.AdditionalAttributesWizardPage;
import com.ericsson.evnfm.acceptance.models.UI.page.OperationStartedDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.PackageSelectionPageWizard;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.SummaryWizardPage;
import com.ericsson.evnfm.acceptance.models.UI.page.UpgradeWizardGeneralAttributesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.UpgradeWizardInfrastructurePage;

public class UpgradeUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeUISteps.class);

    private ResourcesPage resourcesPage;
    private PackageSelectionPageWizard packageSelectionPageWizard;
    private UpgradeWizardInfrastructurePage upgradeWizardInfrastructurePage;
    private UpgradeWizardGeneralAttributesPage upgradeWizardGeneralAttributesPage;
    private AdditionalAttributesWizardPage upgradeAdditionalAttributesWizardPage;
    private SummaryWizardPage upgradeWizardSummaryPage;
    private OperationStartedDialog upgradeOperationStartedDialog;

    public ResourceInfo upgradeResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnfToUpgrade) {
        LOGGER.info("Opening Resources page and choosing instance to upgrade");
        resourcesPage = new ResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        packageSelectionPageWizard = resourcesPage.openUpgradeDialog(driver, evnfmCnfToUpgrade.getVnfInstanceName(),
                                                                     evnfmCnfToUpgrade.getCluster().getUIName(),
                                                                     evnfmCnfToUpgrade.getApplicationTimeout());

        LOGGER.info("Instantiate Step 1 - Package Selection");
        packageSelectionPageWizard.selectPackage(driver, evnfmCnfToUpgrade.getPackageName());
        upgradeWizardInfrastructurePage = packageSelectionPageWizard.goToUpgradeWizardInfrastructurePage(driver);

        LOGGER.info("Instantiate Step 2 - Infrastructure");
        upgradeWizardGeneralAttributesPage = upgradeWizardInfrastructurePage.goToUpgradeWizardGeneralAttributesPage(driver);

        LOGGER.info("Instantiate Step 3 - General attributes");
        upgradeWizardGeneralAttributesPage.setDescription(driver, evnfmCnfToUpgrade.getVnfInstanceDescription());
        upgradeWizardGeneralAttributesPage.setApplicationTimeout(driver, evnfmCnfToUpgrade.getApplicationTimeout());
        CommonActions.setExtensions(driver, evnfmCnfToUpgrade.getExtensions());
        upgradeAdditionalAttributesWizardPage = upgradeWizardGeneralAttributesPage.goToUpgradeWizardAdditionalAttributesPage(driver);

        LOGGER.info("Instantiate Step 4 - Additional attributes");
        upgradeAdditionalAttributesWizardPage.setAdditionalAttributes(driver, evnfmCnfToUpgrade.getAdditionalParams());
        upgradeWizardSummaryPage = upgradeAdditionalAttributesWizardPage.goToInstantiateWizardSummaryPage(driver);

        LOGGER.info("Instantiate Step 5 - Summary");
        upgradeOperationStartedDialog = upgradeWizardSummaryPage.goToOperationStartedDialog(driver);

        LOGGER.info("Waiting for operation to be completed");
        resourcesPage = upgradeOperationStartedDialog.openResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        return resourcesPage.verifyResourceState(driver, evnfmCnfToUpgrade, INVALID_OPERATION_STATES_INSTANTIATE);
    }

    public static void verifyResourceUpgraded(final ResourceInfo resourceInfo, final EvnfmCnf evnfmCnf) {
        Assertions.assertThat(resourceInfo.getOperationState()).isEqualToIgnoringCase("COMPLETED");
        Assertions.assertThat(resourceInfo.getLastOperation()).isEqualToIgnoringCase("Change_vnfpkg");
        Assertions.assertThat(resourceInfo.getCluster()).isEqualToIgnoringCase(evnfmCnf.getCluster().getUIName());
        Assertions.assertThat(resourceInfo.getResourceInstanceName()).isEqualToIgnoringCase(evnfmCnf.getVnfInstanceName());
    }
}
