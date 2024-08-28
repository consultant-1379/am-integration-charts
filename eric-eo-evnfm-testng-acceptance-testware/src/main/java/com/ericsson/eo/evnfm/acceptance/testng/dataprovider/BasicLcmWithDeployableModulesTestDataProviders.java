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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_MODIFY;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_ROLLBACK;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_SYNC;
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

public class BasicLcmWithDeployableModulesTestDataProviders {

    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION = "additionalCnfDataForScaleOutAspect1Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION = "additionalCnfDataForScaleInAspect1Operation";
    public static final String SCALE_CONFIG_ASPECT_1_OUT_REQUEST = "scaleConfigAspect1OutRequest";
    public static final String SCALE_CONFIG_ASPECT_1_IN_REQUEST = "scaleConfigAspect1InRequest";
    public static final String HELM_HISTORY_FOR_FIRST_SCALE_OPERATION = "helmHistoryForFirstScaleOperation";
    public static final String HELM_HISTORY_FOR_SECOND_SCALE_OPERATION = "helmHistoryForSecondScaleOperation";

    private static final String BASIC_LCM_DATA_PARAMETER = "basicLcmDeployableModulesData";
    private static final String DEFAULT_BASIC_LCM_DATA_PATH = "instantiate/basic-lcm-dm.yaml";

    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION = "additionalCnfDataForScaleOutAspect2Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION = "additionalCnfDataForScaleInAspect2Operation";
    public static final String SCALE_CONFIG_ASPECT_2_OUT_REQUEST = "scaleConfigAspect2OutRequest";
    public static final String SCALE_CONFIG_ASPECT_2_IN_REQUEST = "scaleConfigAspect2InRequest";

    public static List<EvnfmCnf> loadLcmConfigData(ITestContext iTestContext, String configEntityName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, configEntityName, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::setTargets);
        return cnfsToInstantiate;
    }

    public static <T> T loadData(ITestContext iTestContext, String configEntityName, Class<T> entityClass) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, configEntityName, entityClass);
    }

    @DataProvider
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_INSTANTIATE).toArray();
    }

    @DataProvider
    public Object[][] getInstancesToUpgrade(ITestContext iTestContext) throws IOException {
        final List<EvnfmCnf> cnfsToUpgrade = loadLcmConfigData(iTestContext, CNF_TO_UPGRADE);
        return new Object[][] { { cnfsToUpgrade.get(0), cnfsToUpgrade.get(1) } };
    }

    @DataProvider
    public Object[] getInstancesToScale(ITestContext iTestContext) throws IOException {
        return new Object[] { loadLcmConfigData(iTestContext, CNF_TO_UPGRADE).get(1) };
    }

    @DataProvider
    public Object[][] getInstancesToModify(ITestContext iTestContext) throws IOException {
        return new Object[][] { loadLcmConfigData(iTestContext, CNF_TO_MODIFY).toArray() };
    }

    @DataProvider
    public Object[] getInstancesToSync(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_SYNC).toArray();
    }

    @DataProvider
    public Object[] getInstancesToHeal(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_HEAL).toArray();
    }

    @DataProvider
    public Object[] getInstancesToUpgradeWithPersistDMconfig(ITestContext iTestContext) throws IOException {
        return new Object[] { loadLcmConfigData(iTestContext, CNF_TO_UPGRADE).get(2) };
    }

    @DataProvider
    public Object[] getInstancesToRollback(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_ROLLBACK).toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BASIC_LCM_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BASIC_LCM_DATA_PATH : dataFilename;
    }

    private static <T> T loadData(ITestContext iTestContext, String configEntityName, TypeReference<T> typeReference) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, configEntityName, typeReference);
    }
}
