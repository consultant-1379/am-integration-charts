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

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.commonUIActions.ContextMenuActions.clickContextMenuItem;
import static com.ericsson.evnfm.acceptance.commonUIActions.ContextMenuActions.clickTableContextMenu;
import static com.ericsson.evnfm.acceptance.commonUIActions.TableActions.getTableRowByRowName;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.manualSleep;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_MENU_ITEM;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.UI.BackupInfo;

public class BackupsTab {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupsTab.class);

    private static final String BACKUPS_TABLE_SELECTOR = "#resource-details-backups-table";
    private static final String COLUMN_NAME_SELECTOR = "td e-custom-cell[column=name]";
    private static final String COLUMN_CREATION_TIME_SELECTOR = "[column=creationTime]";
    private static final String COLUMN_STATUS_SELECTOR = "td e-custom-cell[column=status]";
    private static final String COLUMN_BACKUP_SCOPE_SELECTOR = "[column=scope]";
    private static final String RESOURCES_PAGE_ITEM_SELECTOR = "li[data-id=resources]";
    private static final String MENU_OPTION_SELECTOR = ".menu-option[value=\"%s\"]";
    private static final String BACKUP_ROW_CONTEXT_MENU_SELECTOR = "div[class=custom-table__cell_value][title=\"%s\"]+e-context-menu[class=custom-table__cell_context_menu]";

    private static final String TITLE_ATTRIBUTE = "title";

    public WebElement findBackupByName(RemoteWebDriver driver, String backupName){
        LOGGER.info("Find backup row by backup name in the Backups table");
        WebElement row = getTableRowByRowName(driver, backupName, BACKUPS_TABLE_SELECTOR);
        return row;
    }

    public BackupInfo getBackupRowInfo(RemoteWebDriver driver, String backupName){
        LOGGER.info("Get backup row info from Backups table");
        WebElement operationRow = findBackupByName(driver, backupName);
        return getBackupInfo(operationRow);
    }

    private static BackupInfo getBackupInfo(WebElement element) {
        LOGGER.info("Get backup info from the Backups table");
        BackupInfo backupInfo = new BackupInfo();
        String backupName = element.findElement(By.cssSelector(COLUMN_NAME_SELECTOR)).getText();
        String creationTime = element.findElement(By.cssSelector(COLUMN_CREATION_TIME_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);
        String status = element.findElement(By.cssSelector(COLUMN_STATUS_SELECTOR)).getText();//.getAttribute(TITLE_ATTRIBUTE);
        String backupScope = element.findElement(By.cssSelector(COLUMN_BACKUP_SCOPE_SELECTOR)).getAttribute(TITLE_ATTRIBUTE);

        backupInfo.setBackupName(backupName);
        backupInfo.setCreationTime(creationTime);
        backupInfo.setStatus(status);
        backupInfo.setBackupScope(backupScope);
        return backupInfo;
    }

    public BackupInfo verifyBackupState(RemoteWebDriver driver, String backupName, String status, String timeInterval){
        long applicationTimeoutMs = Long.parseLong(timeInterval)*1000;
        BackupInfo backupInfo = getBackupRowInfo(driver, backupName);
        LOGGER.debug("Waiting for backup to complete operation...");
        while(!status.equals(backupInfo.getStatus())){
            manualSleep(applicationTimeoutMs);
            driver.navigate().refresh();
            ResourceDetailsPage resourceDetailsPage = new ResourceDetailsPage();
            resourceDetailsPage.openBackupsTab(driver);
            backupInfo = getBackupRowInfo(driver, backupName);
        }
        return backupInfo;
    }

    public void verifyBackupInfo(RemoteWebDriver driver, BackupInfo backupInfoFromPreconditions, BackupInfo backupInfoFromTable){
        LOGGER.info("Verify that backup info is displayed in the table according to the backup creation info");
        assertThat(backupInfoFromTable.getBackupName()).isEqualTo(backupInfoFromPreconditions.getBackupName());
        assertThat(backupInfoFromTable.getBackupScope()).isEqualTo(backupInfoFromPreconditions.getBackupScope());
    }

    public ResourcesPage openResourcesPage(RemoteWebDriver driver){
        LOGGER.info("Open Resources page");
        WebElement resourcesPageItem = querySelect(driver, RESOURCES_PAGE_ITEM_SELECTOR);
        resourcesPageItem.click();
        return new ResourcesPage();
    }

    public DeleteBackupDialog openDeleteBackupDialog(RemoteWebDriver driver, String backupName){
        WebElement backupRowContextMenu = querySelect(driver, String.format(BACKUP_ROW_CONTEXT_MENU_SELECTOR, backupName));
        clickTableContextMenu(driver, backupRowContextMenu);
        LOGGER.debug("Clicking delete option to open Delete backup dialog");
        clickContextMenuItem(driver, String.format(BACKUP_ROW_CONTEXT_MENU_SELECTOR, backupName), String.format(MENU_OPTION_SELECTOR, DELETE_MENU_ITEM));
        return new DeleteBackupDialog();
    }

    public void verifyBackupIsDeleted(RemoteWebDriver driver, String backupName){
        LOGGER.debug("Verify that backup is deleted and not displayed in the list of backups");
        WebElement row = getTableRowByRowName(driver, backupName, BACKUPS_TABLE_SELECTOR);
        while(row!=null){
            manualSleep(5000);
            driver.navigate().refresh();
            ResourceDetailsPage resourceDetailsPage = new ResourceDetailsPage();
            resourceDetailsPage.openBackupsTab(driver);
            row = getTableRowByRowName(driver, backupName, BACKUPS_TABLE_SELECTOR);
        }
        assertThat(row).isNull();
    }
}
