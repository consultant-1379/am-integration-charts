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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_HEAL;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_SCALE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_TERMINATE;
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

public class InstantiateNegativeTestDataProviders {
    private static final String INSTANTIATE_NEGATIVE_DATA_PARAMETER = "instantiateNegativeData";
    private static final String INSTANTIATE_NEGATIVE_DATA_PATH = "instantiate/instantiateNegative.yaml";
    private static final String SCALE_CONFIG_ASPECT_1_REQUEST = "scaleConfigAspect1Request";

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadConfigData(iTestContext, CNF_TO_INSTANTIATE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToUpgrade(ITestContext iTestContext) throws IOException {
        return loadConfigData(iTestContext, CNF_TO_UPGRADE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToScale(ITestContext iTestContext) throws IOException {
        return loadConfigData(iTestContext, CNF_TO_SCALE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToHeal(ITestContext iTestContext) throws IOException {
        return loadConfigData(iTestContext, CNF_TO_HEAL).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToTerminate(ITestContext iTestContext) throws IOException {
        return loadConfigData(iTestContext, CNF_TO_TERMINATE).toArray();
    }

    public static ScaleVnfRequest loadScaleRequestConfigData(ITestContext iTestContext) throws IOException {
        return retrieveEntityFromConfig(iTestContext, SCALE_CONFIG_ASPECT_1_REQUEST, ScaleVnfRequest.class);
    }

    private static List<EvnfmCnf> loadConfigData(ITestContext iTestContext, final String key) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, key, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToInstantiate;
    }

    private static <T> T retrieveEntityFromConfig(final ITestContext iTestContext, final String configKey, Class<T> entityType) throws IOException {
        String dataFilename = resolveConfig(iTestContext);

        return loadYamlConfiguration(dataFilename, configKey, entityType);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(INSTANTIATE_NEGATIVE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? INSTANTIATE_NEGATIVE_DATA_PATH : dataFilename;
    }
}
