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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCommonAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.buildHttpEntityInstantiateOrUpgrade;
import static com.ericsson.evnfm.acceptance.utils.Constants.NAMESPACE;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.PERFORMING_GET_INSTANCE_REQUEST_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.InstantiateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class InstantiateApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateApiClient.class);

    private InstantiateApiClient() {
    }

    public static ResponseEntity<Void> executeInstantiateCnfOperationRequest(EvnfmCnf evnfmCnfToInstantiate, User user) {
        return executeInstantiateCnfOperationRequest(Void.class, evnfmCnfToInstantiate, user);
    }

    public static ResponseEntity<VnfInstanceLegacyResponse> executeQueryVnfIdentifierBySelfLinkOperationRequest(EvnfmCnf cnf, User user) {
        final String selfLink = cnf.getVnfInstanceResponseLinks().getSelf().getHref();
        LOGGER.info(PERFORMING_GET_INSTANCE_REQUEST_LOG, selfLink);
        return getRestRetryTemplate().execute(context -> getRestTemplate().exchange(
                selfLink,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(user)),
                VnfInstanceLegacyResponse.class));
    }

    public static <E> ResponseEntity<E> executeInstantiateCnfOperationRequest(Class<E> responseType, EvnfmCnf evnfmCnfToInstantiate, User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, evnfmCnfToInstantiate.getVnfInstanceName());
        long startTime = System.currentTimeMillis();
        final ResponseEntity<E> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(
                        evnfmCnfToInstantiate.getVnfInstanceResponseLinks().getInstantiate().getHref(),
                        HttpMethod.POST,
                        buildInstantiateRequest(evnfmCnfToInstantiate, user),
                        responseType));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds",
                                 evnfmCnfToInstantiate.getVnfInstanceResponseLinks().getInstantiate().getHref(), executionTimeInSeconds),
                   executionTimeInSeconds <= 15);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    public static HttpEntity<?> buildInstantiateRequest(final EvnfmCnf evnfmCnfToInstantiate, final User user) {
        Map<String, Object> additionalParams = getAdditionalParams(evnfmCnfToInstantiate);

        var instantiateVnfRequest = new InstantiateVnfRequest().additionalParams(additionalParams);

        if (evnfmCnfToInstantiate.getCluster() != null) {
            instantiateVnfRequest.setClusterName(evnfmCnfToInstantiate.getCluster().getName());
        }
        Optional.ofNullable(evnfmCnfToInstantiate.getTargetScaleLevelInfo()).ifPresent(instantiateVnfRequest::setTargetScaleLevelInfo);
        instantiateVnfRequest.setExtensions(evnfmCnfToInstantiate.getExtensions());

        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.INSTANTIATE, instantiateVnfRequest);
        return buildHttpEntityInstantiateOrUpgrade(evnfmCnfToInstantiate.getValuesFilePart(), createHeaders(user), instantiateVnfRequest);
    }

    private static Map<String, Object> getAdditionalParams(final EvnfmCnf evnfmCnfToInstantiate) {
        Map<String, Object> additionalParams = buildCommonAdditionalParamsMap(evnfmCnfToInstantiate);
        additionalParams.put(NAMESPACE, evnfmCnfToInstantiate.getNamespace());

        if (evnfmCnfToInstantiate.isContainerStatusVerification()) {
            additionalParams.put("eric-cm-mediator.enabled", true);
            additionalParams.put("eric-cm-mediator.ingress.hostname", "spider-app-cm-mediator.hahn062.rnd.gic.ericsson.se");
        }
        return additionalParams;
    }
}
