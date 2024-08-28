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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.EXPECTED_ERRORS;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.type.TypeReference;

public class EVNFMReleaseFlowDataProviders {
    private static final String INSTANTIATE_DATA_PARAMETER = "instantiateData";
    private static final String DEFAULT_CONFIG_DATA_PATH_PHASE_1 = "phases/phase1Instantiate.yaml";
    private static final String UPGRADE_DATA_PARAMETER = "upgradeData";
    private static final String DEFAULT_CONFIG_DATA_PATH_PHASE_2 = "phases/phase2Upgrade.yaml";
    private static final String UNAUTHORIZED = "Unauthorized";

    @DataProvider
    public static Object[] loadInstantiateConfigDataProvider(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigForPhase1(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate.toArray();
    }

    @DataProvider
    public static Object[][] loadInstantiateConfigDataProviderExpectingError(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigForPhase1(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });

        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {});

        ProblemDetails expectedError = expectedErrors.get(UNAUTHORIZED);
        expectedError.setDetail(String.format(expectedError.getDetail(), "spider-app-multi-a-v2"));

        addFlowSuffix(cnfToInstantiate);
        return new Object[][] {{ cnfToInstantiate.get(0), expectedError }};
    }

    @DataProvider
    public static Object[] loadUpgradeConfigDataProvider(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigForPhase2(iTestContext);
        List<EvnfmCnf> cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToUpgrade);

        return cnfToUpgrade.toArray();
    }

    public static List<EvnfmCnf> loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigForPhase1(iTestContext);
        List<EvnfmCnf> cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate;
    }

    public static List<EvnfmCnf> loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfigForPhase2(iTestContext);
        List<EvnfmCnf> cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, new TypeReference<>() {
        });
        addFlowSuffix(cnfToUpgrade);

        return cnfToUpgrade;
    }

    private static String resolveConfigForPhase1(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(INSTANTIATE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_CONFIG_DATA_PATH_PHASE_1 : dataFilename;
    }

    private static String resolveConfigForPhase2(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_CONFIG_DATA_PATH_PHASE_2 : dataFilename;
    }
}
