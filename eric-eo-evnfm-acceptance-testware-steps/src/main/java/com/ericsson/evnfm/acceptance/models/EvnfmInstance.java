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

public class EvnfmInstance {

    private String evnfmUrl;
    private String idamUrl;
    private String helmRegistryUrl;
    private String idamAdminUser;
    private String idamAdminPassword;
    private String idamClientSecret;
    private String idamClientId;
    private String idamRealm;
    private String csarDownloadPath;
    private String helm;

    private String namespace;
    private String dracEnabled;

    public EvnfmInstance() {
    }

    public String getEvnfmUrl() {
        return evnfmUrl;
    }

    public void setEvnfmUrl(final String evnfmUrl) {
        this.evnfmUrl = evnfmUrl;
    }

    public String getHelmRegistryUrl() {
        return helmRegistryUrl;
    }

    public void setHelmRegistryUrl(final String helmRegistryUrl) {
        this.helmRegistryUrl = helmRegistryUrl;
    }

    public String getIdamUrl() {
        return idamUrl;
    }

    public void setIdamUrl(String idamUrl) {
        this.idamUrl = idamUrl;
    }

    public String getIdamAdminUser() {
        return idamAdminUser;
    }

    public void setIdamAdminUser(String idamAdminUser) {
        this.idamAdminUser = idamAdminUser;
    }

    public String getIdamAdminPassword() {
        return idamAdminPassword;
    }

    public void setIdamAdminPassword(String idamAdminPassword) {
        this.idamAdminPassword = idamAdminPassword;
    }

    public String getIdamClientSecret() {
        return idamClientSecret;
    }

    public void setIdamClientSecret(String idamClientSecret) {
        this.idamClientSecret = idamClientSecret;
    }

    public String getIdamClientId() {
        return idamClientId;
    }

    public void setIdamClientId(String idamClientId) {
        this.idamClientId = idamClientId;
    }

    public String getIdamRealm() {
        return idamRealm;
    }

    public void setIdamRealm(String idamRealm) {
        this.idamRealm = idamRealm;
    }

    public String getCsarDownloadPath() {
        return csarDownloadPath;
    }

    public void setCsarDownloadPath(final String csarDownloadPath) {
        this.csarDownloadPath = csarDownloadPath;
    }

    public String getHelm() {
        return helm;
    }

    public void setHelm(final String helm) {
        this.helm = helm;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public String isDracEnabled() {
        return dracEnabled;
    }

    public void setDracEnabled(final String dracEnabled) {
        this.dracEnabled = dracEnabled;
    }

    @Override
    public String toString() {
        return "EvnfmInstance{" +
                "evnfmUrl='" + evnfmUrl + '\'' +
                ", idamUrl='" + idamUrl + '\'' +
                ", idamAdminUser='****'" +
                ", idamAdminPassword='****'" +
                ", idamClientSecret='" + idamClientSecret + '\'' +
                ", idamClientId='" + idamClientId + '\'' +
                ", idamRealm='" + idamRealm + '\'' +
                ", csarDownloadPath='" + csarDownloadPath + '\'' +
                ", helm='" + helm + '\'' +
                ", namespace='" + namespace + '\'' +
                ", dracEnabled='" + dracEnabled + '\'' +
                '}';
    }
}
