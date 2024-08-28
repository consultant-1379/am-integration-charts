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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class PersistenceOfScalingDataProvider {

    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION = "additionalCnfDataForScaleOutAspect1Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION = "additionalCnfDataForScaleOutAspect2Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_3_OUT_OPERATION = "additionalCnfDataForScaleOutAspect3Operation";
    public static final String SCALE_CONFIG_ASPECT_1_OUT = "scaleConfigAspect1OutRequest";
    public static final String SCALE_CONFIG_ASPECT_2_OUT = "scaleConfigAspect2OutRequest";
    public static final String SCALE_CONFIG_ASPECT_3_OUT = "scaleConfigAspect3OutRequest";
    public static final String PERSIST_SCALE_INFO_PARAM = "persistScaleInfo";
    private static final String DATA_PARAMETER_NAME = "persistenceOfScalingData";
    private static final String DATA_PARAMETER_PATH_VALUE = "scale/persistScaleAtUpgrade.yaml";

    public static EvnfmCnf loadAdditionalConfigData(ITestContext iTestContext, String key) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf evnfmCnf = loadYamlConfiguration(dataFilename, key, EvnfmCnf.class);
        addFlowSuffix(evnfmCnf);
        setTargets(evnfmCnf);
        return evnfmCnf;
    }

    public static ScaleVnfRequest loadScaleRequestConfigData(ITestContext iTestContext, String key) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, key, ScaleVnfRequest.class);
    }

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

    public static EvnfmCnf loadRollbackConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToRollback = loadYamlConfiguration(dataFilename, CNF_TO_ROLLBACK, EvnfmCnf.class);
        addFlowSuffix(cnfToRollback);
        return cnfToRollback;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(DATA_PARAMETER_NAME);
        return Objects.isNull(dataFilename) ? DATA_PARAMETER_PATH_VALUE : dataFilename;
    }
}
