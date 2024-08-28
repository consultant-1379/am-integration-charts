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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickCheckbox;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;

import java.time.Duration;

public class OnboardPackageDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardPackageDialog.class);

    private static final String FILE_UPLOAD_BUTTON = "input[type=file]";
    private static final String ONBOARD_PACKAGE_BUTTON = "#fileUpload-uploadButton";
    private static final String ONBOARDING_DIALOG_SELECTOR = "eui-base-v0-dialog[label=\"Onboard CSAR file\"] .dialog";
    private static final String SKIP_IMAGE_UPLOAD_CHECKBOX_SELECTOR = "#skipImage-checkBox .checkbox__input";

    public PackagesPage uploadFile(RemoteWebDriver driver, EvnfmBasePackage packages, boolean skipImageUpload) {
        driver.setFileDetector(new LocalFileDetector());
        RemoteWebElement addFile = (RemoteWebElement) querySelect(driver, FILE_UPLOAD_BUTTON);
        addFile.sendKeys("/tmp/" + StringUtils.getFilename(packages.getPackageName()));
        if (skipImageUpload) {
            clickCheckbox(driver, SKIP_IMAGE_UPLOAD_CHECKBOX_SELECTOR);
        }
        clickOnTheButton(driver, ONBOARD_PACKAGE_BUTTON);
        checkOnboardingDialogIsClosed(driver);
        return new PackagesPage();
    }

    public PackagesPage uploadFile(RemoteWebDriver driver, EvnfmBasePackage evnfmPackage) {
        return uploadFile(driver, evnfmPackage, evnfmPackage.isSkipImageUpload());
    }

    public static void checkOnboardingDialogIsClosed(RemoteWebDriver driver) {
        LOGGER.info("Verify that onboarding dialog is closed");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30L));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(ONBOARDING_DIALOG_SELECTOR)));
        Assertions.assertThat(querySelect(driver, ONBOARDING_DIALOG_SELECTOR)).isNull();
    }
}
