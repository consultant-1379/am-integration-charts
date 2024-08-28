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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_ROLLBACK;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_SCALE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class AdminScaleDataProviders {
    private static final String ADMIN_SCALE_DATA_PARAMETER = "adminScaleData";
    private static final String DEFAULT_ADMIN_SCALE_DATA_PATH = "adminScale/adminScale.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        addFlowSuffix(cnfToInstantiate);
        setTargets(cnfToInstantiate);

        return cnfToInstantiate;
    }

    public static EvnfmCnf loadRollbackConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToRollback = loadYamlConfiguration(dataFilename, CNF_TO_ROLLBACK, EvnfmCnf.class);
        addFlowSuffix(cnfToRollback);
        setTargets(cnfToRollback);

        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put("data_conversion_identifier", "rollback-value");
        cnfToRollback.setAdditionalParams(additionalParams);

        return cnfToRollback;
    }

    public static EvnfmCnf loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, EvnfmCnf.class);
        addFlowSuffix(cnfToUpgrade);
        setTargets(cnfToUpgrade);

        return cnfToUpgrade;
    }

    public static EvnfmCnf loadScaleConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToScale = loadYamlConfiguration(dataFilename, CNF_TO_SCALE, EvnfmCnf.class);
        addFlowSuffix(cnfToScale);
        setTargets(cnfToScale);

        return cnfToScale;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(ADMIN_SCALE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_ADMIN_SCALE_DATA_PATH : dataFilename;
    }
}
