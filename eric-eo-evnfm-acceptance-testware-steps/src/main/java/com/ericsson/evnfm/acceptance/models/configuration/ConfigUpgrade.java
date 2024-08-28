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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigUpgrade {

    private String packageName;
    private String expectedOperationState;
    private String expectedComponentsState;
    private String applicationTimeOut = "3600";
    private String commandTimeOut = "300";
    private boolean skipVerification;
    private Map<String, Object> additionalAttributes;
    private String additionalAttributesFile;
    private List<Configuration> configurations;

    public Map<String, Object> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public void setAdditionalAttributes(final Map<String, Object> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(final String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }

    public String getApplicationTimeOut() {
        return applicationTimeOut;
    }

    public void setApplicationTimeOut(final String applicationTimeOut) {
        this.applicationTimeOut = applicationTimeOut;
    }

    public String getCommandTimeOut() {
        return commandTimeOut;
    }

    public void setCommandTimeOut(final String commandTimeOut) {
        this.commandTimeOut = commandTimeOut;
    }

    public boolean isSkipVerification() {
        return skipVerification;
    }

    public void setSkipVerification(final boolean skipVerification) {
        this.skipVerification = skipVerification;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getExpectedComponentsState() {
        return expectedComponentsState;
    }

    public void setExpectedComponentsState(final String expectedComponentsState) {
        this.expectedComponentsState = expectedComponentsState;
    }

    public String getAdditionalAttributesFile() {
        return additionalAttributesFile;
    }

    public void setAdditionalAttributesFile(String additionalAttributesFile) {
        this.additionalAttributesFile = additionalAttributesFile;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }
}
