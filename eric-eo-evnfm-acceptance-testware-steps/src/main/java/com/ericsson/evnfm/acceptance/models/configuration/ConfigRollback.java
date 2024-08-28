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

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigRollback {
    private String expectedOperationState;
    private String expectedComponentsState;
    private String commandTimeOut = "300";

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }

    public String getExpectedComponentsState() {
        return expectedComponentsState;
    }

    public void setExpectedComponentsState(String expectedComponentsState) {
        this.expectedComponentsState = expectedComponentsState;
    }

    public String getCommandTimeOut() {
        return commandTimeOut;
    }

    public void setCommandTimeOut(String commandTimeOut) {
        this.commandTimeOut = commandTimeOut;
    }
}
