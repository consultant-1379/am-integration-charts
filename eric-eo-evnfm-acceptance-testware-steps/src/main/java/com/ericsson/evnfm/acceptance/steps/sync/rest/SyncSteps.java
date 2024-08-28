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
package com.ericsson.evnfm.acceptance.steps.sync.rest;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExtensionsAreEqual;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyReplicaCountInDatabaseForAspect;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyScaleInfo;
import static com.ericsson.evnfm.acceptance.utils.Constants.APPLICATION_TIME_OUT;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.SYNC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.SyncVnfRequest;
import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class SyncSteps {

    public static Logger LOGGER = LoggerFactory.getLogger(SyncSteps.class);
    public static String timeout = "300";

    public static void performSync(EvnfmCnf cnaToSync, User user) {
        String operationUrl = executeSyncRequest(cnaToSync, user);
        LOGGER.info("Operation url is {}", operationUrl);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(cnaToSync,
                                                                                                       user,
                                                                                                       operationUrl,
                                                                                                       null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, SYNC, cnaToSync.getExpectedOperationState());
        VnfInstanceLegacyResponse vnfInstanceResponse = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                cnaToSync.getVnfInstanceName(), user);
        LOGGER.info("SyncTest : Verify scale info:");
        verifyScaleInfo(cnaToSync, vnfInstanceResponse);
        LOGGER.info("SyncTest : Verification scale info is finished successfully");

        LOGGER.info("SyncTest : Verify extensions:");
        verifyExtensionsAreEqual(cnaToSync, vnfInstanceResponse);
        LOGGER.info("SyncTest : Verification extensions is finished successfully");

        LOGGER.info("SyncTest : Verify number of targets:");
        verifyNumberOfTargets(cnaToSync);
        LOGGER.info("SyncTest : Number of targets verification is finished successfully");

        LOGGER.info("SyncTest : Starts test: Replica count in database for aspect");
        testReplicaCountInDatabaseForAspect(user, cnaToSync);
    }

    private static void testReplicaCountInDatabaseForAspect(User user, EvnfmCnf cnfToScale) {
        if (cnfToScale.getScaleMapping() != null) {
            cnfToScale.getScaleMapping().forEach(scaleMapping ->
                                                         verifyReplicaCountInDatabaseForAspect(user,
                                                                                               cnfToScale.getVnfInstanceName(),
                                                                                               scaleMapping.getAspectId(),
                                                                                               scaleMapping.getTargetToReplicasMap()
                                                                                                       .values()
                                                                                                       .stream()
                                                                                                       .findFirst()
                                                                                                       .get(),
                                                                                               scaleMapping.getTargetToReplicasMap()
                                                                                                       .keySet()
                                                                                                       .toArray(new String[0]))
            );
        }
    }

    private static String executeSyncRequest(EvnfmCnf cnaToSync, User user) {
        Optional<VnfInstanceResponse> vnfInstanceResponse = queryVnfInstance(cnaToSync.getVnfInstanceName(), user);
        URILink sync = null;
        if (vnfInstanceResponse.isPresent()) {
            cnaToSync.setVnfInstanceResponseLinks(vnfInstanceResponse.get().getLinks());
            sync = vnfInstanceResponse.get().getLinks().getSelf();
        }
        LOGGER.info("SYNC link is: {}", sync);
        HttpHeaders httpHeaders = getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SyncVnfRequest> syncVnfRequest = getSyncHttpEntity(cnaToSync, user);
        ResponseEntity<String> response = executeOperationWithLogs(sync.getHref() + "/sync", HttpMethod.POST, syncVnfRequest, String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(202);
        LOGGER.info("CNA SYNC request has been accepted");

        return response.getHeaders().get(HttpHeaders.LOCATION).get(0);
    }

    private static Optional<VnfInstanceResponse> queryVnfInstance(String vnfInstanceName, User user) {
        return Optional.ofNullable(getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                           vnfInstanceName, user));
    }

    private static HttpEntity<SyncVnfRequest> getSyncHttpEntity(final EvnfmCnf cnaToSync, User user) {
        HttpHeaders httpHeaders = createHeaders(user);
        SyncVnfRequest syncVnfRequest = createSyncVnfRequestBody(cnaToSync);
        LOGGER.info("SYNC Request body is: {}", syncVnfRequest);
        return new HttpEntity<>(syncVnfRequest, httpHeaders);
    }

    private static SyncVnfRequest createSyncVnfRequestBody(final EvnfmCnf cnaToSync) {
        SyncVnfRequest request = new SyncVnfRequest();
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(APPLICATION_TIME_OUT, cnaToSync.getApplicationTimeout());
        request.setAdditionalParams(additionalParams);
        return request;
    }
}
