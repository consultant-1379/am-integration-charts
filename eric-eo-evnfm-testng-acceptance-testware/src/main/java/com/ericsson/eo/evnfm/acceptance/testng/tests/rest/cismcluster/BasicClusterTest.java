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

import static com.ericsson.eo.evnfm.acceptance.testng.infrastructure.ClusterUtils.checkClusterIsReachable;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.*;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigListContainsCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigListDoesNotContainCluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ClusterConfigStatus;

public class BasicClusterTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicClusterTest.class);

    @Test(description = "Register default cluster",
            dataProvider = "clusterConfigTestDataTarget", dataProviderClass = CISMClustersDataProviders.class)
    public void testRegisterCluster(ClusterConfig clusterConfig) {
        LOGGER.info("Cluster for tests to register: {}", clusterConfig);
        checkClusterIsReachable(clusterConfig);

        for (ClusterConfig config: getClustersList(user)) {
            if (getCluster(user, config.getName()).equals(clusterConfig)) {
                LOGGER.info("Cluster {} already registered", config.getName());
                return;
            }
        }

        registerCluster(clusterConfig, user);
        verifyClusterConfigListContainsCluster(getClustersList(user), clusterConfig);
    }

    @Test(description = "Register second cluster for running tests",
            dataProvider = "clusterConfigDataGenerateNewConfig", dataProviderClass = CISMClustersDataProviders.class)
    public void testRegisterTestCluster(ClusterConfig clusterConfig) {
        checkClusterIsReachable(clusterConfig);
        testRegisterCluster(clusterConfig);
    }

    @Test(description = "Deregister cluster for tests",
            dataProvider = "clusterConfigDataGenerateNewConfig", dataProviderClass = CISMClustersDataProviders.class)
    public void testDeregisterTestCluster(ClusterConfig clusterConfig) {
        LOGGER.info("Cluster to deregister: {}", clusterConfig);

        String configName = clusterConfig.getName();
        if (!getCluster(user, configName).equals(clusterConfig)) {
            if (getCluster(user, configName).getStatus().equals(ClusterConfigStatus.IN_USE.name())) {
                LOGGER.info("Cluster {} can't be deregister - {} status", configName, getCluster(user, configName).getStatus());
                return;
            }
        }

        deregisterCluster(clusterConfig, user);
        verifyClusterConfigListDoesNotContainCluster(getClustersList(user), clusterConfig);
    }
}