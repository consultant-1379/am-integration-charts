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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clearTextField;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutWizardSelectCombobox;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillTextField;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;

public class InstantiateWizardInfrastructurePage {

    private static final String NEXT_BUTTON_SELECTOR = "#next";
    private static final String NAMESPACE_FIELD_SELECTOR = "#namespace-name";
    private static final String CLUSTER_COMBOBOX = "e-generic-combo-box#cluster-name";
    private static final String CLUSTER_COMBOBOX_ID = "cluster-name";

    public void setClusterName(RemoteWebDriver driver, String clusterName) {
        WebElement clusterNameCombobox = querySelect(driver, CLUSTER_COMBOBOX);
        fillOutWizardSelectCombobox(driver, CLUSTER_COMBOBOX_ID, clusterName, clusterNameCombobox);
    }

    public void setNamespace(RemoteWebDriver driver, String namespace) {
        clearTextField(driver, NAMESPACE_FIELD_SELECTOR);
        fillTextField(driver, NAMESPACE_FIELD_SELECTOR, namespace);
    }

    public InstantiateWizardGeneralAttributesPage goToInstantiateWizardGeneralAttributesPage(RemoteWebDriver driver) {
        clickOnTheButton(driver, NEXT_BUTTON_SELECTOR);
        return new InstantiateWizardGeneralAttributesPage();
    }
}
