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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.ALL_ADDITIONAL_ATTRIBUTES_SELECTOR;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutWizardFieldsAdditionalAttributes;

import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;

public class AdditionalAttributesWizardPage {

    private static final String NEXT_BUTTON_SELECTOR = "#next";

    public void setAdditionalAttributes(RemoteWebDriver driver, Map<String, Object> additionalAttributes){
        fillOutWizardFieldsAdditionalAttributes(driver, ALL_ADDITIONAL_ATTRIBUTES_SELECTOR, additionalAttributes);
    }

    public SummaryWizardPage goToInstantiateWizardSummaryPage(RemoteWebDriver driver){
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new SummaryWizardPage();
    }
}
