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

public class PersistenceOfScalingUIDataProviders {

    public static final String PERSISTENCE_SCALE_OPERATION_DATA = "persistenceOfScalingUIData";
    public static final String DEFAULT_PERSISTENCE_SCALE_DATA_PATH = "scale/persistenceOfScalingUI.yaml";

    public static EvnfmCnf loadConfigData(ITestContext iTestContext, String cnfTo) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfProperties = loadYamlConfiguration(dataFilename, cnfTo, EvnfmCnf.class);

        addFlowSuffix(cnfProperties);
        setTargets(cnfProperties);

        return cnfProperties;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(PERSISTENCE_SCALE_OPERATION_DATA);
        return Objects.isNull(dataFilename) ? DEFAULT_PERSISTENCE_SCALE_DATA_PATH : dataFilename;
    }

    public static ScaleVnfRequest loadScaleVnfRequest(ITestContext iTestContext, String scaleConfigName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, scaleConfigName, ScaleVnfRequest.class);
    }
}