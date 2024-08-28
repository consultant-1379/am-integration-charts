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
package com.ericsson.evnfm.acceptance.steps.backup.ui;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.BackupInfo;
import com.ericsson.evnfm.acceptance.models.UI.page.BackupResourceDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.BackupsTab;
import com.ericsson.evnfm.acceptance.models.UI.page.DeleteBackupDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourceDetailsPage;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;

public class BackupResourceUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupResourceUISteps.class);

    private ResourcesPage resourcesPage;
    private BackupResourceDialog backupResourceDialog;
    private ResourceDetailsPage resourceDetailsPage;
    private BackupsTab backupsTab;
    private BackupInfo backupInfo;
    private DeleteBackupDialog deleteBackupDialog;

    private static final String BACKUP_NAME = "ol-backup";
    private static final String BACKUP_SCOPE = "TEST";
    private static final String TIME_INTERVAL = "5";
    private static final String BACKUP_STATUS = "COMPLETE";

    public void backupResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnf){
        LOGGER.info("Opening Backup resource dialog from the Resources page");
        resourcesPage = new ResourcesPage();
        backupResourceDialog = resourcesPage.openBackupResourceDialog(driver, evnfmCnf.getVnfInstanceName(), evnfmCnf.getCluster().getUIName(),
                                                                      evnfmCnf.getApplicationTimeout());

        LOGGER.info("Creating backup for the resource");
        resourcesPage = backupResourceDialog.createLocalBackupForResource(driver, BACKUP_SCOPE, BACKUP_NAME, evnfmCnf.getVnfInstanceName());

        LOGGER.info("Opening Backups tab for the resource");
        resourceDetailsPage = resourcesPage.openResourceDetailsPage(driver, evnfmCnf.getVnfInstanceName(), evnfmCnf.getCluster().getUIName(),
                                                                    evnfmCnf.getApplicationTimeout());
        backupsTab = resourceDetailsPage.openBackupsTab(driver);

        LOGGER.info("Checking that created backup is displayed in the table and has COMPLETE status");
        BackupInfo backupInfoFromPreconditions = new BackupInfo();
        backupInfoFromPreconditions.setBackupName(BACKUP_NAME);
        backupInfoFromPreconditions.setBackupScope(BACKUP_SCOPE);
        backupInfo = backupsTab.verifyBackupState(driver, BACKUP_NAME, BACKUP_STATUS, TIME_INTERVAL);
        backupsTab.verifyBackupInfo(driver, backupInfoFromPreconditions, backupInfo);

        LOGGER.info("Deleting resource backup");
        deleteBackupDialog = backupsTab.openDeleteBackupDialog(driver, BACKUP_NAME);
        backupsTab = deleteBackupDialog.deleteBackup(driver);
        backupsTab.verifyBackupIsDeleted(driver, BACKUP_NAME);

        LOGGER.info("Return to the Resources page");
        backupsTab.openResourcesPage(driver);
    }
}
