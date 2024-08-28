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

import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.deleteVnfIdentifier;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfOperation;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.terminateVnf;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.TERMINATE;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigTerminate;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;

public class TerminateSteps {

    public static void performTerminateAndDeleteIdentifier(final ConfigTerminate configTerminate,
                                                     final VnfInstanceResponseLinks vnfInstanceResponseLinks) {
        String terminateHeader = terminateVnf(vnfInstanceResponseLinks, configTerminate);
        queryVnfOperation(terminateHeader, configTerminate.getExpectedOperationState(),
                configTerminate.getApplicationTimeOut(), TERMINATE);
        deleteVnfIdentifier(vnfInstanceResponseLinks);
    }
}
