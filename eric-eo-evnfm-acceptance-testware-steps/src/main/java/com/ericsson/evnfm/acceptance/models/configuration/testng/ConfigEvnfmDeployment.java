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
package com.ericsson.evnfm.acceptance.models.configuration.testng;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigEvnfmDeployment {
    private String releaseName = "upgrade-release";
    private ClusterConfig cluster;
    private String namespace = "evnfm-upgrade";
    private int commonTimeout = 600;
    private String helmRepoName = "eric-eo";
    private String helmRepoUrl;

    @JsonProperty(required = true)
    private String chart;

    @JsonProperty("install")
    private ConfigEvnfmInstall install;

    @JsonProperty("upgrade")
    private ConfigEvnfmUpgrade upgrade;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(final String releaseName) {
        this.releaseName = releaseName;
    }

    public int getCommonTimeout() {
        return commonTimeout;
    }

    public void setCommonTimeout(final int commonTimeout) {
        this.commonTimeout = commonTimeout;
    }

    public ConfigEvnfmUpgrade getUpgrade() {
        return upgrade;
    }

    public void setUpgrade(final ConfigEvnfmUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    public ConfigEvnfmInstall getInstall() {
        return install;
    }

    public void setInstall(final ConfigEvnfmInstall install) {
        this.install = install;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public void setCluster(final ClusterConfig cluster) {
        this.cluster = cluster;
    }

    public String getHelmRepoUrl() {
        return helmRepoUrl;
    }

    public void setHelmRepoUrl(final String helmRepoUrl) {
        this.helmRepoUrl = helmRepoUrl;
    }

    public String getHelmRepoName() {
        return helmRepoName;
    }

    public void setHelmRepoName(final String helmRepoName) {
        this.helmRepoName = helmRepoName;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(final String chart) {
        this.chart = chart;
    }

}

