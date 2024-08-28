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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SupportedOperationsTab {
    private static final Logger LOGGER = LoggerFactory.getLogger(SupportedOperationsTab.class);

    private static final String OPERATION_ROWS_SELECTOR = "#supported-operations-tab--table table tbody > [tabindex]";

    public Map<String, String> getOperationSates(RemoteWebDriver driver) {
        Map<String, String> supportedOperationStatusMap = new HashMap<>();
        List<WebElement> tableRows = querySelectAll(driver, OPERATION_ROWS_SELECTOR);
        for (WebElement element : tableRows) {
            try {
                String operationName = element.findElement(By.cssSelector(".common-cell[column=\"operationName\"]")).getText();
                String status = element.findElement(By.cssSelector("e-custom-cell-state")).getAttribute("cell-value");
                supportedOperationStatusMap.put(operationName, status);
            } catch (NoSuchElementException nsee) {
                LOGGER.error("Element not found: {}", nsee.getMessage());
            }
        }
        LOGGER.info("Supported operations states are: {}", supportedOperationStatusMap);
        return supportedOperationStatusMap;
    }
}
