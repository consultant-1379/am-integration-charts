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
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.setTargets;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class UpgradeTwiceWithDifferentFlagValuesDataProviders {

    private static final String UPGRADE_TWICE_VALUES_DATA = "upgradeTwiceWithDifferentFlagValuesData";
    private static final String UPGRADE_TWICE_YAML = "upgrade/skipMergingPreviousValues/upgrade-twice-with-different-flags.yaml";

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

    public static EvnfmCnf loadSecondUpgradeConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToUpgrade = loadYamlConfiguration(dataFilename, "cnfToSecondUpgrade", EvnfmCnf.class);
        addFlowSuffix(cnfToUpgrade);

        return cnfToUpgrade;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_TWICE_VALUES_DATA);
        return Objects.isNull(dataFilename) ? UPGRADE_TWICE_YAML : dataFilename;
    }
}
