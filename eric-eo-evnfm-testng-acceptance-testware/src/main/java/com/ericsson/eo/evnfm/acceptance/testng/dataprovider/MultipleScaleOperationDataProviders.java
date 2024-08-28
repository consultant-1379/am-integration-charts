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

import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class MultipleScaleOperationDataProviders {

    public static final String MULTIPLE_SCALE_OPERATION_DATA = "multipleScaleOperationData";
    public static final String SCALE_CONFIG_ASPECT1_OUT_REQUEST = "scaleConfigAspect1OutRequest";
    public static final String SCALE_CONFIG_ASPECT1_IN_REQUEST = "scaleConfigAspect1InRequest";
    public static final String SCALE_CONFIG_ASPECT5_OUT_REQUEST = "scaleConfigAspect5OutRequest";
    public static final String DEFAULT_SCALE_DATA_PATH = "multipleScaleOperation/multipleScaleOperationTest.yaml";

    public static EvnfmCnf loadConfigData(ITestContext iTestContext, String cnfTo) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfProperties = loadYamlConfiguration(dataFilename, cnfTo, EvnfmCnf.class);

        addFlowSuffix(cnfProperties);
        setTargets(cnfProperties);
        return cnfProperties;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(MULTIPLE_SCALE_OPERATION_DATA);
        return Objects.isNull(dataFilename) ? DEFAULT_SCALE_DATA_PATH : dataFilename;
    }

    public static ScaleVnfRequest loadScaleVnfRequest(ITestContext iTestContext, String scaleConfigName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        ScaleVnfRequest scaleVnfRequest = loadYamlConfiguration(dataFilename, scaleConfigName, ScaleVnfRequest.class);
        return scaleVnfRequest;
    }
}