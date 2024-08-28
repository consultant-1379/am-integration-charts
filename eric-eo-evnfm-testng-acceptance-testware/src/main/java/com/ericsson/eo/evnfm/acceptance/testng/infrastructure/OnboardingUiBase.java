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

import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getCsar;
import static com.ericsson.evnfm.acceptance.utils.Constants.WEBDRIVER_GECKO_DRIVER;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.utility.MountableFile;
import org.testng.annotations.BeforeClass;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.fasterxml.jackson.core.type.TypeReference;

public class OnboardingUiBase extends UiBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingUiBase.class);

    @Override
    @BeforeClass(alwaysRun = true)
    public void prepare() {
        user = Idam.createUser(configGeneral);
        configureDriver();
    }

    private void configureDriver() {
        if (System.getProperty(WEBDRIVER_GECKO_DRIVER) == null) {
            String dockerImageName = "selenium/standalone-firefox:3.141.59-titanium";
            if (System.getProperty("seleniumContainerName") != null) {
                dockerImageName = System.getProperty("seleniumContainerName");
            }
            try {
                List<MountableFile> onboardingUIPackage = getFilesForOnboardingUI();
                BrowserWebDriverContainer firefox68Rule = new BrowserWebDriverContainer(
                        dockerImageName).withCapabilities(new FirefoxOptions());
                firefox68Rule.start();
                for (MountableFile file : onboardingUIPackage) {
                    firefox68Rule.copyFileToContainer(file, "/tmp/");
                }
                String filesList = firefox68Rule.execInContainer("ls", "/tmp/").getStdout();
                LOGGER.info("Checking files in setup of WebDriver");
                LOGGER.info(filesList);
                driver = setupDriverFromRule(firefox68Rule);
            } catch (IOException | InterruptedException e) {
                fail("Could not onboard file for UI base due to {}", e.getMessage());
            }
        } else {
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setCapability("strictFileInteractability", false);
            driver = new FirefoxDriver(firefoxOptions);
        }
        driver.manage().window().maximize();
    }

    private List<MountableFile> getFilesForOnboardingUI() throws IOException {
        List<MountableFile> csarMountableFiles = new ArrayList<>();
        List<EvnfmBasePackage> packagesToOnboard = loadYamlConfiguration("cnfOnboardingUI.yaml", "packages", new TypeReference<>() {
        });
        for (EvnfmBasePackage basePackage : packagesToOnboard) {
            FileSystemResource file = getCsar(basePackage, EVNFM_INSTANCE.getCsarDownloadPath());
            csarMountableFiles.add(MountableFile.forHostPath(file.getPath()));
        }
        return csarMountableFiles;
    }
}
