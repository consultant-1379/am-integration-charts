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
package com.ericsson.evnfm.acceptance.steps.enm;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getReasonPhrase;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupAPIClient.queryVnfInstanceId;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.buildCommonAdditionalParamsMap;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.utils.Constants.ADD_NODE_TO_ENM_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_NODE_FROM_ENM_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REQUEST_BODY_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_PERFORMING_OPERATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class EnmApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnmApiClient.class);

    private EnmApiClient() {
    }

    public static void executeAddNodeToEnmRequest(final EvnfmCnf cnfToAddToEnm, final User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, VnfLcmOpOcc.OperationEnum.HEAL, cnfToAddToEnm.getVnfInstanceName());
        String vnfInstanceId = queryVnfInstanceId(cnfToAddToEnm.getVnfInstanceName(), user);
        HttpEntity<Map<String, Object>> addNodeRequest = getAddNodeHttpEntity(cnfToAddToEnm, user);

        final String uri = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ADD_NODE_TO_ENM_URI, vnfInstanceId);
        ResponseEntity<Void> response;
        try {
            response = getRestTemplate().postForEntity(uri, addNodeRequest, Void.class);
        } catch (HttpClientErrorException e) {
            if (cnfToAddToEnm.getExpectedError() == null) {
                fail(String.format("Error occurred during deleting node from ENM: %s", e.getMessage()));
            }
            verifyExpectedHttpError(e, cnfToAddToEnm.getExpectedError());
            return;
        }
        if (cnfToAddToEnm.getExpectedError() != null) {
            fail(String.format("Excepted error: {} but request was executed successfully", cnfToAddToEnm.getExpectedError().toString()
            ));
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, "Add Node", getReasonPhrase(response.getStatusCode()));
    }

    public static void executeDeleteNodeFromEnmRequest(final EvnfmCnf cnaToDeleteFromEnm, final User user) {
        LOGGER.info(STARTS_PERFORMING_OPERATION_LOG, "Delete Node", cnaToDeleteFromEnm.getVnfInstanceName());
        String vnfInstanceId = queryVnfInstanceId(cnaToDeleteFromEnm.getVnfInstanceName(), user);
        final String uri = EVNFM_INSTANCE.getEvnfmUrl() + String.format(DELETE_NODE_FROM_ENM_URI, vnfInstanceId);
        ResponseEntity<Void> response;
        try {
            response = getRestTemplate().postForEntity(uri, new HttpEntity<>(null, createHeaders(user)), Void.class);
        } catch (HttpClientErrorException e) {
            if (cnaToDeleteFromEnm.getExpectedError() == null) {
                fail(String.format("Error occurred during deleting node from ENM: %s", e.getMessage()));
            }
            verifyExpectedHttpError(e, cnaToDeleteFromEnm.getExpectedError());
            return;
        }
        if (cnaToDeleteFromEnm.getExpectedError() != null) {
            fail(String.format("Excepted error: {} but request was executed successfully", cnaToDeleteFromEnm.getExpectedError().toString()
            ));
        }
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private static HttpEntity<Map<String, Object>> getAddNodeHttpEntity(final EvnfmCnf cnaToAddToEnm, User user) {
        Map<String, Object> addNodeRequest = buildCommonAdditionalParamsMap(cnaToAddToEnm);
        LOGGER.info(REQUEST_BODY_LOG, "Add Node", addNodeRequest);
        return new HttpEntity<>(addNodeRequest, createHeaders(user));
    }
}
