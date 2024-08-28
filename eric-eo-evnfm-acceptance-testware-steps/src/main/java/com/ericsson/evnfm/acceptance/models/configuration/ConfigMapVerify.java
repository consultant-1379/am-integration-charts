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

public class ConfigMapVerify {

    private String attributeData;
    private String configmapData;

    public ConfigMapVerify(String attributeData, String configmapData) {
        this.attributeData = attributeData;
        this.configmapData = configmapData;
    }

    public String getAttributeData() {
        return attributeData;
    }

    public void setAttributeData(String attributeData) {
        this.attributeData = attributeData;
    }

    public String getConfigmapData() {
        return configmapData;
    }

    public void setConfigmapData(String configmapData) {
        this.configmapData = configmapData;
    }
}
