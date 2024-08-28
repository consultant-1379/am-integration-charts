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
package com.ericsson.evnfm.acceptance.steps.upgrade.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCommonAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.buildHttpEntityInstantiateOrUpgrade;
import static com.ericsson.evnfm.acceptance.utils.Constants.ONBOARDING_V1_API_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.am.shared.vnfd.model.OperationDetail;
import com.ericsson.amonboardingservice.model.OperationDetailResponse;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps;
import com.ericsson.vnfm.orchestrator.model.ChangeCurrentVnfPkgRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpgradeCnfApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeCnfApiClient.class);

    public static ResponseEntity<Void> executeUpgradeCnfOperation(EvnfmCnf evnfmCnfToUpgrade, User user) {
        String upgradeCnfUrl = evnfmCnfToUpgrade.getVnfInstanceResponseLinks().getChangeVnfpkg().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG, evnfmCnfToUpgrade.getVnfInstanceName());
        ChangeCurrentVnfPkgRequest changeCurrentVnfPkgRequest = buildChangeCurrentCnfPkgRequestBody(evnfmCnfToUpgrade);
        return sendChangePackageRequest(changeCurrentVnfPkgRequest, evnfmCnfToUpgrade.getValuesFilePart(), user, upgradeCnfUrl);
    }

    public static ResponseEntity<Void> executeRollbackCnfOperation(EvnfmCnf evnfmCnfToRollback, User user) {
        String upgradeCnfUrl = evnfmCnfToRollback.getVnfInstanceResponseLinks().getChangeVnfpkg().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG, evnfmCnfToRollback.getVnfInstanceName());
        ChangeCurrentVnfPkgRequest changeCurrentVnfPkgRequest = buildRollbackRequestBody(evnfmCnfToRollback);
        return sendChangePackageRequest(changeCurrentVnfPkgRequest, null, user, upgradeCnfUrl);
    }

    public static ResponseEntity<Void> executeUpgradeCnfOperation(EvnfmCnf evnfmCnfToUpgrade, ChangeCurrentVnfPkgRequest request, User user) {
        String upgradeCnfUrl = evnfmCnfToUpgrade.getVnfInstanceResponseLinks().getChangeVnfpkg().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG, evnfmCnfToUpgrade.getVnfInstanceName());
        return sendChangePackageRequest(request, evnfmCnfToUpgrade.getValuesFilePart(), user, upgradeCnfUrl);
    }

    private static ResponseEntity<Void> sendChangePackageRequest(final ChangeCurrentVnfPkgRequest request,
                                                                 final Map<String, Object> valuesFilePart,
                                                                 final User user,
                                                                 final String upgradeCnfUrl) {
        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG, request);

        HttpEntity<?> requestHttpEntity = buildHttpEntityInstantiateOrUpgrade(valuesFilePart, createHeaders(user), request);
        long startTime = System.currentTimeMillis();
        ResponseEntity<Void> response = getRestRetryTemplate()
                .execute(context -> getRestTemplate().postForEntity(upgradeCnfUrl, requestHttpEntity, Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 25 seconds for url %s, execution time: %d seconds", upgradeCnfUrl,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 25);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG, getReasonPhrase(response.getStatusCode()));

        return response;
    }

    public static ChangeCurrentVnfPkgRequest buildChangeCurrentCnfPkgRequestBody(final EvnfmCnf evnfmCnfToUpgrade) {
        ChangeCurrentVnfPkgRequest request = new ChangeCurrentVnfPkgRequest();
        Map<String, Object> additionalMap = buildCommonAdditionalParamsMap(evnfmCnfToUpgrade);

        request.setVnfdId(evnfmCnfToUpgrade.getVnfdId());
        request.setAdditionalParams(additionalMap);
        request.setExtensions(evnfmCnfToUpgrade.getExtensions());

        return request;
    }

    public static ChangeCurrentVnfPkgRequest buildRollbackRequestBody(final EvnfmCnf evnfmCnfToUpgrade) {
        ChangeCurrentVnfPkgRequest request = new ChangeCurrentVnfPkgRequest();

        request.setVnfdId(evnfmCnfToUpgrade.getVnfdId());
        request.setAdditionalParams(evnfmCnfToUpgrade.getAdditionalParams());

        return request;
    }

    public static List<OperationDetail> getOperationDetailsForVnfPkgId(String vnfPackageId, User user) {
        String responseBody = makeRequestForSupportedOperations(vnfPackageId, user);
        ObjectMapper objectMapper = new ObjectMapper();
        List<OperationDetailResponse> operationDetailResponseList;
        try {
            operationDetailResponseList = objectMapper.readValue(responseBody,
                                                                 objectMapper.getTypeFactory()
                                                                         .constructCollectionType(List.class, OperationDetailResponse.class));
        } catch (JsonProcessingException e) {
            LOGGER.error("Can't parse OperationDetailResponse String into List, exceptionMessage={}", e.getMessage());
            return null; // we keep supported operations field not initialized
        }
        return operationDetailResponseList.stream().map(UpgradeCnfApiClient::buildOperationDetail).collect(Collectors.toList());
    }

    private static String makeRequestForSupportedOperations(String vnfPackageId, User user) {
        URI packageSupportedOperationsQueryURI = getPackageSupportedOperationsQueryURI(vnfPackageId);
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return getRestTemplate().exchange(packageSupportedOperationsQueryURI, HttpMethod.GET,
                                          new HttpEntity<>(httpHeaders), String.class).getBody();
    }

    private static OperationDetail buildOperationDetail(final OperationDetailResponse operationDetailResponse) {
        return new OperationDetail.Builder()
                .operationName(operationDetailResponse.getOperationName())
                .supported(operationDetailResponse.getSupported())
                .errorMessage(operationDetailResponse.getError())
                .build();
    }

    private static URI getPackageSupportedOperationsQueryURI(String vnfPkgId) {
        try {
            return new URL(
                    UriComponentsBuilder.fromHttpUrl(EVNFM_INSTANCE.getEvnfmUrl())
                            .path(ONBOARDING_V1_API_URI + "/")
                            .pathSegment("packages", vnfPkgId, "supported_operations")
                            .build().toString())
                    .toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
