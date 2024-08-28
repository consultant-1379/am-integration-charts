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
package com.ericsson.evnfm.acceptance.api.ui;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.querySelectAll;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementToBeEnabled;
import static com.ericsson.evnfm.acceptance.api.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CHECKBOX;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.DATE_PICKER;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.DROPDOWN;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.FILE_INPUT;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.PASSWORD_FIELD;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.RADIO_BUTTON;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TEXTAREA;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TEXTFIELD;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.fillOutWizardSelectDropdown;
import static org.assertj.core.api.Assertions.assertThat;

public class Wizard {

    private static final Logger LOGGER = LoggerFactory.getLogger(Selection.class);
    public static final String DROPDOWN_MENU_SELECTOR = "e-generic-dropdown[label=\"%s\"] div.menu eui-base-v0-radio-button";

    private static void fillOutWizardValue(RemoteWebDriver driver, String selector, String keyValue) {
        WebElement inputField = (WebElement) querySelect(driver, selector, true);
        if(inputField.isEnabled()) {
            if (!inputField.getAttribute("value").isEmpty()) {
                inputField.clear();
            }
            inputField.click();
            Actions builder = new Actions(driver);
            builder.sendKeys(keyValue);
            builder.perform();
            assertThat(inputField.getAttribute("value")).isEqualTo(keyValue);
        }
    }

    public static void fillOutWizardInput(RemoteWebDriver driver, String keyName, String keyValue) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        if(!StringUtils.isEmpty(keyValue)) {
            fillOutWizardValue(driver, "input[placeholder=\"" + keyName + "\"]", keyValue);
        } else {
            LOGGER.info("{} is not set to skipping in UI", keyName);
        }
    }

    public static void fillOutWizardTextArea(RemoteWebDriver driver, String keyName, String keyValue) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        fillOutWizardValue(driver, "textarea[placeholder=\"" + keyName + "\"]", keyValue);
    }

    public static void fillOutWizardFieldsAdditionalAttributes (RemoteWebDriver driver, Map<String, Object> additionalAttributes) {
        if (additionalAttributes == null || additionalAttributes.isEmpty()) {
            return;
        }
        ArrayList<WebElement> fields = querySelectAll(driver, "div.centeredParent div.table div.tr div.td.paddingInputTd div.input");
        for (WebElement element : fields) {

            int numberOfChildToFindTypeOfElement = 1;
            String elementType = element.findElement(By.cssSelector(":first-child")).getTagName();
            if (!elementType.contains("eui-base-v0")&&!elementType.contains("e-generic-dropdown") && !elementType.contains("e-generic-key-map-card-group")){
                numberOfChildToFindTypeOfElement = 2;
                elementType = element.findElement(By.cssSelector(":nth-child(" + numberOfChildToFindTypeOfElement + ")")).getTagName();
            }

            String label = element.findElement(By.cssSelector(":nth-child(" + numberOfChildToFindTypeOfElement + ")")).getAttribute("name");
            if(label==null)
                label = element.findElement(By.cssSelector(":nth-child(" + numberOfChildToFindTypeOfElement + ")")).getAttribute("label");
            Object value = additionalAttributes.get(label);

            if(value!=null){
                switch (elementType) {
                    case TEXTAREA:
                        fillOutWizardTextArea(driver, label, (String)value);
                        break;
                    case CHECKBOX:
                        LOGGER.warn("Ignoring checkbox additional attributes temporarily");
                        break;
                    case TEXTFIELD:
                    case PASSWORD_FIELD:
                        if (value instanceof Integer) {
                            fillOutWizardInput(driver, label, value.toString());
                        } else {
                            fillOutWizardInput(driver, label, (String) value);
                        }
                        break;
                    case DROPDOWN:
                        fillOutWizardSelectDropdown(driver, label, value, element);
                        break;
                    case RADIO_BUTTON:
                        WebElement radioButtonElement = querySelect(driver, "eui-base-v0-radio-button[name=\"" + label + "-" + value + "\"]");
                        radioButtonElement.click();
                        break;
                    case FILE_INPUT:
                        LOGGER.warn("Ignoring input file additional attributes temporarily");
                        break;
                    case DATE_PICKER:
                        WebElement datePickerElement = querySelect(driver, "e-generic-datepicker[data-attribute=\"" + label + "\"] input");
                        datePickerElement.clear();
                        Actions builder = new Actions(driver);
                        builder.click(datePickerElement);
                        builder.sendKeys((CharSequence) value);
                        builder.click(datePickerElement);
                        builder.perform();
                        break;
                    default:
                        LOGGER.error("Element type is not recognized.");
                }
            }
        }
    }

    public static void checkCurrentWizardStep(RemoteWebDriver driver, String expectedStep) {
        LOGGER.info("Checking current wizard step is {}", expectedStep);
        waitForElementWithText(driver, "eui-layout-v0-wizard-step[selected] .title", expectedStep, 5000, 1000);
    }

    private static void clickWizardButton(RemoteWebDriver driver, String buttonId) {
        WebElement wizardButton = (WebElement) querySelect(driver, "eui-base-v0-button#" + buttonId, true);
        waitForElementToBeEnabled(driver, wizardButton);
        assertThat(wizardButton).isNotNull();
        wizardButton.click();
    }

    public static void clickNextWizardStepButton(RemoteWebDriver driver, String expectedStepNumber) {
        LOGGER.info("Click to page {} on wizard", expectedStepNumber);
        clickWizardButton(driver, "next");
        waitForElementWithText(driver, ".current", expectedStepNumber, 5000, 1000);
    }

    public static void clickFinishWizardStepButton(RemoteWebDriver driver, String expectedDialogTitle) {
        LOGGER.info("Click wizard finish button, expecting {}", expectedDialogTitle);
        clickWizardButton(driver, "finish");
        waitForElementWithText(driver, ".dialog__title", expectedDialogTitle, 5000, 1000);
    }

    // Upgrade Specific
    public static void checkReadOnlyFieldsInfrastructure(RemoteWebDriver driver, String fieldValue) {
        WebElement field = (WebElement) querySelect(driver, " .key-value-list-value[title=" + fieldValue + "]", true);
        assertThat(field).isNotNull();
        assertThat(field.getText()).isEqualTo(fieldValue);
    }

    public static void checkReadOnlyFieldsGeneralAttributes(RemoteWebDriver driver, String fieldTitle, String fieldValue) {
        WebElement field = (WebElement) querySelect(driver, "." + fieldTitle + " .fieldValue", true);
        assertThat(field).isNotNull();
        assertThat(field.getText()).isEqualTo(fieldValue);
    }

    public static void setCheckbox(RemoteWebDriver driver, String selector, boolean value) {
        LOGGER.info("Setting checkbox {} with value {}", selector, value);
        WebElement checkbox = (WebElement) querySelect(driver, selector, true);
        if ((checkbox.isSelected() && !value) || (!checkbox.isSelected() && value)) {
            checkbox.click();
        }
    }
}
