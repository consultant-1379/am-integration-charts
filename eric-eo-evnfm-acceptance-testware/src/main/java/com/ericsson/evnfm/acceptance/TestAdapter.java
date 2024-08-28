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
package com.ericsson.evnfm.acceptance;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.EVNFM_PASSWORD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.EVNFM_SUPER_USER_ROLE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.EVNFM_UI_USER_ROLE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.EVNFM_USERNAME;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.WEBDRIVER_GECKO_DRIVER;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BrowserWebDriverContainer;

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.ui.resources.AuthenticationStep;
import com.ericsson.evnfm.acceptance.utils.KeycloakHelper;

public class TestAdapter {

    public static RemoteWebDriver driver;
    private static User user;
    private static Logger LOGGER = LoggerFactory.getLogger(TestAdapter.class);

    @BeforeAll
    public static void setup() throws IOException {
        if (ConfigurationProvider.getTestInfo().getPhase() == 0) {
            if (!ConfigurationProvider.isPreconfigured()) {
                ConfigurationProvider.setConfiguration("config.json");
            }
            String userId = KeycloakHelper.createUser(ConfigurationProvider.getGeneralConfig(), EVNFM_USERNAME, EVNFM_PASSWORD, EVNFM_SUPER_USER_ROLE,
                                       EVNFM_UI_USER_ROLE);
            configureDriver();
            AuthenticationStep.login(driver, EVNFM_USERNAME,EVNFM_PASSWORD,ConfigurationProvider.getGeneralConfig().getApiGatewayHost());
            user = new User(userId, EVNFM_USERNAME, EVNFM_PASSWORD);
        }
    }

    @AfterAll
    public static void cleanUp() {
        if (ConfigurationProvider.getTestInfo().getPhase() == 0) {
            AuthenticationStep.logout(driver);
            KeycloakHelper.deleteUser(ConfigurationProvider.getGeneralConfig(), user);
            safelyQuitWebDriver(driver);
        }
    }

    private static void configureDriver() {
        if (System.getProperty(WEBDRIVER_GECKO_DRIVER) == null) {
            if (SystemUtils.IS_OS_WINDOWS) {
                System.setProperty(WEBDRIVER_GECKO_DRIVER, "src/main/resources/geckodriver.exe");
            } else {
                System.setProperty(WEBDRIVER_GECKO_DRIVER, "src/main/resources/geckodriver");
            }
        }

        String dockerImageName = "selenium/standalone-firefox:3.141.59-titanium";
        if (System.getProperty("seleniumContainerName") != null) {
            dockerImageName = System.getProperty("seleniumContainerName");
        }
        BrowserWebDriverContainer firefox68Rule =
                new BrowserWebDriverContainer(dockerImageName).withCapabilities(new FirefoxOptions());
        firefox68Rule.start();
        driver = setupDriverFromRule(firefox68Rule);
    }

    public static RemoteWebDriver setupDriverFromRule(BrowserWebDriverContainer rule) {
        RemoteWebDriver driver = rule.getWebDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return driver;
    }

    public static void safelyQuitWebDriver(RemoteWebDriver driver) {
        try {
            driver.quit();
        } catch (Exception e) {
            LOGGER.error("Couldn't quit remoteWebDriver", e);
        }
    }
}
