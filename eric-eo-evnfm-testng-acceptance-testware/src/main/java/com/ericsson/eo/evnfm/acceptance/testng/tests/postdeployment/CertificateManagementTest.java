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
package com.ericsson.eo.evnfm.acceptance.testng.tests.postdeployment;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.UploadCertificatesDataProvider;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.Base;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.User;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.installTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.listTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementScriptClient.printCertificateManagementHelp;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.deleteTrustedCertificatesUsingRest;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.installTrustedCertificatesUsingScript;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementVerify.verifyCertificatesWereNotInstalledWithException;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementVerify.verifyHelpPrintedSuccessfully;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementVerify.verifyListCertificatesReturnedExceptionDueToInvalidCredentials;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

public class CertificateManagementTest extends Base {

    @Test(description = "Print help")
    public void testPrintHelp() throws IOException {
        ProcessExecutorResponse helpResponse = printCertificateManagementHelp();
        verifyHelpPrintedSuccessfully(helpResponse);
    }

    @Test(description = "List trusted certificates using invalid credentials",
            dataProvider = "testListCertificatesWithInvalidCredentialsData", dataProviderClass = UploadCertificatesDataProvider.class)
    public void testListCertificatesWithInvalidCredentials(final ClusterConfig config) throws IOException, URISyntaxException {
        User invalidUser = new User("invalid-id", "invalid-user", "invalid-password");
        ProcessExecutorResponse listCertificatesResponse = listTrustedCertificates(config, invalidUser);
        verifyListCertificatesReturnedExceptionDueToInvalidCredentials(listCertificatesResponse);
    }

    @Test(description = "Install valid trusted certificates",
            dataProvider = "testInstallValidCertificatesData", dataProviderClass = UploadCertificatesDataProvider.class)
    public void testInstallValidCertificates(final TrustedCertificatesRequest certificatesRequest, final ClusterConfig config) throws IOException, URISyntaxException, InterruptedException {
        User adminUser = new User("admin", EVNFM_INSTANCE.getIdamAdminUser(), EVNFM_INSTANCE.getIdamAdminPassword());
        installTrustedCertificatesUsingScript(certificatesRequest, config, adminUser);
        deleteTrustedCertificatesUsingRest(config, adminUser);
    }

    @Test(description = "Upload trusted certificates with invalid login",
            dataProvider = "testInstallInvalidCertificatesData", dataProviderClass = UploadCertificatesDataProvider.class)
    public void testInstallInvalidCertificates(final TrustedCertificatesRequest certificatesRequest, final ClusterConfig config)
            throws IOException, URISyntaxException {
        User adminUser = new User("admin", EVNFM_INSTANCE.getIdamAdminUser(), EVNFM_INSTANCE.getIdamAdminPassword());
        ProcessExecutorResponse installCertificatesResponse = installTrustedCertificates(certificatesRequest, config, adminUser);
        verifyCertificatesWereNotInstalledWithException(installCertificatesResponse);
    }

}
