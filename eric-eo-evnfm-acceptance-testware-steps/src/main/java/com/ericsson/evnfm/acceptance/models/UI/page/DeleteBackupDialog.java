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
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class DeleteBackupDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteBackupDialog.class);
    private static final String DELETE_BACKUP_DIALOG_SELECTOR= "eui-base-v0-dialog[label=\"Delete Backup\"] .dialog";
    private static final String DELETE_BUTTON_SELECTOR = "button.btn.primary.warning   ";

    private static final String BACKUP_DELETED_MESSAGE = "Delete backup started\n" + "Request to BRO was successful";

    public BackupsTab deleteBackup(RemoteWebDriver driver){
        checkDeleteBackupFormIsOpened(driver);
        clickOnTheButton(driver, DELETE_BUTTON_SELECTOR);
        checkOperationResultMessageIsReceived(driver, BACKUP_DELETED_MESSAGE);
        checkDeleteBackupFormIsClosed(driver);
        return new BackupsTab();
    }

    public static void checkDeleteBackupFormIsClosed(RemoteWebDriver driver) {
        LOGGER.info("Verify that Delete backup form is closed");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20L));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(DELETE_BACKUP_DIALOG_SELECTOR)));
        assertThat(querySelect(driver, DELETE_BACKUP_DIALOG_SELECTOR)).isNull();
    }

    public static void checkDeleteBackupFormIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Verify that Delete backup form is opened");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20L));
        wait.until(ExpectedConditions.visibilityOf(querySelect(driver, DELETE_BACKUP_DIALOG_SELECTOR)));
        assertThat(querySelect(driver, DELETE_BACKUP_DIALOG_SELECTOR)).isNotNull();
    }
}
