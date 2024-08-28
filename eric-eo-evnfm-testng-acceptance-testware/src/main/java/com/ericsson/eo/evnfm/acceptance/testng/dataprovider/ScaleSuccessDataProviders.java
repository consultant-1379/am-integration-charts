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
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class ScaleSuccessDataProviders {
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_FIRST = "additionalCnfDataForScaleOutAspect1OperationFirst";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION = "additionalCnfDataForScaleInAspect1Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_SECOND = "additionalCnfDataForScaleOutAspect1OperationSecond";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION = "additionalCnfDataForScaleOutAspect2Operation";
    public static final String ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION = "additionalCnfDataForScaleInAspect2Operation";
    public static final String SCALE_CONFIG_ASPECT_1_OUT_REQUEST_FIRST = "scaleConfigAspect1OutRequestFirst";
    public static final String SCALE_CONFIG_ASPECT_1_IN_REQUEST = "scaleConfigAspect1InRequest";
    public static final String SCALE_CONFIG_ASPECT_1_OUT_REQUEST_SECOND = "scaleConfigAspect1OutRequestSecond";
    public static final String SCALE_CONFIG_ASPECT_2_OUT_REQUEST = "scaleConfigAspect2OutRequest";
    public static final String SCALE_CONFIG_ASPECT_2_IN_REQUEST = "scaleConfigAspect2InRequest";
    public static final String HELM_HISTORY_FOR_FIRST_SCALE_OPERATION = "helmHistoryForFirstScaleOperation";
    public static final String HELM_HISTORY_FOR_SECOND_SCALE_OPERATION = "helmHistoryForSecondScaleOperation";
    public static final String HELM_HISTORY_FOR_THIRD_SCALE_OPERATION = "helmHistoryForThirdScaleOperation";
    public static final String HELM_HISTORY_FOR_FOURTH_SCALE_OPERATION = "helmHistoryForFourthScaleOperation";
    public static final String HELM_HISTORY_FOR_FIFTH_SCALE_OPERATION = "helmHistoryForFifthScaleOperation";
    private static final String CNF_SCALE_DATA_PARAMETER = "cnfScaleData";
    private static final String DEFAULT_SCALE_DATA_PATH = "scale/scaleCnf.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        EvnfmCnf cnfToInstantiate = retrieveEntityFromConfig(iTestContext, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        addFlowSuffix(cnfToInstantiate);
        return cnfToInstantiate;
    }

    public static EvnfmCnf loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        EvnfmCnf cnfToUpgrade = retrieveEntityFromConfig(iTestContext, CNF_TO_UPGRADE, EvnfmCnf.class);
        addFlowSuffix(cnfToUpgrade);
        return cnfToUpgrade;
    }

    public static EvnfmCnf loadAdditionalConfigData(ITestContext iTestContext, final String configKey) throws IOException {
        EvnfmCnf additionalCnf = retrieveEntityFromConfig(iTestContext, configKey, EvnfmCnf.class);
        addFlowSuffix(additionalCnf);
        setTargets(additionalCnf);
        return additionalCnf;
    }

    public static ScaleVnfRequest loadScaleRequestConfigData(ITestContext iTestContext, String key) throws IOException {
        return retrieveEntityFromConfig(iTestContext, key, ScaleVnfRequest.class);
    }

    public static EvnfmCnf loadHelmHistoryConfigData(ITestContext iTestContext, String key) throws IOException {
        return retrieveEntityFromConfig(iTestContext, key, EvnfmCnf.class);
    }

    private static <T> T retrieveEntityFromConfig(final ITestContext iTestContext, final String configKey, Class<T> entityType) throws IOException {
        String dataFilename = resolveConfig(iTestContext);

        return loadYamlConfiguration(dataFilename, configKey, entityType);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CNF_SCALE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_SCALE_DATA_PATH : dataFilename;
    }
}