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

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.ericsson.evnfm.acceptance.models.TrustedCertificateManagementDetails;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesResponse;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.utils.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementApiClient.deleteTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementApiClient.listTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.installTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementVerify.verifyCertificatesDeletedSuccessfully;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementVerify.verifyCertificatesInstalledSuccessfully;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class CertificateManagementSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateManagementSteps.class);

    public static final String TRUSTED_CERTIFICATES_LIST = "onboarding-trusted-certs";

    private static final ProcessExecutor executor = new ProcessExecutor();

    public static TrustedCertificatesResponse listInstalledTrustedCertificates(final User user) {
        LOGGER.info("Listing installed trusted certificates");
        ResponseEntity<TrustedCertificatesResponse> response = listTrustedCertificates(TRUSTED_CERTIFICATES_LIST, user);
        assertThat(response.getStatusCode())
                .withFailMessage(String.format("Expected list trusted certificates response to be 200 OK status code, actual code is %s",
                                               response.getStatusCode()))
                .isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    public static void installTrustedCertificatesUsingScript(
            final TrustedCertificatesRequest certificates, final ClusterConfig config, final User user) throws IOException, URISyntaxException, InterruptedException {
        ProcessExecutorResponse installCertificatesResponse = installTrustedCertificates(certificates, config, user);
        verifyCertificatesInstalledSuccessfully(installCertificatesResponse, certificates, config, user);
    }

    public static void deleteTrustedCertificatesUsingRest(final ClusterConfig config, final User user) throws InterruptedException {
        LOGGER.info("Deleting installed trusted certificates");
        ResponseEntity<TrustedCertificateManagementDetails> response = deleteTrustedCertificates(TRUSTED_CERTIFICATES_LIST, user);
        verifyCertificatesDeletedSuccessfully(response, config);
    }

    public static String listMountedTrustedCertificatesInOnboardingPod(final ClusterConfig config) {
        try {
            String command = String.format("kubectl --kubeconfig %s -n %s exec -it $(kubectl --kubeconfig %s -n %s get pods | grep onboarding | awk '{print $1}') -- /bin/bash -c 'ls /run/secrets/ca/root'",
                    config.getLocalPath(), EVNFM_INSTANCE.getNamespace(), config.getLocalPath(), EVNFM_INSTANCE.getNamespace());
            ProcessExecutorResponse response = executor.executeProcess(command, 30, false);
            assertThat(response.getExitValue())
                    .withFailMessage("Command that listed mounted certificates exited with error code %s", response.getExitValue())
                    .isEqualTo(0);
            return response.getCommandOutput();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to list mounted certificates in onboarding pods", e);
            throw new RuntimeException(e);
        }
    }

}
