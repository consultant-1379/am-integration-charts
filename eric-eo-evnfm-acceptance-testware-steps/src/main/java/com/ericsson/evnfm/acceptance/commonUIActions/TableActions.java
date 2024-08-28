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
package com.ericsson.evnfm.acceptance.commonUIActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelectAll;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;

public class TableActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableActions.class);
    private static final String ALL_TABLE_ROWS_SELECTOR = "e-generic-table tbody [column=appCompositeName]";
    private static final String TABLE_ROW_ID_SELECTOR = "e-generic-table tbody tr e-custom-cell[id=\"%s\"]";
    private static final String TABLE_ROW_SELECTOR = "%s table tbody tr";
    private static final String TABLE_CELL_NAME_ROW_SELECTOR = " td e-custom-cell[column=name] div div";
    private static final String COLUMN_LAST_OPERATION_SELECTOR = "[column=lifecycleOperationType]";
    private static final String COLUMN_RESOURCE_INSTANCE_NAME_SELECTOR = "[column=vnfInstanceName]";
    private static final String COLUMN_TYPE_SELECTOR = "[column=vnfProductName]";
    private static final String COLUMN_SOFTWARE_VERSION_SELECTOR = "[column=vnfSoftwareVersion]";
    private static final String COLUMN_PACKAGE_VERSION_SELECTOR = "[column=vnfdVersion]";
    private static final String COLUMN_CLUSTER_SELECTOR = "[column=clusterName]";
    private static final String COLUMN_LAST_MODIFIED_AT_SELECTOR = "[column=lastStateChanged]";
    private static final String COLUMN_LAST_OPERATION_STATE_SELECTOR = "[column=operationState]";
    private static final String ALL_PRODUCT_ROWS_SELECTOR = "e-generic-table tbody [column=appCompositeName].custom-table__cell_value";
    private static final String ALL_CLUSTER_ROWS_SELECTOR = "e-generic-table tbody div [column=name]";

    private static final String TITLE_ATTRIBUTE = "title";
    private static final String CELL_VALUE_ATTRIBUTE = "cell-value";

    public static void clickTableRowPackageByName(RemoteWebDriver driver, String packageName) {
        LOGGER.info("Click table row by package name {}", packageName);
        List<WebElement> tableRows = (List) querySelect(driver, ALL_TABLE_ROWS_SELECTOR, false);
        boolean clicked = false;
        for (WebElement element : tableRows) {
            if (element.getText().equals(packageName)) {
                assertThat(element).isNotNull();
                assertThat(element.getText()).isEqualTo(packageName);
                element.click();
                clicked = true;
            }
        }
        if (!clicked) {
            LOGGER.info("Did not find any row in the table with package name {}", packageName);
            fail(String.format("Did not find any row in the table with package name %s", packageName));
        }
    }

    public static ResourceInfo getResourceInfoByResourceNameAndCluster(RemoteWebDriver driver, String resourceInstanceName, String cluster) {
        WebElement operationRow = getTableRowByReleaseNameAndClusterName(driver, resourceInstanceName, cluster);
        return getResourceInfo(operationRow);
    }

    public static WebElement getTableRowByReleaseNameAndClusterName(RemoteWebDriver driver, String resourceInstanceName, String clusterName) {
        LOGGER.info("Get table row for resource {} and cluster {}", resourceInstanceName, clusterName);
        return getTableRowByRowInstanceId(driver, resourceInstanceName + "__" + clusterName);
    }

    public static WebElement getTableRowByRowInstanceId(RemoteWebDriver driver, String rowInstanceId) {
        WebElement row = querySelect(driver, String.format(TABLE_ROW_ID_SELECTOR, rowInstanceId)).findElement(By.xpath("./../.."));
        return row;
    }

    public static Optional<WebElement> getTableRowByPackageName(RemoteWebDriver driver, String packageName) {
        List<WebElement> tableRows = (List) querySelect(driver, ALL_PRODUCT_ROWS_SELECTOR, false);
        if (Objects.isNull(tableRows)) {
            return Optional.empty();
        }
        final String pkgNameWithoutVersion = removeVersionFromPkgName(packageName);
        return tableRows.stream().filter(element -> element.getText().contains(pkgNameWithoutVersion)).findFirst();
    }

    /**
     * This method is used to remove package version from the middle of the package name.
     * Otherwise, the name of the packages will not match to the one displayed in Packages
     * List on UI. Example:
     * package name is spider-app-multi-a-1.0.23-v2
     * UI will display this package as spider-app-multi-a-v2
     * so, it is impossible to match one against another.
     */
    private static String removeVersionFromPkgName(String packageName) {
        Pattern p = Pattern.compile("-\\d+\\.+[.\\d]+-*",
                                    Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        return p.matcher(packageName).replaceAll("-").replaceAll("-$", "");
    }

    // CISM Clusters Table
    public static Optional<WebElement> getTableRowByCISMClusterName(RemoteWebDriver driver, String cluterName) {
        List<WebElement> tableRows = (List) querySelect(driver, ALL_CLUSTER_ROWS_SELECTOR, false);
        return tableRows.stream().filter(element -> cluterName.contains(element.getText())).findFirst();
    }

    public static WebElement getTableRowByRowName(RemoteWebDriver driver, String rowName, String tableIDSelector) {
        List<WebElement> allRows = querySelectAll(driver, String.format(TABLE_ROW_SELECTOR, tableIDSelector));
        if (allRows != null) {
            for (int i = 1; i <= allRows.size(); i++) {
                WebElement test = querySelect(driver,
                                              String.format(TABLE_ROW_SELECTOR, tableIDSelector) + ":nth-of-type(" + i + ")"
                                                      + TABLE_CELL_NAME_ROW_SELECTOR);
                if (test.getAttribute(TITLE_ATTRIBUTE).equals(rowName)) {
                    return allRows.get(i - 1);
                }
            }
        }
        return null;
    }

    private static ResourceInfo getResourceInfo(WebElement element) {
        LOGGER.info("Get resource info from the Resources table");
        String lcmOperation = element.findElement(By.cssSelector(COLUMN_LAST_OPERATION_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String resourceName = element.findElement(By.cssSelector(COLUMN_RESOURCE_INSTANCE_NAME_SELECTOR)).getText();
        String type = element.findElement(By.cssSelector(COLUMN_TYPE_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String softwareVersion = element.findElement(By.cssSelector(COLUMN_SOFTWARE_VERSION_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String packageVersion = element.findElement(By.cssSelector(COLUMN_PACKAGE_VERSION_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String cluster = element.findElement(By.cssSelector(COLUMN_CLUSTER_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String lastModifiedAt = element.findElement(By.cssSelector(COLUMN_LAST_MODIFIED_AT_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        WebElement operationStateElement = element.findElement(By.cssSelector(COLUMN_LAST_OPERATION_STATE_SELECTOR));
        String operationState;
        String operationTitleAttribute = operationStateElement.getAttribute(TITLE_ATTRIBUTE);

        if (operationTitleAttribute != null && !operationTitleAttribute.isEmpty()) {
            operationState = operationStateElement.getAttribute(TITLE_ATTRIBUTE);
        } else {
            operationState = operationStateElement.getAttribute(CELL_VALUE_ATTRIBUTE);
        }

        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceInstanceName(resourceName);
        resourceInfo.setType(type);
        resourceInfo.setSoftwareVersion(softwareVersion);
        resourceInfo.setPackageVersion(packageVersion);
        resourceInfo.setLastOperation(lcmOperation);
        resourceInfo.setOperationState(operationState);
        resourceInfo.setCluster(cluster);
        resourceInfo.setLastModifiedAt(lastModifiedAt);

        return resourceInfo;
    }
}
