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
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutEuiWizardSelectCombobox;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillTextField;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public class ScaleResourcePage {

    private static final String SCALING_ASPECT_COMBOBOX ="eui-base-v0-combo-box#aspectId-combo-box";
    private static final String SCALING_ASPECT_COMBOBOX_ID ="aspectId-combo-box";
    private static final String STEPS_TO_SCALE_FIELD_SELECTOR ="#steps-to-scale";
    private static final String APPLICATION_TIMEOUT_FIELD_SELECTOR = "#applicationTimeOut";
    private static final String SCALE_BUTTON_SELECTOR = ".scale-perform-button";
    private static final String SCALE_TYPE_SELECTOR = "#scale-type-";

    public void setScalingAspect(RemoteWebDriver driver, String aspect){
        WebElement scalingAspectCombobox = querySelect(driver, SCALING_ASPECT_COMBOBOX);
        fillOutEuiWizardSelectCombobox(driver, SCALING_ASPECT_COMBOBOX_ID, aspect, scalingAspectCombobox);
    }

    public void setStepsToScale(final RemoteWebDriver driver, String stepsToScale) {
        clearTextField(driver, STEPS_TO_SCALE_FIELD_SELECTOR);
        fillTextField(driver, STEPS_TO_SCALE_FIELD_SELECTOR, stepsToScale);
    }

    public void setApplicationTimeout(final RemoteWebDriver driver, final String applicationTimeout) {
        clearTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR);
        fillTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR, applicationTimeout);
    }

    public ConfirmScaleResourceDialog openScaleDialog(final RemoteWebDriver driver) {
        clickOnTheButton(driver, SCALE_BUTTON_SELECTOR);
        return new ConfirmScaleResourceDialog();
    }

    public void setScaleType(final RemoteWebDriver driver, String scaleType) {
        String selectorType = scaleType.replace("_", "-");
        clickOnTheButton(driver, SCALE_TYPE_SELECTOR + selectorType.toLowerCase());
    }
}
