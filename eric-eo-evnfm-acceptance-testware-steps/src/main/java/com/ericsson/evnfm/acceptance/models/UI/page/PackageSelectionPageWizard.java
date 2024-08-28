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
import static com.ericsson.evnfm.acceptance.commonUIActions.TableActions.clickTableRowPackageByName;

import org.openqa.selenium.remote.RemoteWebDriver;

public class PackageSelectionPageWizard {

    private static final String NEXT_BUTTON_SELECTOR = "#next";

    public void selectPackage(RemoteWebDriver driver, String packageName){
        clickTableRowPackageByName(driver, packageName);
    }

    public InstantiateWizardInfrastructurePage goToInstantiateWizardInfrastructurePage(RemoteWebDriver driver){
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new InstantiateWizardInfrastructurePage();
    }

    public UpgradeWizardInfrastructurePage goToUpgradeWizardInfrastructurePage(RemoteWebDriver driver){
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new UpgradeWizardInfrastructurePage();
    }
}
