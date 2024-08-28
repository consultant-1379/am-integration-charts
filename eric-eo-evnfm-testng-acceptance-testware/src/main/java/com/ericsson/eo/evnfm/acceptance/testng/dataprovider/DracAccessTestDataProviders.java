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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class DracAccessTestDataProviders {
    private static final String DRAC_DATA_PARAMETER = "dracData";
    private static final String DRAC_DATA_PATH = "dra/drac.yaml";

    @DataProvider
    public Object[][] getBasicAppToInstantiateWithClusterConfig(ITestContext iTestContext) throws IOException {
        final List<EvnfmCnf> cnfsToInstantiate = loadConfigData(iTestContext, CNF_TO_INSTANTIATE);
        final ClusterConfig clusterConfig = loadClusterConfig(iTestContext);

        return new Object[][] { { cnfsToInstantiate.get(0), clusterConfig } };
    }

    @DataProvider
    public Object[] getSpiderAppToInstantiate(ITestContext iTestContext) throws IOException {
        final List<EvnfmCnf> cnfsToInstantiate = loadConfigData(iTestContext, CNF_TO_INSTANTIATE);

        return new Object[] { cnfsToInstantiate.get(1) };
    }

    @DataProvider
    public Object[] getBasicAppToUpgrade(ITestContext iTestContext) throws IOException {
        final List<EvnfmCnf> cnfsToInstantiate = loadConfigData(iTestContext, CNF_TO_UPGRADE);

        return new Object[] { cnfsToInstantiate.get(0) };
    }

    public static List<EvnfmCnf> loadConfigData(ITestContext iTestContext, final String key) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfs = loadYamlConfiguration(dataFilename, key, new TypeReference<>() {
        });
        cnfs.forEach(TestExecutionGlobalConfig::addFlowSuffix);

        return cnfs;
    }

    private static ClusterConfig loadClusterConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, CLUSTER_CONFIG, ClusterConfig.class);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(DRAC_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DRAC_DATA_PATH : dataFilename;
    }
}
