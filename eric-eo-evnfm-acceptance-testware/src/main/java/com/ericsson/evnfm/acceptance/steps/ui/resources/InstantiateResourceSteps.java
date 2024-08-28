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
package com.ericsson.evnfm.acceptance.steps.ui.resources;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickContextMenuItem;
import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickTableContextMenu;
import static com.ericsson.evnfm.acceptance.api.ui.DialogBox.clickDialogButton;
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.loadApplication;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.getAllComponentsStates;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.api.ui.Table.clickHrefOnCustomTableCell;
import static com.ericsson.evnfm.acceptance.api.ui.Table.getCellTextByColumnName;
import static com.ericsson.evnfm.acceptance.api.ui.Table.getSelectedRow;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.clickFinishWizardStepButton;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.clickNextWizardStepButton;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.fillOutWizardFieldsAdditionalAttributes;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.fillOutWizardInput;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.fillOutWizardTextArea;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.APPLICATION_TIMEOUT_FIELD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CLUSTER_COMBOBOX;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CLUSTER_COMBOBOX_ID;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.COMPONENTS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.DESCRIPTION_FIELD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.GENERAL_INFORMATION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.INSTANTIATE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.INSTANTIATE_STARTED;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.OPERATIONS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.PACKAGES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.PACKAGE_NAME_ID;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCE_DETAILS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCE_LIST_ITEM;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCE_NAME_FIELD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TAB;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.getAppPackageCompositeName;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutWizardSelectCombobox;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.getResourceInfoById;
import static com.ericsson.evnfm.acceptance.steps.ui.resources.CommonSteps.checkAdditionalAttributes;
import static org.assertj.core.api.Assertions.assertThat;

