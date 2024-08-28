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
package com.ericsson.evnfm.acceptance.steps.ui.resources;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.ericsson.evnfm.acceptance.api.ui.Navigation;

public class AuthenticationStep {

    private AuthenticationStep(){}

    public static void login(RemoteWebDriver driver, String username, String password, String apiGatewayHost) {
            String rootURL = apiGatewayHost + UI_ROOT_PATH;
            Navigation.goTo(driver, rootURL);
            WebElement usernameField = driver.findElement(By.id("username"));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("kc-login-input"));
            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            assertThat(loginButton).isNotNull();
            loginButton.click();
    }

    public static void logout(RemoteWebDriver driver) {
            WebElement userLogoutPanel = (WebElement) querySelect(driver, "div[data-payload*=user-logout-panel]", true);
            assertThat(userLogoutPanel).isNotNull();
            userLogoutPanel.click();
            WebElement signoutButton = (WebElement) querySelect(driver, "e-user-logout-panel eui-base-v0-button", true);
            assertThat(signoutButton).isNotNull();
            signoutButton.click();
    }
}
