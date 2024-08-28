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

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickContextMenuItem;
import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickTableContextMenu;
import static com.ericsson.evnfm.acceptance.api.ui.DialogBox.clickDialogButton;
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.loadApplication;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.getAllComponentsStates;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.api.ui.Table.clickHrefOnCustomTableCell;
import static com.ericsson.evnfm.acceptance.api.ui.Table.clickTableRowPackagesByName;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.checkCurrentWizardStep;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.checkReadOnlyFieldsGeneralAttributes;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.clickFinishWizardStepButton;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.clickNextWizardStepButton;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.fillOutWizardFieldsAdditionalAttributes;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.fillOutWizardInput;
import static com.ericsson.evnfm.acceptance.api.ui.Wizard.setCheckbox;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ADDITIONAL_ATTRIBUTES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.APPLICATION_TIMEOUT_FIELD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CHECKBOX;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.COMPONENTS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.GENERAL_ATTRIBUTES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.GENERAL_INFORMATION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.INFRASTRUCTURE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.OPERATIONS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.PACKAGE_SELECTION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCE_DETAILS;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCE_LIST_ITEM;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.SUMMARY;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TAB;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UPGRADE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UPGRADE_OPERATION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UPGRADE_STARTED;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.getAppPackageCompositeName;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.getResourceInfoById;
import static com.ericsson.evnfm.acceptance.steps.ui.resources.CommonSteps.checkAdditionalAttributes;

import java.time.Duration;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;

