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
package com.ericsson.evnfm.acceptance.steps.scale.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.utils.Constants.APPLICATION_TIME_OUT;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.QUERY_VNFC_SCALE_INFO_URL;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.ericsson.vnfm.orchestrator.model.VnfcScaleInfo;

public class ScaleCnfApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleCnfApiClient.class);

    private ScaleCnfApiClient() {
    }

    public static ResponseEntity<Void> executeScaleCnfOperationRequest(EvnfmCnf evnfmCnfToScale,
                                                                       User user,
                                                                       final ConfigScale configScale,
                                                                       ScaleVnfRequest.TypeEnum scaleType,
                                                                       String aspectId) {
        String scaleCnfUrl = evnfmCnfToScale.getVnfInstanceResponseLinks().getScale().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.SCALE, evnfmCnfToScale.getVnfInstanceName());

        ScaleVnfRequest scaleVnfRequest = buildScaleCnfRequestBody(configScale, scaleType, aspectId);
        return sendScaleRequest(user, scaleCnfUrl, scaleVnfRequest);
    }

    public static ResponseEntity<Void> executeScaleCnfOperationRequest(EvnfmCnf evnfmCnfToScale,
                                                                       User user, ScaleVnfRequest scaleVnfRequest) {
        String scaleCnfUrl = evnfmCnfToScale.getVnfInstanceResponseLinks().getScale().getHref();

        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.SCALE, evnfmCnfToScale.getVnfInstanceName());
        return sendScaleRequest(user, scaleCnfUrl, scaleVnfRequest);
    }

    private static ResponseEntity<Void> sendScaleRequest(final User user, final String scaleCnfUrl, final ScaleVnfRequest scaleVnfRequest) {
        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.SCALE, scaleVnfRequest);

        final HttpEntity<ScaleVnfRequest> requestHttpEntity = new HttpEntity<>(scaleVnfRequest, createHeaders(user));
        long startTime = System.currentTimeMillis();
        ResponseEntity<Void> response = getRestRetryTemplate()
                .execute(context -> getRestTemplate().postForEntity(scaleCnfUrl, requestHttpEntity, Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds", scaleCnfUrl,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 15);

        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.SCALE, getReasonPhrase(response.getStatusCode()));

        return response;
    }

    public static ScaleVnfRequest buildScaleCnfRequestBody(final ConfigScale configScale, ScaleVnfRequest.TypeEnum scaleType, String aspectId) {
        Map<String, Object> additionalMap = buildScaleCnfAdditionalMap(configScale);
        return new ScaleVnfRequest()
                .aspectId(aspectId)
                .numberOfSteps(Integer.parseInt(configScale.getNumberOfSteps()))
                .type(scaleType)
                .additionalParams(additionalMap);
    }

    public static Map<String, Object> buildScaleCnfAdditionalMap(final ConfigScale configScale) {
        Map<String, Object> additionalParams = configScale.getAdditionalAttributes();

        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }

        additionalParams.put(APPLICATION_TIME_OUT, configScale.getApplicationTimeout());

        additionalParams.values().removeAll(Collections.singleton(null));

        return additionalParams;
    }

    public static List<VnfcScaleInfo> getVnfcScaleInfo(User user, final String vnfInstanceId, final String queryParams) {
        String url = EVNFM_INSTANCE.getEvnfmUrl() + String.format(QUERY_VNFC_SCALE_INFO_URL, vnfInstanceId, queryParams);
        HttpHeaders httpHeaders = getHttpHeadersWithToken(user);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query VNFC scale info request {}\n", url);
        final ResponseEntity<VnfcScaleInfo[]> response = executeOperationWithLogs(url, HttpMethod.GET, entity, VnfcScaleInfo[].class);
        Assertions.assertThat(response.getStatusCode().value())
                .withFailMessage("Query Vnfc scale info request was not accepted: %s", response.getBody()).isEqualTo(200);
        LOGGER.info(Arrays.toString(response.getBody()));
        return Arrays.stream(Objects.requireNonNull(response.getBody())).collect(Collectors.toList());
    }
}
