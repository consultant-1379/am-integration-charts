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
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_SECOND_UPGRADE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.fasterxml.jackson.core.type.TypeReference;

public class UpgradeTwiceWithoutDowngradePolicyDataProvider {
    private static final String UPGRADE_TWICE_VALUES_DATA = "upgradeTwiceWithoutDowngradePolicyData";
    private static final String UPGRADE_TWICE_YAML = "upgrade/upgrade-twice-without-downgrade-policy.yaml";

    public static List<EvnfmCnf> loadLcmConfigData(final ITestContext iTestContext, final String configEntityName) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnf = loadYamlConfiguration(dataFilename, configEntityName, EvnfmCnf.class);
        addFlowSuffix(cnf);

        return Collections.singletonList(cnf);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(UPGRADE_TWICE_VALUES_DATA);
        return Objects.isNull(dataFilename) ? UPGRADE_TWICE_YAML : dataFilename;
    }

    @DataProvider(parallel = true)
    public Object[] getInstanceToInstantiate(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_INSTANTIATE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstanceToUpgrade(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_UPGRADE).toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstanceToSecondUpgrade(ITestContext iTestContext) throws IOException {
        return loadLcmConfigData(iTestContext, CNF_TO_SECOND_UPGRADE).toArray();
    }
}
