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

public class PackagesPage {
    private RemoteWebDriver webDriver;

    public PackagesPage(RemoteWebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public OperationsPage goToOperationsPage() {
        return Navigation.loadOperationsApplication(webDriver);
    }

    public ResourcesPage goToResourcesPage() {
        return Navigation.loadResourcesApplication(webDriver);
    }

    public InstantiateWizardPage goToInstantiateWizardPage(int row) { //TODO may need to use resource IDs instead of row #
        WebElement contextMenu = ContextMenu.openTableContextMenu(webDriver, row);
        ContextMenu.selectTableContextMenuItem(contextMenu, ContextMenuOption.INSTANTIATE.toString());
        return new InstantiateWizardPage(webDriver);
    }

    public PackageDetailsPage goToDetailsPage(int row) { //TODO may need to use resource IDs instead of row #
        WebElement contextMenu = ContextMenu.openTableContextMenu(webDriver, row);
        ContextMenu.selectTableContextMenuItem(contextMenu, ContextMenuOption.GO_TO_DETAILS_PAGE.toString());
        return new PackageDetailsPage(webDriver);
    }

    public PackagesTable getTable() {
        return new PackagesTable(getTableHeader(), getTableRows());
    }

    public PackagesTableHeader getTableHeader() {
        return new PackagesTableHeader(Table.getHeaders(webDriver));
    }

    public List<PackagesTableRow> getTableRows() {
        List<WebElement> rows = Table.getRows(webDriver);
        List<String> packageNames = getPackageNames();
        List<PackagesTableRow> tableRows = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            tableRows.add(new PackagesTableRow(rows.get(i).findElements(By.cssSelector("td")), packageNames.get(i)));
        }
        return tableRows;
    }

    /**
     * Get the table row data. Table with no data returns null.
     */
    public PackagesTableRow getTableRow(int row) {
        List<WebElement> columns = Table.getRow(webDriver, row);
        String packageName = getPackageNames().get(row);
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        return new PackagesTableRow(columns, packageName);
    }

    public static class PackagesTable {
        public PackagesTableHeader header;
        public List<PackagesTableRow> rows;

        public PackagesTable(PackagesTableHeader header, List<PackagesTableRow> rows) {
            this.header = header;
            this.rows = rows;
        }
    }

    public static class PackagesTableHeader {
        public List<String> headers;

        PackagesTableHeader(List<WebElement> columns) {
            headers = new ArrayList<>(columns.size());
            for (int i = 0; i < columns.size(); i++) {
                headers.add(columns.get(i).getText());
            }
        }
    }

    public static class PackagesTableRow {
        public String packageName;
        public String type;
        public String softwareVersion;
        public String packageVersion;
        public String provider;
        public String usageState;

        PackagesTableRow(List<WebElement> columns, String packageName) {
            this.packageName = packageName;
            type = columns.get(1).getText();
            softwareVersion = columns.get(2).getText();
            packageVersion = columns.get(3).getText();
            provider = columns.get(4).getText();
            usageState = columns.get(5).getText();
        }
    }

    private enum ContextMenuOption {
        INSTANTIATE(TestwareConstants.INSTANTIATE),
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
    private List<String> getPackageNames() {
        String selector = ".custom-table__cell .custom-table__cell_value a";
        List<WebElement> columns = (List<WebElement>) querySelect(webDriver, selector, false);
        return columns.stream().map(WebElement::getText).collect(Collectors.toList());
    }
}
