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
package com.ericsson.eo.evnfm.acceptance.testng.dataprovider;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CERTIFICATES;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CLUSTER_CONFIG;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.PACKAGES;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.TCDataProviders.loadPackagesData;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.getConfigContent;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.fasterxml.jackson.core.type.TypeReference;

public class BasicOnboardingDataProviders {
    private static final String BASIC_ONBOARDING_DATA_PARAMETER = "basicOnboardingData";
    private static final String DEFAULT_BASIC_ONBOARDING_DATA_PATH = "onboarding.yaml";

    public static List<EvnfmBasePackage> loadPackagesToOnboard(ITestContext iTestContext) throws IOException, InterruptedException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmBasePackage> packages = loadYamlConfiguration(dataFilename, PACKAGES, new TypeReference<>() {
        });
        loadPackagesData(packages);
        return packages;
    }

    public static Optional<TrustedCertificatesRequest> loadTrustedCertificates(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, Object> onboardingConfig = getYamlParser().load(getConfigContent(dataFilename));
        if (onboardingConfig.containsKey(CERTIFICATES)) {
            return Optional.of(loadYamlConfiguration(dataFilename, CERTIFICATES, TrustedCertificatesRequest.class));
        }
        return Optional.empty();
    }

    public static ClusterConfig loadClusterConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, CLUSTER_CONFIG, ClusterConfig.class);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BASIC_ONBOARDING_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BASIC_ONBOARDING_DATA_PATH : dataFilename;
    }

    @DataProvider(parallel = true)
    public Object[] getPackagesToOnboard(ITestContext iTestContext) throws IOException, InterruptedException {
        return loadPackagesToOnboard(iTestContext).toArray();
    }

    @DataProvider
    public Object[] getClusterConfig(ITestContext iTestContext) throws IOException {
        return new Object[] { loadClusterConfig(iTestContext) };
    }
}
