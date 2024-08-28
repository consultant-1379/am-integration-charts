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

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

public class UpgradeWithDifferentChartNumbersTestDataProviders {

    private static final String UPGRADE_WITH_DIFFERENT_CHART_NUMBERS_DATA_PARAMETER = "upgradeWithDifferentChartNumbersData";
    private static final String SINGLE_MULTI_LCM_DATA_PATH = "release-multi/upgrade/single-multi-lcm.yaml";

    public static List<EvnfmCnf> loadLcmConfigData(ITestContext iTestContext, String configEntityName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, configEntityName, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfsToInstantiate;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_WITH_DIFFERENT_CHART_NUMBERS_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? SINGLE_MULTI_LCM_DATA_PATH : dataFilename;
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_INSTANTIATE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToUpgrade(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_UPGRADE).toArray();
    }
}
