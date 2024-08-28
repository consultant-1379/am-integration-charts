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

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelectAll;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.utils.Constants.COMPONENTS;
import static com.ericsson.evnfm.acceptance.utils.Constants.TAB;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentsTab {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentsTab.class);

    public void verifyComponentsTabIsOpened(RemoteWebDriver driver) {
        LOGGER.debug("Clicked on components tab...");
        try {
            waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, 10, 500);
        } catch (RuntimeException exception) {
            LOGGER.info("Couldn't find the Components tab, retrying...");
            driver.navigate().refresh();
            waitForElementWithText(driver, TAB + "[selected]", COMPONENTS, 10, 500);
        }
    }

    public Map<String, String> getAllComponents(RemoteWebDriver driver, final long pageLoadTimeout, final int totalPods) {
        LOGGER.info("Getting the state of all components");
        Map<String, String> componentMapNameStatus = new HashMap<>();
        new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeout)).until(item -> querySelectAll(driver,
                                                                                "#resource-details-component-table tbody tr").size() >= totalPods);
        List<WebElement> tableRows = querySelectAll(driver, "#resource-details-component-table tbody tr");
        for (WebElement element : tableRows) {
            try {
                String name = element.findElement(By.className("common-cell")).getText();
                String status = element.findElement(By.tagName("e-custom-cell-state")).getAttribute("cell-value");
                componentMapNameStatus.put(name, status);
            } catch (NoSuchElementException nsee) {
                LOGGER.error("Element not found: " + nsee.getMessage());
            }
        }
        LOGGER.info("Component states are: {}", componentMapNameStatus);
        return componentMapNameStatus;
    }
}
