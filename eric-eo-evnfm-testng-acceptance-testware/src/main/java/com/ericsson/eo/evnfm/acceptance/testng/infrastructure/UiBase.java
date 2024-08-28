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
package com.ericsson.eo.evnfm.acceptance.testng.infrastructure;

import static com.ericsson.evnfm.acceptance.utils.Constants.WEBDRIVER_GECKO_DRIVER;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

// Base class for UI Specific configurations
public class UiBase extends Base {
    private static final Logger LOGGER = LoggerFactory.getLogger(UiBase.class);
    protected RemoteWebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void prepare() {
        user = Idam.createUser(configGeneral);
        configureDriver();
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        safelyQuitWebDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(driver, result.getTestName());
        }
    }

    private void safelyQuitWebDriver() {
        try {
            driver.quit();
        } catch (Exception e) {
            LOGGER.error("Couldn't quit remoteWebDriver", e);
        }
    }

    private void configureDriver() {
        if (System.getProperty(WEBDRIVER_GECKO_DRIVER) == null) {
            String dockerImageName = "selenium/standalone-firefox:3.141.59-titanium";
            if (System.getProperty("seleniumContainerName") != null) {
                dockerImageName = System.getProperty("seleniumContainerName");
            }
            BrowserWebDriverContainer firefox68Rule =
                    new BrowserWebDriverContainer(dockerImageName).withCapabilities(new FirefoxOptions());
            firefox68Rule.start();
            driver = setupDriverFromRule(firefox68Rule);
        } else {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            driver = new FirefoxDriver(firefoxOptions);
        }
        driver.manage().window().maximize();
    }

    RemoteWebDriver setupDriverFromRule(BrowserWebDriverContainer rule) {
        RemoteWebDriver driver = rule.getWebDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        return driver;
    }

    static void takeScreenshot(RemoteWebDriver driver, String imageName) {
        try {
            LOGGER.info("Creating screenshot {}-{}.png", imageName, System.currentTimeMillis());
            imageName += "-" + System.currentTimeMillis() + ".png";
            File src = driver.getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(src, new File("./target/screenshots/" + imageName));
        } catch (Exception e) {
            LOGGER.error("Failed to take a snapshot due to {}", e.getMessage());
        }
    }

}
