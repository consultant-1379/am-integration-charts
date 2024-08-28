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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.utils.Constants.COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS;
import static com.ericsson.evnfm.acceptance.utils.Constants.CREATE_IDENTIFIER_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.CreateVnfRequest;

public class CreateVnfIdentifierApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateApiClient.class);

    private CreateVnfIdentifierApiClient() {
    }

    public static ResponseEntity<VnfInstanceLegacyResponse> executeCreateVnfIdentifierOperationRequest(EvnfmCnf evnfmCnfToInstantiate, User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, CREATE_IDENTIFIER_LIFECYCLE_OPERATION, evnfmCnfToInstantiate.getVnfInstanceName());

        HttpEntity<CreateVnfRequest> request = buildCreateVnfIdentifierRequest(evnfmCnfToInstantiate, user);

        String createVnfIdentifierUrl = EVNFM_INSTANCE.getEvnfmUrl() + COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS;
        ResponseEntity<VnfInstanceLegacyResponse> response = executeOperationWithLogs(
                createVnfIdentifierUrl, HttpMethod.POST, request, VnfInstanceLegacyResponse.class);

        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, CREATE_IDENTIFIER_LIFECYCLE_OPERATION, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    public static HttpEntity<CreateVnfRequest> buildCreateVnfIdentifierRequest(final EvnfmCnf evnfmCnfToInstantiate, final User user) {
        HttpHeaders httpHeaders = createHeaders(user);
        CreateVnfRequest createVnfRequest = new CreateVnfRequest()
                .vnfdId(evnfmCnfToInstantiate.getVnfdId())
                .vnfInstanceName(evnfmCnfToInstantiate.getVnfInstanceName())
                .vnfInstanceDescription(evnfmCnfToInstantiate.getVnfInstanceDescription())
                .metadata(evnfmCnfToInstantiate.getMetadata());
        LOGGER.info(REQUEST_BODY_LOG, CREATE_IDENTIFIER_LIFECYCLE_OPERATION, createVnfRequest);
        return new HttpEntity<>(createVnfRequest, httpHeaders);
    }
}
