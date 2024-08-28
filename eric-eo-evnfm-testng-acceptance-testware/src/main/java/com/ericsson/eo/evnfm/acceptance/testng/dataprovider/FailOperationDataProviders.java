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
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class FailOperationDataProviders {
    private static final String FAIL_OPERATION_DATA_PARAMETER = "failOperationData";
    private static final String DEFAULT_FAIL_OPERATION_DATA_PATH = "failOperation/failOperation.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate;
    }

    public static EvnfmCnf loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, EvnfmCnf.class);
        addFlowSuffix(cnfToUpgrade);

        return cnfToUpgrade;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(FAIL_OPERATION_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_FAIL_OPERATION_DATA_PATH : dataFilename;
    }
}
