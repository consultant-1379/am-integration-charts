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

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;

public class DialogBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(DialogBox.class);

    public static void clickDialogButton(RemoteWebDriver driver, String buttonText) {
        LOGGER.info("Click button {}", buttonText);
        WebElement button = waitForElementWithText(driver, "eui-base-v0-button", buttonText, 10000, 1000);
        if (button != null) {
            button.click();
        } else {
            throw new NullPointerException("\"" + buttonText + "\" button is not found.");
        }
    }

    public static void clickCleanUpResourcesCheckBox(RemoteWebDriver driver, String checkboxOption) {
        LOGGER.debug("Click clean up resources option on terminate dialog...");
        WebElement checkbox = waitForElementWithText(driver, "eui-base-v0-dialog eui-base-v0-checkbox#" + checkboxOption, "Clean up resources", 10000,
                                                   500);
        checkbox.click();
        assertThat(checkbox.getAttribute("checked")).isNotNull();
    }
}
