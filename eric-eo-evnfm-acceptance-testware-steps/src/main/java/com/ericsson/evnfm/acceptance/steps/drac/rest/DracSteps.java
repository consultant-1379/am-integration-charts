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
package com.ericsson.evnfm.acceptance.steps.drac.rest;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.DracConfig;
import com.ericsson.evnfm.acceptance.models.DracConfig.DracRole;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;

public class DracSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(DracSteps.class);

    private static final ObjectMapper MAPPER = getObjectMapper();

    private static final String DRAC_CONFIGMAP_NAME = "eric-evnfm-rbac-drac-configmap";
    private static final String DRAC_CONFIG_JSON_KEY = "drac.config.json";

    private DracSteps() {
    }

    public static void addNodeTypeToRoleInDracConfig(final EvnfmCnf cnf, final String roleName, final String cluster, final String namespace) {
        final KubernetesApiClient kubernetesApiClient = new KubernetesApiClient(cluster);

        final DracConfig dracConfig = readDracConfigFromConfigMap(namespace, kubernetesApiClient);
        final DracRole dracRole = findDracRole(dracConfig, roleName);

        final String nodeType = cnf.getPackageName();

        final List<String> roleNodeTypes = dracRole.getNodeTypes();
        if (roleNodeTypes.contains(nodeType)) {
            LOGGER.info("DRAC config already contains {} node type for role {}. DRAC config: {}", nodeType, roleName, dracConfig);

            return;
        }

        LOGGER.info("Adding {} node type for role {} to DRAC config", nodeType, roleName);

        roleNodeTypes.add(nodeType);

        patchConfigMapWithDracConfig(dracConfig, namespace, kubernetesApiClient);
    }

    public static void removeNodeTypeFromRoleInDracConfig(final EvnfmCnf cnf, final String roleName, final String cluster, final String namespace) {
        final KubernetesApiClient kubernetesApiClient = new KubernetesApiClient(cluster);

        final DracConfig dracConfig = readDracConfigFromConfigMap(namespace, kubernetesApiClient);
        final DracRole dracRole = findDracRole(dracConfig, roleName);

        final String nodeType = cnf.getPackageName();

        final List<String> roleNodeTypes = dracRole.getNodeTypes();
        if (!roleNodeTypes.contains(nodeType)) {
            LOGGER.info("DRAC config already does not contain {} node type for role {}. DRAC config: {}", nodeType, roleName, dracConfig);

            return;
        }

        LOGGER.info("Removing {} node type for role {} from DRAC config", nodeType, roleName);

        roleNodeTypes.remove(nodeType);

        patchConfigMapWithDracConfig(dracConfig, namespace, kubernetesApiClient);
    }

    private static DracConfig readDracConfigFromConfigMap(final String namespace, final KubernetesApiClient kubernetesApiClient) {
        final ConfigMap configMap = kubernetesApiClient.getConfigMap(namespace, DRAC_CONFIGMAP_NAME);

        final Map<String, String> configMapData = configMap.getData();

        if (!configMapData.containsKey(DRAC_CONFIG_JSON_KEY)) {
            throw new RuntimeException(format("DRAC ConfigMap %s does not contain actual DRAC configuration key %s. ConfigMap data: %s",
                                              DRAC_CONFIGMAP_NAME,
                                              DRAC_CONFIG_JSON_KEY,
                                              configMapData));
        }

        final String dracConfigString = configMapData.get(DRAC_CONFIG_JSON_KEY);

        return readDracConfigFrom(dracConfigString);
    }

    private static DracRole findDracRole(final DracConfig dracConfig, final String roleName) {
        final DracRole dracRole = ObjectUtils.<List<DracRole>>firstNonNull(dracConfig.getRoles(), emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(currentRole -> Objects.equals(currentRole.getName(), roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(format("DRAC config does not contain role with name %s. DRAC config: %s",
                                                               roleName,
                                                               dracConfig)));

        if (dracRole.getNodeTypes() == null) {
            dracRole.setNodeTypes(new ArrayList<>());
        }

        return dracRole;
    }

    private static void patchConfigMapWithDracConfig(final DracConfig dracConfig,
                                                     final String namespace,
                                                     final KubernetesApiClient kubernetesApiClient) {

        kubernetesApiClient.patchConfigMap(namespace,
                                           DRAC_CONFIGMAP_NAME,
                                           cm -> new ConfigMapBuilder(cm)
                                                   .addToData(DRAC_CONFIG_JSON_KEY, writeDracConfigFrom(dracConfig))
                                                   .build());
    }

    private static DracConfig readDracConfigFrom(final String dracConfigString) {
        try {
            return MAPPER.readValue(dracConfigString, DracConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(format("Failed to read DRAC configuration from string: %s", dracConfigString));
        }
    }

    private static String writeDracConfigFrom(final DracConfig dracConfig) {
        try {
            return MAPPER.writeValueAsString(dracConfig);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(format("Failed to write DRAC configuration from object: %s", dracConfig));
        }
    }
}
