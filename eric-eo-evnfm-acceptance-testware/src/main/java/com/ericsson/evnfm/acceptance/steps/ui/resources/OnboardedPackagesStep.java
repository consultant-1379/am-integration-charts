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

import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import com.ericsson.evnfm.acceptance.api.ui.model.PackagesPage;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourcesPage;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UI_ROOT_PATH;

public class OnboardedPackagesStep {

    private static Logger LOGGER = LoggerFactory.getLogger(OnboardedPackagesStep.class); //TODO shadows superclass LOGGER

    public static void checkPackageNames(RemoteWebDriver driver, ConfigGeneral configGeneral, List<String> expectedPackageNames) {
        Navigation.goTo(driver, configGeneral.getApiGatewayHost() + UI_ROOT_PATH);
        List<PackagesPage.PackagesTableRow> tableRows = new ResourcesPage(driver)
                .goToPackagesPage()
                .getTableRows();
        List<String> actualPackageNames = tableRows
                .stream().map(packagesTableRow -> packagesTableRow.packageName).collect(Collectors.toList());
        LOGGER.info("UI. Actual package names: {}", actualPackageNames);

        List<String> packageNamesToFind = new ArrayList<>(expectedPackageNames);
        for (String foundPackageName : actualPackageNames) {
            packageNamesToFind.remove(foundPackageName);
        }

        for (String missingPackage : packageNamesToFind) {
            LOGGER.error("Missing package: " + missingPackage);
        }
        assertThat(packageNamesToFind).as("Failed to find some packages on the Packages page").isEmpty();
    }
}
