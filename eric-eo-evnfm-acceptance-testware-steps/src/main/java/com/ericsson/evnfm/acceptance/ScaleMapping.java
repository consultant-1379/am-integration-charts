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
package com.ericsson.evnfm.acceptance;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScaleMapping {
    private String aspectId;
    private Map<String, Integer> targetToReplicasMap;

    public String getAspectId() {
        return aspectId;
    }

    public void setAspectId(String aspectId) {
        this.aspectId = aspectId;
    }

    public Map<String, Integer> getTargetToReplicasMap() {
        return targetToReplicasMap;
    }

    public void setTargetToReplicasMap(Map<String, Integer> targetToReplicasMap) {
        this.targetToReplicasMap = targetToReplicasMap;
    }

    @Override
    public String toString() {
        return "ScaleMapping{" +
                "aspectId='" + aspectId + '\'' +
                ",targetToReplicasMap='" + targetToReplicasMap + '\'' +
                '}';
    }
}
