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
package com.ericsson.evnfm.acceptance.steps.certificates;

import static com.ericsson.evnfm.acceptance.utils.Constants.CA_CERTIFICATE_SECRET_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.Constants.CA_SECRET_NAME;
import static com.ericsson.evnfm.acceptance.utils.Constants.INTERMEDIATE_CA_CERTIFICATE;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.keycloak.common.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.Certificate;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesApiClient;
import com.ericsson.evnfm.acceptance.utils.ProcessExecutor;

import io.fabric8.kubernetes.api.model.Secret;

public class CertificateManagementScriptClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateManagementScriptClient.class);

    private static final String SCRIPT_NAME = "certificate_management.py";
    private static final String SCRIPT_PATH = "../Scripts/eo-evnfm/certificate_management.py";

    public static final String HELP_COMMAND = "--help";
    public static final String LIST_CERTIFICATES_COMMAND = "list-certificates";
    public static final String INSTALL_CERTIFICATES_COMMAND = "install-certificates";

    public static final String TLS_CERTIFICATES_DIRECTORY = "certificates";
    public static final String TRUSTED_CERTIFICATES_DIRECTORY = "trusted";

    private static final ProcessExecutor executor = new ProcessExecutor();

    public static ProcessExecutorResponse printCertificateManagementHelp() throws IOException {
        LOGGER.info("Printing help of the certificate_management.py script");
        String workdir = setupScriptWorkdir();
        String scriptPath = Paths.get(workdir, SCRIPT_NAME).toString();
        String command = getCommand(scriptPath, HELP_COMMAND);
        try {
            return executor.executeProcess(command, workdir, Collections.emptyList(), 10, false);
        } catch (Exception e) {
            String message = String.format("Received exception while executing --help command for script %s", scriptPath);
            LOGGER.error(message, e);
            throw new RuntimeException(e);
        }
    }

    public static ProcessExecutorResponse listTrustedCertificates(final ClusterConfig config, final User user) throws IOException, URISyntaxException {
        LOGGER.info("Listing trusted certificates using certificate_management.py script");
        String workdir = setupScriptWorkdirWithValuesAndCA(config);
        String scriptPath = Paths.get(workdir, SCRIPT_NAME).toString();
        String command = getCommand(scriptPath, LIST_CERTIFICATES_COMMAND, user);
        try {
            return executor.executeProcess(command, workdir, getSensitiveData(user), 10, false);
        } catch (Exception e) {
            String message = String.format("Received exception while executing list-certificates command for script %s", scriptPath);
            LOGGER.error(message, e);
            throw new RuntimeException(e);
        }
    }

    public static ProcessExecutorResponse installTrustedCertificates(
            final TrustedCertificatesRequest certificates, final ClusterConfig config, final User user) throws IOException, URISyntaxException {
        LOGGER.info("Installing trusted certificates using certificate_management.py script");
        String workdir = setupScriptWorkdirWithValuesCAAndCertificates(certificates, config);
        String scriptPath = Paths.get(workdir, SCRIPT_NAME).toString();
        String command = getCommand(scriptPath, INSTALL_CERTIFICATES_COMMAND, user);
        try {
            return executor.executeProcess(command, workdir, getSensitiveData(user), 10, false);
        } catch (Exception e) {
            String message = String.format("Received exception while executing install-certificates command for script %s", scriptPath);
            LOGGER.error(message, e);
            throw new RuntimeException(e);
        }
    }

    private static String setupScriptWorkdir() throws IOException {
        Path tempWorkdir = Files.createTempDirectory("certificate-management-");
        LOGGER.info("Created tmp working directory {}", tempWorkdir);

        Path sourceScriptPath = Paths.get(SCRIPT_PATH);
        Path targetScriptPath = Paths.get(tempWorkdir.toString(), SCRIPT_NAME);
        Files.copy(sourceScriptPath, targetScriptPath);
        return tempWorkdir.toString();
    }

    private static String setupScriptWorkdirWithValuesAndCA(final ClusterConfig config) throws IOException, URISyntaxException {
        String workingDirectory = setupScriptWorkdir();
        createSiteValuesFile(workingDirectory);
        createCertificateAuthority(workingDirectory, config);
        return workingDirectory;
    }

    private static String setupScriptWorkdirWithValuesCAAndCertificates(
            final TrustedCertificatesRequest request, final ClusterConfig config) throws IOException, URISyntaxException {
        String workingDirectory = setupScriptWorkdirWithValuesAndCA(config);
        createTrustedCertificates(workingDirectory, request);
        return workingDirectory;
    }

    private static void createSiteValuesFile(final String workdir) throws IOException, URISyntaxException {
        LOGGER.info("Writing site values to working directory");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Paths.get(workdir, "site_values_.yaml").toFile()))) {
            writer.write("global:\n");
            writer.write("  hosts:\n");
            writer.write(String.format("    vnfm: %s", new URI(EVNFM_INSTANCE.getEvnfmUrl()).getHost()));
        }
    }

    private static void createCertificateAuthority(final String workdir, final ClusterConfig config) throws IOException {
        Path tlsCertificatesDirectory = Paths.get(workdir, TLS_CERTIFICATES_DIRECTORY);
        Files.createDirectory(tlsCertificatesDirectory);
        writeCertificateToFile(readCertificatesAuthority(config), Paths.get(
                tlsCertificatesDirectory.toString(), INTERMEDIATE_CA_CERTIFICATE).toString());
    }

    private static void createTrustedCertificates(final String workdir, final TrustedCertificatesRequest request) throws IOException {
        Path trustedCertificatesDirectory = Paths.get(workdir, TLS_CERTIFICATES_DIRECTORY, TRUSTED_CERTIFICATES_DIRECTORY);
        LOGGER.info("Writing trusted certificates to working directory {}", trustedCertificatesDirectory);

        Files.createDirectory(trustedCertificatesDirectory);
        for (Certificate certificate : request.getCertificates()) {
            Path certificatePath = Paths.get(workdir, TLS_CERTIFICATES_DIRECTORY, TRUSTED_CERTIFICATES_DIRECTORY, certificate.getName());
            writeCertificateToFile(certificate.getCertificate(), certificatePath.toString());
        }
    }

    private static String readCertificatesAuthority(final ClusterConfig config) throws IOException {
        LOGGER.info("Reading certificate authority from Evnfm namespace");
        KubernetesApiClient kubernetesApiClient = new KubernetesApiClient(config.getLocalPath());
        Secret caSecret = Optional.ofNullable(kubernetesApiClient.getSecret(EVNFM_INSTANCE.getNamespace(), CA_SECRET_NAME))
                .orElseThrow(() -> new RuntimeException(String.format(
                        "Could not read %s secret from evnfm namespace %s", CA_SECRET_NAME, EVNFM_INSTANCE.getNamespace())));
        String base64Ca = Optional.ofNullable(caSecret.getData().get(CA_CERTIFICATE_SECRET_PROPERTY))
                .orElseThrow(() -> new RuntimeException(String.format(
                        "CA secret goes not contain %s property", CA_CERTIFICATE_SECRET_PROPERTY)));
        return new String(Base64.decode(base64Ca), StandardCharsets.UTF_8);
    }

    private static void writeCertificateToFile(final String certificate, final String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(certificate);
        }
    }

    private static String getCommand(final String scriptPath, final String arguments) {
        return String.format("%s %s", scriptPath, arguments);
    }

    private static String getCommand(final String scriptPath, final String arguments, final User user) {
        return String.format("%s %s --login '%s' --password '%s'", scriptPath, arguments, user.getUsername(), user.getPassword());
    }

    private static List<String> getSensitiveData(final User user) {
        return Arrays.asList(user.getUsername(), user.getPassword());
    }
}
