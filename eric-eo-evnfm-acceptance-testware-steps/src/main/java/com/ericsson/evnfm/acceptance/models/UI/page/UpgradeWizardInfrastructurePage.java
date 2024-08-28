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

public class UpgradeWizardInfrastructurePage {
    private static final String NEXT_BUTTON_SELECTOR = "#next";

    public UpgradeWizardGeneralAttributesPage goToUpgradeWizardGeneralAttributesPage(RemoteWebDriver driver) {
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new UpgradeWizardGeneralAttributesPage();
    }
}
