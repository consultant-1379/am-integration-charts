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
package com.ericsson.evnfm.acceptance.models.UI.page;

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.ContextMenuActions.clickTableContextWithRetry;
import static com.ericsson.evnfm.acceptance.commonUIActions.TableActions.getTableRowByCISMClusterName;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.utils.Constants.CISM_CLUSTERS;
import static com.ericsson.evnfm.acceptance.utils.Constants.DEREGISTER_CLUSTER_MENU_ITEM;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;

public class CISMClustersPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(CISMClustersPage.class);
    private static final String REGISTER_CLUSTER_BUTTON_SELECTOR = "#appbar-component-container > eui-base-v0-button";

    private static final String RESOURCE_ROW_ID_SELECTOR = "e-custom-cell#";
    private static final String TABLE_ROW_ID_SELECTOR = "#%s";
    private static final String MENU_OPTION_SELECTOR = ".menu-option[value=\"%s\"]";

    public void verifyCISMClustersPageIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Check that CISM Clusters page is opened");
        checkCurrentPage(driver, CISM_CLUSTERS);
    }

    public RegisterClusterDialog openRegisterClusterDialog(RemoteWebDriver driver) {
        LOGGER.info("Open Register Cluster Dialog");
        clickOnTheButton(driver, REGISTER_CLUSTER_BUTTON_SELECTOR);
        return new RegisterClusterDialog();
    }

    public DeregisterClusterDialog openDeregisterClusterDialog(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        LOGGER.info("Opening CISM Clusters page and choosing cluster to deregister");
        String rowId = clusterConfig.getUIName();
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, rowId);
        LOGGER.debug("Clicking Deregister Cluster option");
        clickTableContextWithRetry(driver, rowId,
                                   String.format(TABLE_ROW_ID_SELECTOR, rowId),
                                   String.format(MENU_OPTION_SELECTOR, DEREGISTER_CLUSTER_MENU_ITEM));
        return new DeregisterClusterDialog();
    }

    public void verifyCISMClusterIsRegistered(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        LOGGER.info("Verify the CISM Cluster has been registered");
        driver.navigate().refresh();
        verifyCISMClustersPageIsOpened(driver);
        Optional<WebElement> row = getTableRowByCISMClusterName(driver, clusterConfig.getUIName());
        Assertions.assertThat(row)
                .as("Registered CISM Cluster \"%s\" is not present on the CISM Clusters Page", clusterConfig.getName())
                .isPresent();
    }

    public void verifyCISMClusterIsNotRegistered(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        LOGGER.info("Verify the CISM Cluster has been registered");
        driver.navigate().refresh();
        verifyCISMClustersPageIsOpened(driver);
        Optional<WebElement> row = getTableRowByCISMClusterName(driver, clusterConfig.getUIName());
        Assertions.assertThat(row).as("Registered CISM Cluster \"%s\" is not present on the CISM Clusters Page",
                                                  clusterConfig.getName()).isEmpty();
    }
}
