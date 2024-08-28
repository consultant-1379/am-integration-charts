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

import com.ericsson.evnfm.acceptance.ScaleMapping;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigDay0;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EvnfmCnf {

    private String vnfdId;
    private String sourceVnfdId;
    private String packageName;
    private String vnfInstanceName;
    private String vnfInstanceNameToModify;
    private String vnfInstanceDescription;
    private Map<String, String> metadata;
    private ClusterConfig cluster;
    private String namespace;
    private Map<String, Object> additionalParams;
    private String additionalAttributesFile;
    private String applicationTimeout;
    private String expectedOperationState;
    private String expectedComponentsState;
    private boolean cleanUpResources;
    private boolean skipVerification;
    private boolean containerStatusVerification;
    private String instantiateId;
    private VnfInstanceResponseLinks vnfInstanceResponseLinks;
    private Map<String, Object> valuesFilePart;
    private Map<String, Object> expectedHelmValues;
    private String pvcDeleteTimeout;
    private List<String> pods;
    private Map<String, Integer> targets;
    private Map<String, Object> extensions;
    private String instantiationLevel;
    private Map<String, Integer> scaleInfo;
    private String aspectToScale;
    private String stepsToScale;
    private ConfigDay0 configDay0;
    private List<ScaleInfo> targetScaleLevelInfo;
    private Map<String, List<HelmHistory>> expectedHelmHistory;
    private HttpResponse expectedError;

    private List<ScaleMapping> scaleMapping;

    private List<ManualUpgrade> manualUpgrade;
    private Map<String, String> instantiateOssTopology;

    public String getVnfdId() {
        return vnfdId;
    }

    public void setVnfdId(final String vnfdId) {
        this.vnfdId = vnfdId;
    }

    public String getSourceVnfdId() {
        return sourceVnfdId;
    }

    public void setSourceVnfdId(final String sourceVnfdId) {
        this.sourceVnfdId = sourceVnfdId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getVnfInstanceName() {
        return vnfInstanceName;
    }

    public void setVnfInstanceName(final String vnfInstanceName) {
        this.vnfInstanceName = vnfInstanceName;
    }

    public String getVnfInstanceNameToModify() {
        return vnfInstanceNameToModify;
    }

    public void setVnfInstanceNameToModify(final String vnfInstanceNameToModify) {
        this.vnfInstanceNameToModify = vnfInstanceNameToModify;
    }

    public String getVnfInstanceDescription() {
        return vnfInstanceDescription;
    }

    public void setVnfInstanceDescription(final String vnfInstanceDescription) {
        this.vnfInstanceDescription = vnfInstanceDescription;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public ClusterConfig getCluster() {
        return cluster;
    }

    public void setCluster(final ClusterConfig cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(final String namespace) {
        this.namespace = namespace;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    public void setAdditionalParams(final Map<String, Object> additionalParams) {
        this.additionalParams = additionalParams;
    }

    public String getAdditionalAttributesFile() {
        return additionalAttributesFile;
    }

    public void setAdditionalAttributesFile(final String additionalAttributesFile) {
        this.additionalAttributesFile = additionalAttributesFile;
    }

    public String getApplicationTimeout() {
        return applicationTimeout;
    }

    public void setApplicationTimeout(final String applicationTimeout) {
        this.applicationTimeout = applicationTimeout;
    }

    public String getExpectedOperationState() {
        return expectedOperationState;
    }

    public void setExpectedOperationState(final String expectedOperationState) {
        this.expectedOperationState = expectedOperationState;
    }

    public String getExpectedComponentsState() {
        return expectedComponentsState;
    }

    public void setExpectedComponentsState(final String expectedComponentsState) {
        this.expectedComponentsState = expectedComponentsState;
    }

    public boolean isCleanUpResources() {
        return cleanUpResources;
    }

    public void setCleanUpResources(final boolean cleanUpResources) {
        this.cleanUpResources = cleanUpResources;
    }

    public boolean isSkipVerification() {
        return skipVerification;
    }

    public void setSkipVerification(final boolean skipVerification) {
        this.skipVerification = skipVerification;
    }

    public boolean isContainerStatusVerification() {
        return containerStatusVerification;
    }

    public void setContainerStatusVerification(final boolean containerStatusVerification) {
        this.containerStatusVerification = containerStatusVerification;
    }

    public String getInstantiateId() {
        return instantiateId;
    }

    public void setInstantiateId(final String instantiateId) {
        this.instantiateId = instantiateId;
    }

    public VnfInstanceResponseLinks getVnfInstanceResponseLinks() {
        return vnfInstanceResponseLinks;
    }

    public void setVnfInstanceResponseLinks(final VnfInstanceResponseLinks vnfInstanceResponseLinks) {
        this.vnfInstanceResponseLinks = vnfInstanceResponseLinks;
    }

    public Map<String, Object> getValuesFilePart() {
        return valuesFilePart;
    }

    public void setValuesFilePart(final Map<String, Object> valuesFilePart) {
        this.valuesFilePart = valuesFilePart;
    }

    public String getPvcDeleteTimeout() {
        return pvcDeleteTimeout;
    }

    public void setPvcDeleteTimeout(final String pvcDeleteTimeout) {
        this.pvcDeleteTimeout = pvcDeleteTimeout;
    }

    public List<String> getPods() {
        return pods;
    }

    public void setPods(List<String> pods) {
        this.pods = pods;
    }

    public Map<String, Integer> getTargets() {
        return targets;
    }

    public void setTargets(final Map<String, Integer> targets) {
        this.targets = targets;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(final Map<String, Object> extensions) {
        this.extensions = extensions;
    }

    public String getInstantiationLevel() {
        return instantiationLevel;
    }

    public void setInstantiationLevel(final String instantiationLevel) {
        this.instantiationLevel = instantiationLevel;
    }

    public Map<String, Integer> getScaleInfo() {
        return scaleInfo;
    }

    public void setScaleInfo(final Map<String, Integer> scaleInfo) {
        this.scaleInfo = scaleInfo;
    }

    public String getAspectToScale() {
        return aspectToScale;
    }

    public void setAspectToScale(final String aspectToScale) {
        this.aspectToScale = aspectToScale;
    }

    public String getStepsToScale() {
        return stepsToScale;
    }

    public void setStepsToScale(final String stepsToScale) {
        this.stepsToScale = stepsToScale;
    }

    public ConfigDay0 getConfigDay0() {
        return configDay0;
    }

    public void setConfigDay0(final ConfigDay0 configDay0) {
        this.configDay0 = configDay0;
    }

    public List<ScaleInfo> getTargetScaleLevelInfo() {
        return targetScaleLevelInfo;
    }

    public void setTargetScaleLevelInfo(final List<ScaleInfo> targetScaleLevelInfo) {
        this.targetScaleLevelInfo = targetScaleLevelInfo;
    }

    public Map<String, List<HelmHistory>> getExpectedHelmHistory() {
        return expectedHelmHistory;
    }

    public void setExpectedHelmHistory(final Map<String, List<HelmHistory>> expectedHelmHistory) {
        this.expectedHelmHistory = expectedHelmHistory;
    }

    public Map<String, Object> getExpectedHelmValues() {
        return expectedHelmValues;
    }

    public void setExpectedHelmValues(final Map<String, Object> expectedHelmValues) {
        this.expectedHelmValues = expectedHelmValues;
    }

    public List<ScaleMapping> getScaleMapping() {
        return scaleMapping;
    }

    public void setScaleMapping(List<ScaleMapping> scaleMapping) {
        this.scaleMapping = scaleMapping;
    }

    public List<ManualUpgrade> getManualUpgrade() {
        return manualUpgrade;
    }

    public void setManualUpgrade(List<ManualUpgrade> manualUpgrade) {
        this.manualUpgrade = manualUpgrade;
    }

    public HttpResponse getExpectedError() {
        return expectedError;
    }

    public void setExpectedError(final HttpResponse expectedError) {
        this.expectedError = expectedError;
    }

    public Map<String, String> getInstantiateOssTopology() {
        return instantiateOssTopology;
    }

    public void setInstantiateOssTopology(final Map<String, String> instantiateOssTopology) {
        this.instantiateOssTopology = instantiateOssTopology;
    }

    @Override
    public String toString() {
        return "EvnfmCnf{" +
                "vnfdId='" + vnfdId + '\'' +
                ", packageName='" + packageName + '\'' +
                ", sourceVnfdId='" + sourceVnfdId + '\'' +
                ", vnfInstanceName='" + vnfInstanceName + '\'' +
                ", vnfInstanceNameToModify='" + vnfInstanceNameToModify + '\'' +
                ", vnfInstanceDescription='" + vnfInstanceDescription + '\'' +
                ", metadata=" + metadata +
                ", cluster=" + cluster +
                ", namespace='" + namespace + '\'' +
                ", additionalParams=" + additionalParams +
                ", additionalAttributesFile='" + additionalAttributesFile + '\'' +
                ", applicationTimeout='" + applicationTimeout + '\'' +
                ", expectedOperationState='" + expectedOperationState + '\'' +
                ", expectedComponentsState='" + expectedComponentsState + '\'' +
                ", cleanUpResources=" + cleanUpResources +
                ", skipVerification=" + skipVerification +
                ", containerStatusVerification=" + containerStatusVerification +
                ", instantiateId='" + instantiateId + '\'' +
                ", vnfInstanceResponseLinks=" + vnfInstanceResponseLinks +
                ", valuesFilePart='" + valuesFilePart + '\'' +
                ", pvcDeleteTimeout='" + pvcDeleteTimeout + '\'' +
                ", pods=" + pods +
                ", targets=" + targets +
                ", extensions=" + extensions +
                ", instantiationLevel='" + instantiationLevel + '\'' +
                ", scaleInfo=" + scaleInfo +
                ", aspectToScale='" + aspectToScale + '\'' +
                ", stepsToScale='" + stepsToScale + '\'' +
                ", scaleMapping='" + scaleMapping + '\'' +
                ", manualUpgrade='" + manualUpgrade + '\'' +
                ", targetScaleLevelInfo=" + targetScaleLevelInfo + '\'' +
                ", expectedHelmHistory=" + expectedHelmHistory + '\'' +
                ", expectedError=" + expectedError + '\'' +
                ", instantiateOssTopology=" + instantiateOssTopology + '\'' +
                '}';
    }
}
