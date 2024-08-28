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
package com.ericsson.evnfm.acceptance.steps.heal.rest;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCommonAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.utils.Constants.HEAL_VNF_LCM_OPP_URI_SUFFIX;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.HealVnfRequest;
import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class HealApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealApiClient.class);
    private static final String FULL_RESTORE = "FULL RESTORE";

    private HealApiClient() {
    }

    public static ResponseEntity<Void> executeHealCnfOperationRequest(final URILink selfLink, final EvnfmCnf cnaToHeal, final User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.HEAL, cnaToHeal.getVnfInstanceName());
        HttpEntity<HealVnfRequest> healVnfRequest = getHealHttpEntity(cnaToHeal, user);

        final String uri = selfLink.getHref() + HEAL_VNF_LCM_OPP_URI_SUFFIX;
        long startTime = System.currentTimeMillis();
        final ResponseEntity<Void> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().postForEntity(uri, healVnfRequest, Void.class));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 15 seconds for url %s, execution time: %d seconds", uri,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 15);
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, VnfLcmOpOcc.OperationEnum.HEAL, getReasonPhrase(response.getStatusCode()));
        return response;
    }

    private static HttpEntity<HealVnfRequest> getHealHttpEntity(final EvnfmCnf cnaToHeal, User user) {
        HealVnfRequest healVnfRequest = createHealVnfRequestBody(cnaToHeal);
        LOGGER.info(REQUEST_BODY_LOG, VnfLcmOpOcc.OperationEnum.HEAL, healVnfRequest);
        return new HttpEntity<>(healVnfRequest, createHeaders(user));
    }

    private static HealVnfRequest createHealVnfRequestBody(final EvnfmCnf cnaToHeal) {
        Map<String, Object> additionalMap = buildCommonAdditionalParamsMap(cnaToHeal);
        return new HealVnfRequest().cause(FULL_RESTORE).additionalParams(additionalMap);
    }
}
