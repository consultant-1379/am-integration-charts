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
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.loadApplication;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.manualSleep;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.SCALE_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.SCALE_OPERATION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.checkIfScaleCanBePerformed;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.getCurrentValues;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScale;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScaleReset;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;

public class ScaleResourceSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleResourceSteps.class);

    public static void scaleResource(RemoteWebDriver driver,
                                     ConfigGeneral configGeneral,
                                     ConfigInstantiate configInstantiate,
                                     ConfigScale configScale, String scaleType) {
        LOGGER.info("Performing {} ", scaleType);
        String apiGatewayHost = configGeneral.getApiGatewayHost();
        LOGGER.debug("Navigate to Resources page..");
        Navigation.goTo(driver, apiGatewayHost + UI_ROOT_PATH);
        loadApplication(driver, RESOURCES);
        String releaseName = configInstantiate.getReleaseName();
        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout()*1000;
        LOGGER.info("Release name to scale is: {} ", releaseName);
        String rowId = releaseName + "__" + configInstantiate.getCluster();
        waitForElementWithText(driver, "e-generic-table e-custom-cell#" + rowId, releaseName, pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, rowId);
        LOGGER.debug("Clicking scale option in context menu...");
        clickContextMenuItem(driver, "#" + rowId, ".menu-option[value=" + SCALE_MENU_ITEM + "]");
        String textContent = "Scale " + releaseName;
        waitForElementWithText(driver, "eui-layout-v0-tile .tile__header .tile__header__left__title", textContent, pageLoadTimeoutMs, 500);

        if ("scaleIn".equalsIgnoreCase(scaleType)) {
            WebElement scaleIn = (WebElement) querySelect(driver, "eui-base-v0-radio-button[name=\"scale-type-scale-in\"]", true);
            scaleIn.click();
        }

        WebElement aspectDropDown = (WebElement) querySelect(driver, "eui-layout-v0-tile eui-base-v0-combo-box", true);
        aspectDropDown.click();

        WebElement aspectMenu = (WebElement) querySelect(driver, "eui-layout-v0-tile eui-base-v0-combo-box .dropdown .menu div", true);
        aspectMenu.click();

        WebElement scaleBtn = (WebElement) querySelect(driver, "eui-layout-v0-tile eui-base-v0-button.scale-perform-button[primary=true]", true);
        scaleBtn.click();

        WebElement scaleConfirmationDialog = (WebElement) querySelect(driver, "e-scale-resource-panel eui-base-v0-dialog eui-base-v0-button[primary=true]", true);
        scaleConfirmationDialog.click();

        long applicationTimeoutSeconds = Long.parseLong(configScale.getApplicationTimeout());
        manualSleep(10000); //Adding as Operation will be "Scale" regardless of whether scale in or out and to allow for polling time
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(applicationTimeoutSeconds), Duration.ofSeconds(1000));
        ResourceInfo scaleResourceInfo = wait.until(item -> Table.getResourceInfoByInstanceIdAndOperationAndState(driver,
                rowId,
                SCALE_OPERATION,
                configScale
                        .getExpectedOperationState()));
        assertThat(scaleResourceInfo.getOperationState()).isEqualToIgnoringCase(configScale.getExpectedOperationState());

    }

    public static void performScaleUI(RemoteWebDriver driver, ConfigGeneral configGeneral, ConfigScale configScale,
                                ConfigInstantiate configInstantiate, final ConfigCluster configCluster)
            throws IOException, InterruptedException {
        if (checkIfScaleCanBePerformed(configScale)) {
            Map<String, Map<String, String>> currentValues = getCurrentValues(configScale, configInstantiate,
                    configCluster);
            ScaleResourceSteps.scaleResource(driver, configGeneral, configInstantiate, configScale, "scaleOut");
            Map<String, Map<String, String>> scaledValues = getCurrentValues(configScale, configInstantiate,
                    configCluster);
            verifyScale(configScale, scaledValues);
            ScaleResourceSteps.scaleResource(driver, configGeneral, configInstantiate, configScale, "scaleIn");
            Map<String, Map<String, String>> resetValues = getCurrentValues(configScale, configInstantiate,
                    configCluster);
            verifyScaleReset(currentValues, resetValues);
        } else {
            LOGGER.info("Scale config missing mandatory params skipping scale\n");
        }
    }
}
