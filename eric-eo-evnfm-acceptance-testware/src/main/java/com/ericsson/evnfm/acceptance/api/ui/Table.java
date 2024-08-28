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

import com.ericsson.evnfm.acceptance.api.ui.model.OperationInfo;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TABLE_ROW_SELECTOR;

import static org.assertj.core.api.Assertions.assertThat;

public class Table {

    private static final Logger LOGGER = LoggerFactory.getLogger(Table.class);

    public static void clickOnTableRowCheckBox(RemoteWebDriver driver, WebDriverWait wait, String tableRow) {
        WebElement selectedRow = wait.until(
                ExpectedConditions.elementToBeClickable((WebElement) querySelect(driver, TABLE_ROW_SELECTOR + tableRow + ") td eui-base-v0-checkbox", true)));
        assertThat(selectedRow).isNotNull();
        selectedRow.click();
    }

    public static void clickHrefOnCustomTableCell(RemoteWebDriver driver, String hrefText) {
        LOGGER.info("Click link {}", hrefText);
        WebElement hrefParentDiv = (WebElement) querySelect(driver, "div.custom-table__cell_value[title=" + hrefText + "]", true);
        hrefParentDiv.click();
    }

    public static WebElement getTableRowByReleaseNameAndClusterName(RemoteWebDriver driver, String resourceInstanceName, String clusterName) {
        LOGGER.info("Get table row for resource {} and cluster {}", resourceInstanceName, clusterName);
        return getTableRowByRowInstanceId(driver, resourceInstanceName + "__" + clusterName);
    }

    public static WebElement getTableRowByRowInstanceId(RemoteWebDriver driver, String rowInstanceId) {
        WebElement row = querySelect(driver, "e-generic-table tbody tr e-custom-cell[id=" + rowInstanceId + "]").findElement(By.xpath("./../.."));
        return row;
    }

    public static Optional<WebElement> findTableRowByInstanceIdAndOperationAndState(RemoteWebDriver driver,
                                                                            String rowInstanceId,
                                                                            String operation,
                                                                            String state) {
        List<WebElement> webElement = (List<WebElement>) querySelect(driver, "e-generic-table tbody tr", false);
        if (webElement == null) {
            return null;
        }
        Optional<WebElement> element = webElement.stream().filter(item -> {
            WebElement resourceInstanceElement = item.findElement(By.cssSelector("[column=vnfInstanceName]"));
            WebElement operationElement = item.findElement(By.cssSelector("[column=lifecycleOperationType]"));
            WebElement stateElement = item.findElement(By.cssSelector("[column=operationState]"));
            String extractedRowInstanceId = resourceInstanceElement.getAttribute("id");
            String extractedRowOperation= operationElement.getAttribute("title");
            String extractedRowStatus = stateElement.getAttribute("title");
            return (extractedRowInstanceId.equalsIgnoreCase(rowInstanceId)
                    && extractedRowOperation.equalsIgnoreCase(operation)
                    && (extractedRowStatus.equalsIgnoreCase(state) || stateElement.getAttribute("cell-value").equalsIgnoreCase(state)));
        }).findFirst();

        return element;
    }

    public static ResourceInfo getResourceInfoByInstanceIdAndOperationAndState(RemoteWebDriver driver,
                                                                               String rowInstanceId,
                                                                               String operation,
                                                                               String state) {

        Optional<WebElement> row = findTableRowByInstanceIdAndOperationAndState(driver, rowInstanceId, operation, state);

        return row.map(Table::getResourceInfo).orElse(null);
    }

    public static OperationInfo getOperationInfoByInstanceIdAndOperationAndState(RemoteWebDriver driver,
                                                                               String rowInstanceId,
                                                                               String operation,
                                                                               String state) {

        Optional<WebElement> row = findTableRowByInstanceIdAndOperationAndState(driver, rowInstanceId, operation, state);

        return row.map(Table::getOperationInfo).orElse(null);
    }

    public static ResourceInfo getResourceInfoByResourceNameAndCluster(RemoteWebDriver driver,
                                                                       String resourceInstanceName,
                                                                       String cluster){
        WebElement operationRow = getTableRowByReleaseNameAndClusterName(driver, resourceInstanceName, cluster);
        return getResourceInfo(operationRow);
    }

    private static ResourceInfo getResourceInfo(WebElement element) {
        String lcmOperation = element.findElement(By.cssSelector("[column=lifecycleOperationType]")).getAttribute("title");
        String resourceName = element.findElement(By.cssSelector("[column=vnfInstanceName]")).getAttribute("title");
        String type = element.findElement(By.cssSelector("[column=vnfProductName]")).getAttribute("title");
        String softwareVersion = element.findElement(By.cssSelector("[column=vnfSoftwareVersion]")).getAttribute("title");
        String packageVersion = element.findElement(By.cssSelector("[column=vnfdVersion]")).getAttribute("title");
        String cluster = element.findElement(By.cssSelector("[column=clusterName]")).getAttribute("title");
        String lastModifiedAt = element.findElement(By.cssSelector("[column=lastStateChanged]")).getAttribute("title");
        WebElement operationStateElement = element.findElement(By.cssSelector("[column=operationState]"));
        String operationState;
        String operationTitleAttribute = operationStateElement.getAttribute("title");

        if (operationTitleAttribute != null && !operationTitleAttribute.isEmpty()) {
            operationState = operationStateElement.getAttribute("title");
        } else {
            operationState = operationStateElement.getAttribute("cell-value");
        }

        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setVnfInstanceId(resourceName);
        resourceInfo.setType(type);
        resourceInfo.setSoftwareVersion(softwareVersion);
        resourceInfo.setPackageVersion(packageVersion);
        resourceInfo.setLastOperation(lcmOperation);
        resourceInfo.setOperationState(operationState);
        resourceInfo.setCluster(cluster);
        resourceInfo.setLastModifiedAt(lastModifiedAt);

        return resourceInfo;
    }

