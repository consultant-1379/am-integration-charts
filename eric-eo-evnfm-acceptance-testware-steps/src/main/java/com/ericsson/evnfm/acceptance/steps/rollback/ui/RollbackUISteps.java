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
package com.ericsson.evnfm.acceptance.steps.rollback.ui;

import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_OPERATION_STATES_ROLLBACK;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.RollbackResourcePage;

public class RollbackUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackUISteps.class);

    private ResourcesPage resourcesPage;
    private RollbackResourcePage rollbackResourcePage;

    public ResourceInfo rollbackResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnfToRollback) {
        LOGGER.info("Opening Resources page and choosing instance to rollback");
        resourcesPage = new ResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        rollbackResourcePage = resourcesPage.openRollbackDialog(driver,
                                                                evnfmCnfToRollback.getVnfInstanceName(),
                                                                evnfmCnfToRollback.getCluster().getUIName(),
                                                                evnfmCnfToRollback.getApplicationTimeout());

        LOGGER.info("Rollback resource:: {}", evnfmCnfToRollback.getVnfInstanceName());
        resourcesPage = rollbackResourcePage.rollback(driver, evnfmCnfToRollback.getAdditionalParams());
        resourcesPage.verifyResourcesPageIsOpened(driver);
        return resourcesPage.verifyResourceState(driver, evnfmCnfToRollback, INVALID_OPERATION_STATES_ROLLBACK);
    }
}
