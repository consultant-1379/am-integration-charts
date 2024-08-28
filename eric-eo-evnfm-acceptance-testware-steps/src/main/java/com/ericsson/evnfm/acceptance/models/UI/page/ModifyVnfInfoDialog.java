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
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.setDescription;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.setExtensions;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

import org.openqa.selenium.remote.RemoteWebDriver;

public class ModifyVnfInfoDialog {

    private static final String DESCRIPTION_FIELD_SELECTOR = "e-generic-text-area #vnfInstanceDescription";
    private static final String MODIFY_BUTTON_SELECTOR = "#Modify";

    public ResourcesPage modifyVnfInfo(RemoteWebDriver driver, EvnfmCnf evnfmCnf) {
        setExtensions(driver, evnfmCnf.getExtensions());
        setDescription(driver, evnfmCnf.getVnfInstanceDescription(), DESCRIPTION_FIELD_SELECTOR);
        clickOnTheButton(driver, MODIFY_BUTTON_SELECTOR);
        return new ResourcesPage();
    }
}
