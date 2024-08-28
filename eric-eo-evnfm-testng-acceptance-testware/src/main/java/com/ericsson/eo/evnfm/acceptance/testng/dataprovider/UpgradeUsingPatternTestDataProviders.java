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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.*;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

public class UpgradeUsingPatternTestDataProviders {
    private static final String UPGRADE_PATTERN_DATA_PARAMETER = "upgradePatternData";
    private static final String DEFAULT_UPGRADE_PATTERN_DATA_PATH = "upgrade/upgradePattern.yaml";

    public static List<EvnfmCnf> loadLcmConfigData(ITestContext iTestContext, String configEntityName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, configEntityName, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfsToInstantiate;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_PATTERN_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_UPGRADE_PATTERN_DATA_PATH : dataFilename;
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_INSTANTIATE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[][] getCombinedData(ITestContext iTestContext) throws IOException {
        Object[] cnfToUpgrade = loadLcmConfigData(iTestContext, CNF_TO_UPGRADE).toArray();
        Object[] cnfToRollback = loadLcmConfigData(iTestContext, CNF_TO_ROLLBACK).toArray();

        Object[][] combinedData = new Object[cnfToUpgrade.length][2];

        for (int i = 0; i < cnfToUpgrade.length; i++) {
            combinedData[i][0] = cnfToUpgrade[i];
            combinedData[i][1] = cnfToRollback[i];
        }

        return combinedData;
    }
}
