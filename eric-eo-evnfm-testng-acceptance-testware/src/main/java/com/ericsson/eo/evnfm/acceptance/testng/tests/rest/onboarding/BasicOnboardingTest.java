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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.onboarding;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicOnboardingDataProviders.loadClusterConfig;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicOnboardingDataProviders.loadTrustedCertificates;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.deleteTrustedCertificatesUsingRest;
import static com.ericsson.evnfm.acceptance.steps.certificates.CertificateManagementSteps.installTrustedCertificatesUsingScript;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.LicenseAvailabilityVerify.verifyLicensesAvailable;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.retrieveVnfdAsZipRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.createPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.deleteOnboardedPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.getOnboardedVnfPackages;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.getPackageByVnfdIdentifier;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.onboardPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.unpackVnfdArchive;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyAdditionalValuesIfPresent;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyContainerRegistryHealthcheck;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyHealthcheck;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyHelmRegistryHealthcheck;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyVnfdArchive;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyZipVnfdResponse;
import static com.ericsson.evnfm.acceptance.utils.Constants.PACKAGE_VNFD_ID_NOT_FOUND;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicOnboardingDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.User;

public class BasicOnboardingTest extends RestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicOnboardingTest.class);

    @BeforeClass(alwaysRun = true)
    public void setUp(ITestContext iTestContext) throws IOException, URISyntaxException, InterruptedException {
        User adminUser = new User("admin", EVNFM_INSTANCE.getIdamAdminUser(), EVNFM_INSTANCE.getIdamAdminPassword());
        Optional<TrustedCertificatesRequest> certificates = loadTrustedCertificates(iTestContext);
        ClusterConfig config = loadClusterConfig(iTestContext);
        if (certificates.isPresent()) {
            installTrustedCertificatesUsingScript(certificates.get(), config, adminUser);
        } else {
            LOGGER.info("No certificates and cluster config configurations provided, skipping certificate installation");
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpAfterTest(ITestContext iTestContext) throws InterruptedException, IOException {
        User adminUser = new User("admin", EVNFM_INSTANCE.getIdamAdminUser(), EVNFM_INSTANCE.getIdamAdminPassword());
        Optional<TrustedCertificatesRequest> certificates = loadTrustedCertificates(iTestContext);
        ClusterConfig config = loadClusterConfig(iTestContext);
        if (certificates.isPresent()) {
            deleteTrustedCertificatesUsingRest(config, adminUser);
        } else {
            LOGGER.info("No certificates configuration provided, skipping certificate deletion");
        }
    }

    @Test(description = "Verify that licenses are available",
            dataProvider = "getClusterConfig", dataProviderClass = BasicOnboardingDataProviders.class)
    public void testLicensesAvailable(final ClusterConfig config) throws InterruptedException {
        verifyLicensesAvailable(config);
    }

    @Test(description = "Verify that Container registry and Helm registry are available")
    public void testHealthCheck() {
        verifyHelmRegistryHealthcheck(240, user);
        verifyContainerRegistryHealthcheck(240, user);
        verifyHealthcheck(240, user);
    }

    @Test(description = "Onboard a package", dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingDataProviders.class, dependsOnMethods = { "testLicensesAvailable", "testHealthCheck"} )
    public void testOnboardCsarREST(EvnfmBasePackage evnfmPackage) {
        String filename = evnfmPackage.getPackageName().substring(evnfmPackage.getPackageName().lastIndexOf("/") + 1);
        Optional<VnfPkgInfo> vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user);
        if (vnfPkgInfo.isEmpty()) {
            String packageId = createPackage(evnfmPackage.getTimeOut(), evnfmPackage.isSkipImageUpload(), user, filename);
            onboardPackage(evnfmPackage, packageId, user, false);
            evnfmPackage.setOnboardedId(packageId);
        } else {
            LOGGER.info("Package with vnfd id {} already onboarded", evnfmPackage.getVnfdId());
        }
    }

    @Test(description = "Test that zip archive with vnfd files has been onboarded correctly",
            dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingDataProviders.class)
    public void testVnfdZipOnboardedCorrectly(EvnfmBasePackage evnfmPackage) throws IOException, InterruptedException {
        VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user).orElseThrow(
                () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, evnfmPackage.getVnfdId())));
        ResponseEntity<byte[]> zipVnfdResponse = retrieveVnfdAsZipRequest(vnfPkgInfo.getId(), user);
        verifyZipVnfdResponse(evnfmPackage, zipVnfdResponse);
        Path archiveBaseDirectory = unpackVnfdArchive(evnfmPackage, zipVnfdResponse.getBody());
        verifyVnfdArchive(evnfmPackage, archiveBaseDirectory);
    }

    @Test(description = "Test that additional values files inside package are stored correctly",
            dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingDataProviders.class)
    public void testAdditionalValuesArtifacts(EvnfmBasePackage evnfmPackage) {
        VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user).orElseThrow(
                () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, evnfmPackage.getVnfdId())));
        verifyAdditionalValuesIfPresent(vnfPkgInfo, user);
    }

    @Test(description = "Onboard a package", dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingDataProviders.class, dependsOnMethods = {"testLicensesAvailable", "testHealthCheck" })
    public void testOnboardCsarRESTPhases(EvnfmBasePackage evnfmPackage) {
        verifyHealthcheck(240, user);
        String filename = evnfmPackage.getPackageName().substring(evnfmPackage.getPackageName().lastIndexOf("/") + 1);
        Optional<VnfPkgInfo> vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user);
        if (vnfPkgInfo.isEmpty()) {
            String packageId = createPackage(evnfmPackage.getTimeOut(), evnfmPackage.isSkipImageUpload(), user, filename);
            onboardPackage(evnfmPackage, packageId, user, true);
            evnfmPackage.setOnboardedId(packageId);
        } else {
            LOGGER.info("Package with vnfd id {} already onboarded", evnfmPackage.getVnfdId());
        }
    }

    @Test(description = "Delete a package from onboarding service", dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingDataProviders.class)
    public void testDeletePackageREST(EvnfmBasePackage evnfmPackage) {
        VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user).orElseThrow(
                () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, evnfmPackage.getVnfdId())));
        deleteOnboardedPackage(vnfPkgInfo.getId(), evnfmPackage.getVnfdId(), user);
    }

    @Test(description = "Delete all packages from onboarding service")
    public void testDeletePackagesRESTPhases() {
        List<EvnfmBasePackage> onboardedVnfPackages = getOnboardedVnfPackages(user);
        for (EvnfmBasePackage evnfmPackage : onboardedVnfPackages) {
            VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user).orElseThrow(
                    () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, evnfmPackage.getVnfdId())));
            deleteOnboardedPackage(vnfPkgInfo.getId(), evnfmPackage.getVnfdId(), user);
        }
    }
}
