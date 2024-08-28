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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvnfmHelmChart {
    private String name;
    private String version;
    private ChartType chartType;

    public EvnfmHelmChart() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(final ChartType chartType) {
        this.chartType = chartType;
    }

    @Override
    public String toString() {
        return "HelmChart{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", chartType=" + chartType +
                '}';
    }

    public enum ChartType {
        CNF,
        CRD
    }
}
