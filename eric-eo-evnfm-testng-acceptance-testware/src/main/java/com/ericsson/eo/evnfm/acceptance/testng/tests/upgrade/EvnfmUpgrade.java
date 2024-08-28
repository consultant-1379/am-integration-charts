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
package com.ericsson.eo.evnfm.acceptance.testng.tests.upgrade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.steps.evnfm.rest.EvnfmLcmSteps.addHelmRepo;
import static com.ericsson.evnfm.acceptance.steps.evnfm.rest.EvnfmLcmSteps.installEvnfm;
import static com.ericsson.evnfm.acceptance.steps.evnfm.rest.EvnfmLcmSteps.updateHelmRepo;
import static com.ericsson.evnfm.acceptance.steps.evnfm.rest.EvnfmLcmSteps.upgradeEvnfm;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ericsson.evnfm.acceptance.models.configuration.testng.ConfigEvnfmDeployment;
import com.ericsson.evnfm.acceptance.steps.evnfm.rest.EvnfmLcmSteps;

public class EvnfmUpgrade {
    private static final Logger LOGGER = getLogger(EvnfmUpgrade.class);
    public static final String EVNFM_DEPLOY_PARAMS = "evnfmDeployParams";
    public static final String EVNFM_UPGRADE_KEY = "evnfmUpgrade";

    @BeforeSuite()
    public void setup(ITestContext iTestContext) throws IOException {
        ConfigEvnfmDeployment configEvnfmDeployment = getConfigEvnfmDeployment(iTestContext);
        validateEvnfmDeployParams(configEvnfmDeployment);
        addHelmRepo(configEvnfmDeployment);
        updateHelmRepo(configEvnfmDeployment);
        iTestContext.setAttribute("configEvnfmDeployment", configEvnfmDeployment);
    }

    @Test(description = "Install EVNFM")
    public void install(ITestContext iTestContext) throws IOException {
        installEvnfm(getConfigEvnfmDeployment(iTestContext));
    }

    private ConfigEvnfmDeployment getConfigEvnfmDeployment(final ITestContext iTestContext) throws IOException {
        String evnfmDeployParams = iTestContext.getCurrentXmlTest().getParameter(EVNFM_DEPLOY_PARAMS);
        return loadYamlConfiguration(evnfmDeployParams, EVNFM_UPGRADE_KEY, ConfigEvnfmDeployment.class);
    }

    @Test(description = "Upgrade EVNFM")
    public void upgrade(ITestContext iTestContext) throws IOException {
        upgradeEvnfm(getConfigEvnfmDeployment(iTestContext));
    }

    @AfterSuite(description = "Uninstall EVNFM")
    public void uninstall(ITestContext iTestContext) throws IOException {
        EvnfmLcmSteps.uninstallEvnfm(getConfigEvnfmDeployment(iTestContext), false);
    }

    private static void validateEvnfmDeployParams(final ConfigEvnfmDeployment configEvnfmDeployment) {
        assertThat(configEvnfmDeployment.getChart()).withFailMessage("EVNFM install chart path cannot be null or empty").isNotNull();
        assertThat(configEvnfmDeployment.getInstall().getValuesFile())
                .withFailMessage("EVNFM install values file path cannot be null or empty").isNotNull();
        assertThat(configEvnfmDeployment.getChart()).withFailMessage("EVNFM upgrade chart path cannot be null or empty").isNotNull();
        assertThat(configEvnfmDeployment.getUpgrade().getValuesFile())
                .withFailMessage("EVNFM upgrade values file path cannot be null or empty").isNotNull();

        LOGGER.info("Helm repo name is :: {}", configEvnfmDeployment.getHelmRepoName());
        LOGGER.info("Helm repo url is :: {}", configEvnfmDeployment.getHelmRepoUrl());
        LOGGER.info("Namespace will be used for Evnfm upgrade tests :: {}", configEvnfmDeployment.getNamespace());
        LOGGER.info("Cluster will be used for Evnfm upgrade tests :: {}", configEvnfmDeployment.getCluster().getName());
        LOGGER.info("Release name will be used for Evnfm upgrade tests :: {}", configEvnfmDeployment.getReleaseName());
        LOGGER.info("EVNFM install/upgrade chart path :: {}", configEvnfmDeployment.getChart());
        String installVersion = configEvnfmDeployment.getInstall().getChartVersion().isEmpty() ?
                "latest" :
                configEvnfmDeployment.getInstall().getChartVersion();
        LOGGER.info("EVNFM install version :: {}", installVersion);
        LOGGER.info("EVNFM install values file path :: {}", configEvnfmDeployment.getInstall().getValuesFile());
        String upgradeVersion = StringUtils.isEmpty(configEvnfmDeployment.getUpgrade().getChartVersion()) ?
                "latest" :
                configEvnfmDeployment.getUpgrade().getChartVersion();
        LOGGER.info("EVNFM upgrade version :: {}", upgradeVersion);
        LOGGER.info("EVNFM upgrade values file path :: {}", configEvnfmDeployment.getUpgrade().getValuesFile());
    }
}
