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
package com.ericsson.evnfm.acceptance.models.UI;

public class BackupInfo {

    private String backupName;

    private String creationTime;

    private String status;

    private String backupScope;

    public String getBackupName() { return backupName; }

    public void setBackupName(final String backupName) { this.backupName = backupName; }

    public String getCreationTime() { return creationTime; }

    public void setCreationTime(final String creationTime) { this.creationTime = creationTime; }

    public String getStatus() { return status; }

    public void setStatus(final String status) { this.status = status; }

    public String getBackupScope() { return backupScope; }

    public void setBackupScope(final String backupScope) { this.backupScope = backupScope; }
}
