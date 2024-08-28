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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.HELP_COMMAND;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.INSTALL_CERTIFICATES_COMMAND;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.LIST_CERTIFICATES_COMMAND;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.TRUSTED_CERTIFICATES_LIST;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.listInstalledTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.listMountedTrustedCertificatesInOnboardingPod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.evnfm.acceptance.models.Certificate;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.ericsson.evnfm.acceptance.models.TrustedCertificateManagementDetails;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesResponse;
import com.ericsson.evnfm.acceptance.models.User;

public class CertificateManagementVerify {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateManagementVerify.class);
    private static final int DEFAULT_CERTIFICATES_MOUNT_TIMEOUT = 180;

    public static void verifyHelpPrintedSuccessfully(final ProcessExecutorResponse helpResponse) {
        assertThat(helpResponse.getExitValue())
                .withFailMessage("Expected --help command to exit successfully, actual exit code is %s, command output %s, command error %s",
                                 helpResponse.getExitValue(), helpResponse.getCommandOutput(), helpResponse.getCommandError())
                .isEqualTo(0);

        assertThat(helpResponse.getCommandOutput())
                .withFailMessage("Expected --help output to contain %s %s %s, command output %s",
                                 HELP_COMMAND, LIST_CERTIFICATES_COMMAND, INSTALL_CERTIFICATES_COMMAND, helpResponse.getCommandOutput())
                .contains(Arrays.asList(HELP_COMMAND, LIST_CERTIFICATES_COMMAND, INSTALL_CERTIFICATES_COMMAND));
    }

    public static void verifyListCertificatesReturnedExceptionDueToInvalidCredentials(
            final ProcessExecutorResponse listCertificatesResponse) {
        assertThat(listCertificatesResponse.getExitValue())
                .withFailMessage("Expected script to return non-zero value when provided with invalid credentials")
                .isNotEqualTo(0);
    }

    public static void verifyCertificatesInstalledSuccessfully(final ProcessExecutorResponse installCertificatesResponse,
                                                               final TrustedCertificatesRequest expectedCertificates,
                                                               final ClusterConfig config,
                                                               final User user) throws InterruptedException {
        assertThat(installCertificatesResponse.getExitValue())
                .withFailMessage("Expected script to return zero value on successful installation of certificates")
                .isEqualTo(0);
        verifyAllCertificatesWereUploaded(expectedCertificates, user);
        waitForCertificatesToBeMounted(config);
    }

    private static void waitForCertificatesToBeMounted(final ClusterConfig config) throws InterruptedException {
        LOGGER.info("Waiting for the uploaded certificates to appear in the onboarding pod");
        StopWatch stopwatch = StopWatch.createStarted();
        while (stopwatch.getTime(TimeUnit.SECONDS) < DEFAULT_CERTIFICATES_MOUNT_TIMEOUT) {
            String certificates = listMountedTrustedCertificatesInOnboardingPod(config);
            LOGGER.info("List of certificates from /run/secrets/ca/root folder in onboarding pod {}", certificates);
            if (certificates.isBlank()) {
                LOGGER.info("Certificates folder is empty, continue to wait and retry");
            } else {
                LOGGER.info("Certificates were mounted successfully");
                return;
            }
            TimeUnit.SECONDS.sleep(2);
        }
        fail(String.format("Unable to find mounted certificates in onboarding pod in the provided time: %s seconds", DEFAULT_CERTIFICATES_MOUNT_TIMEOUT));
    }

    private static void verifyAllCertificatesWereUploaded(final TrustedCertificatesRequest expectedCertificates, final User user) {
        TrustedCertificatesResponse actualCertificates = listInstalledTrustedCertificates(user);
        assertThat(actualCertificates.getDescription())
                .withFailMessage("Trusted certificates description does not match")
                .isEqualTo(expectedCertificates.getDescription());

        Set<Certificate> expectedCertificatesSet = new HashSet<>(expectedCertificates.getCertificates());
        Set<Certificate> actualCertificatesSet = new HashSet<>(actualCertificates.getCertificates());
        assertThat(actualCertificatesSet).withFailMessage(
                        "Installed trusted certificates do not match expected list of certificates, expected: %s, actual %s",
                        expectedCertificatesSet, actualCertificatesSet)
                .isEqualTo(expectedCertificatesSet);
    }

    public static void verifyCertificatesDeletedSuccessfully(
            final ResponseEntity<TrustedCertificateManagementDetails> response, final ClusterConfig config) throws InterruptedException {
        assertThat(response.getStatusCode())
                .withFailMessage(String.format("Expected delete trusted certificates response to be 200 OK status code, actual code is %s",
                                               response.getStatusCode()))
                .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .withFailMessage("Delete trusted certificates response body should not be null")
                .isNotNull();
        assertThat(response.getBody().getMessage())
                .withFailMessage(String.format("Delete trusted certificates response message does not match, actual is %s",
                                               response.getBody().getMessage()))
                .isEqualTo(String.format("Deleted trusted-certificates '%s'", TRUSTED_CERTIFICATES_LIST));

        waitForCertificatesToBeUnmounted(config);
    }

    private static void waitForCertificatesToBeUnmounted(final ClusterConfig config) throws InterruptedException {
        LOGGER.info("Waiting for the uploaded certificates to disappear in the onboarding pod");
        StopWatch stopwatch = StopWatch.createStarted();
        while (stopwatch.getTime(TimeUnit.SECONDS) < DEFAULT_CERTIFICATES_MOUNT_TIMEOUT) {
            String certificates = listMountedTrustedCertificatesInOnboardingPod(config);
            LOGGER.info("List of certificates from /run/secrets/ca/root folder in onboarding pod {}", certificates);
            if (certificates.isBlank()) {
                LOGGER.info("Certificates were unmounted successfully");
                return;
            } else {
                LOGGER.info("Certificates folder is not empty, continue to wait and retry");
            }
            TimeUnit.SECONDS.sleep(2);
        }
        fail(String.format("Unable to verify that certificates are unmounted from onboarding pod in the provided time: %s seconds", DEFAULT_CERTIFICATES_MOUNT_TIMEOUT));
    }

    public static void verifyCertificatesWereNotInstalledWithException(final ProcessExecutorResponse installCertificatesResponse) {
        assertThat(installCertificatesResponse.getExitValue())
                .withFailMessage("Expected script to return non-zero value on failed installation of certificates")
                .isNotEqualTo(0);
    }
}
