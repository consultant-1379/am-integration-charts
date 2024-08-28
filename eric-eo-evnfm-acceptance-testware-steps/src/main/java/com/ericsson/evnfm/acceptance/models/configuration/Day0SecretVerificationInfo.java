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
package com.ericsson.evnfm.acceptance.models.configuration;

public class Day0SecretVerificationInfo {
    private ConfigDay0 configDay0;
    private String vnfInstanceName;
    private String namespace;
    private String clusterConfig;

    public Day0SecretVerificationInfo(final ConfigDay0 configDay0, final String vnfInstanceName, final String namespace, final String clusterConfig) {
        this.configDay0 = configDay0;
        this.vnfInstanceName = vnfInstanceName;
        this.namespace = namespace;
        this.clusterConfig = clusterConfig;
    }

    public ConfigDay0 getConfigDay0() {
        return configDay0;
    }

    public void setConfigDay0(final ConfigDay0 configDay0) {
        this.configDay0 = configDay0;
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

    public String getClusterConfig() {
        return clusterConfig;
    }

    public void setClusterConfig(final String clusterConfig) {
        this.clusterConfig = clusterConfig;
    }
}
