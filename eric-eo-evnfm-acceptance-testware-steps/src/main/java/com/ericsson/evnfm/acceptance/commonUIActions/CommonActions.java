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
package com.ericsson.evnfm.acceptance.commonUIActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelectAll;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementPresence;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementToBeEnabled;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.utils.Constants.CHECKBOX;
import static com.ericsson.evnfm.acceptance.utils.Constants.DATE_PICKER;
import static com.ericsson.evnfm.acceptance.utils.Constants.DROPDOWN;
import static com.ericsson.evnfm.acceptance.utils.Constants.FILE_INPUT;
import static com.ericsson.evnfm.acceptance.utils.Constants.KEY_MAP_CARD_GROUP;
import static com.ericsson.evnfm.acceptance.utils.Constants.PASSWORD_FIELD;
import static com.ericsson.evnfm.acceptance.utils.Constants.RADIO_BUTTON;
import static com.ericsson.evnfm.acceptance.utils.Constants.TEXTAREA;
import static com.ericsson.evnfm.acceptance.utils.Constants.TEXTFIELD;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class CommonActions {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonActions.class);
    private static final String APP_CONTAINER = "eui-container";
    private static final String DROPDOWN_MENU_SELECTOR = "e-generic-dropdown[label=\"%s\"] div.menu eui-base-v0-radio-button";
    private static final String DROPDOWN_ITEM_SELECTOR = "eui-base-v0-radio-button";
    private static final String DROPDOWN_MENU_OPTION_SELECTOR = "eui-base-v0-radio-button[name=\"%s\"]";
    public static final String ALL_ADDITIONAL_ATTRIBUTES_SELECTOR = ".editAttribute";
    private static final String ALL_SECRET_NAME_TEXT_FIELDS_SELECTOR = ALL_ADDITIONAL_ATTRIBUTES_SELECTOR +
            " div#cards div.generic-ui-card div.content div#keyTextFieldContainer eui-base-v0-text-field#keyTextField";
    private static final String ALL_SECRET_KEY_TEXT_FIELDS_SELECTOR = ALL_ADDITIONAL_ATTRIBUTES_SELECTOR +
            " div#cards div.generic-ui-card div.content div.container div.key e-generic-text-field.textField";
    private static final String ALL_SECRET_VALUE_TEXT_FIELDS_SELECTOR = ALL_ADDITIONAL_ATTRIBUTES_SELECTOR +
            " div#cards div.generic-ui-card div.content div.container div.value e-generic-text-field.textField";
    private static final String WIZARD_INPUT_SELECTOR = "input[placeholder=\"%s\"]";
    private static final String TEXT_AREA_SELECTOR = "textarea[placeholder=\"%s\"]";
    private static final String DATE_PICKER_SELECTOR = "e-generic-datepicker[data-attribute=\"%s\"] input";
    private static final String RADIO_BUTTON_SELECTOR = "eui-base-v0-radio-button[name=\"%1$s-%2$s\"]";
    private static final String OPERATION_RESULT_MESSAGE_SELECTOR = "eui-base-v0-notification";
    private static final String COMBOBOX_MENU_SELECTOR = "e-generic-combo-box#%s div[menu-item]";
    private static final String EUI_COMBOBOX_MENU_SELECTOR = "eui-base-v0-combo-box#%s div[menu-item]";
    private static final String COMBOBOX_ITEM_SELECTOR = "div[menu-item]";
    private static final String ADD_SECRET_BUTTON_SELECTOR = "#addCardBtn";
    private static final String ADD_KEY_VALUE_PAIR_BUTTON_SELECTOR = "#addKeyValuePairBtn";
    public  static final String EXTENSIONS_PREFIX = "extension-vnfControlledScaling";
    private static final String SINGLE_NAME_RADIO_BUTTON_SELECTOR = "eui-base-v0-radio-button[name=\"%s\"]";

    private CommonActions() {
    }

    public static void clickOnTheButton(RemoteWebDriver driver, String buttonSelector) {
        LOGGER.info("Find the button by selector " + buttonSelector + " and click on it");
        waitForElementPresence(driver, By.cssSelector(APP_CONTAINER));
        WebElement button = querySelect(driver, buttonSelector);
        clickOnTheButton(driver, button);
    }

    public static void clickOnTheButton(RemoteWebDriver driver, WebElement button) {
        waitForElementToBeEnabled(driver, button);
        button.click();
    }

    public static void clickOnTheTab(RemoteWebDriver driver, String tabSelector) {
        LOGGER.info("Find the button by selector " + tabSelector + " and click on it");
        WebElement tab = querySelect(driver, tabSelector);
        assertThat(tab).isNotNull();
        tab.click();
    }

    public static void clickCheckbox(RemoteWebDriver driver, String checkboxSelector) {
        LOGGER.info("Find the checkbox by selector " + checkboxSelector + " and tick it");
        WebElement checkbox = querySelect(driver, checkboxSelector);
        assertThat(checkbox).isNotNull();
        checkbox.click();
    }

    public static void fillTextField(RemoteWebDriver driver, String textFieldSelector, String value) {
        WebElement textField = querySelect(driver, textFieldSelector);
        assertThat(textField).isNotNull();
        fillTextField(driver, textField, value);
    }

    public static void fillTextField(RemoteWebDriver driver, WebElement textField, String value) {
        textField.click();
        Actions builder = new Actions(driver);
        builder.sendKeys(value).perform();
    }

    public static void clearTextField(RemoteWebDriver driver, String textFieldSelector) {
        WebElement textField = querySelect(driver, textFieldSelector);
        assertThat(textField).isNotNull();
        clearTextField(driver, textField);
    }

    public static void clearTextField(RemoteWebDriver driver, WebElement textField) {
        textField.click();
        Actions builder = new Actions(driver);
        builder.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.BACK_SPACE).build().perform();
    }

    public static void fillOutWizardInput(RemoteWebDriver driver, String keyName, String keyValue) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        if (!StringUtils.isEmpty(keyValue)) {
            clearTextField(driver, String.format(WIZARD_INPUT_SELECTOR, keyName));
            fillTextField(driver, String.format(WIZARD_INPUT_SELECTOR, keyName), keyValue);
        } else {
            LOGGER.info("{} is not set to skipping in UI", keyName);
        }
    }

    public static void fillOutWizardInput(RemoteWebDriver driver, WebElement input, String inputName, String keyValue) {
        LOGGER.info("Fill {} with value {}", inputName, keyValue);
        if (!StringUtils.isEmpty(keyValue)) {
            clearTextField(driver, input);
            fillTextField(driver, input, keyValue);
        } else {
            LOGGER.info("{} is not set to skipping in UI", inputName);
        }
    }

    public static void fillOutWizardTextArea(RemoteWebDriver driver, String keyName, String keyValue) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        fillTextField(driver, String.format(TEXT_AREA_SELECTOR, keyName), keyValue);
    }

    public static void fillOutKeyMapCardGroup(RemoteWebDriver driver, String keyName, Map<String, Map<String, String>> keyValue) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);

        // create cards for every secret
        for (int i = 0; i < keyValue.size(); ++i) {
            clickOnTheButton(driver, ADD_SECRET_BUTTON_SELECTOR);
        }

        // create input fields for key-value pairs
        List<WebElement> addKeyValuePairButtons = querySelectAll(driver, ADD_KEY_VALUE_PAIR_BUTTON_SELECTOR);
        assertThat(keyValue).hasSameSizeAs(addKeyValuePairButtons);
        int buttonIndex = 0;
        for (Map.Entry<String, Map<String, String>> entry : keyValue.entrySet()) {
            WebElement addKeyValuePairButton = addKeyValuePairButtons.get(buttonIndex);
            for (int keyValuePairIndex = 0; keyValuePairIndex < entry.getValue().size() - 1; ++keyValuePairIndex) {
                clickOnTheButton(driver, addKeyValuePairButton);
            }
            ++buttonIndex;
        }

        // select all input fields for secret names and key-value pairs
        List<WebElement> secretNameTextFields = querySelectAll(driver, ALL_SECRET_NAME_TEXT_FIELDS_SELECTOR);
        List<WebElement> secretKeyTextFields = querySelectAll(driver, ALL_SECRET_KEY_TEXT_FIELDS_SELECTOR);
        List<WebElement> secretValueTextFields = querySelectAll(driver, ALL_SECRET_VALUE_TEXT_FIELDS_SELECTOR);
        int numberOfEntries = keyValue.values().stream().mapToInt(Map::size).sum();
        assertThat(secretNameTextFields.size()).isEqualTo(keyValue.size());
        assertThat(secretKeyTextFields.size()).isEqualTo(numberOfEntries);
        assertThat(secretValueTextFields.size()).isEqualTo(numberOfEntries);

        Iterator<WebElement> secretNameFieldsIterator = secretNameTextFields.iterator();
        Iterator<WebElement> secretKeyFieldsIterator = secretKeyTextFields.iterator();
        Iterator<WebElement> secretValueFieldsIterator = secretValueTextFields.iterator();

        // fill all selected input fields
        for (Map.Entry<String, Map<String, String>> secretEntry : keyValue.entrySet()) {
            String secretName = secretEntry.getKey();
            WebElement secretNameField = secretNameFieldsIterator.next();
            fillOutWizardInput(driver, secretNameField, "Secret name", secretName);
            for (Map.Entry<String, String> keyValueEntry : secretEntry.getValue().entrySet()) {
                String secretKey = keyValueEntry.getKey();
                String secretValue = keyValueEntry.getValue();
                WebElement secretKeyField = secretKeyFieldsIterator.next();
                WebElement secretValueField = secretValueFieldsIterator.next();
                fillOutWizardInput(driver, secretKeyField, "Secret key", secretKey);
                fillOutWizardInput(driver, secretValueField, "Secret value", secretValue);
            }
        }
    }

    public static void clickDropdownElement(WebElement dropdownElement) {
        List<WebElement> dropdownChildElements = dropdownElement.findElements(By.cssSelector(":first-child"));

        if (dropdownChildElements.isEmpty()) {
            dropdownElement.click();
        } else {
            dropdownChildElements.get(0).click();
        }
    }

    public static void fillOutWizardSelectDropdown(RemoteWebDriver driver, String keyName, Object keyValue, WebElement dropdownElement) {
        fillOutWizardFromList(driver, String.format(DROPDOWN_MENU_SELECTOR, keyName), DROPDOWN_ITEM_SELECTOR, keyName, keyValue, dropdownElement);
    }

    public static void fillOutWizardSelectCombobox(RemoteWebDriver driver, String keyName, Object keyValue, WebElement dropdownElement) {
        fillOutWizardFromList(driver, String.format(COMBOBOX_MENU_SELECTOR, keyName), COMBOBOX_ITEM_SELECTOR, keyName, keyValue, dropdownElement);
    }

    public static void fillOutEuiWizardSelectCombobox(RemoteWebDriver driver, String keyName, Object keyValue, WebElement dropdownElement) {
        fillOutWizardFromList(driver, String.format(EUI_COMBOBOX_MENU_SELECTOR, keyName), COMBOBOX_ITEM_SELECTOR, keyName, keyValue, dropdownElement);
    }

    public static void fillOutWizardFieldsAdditionalAttributes(RemoteWebDriver driver,
                                                               String additionalAttributesSelector,
                                                               Map<String, Object> additionalAttributes) {
        if (CollectionUtils.isEmpty(additionalAttributes)) {
            return;
        }
        ArrayList<WebElement> fields = querySelectAll(driver, additionalAttributesSelector);
        for (WebElement element : fields) {

            String elementType = element.getTagName();
            if (!elementType.contains("eui-base-v0") && !elementType.contains(DROPDOWN) && !elementType.contains(KEY_MAP_CARD_GROUP)) {
                elementType = element.findElement(By.cssSelector(":nth-child(2)")).getTagName();
            }

            String label = element.getAttribute("name");
            if (label == null) {
                label = element.getAttribute("label");
            }

            Object value = additionalAttributes.get(label);
            if (value != null) {
                switch (elementType) {
                    case TEXTAREA:
                        fillOutWizardTextArea(driver, label, (String) value);
                        break;
                    case CHECKBOX:
                        LOGGER.warn("Ignoring checkbox additional attributes temporarily");
                        break;
                    case TEXTFIELD:
                    case PASSWORD_FIELD:
                        fillOutWizardInput(driver, label, (String) value);
                        break;
                    case KEY_MAP_CARD_GROUP:
                        fillOutKeyMapCardGroup(driver, label, (Map<String, Map<String, String>>) value);
                        break;
                    case DROPDOWN:
                        fillOutWizardSelectDropdown(driver, label, value, element);
                        break;
                    case RADIO_BUTTON:
                        WebElement radioButtonElement = querySelect(driver, String.format(RADIO_BUTTON_SELECTOR, label, value));
                        radioButtonElement.click();
                        break;
                    case FILE_INPUT:
                        LOGGER.warn("Ignoring input file additional attributes temporarily");
                        break;
                    case DATE_PICKER:
                        WebElement datePickerElement = querySelect(driver, String.format(DATE_PICKER_SELECTOR, label));
                        datePickerElement.clear();
                        Actions builder = new Actions(driver);
                        builder.click(datePickerElement);
                        builder.sendKeys((CharSequence) value);
                        builder.click(datePickerElement);
                        builder.perform();
                        break;
                    default:
                        LOGGER.error("Element type {} is not recognized.", elementType);
                }
            }
        }
    }

    public static void selectDropdownOption(RemoteWebDriver driver, String keyName, String keyValue, String dropdownSelector) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        querySelect(driver, dropdownSelector).click();
        List<WebElement> listOfWebElements = querySelectAll(driver, keyName);
        assertThat(listOfWebElements).isNotNull();
        List<String> listOfDropdownValues = listOfWebElements.stream().map(WebElement::getText).collect(Collectors.toList());// NOSONAR
        if (listOfDropdownValues.stream().filter(item -> (item.equals(keyValue))).findFirst().get() != null) {
            waitForElementWithText(driver, String.format(DROPDOWN_MENU_OPTION_SELECTOR, keyValue), keyValue).click();
        }
    }

    public static void checkSelectedDropdownValue(RemoteWebDriver driver, String dropdownFieldSelector, String keyValue) {
        LOGGER.info("Check that correct value {} was selected in the dropdown field", keyValue);
        WebElement dropdownField = querySelect(driver, dropdownFieldSelector);
        assertThat(dropdownField).isNotNull();
        assertThat(dropdownField.getAttribute("label")).isEqualTo(keyValue);// NOSONAR
    }

    public static void checkOperationResultMessageIsReceived(RemoteWebDriver driver, String message) {
        LOGGER.info("Verify that operation result message " + message + " was received");
        waitForElementWithText(driver, OPERATION_RESULT_MESSAGE_SELECTOR, message);
    }

    public static void clickElement(final RemoteWebDriver driver, final WebElement element) {
        assertThat(element).isNotNull();
        scrollIntoView(driver, element);
        element.click();
    }

    public static void scrollIntoView(final RemoteWebDriver driver, final WebElement contextMenuItem) {
        driver.executeScript("arguments[0].scrollIntoView();", contextMenuItem);
    }

    private static void fillOutWizardFromList(RemoteWebDriver driver, String menuSelector, String itemSelector, String keyName, Object keyValue,
                                              WebElement listContainingElement) {
        LOGGER.info("Fill {} with value {}", keyName, keyValue);
        clickDropdownElement(listContainingElement);
        Optional<ArrayList<WebElement>> dropdownList = Optional.ofNullable(querySelectAll(driver, String.format(menuSelector, keyName)));
        Optional<WebElement> item = dropdownList.flatMap(webElements -> webElements.stream()
                .filter(webElement -> webElement.getText().equalsIgnoreCase(keyValue.toString())).findFirst());
        item.ifPresentOrElse(webElement -> waitForElementWithText(driver, itemSelector, webElement.getText()).click(),
                             () -> fail(String.format("There is no value \"%s\" in the list", keyValue)));
    }

    public static void setExtensions(RemoteWebDriver driver, final Map<String, Object> extensions) {
        if (extensions != null) {
            extensions.forEach((k, v) -> {
                var extension  = String.format("%s-%s-%s", EXTENSIONS_PREFIX, v, k);
                var radioButton  = String.format(SINGLE_NAME_RADIO_BUTTON_SELECTOR, extension);
                querySelect(driver, radioButton).click();
            });
        }
    }

    public static void setDescription(RemoteWebDriver driver, String description, String descriptionSelector) {
        if (description != null) {
            clearTextField(driver, descriptionSelector);
            fillTextField(driver, descriptionSelector, description);
        }
    }

    public static void loadApplication(RemoteWebDriver driver, String applicationName) {
        LOGGER.info("Load {}", applicationName);
        WebElement openAppNavigationFlyOutButton = (WebElement) querySelect(driver, "#AppBar-menu-toggle",true, 5000, 1000);
        if (openAppNavigationFlyOutButton != null) {
            openAppNavigationFlyOutButton.click();
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15L));
        ArrayList<WebElement> listOfApplications = (ArrayList<WebElement>) querySelect(driver, "eui-base-v0-tree-item", false, 5000, 1000);
        for (WebElement webElement : listOfApplications) {
            if (webElement.getText().equalsIgnoreCase(applicationName)) {
                wait.until(ExpectedConditions.elementToBeClickable(webElement));
                webElement.click();
                checkCurrentPage(driver, applicationName);
                WebElement closeAppNavigationFlyOutButton = (WebElement) querySelect(driver, "#AppBar-menu-toggle", true, 5000, 1000);
                if (closeAppNavigationFlyOutButton != null) {
                    closeAppNavigationFlyOutButton.click();
                }
                //Wait added to allow time for flyout to close completely
                waitForElement(driver, wait, ".menuhidden#LayoutHolder");
                return;
            }
        }
        throw new RuntimeException("Could not open application from application list");
    }

    public static void checkCurrentPage(RemoteWebDriver driver, String expectedPage) {
        waitForElementWithText(driver, ".current-page", expectedPage, 15000, 1000);
    }

    public static void waitForElement(RemoteWebDriver driver, WebDriverWait wait, String selector) {
        wait.until(ExpectedConditions.visibilityOf((WebElement) querySelect(driver, selector, true)));
    }
}
