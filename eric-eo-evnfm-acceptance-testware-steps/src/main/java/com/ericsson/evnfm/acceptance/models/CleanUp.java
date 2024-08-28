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

public class CleanUp {

    private String releaseName;
    private String cluster;
    private String kubeConfig;
    private String applicationTimeOut;
    private String namespace;

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(final String cluster) {
        this.cluster = cluster;
    }

    public String getKubeConfig() {
        return kubeConfig;
    }

    public void setKubeConfig(final String kubeConfig) {
        this.kubeConfig = kubeConfig;
    }

    public String getApplicationTimeOut() {
        return applicationTimeOut;
    }

    public void setApplicationTimeOut(final String applicationTimeOut) {
        this.applicationTimeOut = applicationTimeOut;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }
}
