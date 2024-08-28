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

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelectExpectedAbsence;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;

import java.time.Duration;

public class RegisterClusterDialog {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterClusterDialog.class);

    private static final String SELECT_FILE_BUTTON = "input[type=file]";
    private static final String UPLOAD_BUTTON = "#Upload";
    private static final String REGISTER_CLUSTER_DIALOG_SELECTOR = "eui-base-v0-dialog[label=\"Register cluster\"] .dialog";

    public CISMClustersPage uploadFile(RemoteWebDriver driver, ClusterConfig clusterConfig) {
        driver.setFileDetector(new LocalFileDetector());
        RemoteWebElement addFile = (RemoteWebElement) querySelect(driver, SELECT_FILE_BUTTON);
        addFile.sendKeys(clusterConfig.getLocalPath());
        clickOnTheButton(driver, UPLOAD_BUTTON);
        checkRegisterClusterDialogIsClosed(driver);
        return new CISMClustersPage();
    }

    public static void checkRegisterClusterDialogIsClosed(RemoteWebDriver driver) {
        LOGGER.info("Verify that Register Cluster Dialog is closed");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30L));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(REGISTER_CLUSTER_DIALOG_SELECTOR)));
        final WebElement webElement = (WebElement) querySelectExpectedAbsence(driver, REGISTER_CLUSTER_DIALOG_SELECTOR);
        Assertions.assertThat(webElement).as("Register Cluster Dialog is not closed").isNull();
    }
}
