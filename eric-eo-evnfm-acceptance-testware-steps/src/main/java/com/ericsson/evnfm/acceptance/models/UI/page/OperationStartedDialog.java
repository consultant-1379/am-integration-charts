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
package com.ericsson.evnfm.acceptance.models.UI.page;

import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;

import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationStartedDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(OperationStartedDialog.class);
    private static final String SEE_RESOURCE_LIST_SELECTOR = "See Resource list";
    private static final String DIALOG_TITLE_SELECTOR = ".dialog__title";
    private static final String DIALOG_CONTENT_SELECTOR = ".show > div:nth-child(1) > div:nth-child(1)";

    public ResourcesPage openResourcesPage(RemoteWebDriver driver) {
        checkDialogTitleForOperationState(driver, "operation started", "Operation failed to start");
        clickDialogButton(driver, SEE_RESOURCE_LIST_SELECTOR);
        return new ResourcesPage();
    }

    public void checkDialogTitleForOperationState(final RemoteWebDriver driver, final String titleMessage, final String defaultContentMessage) {
        Optional<WebElement> dialogTitle = Optional.ofNullable(querySelect(driver, DIALOG_TITLE_SELECTOR));
        dialogTitle.ifPresentOrElse(webElement -> {
            if (!webElement.getText().toLowerCase().contains(titleMessage)) {
                fail(getDialogContent(driver, defaultContentMessage));
            }
        }, () -> fail("Couldn't find the dialog box title component"));
    }

    public static String getDialogContent(final RemoteWebDriver driver, String defaultMessage) {
        Optional<WebElement> dialogContent = Optional.ofNullable(querySelect(driver, DIALOG_CONTENT_SELECTOR));
        return dialogContent.map(WebElement::getText).orElse(defaultMessage);
    }

    public static void clickDialogButton(RemoteWebDriver driver, String buttonText) {
        LOGGER.info("Click on dialog button {}", buttonText);
        WebElement button = waitForElementWithText(driver, "eui-base-v0-button", buttonText, 10000, 1000);
        button.click();
    }
}
