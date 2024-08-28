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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_ADD_TO_ENM;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_DELETE_FROM_ENM_NOT_ACCESSIBLE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_DELETE_FROM_ENM_SECOND_TIME;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.evnfm.acceptance.steps.common.CommonActions.getNodeIpAddress;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class AddDeleteNodeDataProviders {

    private static final String ADD_DELETE_NODE_DATA_PARAMETER = "addDeleteNode";
    private static final String ADD_DELETE_NODE_DATA_PATH = "enm/addDeleteNode.yaml";

    @DataProvider(parallel = true)
    public Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        String nodeIp = getNodeIpAddress(cnfsToInstantiate.get(0).getCluster().getLocalPath());
        cnfsToInstantiate.forEach(cnfToInstantiate -> cnfToInstantiate.getAdditionalParams()
                .computeIfPresent("ossTopology.nodeIpAddress",
                                  (key, value) -> value.toString().replace("<node-ip>", nodeIp)));
        cnfsToInstantiate.forEach(cnfToInstantiate -> cnfToInstantiate.getInstantiateOssTopology()
                .computeIfPresent("nodeIpAddress",
                                  (key, value) -> value.replace("<node-ip>", nodeIp)));
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToAddToEnm(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToAddToEnm = loadYamlConfiguration(dataFilename, CNF_TO_ADD_TO_ENM, new TypeReference<>() {
        });
        cnfsToAddToEnm.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToAddToEnm.toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToDeleteFromEnmSecondTime(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToAddToEnm = loadYamlConfiguration(dataFilename, CNF_TO_DELETE_FROM_ENM_SECOND_TIME, new TypeReference<>() {
        });
        cnfsToAddToEnm.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToAddToEnm.toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToDeleteFromEnmNotAccessible(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToAddToEnm = loadYamlConfiguration(dataFilename, CNF_TO_DELETE_FROM_ENM_NOT_ACCESSIBLE, new TypeReference<>() {
        });
        cnfsToAddToEnm.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        return cnfsToAddToEnm.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(ADD_DELETE_NODE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? ADD_DELETE_NODE_DATA_PATH : dataFilename;
    }
}


