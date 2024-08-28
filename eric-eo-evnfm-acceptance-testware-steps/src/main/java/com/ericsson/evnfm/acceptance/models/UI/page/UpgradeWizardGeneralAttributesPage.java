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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clearTextField;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillTextField;

import org.openqa.selenium.remote.RemoteWebDriver;

import com.ericsson.evnfm.acceptance.commonUIActions.CommonActions;

public class UpgradeWizardGeneralAttributesPage {

    private static final String NEXT_BUTTON_SELECTOR = "#next";
    private static final String DESCRIPTION_FIELD_SELECTOR = "#description";
    private static final String APPLICATION_TIMEOUT_FIELD_SELECTOR = "#application-timeout";

    public void setDescription(RemoteWebDriver driver, String description){
        CommonActions.setDescription(driver, description, DESCRIPTION_FIELD_SELECTOR);
    }

    public void setApplicationTimeout(RemoteWebDriver driver, String applicationTimeout){
        clearTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR);
        fillTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR, applicationTimeout);
    }

    public AdditionalAttributesWizardPage goToUpgradeWizardAdditionalAttributesPage(RemoteWebDriver driver){
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new AdditionalAttributesWizardPage();
    }
}
