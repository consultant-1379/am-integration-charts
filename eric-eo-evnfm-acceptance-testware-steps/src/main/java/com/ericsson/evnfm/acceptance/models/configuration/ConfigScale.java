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

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigScale {

    private String packageName;
    private String expectedOperationState;
    private String expectedComponentsState;
    private String applicationTimeout = "3600";
    private String commandTimeout = "300";
    private String numberOfSteps = "1";
    private String aspectId;
    private Map<String, String> deployments;
    private Map<String, String> statefulSets;
    private Map<String, String> replicaSets;
    private Map<String, Object> additionalAttributes;

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

    public String getApplicationTimeout() {
        return applicationTimeout;
    }

    public void setApplicationTimeout(final String applicationTimeout) {
        this.applicationTimeout = applicationTimeout;
    }

    public String getCommandTimeout() {
        return commandTimeout;
    }

    public void setCommandTimeout(final String commandTimeout) {
        this.commandTimeout = commandTimeout;
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

    public Map<String, String> getDeployments() {
        return deployments;
    }

    public void setDeployments(final Map<String, String> deployments) {
        this.deployments = deployments;
    }

    public Map<String, String> getStatefulSets() {
        return statefulSets;
    }

    public void setStatefulSets(final Map<String, String> statefulSets) {
        this.statefulSets = statefulSets;
    }

    public Map<String, String> getReplicaSets() {
        return replicaSets;
    }

    public void setReplicaSets(final Map<String, String> replicaSets) {
        this.replicaSets = replicaSets;
    }

    public String getNumberOfSteps() {
        return numberOfSteps;
    }

    public void setNumberOfSteps(final String numberOfSteps) {
        this.numberOfSteps = numberOfSteps;
    }

    public String getAspectId() {
        return aspectId;
    }

    public void setAspectId(final String aspectId) {
        this.aspectId = aspectId;
    }
}
