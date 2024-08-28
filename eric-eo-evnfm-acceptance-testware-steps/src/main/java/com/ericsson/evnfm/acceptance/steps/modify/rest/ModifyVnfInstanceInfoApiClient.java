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
package com.ericsson.evnfm.acceptance.steps.modify.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfInfoModificationRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class ModifyVnfInstanceInfoApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInstanceInfoApiClient.class);

    private ModifyVnfInstanceInfoApiClient() {
    }

    public static ResponseEntity<Void> executeModifyVnfInfoOperationRequest(EvnfmCnf evnfmCnfToModify, User user) {
        String cnfUrl = evnfmCnfToModify.getVnfInstanceResponseLinks().getSelf().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.MODIFY_INFO, evnfmCnfToModify.getVnfInstanceName());

        VnfInfoModificationRequest modifyVnfRequest = buildModifyVnfRequestBody(evnfmCnfToModify);
        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.MODIFY_INFO, modifyVnfRequest);

        final HttpEntity<VnfInfoModificationRequest> requestHttpEntity = new HttpEntity<>(modifyVnfRequest, createHeaders(user));

        ResponseEntity<Void> response = executeOperationWithLogs(cnfUrl, HttpMethod.PATCH, requestHttpEntity, Void.class);

        LOGGER.info("Modify INFO response : {}", response);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.MODIFY_INFO, response.getStatusCode());
        return response;
    }

    private static VnfInfoModificationRequest buildModifyVnfRequestBody(final EvnfmCnf evnfmCnfToModify) {
        VnfInfoModificationRequest request = new VnfInfoModificationRequest();

        if (evnfmCnfToModify.getVnfInstanceNameToModify() != null) {
            request.setVnfInstanceName(evnfmCnfToModify.getVnfInstanceNameToModify());
        }
        if (evnfmCnfToModify.getMetadata() != null) {
            request.setMetadata(evnfmCnfToModify.getMetadata());
        }
        if (evnfmCnfToModify.getVnfInstanceDescription() != null) {
            request.setVnfInstanceDescription(evnfmCnfToModify.getVnfInstanceDescription());
        }
        if (evnfmCnfToModify.getExtensions() != null) {
            request.setExtensions(evnfmCnfToModify.getExtensions());
        }

        return request;
    }
}
