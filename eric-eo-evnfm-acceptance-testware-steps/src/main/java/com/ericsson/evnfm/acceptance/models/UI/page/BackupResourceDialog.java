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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.checkOperationResultMessageIsReceived;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.checkSelectedDropdownValue;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillTextField;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.selectDropdownOption;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class BackupResourceDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupResourceDialog.class);

    private static final String BACKUP_RESOURCE_DIALOG_SELECTOR = "eui-base-v0-dialog[label=\"Backup resource\"] .dialog";
    private static final String CANCEL_BUTTON_SELECTOR = ".cancel";
    private static final String BACKUP_BUTTON_SELECTOR = "#backup";
    private static final String SCOPE_DROPDOWN_SELECTOR = "#backup-scope";
    private static final String SCOPE_DROPDOWN_LIST_SELECTOR = "#backup-scope eui-base-v0-radio-button";
    private static final String DROPDOWN_FIELD_SELECTOR = "#backup-scope eui-base-v0-dropdown";
    private static final String BACKUP_NAME_FIELD_SELECTOR = "#backup-name";

    private static final String BACKUP_STARTED_MESSAGE = "Backup started\n" + "%s is being backed up";

    public ResourcesPage createLocalBackupForResource(RemoteWebDriver driver, String scope,
                                                      String backupName, String vnfInstanceName){
        checkBackupResourceFormIsOpened(driver);
        selectDropdownOption(driver, SCOPE_DROPDOWN_LIST_SELECTOR, scope, SCOPE_DROPDOWN_SELECTOR);
        fillTextField(driver, BACKUP_NAME_FIELD_SELECTOR, backupName);
        checkBackupResourceFormFilledCorrectly(driver, scope, backupName);
        clickOnTheButton(driver, BACKUP_BUTTON_SELECTOR);
        checkOperationResultMessageIsReceived(driver, String.format(BACKUP_STARTED_MESSAGE, vnfInstanceName));
        checkBackupResourceFormIsClosed(driver);
        return new ResourcesPage();
    }

    public static void checkBackupResourceFormIsClosed(RemoteWebDriver driver) {
        LOGGER.info("Verify that Backup resource form is closed");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20L));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(BACKUP_RESOURCE_DIALOG_SELECTOR)));
        assertThat(querySelect(driver, BACKUP_RESOURCE_DIALOG_SELECTOR)).isNull();
    }

    public static void checkBackupResourceFormIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Verify that Backup resource form is opened");
        waitForElementWithText(driver, BACKUP_RESOURCE_DIALOG_SELECTOR, "Backup resource\nCancel", 360000, 10000);
        assertThat(querySelect(driver, BACKUP_RESOURCE_DIALOG_SELECTOR)).isNotNull();
    }

    private void checkBackupResourceFormFilledCorrectly(RemoteWebDriver driver, String scope, String backupName){
        LOGGER.info("Verify that Backup resource form is filled correctly");
        checkSelectedDropdownValue(driver, DROPDOWN_FIELD_SELECTOR, scope);
        assertThat(querySelect(driver, BACKUP_NAME_FIELD_SELECTOR).getAttribute("value")).isEqualTo(backupName);
    }
}
