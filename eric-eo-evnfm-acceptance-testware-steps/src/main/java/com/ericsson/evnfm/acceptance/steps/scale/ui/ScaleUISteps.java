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

package com.ericsson.evnfm.acceptance.steps.scale.ui;

import static com.ericsson.evnfm.acceptance.utils.Constants.APPLICATION_TIME_OUT;
import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_OPERATION_STATES_SCALE;
import static com.ericsson.evnfm.acceptance.utils.Constants.SCALE_OPERATION;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.page.ConfirmScaleResourceDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.ScaleResourcePage;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class ScaleUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleUISteps.class);

    private ResourcesPage resourcesPage;
    private ScaleResourcePage scaleResourcePage;
    private ConfirmScaleResourceDialog confirmScaleResourceDialog;

    public void scaleResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnf) {
        scalePreparation (driver, evnfmCnf);

        LOGGER.info("Perform scale");
        scaleResourcePage = new ScaleResourcePage();
        scaleResourcePage.setScalingAspect(driver, evnfmCnf.getAspectToScale());
        scaleResourcePage.setStepsToScale(driver, evnfmCnf.getStepsToScale());
        scaleResourcePage.setApplicationTimeout(driver, evnfmCnf.getApplicationTimeout());
        confirmScaleResourceDialog = scaleResourcePage.openScaleDialog(driver);
        confirmScaleResourceDialog.scaleResource(driver);

        verifyScaledResource(driver, evnfmCnf);
    }

    public void scaleResourceStepsUI(RemoteWebDriver driver, EvnfmCnf evnfmCnf, ScaleVnfRequest scaleRequest) {
        scalePreparation (driver, evnfmCnf);

        LOGGER.info("Perform scale");
        scaleResourcePage = new ScaleResourcePage();
        scaleResourcePage.setScaleType(driver, scaleRequest.getType().toString());
        scaleResourcePage.setScalingAspect(driver, scaleRequest.getAspectId());
        scaleResourcePage.setStepsToScale(driver, scaleRequest.getNumberOfSteps().toString());
        scaleResourcePage.setApplicationTimeout(driver, evnfmCnf.getAdditionalParams().get(APPLICATION_TIME_OUT).toString());
        confirmScaleResourceDialog = scaleResourcePage.openScaleDialog(driver);
        confirmScaleResourceDialog.scaleResource(driver);

        verifyScaledResource(driver, evnfmCnf);
    }

    private void scalePreparation (RemoteWebDriver driver, EvnfmCnf evnfmCnf) {
        LOGGER.info("Opening Scale resource page from the Resources page");
        resourcesPage = new ResourcesPage(driver);
        resourcesPage.verifyResourcesPageIsOpened(driver);
        resourcesPage.openScaleResourcePage(driver, evnfmCnf.getVnfInstanceName(), evnfmCnf.getCluster().getUIName(), evnfmCnf.getApplicationTimeout());
    }

    private void verifyScaledResource(RemoteWebDriver driver, EvnfmCnf evnfmCnf) {
        LOGGER.info("Waiting for operation to be completed");
        resourcesPage.verifyResourcesPageIsOpened(driver);
        resourcesPage.verifyResourceIsScaled(driver, evnfmCnf.getVnfInstanceName(),
                                             evnfmCnf.getCluster().getUIName(),
                                             evnfmCnf.getExpectedOperationState(), evnfmCnf.getApplicationTimeout(),
                                             SCALE_OPERATION, INVALID_OPERATION_STATES_SCALE);
    }
}
