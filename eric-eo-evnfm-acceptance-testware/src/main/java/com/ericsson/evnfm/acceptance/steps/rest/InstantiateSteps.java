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

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CREATE_IDENTIFER_URI;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getGatewayUrl;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.createIdentifier;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.instantiate;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.instantiateBadRequest;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfOperation;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;

public class InstantiateSteps {

    public static Logger LOGGER = LoggerFactory.getLogger(InstantiateSteps.class);

    public static VnfInstanceResponseLinks performInstantiate(final ConfigInstantiate configInstantiate,
                                                        final Optional<AppPackageResponse> instantiatePackage, final boolean isContainerStatusVerification,
                                                        final String valuesFilePart) {
        VnfInstanceResponseLinks vnfInstanceResponseLinks = createIdentifier(getGatewayUrl() + CREATE_IDENTIFER_URI,
                instantiatePackage.get().getAppDescriptorId(), configInstantiate);
        LOGGER.info("VnfInstanceLinks are: {}", vnfInstanceResponseLinks);
        String instantiateHeader = instantiate(vnfInstanceResponseLinks, configInstantiate,
                isContainerStatusVerification, valuesFilePart);
        queryVnfOperation(instantiateHeader, configInstantiate.getExpectedOperationState(),
                configInstantiate.getApplicationTimeOut(), INSTANTIATE);
        return vnfInstanceResponseLinks;
    }

    public static VnfInstanceResponseLinks performNegativeInstantiate(final ConfigInstantiate configInstantiate,
                                                                final Optional<AppPackageResponse> instantiatePackage, final boolean isContainerStatusVerification,
                                                                final String valuesFilePart) {
        VnfInstanceResponseLinks vnfInstanceResponseLinks = createIdentifier(getGatewayUrl() + CREATE_IDENTIFER_URI,
                instantiatePackage.get().getAppDescriptorId(), configInstantiate);
        LOGGER.info("VnfInstanceLinks are: {}", vnfInstanceResponseLinks);
        instantiateBadRequest(vnfInstanceResponseLinks, configInstantiate, isContainerStatusVerification,
                valuesFilePart);

        return vnfInstanceResponseLinks;
    }
}
