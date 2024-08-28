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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.PackageUtils.loadPackageData;

import java.io.IOException;
import java.util.List;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.configuration.testng.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.testng.ConfigOnboarding;
import com.ericsson.evnfm.acceptance.models.configuration.testng.ConfigUpgrade;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class TCDataProviders {
    private static final String PACKAGES_TO_ONBOARD = "packagesToOnboard";
    private static final String CNFS_TO_INSTANTIATE = "cnfsToInstantiate";
    private static final String CNFS_TO_UPGRADE = "cnfsToUpgrade";
    private static final String CNFS_TO_SCALE = "cnfsToScale";
    private static final String PACKAGES_TO_ONBOARD_UI = "packagesToOnboardUI";

    static void loadPackagesData(List<EvnfmBasePackage> packages) throws IOException, InterruptedException {
        for (EvnfmBasePackage evnfmBasePackage : packages) {
            loadPackageData(evnfmBasePackage);
        }
    }

    public static List<EvnfmBasePackage> loadPackagesToOnboard(ITestContext iTestContext) throws IOException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(PACKAGES_TO_ONBOARD);
        return loadYamlConfiguration(dataFilename, PACKAGES, new TypeReference<>() {
        });
    }

    public static List<EvnfmCnf> loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CNFS_TO_INSTANTIATE);
        ConfigInstantiate configInstantiate = loadYamlConfiguration(dataFilename, ConfigInstantiate.class);
        List<EvnfmCnf> cnfsToInstantiate = configInstantiate.getCnfsToInstantiate();
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        iTestContext.setAttribute(CNFS_TO_INSTANTIATE, cnfsToInstantiate);
        return cnfsToInstantiate;
    }

    @DataProvider
    public Object[] getOnboardingConfig(ITestContext iTestContext) throws IOException, InterruptedException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(PACKAGES_TO_ONBOARD);
        ConfigOnboarding configOnboarding = loadYamlConfiguration(dataFilename, ConfigOnboarding.class);
        List<EvnfmBasePackage> packages = configOnboarding.getPackages();
        loadPackagesData(packages);
        iTestContext.setAttribute(PACKAGES_TO_ONBOARD, packages);
        return packages.toArray();
    }

    @DataProvider
    public Object[] getUIOnboardingConfig(ITestContext iTestContext) throws IOException, InterruptedException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(PACKAGES_TO_ONBOARD_UI);
        ConfigOnboarding configOnboarding = loadYamlConfiguration(dataFilename, ConfigOnboarding.class);
        List<EvnfmBasePackage> packages = configOnboarding.getPackages();
        loadPackagesData(packages);
        iTestContext.setAttribute(PACKAGES_TO_ONBOARD_UI, packages);
        return packages.toArray();
    }

    @DataProvider
    public Object[] getOnboardedPackages(ITestContext iTestContext) {
        List<EvnfmBasePackage> onboardedPackages = (List<EvnfmBasePackage>) iTestContext.getAttribute(PACKAGES_TO_ONBOARD);
        return onboardedPackages.toArray();
    }

    @DataProvider
    public Object[] getInstantiateConfig(ITestContext iTestContext) throws IOException {
        return loadInstantiateConfigData(iTestContext).toArray();
    }

    @DataProvider
    public Object[] getInstantiatedCnfs(ITestContext iTestContext) {
        List<EvnfmCnf> evnfmCnfs = (List<EvnfmCnf>) iTestContext.getAttribute(CNFS_TO_INSTANTIATE);
        return evnfmCnfs.toArray();
    }

    @DataProvider
    public Object[] getUpgradeConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CNFS_TO_UPGRADE);
        ConfigUpgrade configUpgrade = loadYamlConfiguration(dataFilename, ConfigUpgrade.class);
        List<EvnfmCnf> cnfsToUpgrade = configUpgrade.getCnfsToUpgrade();
        iTestContext.setAttribute(CNFS_TO_UPGRADE, cnfsToUpgrade);
        return cnfsToUpgrade.toArray();
    }

    @DataProvider
    public Object[] getUpgradedCnfs(ITestContext iTestContext) {
        List<EvnfmCnf> evnfmCnfs = (List<EvnfmCnf>) iTestContext.getAttribute(CNFS_TO_UPGRADE);
        return evnfmCnfs.toArray();
    }

    @DataProvider
    public Object[] getScaleConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CNFS_TO_SCALE);
        ConfigInstantiate configInstantiate = loadYamlConfiguration(dataFilename, ConfigInstantiate.class);
        List<EvnfmCnf> packages = configInstantiate.getCnfsToInstantiate();
        iTestContext.setAttribute(CNFS_TO_SCALE, packages);
        return packages.toArray();
    }

    @DataProvider
    public Object[] getTerminateConfig(ITestContext iTestContext) {
        List<EvnfmCnf> evnfmCnfs = (List<EvnfmCnf>) iTestContext.getAttribute(CNFS_TO_INSTANTIATE);
        return evnfmCnfs.stream().filter(cnf -> cnf.getExpectedComponentsState().equalsIgnoreCase("Completed")).toArray();
    }

    @DataProvider
    public Object[] getCleanupConfig(ITestContext iTestContext) {
        List<EvnfmCnf> evnfmCnfs = (List<EvnfmCnf>) iTestContext.getAttribute(CNFS_TO_INSTANTIATE);
        return evnfmCnfs.stream().filter(cnf -> cnf.getExpectedComponentsState().equalsIgnoreCase("Failed")).toArray();
    }
}
