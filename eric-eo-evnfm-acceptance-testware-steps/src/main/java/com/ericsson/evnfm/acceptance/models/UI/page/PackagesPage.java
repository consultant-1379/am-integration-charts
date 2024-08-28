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
package com.ericsson.evnfm.acceptance.models.UI.page;

import static org.awaitility.Awaitility.await;

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.waitForElement;
import static com.ericsson.evnfm.acceptance.commonUIActions.ContextMenuActions.clickTableContextWithRetry;
import static com.ericsson.evnfm.acceptance.commonUIActions.TableActions.getTableRowByPackageName;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.getPackageByVnfdIdentifier;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_PACKAGE_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.Constants.PACKAGE_DETAILS_MENU_ITEM;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.User;

public class PackagesPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackagesPage.class);
    private static final String ONBOARD_NEW_BUTTON_SELECTOR = "#packages_onboarding_button";
    private static final String PACKAGE_ROW_ID_SELECTOR = "e-custom-cell#";
    private static final String TABLE_ROW_ID_SELECTOR = "#%s";
    private static final String MENU_OPTION_SELECTOR = ".menu-option[value=\"%s\"]";

    public void verifyPackagesPageIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Check that Packages page is opened");
        checkCurrentPage(driver, PACKAGES);
    }

    public OnboardPackageDialog openOnboardPackageDialog(RemoteWebDriver driver) {
        clickOnTheButton(driver, ONBOARD_NEW_BUTTON_SELECTOR);
        return new OnboardPackageDialog();
    }

    public void verifyPackageIsOnboarded(RemoteWebDriver driver, EvnfmBasePackage packageToOnboard) {
        LOGGER.info("Verifying package {} onboarded in timeout {}",
                    FilenameUtils.removeExtension(StringUtils.getFilename(packageToOnboard.getPackageName())),
                    100);
        await().timeout(100, TimeUnit.SECONDS).pollDelay(10, TimeUnit.SECONDS).until(
                packageIsOnboarded(driver,
                                   getFormattedPackageName(packageToOnboard)));
    }

    private String getFormattedPackageName(final EvnfmBasePackage packageToOnboard) {
        return FilenameUtils.removeExtension(StringUtils.getFilename(packageToOnboard.getPackageName()).replace("-imageless", ""));
    }

    public Callable<Boolean> packageIsOnboarded(RemoteWebDriver driver, String packageName) {
        LOGGER.debug("Verify that the package has been onboarded successfully");
        final Optional<WebElement> webElement = refreshAndGetTableRowByPackageName(driver, packageName);
        return webElement::isPresent;
    }

    public Boolean packageIsNotOnboarded(RemoteWebDriver driver, String packageName) {
        LOGGER.debug("Verify that the package has been deleted");
        final Optional<WebElement> webElement = refreshAndGetTableRowByPackageName(driver, packageName);
        return webElement.isEmpty();
    }

    private Optional<WebElement> refreshAndGetTableRowByPackageName(RemoteWebDriver driver, String packageName) {
        driver.navigate().refresh();
        verifyPackagesPageIsOpened(driver);
        return getTableRowByPackageName(driver, packageName);
    }

    public DeletePackageDialog openDeletePackageDialog(RemoteWebDriver driver, User user, EvnfmBasePackage packageToDelete) {
        LOGGER.info("Opening Packages page and choosing package to delete");
        String rowId = "row-" + getCnfPackageOnboardedId(user, packageToDelete);
        waitForElement(driver, new WebDriverWait(driver, Duration.ofSeconds(15L)), PACKAGE_ROW_ID_SELECTOR + rowId);
        LOGGER.debug("Clicking Delete Package option");
        clickTableContextWithRetry(driver, rowId,
                                   String.format(TABLE_ROW_ID_SELECTOR, rowId),
                                   String.format(MENU_OPTION_SELECTOR, DELETE_PACKAGE_MENU_ITEM));
        return new DeletePackageDialog();
    }

    public void verifyPackageIsNotOnboarded(final RemoteWebDriver driver, final EvnfmBasePackage cnfPackage) {
        LOGGER.info("Verifying package {} in not onboarded in timeout {}",
                    FilenameUtils.removeExtension(StringUtils.getFilename(cnfPackage.getPackageName())), 100);
        await().timeout(100, TimeUnit.SECONDS).pollDelay(10, TimeUnit.SECONDS).until(() -> packageIsNotOnboarded(driver,
                                                                                                                 getFormattedPackageName(cnfPackage)));
    }

    private String getCnfPackageOnboardedId(User user, EvnfmBasePackage packageToDelete) {
        return getPackageByVnfdIdentifier(packageToDelete.getVnfdId(), user)
                .map(VnfPkgInfo::getId).orElseThrow();
    }

    public PackageDetails openPackageDetails(RemoteWebDriver driver, User user, EvnfmBasePackage evnfmBasePackage) {
        LOGGER.info("Opening Packages details page");
        String rowId = "row-" + getCnfPackageOnboardedId(user, evnfmBasePackage);
        waitForElement(driver, new WebDriverWait(driver, Duration.ofSeconds(15L)), PACKAGE_ROW_ID_SELECTOR + rowId);
        LOGGER.debug("Clicking the Package row");
        clickTableContextWithRetry(driver, rowId,
                                   String.format(TABLE_ROW_ID_SELECTOR, rowId),
                                   String.format(MENU_OPTION_SELECTOR, PACKAGE_DETAILS_MENU_ITEM));
        return new PackageDetails();
    }
}
