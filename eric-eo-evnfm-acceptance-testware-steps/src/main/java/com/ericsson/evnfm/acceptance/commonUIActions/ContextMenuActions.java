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
package com.ericsson.evnfm.acceptance.commonUIActions;

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickElement;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.scrollIntoView;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.manualSleep;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextMenuActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextMenuActions.class);
    private static final String MENU_SELECTOR = ".custom-table__cell .custom-table__cell_context_menu eui-base-v0-dropdown";
    private static final String MENU_OPTION_SELECTOR = "menu-option";
    private static final String CONTEXT_MENU_OPTION_SELECTOR = "e-generic-table e-custom-cell#%1$s eui-base-v0-dropdown [value=\"%2$s\"]";

    public static void clickTableContextWithRetry(RemoteWebDriver driver, String rowId, String contextMenuSelector, String menuOption) {
        // Retry once since location of the row might change
        try {
            clickTableContext(driver, rowId, contextMenuSelector, menuOption);
        } catch (RuntimeException exception) {
            querySelect(driver, ".current-page").click();
            manualSleep(2000);
            clickTableContext(driver, rowId, contextMenuSelector, menuOption);
        }
    }

    public static void clickTableContext(RemoteWebDriver driver, String rowId, String contextMenuSelector, String menuOption) {
        LOGGER.info("Clicking context menu for {}", rowId);
        WebElement contextMenu = ((WebElement) querySelect(driver, "e-generic-table e-context-menu#" + rowId, true));
        clickElement(driver, contextMenu);
        LOGGER.info("Clicking context menu item for {}", contextMenuSelector);
        clickMenuItem(driver, contextMenuSelector, menuOption);
    }

    public static void clickTableContextMenu(RemoteWebDriver driver, String rowId) {
        LOGGER.info("Clicking context menu for {}", rowId);
        WebElement contextMenu = ((WebElement) querySelect(driver, "e-generic-table e-context-menu#" + rowId, true));
        clickElement(driver, contextMenu);
    }

    public static void clickTableContextMenu(RemoteWebDriver driver, WebElement rowContextMenu) {
        LOGGER.info("Clicking context menu for {}", rowContextMenu);
        clickElement(driver, rowContextMenu);
    }

    public static void clickContextMenuItem(RemoteWebDriver driver, String contextMenuSelector, String menuOption) {
        LOGGER.info("Clicking context menu for {}", contextMenuSelector);
        clickMenuItem(driver, contextMenuSelector, menuOption);
    }

    public static void clickContextMenuItem(RemoteWebDriver driver, WebElement contextMenuDropdown, String menuOption) {
        LOGGER.info("Clicking context menu for {}", contextMenuDropdown);
        clickMenuItem(driver, contextMenuDropdown, menuOption);
    }

    public static void clickMenuItem(RemoteWebDriver driver, String contextMenuSelector, String menuOption) {
        LOGGER.info("Click menu item {}", menuOption);
        WebElement contextMenuItem = (WebElement)querySelect(driver,contextMenuSelector + " eui-base-v0-dropdown " + menuOption, true);
        if (contextMenuItem == null) {
            throw new RuntimeException("Couldn't find the menu item");
        }
        scrollIntoView(driver, contextMenuItem);
        contextMenuItem.click();
    }

    public static void clickMenuItem(RemoteWebDriver driver, WebElement contextMenuDropdown, String menuOption) {
        LOGGER.info("Click menu item {}", menuOption);
        clickElement(driver, contextMenuDropdown);
    }

    public static void clickContextMenuOption(RemoteWebDriver driver, String rowId, String optionToClick) {
        LOGGER.info("Clicking {} for {}", optionToClick, rowId);
        WebElement contextMenuOption = (WebElement) querySelect(driver,String.format(CONTEXT_MENU_OPTION_SELECTOR, rowId, optionToClick)/*"e-generic-table e-custom-cell#" + rowId + " eui-base-v0"
                                                                        + "-dropdown [value=" + optionToClick + "]"*/, true);
        clickElement(driver, contextMenuOption);
    }

    /**
     * @return the WebElement of the opened context menu
     */
    public static WebElement openTableContextMenu(RemoteWebDriver driver, int row) {
        List<WebElement> contextMenus = (List<WebElement>) querySelect(driver, MENU_SELECTOR, false);
        contextMenus.get(row).click();
        return contextMenus.get(row);
    }

    public static void selectTableContextMenuItem(WebElement contextMenu, String item) {
        List<WebElement> options = contextMenu.findElements(By.className(MENU_OPTION_SELECTOR));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase(item)) {
                option.click();
                return;
            }
        }
        throw new IllegalStateException("Failed to locate the context menu option: " + item);
    }
}
