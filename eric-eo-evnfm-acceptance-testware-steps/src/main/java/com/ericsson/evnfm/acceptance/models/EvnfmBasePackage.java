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

import java.util.List;
import java.util.Map;

import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvnfmBasePackage {

    private String packageName;
    private String vnfdId;
    private int numberOfCharts;
    private boolean skipImageUpload;
    private Integer timeOut;
    private String onboardedId;
    private String vnfdFile;
    private Map<String, String> supportedOperations;
    private List<EvnfmHelmChart> charts;
    private List<EvnfmDockerImage> images;
    private String packagePath;
    private VnfPkgInfo.PackageSecurityOptionEnum packageSecurityOption;
    private boolean inputStreamRequest;
    private boolean rel4;
}
