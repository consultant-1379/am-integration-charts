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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_HEAL;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class UpgradeWithHealDataProvider {


    private static final String UPGRADE_WITH_HEAL_DATA = "upgradeWithHealData";
    private static final String UPGRADE_WITH_HEAL_YAML = "upgrade/skipMergingPreviousValues/upgrade-with-heal.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        addFlowSuffix(cnfToInstantiate);

        return cnfToInstantiate;
    }

    public static EvnfmCnf loadUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, EvnfmCnf.class);
        addFlowSuffix(cnfToUpgrade);

        return cnfToUpgrade;
    }

    public static EvnfmCnf loadHealConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToHeal = loadYamlConfiguration(dataFilename, CNF_TO_HEAL, EvnfmCnf.class);
        addFlowSuffix(cnfToHeal);

        return cnfToHeal;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_WITH_HEAL_DATA);
        return Objects.isNull(dataFilename) ? UPGRADE_WITH_HEAL_YAML : dataFilename;
    }
}
