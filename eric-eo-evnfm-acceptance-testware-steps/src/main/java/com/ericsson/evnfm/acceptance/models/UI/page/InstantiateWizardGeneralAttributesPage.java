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
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutWizardSelectDropdown;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillTextField;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ericsson.evnfm.acceptance.commonUIActions.CommonActions;

public class InstantiateWizardGeneralAttributesPage {

    private static final String NEXT_BUTTON_SELECTOR = "#next";
    private static final String RESOURCE_INSTANCE_NAME_FIELD_SELECTOR = "#instance-name";
    private static final String DESCRIPTION_FIELD_SELECTOR = "#description";
    private static final String APPLICATION_TIMEOUT_FIELD_SELECTOR = "#application-timeout";
    private static final String INSTANTIATION_LEVEL_DROPDOWN = "e-generic-dropdown#instantiation-level-id";
    private static final String INSTANTIATION_LEVEL_LABEL = "instantiation_level_1";

    public void setResourceInstanceName(RemoteWebDriver driver, String resourceName){
        clearTextField(driver, RESOURCE_INSTANCE_NAME_FIELD_SELECTOR);
        fillTextField(driver, RESOURCE_INSTANCE_NAME_FIELD_SELECTOR, resourceName);
    }

    public void setDescription(RemoteWebDriver driver, String description){
        CommonActions.setDescription(driver, description, DESCRIPTION_FIELD_SELECTOR);
    }

    public void setApplicationTimeout(RemoteWebDriver driver, String applicationTimeout){
        clearTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR);
        fillTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR, applicationTimeout);
    }

    public AdditionalAttributesWizardPage goToInstantiateWizardAdditionalAttributesPage(RemoteWebDriver driver){
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new AdditionalAttributesWizardPage();
    }

    public void setInstantiationLevel(RemoteWebDriver driver, String instantiationLevel){
        WebElement instantiationLevelDropdown = querySelect(driver, INSTANTIATION_LEVEL_DROPDOWN);
        if (instantiationLevel != null){
            fillOutWizardSelectDropdown(driver, INSTANTIATION_LEVEL_LABEL, instantiationLevel, instantiationLevelDropdown);
        }
    }
}
