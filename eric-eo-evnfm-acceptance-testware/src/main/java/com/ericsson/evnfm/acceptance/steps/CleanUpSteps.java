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

import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_NAMESPACE;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.CleanUp;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.utils.ProcessExecutor;

public class CleanUpSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUpSteps.class);


    public static void cleanUpResources(final List<CleanUp> releasesToCleanUp) throws IOException, InterruptedException {
        LOGGER.info("Cleaning up");
        ProcessExecutor executor = new ProcessExecutor();
        LOGGER.info("Deleting any releases left behind and namespaces");
        for(CleanUp release: releasesToCleanUp){
            StringBuilder helmDelete = new StringBuilder("helm3 uninstall -n ");
            helmDelete.append(release.getNamespace());
            helmDelete.append(" ");
            helmDelete.append(release.getReleaseName());
            if(release.getCluster()!=null){
                helmDelete.append(" --kubeconfig ").append(release.getKubeConfig());
            }
            int timeOut = Integer.parseInt(release.getApplicationTimeOut());
            LOGGER.info("Cleanup resource command to run '{}'", helmDelete);
            executor.executeProcess(helmDelete.toString(), timeOut, false);
            String namespaceDelete = String.format(DELETE_NAMESPACE, release.getNamespace(), release.getKubeConfig());
            LOGGER.info("Cleanup namespace command to run '{}'", namespaceDelete);
            executor.executeProcess(namespaceDelete, timeOut, false);
        }
    }

    public static void createAndAddCleanUp(ConfigCluster configCluster, ConfigInstantiate configInstantiate,
                                           List<CleanUp> releasesToCleanUp, String appendReleaseName) {
        CleanUp cleanUp = new CleanUp();
        cleanUp.setApplicationTimeOut(configInstantiate.getApplicationTimeOut());
        cleanUp.setCluster(configInstantiate.getCluster());
        cleanUp.setKubeConfig(configCluster.getExternalConfigFile());
        cleanUp.setNamespace(configInstantiate.getNamespace());
        cleanUp.setReleaseName(configInstantiate.getReleaseName() + appendReleaseName);
        releasesToCleanUp.add(cleanUp);
    }

}
