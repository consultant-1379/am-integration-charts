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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.fasterxml.jackson.core.type.TypeReference;

public class LcmWithNonScalableVdusTestDataProviders {

    private static final String LCM_NON_SCALABLE_VDUS_DATA_PARAMETER = "lcmNonScalableVdusData";
    private static final String LCM_NON_SCALABLE_VDUS_DATA_PATH = "lcm/lcmNonScalableVdus.yaml";
    private static final String SCALE_CONFIG_ASPECT_1_REQUEST = "scaleConfigAspect1Request";

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadInstantiateConfigData(iTestContext).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToUpgrade(ITestContext iTestContext) throws IOException {
        return loadUpgradeConfigData(iTestContext).toArray();
    }

    @DataProvider
    public Object[] getInstancesToScale(ITestContext iTestContext) throws IOException {
        return loadScaleConfigData(iTestContext).toArray();
    }

    public static List<EvnfmCnf> loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfsToInstantiate;
    }

    public static List<EvnfmCnf> loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToTerminate = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, new TypeReference<>() {
        });
        cnfsToTerminate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToTerminate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfsToTerminate;
    }

    public static List<EvnfmCnf> loadScaleConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToTerminate = loadYamlConfiguration(dataFilename, CNF_TO_SCALE, new TypeReference<>() {
        });
        cnfsToTerminate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToTerminate;
    }

    public static ScaleVnfRequest loadScaleRequestConfigData(ITestContext iTestContext) throws IOException {
        return retrieveEntityFromConfig(iTestContext, SCALE_CONFIG_ASPECT_1_REQUEST, ScaleVnfRequest.class);
    }

    private static <T> T retrieveEntityFromConfig(final ITestContext iTestContext, final String configKey, Class<T> entityType) throws IOException {
        String dataFilename = resolveConfig(iTestContext);

        return loadYamlConfiguration(dataFilename, configKey, entityType);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(LCM_NON_SCALABLE_VDUS_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? LCM_NON_SCALABLE_VDUS_DATA_PATH : dataFilename;
    }
}
