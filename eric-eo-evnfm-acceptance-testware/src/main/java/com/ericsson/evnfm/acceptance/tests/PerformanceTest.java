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
package com.ericsson.evnfm.acceptance.tests;

import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.onboardCsars;
import static com.ericsson.evnfm.acceptance.utils.Constants.PERFORMANCE;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.models.EvnfmPackage;

@Tag(PERFORMANCE)
public class PerformanceTest {

    @BeforeAll
    public static void setup() throws IOException {
        if (!ConfigurationProvider.isPreconfigured()) {
            ConfigurationProvider.setConfiguration("config.json");
        }
    }

    @Execution(ExecutionMode.CONCURRENT)
    @DisplayName("Onboarding Csar")
    @ParameterizedTest
    @MethodSource("com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps#csarPackagesList")
    public void onboardingAcceptanceTest(EvnfmPackage csarPackage) {
        onboardCsars(csarPackage);
    }
}
