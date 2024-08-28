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

public class BackupRequest {

    private String vnfInstanceName;

    private String namespace;

    private ClusterConfig cluster;

    private BackupAdditionalParam additionalParams;

    public BackupRequest() {
    }

    public BackupRequest(final BackupAdditionalParam additionalParams) {
        this.additionalParams = additionalParams;
    }

    public void setAdditionalParams(final BackupAdditionalParam additionalParams) {
        this.additionalParams = additionalParams;
    }

    public BackupAdditionalParam getAdditionalParams() {
        return additionalParams;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(final String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public void setCluster(final ClusterConfig cluster) {
        this.cluster = cluster;
    }

    @Override
    public String toString() {
        return "BackupRequest{" +
                "additionalParams=" + additionalParams +
                "vnfInstanceName=" + vnfInstanceName +
                "cluster=" + cluster +
                "namespace=" + namespace +
                '}';
    }
}