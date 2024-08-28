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
package com.ericsson.evnfm.acceptance.steps.evnfm.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.deletePVCs;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.getConfigContent;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.runCommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.configuration.testng.ConfigEvnfmDeployment;
import com.ericsson.evnfm.acceptance.utils.HelmCommand;

public class EvnfmLcmSteps {
    private EvnfmLcmSteps() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EvnfmLcmSteps.class);

    public static void addHelmRepo(final ConfigEvnfmDeployment configEvnfmDeployment) {
        var commandToExecute = new HelmCommand()
                .addRepo(configEvnfmDeployment.getHelmRepoName(), configEvnfmDeployment.getHelmRepoUrl())
                .kubeconfig(configEvnfmDeployment.getCluster().getLocalPath())
                .build();
        runCommand(commandToExecute, configEvnfmDeployment.getCommonTimeout(), true);
    }

    public static void updateHelmRepo(final ConfigEvnfmDeployment configEvnfmDeployment) {
        var commandToExecute = new HelmCommand()
                .updateRepo()
                .kubeconfig(configEvnfmDeployment.getCluster().getLocalPath())
                .build();
        runCommand(commandToExecute, configEvnfmDeployment.getCommonTimeout(), true);
    }

    public static void installEvnfm(ConfigEvnfmDeployment configEvnfmDeployment) {
        var valuesFile = setValuesFile(configEvnfmDeployment.getInstall().getValuesFile());
        String chart = configEvnfmDeployment.getHelmRepoName() + "/" + configEvnfmDeployment.getChart();
        var commandBuilder = new HelmCommand()
                .install(configEvnfmDeployment.getReleaseName(), chart)
                .timeout(configEvnfmDeployment.getCommonTimeout())
                .namespace(configEvnfmDeployment.getNamespace())
                .values(valuesFile.getPath())
                .kubeconfig(configEvnfmDeployment.getCluster().getLocalPath())
                .helmWait()
                .devel()
                .debug();
        // If version is not provided, the latest version will be installed
        if (!Strings.isNullOrEmpty(configEvnfmDeployment.getInstall().getChartVersion())) {
            commandBuilder.version(configEvnfmDeployment.getInstall().getChartVersion());
        }
        runCommand(commandBuilder.build(), configEvnfmDeployment.getCommonTimeout(), true);
    }

    public static void upgradeEvnfm(ConfigEvnfmDeployment configEvnfmDeployment) {
        var valuesFile = setValuesFile(configEvnfmDeployment.getUpgrade().getValuesFile());
        String chart = configEvnfmDeployment.getHelmRepoName() + "/" + configEvnfmDeployment.getChart();
        var commandBuilder = new HelmCommand()
                .upgrade(configEvnfmDeployment.getReleaseName(), chart)
                .timeout(configEvnfmDeployment.getCommonTimeout())
                .namespace(configEvnfmDeployment.getNamespace())
                .values(valuesFile.getPath())
                .kubeconfig(configEvnfmDeployment.getCluster().getLocalPath())
                .helmWait()
                .devel()
                .debug();
        // If version is not provided, it will be upgraded to the latest version
        if (!Strings.isNullOrEmpty(configEvnfmDeployment.getUpgrade().getChartVersion())) {
            commandBuilder.version(configEvnfmDeployment.getUpgrade().getChartVersion());
        }
        runCommand(commandBuilder.build(), configEvnfmDeployment.getCommonTimeout(), true);
    }

    public static void uninstallEvnfm(ConfigEvnfmDeployment configEvnfmDeployment, boolean skipCleanup) {
        var command = new HelmCommand()
                .uninstall(configEvnfmDeployment.getReleaseName())
                .timeout(configEvnfmDeployment.getCommonTimeout())
                .namespace(configEvnfmDeployment.getNamespace())
                .kubeconfig(configEvnfmDeployment.getCluster().getLocalPath())
                .debug()
                .build();
        runCommand(command, configEvnfmDeployment.getCommonTimeout(), true);
        if (skipCleanup) {
            LOGGER.info("Skipping delete PVC's from:: {}", configEvnfmDeployment.getNamespace());
        } else {
            cleanUpPVCs(configEvnfmDeployment.getCluster(), configEvnfmDeployment.getNamespace());
        }
    }

    public static void cleanUpPVCs(ClusterConfig kubeConfig, String namespace) {
        assertThat(deletePVCs(kubeConfig.getLocalPath(), namespace)).isTrue();
        LOGGER.info("PVC's deleted successfully from namespace: {}, cluster: {} ", namespace, kubeConfig.getName());
    }

    private static File setValuesFile(final String valuesFilePath) {
        File valuesFileUpdated = null;
        try {
            valuesFileUpdated = Files.createTempFile("values", ".yaml").toFile();
            var contents = getConfigContent(valuesFilePath);
            try (var outputStream = new FileOutputStream(valuesFileUpdated)) {
                byte[] strToBytes = contents.getBytes();
                outputStream.write(strToBytes);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to set values files.", e);
            fail("Failed to set values files:: %s", e.getMessage());
        }
        return valuesFileUpdated;
    }
}
