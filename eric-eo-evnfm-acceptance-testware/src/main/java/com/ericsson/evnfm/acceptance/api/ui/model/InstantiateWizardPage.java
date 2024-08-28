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
package com.ericsson.evnfm.acceptance.api.ui.model;

import com.ericsson.evnfm.acceptance.api.ui.Navigation;
import org.openqa.selenium.remote.RemoteWebDriver;

public class InstantiateWizardPage {
    private RemoteWebDriver webDriver;

    public InstantiateWizardPage(RemoteWebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public ResourcesPage goToResourcesPage() {
        return Navigation.loadResourcesApplication(webDriver);
    }

    public PackagesPage goToPackagesPage() {
        return Navigation.loadPackagesApplication(webDriver);
    }

    public OperationsPage goToOperationsPage() {
        return Navigation.loadOperationsApplication(webDriver);
    }
}
