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

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

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
        return querySelect(driver, selector, returnFirstFound, 15000, 1000);
    }

    public static Object querySelect(RemoteWebDriver driver, String selector, boolean returnFirstFound, long totalTime,
                                     long interval) {
        long startTime = System.currentTimeMillis();
        boolean firstLoop = true;
        do {
            LOGGER.debug("Retrieve element: {}. Current execution time: {}. Total time {}.", selector, (System.currentTimeMillis() - startTime), totalTime);
            if (firstLoop) {
                firstLoop = false;
            } else {
                manualSleep(interval);
            }
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
            } else if (!returnFirstFound && !((ArrayList) element).isEmpty()) {
                LOGGER.info("Found at least one element using selector: {}", selector);
                return element;
            } else {
                LOGGER.debug("Element '{}' not found yet", selector);
            }
        } while ((System.currentTimeMillis() - startTime) < totalTime);
        LOGGER.warn("Element '{}' not found after timeout of {}", selector, totalTime);
        return null;
    }

    public static Object querySelectExpectedAbsence(RemoteWebDriver driver, String selector) {
        return querySelectExpectedAbsence(driver, selector, 15000, 1000);
    }

    public static Object querySelectExpectedAbsence(RemoteWebDriver driver, String selector, long totalTime,
                                                    long interval) {
        long startTime = System.currentTimeMillis();
        boolean firstLoop = true;
        Object element = null;
        do {
            LOGGER.debug("Retrieve element: {}. Current execution time: {}. Total time {}.",
                         selector, (System.currentTimeMillis() - startTime), totalTime);
            if (firstLoop) {
                firstLoop = false;
            } else {
                manualSleep(interval);
            }
            try {
                element = executeQuerySelect(driver, selector);
            } catch (Exception e) {
                LOGGER.error("Could not retrieve element {}. Exception occured : {}", selector, e.getMessage());
            }
            if (element != null) {
                LOGGER.info("Found single element using selector: {}. But Expected null.", selector);
            } else {
                LOGGER.debug("Element '{}' not found as was expected", selector);
                return element;
            }
        } while ((System.currentTimeMillis() - startTime) < totalTime);
        return element;
    }

    private static WebElement executeQuerySelect(RemoteWebDriver driver, String selector) {
        return (WebElement) driver.executeScript(" return window.querySelectorDeep('" + selector + "');");
    }

    private static ArrayList<WebElement> executeQuerySelectAll(RemoteWebDriver driver, String selector) {
        return (ArrayList<WebElement>) driver.executeScript(" return window.querySelectorAllDeep('" + selector + "');");
    }

    public static void waitForElementToBeEnabled(RemoteWebDriver driver, WebElement element) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15L));
        wait.until(attributeToBeAbsentInElement(element, "disabled"));
    }

    public static void waitForElementPresence(RemoteWebDriver driver, By by) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15L));
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

    public static WebElement waitForElementWithText(RemoteWebDriver driver, String selector, String textContent) {
        return waitForElementWithText(driver, selector, textContent, 15000, 1000);
    }

    public static WebElement waitForElementWithText(RemoteWebDriver driver, String selector, String textContent,
                                                    long totalTime, long interval, String... invalidTextContentList) {
        LOGGER.info("Wait for element {} with text {} to appear", selector, textContent);
        long startTime = System.currentTimeMillis();
        boolean firstLoop = true;
        do {
            performManualSleep(interval, firstLoop);
            firstLoop = false;
            List<WebElement> elements = getElementsBySelector(driver, selector);
            logElements(elements, selector);
            if (!CollectionUtils.isEmpty(elements)) {
                for (WebElement element : elements) {
                    try {
                        String text = element.getText().trim();
                        LOGGER.debug("Element text = {}", text);
                        if (text.equalsIgnoreCase(textContent)) {
                            return element;
                        }
                        checkIfInvalidText(text, textContent, invalidTextContentList);
                    } catch (StaleElementReferenceException exception) {
                        LOGGER.debug("Stale element found, will try again:: {}", exception.getMessage());
                    }
                }
            } else {
                LOGGER.debug("Element '{}' not found yet", selector);
            }
        } while ((System.currentTimeMillis() - startTime) < totalTime);
        throw new RuntimeException("Element " + selector + " with text content " + textContent + " not found");
    }

    private static void performManualSleep(long interval, boolean firstLoop) {
        if (!firstLoop) {
            manualSleep(interval);
        }
    }

    private static List<WebElement> getElementsBySelector(RemoteWebDriver driver, String selector) {
        List<WebElement> elements = null;
        try {
            elements = executeQuerySelectAll(driver, selector);
        } catch (Exception e) {
            LOGGER.error("Could not retrieve element {}. Exception occurred : {}", selector, e.getMessage());
        }
        return elements;
    }

    private static void logElements(List<WebElement> elements, String selector) {
        if (elements != null) {
            LOGGER.debug("Found {} elements matching {}", elements.size(), selector);
        } else {
            LOGGER.debug("querySelect has not found any elements yet for the selector {}", selector);
        }
    }

    private static void checkIfInvalidText(final String text, final String expectedText, final String[] invalidTextContentList) {
        Optional<String> invalidTextOptional = Arrays.stream(invalidTextContentList).filter(text::equalsIgnoreCase).findAny();
        invalidTextOptional.ifPresent(invalidText -> {
            throw new RuntimeException(String.format("Selection was expecting :: %s but found instead :: %s",
                                                     expectedText,
                                                     invalidText));
        });
    }
}
