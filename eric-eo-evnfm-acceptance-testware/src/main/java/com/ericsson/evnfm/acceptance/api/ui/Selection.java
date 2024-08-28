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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class Selection {

    private static final Logger LOGGER = LoggerFactory.getLogger(Selection.class);

    public static void manualSleep(long millis) {
        try {
            LOGGER.debug("Sleeping for {} milliseconds", millis);
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException during manualSleep", e);
        }
    }

    public static WebElement querySelect(RemoteWebDriver driver, String selector) {
        return (WebElement) querySelect(driver, selector, true);
    }

    public static ArrayList<WebElement> querySelectAll(RemoteWebDriver driver, String selector) {
        return (ArrayList<WebElement>) querySelect(driver, selector, false);
    }

    public static Object querySelect(RemoteWebDriver driver, String selector, boolean returnFirstFound) {
        return querySelect(driver, selector, returnFirstFound, 5000, 1000);
    }

    public static Object querySelect(RemoteWebDriver driver, String selector, boolean returnFirstFound, long totalTime,
                                     long interval) {
        long startTime = System.currentTimeMillis();
        boolean firstLoop = true;
        do {
            LOGGER.debug("Retrieve element: {}. Current execution time: {}. Total time {}.", selector, (System.currentTimeMillis() - startTime), totalTime);
            firstLoop = isFirstLoop(interval, firstLoop);
            Object element = null;
            try {
                if (returnFirstFound) {
                    element = executeQuerySelect(driver, selector);
                } else {
                    element = executeQuerySelectAll(driver, selector);
                }
            } catch (Exception e) {
                LOGGER.error("Could not retrieve element {}. Exception occured : {}", selector, e.getMessage());
            }
            if (returnFirstFound && element != null) {
                LOGGER.info("Found single element using selector: {}", selector);
                return element;
            } else if (!returnFirstFound && element != null && !((ArrayList) element).isEmpty()) {
                LOGGER.info("Found at least one element using selector: {}", selector);
                return element;
            } else {
                LOGGER.debug("Element '{}' not found yet", selector);
            }
        } while ((System.currentTimeMillis() - startTime) < totalTime);
        LOGGER.warn("Element '{}' not found after timeout of {}", selector, totalTime);
        return null;
    }

    private static WebElement executeQuerySelect(RemoteWebDriver driver, String selector) {
        return (WebElement) driver.executeScript(" return window.querySelectorDeep('" + selector + "');");
    }

    private static ArrayList<WebElement> executeQuerySelectAll(RemoteWebDriver driver, String selector) {
        return (ArrayList<WebElement>) driver.executeScript(" return window.querySelectorAllDeep('" + selector + "');");
    }

    public static WebElement waitForElementWithText(RemoteWebDriver driver, String selector, String textContent) {
        return waitForElementWithText(driver, selector, textContent, 5000, 1000);
    }

    public static WebElement waitForElementWithText(RemoteWebDriver driver, String selector, String textContent,
                                                    long totalTime, long interval) {
        LOGGER.info("Wait for element {} with text {} to appear", selector, textContent);
        long startTime = System.currentTimeMillis();
        boolean firstLoop = true;
        do {
            firstLoop = isFirstLoop(interval, firstLoop);
            ArrayList<WebElement> elements = null;
            try {
                elements = executeQuerySelectAll(driver, selector);
            } catch (Exception e) {
                LOGGER.error("Could not retrieve element {}. Exception occured : {}", selector, e.getMessage());
            }
            if(elements != null) {
                LOGGER.debug("Found {} elements matching {}", elements.size(), selector);
            } else {
                LOGGER.debug("querySelect has not found any elements yet for the selector {}", selector);
            }
            if (elements != null && !elements.isEmpty()) {
                for (WebElement element : elements) {
                    LOGGER.debug("Element text = {}", element.getText());
                    if (element.getText().trim().equalsIgnoreCase(textContent)) {
                        return element;
                    }
                }
            } else {
                LOGGER.debug("Element '{}' not found yet", selector);
            }
        } while ((System.currentTimeMillis() - startTime) < totalTime);
        throw new RuntimeException("Element " + selector + " with text content " + textContent + " not found");
    }

    private static boolean isFirstLoop(long interval, boolean firstLoop) {
        if (firstLoop) {
            return false;
        } else {
            manualSleep(interval);
        }
        return firstLoop;
    }

    public static void additionalAttributesFilterSearch(RemoteWebDriver driver, String stringEntered, String numberExpectedFromSearch) {
        WebElement filterField = (WebElement) querySelect(driver, "#filterSearchArea", true);
        assertThat(filterField).isNotNull();
        filterField.clear();
        filterField.click();

        Actions builder = new Actions(driver);
        builder.sendKeys(stringEntered);
        builder.perform();
        assertThat(filterField.getAttribute("value")).isEqualTo(stringEntered);

        WebElement filterNumber = (WebElement) querySelect(driver, "div[class=additionalAttributesTab]", true);
        assertThat(filterNumber).isNotNull();
        assertThat(filterNumber.getText()).isNotNull();
        assertThat(filterNumber.getText()).contains("Additional Attributes (" + numberExpectedFromSearch);
    }

    public static void waitForElementText(RemoteWebDriver driver, WebDriverWait wait, String selector, String text) {
        wait.until(
                ExpectedConditions.textToBePresentInElement((WebElement) querySelect(driver, selector, true), text));
    }

    public static void waitForElement(RemoteWebDriver driver, WebDriverWait wait, String selector) {
        wait.until(ExpectedConditions.visibilityOf((WebElement) querySelect(driver, selector, true)));
    }
    public static void waitForElementToBeEnabled(RemoteWebDriver driver, WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5L));
        wait.until(attributeToBeAbsentInElement(element, "disabled"));
    }

    public static void waitForElementPresence(RemoteWebDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5L));
        wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public static ExpectedCondition<Boolean> attributeToBeAbsentInElement(final WebElement element, final String attribute) {
        return new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver driver) {
                try {
                    boolean result = element.getAttribute(attribute) == null;
                    if (result) {
                        LOGGER.info("Element {} is enabled", element.getText());
                    }
                    return result;
                } catch (StaleElementReferenceException e) {
                    return null;
                }
            }

            @Override
            public String toString() {
                return "attribute '" + attribute + "' to be absent in element: " + element;
            }
        };
    }

    public static Map<String, String> getAllComponentsStates(RemoteWebDriver driver, final long pageLoadTimeout) {
        LOGGER.info("Getting the state of all components");
        Map<String, String> componentMapNameStatus = new HashMap<>();
        new WebDriverWait(driver, Duration.ofSeconds(pageLoadTimeout)).until(item -> executeQuerySelectAll(driver, "#resource-details-component-table tbody tr").size() != 0);
        List<WebElement> tableRows = executeQuerySelectAll(driver, "#resource-details-component-table tbody tr");
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
