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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_MODIFY;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class ModifyVnfInfoUIDataProviders {
    public static final String MODIFY_VNF_INFO_UI_DATA_PARAMETER = "modifyVnfInfoUIData";
    public static final String DEFAULT_MODIFY_VNF_INFO_UI_DATA_PATH = "modifyVnfInfo/modifyVnfInfoUI.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate;
    }

    public static EvnfmCnf loadModifyVnfInfoConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToModify = loadYamlConfiguration(dataFilename, CNF_TO_MODIFY, EvnfmCnf.class);
        addFlowSuffix(cnfToModify);

        return cnfToModify;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(MODIFY_VNF_INFO_UI_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_MODIFY_VNF_INFO_UI_DATA_PATH : dataFilename;
    }
}
