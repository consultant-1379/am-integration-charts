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
package com.ericsson.evnfm.acceptance.steps.rest;

import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfModel;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfOperation;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.upgradeVnf;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG;

import java.util.Optional;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;

public class UpgradeSteps {

    public static void performUpgrade(final ConfigUpgrade configUpgrade,
                                final VnfInstanceResponseLinks vnfInstanceResponseLinks,
                                final Optional<AppPackageResponse> upgradePackage, final String valuesFilePart, final String testType,
                                      boolean persistScaleIno) {
        String upgradeHeader = upgradeVnf(vnfInstanceResponseLinks, configUpgrade, valuesFilePart, testType, persistScaleIno);
        queryVnfOperation(upgradeHeader, configUpgrade.getExpectedOperationState(),
                configUpgrade.getApplicationTimeOut(), CHANGE_VNFPKG);
        queryVnfModel(vnfInstanceResponseLinks.getSelf().getHref(), upgradePackage.get().getAppDescriptorId());
    }
}
