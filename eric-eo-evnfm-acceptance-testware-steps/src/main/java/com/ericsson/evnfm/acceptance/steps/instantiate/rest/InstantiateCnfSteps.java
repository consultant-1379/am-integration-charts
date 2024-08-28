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
package com.ericsson.evnfm.acceptance.steps.instantiate.rest;

import static org.assertj.core.api.Fail.fail;
import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyFailedRequest;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectDay0VerificationInfo;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExtensionsAreEqual;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.enm.EnmVerify.verifyInstantiateOssParameters;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.CreateVnfIdentifierApiClient.executeCreateVnfIdentifierOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateApiClient.executeInstantiateCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateApiClient.executeQueryVnfIdentifierBySelfLinkOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyCnfIdentifierCreated;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyCnfInstanceReturnedInResponse;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyCnfInstantiatedResponse;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyCnfIsInstantiated;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyScaleStatus;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.fasterxml.jackson.core.JsonProcessingException;

public class InstantiateCnfSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateCnfSteps.class);

    private InstantiateCnfSteps() {
    }

    /**
     * Create Cnf Identifier and instantiate the Cnf with relevant parameters
     *
     * @param evnfmCnfToInstantiate Cnf instantiate details
     * @param user                  User performing operations
     */
    public static void performCreateIdentifierAndInstantiateCnfStep(EvnfmCnf evnfmCnfToInstantiate, User user) {
        performCreateCnfIdentifierStep(evnfmCnfToInstantiate, user);
        performInstantiateCnfStep(evnfmCnfToInstantiate, user);
    }

    /**
     * Create a Cnf identifier - pre-requisite to creating the Cnf
     *
     * @param evnfmCnfToInstantiate Cnf instantiate details
     * @param user                  User performing operations
     */
    public static void performCreateCnfIdentifierStep(EvnfmCnf evnfmCnfToInstantiate, User user) {
        final ResponseEntity<VnfInstanceLegacyResponse> createIdentifierResponse =
                executeCreateVnfIdentifierOperationRequest(evnfmCnfToInstantiate, user);
        verifyCnfIdentifierCreated(createIdentifierResponse, evnfmCnfToInstantiate);

        evnfmCnfToInstantiate.setVnfInstanceResponseLinks(
                Objects.requireNonNull(createIdentifierResponse.getBody()).getLinks()
        );
    }

    /**
     * Instantiate the Cnf with relevant parameters
     *
     * @param evnfmCnfToInstantiate Cnf instantiate details
     * @param user                  User performing operations
     */
    public static void performInstantiateCnfStep(EvnfmCnf evnfmCnfToInstantiate, User user) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToInstantiate, user);

        final String operationLink = executeInstantiateCnfOperationRequestAndVerifyResponse(evnfmCnfToInstantiate, user);

        Day0SecretVerificationInfo verificationInfo = collectDay0VerificationInfo(evnfmCnfToInstantiate);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(
                evnfmCnfToInstantiate, user, operationLink, verificationInfo);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, INSTANTIATE);

        final ResponseEntity<VnfInstanceLegacyResponse> getVnfinstanceResponse = executeQueryVnfIdentifierBySelfLinkOperationRequest(
                evnfmCnfToInstantiate,
                user);
        verifyCnfIsInstantiated(getVnfinstanceResponse);
        if (evnfmCnfToInstantiate.getTargetScaleLevelInfo() != null && evnfmCnfToInstantiate.getScaleInfo() != null) {
            verifyScaleStatus(evnfmCnfToInstantiate.getScaleInfo(), getVnfinstanceResponse);
        }
        if (evnfmCnfToInstantiate.getExpectedHelmValues() != null) {
            verifyHelmValues(evnfmCnfToInstantiate);
        }
        if (evnfmCnfToInstantiate.getExtensions() != null) {
            verifyExtensionsAreEqual(evnfmCnfToInstantiate, getVnfinstanceResponse.getBody());
        }
        if (evnfmCnfToInstantiate.getInstantiateOssTopology() != null) {
            verifyInstantiateOssParameters(evnfmCnfToInstantiate, Objects.requireNonNull(getVnfinstanceResponse.getBody()).getId(), user);
        }
    }

    public static String executeInstantiateCnfOperationRequestAndVerifyResponse(final EvnfmCnf evnfmCnfToInstantiate, final User user) {
        final ResponseEntity<Void> instantiateCnfResponse = executeInstantiateCnfOperationRequest(evnfmCnfToInstantiate, user);
        verifyCnfInstantiatedResponse(instantiateCnfResponse);

        final String operationLink = Objects.requireNonNull(instantiateCnfResponse.getHeaders().get(LOCATION)).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationLink);

        return operationLink;
    }

    /**
     * Get Cnf instance by self link
     *
     * @param cnf  Cnf with link to instance
     * @param user User performing operations
     */
    public static VnfInstanceResponse getVnfInstanceByLink(EvnfmCnf cnf, User user) {
        final var instanceLegacyResponseEntity = executeQueryVnfIdentifierBySelfLinkOperationRequest(cnf, user);
        verifyCnfInstanceReturnedInResponse(instanceLegacyResponseEntity);
        return instanceLegacyResponseEntity.getBody();
    }

    public static void executeCreateVnfIdentifierExpectingError(EvnfmCnf evnfmCnfToInstantiate, ProblemDetails expectedError, User user)
    throws JsonProcessingException {
        try {
            LOGGER.info("createVnfIdentifierExpectingError");
            performCreateCnfIdentifierStep(evnfmCnfToInstantiate, user);
        } catch (HttpClientErrorException httpClientErrorException) {
            ProblemDetails errorMessageReceived = getObjectMapper().readValue(httpClientErrorException.getResponseBodyAsString(),
                                                                              ProblemDetails.class);
            LOGGER.info("Error message received: {}", errorMessageReceived);
            verifyFailedRequest(errorMessageReceived, expectedError);
            return;
        }
        fail("Exception was expected, but none received");
    }

    public static void performCreateCnfIdentifierStepExpectingError(EvnfmCnf evnfmCnfToCreate, User user) {
        try {
            performCreateCnfIdentifierStep(evnfmCnfToCreate, user);
        } catch (HttpClientErrorException exception) {
            verifyExpectedHttpError(exception, evnfmCnfToCreate.getExpectedError());
        }
    }
}
