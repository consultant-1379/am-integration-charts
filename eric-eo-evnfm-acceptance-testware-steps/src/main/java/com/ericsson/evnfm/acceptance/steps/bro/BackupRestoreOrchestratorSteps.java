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
package com.ericsson.evnfm.acceptance.steps.bro;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.utils.HelmUtils;

public class BackupRestoreOrchestratorSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupRestoreOrchestratorSteps.class);
    private static final String SPEC_KEY = "spec";
    private static final String TEMPLATE_KEY = "template";
    private static final String METADATA_KEY = "metadata";
    private static final String LABELS_KEY = "labels";
    private static final String CONTAINERS_KEY = "containers";
    private static final String ENV_KEY = "env";
    private static final String NAME_KEY = "name";
    private static final String VALUE_KEY = "value";
    private static final String ADPBRLABELKEY_LABEL = "adpbrlabelkey";
    private static final String REGISTRATION_AGENT_ID_ENV_VAR = "REGISTRATION_AGENT_ID";

    private BackupRestoreOrchestratorSteps() {
    }

    public static Set<String> findBRAgentsInEvnfmNamespace(final ClusterConfig config) {
        LOGGER.info("Finding backup-restore agents in {} namespace", EVNFM_INSTANCE.getNamespace());

        final List<Map<String, Object>> manifests = HelmUtils.getReleaseManifests(config, EVNFM_INSTANCE.getNamespace());

        final Set<String> agents = manifests.stream()
                .map(BackupRestoreOrchestratorSteps::maybeExtractBRAgent)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        LOGGER.info("Found following backup-restore agents in {} namespace: {}", EVNFM_INSTANCE.getNamespace(), agents);

        return agents;
    }

    private static Optional<String> maybeExtractBRAgent(final Map<String, Object> item) {
        final Map<String, Object> spec = getChildObject(item, SPEC_KEY);
        final Map<String, Object> template = getChildObject(spec, TEMPLATE_KEY);

        return maybeExtractBRAgentFromLabel(template)
                .or(() -> maybeExtractBRAgentFromEnv(template));
    }

    private static Optional<String> maybeExtractBRAgentFromLabel(final Map<String, Object> template) {
        final Map<String, Object> metadata = getChildObject(template, METADATA_KEY);
        final Map<String, Object> labels = getChildObject(metadata, LABELS_KEY);

        // Check for label
        final Object adpbrlabelkeyValue = labels.get(ADPBRLABELKEY_LABEL);
        if (adpbrlabelkeyValue != null) {
            return Optional.of((String) adpbrlabelkeyValue);
        }

        return Optional.empty();
    }

    private static Optional<String> maybeExtractBRAgentFromEnv(final Map<String, Object> template) {
        final Map<String, Object> templateSpec = getChildObject(template, SPEC_KEY);
        final List<Map<String, Object>> templateContainers = getChildObjectList(templateSpec, CONTAINERS_KEY);

        for (final Map<String, Object> container : templateContainers) {
            final List<Map<String, Object>> containerEnv = getChildObjectList(container, ENV_KEY);

            for (final Map<String, Object> envItem : containerEnv) {
                // Check container env variable
                if (Objects.equals(envItem.get(NAME_KEY), REGISTRATION_AGENT_ID_ENV_VAR)) {
                    return Optional.of((String) envItem.get(VALUE_KEY));
                }
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getChildObject(final Map<String, Object> parent, final String childKey) {
        final Object child = parent.get(childKey);
        if (!(child instanceof Map)) {
            return Map.of();
        }

        return (Map<String, Object>) child;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getChildObjectList(final Map<String, Object> parent, final String childKey) {
        final Object child = parent.get(childKey);
        if (!(child instanceof List)) {
            return List.of();
        }

        return (List<Map<String, Object>>) child;
    }
}
