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
package com.ericsson.evnfm.acceptance.steps.terminate.UI;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.page.ConfirmTerminationDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;

public class TerminateUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateUISteps.class);

    private ResourcesPage resourcesPage;
    private ConfirmTerminationDialog confirmTerminationDialog;

    public void terminateResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnfToInstantiate){
        LOGGER.info("Opening Confirm Termination dialog screen from the Resources page");
        resourcesPage = new ResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        confirmTerminationDialog = resourcesPage.openTerminateDialog(driver, evnfmCnfToInstantiate.getVnfInstanceName(),
                                                                     evnfmCnfToInstantiate.getCluster().getUIName(),
                                                                     evnfmCnfToInstantiate.getApplicationTimeout());

        LOGGER.info("Terminate resource with clean up :: {}", evnfmCnfToInstantiate.getVnfInstanceName());
        resourcesPage = confirmTerminationDialog.terminateResourceWithCleanUp(driver, evnfmCnfToInstantiate.getApplicationTimeout());
        resourcesPage.verifyResourceIsTerminated(driver, evnfmCnfToInstantiate.getVnfInstanceName(), evnfmCnfToInstantiate.getCluster().getUIName(),
                                                 evnfmCnfToInstantiate.getApplicationTimeout());
    }
}
