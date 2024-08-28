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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class RollbackAtFailureDataProviders {
    private static final String ROLLBACK_AT_FAILURE_DATA_PARAMETER = "rollbackAtFailureData";
    private static final String DEFAULT_ROLLBACK_AT_FAILURE_DATA_PATH = "rollback/rollbackAtFailure.yaml";

    @DataProvider(parallel = true)
    public static Object[] loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);

        return cnfToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, new TypeReference<>() {
        });
        cnfToUpgrade.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfToUpgrade.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadRollbackConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToRollback = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfToRollback.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfToRollback.forEach(TestExecutionGlobalConfig::setExpectedHelmHistory);
        cnfToRollback.forEach(cnf -> cnf.setExpectedOperationState("ROLLED_BACK"));
        return cnfToRollback.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(ROLLBACK_AT_FAILURE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_ROLLBACK_AT_FAILURE_DATA_PATH : dataFilename;
    }
}