    private static OperationInfo getOperationInfo(WebElement element) {
        String operationName = element.findElement(By.cssSelector("[column=lifecycleOperationType]")).getAttribute("title");
        String resourceName = element.findElement(By.cssSelector("[column=vnfInstanceName]")).getAttribute("title");
        String type = element.findElement(By.cssSelector("[column=vnfProductName]")).getAttribute("title");
        String softwareVersion = element.findElement(By.cssSelector("[column=vnfSoftwareVersion]")).getAttribute("title");
        String cluster = element.findElement(By.cssSelector("[column=clusterName]")).getAttribute("title");
        String timestamp = element.findElement(By.cssSelector("[column=stateEnteredTime]")).getAttribute("title");
        WebElement operationStateElement = element.findElement(By.cssSelector("[column=operationState]"));
        String operationState;
        String operationTitleAttribute = operationStateElement.getAttribute("title");

        if (operationTitleAttribute != null && !operationTitleAttribute.isEmpty()) {
            operationState = operationStateElement.getAttribute("title");
        } else {
            operationState = operationStateElement.getAttribute("cell-value");
        }

        OperationInfo operationInfo = new OperationInfo();
        operationInfo.setResourceInstanceName(resourceName);
        operationInfo.setCluster(cluster);
        operationInfo.setName(operationName);
        operationInfo.setOperationState(operationState);
        operationInfo.setType(type);
        operationInfo.setSoftwareVersion(softwareVersion);
        operationInfo.setTimestamp(timestamp);

        return operationInfo;
    }

    public static void sortColumn(RemoteWebDriver driver, String columnName, String elementTag,
                                  String expectedTextBeforeSort, String expectedTextAfterSort) {
        WebElement icon = (WebElement) querySelect(driver, "#icon-" + columnName, true);
        assertThat(icon).isNotNull();
        WebElement columnElement = (WebElement) querySelect(driver, elementTag + "[column=" + columnName + "]", true);
        assertThat(columnElement).isNotNull();
        assertThat(columnElement.getText()).isNotNull();
        assertThat(columnElement.getText()).isEqualTo(expectedTextBeforeSort);

        icon.click();

        WebElement firstColumnElementAfterSort = (WebElement) querySelect(driver,
                                                                          elementTag + "[column=" + columnName + "]", true);
        assertThat(firstColumnElementAfterSort).isNotNull();
        assertThat(firstColumnElementAfterSort.getText()).isNotNull();
        assertThat(firstColumnElementAfterSort.getText()).isEqualTo(expectedTextAfterSort);
    }

    public static void clickTableRowPackagesByName(RemoteWebDriver driver, String packageName) {
        LOGGER.info("Click table row by package name {}", packageName);
        List<WebElement> tableRows = (List) querySelect(driver, "e-generic-table tbody [column=appCompositeName]", false);
        for (WebElement element : tableRows) {
            if (element.getText().equals(packageName)) {
                assertThat(element).isNotNull();
                assertThat(element.getText()).isEqualTo(packageName);
                element.click();
            }
        }
    }

    public static void clickTableRowPackagesByOrder(RemoteWebDriver driver, int rowNumber) {
        List<WebElement> tableRows = (List) querySelect(driver, "e-generic-table tbody [column=appCompositeName]", false);
        assertThat(tableRows.get(rowNumber)).isNotNull();
        tableRows.get(rowNumber).click();
    }

    public static Object getSelectedRow(RemoteWebDriver driver) {
        return querySelect(driver, "tr[selected]", true);
    }

    public static String getCellTextByColumnName(WebElement row, String columnName) {
        return row.findElement(By.cssSelector("[column=" + columnName + "]")).getText();
    }

    public static List<WebElement> getHeaders(RemoteWebDriver driver) {
        String headerSelector = "e-generic-table th";
        return (List<WebElement>) querySelect(driver, headerSelector, false);
    }

    public static List<WebElement> getRows(RemoteWebDriver driver) {
        String tableRowSelector = "e-generic-table tbody tr";
        return (List<WebElement>) querySelect(driver, tableRowSelector, false);
    }

    public static List<WebElement> getRow(RemoteWebDriver driver, int row) {
        String cellSelector = "e-generic-table tbody tr:nth-child(" + row + ") td";
        return (List<WebElement>) querySelect(driver, cellSelector, false);
    }
}
