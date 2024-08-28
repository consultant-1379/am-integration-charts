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

import com.ericsson.evnfm.acceptance.api.ui.model.OperationsPage;
import com.ericsson.evnfm.acceptance.api.ui.model.PackagesPage;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourcesPage;
import com.ericsson.evnfm.acceptance.common.TestwareConstants;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElement;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;

import static org.assertj.core.api.Assertions.assertThat;

public class Navigation {
    private static final Logger LOGGER = LoggerFactory.getLogger(Navigation.class);

    public static void goTo(RemoteWebDriver driver, String url) {
        LOGGER.info("Go to {}", url);
        driver.get(url);
    }

    public static PackagesPage loadPackagesApplication(RemoteWebDriver driver){
        loadApplication(driver, TestwareConstants.PACKAGES);
        return new PackagesPage(driver);
    }

    public static ResourcesPage loadResourcesApplication(RemoteWebDriver driver){
        loadApplication(driver, TestwareConstants.RESOURCES);
        return new ResourcesPage(driver);
    }

    public static OperationsPage loadOperationsApplication(RemoteWebDriver driver){
        loadApplication(driver, TestwareConstants.OPERATIONS);
        return new OperationsPage(driver);
    }

    public static void loadApplication(RemoteWebDriver driver, String applicationName) {
        LOGGER.info("Load {}", applicationName);
        WebElement openAppNavigationFlyOutButton = (WebElement) querySelect(driver, "#AppBar-menu-toggle",true, 5000, 1000);
        if (openAppNavigationFlyOutButton != null) {
            openAppNavigationFlyOutButton.click();
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10L));
        ArrayList<WebElement> listOfApplications = (ArrayList<WebElement>) querySelect(driver, "eui-base-v0-tree-item", false, 5000, 1000);
        for (WebElement webElement : listOfApplications) {
            if (webElement.getText().equalsIgnoreCase(applicationName)) {
                wait.until(ExpectedConditions.elementToBeClickable(webElement));
                webElement.click();
                checkCurrentPage(driver, applicationName);
                WebElement closeAppNavigationFlyOutButton = (WebElement) querySelect(driver, "#AppBar-menu-toggle", true, 2000, 500);
                if (closeAppNavigationFlyOutButton != null) {
                    closeAppNavigationFlyOutButton.click();
                }
                //Wait added to allow time for flyout to close completely
                waitForElement(driver, wait, ".menuhidden#LayoutHolder");
                return;
            }
        }
        throw new RuntimeException("Could not open application from application list");
    }

    public static void openAdditionalAttributesTab(RemoteWebDriver driver) {
        WebElement detailsTabRightArrow = (WebElement) querySelect(driver, ".eui__tabs__titles__right__arrow", true);
        if (detailsTabRightArrow != null && detailsTabRightArrow.isDisplayed()) {
            detailsTabRightArrow.click();
        }
        WebElement attributeTab = (WebElement) querySelect(driver, "#additionalAttributes", true);
        assertThat(attributeTab).isNotNull();
        attributeTab.click();
    }

    public static void openGeneralInfoTab(RemoteWebDriver driver) {
        WebElement generalTab = (WebElement) querySelect(driver, "#generalInfo", true);
        assertThat(generalTab).isNotNull();
        generalTab.click();
    }

    public static void checkCurrentPage(RemoteWebDriver driver, String expectedPage) {
        waitForElementWithText(driver, ".current-page", expectedPage, 10000, 1000);
    }
}
