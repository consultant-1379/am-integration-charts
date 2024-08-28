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
package com.ericsson.evnfm.acceptance.steps.terminate.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_IDENTIFIER_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.FAILED_TO_DELETE_IDENTIFIER;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.TERMINATE_REQUEST_WAS_NOT_ACCEPTED;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLEAN_UP_CNF_ERROR;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLEANUP;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class TerminateCnfVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminateCnfVerify.class);

    private TerminateCnfVerify() {
    }

    public static void verifyTerminateCnfResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.TERMINATE, ACCEPTED);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).withFailMessage(TERMINATE_REQUEST_WAS_NOT_ACCEPTED, response.getBody()).isEqualTo(ACCEPTED);
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.TERMINATE);
    }

    public static void verifyDeleteCnfIdentifierResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, DELETE_IDENTIFIER_LIFECYCLE_OPERATION, NO_CONTENT);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).withFailMessage(FAILED_TO_DELETE_IDENTIFIER, response.getBody()).isEqualTo(NO_CONTENT);
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, DELETE_IDENTIFIER_LIFECYCLE_OPERATION);
    }

    public static void verifyCleanupVnfResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, CLEANUP, ACCEPTED);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).withFailMessage(CLEAN_UP_CNF_ERROR, response.getBody()).isEqualTo(ACCEPTED);
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, CLEANUP);
    }
}
