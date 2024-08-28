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

import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.Table;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;

public class OperationsPage {
    private RemoteWebDriver webDriver;

    public OperationsPage(RemoteWebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public ResourcesPage goToResourcesPage() {
        return Navigation.loadResourcesApplication(webDriver);
    }

    public PackagesPage goToPackagesPage() {
        return Navigation.loadPackagesApplication(webDriver);
    }

    public OperationsTable getTable() {
        OperationsTableHeader header = getTableHeader();
        List<OperationsTableRow> rows = getTableRows();
        return new OperationsTable(header, rows);
    }

    public OperationsTableHeader getTableHeader() {
        return new OperationsTableHeader(Table.getHeaders(webDriver));
    }

    public List<OperationsTableRow> getTableRows() {
        List<WebElement> rows = Table.getRows(webDriver);
        List<String> resourceInstanceNames = getResourceInstanceNames();
        List<OperationsTableRow> tableRows = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            tableRows.add(new OperationsTableRow(rows.get(i).findElements(By.cssSelector("td")), resourceInstanceNames.get(i)));
        }
        return tableRows;
    }

    /**
     * Get the table row data. Table with no data returns null.
     */
    public OperationsTableRow getTableRow(int row) {
        List<WebElement> columns = Table.getRow(webDriver, row);
        String resourceInstanceName = getResourceInstanceNames().get(row);
        if (columns.isEmpty()) {
            return null;
        }
        return new OperationsTableRow(columns, resourceInstanceName);
    }

    public static class OperationsTable {
        public final OperationsTableHeader header;
        public final List<OperationsTableRow> rows;

        public OperationsTable(OperationsTableHeader header, List<OperationsTableRow> rows){
            this.header = header;
            this.rows = rows;
        }
    }

    public static class OperationsTableHeader {
        public List<String> headers;

        public OperationsTableHeader(List<WebElement> columns){
            headers = columns.stream().map(WebElement::getText).collect(Collectors.toList());
        }
    }

    public static class OperationsTableRow {
        public String resourceInstanceName;
        public String operation;
        public String event;
        public String type;
        public String softwareVersion;
        public String timestamp;

        public OperationsTableRow(List<WebElement> columns, String resourceInstanceName) {
            this.resourceInstanceName = resourceInstanceName;
            operation = columns.get(1).getText();
            event = columns.get(2).getText();
            type = columns.get(3).getText();
            softwareVersion = columns.get(4).getText();
            timestamp = columns.get(5).getText();
        }
    }

    //TODO this is a very fragile function that is likely to break if more links are added to the table as there aren't enough unique identifiers in our HTML
    private List<String> getResourceInstanceNames() {
        String selector = ".custom-table__cell .custom-table__cell_value a";
        List<WebElement> columns = (List<WebElement>) querySelect(webDriver, selector, false);
        return columns.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}

