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
package com.ericsson.evnfm.acceptance.steps.common.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.utils.Constants.KEYCLOAK_LOGIN_BUTTON;
import static com.ericsson.evnfm.acceptance.utils.Constants.KEYCLOAK_PASSWORD_ID;
import static com.ericsson.evnfm.acceptance.utils.Constants.KEYCLOAK_USERNAME_ID;
import static com.ericsson.evnfm.acceptance.utils.Constants.LOGOUT_USER_BUTTON;
import static com.ericsson.evnfm.acceptance.utils.Constants.LOGOUT_USER_PANEL;
import static com.ericsson.evnfm.acceptance.utils.Constants.UI_ROOT_PATH;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationStep {

    private AuthenticationStep(){}

    public static void login(RemoteWebDriver driver, String username, String password, String apiGatewayHost) {
            String rootURL = apiGatewayHost + UI_ROOT_PATH;
            Navigation.goTo(driver, rootURL);
            WebElement usernameField = driver.findElement(By.id(KEYCLOAK_USERNAME_ID));
            WebElement passwordField = driver.findElement(By.id(KEYCLOAK_PASSWORD_ID));
            WebElement loginButton = driver.findElement(By.id(KEYCLOAK_LOGIN_BUTTON));
            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            assertThat(loginButton).isNotNull();
            loginButton.click();
    }

    public static void logout(RemoteWebDriver driver) {
            WebElement userLogoutPanel = (WebElement) querySelect(driver, LOGOUT_USER_PANEL, true);
            assertThat(userLogoutPanel).isNotNull();
            userLogoutPanel.click();
            WebElement signoutButton = (WebElement) querySelect(driver, LOGOUT_USER_BUTTON, true);
            assertThat(signoutButton).isNotNull();
            signoutButton.click();
    }
}