public class InstantiateResourceSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateResourceSteps.class);
    private static final String RESOURCE_ID_SELECTOR = "div.divTableCell:nth-of-type(2)";

    public static ResourceInfo instantiatePackage(RemoteWebDriver driver,
                                                  ConfigGeneral configGeneral,
                                                  ConfigInstantiate configInstantiate,
                                                  AppPackageResponse onboardedPackage) {
        LOGGER.debug("Reached instantiatePackage test steps...");
        Navigation.goTo(driver, configGeneral.getApiGatewayHost() + UI_ROOT_PATH);
        loadApplication(driver, PACKAGES);

        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout() * 1000;
        waitForElementWithText(driver, "e-generic-table e-custom-cell#row-" + onboardedPackage.getAppPkgId(),
                               getAppPackageCompositeName(onboardedPackage), pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, "row-" + onboardedPackage.getAppPkgId());
        LOGGER.debug("Clicking instantiate option...");
        clickContextMenuItem(driver, "#row-" + onboardedPackage.getAppPkgId(), ".menu-option[value=" + INSTANTIATE + "]");
        checkCurrentPage(driver, INSTANTIATE);

        LOGGER.debug("Select package to instantiate...");
        WebElement tableRow = (WebElement) getSelectedRow(driver);
        assertThat(getCellTextByColumnName(tableRow, PACKAGE_NAME_ID)).isEqualTo(getAppPackageCompositeName(onboardedPackage));
        clickNextWizardStepButton(driver, "2");

        WebElement clusterNameCombobox = querySelect(driver, CLUSTER_COMBOBOX);
        fillOutWizardSelectCombobox(driver, CLUSTER_COMBOBOX_ID, configInstantiate.getCluster(), clusterNameCombobox);
        final String namespace = configInstantiate.getNamespace();
        fillOutWizardInput(driver, "Namespace", namespace);

        clickNextWizardStepButton(driver, "3");
        final String releaseName = configInstantiate.getReleaseName();
        fillOutWizardInput(driver, RESOURCE_NAME_FIELD, releaseName);
        fillOutWizardTextArea(driver, DESCRIPTION_FIELD, configInstantiate.getResourceDescription());
        fillOutWizardInput(driver, APPLICATION_TIMEOUT_FIELD, configInstantiate.getApplicationTimeOut());

        clickNextWizardStepButton(driver, "4");

        //write a ticket to fix properly along with all additional attributes
        fillOutWizardFieldsAdditionalAttributes(driver, configInstantiate.getAdditionalAttributes());
        clickNextWizardStepButton(driver, "5");

        clickFinishWizardStepButton(driver, INSTANTIATE_STARTED);
        clickDialogButton(driver, RESOURCE_LIST_ITEM);
        checkCurrentPage(driver, RESOURCES);

        long applicationTimeoutMs = Long.parseLong(configInstantiate.getApplicationTimeOut()) * 1000;
        String rowId = configInstantiate.getReleaseName() + "__" + configInstantiate.getCluster();
        waitForElementWithText(driver, "e-custom-cell#" + rowId, releaseName, applicationTimeoutMs, 15000);
        LOGGER.debug("Waiting for resource to complete operation...");
        waitForElementWithText(driver, "e-custom-cell-state#" + rowId, configInstantiate.getExpectedOperationState(),
                               applicationTimeoutMs, 500);
        ResourceInfo instantiateInfo = Table.getResourceInfoByResourceNameAndCluster(driver,
                                                                                     configInstantiate.getReleaseName(),
                                                                                     configInstantiate.getCluster());
        LOGGER.debug("Navigate to resource details page...");
        LOGGER.info("Release name to be clicked... {} ", releaseName);
        clickHrefOnCustomTableCell(driver, releaseName);
        checkCurrentPage(driver, RESOURCE_DETAILS);

        LOGGER.info("Getting resource id from General Information tab");
        waitForElementWithText(driver, TAB, GENERAL_INFORMATION, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", GENERAL_INFORMATION, pageLoadTimeoutMs, 500);
        WebElement resourceId = querySelect(driver, RESOURCE_ID_SELECTOR);
        instantiateInfo.setVnfInstanceId(resourceId.getText());

        waitForElementWithText(driver, TAB, COMPONENTS, pageLoadTimeoutMs, 500).click();
        LOGGER.debug("Clicked on components tab...");
        waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, pageLoadTimeoutMs, 500);

        Map<String, String> components = getAllComponentsStates(driver, pageLoadTimeoutMs);
        assertThat(components.size()).isGreaterThan(0);
        for (Map.Entry<String, String> component : components.entrySet()) {
            assertThat(Arrays.asList(configInstantiate.getExpectedComponentsState(), "Succeeded")).contains(component.getValue());
        }

        waitForElementWithText(driver, TAB, "Operations", pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", OPERATIONS, pageLoadTimeoutMs, 500).click();
        ArrayList<WebElement> operations = (ArrayList<WebElement>) querySelect(driver, "div[column=lifecycleOperationType]", false);
        assertThat(operations.size()).isEqualTo(1);
        assertThat(operations.get(0).getText()).isEqualTo(INSTANTIATE);
        WebElement operationState = (WebElement) querySelect(driver, "div[column=operationState]", true);
        assertThat(operationState.getText()).isEqualTo(configInstantiate.getExpectedOperationState());

        LOGGER.debug("Checking that additional attributes were set correctly during instantiate process.");
        Map<String, Object> actualAdditionalAttributes = getResourceInfoById(instantiateInfo.getVnfInstanceId());
        checkAdditionalAttributes(driver, configInstantiate.getAdditionalAttributes(), actualAdditionalAttributes);

        LOGGER.debug("Finished instantiation steps...");

        return instantiateInfo;
    }

    public static ResourceInfo instantiatePackageWithoutAdditionalParams(RemoteWebDriver driver,
                                                                         ConfigGeneral configGeneral,
                                                                         ConfigInstantiate configInstantiate,
                                                                         AppPackageResponse onboardedPackage) {
        LOGGER.debug("Reached instantiatePackage test steps...");
        Navigation.goTo(driver, configGeneral.getApiGatewayHost() + UI_ROOT_PATH);
        loadApplication(driver, PACKAGES);

        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout() * 1000;
        waitForElementWithText(driver, "e-generic-table e-custom-cell#row-" + onboardedPackage.getAppPkgId(),
                               getAppPackageCompositeName(onboardedPackage), pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, "row-" + onboardedPackage.getAppPkgId());
        LOGGER.debug("Clicking instantiate option...");
        clickContextMenuItem(driver, "#row-" + onboardedPackage.getAppPkgId(), ".menu-option[value=" + INSTANTIATE + "]");
        checkCurrentPage(driver, INSTANTIATE);

        LOGGER.debug("Select package to instantiate...");
        WebElement tableRow = (WebElement) getSelectedRow(driver);
        assertThat(getCellTextByColumnName(tableRow, PACKAGE_NAME_ID)).isEqualTo(getAppPackageCompositeName(onboardedPackage));
        clickNextWizardStepButton(driver, "2");
        WebElement clusterNameCombobox = querySelect(driver, CLUSTER_COMBOBOX);
        fillOutWizardSelectCombobox(driver, CLUSTER_COMBOBOX_ID, configInstantiate.getCluster(), clusterNameCombobox);

        final String namespace = configInstantiate.getNamespace();
        fillOutWizardInput(driver, "Namespace", namespace);

        clickNextWizardStepButton(driver, "3");
        final String releaseName = configInstantiate.getReleaseName();
        fillOutWizardInput(driver, RESOURCE_NAME_FIELD, releaseName);
        fillOutWizardTextArea(driver, DESCRIPTION_FIELD, configInstantiate.getResourceDescription());
        fillOutWizardInput(driver, APPLICATION_TIMEOUT_FIELD, configInstantiate.getApplicationTimeOut());

        clickNextWizardStepButton(driver, "4");

        clickNextWizardStepButton(driver, "5");

        clickFinishWizardStepButton(driver, INSTANTIATE_STARTED);
        clickDialogButton(driver, RESOURCE_LIST_ITEM);
        checkCurrentPage(driver, RESOURCES);

        long applicationTimeoutMs = Long.parseLong(configInstantiate.getApplicationTimeOut()) * 1000;
        String rowId = configInstantiate.getReleaseName() + "__" + configInstantiate.getCluster();
        waitForElementWithText(driver, "e-custom-cell#" + rowId, releaseName, applicationTimeoutMs, 15000);
        LOGGER.debug("Waiting for resource to complete operation...");
        waitForElementWithText(driver, "e-custom-cell-state#" + rowId, configInstantiate.getExpectedOperationState(),
                               applicationTimeoutMs, 500);
        ResourceInfo instantiateInfo = Table.getResourceInfoByResourceNameAndCluster(driver,
                                                                                     configInstantiate.getReleaseName(),
                                                                                     configInstantiate.getCluster());
        LOGGER.debug("Navigate to resource details page...");
        LOGGER.info("Release name to be clicked... {} ", releaseName);
        clickHrefOnCustomTableCell(driver, releaseName);
        checkCurrentPage(driver, RESOURCE_DETAILS);

        LOGGER.info("Getting resource id from General Information tab");
        waitForElementWithText(driver, TAB, GENERAL_INFORMATION, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", GENERAL_INFORMATION, pageLoadTimeoutMs, 500);
        WebElement resourceId = querySelect(driver, RESOURCE_ID_SELECTOR);
        instantiateInfo.setVnfInstanceId(resourceId.getText());

        waitForElementWithText(driver, TAB, COMPONENTS, pageLoadTimeoutMs, 500).click();
        LOGGER.debug("Clicked on components tab...");
        waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, pageLoadTimeoutMs, 500);

        Map<String, String> components = getAllComponentsStates(driver, pageLoadTimeoutMs);
        assertThat(components.size()).isGreaterThan(0);
        for (Map.Entry<String, String> component : components.entrySet()) {
            assertThat(Arrays.asList(configInstantiate.getExpectedComponentsState(), "Succeeded")).contains(component.getValue());
        }

        waitForElementWithText(driver, TAB, "Operations", pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", OPERATIONS, pageLoadTimeoutMs, 500).click();
        ArrayList<WebElement> operations = (ArrayList<WebElement>) querySelect(driver, "div[column=lifecycleOperationType]", false);
        assertThat(operations.size()).isEqualTo(1);
        assertThat(operations.get(0).getText()).isEqualTo(INSTANTIATE);
        WebElement operationState = (WebElement) querySelect(driver, "div[column=operationState]", true);
        assertThat(operationState.getText()).isEqualTo(configInstantiate.getExpectedOperationState());

        LOGGER.debug("Finished instantiation steps...");

        return instantiateInfo;
    }
}
