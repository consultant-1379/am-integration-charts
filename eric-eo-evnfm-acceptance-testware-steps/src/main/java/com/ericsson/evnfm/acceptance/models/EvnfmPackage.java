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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class EvnfmPackage extends EvnfmBasePackage {

    private String operation;
    private boolean isContainerVerification;
    private Integer phase = 0;
    private boolean isMultiYaml = false;
    private String packageBeingUpgraded;
    private String testType;
    private Integer numberCharts = 1;

    public EvnfmPackage() {
    }

    public EvnfmPackage(final String operation, final String vnfdId, final String packageName, final boolean isContainerVerification) {
        setOperation(operation);
        setVnfdId(vnfdId);
        setPackageName(packageName);
        setContainerVerification(isContainerVerification);
    }

    public String getPackageBeingUpgraded() {
        return packageBeingUpgraded;
    }

    public void setPackageBeingUpgraded(final String packageBeingUpgraded) {
        this.packageBeingUpgraded = packageBeingUpgraded;
    }

    public Integer getPhase() {
        return phase;
    }

    public void setPhase(final Integer phase) {
        this.phase = phase;
    }

    public boolean isMultiYaml() {
        return this.isMultiYaml;
    }

    public void setMultiYaml(boolean multiYaml) {
        this.isMultiYaml = multiYaml;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(final String testType) {
        this.testType = testType;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(final String operation) {
        this.operation = operation;
    }

    public boolean isContainerVerification() {
        return isContainerVerification;
    }

    public void setContainerVerification(final boolean containerVerification) {
        isContainerVerification = containerVerification;
    }

    public Integer getNumberCharts() {
        return numberCharts;
    }

    public void setNumberCharts(final Integer numberCharts) {
        this.numberCharts = numberCharts;
    }

    @Override
    public String toString(){
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
