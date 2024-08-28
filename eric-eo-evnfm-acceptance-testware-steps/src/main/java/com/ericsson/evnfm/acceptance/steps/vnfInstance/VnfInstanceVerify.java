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

import static java.util.concurrent.TimeUnit.SECONDS;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.QUERY_INSTANCES_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesApiClient;
import com.ericsson.vnfm.orchestrator.model.ComputeResource;
import com.ericsson.vnfm.orchestrator.model.InstantiatedVnfInfo;
import com.ericsson.vnfm.orchestrator.model.McioInfo;
import com.ericsson.vnfm.orchestrator.model.VimLevelAdditionalResourceInfo;
import com.ericsson.vnfm.orchestrator.model.VimLevelAdditionalResourceInfoRel4;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfcResourceInfo;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;

public class VnfInstanceVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfInstanceVerify.class);

    private VnfInstanceVerify() {
    }

    public static void verifyCnfInstanceResponse(VnfInstanceResponse response, List<EvnfmBasePackage> packages,
                                                 String clusterConfigPath, String namespace) {
        boolean isRel4 = packages.stream()
                .filter(basePackage -> basePackage.getVnfdId().equals(response.getVnfdId()))
                .anyMatch(EvnfmBasePackage::isRel4);
        if (isRel4) {
            verifyRel4CnfInstanceResponse(response, clusterConfigPath, namespace);
        } else {
            verifyLegacyCnfInstanceReturnedInResponse(response, clusterConfigPath);
        }
    }

    public static void verifyRel4CnfInstanceResponse(VnfInstanceResponse response, String clusterLocalPath, String namespace) {
        KubernetesApiClient client = new KubernetesApiClient(clusterLocalPath);
        List<McioInfo> mcioInfoList = response.getInstantiatedVnfInfo().getMcioInfo();
        List<Deployment> deployments = client.getDeployments(namespace);
        List<StatefulSet> statefulSets = client.getStatefulSets(namespace);
        List<VnfcResourceInfo> vnfcResourcesInfo = response.getInstantiatedVnfInfo().getVnfcResourceInfo();
        verifyRel4VnfInstanceHostname(vnfcResourcesInfo, client);
        verifyMcioInfoResponse(vnfcResourcesInfo, mcioInfoList, deployments, statefulSets, response, namespace);
    }

    public static void verifyLegacyCnfInstanceReturnedInResponse(VnfInstanceResponse response, String clusterLocalPath) {
        KubernetesApiClient client = new KubernetesApiClient(clusterLocalPath);
        InstantiatedVnfInfo instantiatedVnfInfo = response.getInstantiatedVnfInfo();

        assertThat(instantiatedVnfInfo).isNotNull();
        LOGGER.info("Fetching mcioinfo to verify data is null.");
        assertThat(instantiatedVnfInfo.getMcioInfo()).isNull();
        List<VnfcResourceInfo> vnfcResourcesInfo = instantiatedVnfInfo.getVnfcResourceInfo();
        verifyLegacyVnfInstanceHostname(vnfcResourcesInfo, client);
    }

    private static Callable<Boolean> podIsCreated(KubernetesApiClient client, String namespace, String podName) {
        return () -> client.getPod(namespace, podName) != null;
    }

    private static void verifyMcioInfoResponse(List<VnfcResourceInfo> vnfResourceInfoList,
                                               List<McioInfo> mcioInfoList,
                                               List<Deployment> deployments,
                                               List<StatefulSet> statefulSets,
                                               VnfInstanceResponse response,
                                               String namespace) {

        Set<String> vduIds = vnfResourceInfoList.stream().map(VnfcResourceInfo::getVduId).collect(Collectors.toSet());

        for (McioInfo mcioInfo : mcioInfoList) {
            if (mcioInfo.getDesiredInstances() > 0) {
                assertThat(mcioInfo.getVduId()).matches(vduIds::contains);
            }
            assertThat(mcioInfo.getCismId()).isEqualTo(response.getClusterName());
            assertThat(mcioInfo.getMcioNamespace()).isEqualTo(namespace);
            if (mcioInfo.getMcioType().equals(McioInfo.McioTypeEnum.DEPLOYMENT)) {
                Optional<Deployment> deployment = deployments.stream()
                        .filter(currentDeployment -> currentDeployment.getMetadata().getName().equals(mcioInfo.getMcioName()))
                        .findFirst();

                assertThat(deployment)
                        .isNotEmpty()
                        .hasValueSatisfying(deploymentPresent -> {
                                                assertThat(mcioInfo.getMcioId()).isEqualTo("Deployment/" + mcioInfo.getMcioName());
                                                assertThat(mcioInfo.getMcioType().toString()).isEqualTo("Deployment");
                                                assertThat(mcioInfo.getDesiredInstances()).isEqualTo(deploymentPresent.getSpec().getReplicas());
                                                assertThat(mcioInfo.getAvailableInstances())
                                                        .isEqualTo(defaultIfNull(deploymentPresent.getStatus().getAvailableReplicas(), 0));
                                            }
                        );
            } else {
                Optional<StatefulSet> statefulSet = statefulSets.stream()
                        .filter(currentStatefulset -> currentStatefulset.getMetadata().getName().equals(mcioInfo.getMcioName()))
                        .findFirst();
                assertThat(statefulSet)
                        .isNotEmpty()
                        .hasValueSatisfying(statefulSetPresent -> {
                                                assertThat(mcioInfo.getMcioId()).isEqualTo("Statefulset/" + mcioInfo.getMcioName());
                                                assertThat(mcioInfo.getMcioType().toString()).isEqualTo("Statefulset");
                                                assertThat(mcioInfo.getDesiredInstances()).isEqualTo(statefulSetPresent.getSpec().getReplicas());
                                                assertThat(mcioInfo.getAvailableInstances())
                                                        .isEqualTo(defaultIfNull(statefulSetPresent.getStatus().getAvailableReplicas(), 0));
                                            }
                        );
            }
        }
    }

    private static void verifyLegacyVnfInstanceHostname(List<VnfcResourceInfo> vnfResourceInfoList, KubernetesApiClient client) {
        vnfResourceInfoList.forEach(resourceInfo -> {
            ComputeResource computeResource = resourceInfo.getComputeResource();
            String podName = computeResource.getResourceId();
            final VimLevelAdditionalResourceInfo vimLevelAdditionalResourceInfo =
                    deserialize(computeResource.getVimLevelAdditionalResourceInfo(), VimLevelAdditionalResourceInfo.class);

            String namespace = vimLevelAdditionalResourceInfo.getNamespace();

            await().atMost(180, SECONDS).until(podIsCreated(client, namespace, podName));
            Pod pod = client.getPod(namespace, podName);

            LOGGER.info("Fetching hostname for pod: {} to verify data.", podName);
            assertThat(vimLevelAdditionalResourceInfo.getHostname()).isEqualTo(pod.getSpec().getNodeName());
        });
    }

    private static void verifyRel4VnfInstanceHostname(List<VnfcResourceInfo> vnfResourceInfoList, KubernetesApiClient client) {
        vnfResourceInfoList.forEach(resourceInfo -> {
            ComputeResource computeResource = resourceInfo.getComputeResource();
            String podName = computeResource.getResourceId();
            final VimLevelAdditionalResourceInfoRel4 vimLevelAdditionalResourceInfoRel4 =
                    deserialize(computeResource.getVimLevelAdditionalResourceInfo(), VimLevelAdditionalResourceInfoRel4.class);

            String namespace = vimLevelAdditionalResourceInfoRel4.getAdditionalInfo().getNamespace();

            await().atMost(180, SECONDS).until(podIsCreated(client, namespace, podName));
            Pod pod = client.getPod(namespace, podName);

            LOGGER.info("Fetching hostname for pod: {} to verify data.", podName);
            assertThat(vimLevelAdditionalResourceInfoRel4.getHostname()).isEqualTo(pod.getSpec().getNodeName());
        });
    }

    public static void verifyCnfInstancesReturnedInResponse(ResponseEntity<List<VnfInstanceLegacyResponse>> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, QUERY_INSTANCES_OPERATION, HttpStatus.OK);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        for (VnfInstanceResponse vnfInstance : response.getBody()) {
            assertThat(vnfInstance).isInstanceOf(VnfInstanceResponse.class);
        }
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, QUERY_INSTANCES_OPERATION);
    }

    private static <T> T deserialize(final Object vimLevelAdditionalResourceInfo, final Class<T> clazz) {
        try {
            return getObjectMapper().readValue(getObjectMapper().writeValueAsString(vimLevelAdditionalResourceInfo), clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(String.format("Failed to read value from :: %s", vimLevelAdditionalResourceInfo.toString()));
        }
    }
}
