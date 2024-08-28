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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigCustomResource {
    private List<CustomResourceInfo> customResourceInfoList;

    public List<CustomResourceInfo> getCustomResourceInfoList() {
        return customResourceInfoList;
    }

    public void setCustomResourceInfoList(final List<CustomResourceInfo> customResourceInfoList) {
        this.customResourceInfoList = customResourceInfoList;
    }
}
