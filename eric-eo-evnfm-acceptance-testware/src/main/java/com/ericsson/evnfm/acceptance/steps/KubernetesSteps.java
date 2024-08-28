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
package com.ericsson.evnfm.acceptance.steps;

import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryClusterDetails;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.verifyMaps;
import static com.ericsson.evnfm.acceptance.utils.Constants.DEPLOYMENT;
import static com.ericsson.evnfm.acceptance.utils.Constants.REPLICASET;
import static com.ericsson.evnfm.acceptance.utils.Constants.STATEFULSET;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;

public class KubernetesSteps {

    public static boolean checkIfScaleCanBePerformed(final ConfigScale configScale) {
        return (StringUtils.isNotEmpty(configScale.getAspectId()) && (
                !CollectionUtils.isEmpty(configScale.getDeployments()) || !CollectionUtils
                        .isEmpty(configScale.getStatefulSets()) || !CollectionUtils
                        .isEmpty(configScale.getReplicaSets())));
    }

    public static void verifyScale(final ConfigScale configScale,
                             final Map<String, Map<String, String>> actualValues) {
        if (!CollectionUtils.isEmpty(configScale.getDeployments())) {
            verifyMaps(actualValues.get(DEPLOYMENT), configScale.getDeployments());
        }
        if (!CollectionUtils.isEmpty(configScale.getStatefulSets())) {
            verifyMaps(actualValues.get(STATEFULSET), configScale.getStatefulSets());
        }
        if (!CollectionUtils.isEmpty(configScale.getReplicaSets())) {
            verifyMaps(actualValues.get(REPLICASET), configScale.getReplicaSets());
        }
    }

    public static void verifyScaleReset(final Map<String, Map<String, String>> startValues,
                                  final Map<String, Map<String, String>> resetValues) {
        if (!CollectionUtils.isEmpty(startValues.get(DEPLOYMENT))) {
            verifyMaps(startValues.get(DEPLOYMENT), resetValues.get(DEPLOYMENT));
        }
        if (!CollectionUtils.isEmpty(startValues.get(STATEFULSET))) {
            verifyMaps(startValues.get(STATEFULSET), resetValues.get(STATEFULSET));
        }
        if (!CollectionUtils.isEmpty(startValues.get(REPLICASET))) {
            verifyMaps(startValues.get(REPLICASET), resetValues.get(REPLICASET));
        }
    }

    public static Map<String, Map<String, String>> getCurrentValues(final ConfigScale configScale,
                                                                        final ConfigInstantiate configInstantiate, final ConfigCluster configCluster) {
        Map<String, Map<String, String>> currentValues = new HashMap<>();
        Map<String, String> deploymentCurrentValues;
        Map<String, String> statefulSetsCurrentValues;
        Map<String, String> replicaSetsCurrentValues;
        if (configScale.getDeployments() != null) {
            deploymentCurrentValues = queryClusterDetails(configScale.getDeployments(), DEPLOYMENT, configCluster,
                    configInstantiate);
            currentValues.put(DEPLOYMENT, deploymentCurrentValues);
        }
        if (configScale.getStatefulSets() != null) {
            statefulSetsCurrentValues = queryClusterDetails(configScale.getStatefulSets(), STATEFULSET,
                    configCluster, configInstantiate);
            currentValues.put(STATEFULSET, statefulSetsCurrentValues);
        }
        if (configScale.getReplicaSets() != null) {
            replicaSetsCurrentValues = queryClusterDetails(configScale.getReplicaSets(), REPLICASET, configCluster,
                    configInstantiate);
            currentValues.put(REPLICASET, replicaSetsCurrentValues);
        }
        currentValues.values().removeAll(Collections.singleton(null));
        return currentValues;
    }
}
