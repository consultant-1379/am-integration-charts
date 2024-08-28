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
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class LcmWithValuesSchemaTestDataProviders {

    private static final String LCM_VALUES_SCHEMA_DATA_PARAMETER = "lcmValuesSchemaData";
    private static final String LCM_VALUES_SCHEMA_DATA_PATH = "lcm/lcmValuesSchema.yaml";

    @DataProvider
    public Object[] getInstancesToInstantiatePositive(ITestContext iTestContext) throws IOException {
        return new Object[] { loadInstantiateConfigData(iTestContext).get(0) };
    }

    @DataProvider
    public Object[] getInstancesToInstantiateNegative(ITestContext iTestContext) throws IOException {
        return new Object[] { loadInstantiateConfigData(iTestContext).get(1) };
    }

    public static List<EvnfmCnf> loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToInstantiate;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(LCM_VALUES_SCHEMA_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? LCM_VALUES_SCHEMA_DATA_PATH : dataFilename;
    }
}
