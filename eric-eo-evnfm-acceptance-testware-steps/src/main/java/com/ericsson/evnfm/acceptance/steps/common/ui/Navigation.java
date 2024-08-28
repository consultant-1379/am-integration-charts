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
package com.ericsson.evnfm.acceptance.steps.common.ui;

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Navigation {
    private static final Logger LOGGER = LoggerFactory.getLogger(Navigation.class);

    public static void goTo(RemoteWebDriver driver, String url) {
        LOGGER.info("Go to {}", url);
        driver.get(url);
    }

    public static void checkCurrentPage(RemoteWebDriver driver, String expectedPage) {
        waitForElementWithText(driver, ".current-page", expectedPage, 10000, 1000);
    }
}
