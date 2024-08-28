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
package com.ericsson.evnfm.acceptance.models;

import java.util.Objects;

public class BackupAdditionalParam {

    private String backupName;
    private String scope;

    private BackupExportParams remote;

    public BackupAdditionalParam() {
    }

    public BackupAdditionalParam(final String backupName, final String scope) {
        this.backupName = backupName;
        this.scope = scope;
    }

    public void setBackupName(final String backupName) {
        this.backupName = backupName;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public String getBackupName() {
        return backupName;
    }

    public String getScope() {
        return scope;
    }

    public BackupExportParams getRemote() {
        return remote;
    }

    public void setRemote(final BackupExportParams remote) {
        this.remote = remote;
    }

    @Override
    public String toString() {
        return "BackupAdditionalParam{" +
                "backupName='" + backupName + '\'' +
                ", scope='" + scope + '\'' +
                ", remote='" + remote + '\'' +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BackupAdditionalParam)) {
            return false;
        }
        final BackupAdditionalParam that = (BackupAdditionalParam) o;
        return Objects.equals(backupName, that.backupName) && Objects.equals(scope, that.scope)
                && Objects.equals(remote, that.remote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backupName, scope, remote);
    }
}