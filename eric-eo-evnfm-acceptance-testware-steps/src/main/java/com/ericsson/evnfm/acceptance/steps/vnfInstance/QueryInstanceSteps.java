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
package com.ericsson.evnfm.acceptance.steps.vnfInstance;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.createHeaders;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.executeOperationWithLogs;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.VnfInstanceVerify.verifyCnfInstanceResponse;
import static com.ericsson.evnfm.acceptance.utils.Constants.COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_LIFECYCLE_OCC_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.GET_RESOURCES_URI;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps;
import com.ericsson.vnfm.orchestrator.model.ResourceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class QueryInstanceSteps {

    private static final String FIRST_LINK_SEPARATOR = "<";
    private static final String SECOND_LINK_SEPARATOR = ">";
    private static final String NEXT_LINK_RELATION = "rel=\"next\"";

    private QueryInstanceSteps() {
    }

    public static void queryAllVNFInstancesPageByPage(User user, List<EvnfmBasePackage> packages, String clusterConfigPath) {
        List<ResourceResponse> resourceResponses = Arrays.asList(Objects.requireNonNull(queryAllResources(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                                          user).getBody()));
        Map<String, String> instanceIdToNamespaceMap = resourceResponses.stream().collect(Collectors.toMap(ResourceResponse::getInstanceId,
                                                                                                           ResourceResponse::getNamespace));
        String query = "size=10";

        do {
            ResponseEntity<VnfInstanceLegacyResponse[]> response = queryAllVNFInstances(EVNFM_INSTANCE.getEvnfmUrl(), user, query);
            Arrays.stream(Objects.requireNonNull(response.getBody()))
                    .filter(instance -> instance.getInstantiationState().equals(VnfInstanceResponse.InstantiationStateEnum.INSTANTIATED))
                    .forEach(instance -> verifyCnfInstanceResponse(instance,
                                                                   packages,
                                                                   clusterConfigPath,
                                                                   instanceIdToNamespaceMap.get(instance.getId())));
            Optional<String> queryParams = parseQueryParamsFromHttpHeaders(response.getHeaders());
            query = queryParams.orElse("");
        } while (StringUtils.isNotBlank(query));
    }

    public static void queryAllLcmOperationsPageByPage(User user) {
        String query = "size=10";
        do {
            ResponseEntity<VnfLcmOpOcc[]> response = queryAllLcmOccurrences(EVNFM_INSTANCE.getEvnfmUrl(), user, query);
            Optional<String> queryParams = parseQueryParamsFromHttpHeaders(response.getHeaders());
            query = queryParams.orElse("");
        } while (StringUtils.isNotBlank(query));
    }

    public static ResponseEntity<VnfInstanceLegacyResponse[]> queryAllVNFInstances(String host, User user, String query) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        String uri = UriComponentsBuilder.fromHttpUrl(host)
                .path(COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS)
                .query(query)
                .build().toUriString();

        return executeOperationWithLogs(uri, HttpMethod.GET,
                                        requestEntity, VnfInstanceLegacyResponse[].class);
    }

    public static ResponseEntity<VnfLcmOpOcc[]> queryAllLcmOccurrences(final String host, User user, String query) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        String uri = UriComponentsBuilder.fromHttpUrl(host)
                .path(ETSI_LIFECYCLE_OCC_URI)
                .query(query)
                .build().toUriString();
        return executeOperationWithLogs(uri, HttpMethod.GET, entity, VnfLcmOpOcc[].class);
    }

    public static ResponseEntity<ResourceResponse[]> queryAllResources(final String host, User user) {
        HttpEntity<String> entity = new HttpEntity<>(createHeaders(user));
        String uri = UriComponentsBuilder.fromHttpUrl(host)
                .path(GET_RESOURCES_URI)
                .build().toUriString();
        return executeOperationWithLogs(uri, HttpMethod.GET, entity, ResourceResponse[].class);
    }

    private static Optional<String> parseQueryParamsFromHttpHeaders(HttpHeaders httpHeaders) {
        List<String> links = httpHeaders.get(HttpHeaders.LINK);
        if (CollectionUtils.isEmpty(links)) {
            return Optional.empty();
        }
        return Arrays.stream(links.get(0).split(FIRST_LINK_SEPARATOR))
                .filter(link -> link.contains(NEXT_LINK_RELATION))
                .map(link -> link.split(SECOND_LINK_SEPARATOR))
                .map(link -> URI.create(link[0]))
                .map(URI::getQuery)
                .findFirst();
    }
}
