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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.cismcluster;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders.loadClusterConfigs;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders.loadInitialClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.deregisterCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.deregisterClusterExpectingError;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.getClustersList;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.modifyCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.registerCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.updateCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigListContainsCluster;
import static com.ericsson.evnfm.acceptance.utils.Constants.CISM_CLUSTER_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultClusterTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClusterTest.class);

    @Test(description = "Register, Update, Patch, Deregister operations for default Config", dataProvider = "clusterConfigDataSecondCluster",
            dataProviderClass = CISMClustersDataProviders.class)
    public void registerUpdatePatchDeregisterDefaultCluster(ClusterConfig secondCluster, ProblemDetails problemDetails, ClusterConfig defaultConfig) throws
            JsonProcessingException {

        ClusterConfig firstCluster = getClustersList(user).stream()
                .filter(ClusterConfig::isDefault)
                .findFirst().orElseThrow(() -> new RuntimeException("Default cluster doesn't exist"));

        secondCluster.setIsDefault(true);
        registerCluster(secondCluster, user);

        List<ClusterConfig> clusterConfigList = getClustersList(user);
        verifyClusterConfigListContainsCluster(clusterConfigList, secondCluster);
        assertDefaultValuesChanged(secondCluster.getName(), firstCluster.getName(), clusterConfigList);

        deregisterClusterExpectingError(secondCluster, user, problemDetails);

        updateClusterToDefault(firstCluster, defaultConfig.getLocalPath());
        assertDefaultValuesChanged(firstCluster.getName(), secondCluster.getName(), getClustersList(user));

        partiallyUpdateClusterToDefault(secondCluster);
        assertDefaultValuesChanged(secondCluster.getName(), firstCluster.getName(), getClustersList(user));
    }

    private void partiallyUpdateClusterToDefault(ClusterConfig clusterConfig) {
        String patchFields = "{ \n"
                + "\t\"isDefault\"\t  : true\n"
                + "}";
        modifyCluster(patchFields, clusterConfig, user, true);
    }

    private void updateClusterToDefault(final ClusterConfig firstCluster, final String localPath) {
        firstCluster.setIsDefault(true);
        firstCluster.setLocalPath(localPath);
        LOGGER.info("{} Set cluster config to default for {} with path {}", CISM_CLUSTER_PREFIX, firstCluster.getName(), localPath);
        updateCluster(firstCluster, user, true);
    }


    private void assertDefaultValuesChanged(String expectedDefaultClusterName, String notDefaultClusterName, final List<ClusterConfig> clusterConfigList) {
        ClusterConfig defaultCluster = null;
        ClusterConfig notDefaultCluster = null;
        for (ClusterConfig clusterConfig: clusterConfigList){
            if (clusterConfig.getName().equals(expectedDefaultClusterName)){
                defaultCluster = clusterConfig;
            }

            if (clusterConfig.getName().equals(notDefaultClusterName)) {
                notDefaultCluster = clusterConfig;
            }
        }

        assertThat(defaultCluster).isNotNull();
        assertThat(notDefaultCluster).isNotNull();
        assertThat(defaultCluster.isDefault()).isEqualTo(true);
        assertThat(notDefaultCluster.isDefault()).isEqualTo(false);
    }

    @AfterClass
    public void cleanUpAfterTest(ITestContext iTestContext) throws IOException {
        List<String> clusterConfigNames = loadClusterConfigs(iTestContext).values().stream()
                .map(ClusterConfig::getName).collect(Collectors.toList());
        ClusterConfig initialCluster = loadInitialClusterConfig(iTestContext);
        List<ClusterConfig> registeredClusters = getClustersList(user);
        ClusterConfig initialClusterCurrentValue = registeredClusters.stream()
                .filter(cluster->cluster.getName().equals(initialCluster.getName()))
                .findFirst().orElseThrow();
        registeredClusters.forEach(clusterConfig -> {
            if (clusterConfigNames.contains(clusterConfig.getName())){
                if (clusterConfig.isDefault()){
                    LOGGER.info("{} Check default cluster status {} default status {}",
                            CISM_CLUSTER_PREFIX, clusterConfig.getName(), clusterConfig.isDefault());
                    LOGGER.info("{} Update default cluster for {} with path {}",
                            CISM_CLUSTER_PREFIX, initialClusterCurrentValue.getName(), initialCluster.getLocalPath());
                    updateClusterToDefault(initialClusterCurrentValue, initialCluster.getLocalPath());
                }
                deregisterCluster(clusterConfig, user);
            }
        });
    }
}
