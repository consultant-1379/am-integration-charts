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

import java.util.Map;

public class ModifyVnfInfo {

    private String vnfInstanceName;

    private String vnfInstanceDescription;

    private Map<String,String> extensions;

    private String cluster;

    private String applicationTimeout;

    private String expectedOperationState;

    public String getVnfInstanceDescription() {
        return vnfInstanceDescription;
    }

    public void setVnfInstanceDescription(String vnfInstanceDescription) {
        this.vnfInstanceDescription = vnfInstanceDescription;
    }

    public Map<String, String> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getApplicationTimeout() {
        return applicationTimeout;
    }

    public void setApplicationTimeout(String applicationTimeout) {
        this.applicationTimeout = applicationTimeout;
    }

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }

}
