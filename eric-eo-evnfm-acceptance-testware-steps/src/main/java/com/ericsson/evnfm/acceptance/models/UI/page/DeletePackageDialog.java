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

import org.openqa.selenium.remote.RemoteWebDriver;

public class DeletePackageDialog {
    private static final String DELETE_PACKAGE_BUTTON = "eui-base-v0-dialog > eui-base-v0-button:nth-child(3)";


    public PackagesPage deletePackage(RemoteWebDriver driver) {
        clickOnTheButton(driver, DELETE_PACKAGE_BUTTON);
        return new PackagesPage();
    }
}
