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
package com.ericsson.evnfm.acceptance.utils;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.utils.Constants.CSAR_DOWNLOAD_PATH_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.DRAC_ENABLED;
import static com.ericsson.evnfm.acceptance.utils.Constants.EVNFM_URL_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.HELM_REGISTRY_URL;
import static com.ericsson.evnfm.acceptance.utils.Constants.IDAM_ADMIN_PASSWORD_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.IDAM_ADMIN_USER_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.IDAM_CLIENT_ID_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.IDAM_REALM_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.IDAM_URL_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.NAMESPACE;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_INSTANCE_NAME;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.setEnvVariables;
import static com.ericsson.evnfm.acceptance.utils.HelmCommand.HELM;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.EvnfmInstance;
import com.ericsson.evnfm.acceptance.models.HelmHistory;
import com.ericsson.evnfm.acceptance.models.ManualUpgrade;
import com.google.common.base.Strings;

public final class TestExecutionGlobalConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionGlobalConfig.class);

    private static final String GLOBAL_FLOW_SUFFIX = "globalFlowSuffix"; // unique identifier for cnfs
    private static final String GLOBAL_PROPERTIES = "globalProperties"; // relative path to .properties file
    private static final String GLOBAL_TEST_CONFIG = "globalTestConfig"; // absolute path to .properties file

    public static final EvnfmInstance EVNFM_INSTANCE;

    static {
        Optional<EvnfmInstance> loadedEvnfmInstance = loadFromPropertiesFile();
        EVNFM_INSTANCE = loadedEvnfmInstance.orElseGet(TestExecutionGlobalConfig::loadEvnfmInstanceFromEnvironmentProperties);
        LOGGER.info("Loaded EVNFM instance: {}", EVNFM_INSTANCE);
    }

    private static Optional<EvnfmInstance> loadFromPropertiesFile() {
        Optional<Properties> globalProperties = loadProperties();
        return globalProperties.map(TestExecutionGlobalConfig::loadEvnfmInstanceFromFileProperties);
    }

    private static EvnfmInstance loadEvnfmInstanceFromFileProperties(Properties globalProperties) {
        LOGGER.info("Creating EVNFM instance from file properties");
        return loadEvnfmInstanceFromProvider(
                propertyName -> setEnvVariables(globalProperties.getProperty(propertyName)));
    }

    private static EvnfmInstance loadEvnfmInstanceFromEnvironmentProperties() {
        LOGGER.info("Creating EVNFM instance from environment properties");
        return loadEvnfmInstanceFromProvider(
                propertyName -> setEnvVariables(System.getProperty(propertyName)));
    }

    private static EvnfmInstance loadEvnfmInstanceFromProvider(Function<String, String> propertiesProvider) {
        String evnfmUrl = propertiesProvider.apply(EVNFM_URL_PROPERTY);
        String idamUrl = propertiesProvider.apply(IDAM_URL_PROPERTY);
        String idamAdminUser = propertiesProvider.apply(IDAM_ADMIN_USER_PROPERTY);
        String idamAdminPassword = propertiesProvider.apply(IDAM_ADMIN_PASSWORD_PROPERTY);
        String idamClientId = propertiesProvider.apply(IDAM_CLIENT_ID_PROPERTY);
        String idamRealm = propertiesProvider.apply(IDAM_REALM_PROPERTY);
        String csarDownloadPath = propertiesProvider.apply(CSAR_DOWNLOAD_PATH_PROPERTY);
        String helmCommand = propertiesProvider.apply(HELM);
        String helmRegistryUrl = propertiesProvider.apply(HELM_REGISTRY_URL);
        String namespace = propertiesProvider.apply(NAMESPACE);
        String dracEnabled = propertiesProvider.apply(DRAC_ENABLED);

        return createEvnfmInstance(evnfmUrl, idamUrl, idamAdminUser, idamAdminPassword, idamClientId,
                                   idamRealm, csarDownloadPath, helmCommand, helmRegistryUrl, namespace, dracEnabled);
    }

    private static Optional<Properties> loadProperties() {
        // get properties file path from globalProperties or globalTestConfig environment variable
        String globalProperties = System.getProperty(GLOBAL_PROPERTIES);
        String globalTestConfig = System.getProperty(GLOBAL_TEST_CONFIG);

        Properties properties = new Properties();
        try {
            if (!Strings.isNullOrEmpty(globalProperties)) {
                LOGGER.info("Loading global properties from {}", globalProperties);
                properties.load(TestExecutionGlobalConfig.class.getClassLoader().getResourceAsStream(globalProperties));
            } else if (!Strings.isNullOrEmpty(globalTestConfig)) {
                LOGGER.info("Loading global properties from {}", globalTestConfig);
                properties.load(new FileInputStream(globalTestConfig));
            } else {
                LOGGER.info("No path to file with global properties provided");
                return Optional.empty();
            }
        } catch (Exception e) {
            String exceptionMessage = "Cannot load global properties from file";
            LOGGER.error(exceptionMessage, e);
            throw new RuntimeException(exceptionMessage, e);
        }

        return Optional.of(properties);
    }

    private static EvnfmInstance createEvnfmInstance(String evnfmUrl, String idamUrl, String idamAdminUser,
                                                     String idamAdminPassword, String idamClientId,
                                                     String idamRealm, String csarDownloadPath, String helmCommand,
                                                     String helmRegistryUrl, String namespace, String dracEnabled) {
        assertThat(evnfmUrl).withFailMessage("EVNFM url cannot be null or empty").isNotNull();
        assertThat(idamUrl).withFailMessage("EVNFM idamUrl cannot be null or empty").isNotNull();
        assertThat(idamAdminUser).withFailMessage("EVNFM idamAdminUser cannot be null or empty").isNotNull();
        assertThat(idamAdminPassword).withFailMessage("EVNFM idamAdminPassword cannot be null or empty").isNotNull();
        assertThat(idamClientId).withFailMessage("EVNFM idamClientId cannot be null or empty").isNotNull();
        assertThat(idamRealm).withFailMessage("EVNFM idamRealm cannot be null or empty").isNotNull();
        assertThat(csarDownloadPath).withFailMessage("Csar download path cannot be null or empty").isNotNull();
        assertThat(helmCommand).withFailMessage("Helm executable cannot be null or empty").isNotNull();
        assertThat(helmRegistryUrl).withFailMessage("Helm Registry url cannot be null or empty").isNotNull();
        assertThat(namespace).withFailMessage("Evnfm Namespace cannot be null or empty").isNotNull();

        EvnfmInstance evnfmInstance = new EvnfmInstance();
        evnfmInstance.setEvnfmUrl(evnfmUrl);
        evnfmInstance.setIdamUrl(idamUrl);
        evnfmInstance.setIdamAdminUser(idamAdminUser);
        evnfmInstance.setIdamAdminPassword(idamAdminPassword);
        evnfmInstance.setIdamClientId(idamClientId);
        evnfmInstance.setIdamRealm(idamRealm);
        evnfmInstance.setCsarDownloadPath(csarDownloadPath);
        evnfmInstance.setHelm(helmCommand);
        evnfmInstance.setHelmRegistryUrl(helmRegistryUrl);
        evnfmInstance.setNamespace(namespace);
        evnfmInstance.setDracEnabled(dracEnabled);

        return evnfmInstance;
    }

    public static void addFlowSuffix(final ClusterConfig config) {
        String globalFlowSuffix = System.getProperty(GLOBAL_FLOW_SUFFIX);
        if (globalFlowSuffix != null && config.getNamespace() != null) {
            config.setNamespace(String.format("%s-%s", config.getNamespace(), globalFlowSuffix));
        }
    }

    public static EvnfmCnf addFlowSuffix(final EvnfmCnf evnfmCnf) {
        String globalFlowSuffix = System.getProperty(GLOBAL_FLOW_SUFFIX);
        if (globalFlowSuffix != null) {
            evnfmCnf.setVnfInstanceName(String.format("%s-%s", evnfmCnf.getVnfInstanceName(), globalFlowSuffix));
            evnfmCnf.setNamespace(String.format("%s-%s", evnfmCnf.getNamespace(), globalFlowSuffix));
        }
        return evnfmCnf;
    }

    public static List<EvnfmCnf> addFlowSuffix(final List<EvnfmCnf> cnfList) {
        cnfList.forEach(TestExecutionGlobalConfig::addFlowSuffix);

        return cnfList;
    }

    public static BackupRequest addFlowSuffix(final BackupRequest backup) {
        String globalFlowSuffix = System.getProperty(GLOBAL_FLOW_SUFFIX);
        if (globalFlowSuffix != null) {
            backup.setVnfInstanceName(String.format("%s-%s", backup.getVnfInstanceName(), globalFlowSuffix));
            backup.setNamespace(String.format("%s-%s", backup.getNamespace(), globalFlowSuffix));
        }
        return backup;
    }

    public static EvnfmCnf addFlowSuffixToModifiedName(final EvnfmCnf evnfmCnf) {
        String globalFlowSuffix = System.getProperty(GLOBAL_FLOW_SUFFIX);
        if (globalFlowSuffix != null) {
            evnfmCnf.setVnfInstanceNameToModify(String.format("%s-%s", evnfmCnf.getVnfInstanceNameToModify(), globalFlowSuffix));
        }
        return evnfmCnf;
    }

    public static void setTargets(final EvnfmCnf evnfmCnf) {
        evnfmCnf.setTargets(evnfmCnf.getTargets()
                                    .entrySet()
                                    .stream()
                                    .collect(Collectors.toMap(key -> key.getKey().replace(VNF_INSTANCE_NAME, evnfmCnf.getVnfInstanceName()),
                                                              Map.Entry::getValue)));
    }

    public static void setExpectedHelmHistory(final EvnfmCnf evnfmCnf) {
        evnfmCnf.setExpectedHelmHistory(evnfmCnf.getExpectedHelmHistory().entrySet().stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        entry -> entry.getValue().stream()
                                                                .map(history -> new HelmHistory(
                                                                        history.getRevision(),
                                                                        history.getDescription()
                                                                                .replace(VNF_INSTANCE_NAME, evnfmCnf.getVnfInstanceName())
                                                                                .replace("\n", "")
                                                                ))
                                                                .collect(Collectors.toList())
                                                )));
    }

    public static void setManualUpgrade(final EvnfmCnf evnfmCnf) {
        for (ManualUpgrade manualUpgrade : evnfmCnf.getManualUpgrade()) {
            if (manualUpgrade.getReleaseName() != null) {
                String releaseName = manualUpgrade.getReleaseName().replace(VNF_INSTANCE_NAME, evnfmCnf.getVnfInstanceName());
                manualUpgrade.setReleaseName(releaseName);
            }
        }
    }
}
