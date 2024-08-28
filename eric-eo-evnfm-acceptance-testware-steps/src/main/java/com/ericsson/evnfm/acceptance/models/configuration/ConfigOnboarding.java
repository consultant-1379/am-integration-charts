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

import static com.ericsson.evnfm.acceptance.utils.Constants.BUR;
import static com.ericsson.evnfm.acceptance.utils.Constants.REST;
import static com.ericsson.evnfm.acceptance.utils.Constants.UI;

import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.evnfm.acceptance.models.EvnfmPackage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigOnboarding {

    private List<EvnfmPackage> evnfmPackages;
    private long onboardingHealthTimeout = 240;
    private String csarDownloadPath = "target/csars";

    public long getOnboardingHealthTimeout() {
        return onboardingHealthTimeout;
    }

    public void setOnboardingHealthTimeout(final long onboardingHealthTimeout) {
        this.onboardingHealthTimeout = onboardingHealthTimeout;
    }

    public List<EvnfmPackage> getPackages() {
        return evnfmPackages;
    }

    public List<EvnfmPackage> getPackages(EvnfmTestInfo evnfmTestInfo) {
        String testType = evnfmTestInfo.getType();
        if (testType.equalsIgnoreCase(BUR)) {
            System.out.println("Filtering for BUR packages");
            return evnfmPackages.stream()
                    .filter(p -> evnfmTestInfo.getPhase() == p.getPhase())
                    .collect(Collectors.toList());
        } else if (testType.equalsIgnoreCase(REST) || testType.equalsIgnoreCase(UI)) {
            System.out.println("Filtering for " + testType);
            return evnfmPackages.stream()
                    .filter(p -> testType.equals(p.getTestType()))
                    .collect(Collectors.toList());
        } else {
            System.out.println("Not filtering any packages, using all");
            return evnfmPackages;
        }
    }

    public void setPackages(List<EvnfmPackage> evnfmPackages) {
        this.evnfmPackages = evnfmPackages;
    }

    public String getCsarDownloadPath() {
        return csarDownloadPath;
    }

    public void setCsarDownloadPath(final String csarDownloadPath) {
        this.csarDownloadPath = csarDownloadPath;
    }
}
