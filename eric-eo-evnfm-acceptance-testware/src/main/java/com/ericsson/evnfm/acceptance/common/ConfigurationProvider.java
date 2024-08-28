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
package com.ericsson.evnfm.acceptance.common;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCustomResource;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigOnboarding;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigParent;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigRollback;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigTerminate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;
import com.ericsson.evnfm.acceptance.models.configuration.EvnfmTestInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class ConfigurationProvider {

    private static ConfigGeneral configGeneral;
    private static ConfigTerminate configTerminate;
    private static ConfigOnboarding configOnboarding;
    private static ConfigInstantiate configInstantiate;
    private static ConfigUpgrade configUpgrade;
    private static ConfigRollback configRollback;
    private static ConfigScale configScale;
    private static ConfigCluster configCluster;
    private static ConfigCustomResource configCustomResource;
    private static boolean preconfigured;
    private static EvnfmTestInfo evnfmTestInfo;

    private ConfigurationProvider() {
    }

    public static EvnfmTestInfo getTestInfo() {
        return evnfmTestInfo;
    }

    public static void setTestInfo(final EvnfmTestInfo evnfmTestInfo) {
        ConfigurationProvider.evnfmTestInfo = evnfmTestInfo;
    }

    public static ConfigGeneral getGeneralConfig() {
        return configGeneral;
    }

    public static ConfigTerminate getTerminateConfig() {
        return configTerminate;
    }

    public static ConfigOnboarding getOnboardingConfig() {
        return configOnboarding;
    }

    public static ConfigInstantiate getInstantiateConfig() {
        return configInstantiate;
    }

    public static ConfigUpgrade getUpgradeConfig() {
        return configUpgrade;
    }

    public static ConfigRollback getConfigRollback() {
        return configRollback;
    }

    public static ConfigCluster getConfigCluster() {
        return configCluster;
    }

    public static ConfigCustomResource getConfigCustomResource() {
        return configCustomResource;
    }

    public static boolean isPreconfigured() {
        return preconfigured;
    }

    public static void setConfiguration(final String configFile) throws IOException {
        if (!configFile.isEmpty()) {
            final FileSystemResource fileResource = getFileResource(configFile);
            File resourceFile = fileResource.getFile();

            JsonMapper objectMapper = JsonMapper.builder()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
                    .build();
            ConfigParent configParent = objectMapper.readValue(resourceFile, ConfigParent.class);

            configGeneral = configParent.getConfigGeneral();
            configTerminate = configParent.getConfigTerminate();
            configOnboarding = configParent.getConfigOnboarding();
            configInstantiate = configParent.getConfigInstantiate();
            configUpgrade = configParent.getConfigUpgrade();
            configRollback = configParent.getConfigRollback();
            configScale = configParent.getConfigScale();
            configCluster = configParent.getConfigCluster();
            configCustomResource = configParent.getConfigCustomResource();
            preconfigured = true;
        }
    }

    private static FileSystemResource getFileResource(final String fileToLocate) {
        File file = new File(fileToLocate);
        if (file.isFile()) {
            return new FileSystemResource(file);
        }
        ClassLoader classLoader = ConfigurationProvider.class.getClassLoader();
        file = new File(requireNonNull(classLoader.getResource(fileToLocate)).getFile());
        return new FileSystemResource(file);
    }

    public static ConfigScale getConfigScale() {
        return configScale;
    }
}
