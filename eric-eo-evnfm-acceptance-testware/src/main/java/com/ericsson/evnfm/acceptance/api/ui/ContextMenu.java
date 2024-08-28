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
package com.ericsson.evnfm.acceptance.api.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static org.assertj.core.api.Assertions.assertThat;

public class ContextMenu {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextMenu.class);

    public static void clickContextMenuOption(RemoteWebDriver driver, String rowId, String optionToClick, boolean isTableContextMenu) {
        if(isTableContextMenu) {
            clickTableContextMenu(driver, rowId);
        }else {
            clickFlyoutContextMenu(driver);
        }
        clickContextMenuOption(driver, rowId, optionToClick);
    }

    public static void clickTableContextMenu(RemoteWebDriver driver, String rowId) {
        LOGGER.info("Clicking context menu for {}", rowId);
        ((WebElement) querySelect(driver, "e-generic-table e-context-menu#" + rowId, true)).click();
    }

    public static void clickFlyoutContextMenu(RemoteWebDriver driver) {
        //TODO implement
    }

    public static void clickContextMenuItem(RemoteWebDriver driver, String contextMenuSelector, String menuOption) {
        LOGGER.info("Clicking context menu for {}", contextMenuSelector);
        WebElement dropdown = (WebElement) querySelect(driver, contextMenuSelector + " eui-base-v0-dropdown", true);
        assertThat(dropdown).isNotNull();
        dropdown.click();
        clickMenuItem(driver, contextMenuSelector, menuOption);
    }

    public static void clickMenuItem(RemoteWebDriver driver, String contextMenuSelector, String menuOption) {
        LOGGER.info("Click menu item {}", menuOption);
        WebElement contextMenuItem = (WebElement)querySelect(driver,contextMenuSelector + " eui-base-v0-dropdown " + menuOption, true);
        assertThat(contextMenuItem).isNotNull();
        contextMenuItem.click();

    }

    public static void clickContextMenuOption(RemoteWebDriver driver, String rowId, String optionToClick) {
        LOGGER.info("Clicking {} for {}", optionToClick, rowId);
        WebElement contextMenuOption = (WebElement) querySelect(driver,
                                                                "e-generic-table e-custom-cell#" + rowId + " eui-base-v0"
                                                                        + "-dropdown [value=" + optionToClick + "]", true);
        assertThat(contextMenuOption).isNotNull();
        contextMenuOption.click();
    }

    /**
     * @return the WebElement of the opened context menu
     */
    public static WebElement openTableContextMenu(RemoteWebDriver driver, int row) {
        String menuSelector = ".custom-table__cell .custom-table__cell_context_menu eui-base-v0-dropdown";
        List<WebElement> contextMenus = (List<WebElement>) querySelect(driver, menuSelector, false);
        contextMenus.get(row).click();
        return contextMenus.get(row);
    }

    public static void selectTableContextMenuItem(WebElement contextMenu, String item) {
        List<WebElement> options = contextMenu.findElements(By.className("menu-option"));
        for (WebElement option : options) {
            if (option.getText().equalsIgnoreCase(item)) {
                option.click();
                return;
            }
        }
        throw new IllegalStateException("Failed to locate the context menu option: " + item);
    }
}
