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
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickCheckbox;

import org.openqa.selenium.remote.RemoteWebDriver;

public class ConfirmTerminationDialog {

    private static final String CONFIRM_TERMINATION_DIALOG= ".dialog";
    private static final String CANCEL_BUTTON_SELECTOR = "#Cancel";
    private static final String TERMINATE_BUTTON_SELECTOR = "#Terminate";
    private static final String CLEAN_UP_RESOURCES_CHECKBOX_SELECTOR = "#cleanUpResources";
    private static final String APPLICATION_TIMEOUT_FIELD_SELECTOR = "eui-base-v0-text-field[name=applicationTimeOut]";

    public ResourcesPage terminateResourceWithCleanUp(RemoteWebDriver driver, String timeout){
        clickCheckbox(driver, CLEAN_UP_RESOURCES_CHECKBOX_SELECTOR);
        clearTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR);
        fillTextField(driver, APPLICATION_TIMEOUT_FIELD_SELECTOR, timeout);
        clickOnTheButton(driver, TERMINATE_BUTTON_SELECTOR);
        return new ResourcesPage();
    }



}
