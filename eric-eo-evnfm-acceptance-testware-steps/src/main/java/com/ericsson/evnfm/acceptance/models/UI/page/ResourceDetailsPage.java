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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheTab;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.manualSleep;
import static com.ericsson.evnfm.acceptance.utils.Constants.RESOURCE_DETAILS;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceDetailsPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceDetailsPage.class);
    private static final String BACKUPS_TAB_SELECTOR = "#resourceBackups";
    private static final String COMPONENTS_TAB_SELECTOR = "#resourceComponents";

    public BackupsTab openBackupsTab(RemoteWebDriver driver){
        clickOnTheTab(driver, BACKUPS_TAB_SELECTOR);
        return new BackupsTab();
    }

    public ComponentsTab openComponentsTab(RemoteWebDriver driver){
        clickOnTheTab(driver, COMPONENTS_TAB_SELECTOR);
        manualSleep(2000);
        return new ComponentsTab();
    }

    public void verifyDetailsPageIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Check that Resource Details page is opened");
        try {
            driver.navigate().refresh();
            checkCurrentPage(driver, RESOURCE_DETAILS);
        } catch (RuntimeException exception) {
            LOGGER.info("Couldn't find the Resource Details page, retrying...");
            driver.navigate().refresh();
            checkCurrentPage(driver, RESOURCE_DETAILS);
        }
    }
}
