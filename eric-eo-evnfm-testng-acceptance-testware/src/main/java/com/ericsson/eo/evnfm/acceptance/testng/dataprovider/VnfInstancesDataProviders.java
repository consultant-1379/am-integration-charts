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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CLUSTER_CONFIG;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.fasterxml.jackson.core.type.TypeReference;

public class VnfInstancesDataProviders {

    private static final String VNF_INSTANCES_DATA_PARAMETER = "vnfInstancesData";
    private static final String VNF_INSTANCES_DATA_PATH = "vnfInstance/vnfInstances.yaml";

    private static final String BASIC_ONBOARDING_DATA_PARAMETER = "basicOnboardingData";
    private static final String DEFAULT_BASIC_ONBOARDING_DATA_PATH = "onboarding.yaml";

    @DataProvider(parallel = true)
    public static Object[] loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigVnfInstances(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfsToInstantiate);

        return cnfsToInstantiate.toArray();
    }

    public static List<EvnfmBasePackage> loadOnboardedPackages(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveOnboardingConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, PACKAGES, new TypeReference<>() {
        });
    }

    public static ClusterConfig loadClusterConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigVnfInstances(iTestContext);
        return loadYamlConfiguration(dataFilename, CLUSTER_CONFIG, ClusterConfig.class);
    }

    private static String resolveOnboardingConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BASIC_ONBOARDING_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BASIC_ONBOARDING_DATA_PATH : dataFilename;
    }

    private static String resolveConfigVnfInstances(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(VNF_INSTANCES_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? VNF_INSTANCES_DATA_PATH : dataFilename;
    }
}
