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

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigEvnfmInstall {
    @JsonProperty
    private String chartVersion = "";

    @JsonProperty(required = true)
    private String valuesFile;

    public String getChartVersion() {
        return chartVersion;
    }

    public void setChartVersion(final String chartVersion) {
        this.chartVersion = chartVersion;
    }

    public String getValuesFile() {
        return valuesFile;
    }

    public void setValuesFile(final String valuesFile) {
        this.valuesFile = valuesFile;
    }
}
