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

import com.ericsson.vnfm.orchestrator.model.TerminateVnfRequest.TerminationTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigTerminate {
    private String expectedOperationState;
    private String applicationTimeOut = "3600";
    private String commandTimeOut = "300";
    private boolean skipVerification;
    private boolean cleanUpResources;
    private String pvcTimeOut;
    private String terminationType;

    public String getTerminationType() {
        return terminationType;
    }

    public TerminationTypeEnum getTerminationTypeEnum() {
        return TerminationTypeEnum.valueOf(terminationType);
    }

    public void setTerminationType(final String terminationType) {
        this.terminationType = terminationType;
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

    public boolean isCleanUpResources() {
        return cleanUpResources;
    }

    public void setCleanUpResources(final boolean cleanUpResources) {
        this.cleanUpResources = cleanUpResources;
    }

    public String getPvcTimeOut() {
        return pvcTimeOut;
    }

    public void setPvcTimeOut(final String pvcTimeOut) {
        this.pvcTimeOut = pvcTimeOut;
    }

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }
}
