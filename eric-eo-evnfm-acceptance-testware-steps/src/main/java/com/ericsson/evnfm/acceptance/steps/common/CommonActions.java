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
package com.ericsson.evnfm.acceptance.steps.common;

import static java.lang.String.format;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.utils.HelmUtils.runCommand;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.keycloak.common.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.SftpUsersSecret;
import com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesApiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.fabric8.kubernetes.api.model.Secret;

public class CommonActions {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonActions.class);

    private static final ObjectMapper MAPPER = getObjectMapper();

    public static void addFileToStatefulPod(final BackupRequest backupRequest, String fileName, String text) {
        String addFileToPmServerCommand = String.format("kubectl --kubeconfig %s -n %s exec -i"
                                                                + " eric-pm-server-0 "
                                                                + " --"
                                                                + " /bin/bash -c 'echo \"%s\" > /data/%s'",
                                                        backupRequest.getCluster().getLocalPath(),
                                                        backupRequest.getNamespace(), text, fileName);
        runCommand(addFileToPmServerCommand, 60, true);
        LOGGER.info("File {} was successfully added to eric-pm-server in namespace {}", fileName, backupRequest.getNamespace());
    }

    public static void verifyFileContent(final String namespace, String clusterPath, String fileName, String text) {
        String upgradeCommandWithinWfsPod = String.format("kubectl --kubeconfig %s -n %s exec -i"
                                                                  + " eric-pm-server-0 "
                                                                  + " --"
                                                                  + " /bin/bash -c 'cat /data/%s'",
                                                          clusterPath,
                                                          namespace, fileName);
        String commandOutput = runCommand(upgradeCommandWithinWfsPod, 60, true);
        assertThat(commandOutput).withFailMessage(String.format("Content of %s is different in eric-pm-server in namespace %s. "
                                                                        + "Expected: %s. "
                                                                        + "Actual: %s.",
                                                                fileName, namespace, text, commandOutput)).isEqualTo(text);
    }

    public static String getSftpServer(String clusterConfigPath, String vnfInstanceName) {
        String sftpUser = getSftpCredentials(clusterConfigPath).getUsername();
        String sftpServerCommand = String.format("kubectl --kubeconfig %s get svc -n sftp-server eric-data-sftp-server-external " +
                                                         "--output jsonpath='{.status.loadBalancer.ingress[0].ip}'",
                                                 clusterConfigPath);
        String sftpServer = runCommand(sftpServerCommand, 30, false);
        return String.format("sftp://%s@%s:%s/home/vnfm/%s", sftpUser, sftpServer, "9022", vnfInstanceName);
    }

    public static SftpUsersSecret.SftpUser getSftpCredentials(String clusterConfigPath) {
        String credentials = getSftpSecret(clusterConfigPath);
        String decodedCredentials;
        try {
            decodedCredentials = new String(Base64.decode(credentials), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(format("Failed to decode secret from string: %s", credentials));
        }
        SftpUsersSecret sftpUsersSecret = readSftpUsersSecretFrom(decodedCredentials);
        if (sftpUsersSecret == null || sftpUsersSecret.getUsers() == null || sftpUsersSecret.getUsers().isEmpty()) {
            throw new RuntimeException("No users are present in secret: \"eric-data-sftp-local-user-secrets\"");
        }
        return sftpUsersSecret.getUsers().get(0);
    }

    private static String getSftpSecret(final String clusterConfigPath) {
        final KubernetesApiClient kubernetesApiClient = new KubernetesApiClient(clusterConfigPath);
        Secret secret = kubernetesApiClient.getSecret("sftp-server", "eric-data-sftp-local-user-secrets");
        if (secret == null) {
            throw new RuntimeException("Could not find secret: \"eric-data-sftp-local-user-secrets\"");
        }
        Map<String, String> data = secret.getData();
        if (!data.containsKey("eric-data-sftp-local-users-cfg.json")) {
            throw new RuntimeException("No eric-data-sftp-local-users-cfg.json key are present in secret: \"eric-data-sftp-local-user-secrets\"");
        }
        return data.get("eric-data-sftp-local-users-cfg.json");
    }

    public static String getNodeIpAddress(String clusterConfigPath) {
        String nodeIpAddressCommand = String.format("kubectl --kubeconfig %s get nodes -o jsonpath=\"{.items[0].status.addresses[0].address}\"",
                                                    clusterConfigPath);
        return runCommand(nodeIpAddressCommand, 30, false);
    }

    public static void scaleResource(String clusterConfig, String namespace, String deployName, String replicas) {
        String scaleResourceCommand = String.format("kubectl --kubeconfig %s scale deploy/%s --replicas=%s --namespace %s",
                                                    clusterConfig,
                                                    deployName,
                                                    replicas,
                                                    namespace);
        runCommand(scaleResourceCommand, 30, true);
    }

    private static SftpUsersSecret readSftpUsersSecretFrom(final String sftpUsersSecret) {
        try {
            return MAPPER.readValue(sftpUsersSecret, SftpUsersSecret.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(format("Failed to read secret from string: %s", sftpUsersSecret));
        }
    }
}
