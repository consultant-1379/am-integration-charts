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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigGeneral {

    private String apiGatewayHost;
    private String vnfmUsername;
    private String vnfmPassword;
    private Integer pageLoadTimeout = 10;
    private String idamHost;
    private String idamAdminUsername;
    private String idamAdminPassword;
    private String idamRealm;
    private String idamClient;
    private String idamClientSecret;
    private String helmRegistryUrl;
    private String dracEnabled;
    private JsonNode additionalDetails;

    public String isDracEnabled() {
        return dracEnabled;
    }

    public void setDracEnabled(String dracEnabled) {
        this.dracEnabled = dracEnabled;
    }

    public String getApiGatewayHost() {
        return apiGatewayHost;
    }

    public void setApiGatewayHost(String apiGatewayHost) {
        this.apiGatewayHost = apiGatewayHost;
    }

    public String getIdamHost() {
        return idamHost;
    }

    public void setIdamHost(String idamHost) {
        this.idamHost = idamHost;
    }

    public String getIdamAdminUsername() {
        return idamAdminUsername;
    }

    public void setIdamAdminUsername(String idamAdminUsername) {
        this.idamAdminUsername = idamAdminUsername;
    }

    public String getIdamAdminPassword() {
        return idamAdminPassword;
    }

    public void setIdamAdminPassword(String idamAdminPassword) {
        this.idamAdminPassword = idamAdminPassword;
    }

    public String getIdamRealm() {
        return idamRealm;
    }

    public void setIdamRealm(String idamRealm) {
        this.idamRealm = idamRealm;
    }

    public String getIdamClient() {
        return idamClient;
    }

    public void setIdamClient(String idamClient) {
        this.idamClient = idamClient;
    }

    public String getIdamClientSecret() {
        return idamClientSecret;
    }

    public void setIdamClientSecret(String idamClientSecret) {
        this.idamClientSecret = idamClientSecret;
    }

    public String getVnfmUsername() {
        return vnfmUsername;
    }

    public void setVnfmUsername(String vnfmUsername) {
        this.vnfmUsername = vnfmUsername;
    }

    public String getVnfmPassword() {
        return vnfmPassword;
    }

    public void setVnfmPassword(String vnfmPassword) {
        this.vnfmPassword = vnfmPassword;
    }

    public String getHelmRegistryUrl() {
        return helmRegistryUrl;
    }

    public void setHelmRegistryUrl(final String helmRegistryUrl) {
        this.helmRegistryUrl = helmRegistryUrl;
    }

    public JsonNode getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(JsonNode additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public Integer getPageLoadTimeout() {
        return pageLoadTimeout;
    }

    public void setPageLoadTimeout(Integer pageLoadTimeout) {
        this.pageLoadTimeout = pageLoadTimeout;
    }
}
