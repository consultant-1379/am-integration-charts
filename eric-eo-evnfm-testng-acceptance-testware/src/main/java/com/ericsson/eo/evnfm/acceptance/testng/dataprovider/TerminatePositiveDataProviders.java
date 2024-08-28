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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_TERMINATE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.fasterxml.jackson.core.type.TypeReference;

public class TerminatePositiveDataProviders {

    private static final String TERMINATE_DATA_PARAMETER = "terminateData";
    private static final String DEFAULT_TERMINATE_DATA_PATH = "terminate/terminate.yaml";

    @DataProvider(parallel = true)
    public static Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfsToInstantiate);

        return cnfsToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] getInstancesToTerminate(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_TERMINATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfsToInstantiate);

        return cnfsToInstantiate.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(TERMINATE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_TERMINATE_DATA_PATH : dataFilename;
    }
}
