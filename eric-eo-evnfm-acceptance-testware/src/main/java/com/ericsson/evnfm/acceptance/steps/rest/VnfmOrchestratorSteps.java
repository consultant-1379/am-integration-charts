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
package com.ericsson.evnfm.acceptance.steps.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.APPLICATION_TIME_OUT;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CLEAN_UP_RESOURCES;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CLEAN_UP_URL;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CREATE_IDENTIFER_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.GET_RESOURCE_INFO_URL;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.NAMESPACE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.PERSIST_SCALE_INFO;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.QUERY_VNFC_SCALE_INFO_URL;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.SKIP_VERIFICATION;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.delay;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getFileResource;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.returnResponseEntityWithLogs;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getGatewayUrl;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getRestUpgradePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getUiUpgradePackage;
import static com.ericsson.evnfm.acceptance.utils.Constants.DEPLOYMENT;
import static com.ericsson.evnfm.acceptance.utils.Constants.REPLICASET;
import static com.ericsson.evnfm.acceptance.utils.Constants.REST;
import static com.ericsson.evnfm.acceptance.utils.Constants.STATEFULSET;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.COMPLETED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationStateEnum.FAILED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigTerminate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;
import com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPIClient;
import com.ericsson.vnfm.orchestrator.model.ChangeCurrentVnfPkgRequest;
import com.ericsson.vnfm.orchestrator.model.CreateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.InstantiateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.InstantiatedVnfInfo;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.TerminateVnfRequest;
import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum;
import com.ericsson.vnfm.orchestrator.model.VnfResource;
import com.ericsson.vnfm.orchestrator.model.VnfcScaleInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VnfmOrchestratorSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(VnfmOrchestratorSteps.class);
    private static String resourceId;
    private static String QUERY_RESPONSE_LOG = "Query response is: {}";

    /**
     * @param url               the url to execute
     * @param appPkgId          the appPkgId to instantiate
     * @param configInstantiate the config object for instantiate
     * @return
     */
    public static VnfInstanceResponseLinks createIdentifier(String url, String appPkgId, ConfigInstantiate configInstantiate) {
        String vnfInstanceName = configInstantiate.getReleaseName();
        HttpHeaders httpHeaders = createHeaders();
        CreateVnfRequest createVnfRequest = new CreateVnfRequest();
        createVnfRequest.setVnfdId(appPkgId);
        createVnfRequest.setVnfInstanceName(vnfInstanceName);
        HttpEntity<CreateVnfRequest> request = new HttpEntity<>(createVnfRequest, httpHeaders);
        LOGGER.info("Performing create identifer request {}\n", url);
        ResponseEntity<VnfInstanceResponse> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().postForEntity(url, request, VnfInstanceResponse.class));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("vnfdId", appPkgId)
                .hasFieldOrPropertyWithValue("vnfInstanceName", vnfInstanceName);
        LOGGER.info("Identifier created successfully\n");
        return response.getBody().getLinks();
    }

    /**
     * @param links             list of url links
     * @param configInstantiate the config object for instantiate
     * @return
     */
    public static String instantiate(VnfInstanceResponseLinks links, ConfigInstantiate configInstantiate,
                                     boolean isContainerStatusVerification, String valuesFilePart) {
        URILink instantiate = links.getInstantiate();
        HttpEntity request = getInstantiateHttpEntity(configInstantiate, isContainerStatusVerification, valuesFilePart,
                instantiate, "Performing instantiate request {}\n");
        ResponseEntity<Void> response = getRestRetryTemplate()
                .execute(context -> getRestTemplate().postForEntity(instantiate.getHref(), request, Void.class));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        LOGGER.info("VNF instantiated successfully\n");
        return response.getHeaders().get(HttpHeaders.LOCATION).get(0);
    }

    public static void instantiateBadRequest(VnfInstanceResponseLinks links, ConfigInstantiate configInstantiate,
            boolean isContainerStatusVerification, String valuesFilePart) {
        URILink instantiate = links.getInstantiate();
        HttpEntity request = getInstantiateHttpEntity(configInstantiate, isContainerStatusVerification, valuesFilePart,
                instantiate, "Performing instantiate request {}\n");
        try {
            getRestTemplate().postForEntity(instantiate.getHref(), request, Void.class);
        } catch (HttpClientErrorException hce) {
            assertThat(hce.getResponseBodyAsString()).contains("Cluster config file bad_cluster");
            assertThat(hce.getResponseBodyAsString()).contains("not found");
            return;
        }
    }

    private static HttpEntity getInstantiateHttpEntity(final ConfigInstantiate configInstantiate,
            final boolean isContainerStatusVerification, final String valuesFilePart,
            final URILink instantiate, final String logMessage) {
        HttpHeaders httpHeaders = createHeaders();
        InstantiateVnfRequest instantiateVnfRequest = createInstantiateVnfRequestBody(configInstantiate,
                isContainerStatusVerification);
        HttpEntity request;
        if (valuesFilePart != null) {
            request = getHttpEntity(valuesFilePart, httpHeaders, instantiateVnfRequest);
        } else {
            request = getHttpEntity(null, httpHeaders, instantiateVnfRequest);
        }
        LOGGER.info(logMessage, instantiate.getHref());
        return request;
    }

    /**
     * Method to verify that the {@link VnfInstanceResponse} matches what is on the system.
     *
     * All fields in the ETSI Spec will be compared.
     * Links are excluded from the comparison as we will be adding new links as time goes on.
     * clusterName is not in the ETSI spec, however it is an important field so will be verified.
     *
     * @param vnfInstanceResponse
     * @param vnfInstanceResponseLinks
     */
    public static void verifyInstanceResponseMatchesSystem(final VnfInstanceResponse vnfInstanceResponse,
            final VnfInstanceResponseLinks vnfInstanceResponseLinks) {
        final RecursiveComparisonConfiguration recursiveComparisonConfiguration =
                new RecursiveComparisonConfiguration();
        recursiveComparisonConfiguration.ignoreFields("links");
        if(vnfInstanceResponse != null && vnfInstanceResponse.getVnfInstanceDescription() != null &&
                vnfInstanceResponse.getVnfInstanceDescription().equals("[20.09]")) {
            //Ignoring fields which have a non-backward compatible comparison with 20.09 release
            LOGGER.info("Found description matching '[20.09], ignoring certain fields which are not compatible with 20.09'");
            recursiveComparisonConfiguration.ignoreFields("vnfInstanceDescription", "instantiatedVnfInfo");
        }
        assertThat(vnfInstanceResponse).usingRecursiveComparison(recursiveComparisonConfiguration).isEqualTo(queryVnfInstance(
                vnfInstanceResponseLinks
                        .getSelf().getHref()));
    }

    /**
     * Method to upgrade a vnf.
     *
     * @param links         list of url links
     * @param configUpgrade the config object for upgrade
     * @return
     */
    public static String upgradeVnf(VnfInstanceResponseLinks links, final ConfigUpgrade configUpgrade,
           String valuesFilePart, final String testType, boolean persistScaleInfo) {
        URILink changePackageInfo = links.getChangeVnfpkg();
        HttpHeaders httpHeaders = createHeaders();
        ChangeCurrentVnfPkgRequest changePackageInfoVnfRequest = createChangePackageInfoVnfRequestBody(configUpgrade, testType, persistScaleInfo);
        assertThat(changePackageInfoVnfRequest.getVnfdId()).isNotNull().withFailMessage("Upgrade package not found");
        HttpEntity<ChangeCurrentVnfPkgRequest> entity;
        if (valuesFilePart != null) {
            entity = getHttpEntity(valuesFilePart, httpHeaders, changePackageInfoVnfRequest);
        } else {
            entity = getHttpEntity(null, httpHeaders, changePackageInfoVnfRequest);
        }
        LOGGER.info("Performing change package info request {}\n", changePackageInfo.getHref());
        final ResponseEntity<String> response = returnResponseEntityWithLogs(changePackageInfo.getHref(),
                                                                             HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode().value())
                .withFailMessage("Upgrade request was not accepted: %s", response.getBody()).isEqualTo(202);
        LOGGER.info("VNF changed package successfully\n");
        return response.getHeaders().get(HttpHeaders.LOCATION).get(0);
    }

    /**
     * Method to scale a vnf.
     *
     * @param links       list of url links
     * @param configScale the config object for scale
     * @return
     */
    public static String scaleVnf(VnfInstanceResponseLinks links, final ConfigScale configScale, String scaleType, String aspectId) {
        URILink scaleLink = links.getScale();
        HttpHeaders httpHeaders = createHeaders();
        ScaleVnfRequest scaleVnfRequest = createScaleVnfRequestBody(configScale, aspectId);
        Map<String, Object> map = getObjectMapper().convertValue(scaleVnfRequest, new TypeReference<>() {
        });
        map.put("type", scaleType);
        HttpEntity<String> entity = new HttpEntity<>(new JSONObject(map).toString(), httpHeaders);
        LOGGER.info("Performing scale vnf request {}\n", scaleLink.getHref());
        final ResponseEntity<String> response = returnResponseEntityWithLogs(scaleLink.getHref(), HttpMethod.POST,
                                                                             entity, String.class);
        assertThat(response.getStatusCode().value())
                .withFailMessage("Scale request was not accepted: %s", response.getBody()).isEqualTo(202);
        LOGGER.info("VNF scaled successfully\n");
        return response.getHeaders().get(HttpHeaders.LOCATION).get(0);
    }

    /**
     * Method to terminate a vnf.
     *
     * @param links           list of url links
     * @param configTerminate the config object for terminate
     * @return
     */
    public static String terminateVnf(VnfInstanceResponseLinks links, final ConfigTerminate configTerminate) {
        URILink terminate = links.getTerminate();
        LOGGER.info("Terminate link is: {}", terminate);
        HttpHeaders httpHeaders = createHeaders();
        LOGGER.info("Headers are: {}", httpHeaders);
        TerminateVnfRequest terminateVnfRequestBody = createTerminateVnfRequestBody(configTerminate);
        LOGGER.info("Terminate Request body is: {}", terminateVnfRequestBody);
        HttpEntity<TerminateVnfRequest> entity = new HttpEntity<>(terminateVnfRequestBody, httpHeaders);
        LOGGER.info("Performing terminate request {}\n", terminate.getHref());
        final ResponseEntity<String> response = returnResponseEntityWithLogs(terminate.getHref(), HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode().value())
                .withFailMessage("Terminate request was not accepted: %s", response.getBody()).isEqualTo(202);
        LOGGER.info("VNF terminated successfully\n");
        return response.getHeaders().get(HttpHeaders.LOCATION).get(0);
    }

    /**
     * Method to query a life cycle operation.
     *
     * @param url           the url to execute
     * @param expectedState the expected state of the operation occurrence Id
     * @param timeout       the application timeout
     * @param operationName the name of the operation being performed
     */
    public static void queryVnfOperation(final String url, final String expectedState, final String timeout,
                                         final OperationEnum operationName) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        int applicationTimeOut = timeout == null ? 600 : Integer.parseInt(timeout);
        StopWatch stopwatch = StopWatch.createStarted();
        String state = null;
        int count = 0;
        LOGGER.info("Performing query operation request {}\n", url);
        while (stopwatch.getTime(TimeUnit.SECONDS) < applicationTimeOut) {
            final ResponseEntity<VnfLcmOpOcc> response = returnResponseEntityWithLogs(url, HttpMethod.GET, entity, VnfLcmOpOcc.class);
            VnfLcmOpOcc body = response.getBody();
            if (count == 0) {
                LOGGER.info("Operation response is {}", body);
            }
            state = body.getOperationState().toString();
            if (COMPLETED.toString().equals(state) || FAILED.toString().equals(state)) {
                LOGGER.info("Operation response is {}", body);
                assertThat(state).isEqualToIgnoringCase(expectedState);
                assertThat(body.getOperation().toString()).isEqualToIgnoringCase(operationName.toString());
                return;
            }
            delay(5000);
            count++;
        }
        fail(String.format("Life cycle operation state :: %s does not match the expected state :: %s for url :: %s ", state, expectedState, url));
    }

    public static void queryVnfModel(final String url, final String appDescriptorId) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query vnf get request {}\n", url);
        ResponseEntity<VnfInstanceResponse> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse.class);
        LOGGER.info(QUERY_RESPONSE_LOG, response.getBody());
        assertThat(response.getBody()).hasFieldOrPropertyWithValue("vnfdId", appDescriptorId);
    }

    public static void queryVnfState(final String url, final String expectedVnfState, final int timeoutInSeconds, final int delayInMillis) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query vnf get request {} to verify vnfState\n", url);
        StopWatch stopwatch = StopWatch.createStarted();
        InstantiatedVnfInfo.VnfStateEnum vnfState = null;
        while (stopwatch.getTime(TimeUnit.SECONDS) < timeoutInSeconds) {
            ResponseEntity<VnfInstanceResponse> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse.class);
            vnfState = response.getBody().getInstantiatedVnfInfo().getVnfState();
            if (expectedVnfState.equals(vnfState.toString())) {
                LOGGER.info("Found matching VnfState {}", vnfState);
                return;
            }
            delay(delayInMillis);
        }
        LOGGER.error("Couldn't find matching vnfState. {} should have been {}", vnfState, expectedVnfState);
        fail(String.format("Couldn't find matching vnfState in the given time: %s", expectedVnfState));
    }

    public static void queryInstantiationState(final String url, final String expectedInstantiationState, final int timeoutInSeconds, final int delayInMillis) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query vnf get request {} to verify InstantiationState\n", url);
        StopWatch stopwatch = StopWatch.createStarted();
        VnfInstanceResponse.InstantiationStateEnum instantiationState = null;
        while (stopwatch.getTime(TimeUnit.SECONDS) < timeoutInSeconds) {
            ResponseEntity<VnfInstanceResponse> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse.class);
             instantiationState = response.getBody().getInstantiationState();
            if (expectedInstantiationState.equals(instantiationState.toString())) {
                LOGGER.info("Found matching InstantiationState {}", instantiationState);
                return;
            }
            delay(delayInMillis);
        }
        LOGGER.error("Couldn't find matching InstantiationState {} should have been {}", instantiationState, expectedInstantiationState);
        fail(String.format("Couldn't find matching InstantiationState in the given time: %s", expectedInstantiationState));
    }

    public static void queryClusterName(final String url, final String expectedClusterName, final int timeoutInSeconds,
                                      final int delayInMillis) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query vnf get request {} to verify ClusterName\n", url);
        StopWatch stopwatch = StopWatch.createStarted();
        String clusterName = null;
        while (stopwatch.getTime(TimeUnit.SECONDS) < timeoutInSeconds) {
            ResponseEntity<VnfInstanceResponse> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse.class);
            clusterName = response.getBody().getClusterName();
            if (expectedClusterName.equals(clusterName)) {
                LOGGER.info("Found matching Cluster name {}", clusterName);
                return;
            }
            delay(delayInMillis);
        }
        LOGGER.error("Couldn't find matching Cluster name {} should have been {}", clusterName, expectedClusterName);
        fail(String.format("Couldn't find matching Cluster name in the given time: %s", expectedClusterName));
    }

    public static VnfInstanceResponse[] queryVnfInstances() {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        String url = getGatewayUrl() + CREATE_IDENTIFER_URI;
        LOGGER.info("Performing query vnf instance get request {}\n", url);
        ResponseEntity<VnfInstanceResponse[]> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse[].class);
        LOGGER.info(QUERY_RESPONSE_LOG, response.getBody());
        return response.getBody();
    }

    public static VnfInstanceResponse queryVnfInstance(String url) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query vnf instance get request {}\n", url);
        ResponseEntity<VnfInstanceResponse> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, VnfInstanceResponse.class);
        LOGGER.info(QUERY_RESPONSE_LOG, response.getBody());
        return response.getBody();
    }

    public static Map<String, String> queryClusterDetails(Map<String, String> mapToVerify, String type,
                                                                final ConfigCluster configCluster, final ConfigInstantiate configInstantiate) {
        var client = new KubernetesAPIClient(configCluster.getExternalConfigFile(), configInstantiate.getNamespace());
        Map<String, String> currentValues = new HashMap<>();
        for (String resourceName : mapToVerify.keySet()) {
            Integer replicaCount = getReplicaCount(resourceName, type, client);
            if (replicaCount != -1) {
                currentValues.putIfAbsent(resourceName, replicaCount.toString());
            }
        }
        return CollectionUtils.isEmpty(currentValues) ? null : currentValues;
    }

    private static Integer getReplicaCount(final String resourceName, final String type, final KubernetesAPIClient client) {
        switch (type) {
            case REPLICASET:
                return client.getReplicaCount(client.getReplicaSet(resourceName));
            case STATEFULSET:
                return client.getReplicaCount(client.getStatefulSet(resourceName));
            case DEPLOYMENT:
                return client.getReplicaCount(client.getDeployment(resourceName));
            default:
                return -1;
        }
    }

    /**
     * Method to delete identifier.
     *
     * @param links list of url links
     */
    public static void deleteVnfIdentifier(VnfInstanceResponseLinks links) {
        URILink self = links.getSelf();
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing delete identifier request {}\n", self.getHref());
        final ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(self.getHref(), HttpMethod.DELETE, entity, String.class);
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Failed to delete identifier: %s", responseEntity.getBody()).isEqualTo(204);
        LOGGER.info("Identifier deleted successfully\n");
    }

    public static void cleanUpAfterFail(VnfInstanceResponseLinks links) {
        URILink self = links.getSelf();
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query all Vnf instances get request to see failed instance is present\n");
        queryVnfInstances();

        LOGGER.info("Performing cleanUp of failed instantiate resource {}\n", self.getHref() + CLEAN_UP_URL);
        ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(self.getHref() + CLEAN_UP_URL,
                                                                             HttpMethod.POST,
                                                                             entity,
                                                                             String.class);
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(202);
        LOGGER.info("Performing query all Vnf instances after cleanUp request and checking that list of instances is empty \n");
        assertThat(queryVnfInstances()).isEmpty();
    }

    private static InstantiateVnfRequest createInstantiateVnfRequestBody(final ConfigInstantiate configInstantiate,
                                                                         boolean isContainerStatusVerification) {
        InstantiateVnfRequest request = new InstantiateVnfRequest();
        Map<String, Object> instantiateAdditionalMap = getInstantiateAdditionalMap(configInstantiate);
        if (isContainerStatusVerification) {
            setAdditionalParams(instantiateAdditionalMap);
        }
        request.setAdditionalParams(instantiateAdditionalMap);
        if (configInstantiate.getCluster() != null) {
            request.setClusterName(configInstantiate.getCluster());
        }
        return request;
    }

    private static void setAdditionalParams(Map<String, Object> instantiateAdditionalMap) {
        instantiateAdditionalMap.put("eric-cm-mediator.enabled", true);
        instantiateAdditionalMap.put("eric-cm-mediator.ingress.hostname", "spider-app-cm-mediator.hahn062.rnd.gic.ericsson.se");
    }

    private static TerminateVnfRequest createTerminateVnfRequestBody(final ConfigTerminate configTerminate) {
        TerminateVnfRequest request = new TerminateVnfRequest();
        request.setTerminationType(configTerminate.getTerminationTypeEnum());
        Map<String, Object> additionalMap = getTerminateAdditionalMap(configTerminate);
        request.setAdditionalParams(additionalMap);
        return request;
    }

    private static ChangeCurrentVnfPkgRequest createChangePackageInfoVnfRequestBody(
            final ConfigUpgrade configUpgrade, final String testType, boolean persistScaleInfo) {
        ChangeCurrentVnfPkgRequest request = new ChangeCurrentVnfPkgRequest();
        Optional<AppPackageResponse> upgradePackage = getUpgradePackage(testType);
        if (upgradePackage.isPresent()) {
            LOGGER.info("Descriptor ID :: {} ", upgradePackage.get());
            request.setVnfdId(upgradePackage.get().getAppDescriptorId());
        }
        Map<String, Object> additionalMap = getUpgradeAdditionalMap(configUpgrade, persistScaleInfo);
        request.setAdditionalParams(additionalMap);
        return request;
    }

    private static ScaleVnfRequest createScaleVnfRequestBody(
            final ConfigScale configScale, String aspectId) {
        ScaleVnfRequest request = new ScaleVnfRequest();
        request.setAspectId(aspectId);
        request.setNumberOfSteps(Integer.parseInt(configScale.getNumberOfSteps()));
        Map<String, Object> additionalMap = getScaleAdditionalMap(configScale);
        request.setAdditionalParams(additionalMap);
        return request;
    }

    private static Map<String, Object> getInstantiateAdditionalMap(final ConfigInstantiate configInstantiate) {
        Map<String, Object> additionalParams = configInstantiate.getAdditionalAttributes();
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        String namespace = configInstantiate.getNamespace();

        additionalParams.put(NAMESPACE, namespace);
        additionalParams.put(APPLICATION_TIME_OUT, configInstantiate.getApplicationTimeOut());
        additionalParams.put(CLEAN_UP_RESOURCES, configInstantiate.getCleanUpResources());
        additionalParams.put(SKIP_VERIFICATION, configInstantiate.getSkipVerification());
        additionalParams.values().removeAll(Collections.singleton(null));
        return additionalParams;
    }

    private static Map<String, Object> getTerminateAdditionalMap(final ConfigTerminate configTerminate) {
        Map<String, Object> additionalParams = new HashMap<>();
        additionalParams.put(CLEAN_UP_RESOURCES, configTerminate.isCleanUpResources());
        additionalParams.put(APPLICATION_TIME_OUT, configTerminate.getApplicationTimeOut());
        additionalParams.put(SKIP_VERIFICATION, configTerminate.isSkipVerification());
        additionalParams.values().removeAll(Collections.singleton(null));
        return additionalParams;
    }

    public static void verifyMaps(Map<String, String> current, Map<String, String> desired) {
        assertThat(current).containsAllEntriesOf(desired);
    }

    private static Map<String, Object> getUpgradeAdditionalMap(final ConfigUpgrade configUpgrade, boolean persistScaleInfo) {
        Map<String, Object> additionalParams = configUpgrade.getAdditionalAttributes();
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put(APPLICATION_TIME_OUT, configUpgrade.getApplicationTimeOut());
        additionalParams.put(SKIP_VERIFICATION, configUpgrade.isSkipVerification());
        additionalParams.put(PERSIST_SCALE_INFO, persistScaleInfo);
        additionalParams.values().removeAll(Collections.singleton(null));
        return additionalParams;
    }

    private static Map<String, Object> getScaleAdditionalMap(final ConfigScale configScale) {
        Map<String, Object> additionalParams = configScale.getAdditionalAttributes();
        if (additionalParams == null) {
            additionalParams = new HashMap<>();
        }
        additionalParams.put(APPLICATION_TIME_OUT, configScale.getApplicationTimeout());
        additionalParams.values().removeAll(Collections.singleton(null));
        return additionalParams;
    }

    private static HttpHeaders createHeaders() {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        httpHeaders.setContentType(APPLICATION_JSON);
        return httpHeaders;
    }

    /**
     * Method to query the VNFCs of an instance and their current and expected replica counts after scale.
     *
     * @param appPkgId    the appPkgId to query for Vnfc scale info
     * @param queryParams the query parameters to use in retrieving Vnfc scale info
     * @return
     */
    public static Optional<VnfcScaleInfo> getVnfcScaleInfo(final String appPkgId, final String queryParams) {
        setResourceId(appPkgId);
        String url = getGatewayUrl() + String.format(QUERY_VNFC_SCALE_INFO_URL, resourceId, queryParams);
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        LOGGER.info("Performing query VNFC scale info request {}\n", url);
        final ResponseEntity<VnfcScaleInfo[]> response = returnResponseEntityWithLogs(url, HttpMethod.GET, entity, VnfcScaleInfo[].class);
        assertThat(response.getStatusCode().value())
                .withFailMessage("Query Vnfc scale info request was not accepted: %s", response.getBody()).isEqualTo(200);
        LOGGER.info(QUERY_RESPONSE_LOG, response.getBody());
        return Arrays.stream(response.getBody()).findAny();
    }


    public static List<ScaleInfo> getResourceScaleInfo(String url) {
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);

        LOGGER.info("Get resource info {}\n", url);
        ResponseEntity<String> response = returnResponseEntityWithLogs(url, HttpMethod.GET, request, String.class);

        final ObjectMapper mapper = getObjectMapper();
        JsonNode scaleInfoJsonNode = null;
        try {
            scaleInfoJsonNode = mapper.readTree(response.getBody())
                    .get("instantiatedVnfInfo").get("scaleStatus");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return mapper.convertValue(scaleInfoJsonNode, new TypeReference<List<ScaleInfo>>() {
        });
    }

    public static void verifyScaleDataPersistence(boolean scalePesistInfo, List<ScaleInfo> scaleInfoBeforeUpgrade,
                                                  List<ScaleInfo> scaleInfoAfterUpgrade) {
        List<ScaleInfo> expectedResult = new ArrayList<>();
        for (ScaleInfo beforeInfoItem : scaleInfoBeforeUpgrade) {
            for (ScaleInfo afterInfoItem : scaleInfoAfterUpgrade) {
                if (beforeInfoItem.getAspectId().equals(afterInfoItem.getAspectId()) && beforeInfoItem.getScaleLevel() != 0) {
                    expectedResult.add(beforeInfoItem);
                    break;
                }
            }
        }
        if (!scalePesistInfo) {
            for (ScaleInfo expectedItem : expectedResult) {
                expectedItem.setScaleLevel(0);
            }
        }
        assertThat(scaleInfoAfterUpgrade.containsAll(expectedResult)).isTrue().withFailMessage("Scaling data was not persisted");
     }
    private static void setResourceId(String appPkgId) {
        String resourcesUrl = getGatewayUrl() + "/vnflcm/v1/resources/";
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        final ResponseEntity<VnfResource[]> response = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(resourcesUrl, HttpMethod.GET, entity, VnfResource[].class));
        Optional<VnfResource> vnfResource = Arrays.stream(response.getBody()).filter(resource -> resource.getVnfdId().equals(appPkgId)).findAny();
        if (vnfResource.isPresent()) {
            resourceId = vnfResource.get().getInstanceId();
        } else {
            throw new NullPointerException(String.format("Request for Vnfc scale info failed, no resource found for instance Id %s", appPkgId));
        }
    }

    private static Optional<AppPackageResponse> getUpgradePackage(final String testType) {
        if (testType.equals(REST)) {
            return getRestUpgradePackage();
        }
        return getUiUpgradePackage();
    }

    private static HttpEntity getHttpEntity(String valuesFilePart, HttpHeaders httpHeaders, Object request) {
        HttpEntity requestEntity;

        if (valuesFilePart != null) {
            FileSystemResource fileSystemResource = getFileResource(valuesFilePart);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            String requestJsonPart = request instanceof InstantiateVnfRequest ? "instantiateVnfRequest" : "changeCurrentVnfPkgRequest";
            body.add(requestJsonPart, request);
            body.add("valuesFile", fileSystemResource);
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            requestEntity = new HttpEntity<>(body, httpHeaders);
        } else {
            requestEntity = new HttpEntity<>(request, httpHeaders);
        }
        return requestEntity;
    }

    public static Map<String,Object> getResourceInfoById(String resourceID){
        HttpHeaders httpHeaders = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);
        String url = getGatewayUrl() + String.format(GET_RESOURCE_INFO_URL, resourceID);

        LOGGER.info("Get resource info {}\n", url);
        ResponseEntity<String> response= returnResponseEntityWithLogs(url, HttpMethod.GET, request, String.class);

        final ObjectMapper mapper = getObjectMapper();
        JsonNode additionalParamJsonNode = null;
        try {
            final String operationParamsValue = mapper.readTree(response.getBody())
                    .get("lcmOperationDetails").get(0).get("operationParams").textValue();
            additionalParamJsonNode = mapper.readTree(operationParamsValue).get("additionalParams");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return mapper.convertValue(additionalParamJsonNode, new TypeReference<>() {
        });
    }
}

