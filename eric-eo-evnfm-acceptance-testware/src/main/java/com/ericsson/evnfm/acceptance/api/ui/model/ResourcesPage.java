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
package com.ericsson.evnfm.acceptance.api.ui.model;

import com.ericsson.evnfm.acceptance.api.ui.ContextMenu;
import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import com.ericsson.evnfm.acceptance.common.TestwareConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;

public class ResourcesPage {
    private RemoteWebDriver webDriver;

    public ResourcesPage(RemoteWebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public OperationsPage goToOperationsPage() {
        return Navigation.loadOperationsApplication(webDriver);
    }

    public PackagesPage goToPackagesPage() {
        return Navigation.loadPackagesApplication(webDriver);
    }

    public InstantiateWizardPage goToInstantiateNew() {
        String instantiateNewButtonSelector = "eui-app-bar eui-base-v0-button";
        WebElement instantiateNewButton = (WebElement) querySelect(webDriver, instantiateNewButtonSelector, true);
        instantiateNewButton.click();
        return new InstantiateWizardPage(webDriver);
    }

    public UpgradeWizardPage goToUpgradeWizardPage(int row) { //TODO may need to use resource IDs instead of row #
        WebElement contextMenu = ContextMenu.openTableContextMenu(webDriver, row);
        ContextMenu.selectTableContextMenuItem(contextMenu, ContextMenuOption.UPGRADE.toString());
        return new UpgradeWizardPage(webDriver);
    }

    public TerminateDialogPage goToTerminateDialogPage(int row) { //TODO may need to use resource IDs instead of row #
        WebElement contextMenu = ContextMenu.openTableContextMenu(webDriver, row);
        ContextMenu.selectTableContextMenuItem(contextMenu, ContextMenuOption.TERMINATE.toString());
        return new TerminateDialogPage(webDriver);
    }

    public ResourceDetailsPage goToDetailsPage(int row) { //TODO may need to use resource IDs instead of row #
        WebElement contextMenu = ContextMenu.openTableContextMenu(webDriver, row);
        ContextMenu.selectTableContextMenuItem(contextMenu, ContextMenuOption.GO_TO_DETAILS_PAGE.toString());
        return new ResourceDetailsPage(webDriver);
    }

    public ResourcesTable getTable() {
        return new ResourcesTable(getTableHeader(), getTableRows());
    }

    public ResourcesTableHeader getTableHeader() {
        return new ResourcesTableHeader(Table.getHeaders(webDriver));
    }

    public List<ResourcesTableRow> getTableRows() {
        List<WebElement> rows = Table.getRows(webDriver);
        List<String> resourceInstanceNames = getResourceInstanceNames();
        List<String> operationStates = getOperationStates();
        List<ResourcesTableRow> tableRows = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            tableRows.add(new ResourcesTableRow(rows.get(i).findElements(By.cssSelector("td")), resourceInstanceNames.get(i), operationStates.get(i)));
        }
        return tableRows;
    }

    /**
     * Get the table row data. Table with no data returns null.
     */
    public ResourcesTableRow getTableRow(int row) {
        List<WebElement> columns = Table.getRow(webDriver, row);
        String resourceInstanceName = getResourceInstanceNames().get(row);
        String operationState = getOperationStates().get(row);
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        return new ResourcesTableRow(columns, resourceInstanceName, operationState);
    }

    public static class ResourcesTable {
        private final ResourcesTableHeader header;
        public final List<ResourcesTableRow> rows;

        public ResourcesTable(ResourcesTableHeader header, List<ResourcesTableRow> rows) {
            this.header = header;
            this.rows = rows;
        }
    }

    public static class ResourcesTableHeader {
        public List<String> headers;

        public ResourcesTableHeader(List<WebElement> columns) {
            headers = columns.stream().map(WebElement::getText).collect(Collectors.toList());
        }
    }

    public static class ResourcesTableRow {
        public String resourceInstanceName;
        public String type;
        public String softwareVersion;
        public String packageVersion;
        public String lastOperation;
        public String operationState;
        public String cluster;
        public String lastModifiedAt;

        public ResourcesTableRow(List<WebElement> columns, String resourceInstanceName, String operationState) {
            this.resourceInstanceName = resourceInstanceName;
            type = columns.get(1).getText();
            softwareVersion = columns.get(2).getText();
            packageVersion = columns.get(3).getText();
            lastOperation = columns.get(4).getText();
            this.operationState = operationState;
            cluster = columns.get(6).getText();
            lastModifiedAt = columns.get(7).getText();
        }
    }

    private enum ContextMenuOption {
        UPGRADE(TestwareConstants.UPGRADE),
        TERMINATE(TestwareConstants.TERMINATE_MENU_ITEM),
        GO_TO_DETAILS_PAGE(TestwareConstants.GO_TO_DETAILS_PAGE_MENU_ITEM);

        private String label;

        ContextMenuOption(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    //TODO this is a very fragile function that is likely to break if more links are added to the table as there aren't enough unique identifiers in our HTML
    private List<String> getResourceInstanceNames() {
        String selector = ".custom-table__cell .custom-table__cell_value a";
        List<WebElement> columns = (List<WebElement>) querySelect(webDriver, selector, false);
        return columns.stream().map(WebElement::getText).collect(Collectors.toList());
    }

    //TODO this is a very fragile function that is likely to break if more links are added to the table as there aren't enough unique identifiers in our HTML
    private List<String> getOperationStates() {
        String customCellSelector = ".custom-table__cell .custom-table__cell_value";
        List<WebElement> columns = (List<WebElement>) querySelect(webDriver, customCellSelector, false);
        List<String> operationStates = new ArrayList<>();
        // the result set contains two interleaved columns of custom cells. I only want operation states (odd entries)
        for (int i = 1; i < columns.size(); i += 2) {
            operationStates.add(columns.get(i).getText());
        }
        return operationStates;
    }
}
