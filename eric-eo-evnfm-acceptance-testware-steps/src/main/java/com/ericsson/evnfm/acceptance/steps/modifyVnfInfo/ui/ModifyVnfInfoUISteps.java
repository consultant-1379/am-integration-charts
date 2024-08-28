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
package com.ericsson.evnfm.acceptance.steps.modifyVnfInfo.ui;

import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoSteps.performModifyVnfInstanceInfoUITestStep;
import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_OPERATION_STATES_MODIFY_VNF_INFO;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.page.ModifyVnfInfoDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;
import com.ericsson.evnfm.acceptance.models.User;

public class ModifyVnfInfoUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInfoUISteps.class);
    private ResourcesPage resourcesPage;
    private ModifyVnfInfoDialog modifyVnfInfoDialog;

    public void modifyVnfInfoUI(RemoteWebDriver driver, EvnfmCnf evnfmCnf, User user) {
        LOGGER.info("Opening Resources page and choosing instance to modify");
        resourcesPage = new ResourcesPage();
        resourcesPage.verifyResourcesPageIsOpened(driver);
        modifyVnfInfoDialog = resourcesPage.openModifyVnfInfoDialog(driver, evnfmCnf.getVnfInstanceName(), evnfmCnf.getCluster().getUIName());
        LOGGER.info("Modifying VNF info of resource");
        resourcesPage = modifyVnfInfoDialog.modifyVnfInfo(driver, evnfmCnf);
        resourcesPage.verifyResourceState(driver, evnfmCnf, INVALID_OPERATION_STATES_MODIFY_VNF_INFO);

        performModifyVnfInstanceInfoUITestStep(evnfmCnf, user);
    }
}