public class UpgradeResourceSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeResourceSteps.class);
    private static final String UPGRADED_RESOURCE_ID_SELECTOR = "div.divTableCell:nth-of-type(2)";

    public static ResourceInfo upgradeResource(RemoteWebDriver driver,
                                               ConfigGeneral configGeneral,
                                               ConfigInstantiate configInstantiate,
                                               ConfigUpgrade configUpgrade,
                                               AppPackageResponse onboardedPackage, boolean persistScaleInfo) {

        LOGGER.debug("Starting upgrade steps...");
        Navigation.goTo(driver, configGeneral.getApiGatewayHost() + UI_ROOT_PATH);
        loadApplication(driver, RESOURCES);
        String releaseName = configInstantiate.getReleaseName();
        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout()*1000;
        LOGGER.debug("Resource name is: " + releaseName);
        String rowId = configInstantiate.getReleaseName() + "__" + configInstantiate.getCluster();
        waitForElementWithText(driver, "e-generic-table e-custom-cell#" + rowId, releaseName, pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, rowId);
        LOGGER.debug("Clicking upgrade option & redirecting to wizard...");
        clickContextMenuItem(driver, "#" + rowId, ".menu-option[value=" + UPGRADE + "]");
        checkCurrentPage(driver, UPGRADE);
        checkCurrentWizardStep(driver, PACKAGE_SELECTION);

        clickTableRowPackagesByName(driver, getAppPackageCompositeName(onboardedPackage));
        clickNextWizardStepButton(driver, "2");

        checkCurrentWizardStep(driver, INFRASTRUCTURE);
        clickNextWizardStepButton(driver, "3");

        checkCurrentWizardStep(driver, GENERAL_ATTRIBUTES);
        checkReadOnlyFieldsGeneralAttributes(driver, "instanceName", releaseName);
        fillOutWizardInput(driver, APPLICATION_TIMEOUT_FIELD, configUpgrade.getApplicationTimeOut());
        setCheckbox(driver, CHECKBOX + "#persistScaleInfo .checkbox__input[value*=\"Persist Scale Info\"]", persistScaleInfo);
        clickNextWizardStepButton(driver, "4");

        checkCurrentWizardStep(driver, ADDITIONAL_ATTRIBUTES);
        //write a ticket to fix properly along with all additional attributes
        fillOutWizardFieldsAdditionalAttributes(driver, configUpgrade.getAdditionalAttributes());
        clickNextWizardStepButton(driver, "5");

        checkCurrentWizardStep(driver, SUMMARY);
        clickFinishWizardStepButton(driver, UPGRADE_STARTED);
        clickDialogButton(driver, RESOURCE_LIST_ITEM);

        checkCurrentPage(driver, RESOURCES);

        long applicationTimeoutSeconds = Long.parseLong(configUpgrade.getApplicationTimeOut());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(applicationTimeoutSeconds), Duration.ofSeconds(1000));
        ResourceInfo updatedResourceInfo = wait.until(item -> Table.getResourceInfoByInstanceIdAndOperationAndState(driver,
                                                                                                                 rowId,
                                                                                                                 UPGRADE_OPERATION,
                                                                                                                 configUpgrade
                                                                                                                         .getExpectedOperationState()));

        assertThat(updatedResourceInfo.getOperationState()).isEqualToIgnoringCase(configUpgrade.getExpectedOperationState());

        clickHrefOnCustomTableCell(driver, releaseName);
        checkCurrentPage(driver, RESOURCE_DETAILS);

        LOGGER.info("Getting resource id from General Information tab");
        waitForElementWithText(driver, TAB, GENERAL_INFORMATION, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", GENERAL_INFORMATION, pageLoadTimeoutMs, 500);
        WebElement resourceId = querySelect(driver, UPGRADED_RESOURCE_ID_SELECTOR);
        updatedResourceInfo.setVnfInstanceId(resourceId.getText());

        waitForElementWithText(driver, TAB, COMPONENTS, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, pageLoadTimeoutMs, 500);

        Map<String, String> components = wait.until(item -> getAllComponentsStates(driver, pageLoadTimeoutMs));
        assertThat(components.size()).isGreaterThan(0);
        wait.until(item -> {
            for (Map.Entry<String, String> component : components.entrySet()) {
                if (!StringUtils.equalsAnyIgnoreCase(component.getValue(), configUpgrade.getExpectedComponentsState(), "Succeeded")) {
                    return null;
                }
            }
            return components;
        });

        loadApplication(driver, OPERATIONS);

        Table.getOperationInfoByInstanceIdAndOperationAndState(driver,
                rowId,
                UPGRADE_OPERATION,
                configUpgrade.getExpectedOperationState());

        LOGGER.debug("Checking that additional attributes were set correctly during upgrade process.");
        Map<String, Object> actualAdditionalAttributes = getResourceInfoById(updatedResourceInfo.getVnfInstanceId());
        checkAdditionalAttributes(driver, configUpgrade.getAdditionalAttributes(), actualAdditionalAttributes);

        LOGGER.debug("Finished upgrade steps...");

        return updatedResourceInfo;
    }

    public static ResourceInfo upgradePackageWithoutAdditionalParams(RemoteWebDriver driver,
                                                                     ConfigGeneral configGeneral,
                                                                     ConfigInstantiate configInstantiate,
                                                                     ConfigUpgrade configUpgrade,
                                                                     AppPackageResponse onboardedPackage, boolean persistScaleInfo) {

        LOGGER.debug("Starting upgrade steps...");
        Navigation.goTo(driver, configGeneral.getApiGatewayHost() + UI_ROOT_PATH);
        loadApplication(driver, RESOURCES);
        String releaseName = configInstantiate.getReleaseName();
        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout() * 1000;
        LOGGER.debug("Resource name is: " + releaseName);
        String rowId = configInstantiate.getReleaseName() + "__" + configInstantiate.getCluster();
        waitForElementWithText(driver, "e-generic-table e-custom-cell#" + rowId, releaseName, pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, rowId);
        LOGGER.debug("Clicking upgrade option & redirecting to wizard...");
        clickContextMenuItem(driver, "#" + rowId, ".menu-option[value=" + UPGRADE + "]");
        checkCurrentPage(driver, UPGRADE);
        checkCurrentWizardStep(driver, PACKAGE_SELECTION);

        clickTableRowPackagesByName(driver, getAppPackageCompositeName(onboardedPackage));
        clickNextWizardStepButton(driver, "2");

        checkCurrentWizardStep(driver, INFRASTRUCTURE);
        clickNextWizardStepButton(driver, "3");

        checkCurrentWizardStep(driver, GENERAL_ATTRIBUTES);
        checkReadOnlyFieldsGeneralAttributes(driver, "instanceName", releaseName);
        fillOutWizardInput(driver, APPLICATION_TIMEOUT_FIELD, configUpgrade.getApplicationTimeOut());
        setCheckbox(driver, CHECKBOX + "#persistScaleInfo .checkbox__input[value*=\"Persist Scale Info\"]", persistScaleInfo);
        clickNextWizardStepButton(driver, "4");

        checkCurrentWizardStep(driver, ADDITIONAL_ATTRIBUTES);
        clickNextWizardStepButton(driver, "5");

        checkCurrentWizardStep(driver, SUMMARY);
        clickFinishWizardStepButton(driver, UPGRADE_STARTED);
        clickDialogButton(driver, RESOURCE_LIST_ITEM);

        checkCurrentPage(driver, RESOURCES);

        long applicationTimeoutSeconds = Long.parseLong(configUpgrade.getApplicationTimeOut());
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(applicationTimeoutSeconds), Duration.ofSeconds(1000));
        ResourceInfo updatedResourceInfo = wait.until(item -> Table.getResourceInfoByInstanceIdAndOperationAndState(driver,
                                                                                                                    rowId,
                                                                                                                    UPGRADE_OPERATION,
                                                                                                                    configUpgrade
                                                                                                                            .getExpectedOperationState()));

        assertThat(updatedResourceInfo.getOperationState()).isEqualToIgnoringCase(configUpgrade.getExpectedOperationState());

        clickHrefOnCustomTableCell(driver, releaseName);
        checkCurrentPage(driver, RESOURCE_DETAILS);

        LOGGER.info("Getting resource id from General Information tab");
        waitForElementWithText(driver, TAB, GENERAL_INFORMATION, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", GENERAL_INFORMATION, pageLoadTimeoutMs, 500);
        WebElement resourceId = querySelect(driver, UPGRADED_RESOURCE_ID_SELECTOR);
        updatedResourceInfo.setVnfInstanceId(resourceId.getText());

        waitForElementWithText(driver, TAB, COMPONENTS, pageLoadTimeoutMs, 500).click();
        waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, pageLoadTimeoutMs, 500);

        Map<String, String> components = wait.until(item -> getAllComponentsStates(driver, pageLoadTimeoutMs));
        assertThat(components.size()).isGreaterThan(0);
        wait.until(item -> {
            for (Map.Entry<String, String> component : components.entrySet()) {
                if (!StringUtils.equalsAnyIgnoreCase(component.getValue(), configUpgrade.getExpectedComponentsState(), "Succeeded")) {
                    return null;
                }
            }
            return components;
        });

        loadApplication(driver, OPERATIONS);

        Table.getOperationInfoByInstanceIdAndOperationAndState(driver,
                                                               rowId,
                                                               UPGRADE_OPERATION,
                                                               configUpgrade.getExpectedOperationState());

        LOGGER.debug("Finished upgrade steps...");

        return updatedResourceInfo;
    }
}
