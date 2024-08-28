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
package com.ericsson.eo.evnfm.acceptance.testng.dataprovider.e2e;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.fasterxml.jackson.core.type.TypeReference;

public class BasicOnboardingUIDataProviders {
    private static final String BASIC_ONBOARDING_DATA_PARAMETER = "basicOnboardingUIData";
    private static final String DEFAULT_BASIC_ONBOARDING_UI_DATA_PATH = "cnfOnboardingUI.yaml";

    public static List<EvnfmBasePackage> loadPackagesToOnboard(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, PACKAGES, new TypeReference<>() {
        });
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BASIC_ONBOARDING_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BASIC_ONBOARDING_UI_DATA_PATH : dataFilename;
    }

    @DataProvider(parallel = false)
    public Object[] getPackagesToOnboard(ITestContext iTestContext) throws IOException {
        return loadPackagesToOnboard(iTestContext).toArray();
    }
}
