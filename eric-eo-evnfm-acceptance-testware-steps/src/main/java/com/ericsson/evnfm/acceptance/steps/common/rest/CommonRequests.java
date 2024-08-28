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
package com.ericsson.evnfm.acceptance.steps.common.rest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertTrue;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.delay;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyDay0SecretsAfterVnfLcmOperationWasCompleted;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyDay0SecretsWhileVnfLcmOperationIsInProgress;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_LIFECYCLE_OCC_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_VNF_INSTANCE_URL_QUERY_PACKAGE_ID;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_VNF_INSTANCE_URL_QUERY_RELEASE;
import static com.ericsson.evnfm.acceptance.utils.Constants.GET_RESOURCE_BY_ID_URI;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.COMPLETED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.FAILED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.FAILED_TEMP;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.ROLLED_BACK;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.InstantiateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.ResourceResponse;
import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class CommonRequests {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRequests.class);

    public static final String VALUES_FILE = "valuesFile";
    private static final List<VnfLcmOpOcc.OperationStateEnum> TERMINATED_VNF_LCM_OPP_OCC_STATUS_LIST = Arrays.asList(COMPLETED, FAILED,
                                                                                                                     ROLLED_BACK, FAILED_TEMP);

    private CommonRequests() {
    }

    /**
     * Method to query a vnf instantiation state.
     *
     * @param link the url to execute
     * @param user the user to login with
     * @return VnfInstanceResponse
     */
    public static VnfInstanceResponse getVnfInstanceByLink(String link, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        ResponseEntity<VnfInstanceResponse> response = executeOperationWithLogs(link, HttpMethod.GET, request, VnfInstanceResponse.class);
        return response.getBody();
    }

    public static List<VnfInstanceLegacyResponse> getAllVNFInstancesByPackageId(String vnfPkgId, User user) {
        String url = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ETSI_VNF_INSTANCE_URL_QUERY_PACKAGE_ID, vnfPkgId);

        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<VnfInstanceLegacyResponse[]> responseEntity = getRestRetryTemplate()
                .execute(context -> getRestTemplate().exchange(url, HttpMethod.GET,
                                                               requestEntity, VnfInstanceLegacyResponse[].class));

        return responseEntity.getBody() != null ? Arrays.asList(responseEntity.getBody()) : Collections.emptyList();
    }

    /**
     * Method to query a life cycle operation.
     *
     * @param url     the url to execute
     * @param release the release name to find
     * @param user    the user to login with
     */
    public static VnfInstanceLegacyResponse getVNFInstanceByRelease(String url, String release, User user) {
        return getAllVNFInstancesByRelease(url, release, user)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public static List<VnfInstanceLegacyResponse> getAllVNFInstancesByRelease(String url, String release, User user) {
        String releaseQuery = String.format(ETSI_VNF_INSTANCE_URL_QUERY_RELEASE, release);
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<VnfInstanceLegacyResponse[]> responseEntity = getRestRetryTemplate()
                .execute(context -> getRestTemplate().exchange(url + releaseQuery, HttpMethod.GET,
                                                               requestEntity, VnfInstanceLegacyResponse[].class));

        return Arrays.asList(responseEntity.getBody());
    }

    /**
     * Method to query life cycle occurrences by vnf instance name.
     *
     * @param host            the host of evnfm deployment
     * @param vnfInstanceName the name of the vnf instance for life cycle occurrences
     * @param user            the user information for headers
     * @return Optional<List < VnfLcmOpOcc>>
     */
    public static Optional<List<VnfLcmOpOcc>> getLcmOccurrencesByVnfInstanceName(final String host, final String vnfInstanceName, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);

        Optional<VnfInstanceLegacyResponse> vnfInstance = Optional.ofNullable(getVNFInstanceByRelease(host, vnfInstanceName, user));
        return vnfInstance.map(VnfInstanceLegacyResponse::getId).map(id -> getLcmOccurrencesByVnfInstanceId(host, entity, id));
    }

    /**
     * Method to query life cycle occurrences by vnf instance name.
     *
     * @param host  the host of evnfm deployment
     * @param vnfId the id of the vnf instance for life cycle occurrences
     * @param user  the user information for headers
     * @return Optional<List < VnfLcmOpOcc>>
     */
    public static List<VnfLcmOpOcc> getLcmOccurrencesByVnfInstanceId(final String host, final String vnfId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        return getLcmOccurrencesByVnfInstanceId(host, entity, vnfId);
    }

    private static List<VnfLcmOpOcc> getLcmOccurrencesByVnfInstanceId(final String host, HttpEntity<String> entity, final String vnfId) {
        String uri = UriComponentsBuilder.fromHttpUrl(host)
                .path(ETSI_LIFECYCLE_OCC_URI)
                .queryParam("filter", String.format("(eq,vnfInstanceId,%s)", vnfId))
                .build().toUriString();
        LOGGER.info("Performing query operation request {}\n", host);
        final ResponseEntity<VnfLcmOpOcc[]> occurrencesResponse = executeOperationWithLogs(uri, HttpMethod.GET, entity, VnfLcmOpOcc[].class);
        return Arrays.asList(Objects.requireNonNull(occurrencesResponse.getBody()));
    }

    public static ResourceResponse getResourceByVnfInstanceId(final String host, final String vnfId, User user) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(user));
        String uri = UriComponentsBuilder.fromHttpUrl(host)
                .path(String.format(GET_RESOURCE_BY_ID_URI, vnfId))
                .build().toUriString();
        LOGGER.info("Performing query resource request {}\n", uri);
        final ResponseEntity<ResourceResponse> occurrencesResponse = executeOperationWithLogs(uri, HttpMethod.GET, entity, ResourceResponse.class);
        return Objects.requireNonNull(occurrencesResponse.getBody());
    }

    /**
     * Get HttpEntity for Instantiate and Upgrade
     *
     * @param valuesMap   content of values.yaml
     * @param httpHeaders headers to use
     * @param request     the request body to use
     * @return a HttpEntity that can be used in a REST request
     */
    public static HttpEntity<?> buildHttpEntityInstantiateOrUpgrade(Map<String, Object> valuesMap, HttpHeaders httpHeaders, Object request) {
        HttpEntity<?> requestEntity;

        if (valuesMap != null) {
            FileSystemResource fileSystemResource = CommonHelper.getFileResource(valuesMap);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            String requestJsonPart = request instanceof InstantiateVnfRequest ? "instantiateVnfRequest" : "changeCurrentVnfPkgRequest";
            body.add(requestJsonPart, request);
            body.add(VALUES_FILE, fileSystemResource);
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            requestEntity = new HttpEntity<>(body, httpHeaders);
        } else {
            requestEntity = new HttpEntity<>(request, httpHeaders);
        }
        return requestEntity;
    }

    /**
     * Make a request and log the response (debug log)
     *
     * @param url           the url to send the request to
     * @param httpMethod    the Http method
     * @param httpEntity    the HttpEntity
     * @param responseClass the response representation
     * @return the response as a ResponseEntity
     */
    public static <T> ResponseEntity<T> executeOperationWithLogs(
            String url, HttpMethod httpMethod, HttpEntity<?> httpEntity, Class<T> responseClass) {
        long startTime = System.currentTimeMillis();
        ResponseEntity<T> responseEntity = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(url, httpMethod, httpEntity, responseClass));
        long endTime = System.currentTimeMillis();
        long executionTimeInSeconds = (endTime - startTime) / 1000;
        assertTrue(String.format("HTTP call took longer than 25 seconds for url %s, execution time: %d seconds", url,
                                 executionTimeInSeconds),
                   executionTimeInSeconds <= 25);
        LOGGER.debug(responseEntity.toString());
        return responseEntity;
    }

    public static String failLcmOperation(final String lifecycleOperationId, User user) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(user));
        final URILink href = new URILink().href(lifecycleOperationId + "/fail");

        LOGGER.info("Performing fail operation request {}\n", href.getHref());
        final ResponseEntity<String> responseEntity = executeOperationWithLogs(href.getHref(), HttpMethod.POST, entity, String.class);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
        LOGGER.info("Fail operation was accepted\n");

        return lifecycleOperationId;
    }

    /**
     * Query a VNF operation and assert that the response and state are as expected. Will also check day 0 secret information
     *
     * @param operationUrl the lcm operation url
     * @param user         the user that will be used to make the request
     */
    public static ResponseEntity<VnfLcmOpOcc> pollingVnfLcmOperationOccurrence(EvnfmCnf evnfmCnf, User user, String operationUrl,
                                                                               final Day0SecretVerificationInfo day0VerificationInfo) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        int applicationTimeOut = evnfmCnf.getApplicationTimeout() == null ? 600 : Integer.parseInt(evnfmCnf.getApplicationTimeout());
        StopWatch stopwatch = StopWatch.createStarted();
        VnfLcmOpOcc.OperationStateEnum operationState;
        boolean secretsVerified = false;
        int pollingCounter = 0;
        ResponseEntity<VnfLcmOpOcc> response = null;
        LOGGER.info("Performing query operation request {}\n", operationUrl);
        while (stopwatch.getTime(TimeUnit.SECONDS) < applicationTimeOut) {
            response = executeOperationWithLogs(operationUrl, HttpMethod.GET, entity, VnfLcmOpOcc.class);
            VnfLcmOpOcc body = response.getBody();
            assert body != null;
            operationState = body.getOperationState();

            if (pollingCounter == 0) {
                LOGGER.info("Operation response is {}", body);
            } else {
                LOGGER.info("VnfLcmOppOcc executed {} times and returned response with status : {}", pollingCounter, operationState);
            }

            if (TERMINATED_VNF_LCM_OPP_OCC_STATUS_LIST.contains(operationState)) {
                LOGGER.info("Operation response is {}", body);
                if (COMPLETED.equals(operationState)) {
                    verifyDay0SecretsAfterVnfLcmOperationWasCompleted(day0VerificationInfo, secretsVerified);
                }

                break;
            }
            secretsVerified = verifyDay0SecretsWhileVnfLcmOperationIsInProgress(day0VerificationInfo, secretsVerified, applicationTimeOut);

            pollingCounter++;
            if (pollingCounter % 15 == 0) {
                LOGGER.info("Creating new user");
                entity = updateHttpEntity();
                LOGGER.info("New user created {}", user.getUsername());
            }
            delay(15000);
        }
        return response;
    }

    private static HttpEntity<String> updateHttpEntity() {
        User user = Idam.createUser(getConfigGeneral());
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        return new HttpEntity<>(httpHeaders);
    }

    @NotNull
    private static ConfigGeneral getConfigGeneral() {
        ConfigGeneral configGeneral = new ConfigGeneral();
        configGeneral.setIdamHost(EVNFM_INSTANCE.getIdamUrl());
        configGeneral.setIdamRealm(EVNFM_INSTANCE.getIdamRealm());
        configGeneral.setIdamClient(EVNFM_INSTANCE.getIdamClientId());
        configGeneral.setIdamClientSecret(EVNFM_INSTANCE.getIdamClientSecret());
        configGeneral.setIdamAdminUsername(EVNFM_INSTANCE.getIdamAdminUser());
        configGeneral.setIdamAdminPassword(EVNFM_INSTANCE.getIdamAdminPassword());
        configGeneral.setHelmRegistryUrl(EVNFM_INSTANCE.getHelmRegistryUrl());
        return configGeneral;
    }
}
