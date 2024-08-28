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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TABLE_ROW_SELECTOR;
import static org.assertj.core.api.Assertions.assertThat;

public class Flyout {

    public static void verifyFlyoutIsHidden(RemoteWebDriver driver) {
        WebElement packageDetailsFlyout = (WebElement) querySelect(driver, "#details", true);
        assertThat(packageDetailsFlyout).isNotNull();
        assertThat(packageDetailsFlyout.getAttribute("hide")).isEqualTo("true");
    }

    public static void closeFlyout(RemoteWebDriver driver, WebDriverWait wait) {
        WebElement flyoutClose = wait.until(
                ExpectedConditions.visibilityOf((WebElement) querySelect(driver, "#details eui-v0-icon[name=cross]", true)));
        assertThat(flyoutClose).isNotNull();
        flyoutClose.click();
    }

    public static void verifyFlyoutIsEmpty(String message, RemoteWebDriver driver) {
        WebElement flyoutParagraph = (WebElement) querySelect(driver, "#details p", true);
        assertThat(flyoutParagraph).isNotNull();
        assertThat(flyoutParagraph.getText()).isNotNull();
        assertThat(flyoutParagraph.getText()).isEqualTo(message);
    }

    public static void clickOnInfoIcon(RemoteWebDriver driver) {
        WebElement infoIcon = (WebElement) querySelect(driver, "#open-panel-button-details", true);
        assertThat(infoIcon).isNotNull();
        infoIcon.click();
    }

    public static void verifyTableMatchesFlyout(String tableRow, String tableDataPosition, String id,
                                                RemoteWebDriver driver) {
        WebElement tableRowData = (WebElement) querySelect(driver,TABLE_ROW_SELECTOR + tableRow + ") td:nth-child(" + tableDataPosition + ")", true);
        assertThat(tableRowData).isNotNull();
        assertThat(tableRowData.getText()).isNotNull();

        WebElement flyoutData = (WebElement) querySelect(driver, id + " .divTableCell:nth-child(2)", true);
        assertThat(flyoutData).isNotNull();
        assertThat(flyoutData.getText()).isNotNull();
        assertThat(tableRowData.getText()).isEqualTo(flyoutData.getText());
    }
}
