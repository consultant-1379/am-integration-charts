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
package com.ericsson.evnfm.acceptance.steps.onboarding.ui;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.UI.page.DeletePackageDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.OnboardPackageDialog;
import com.ericsson.evnfm.acceptance.models.UI.page.PackageDetails;
import com.ericsson.evnfm.acceptance.models.UI.page.PackagesPage;
import com.ericsson.evnfm.acceptance.models.UI.page.SupportedOperationsTab;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.common.ui.Navigation;

public class OnboardUISteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardUISteps.class);
    private static final String PACKAGES_RESOURCE = "/vnfm#packages";
    private PackagesPage packagesPage;
    private OnboardPackageDialog onboardPackageDialog;

    public void onboardPackageUI(RemoteWebDriver driver, EvnfmBasePackage packageToOnboard) {
        LOGGER.info("Opening package onboard dialog from the Packages page");
        String rootURL = EVNFM_INSTANCE.getEvnfmUrl() + PACKAGES_RESOURCE;
        Navigation.goTo(driver, rootURL);
        packagesPage = new PackagesPage();
        packagesPage.verifyPackagesPageIsOpened(driver);
        onboardPackageDialog = packagesPage.openOnboardPackageDialog(driver);
        packagesPage = onboardPackageDialog.uploadFile(driver, packageToOnboard);
        packagesPage.verifyPackageIsOnboarded(driver, packageToOnboard);
    }

    public void deletePackageUI(RemoteWebDriver driver, User user, EvnfmBasePackage packageToDelete) {
        LOGGER.info("Opening package delete dialog from the Packages page");
        String rootURL = EVNFM_INSTANCE.getEvnfmUrl() + PACKAGES_RESOURCE;
        Navigation.goTo(driver, rootURL);
        packagesPage = new PackagesPage();
        packagesPage.verifyPackagesPageIsOpened(driver);
        DeletePackageDialog deletePackageDialog = packagesPage.openDeletePackageDialog(driver, user, packageToDelete);
        packagesPage = deletePackageDialog.deletePackage(driver);
        packagesPage.verifyPackageIsNotOnboarded(driver, packageToDelete);
    }

    public Map<String, String> getSupportedOperationsStatus(RemoteWebDriver driver,
                                                            User user,
                                                            EvnfmBasePackage evnfmBasePackage) {
        LOGGER.info("Opening package details from the Packages page");
        String rootURL = EVNFM_INSTANCE.getEvnfmUrl() + PACKAGES_RESOURCE;
        Navigation.goTo(driver, rootURL);
        packagesPage = new PackagesPage();
        PackageDetails packageDetails = packagesPage.openPackageDetails(driver, user, evnfmBasePackage);
        packageDetails.verifyPackageDetailsPageIsOpened(driver);
        SupportedOperationsTab supportedOperationsTab = packageDetails.openSupportedOperationsTab(driver);
        return supportedOperationsTab.getOperationSates(driver);
    }

    public void verifySupportedOperationsStatus(Map<String, String> expected, Map<String, String> actual) {
        final Map<String, String> lowerCaseActual = actual.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().toLowerCase(), e -> e.getValue().toLowerCase()));
        assertThat(expected.equals(lowerCaseActual)).as("Comparing Supported Operations Statuses. \nExpected: %s \nActual: %s",
                                                        expected,
                                                        lowerCaseActual).isTrue();
    }
}
