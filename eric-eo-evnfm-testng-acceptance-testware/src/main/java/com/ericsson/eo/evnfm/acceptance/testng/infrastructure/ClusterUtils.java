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
package com.ericsson.eo.evnfm.acceptance.testng.infrastructure;

import static org.assertj.core.api.Fail.fail;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.delay;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getLcmOccurrencesByVnfInstanceName;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.deleteNamespace;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.deletePVCs;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.getNamespace;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.getNamespaces;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.getPVCs;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class ClusterUtils {
    public static final int NAMESPACE_DELETE_TIMEOUT = 30;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterUtils.class);

    private ClusterUtils() {
    }

    public static void cleanupNamespaces(Collection<EvnfmCnf> cnfs, final User user) {
        cnfs.forEach(evnfmCnf -> {
            String clusterConfig = evnfmCnf.getCluster().getLocalPath();
            String namespace = evnfmCnf.getNamespace();
            boolean isTerminateStillProcessing = checkIfTerminateStillProcessing(evnfmCnf, user);
            if (evnfmCnf.isCleanUpResources() && isTerminateStillProcessing) {
                LOGGER.info("Cleanup for namespace {} is skipped as terminate is still processing for instance :: {}",
                            namespace,
                            evnfmCnf.getVnfInstanceName());
            } else {
                LOGGER.info("Deleting namespace {} for instance :: {}", namespace, evnfmCnf.getVnfInstanceName());
                if (!deleteNamespace(clusterConfig, namespace)) {
                    LOGGER.info("Namespace {} does not exist in {}", namespace, clusterConfig);
                    return;
                }
            }
            StopWatch stopwatch = StopWatch.createStarted();
            while (stopwatch.getTime(TimeUnit.SECONDS) < NAMESPACE_DELETE_TIMEOUT) {
                if (getNamespace(clusterConfig, namespace) == null) {
                    return;
                }
                delay(1000);
            }
            LOGGER.info("Namespace {} has not been deleted in the required time for instance :: {}", namespace, evnfmCnf.getVnfInstanceName());
        });
    }

    public static boolean checkNameSpaceExist(EvnfmCnf evnfmCnf) {
        String clusterConfig = evnfmCnf.getCluster().getLocalPath();
        StopWatch stopwatch = StopWatch.createStarted();
        while (stopwatch.getTime(TimeUnit.SECONDS) < NAMESPACE_DELETE_TIMEOUT) {
            final boolean isNameSpace = getNamespaces(clusterConfig).stream()
                    .anyMatch(config -> evnfmCnf.getNamespace().equals(config.getMetadata().getName()));
            if (!isNameSpace) {
                return Boolean.FALSE;
            }

            delay(1000);
        }

        LOGGER.info("Namespace {} still exist for instance :: {}", evnfmCnf.getNamespace(), evnfmCnf.getVnfInstanceName());
        return Boolean.TRUE;
    }

    public static boolean checkClusterIsReachable(ClusterConfig clusterConfig) {
        String clusterConfigPath = clusterConfig.getLocalPath();
        try {
            getNamespaces(clusterConfigPath);
        } catch (Exception e) {
            LOGGER.error("List namespaces failed with {}", e.getMessage());
            fail("Cluster {} is unreachable: {}", clusterConfig.getName(), e.getMessage());
        }
        LOGGER.info("Cluster {} is reachable", clusterConfig.getName());
        return Boolean.TRUE;
    }

    public static void cleanupResources(String clusterConfig, String namespace) {
        LOGGER.info("Cleaning up all resources in namespace {}", namespace);
        cleanupPVCs(clusterConfig, namespace);
        cleanupNamespace(clusterConfig, namespace);
    }

    private static void cleanupPVCs(String clusterConfig, String namespace) {
        LOGGER.info("Deleting PVCs in namespace {}", namespace);
        if (!deletePVCs(clusterConfig, namespace)) {
            LOGGER.info("PVCs {} do not exist in {}", namespace, clusterConfig);
            return;
        }
        StopWatch stopwatch = StopWatch.createStarted();
        while (stopwatch.getTime(TimeUnit.SECONDS) < NAMESPACE_DELETE_TIMEOUT) {
            if (getPVCs(clusterConfig, namespace) == null) {
                return;
            }
            delay(1000);
        }
        LOGGER.info("PVCs in namespace {} have not been deleted in the required time", namespace);
    }

    private static void cleanupNamespace(String clusterConfig, String namespace) {
        LOGGER.info("Deleting namespace {}", namespace);
        if (!deleteNamespace(clusterConfig, namespace)) {
            LOGGER.info("Namespace {} does not exist in {}", namespace, clusterConfig);
            return;
        }
        StopWatch stopwatch2 = StopWatch.createStarted();
        while (stopwatch2.getTime(TimeUnit.SECONDS) < NAMESPACE_DELETE_TIMEOUT) {
            if (getNamespace(clusterConfig, namespace) == null) {
                return;
            }
            delay(1000);
        }
        LOGGER.info("Namespace {} has not been deleted in the required time", namespace);
    }

    private static boolean checkIfTerminateStillProcessing(final EvnfmCnf evnfmCnf, final User user) {
        Optional<List<VnfLcmOpOcc>> vnfLcmOpOccOptionalList = getLcmOccurrencesByVnfInstanceName(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                                 evnfmCnf.getVnfInstanceName(),
                                                                                                 user);
        return vnfLcmOpOccOptionalList
                .flatMap(vnfLcmOpOccs -> vnfLcmOpOccs.stream()
                        .filter(vnfLcmOpOcc -> vnfLcmOpOcc.getOperation().equals(VnfLcmOpOcc.OperationEnum.TERMINATE))
                        .filter(vnfLcmOpOcc -> vnfLcmOpOcc.getOperationState().equals(VnfLcmOpOcc.OperationStateEnum.PROCESSING))
                        .findFirst()).isPresent();
    }
}
