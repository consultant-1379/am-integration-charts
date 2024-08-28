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
package com.ericsson.evnfm.acceptance.steps.clusterconfigs.ui;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.UI.page.CISMClustersPage;
import com.ericsson.evnfm.acceptance.models.UI.page.DeregisterClusterDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.RegisterClusterDialog;
import com.ericsson.evnfm.acceptance.steps.common.ui.Navigation;

public class ClusterConfigUISteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterConfigUISteps.class);

    public void registerClusterUI(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        LOGGER.info("Opening Register Cluster Dialog from the CISM Clusters page");
        String rootURL = EVNFM_INSTANCE.getEvnfmUrl() + "/vnfm#clusters";
        Navigation.goTo(driver, rootURL);
        CISMClustersPage cismClustersPage = new CISMClustersPage();

        cismClustersPage.verifyCISMClustersPageIsOpened(driver);
        RegisterClusterDialog registerClusterDialog = cismClustersPage.openRegisterClusterDialog(driver);
        cismClustersPage = registerClusterDialog.uploadFile(driver, clusterConfig);
        cismClustersPage.verifyCISMClusterIsRegistered(driver, clusterConfig);
    }

    public void deregisterClusterUI(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        LOGGER.info("Opening Register Cluster Dialog from the CISM Clusters page");
        String rootURL = EVNFM_INSTANCE.getEvnfmUrl() + "/vnfm#clusters";
        Navigation.goTo(driver, rootURL);
        CISMClustersPage cismClustersPage = new CISMClustersPage();
        DeregisterClusterDialog deregisterClusterDialog = cismClustersPage.openDeregisterClusterDialog(driver, clusterConfig);
        cismClustersPage = deregisterClusterDialog.deregisterCluster(driver);
        cismClustersPage.verifyCISMClusterIsNotRegistered(driver, clusterConfig);
    }
}
