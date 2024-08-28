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

import java.util.stream.Stream;

import org.junit.jupiter.params.provider.Arguments;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCustomResource;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigOnboarding;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigRollback;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigTerminate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;

public class TestDataProvider {

    private static Stream restTestConfigurationData(){
        ConfigInstantiate instantiateConfig = ConfigurationProvider.getInstantiateConfig();
        ConfigUpgrade configUpgrade = ConfigurationProvider.getUpgradeConfig();
        ConfigTerminate terminateConfig = ConfigurationProvider.getTerminateConfig();
        ConfigCluster clusterConfig = ConfigurationProvider.getConfigCluster();
        ConfigScale configScale = ConfigurationProvider.getConfigScale();
        return Stream.of(
                Arguments.of(instantiateConfig, configUpgrade, terminateConfig, clusterConfig, configScale));
    }

    private static Stream uiTestConfigurationData(){
        ConfigGeneral generalConfig = ConfigurationProvider.getGeneralConfig();
        ConfigInstantiate instantiateConfig = ConfigurationProvider.getInstantiateConfig();
        ConfigUpgrade upgradeConfig = ConfigurationProvider.getUpgradeConfig();
        ConfigRollback rollbackConfig = ConfigurationProvider.getConfigRollback();
        ConfigScale scaleConfig = ConfigurationProvider.getConfigScale();
        ConfigTerminate terminateConfig = ConfigurationProvider.getTerminateConfig();
        ConfigCluster clusterConfig = ConfigurationProvider.getConfigCluster();
        return Stream.of(
                Arguments.of(generalConfig, instantiateConfig, upgradeConfig, rollbackConfig, scaleConfig, terminateConfig, clusterConfig));
    }

    private static Stream clusterConfigurationData(){
        ConfigCluster clusterConfig = ConfigurationProvider.getConfigCluster();
        return Stream.of(Arguments.of(clusterConfig));
    }

    private static Stream burConfigurationData() {
        ConfigInstantiate instantiateConfig = ConfigurationProvider.getInstantiateConfig();
        ConfigUpgrade upgradeConfig = ConfigurationProvider.getUpgradeConfig();
        ConfigScale scaleConfig = ConfigurationProvider.getConfigScale();
        ConfigTerminate terminateConfig = ConfigurationProvider.getTerminateConfig();
        ConfigCluster clusterConfig = ConfigurationProvider.getConfigCluster();
        ConfigOnboarding onboardingConfig = ConfigurationProvider.getOnboardingConfig();
        ConfigCustomResource customResourceConfig = ConfigurationProvider.getConfigCustomResource();
        return Stream.of(
                Arguments.of(instantiateConfig, upgradeConfig, scaleConfig, terminateConfig, clusterConfig, onboardingConfig, customResourceConfig));
    }
}
