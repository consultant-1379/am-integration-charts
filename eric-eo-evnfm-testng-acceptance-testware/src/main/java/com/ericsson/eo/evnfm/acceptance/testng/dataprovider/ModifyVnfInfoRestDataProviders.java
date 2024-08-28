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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_CREATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_MODIFY;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class ModifyVnfInfoRestDataProviders {
    public static final String MODIFY_VNF_INFO_REST_DATA_PARAMETER = "modifyVnfInfoRestData";
    public static final String DEFAULT_MODIFY_VNF_INFO_REST_DATA_PATH = "modifyVnfInfo/modifyVnfInfoRest.yaml";
    public static final String CNF_TO_MODIFY_ALL_ASPECTS_AFTER_INSTANTIATE = "cnfToModifyAllAspectsAfterInstantiate";
    public static final String CNF_TO_MODIFY_ONE_ASPECT_AFTER_INSTANTIATE = "cnfToModifyOneAspectAfterInstantiate";
    public static final String CNF_TO_MODIFY_DESCRIPTION_AFTER_INSTANTIATE = "cnfToModifyDescriptionAfterInstantiate";

    @DataProvider(parallel = true)
    public static Object[] loadCreateVnfConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToCreate = loadYamlConfiguration(dataFilename, CNF_TO_CREATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToCreate);

        return cnfToCreate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadModifyInfoBeforeInstantiateData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToModify = loadYamlConfiguration(dataFilename, CNF_TO_MODIFY, new TypeReference<>() {
        });
        addFlowSuffix(cnfToModify);
        cnfToModify.forEach(TestExecutionGlobalConfig::addFlowSuffixToModifiedName);

        return cnfToModify.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadModifyAllAspectsAfterInstantiateData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToModify = loadYamlConfiguration(dataFilename, CNF_TO_MODIFY_ALL_ASPECTS_AFTER_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToModify);

        return cnfToModify.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadModifyOneAspectAfterInstantiateData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToModify = loadYamlConfiguration(dataFilename, CNF_TO_MODIFY_ONE_ASPECT_AFTER_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToModify);

        return cnfToModify.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] loadModifyDescriptionAfterInstantiateData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfToModify = loadYamlConfiguration(dataFilename, CNF_TO_MODIFY_DESCRIPTION_AFTER_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToModify);

        return cnfToModify.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(MODIFY_VNF_INFO_REST_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_MODIFY_VNF_INFO_REST_DATA_PATH : dataFilename;
    }
}
