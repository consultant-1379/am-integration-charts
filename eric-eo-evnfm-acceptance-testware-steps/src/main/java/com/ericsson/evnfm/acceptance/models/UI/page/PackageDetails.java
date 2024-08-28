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
import static com.ericsson.evnfm.acceptance.utils.Constants.PACKAGE_DETAILS;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageDetails {
    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDetails.class);
    private static final String SUPPORTED_OPERATIONS_TAB_SELECTOR = "#supportedOperations";

    public void verifyPackageDetailsPageIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Check that Package Details page is opened");
        checkCurrentPage(driver, PACKAGE_DETAILS);
    }

    public SupportedOperationsTab openSupportedOperationsTab(RemoteWebDriver driver) {
        clickOnTheTab(driver, SUPPORTED_OPERATIONS_TAB_SELECTOR);
        return new SupportedOperationsTab();
    }
}
