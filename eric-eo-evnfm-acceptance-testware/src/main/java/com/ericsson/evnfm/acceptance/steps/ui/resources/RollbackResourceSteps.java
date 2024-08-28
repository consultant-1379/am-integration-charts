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

import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickContextMenuItem;
import static com.ericsson.evnfm.acceptance.api.ui.ContextMenu.clickTableContextMenu;
import static com.ericsson.evnfm.acceptance.api.ui.Navigation.loadApplication;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.manualSleep;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RESOURCES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ROLLBACK_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ROLLBACK_OPERATION;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.api.ui.DialogBox;
import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigRollback;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;

import java.time.Duration;

public class RollbackResourceSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackResourceSteps.class);


    public static ResourceInfo rollbackResource(RemoteWebDriver driver,
                                                ConfigGeneral configGeneral,
                                                ConfigInstantiate configInstantiate,
                                                ConfigUpgrade configUpgrade,
                                                ConfigRollback configRollback
    ) {
        LOGGER.debug("Rollback steps...");
        String apiGatewayHost = configGeneral.getApiGatewayHost();
        LOGGER.debug("Navigate to Resources page..");
        Navigation.goTo(driver, apiGatewayHost + UI_ROOT_PATH);
        loadApplication(driver, RESOURCES);
        String releaseName = configInstantiate.getReleaseName();
        long pageLoadTimeoutMs = configGeneral.getPageLoadTimeout()*1000;
        LOGGER.debug("Release name to Rollback is: {} ", releaseName);
        String rowId = releaseName + "__" + configInstantiate.getCluster();
        WebElement resource = waitForElementWithText(driver, "e-generic-table e-custom-cell#" + rowId, releaseName, pageLoadTimeoutMs, 500);
        clickTableContextMenu(driver, rowId);
        LOGGER.debug("Clicking Rollback option in context menu...");
        clickContextMenuItem(driver, "#" + rowId, ".menu-option[value=" + ROLLBACK_MENU_ITEM + "]");
        LOGGER.debug("Waiting for rollback dialog windows...");
        DialogBox.clickDialogButton(driver, ROLLBACK_MENU_ITEM);
        LOGGER.debug("Resource is being rolled back...");
        manualSleep(4000);
        WebDriverWait waitForRollbackToFinish = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(configUpgrade.getApplicationTimeOut())), Duration.ofSeconds(1000));
        ResourceInfo rollbackResourceInfo = waitForRollbackToFinish.until(item -> Table.getResourceInfoByInstanceIdAndOperationAndState(driver,
                rowId,
                ROLLBACK_OPERATION,
                configRollback
                        .getExpectedOperationState()));

        LOGGER.debug("Finished rollback steps...");
        return rollbackResourceInfo;
    }
}
