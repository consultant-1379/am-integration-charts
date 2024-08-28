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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_SCALE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class SyncDataProviders {
    private static final String SYNC_DATA_PARAMETER = "syncData";
    private static final String DEFAULT_SYNC_DATA_PATH = "sync/sync.yaml";

    @DataProvider(parallel = true)
    public static Object[] loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToInstantiate);
        cnfToInstantiate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] getValidSyncCnaData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToScale = loadYamlConfiguration(dataFilename, CNF_TO_SCALE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToScale);
        cnfToScale.forEach(TestExecutionGlobalConfig::setTargets);
        cnfToScale.forEach(TestExecutionGlobalConfig::setManualUpgrade);
        return cnfToScale.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(SYNC_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_SYNC_DATA_PATH : dataFilename;
    }
}
