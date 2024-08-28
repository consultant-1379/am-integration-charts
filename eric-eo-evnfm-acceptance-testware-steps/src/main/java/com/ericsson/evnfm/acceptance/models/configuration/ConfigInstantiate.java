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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigInstantiate {

    private String cluster;
    private String expectedComponentsState;
    private String expectedOperationState;
    private String namespace;
    private String releaseName;
    private String resourceDescription = "";
    private Map<String, Object> additionalAttributes;
    private String additionalAttributesFile;
    private String applicationTimeOut = "3600";
    private String commandTimeOut = "300";
    private boolean cleanUpResources;
    private boolean skipVerification;
    private List<Configuration> configurations;
    private ConfigDay0 configDay0;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(final Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public String getApplicationTimeOut() {
        return applicationTimeOut;
    }

    public void setApplicationTimeOut(String applicationTimeOut) {
        this.applicationTimeOut = applicationTimeOut;
    }

    public String getCommandTimeOut() {
        return commandTimeOut;
    }

    public void setCommandTimeOut(String commandTimeOut) {
        this.commandTimeOut = commandTimeOut;
    }

    public boolean getCleanUpResources() {
        return cleanUpResources;
    }

    public void setCleanUpResources(final boolean cleanUpResources) {
        this.cleanUpResources = cleanUpResources;
    }

    public boolean getSkipVerification() {
        return skipVerification;
    }

    public void setSkipVerification(final boolean skipVerification) {
        this.skipVerification = skipVerification;
    }

    public String getExpectedComponentsState() {
        return expectedComponentsState;
    }

    public void setExpectedComponentsState(String expectedComponentsState) {
        this.expectedComponentsState = expectedComponentsState;
    }

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }

    public ConfigInstantiate clone(){
        ConfigInstantiate clone = new ConfigInstantiate();
        clone.setSkipVerification(this.skipVerification);
        clone.setResourceDescription(this.resourceDescription);
        clone.setReleaseName(this.releaseName);
        clone.setNamespace(this.namespace);
        clone.setExpectedOperationState(this.expectedOperationState);
        clone.setExpectedComponentsState(this.expectedComponentsState);
        clone.setCluster(this.cluster);
        clone.setAdditionalAttributes(new HashMap<>(this.additionalAttributes));
        clone.setApplicationTimeOut(this.applicationTimeOut);
        clone.setCleanUpResources(this.cleanUpResources);
        clone.setCommandTimeOut(this.commandTimeOut);
        clone.setConfigurations(this.configurations);
        clone.setAdditionalAttributesFile(this.additionalAttributesFile);
        clone.setDay0Configuration(this.configDay0);
        return clone;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    public String getAdditionalAttributesFile() {
        return additionalAttributesFile;
    }

    public void setAdditionalAttributesFile(String additionalAttributesFile) {
        this.additionalAttributesFile = additionalAttributesFile;

    }

    public ConfigDay0 getDay0Configuration() {
        return configDay0;
    }

    public void setDay0Configuration(ConfigDay0 configDay0) {
        this.configDay0 = configDay0;
    }
}
