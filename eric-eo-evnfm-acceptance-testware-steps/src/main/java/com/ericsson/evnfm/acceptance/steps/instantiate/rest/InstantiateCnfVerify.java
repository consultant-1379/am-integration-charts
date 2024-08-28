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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.utils.Constants.CREATE_IDENTIFIER_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.GET_INSTANCE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.INSTANTIATED;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class InstantiateCnfVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateCnfVerify.class);

    private InstantiateCnfVerify() {
    }

    public static void verifyCnfIdentifierCreated(ResponseEntity<? extends VnfInstanceResponse> response, EvnfmCnf evnfmCnfToInstantiate) {
        LOGGER.info(STARTS_VERIFICATION_LOG, CREATE_IDENTIFIER_LIFECYCLE_OPERATION, HttpStatus.CREATED);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody())
                .hasFieldOrPropertyWithValue("vnfdId", evnfmCnfToInstantiate.getVnfdId())
                .hasFieldOrPropertyWithValue("vnfInstanceName", evnfmCnfToInstantiate.getVnfInstanceName())
                .hasFieldOrPropertyWithValue("instantiationState", NOT_INSTANTIATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getLinks()).isNotNull();
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, CREATE_IDENTIFIER_LIFECYCLE_OPERATION);
    }

    public static void verifyCnfInstantiatedResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getHeaders()).isNotNull();

        final List<String> headers = response.getHeaders().get(LOCATION);
        assertThat(CollectionUtils.isEmpty(headers)).withFailMessage(HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE).isFalse();
        assertThat(headers.get(0)).isNotNull();
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE);
    }

    public static void verifyCnfInstanceReturnedInResponse(ResponseEntity<? extends VnfInstanceResponse> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, GET_INSTANCE_OPERATION, HttpStatus.OK);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(VnfInstanceResponse.class);
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, GET_INSTANCE_OPERATION);
    }

    public static void verifyCnfWithUnregisteredClusterWasNotInstantiated(ProblemDetails actualNotFoundResponse, ProblemDetails expected) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, HttpStatus.NOT_FOUND);

        assertThat(actualNotFoundResponse).isNotNull();
        assertThat(actualNotFoundResponse.getStatus()).isEqualTo(404);
        assertThat(actualNotFoundResponse).isEqualTo(expected);

        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE);
    }

    public static void verifyCnfIsInstantiated(ResponseEntity<? extends VnfInstanceResponse> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, HttpStatus.OK);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(VnfInstanceResponse.class);
        assertThat(response.getBody().getInstantiationState()).isEqualTo(INSTANTIATED);
    }

    public static void verifyScaleStatus(Map<String, Integer> expectedScaleInfo, ResponseEntity<? extends VnfInstanceResponse> responseEntity) {
        LOGGER.info("Starts verifying ScaleStatus");
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        final VnfInstanceResponse vnfInstanceResponse = responseEntity.getBody();
        final Map<String, Integer> actualScalingInfo = vnfInstanceResponse.getInstantiatedVnfInfo()
                .getScaleStatus()
                .stream()
                .collect(Collectors.toMap(ScaleInfo::getAspectId, ScaleInfo::getScaleLevel));

        assertThat(actualScalingInfo).isEqualTo(expectedScaleInfo);
        LOGGER.info("Verifying ScaleStatus completed successfully");
    }
}
