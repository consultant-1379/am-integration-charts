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

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_TERMINATE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

public class InstantiateInTheSameNamespaceTestDataProviders {

    private static final String INSTANTIATE_SAME_NAMESPACE_PARAMETER = "instantiateSameNamespace";
    private static final String INSTANTIATE_SAME_NAMESPACE_DATA_PATH = "instantiate/instantiateSameNamespace.yaml";

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        return loadInstantiateConfigData(iTestContext).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToTerminate(ITestContext iTestContext) throws IOException {
        return loadTerminateConfigData(iTestContext).toArray();
    }

    public static List<EvnfmCnf> loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToInstantiate;
    }

    public static List<EvnfmCnf> loadTerminateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToTerminate = loadYamlConfiguration(dataFilename, CNF_TO_TERMINATE, new TypeReference<>() {
        });
        cnfsToTerminate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToTerminate;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(INSTANTIATE_SAME_NAMESPACE_PARAMETER);
        return Objects.isNull(dataFilename) ? INSTANTIATE_SAME_NAMESPACE_DATA_PATH : dataFilename;
    }
}
