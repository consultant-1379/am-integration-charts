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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.utils.Constants.HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class ModifyVnfInstanceInfoVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyVnfInstanceInfoVerify.class);

    private ModifyVnfInstanceInfoVerify() {
    }

    public static void verifyModifyVnfInfoResponseSuccess(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.MODIFY_INFO, HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(HttpStatus.ACCEPTED).isEqualTo(response.getStatusCode());
        assertThat(response.getHeaders()).isNotNull();

        final List<String> headers = response.getHeaders().get(LOCATION);
        assertThat(CollectionUtils.isEmpty(headers)).withFailMessage(HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE).isFalse();
        assertThat(headers.get(0)).isNotNull();

        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.MODIFY_INFO);
    }

    public static void verifyModifiedInfoOfVnfInstance(EvnfmCnf evnfmCnf, VnfInstanceResponse actualVnfInstance) {
        LOGGER.info("Starts verifying Modified Info");

        if (evnfmCnf.getVnfInstanceNameToModify() != null) {
            assertThat(actualVnfInstance.getVnfInstanceName()).isEqualTo(evnfmCnf.getVnfInstanceNameToModify());
        }
        if (evnfmCnf.getVnfInstanceDescription() != null) {
            assertThat(actualVnfInstance.getVnfInstanceDescription()).isEqualTo(evnfmCnf.getVnfInstanceDescription());
        }
        if (evnfmCnf.getMetadata() != null) {
            assertThat(actualVnfInstance.getMetadata()).isEqualTo(evnfmCnf.getMetadata());
        }

        LOGGER.info("Verifying Modified Info completed successfully");
    }
}