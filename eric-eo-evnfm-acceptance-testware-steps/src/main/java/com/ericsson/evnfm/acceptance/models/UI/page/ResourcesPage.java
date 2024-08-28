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

import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.clickOnTheButton;
import static com.ericsson.evnfm.acceptance.commonUIActions.CommonActions.loadApplication;
import static com.ericsson.evnfm.acceptance.commonUIActions.ContextMenuActions.clickTableContextWithRetry;
import static com.ericsson.evnfm.acceptance.commonUIActions.TableActions.getTableRowByRowInstanceId;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyDay0SecretsCreated;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyDay0SecretsDeleted;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Navigation.checkCurrentPage;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelect;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.querySelectAll;
import static com.ericsson.evnfm.acceptance.steps.common.ui.Selection.waitForElementWithText;
import static com.ericsson.evnfm.acceptance.utils.Constants.BACKUP_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.GO_TO_DETAILS_PAGE_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.MODIFY_VNF_INFO_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.RESOURCES;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLBACK_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.SCALE_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.TERMINATE_MENU_ITEM;
import static com.ericsson.evnfm.acceptance.utils.Constants.UPGRADE_MENU_ITEM;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.commonUIActions.TableActions;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;

public class ResourcesPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesPage.class);

    private static final String INSTANTIATE_NEW_BUTTON_SELECTOR = "#appbar-component-container";
    private static final String RESOURCE_ROW_ID_SELECTOR = "e-custom-cell#";
    private static final String RESOURCE_STATE_ID_SELECTOR = "e-custom-cell-state#";
    private static final String RESOURCE_NAME_SELECTOR = "div[class=custom-table__cell_value][column=vnfInstanceName]";
    private static final String TABLE_ROW_ID_SELECTOR = "#%s";
    private static final String MENU_OPTION_SELECTOR = ".menu-option[value=\"%s\"]";

    public ResourcesPage() {
    }

    public ResourcesPage(RemoteWebDriver driver) {
        loadApplication(driver, RESOURCES);
    }

    public PackageSelectionPageWizard openInstantiateWizard(RemoteWebDriver driver) {
        clickOnTheButton(driver, INSTANTIATE_NEW_BUTTON_SELECTOR);
        return new PackageSelectionPageWizard();
    }

    public void verifyResourcesPageIsOpened(RemoteWebDriver driver) {
        LOGGER.info("Check that Resources page is opened");
        try {
            checkCurrentPage(driver, RESOURCES);
        } catch (RuntimeException exception) {
            LOGGER.info("Couldn't find the Resources page, retrying...");
            driver.navigate().refresh();
            checkCurrentPage(driver, RESOURCES);
        }
    }

    public ResourceInfo verifyResourceState(RemoteWebDriver driver, EvnfmCnf cnf, String... invalidOperationStates) {
        String timeout = cnf.getApplicationTimeout();
        String releaseName = cnf.getVnfInstanceName();
        String clusterName = cnf.getCluster().getUIName();
        String operationState = cnf.getExpectedOperationState();
        long applicationTimeoutMs = Long.parseLong(timeout) * 1000;
        final String processingState = "Processing";
        String rowId = releaseName + "__" + clusterName;
        LOGGER.debug("Waiting for resource {} to reach appear on Resources page", releaseName);
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName, applicationTimeoutMs, 15000, invalidOperationStates);
        LOGGER.debug("Waiting for resource {} to reach {} state", releaseName, processingState);
        waitForElementWithText(driver, RESOURCE_STATE_ID_SELECTOR + rowId, processingState, applicationTimeoutMs, 500, invalidOperationStates);
        verifyDay0SecretsCreated(cnf, applicationTimeoutMs, 500);
        LOGGER.debug("Waiting for resource {} to reach {} state", releaseName, operationState);
        waitForElementWithText(driver, RESOURCE_STATE_ID_SELECTOR + rowId, operationState, applicationTimeoutMs, 500, invalidOperationStates);
        verifyDay0SecretsDeleted(cnf);
        driver.navigate().refresh();
        return TableActions.getResourceInfoByResourceNameAndCluster(driver, releaseName, clusterName);
    }

    public ResourceInfo verifyResourceIsScaled(RemoteWebDriver driver, String releaseName, String clusterName, String operationState,
                                               String timeout, String scaleOperation, String... invalidOperationStates) {
        long applicationTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        WebElement tableRow = getTableRowByRowInstanceId(driver, rowId);
        long startTime = System.currentTimeMillis();
        try {
            do {
                String lcmOperation = tableRow.findElement(By.cssSelector("[column=lifecycleOperationType]")).getAttribute("title");
                if (lcmOperation.equals(scaleOperation)) {
                    LOGGER.debug("Waiting for resource to complete operation...");
                    waitForElementWithText(driver,
                                           RESOURCE_STATE_ID_SELECTOR + rowId,
                                           operationState,
                                           applicationTimeoutMs,
                                           500,
                                           invalidOperationStates);
                    return TableActions.getResourceInfoByResourceNameAndCluster(driver, releaseName, clusterName);
                }
            } while((System.currentTimeMillis()-startTime)< applicationTimeoutMs);
        } catch (RuntimeException e) {
            LOGGER.info("Scale operation could not be found");
        }

        return TableActions.getResourceInfoByResourceNameAndCluster(driver, releaseName, clusterName);
    }

    public PackageSelectionPageWizard openUpgradeDialog(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long applicationTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName, applicationTimeoutMs, 500);
        LOGGER.debug("Clicking upgrade option");
        clickTableContextWithRetry(driver, rowId, String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, UPGRADE_MENU_ITEM));
        return new PackageSelectionPageWizard();
    }

    public RollbackResourcePage openRollbackDialog(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long applicationTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName, applicationTimeoutMs, 500);
        LOGGER.debug("Clicking rollback option");
        clickTableContextWithRetry(driver, rowId, String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, ROLLBACK_MENU_ITEM));
        return new RollbackResourcePage();
    }

    public ModifyVnfInfoDialog openModifyVnfInfoDialog (RemoteWebDriver driver, String releaseName, String clusterName) {
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName);
        LOGGER.debug("Clicking modifyVnfInfo option");
        clickTableContextWithRetry(driver, rowId,  String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, MODIFY_VNF_INFO_MENU_ITEM));
        return new ModifyVnfInfoDialog ();
    }

    public ConfirmTerminationDialog openTerminateDialog(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long pageLoadTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId,  releaseName, pageLoadTimeoutMs, 500);
        clickTableContextWithRetry(driver, rowId, String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, TERMINATE_MENU_ITEM));
        return new ConfirmTerminationDialog();
    }

    public BackupResourceDialog openBackupResourceDialog(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long pageLoadTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName, pageLoadTimeoutMs, 500);
        LOGGER.debug("Clicking backup option");
        clickTableContextWithRetry(driver, rowId, String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, BACKUP_MENU_ITEM));
        return new BackupResourceDialog();
    }

    public ResourceDetailsPage openResourceDetailsPage(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long pageLoadTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId,  releaseName, pageLoadTimeoutMs, 500);
        LOGGER.debug("Clicking go to details page option");
        clickTableContextWithRetry(driver,
                                   rowId,
                                   String.format(TABLE_ROW_ID_SELECTOR, rowId),
                                   String.format(MENU_OPTION_SELECTOR, GO_TO_DETAILS_PAGE_MENU_ITEM));
        return new ResourceDetailsPage();
    }

    public ScaleResourcePage openScaleResourcePage(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        long pageLoadTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        waitForElementWithText(driver, RESOURCE_ROW_ID_SELECTOR + rowId, releaseName, pageLoadTimeoutMs, 500);
        LOGGER.debug("Clicking scale option");
        clickTableContextWithRetry(driver, rowId, String.format(TABLE_ROW_ID_SELECTOR, rowId), String.format(MENU_OPTION_SELECTOR, SCALE_MENU_ITEM));
        return new ScaleResourcePage();
    }

    public void verifyResourceIsTerminated(RemoteWebDriver driver, String releaseName, String clusterName, String timeout) {
        LOGGER.debug("Verify that resource is terminated and not displayed in the list of resources");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Long.parseLong(timeout)));
        String rowId = releaseName + "__" + clusterName;
        Optional<ArrayList<WebElement>> instances = Optional.ofNullable(querySelectAll(driver, RESOURCE_NAME_SELECTOR));
        instances.ifPresentOrElse(webElements -> {
            if (webElements.size() > 1) {
                wait.until(item -> watchResourceForTerminate(releaseName, driver));
            } else {
                wait.until(item -> querySelect(driver, RESOURCE_ROW_ID_SELECTOR + rowId) == null);
            }
        }, () -> fail("Cannot find resources"));
    }

    private boolean watchResourceForTerminate(String releaseName, final RemoteWebDriver driver) {
        Optional<ArrayList<WebElement>> listOfResources = Optional.ofNullable(querySelectAll(driver, RESOURCE_NAME_SELECTOR));
        try {
            Optional<WebElement> resource = listOfResources
                    .flatMap(webElements -> webElements.stream()
                            .filter(webElement -> webElement.getText().equalsIgnoreCase(releaseName))
                            .findFirst());
            if (resource.isEmpty()) {
                return true;
            }
        } catch (StaleElementReferenceException e) {
            LOGGER.info("Web elements is not attached to DOM, trying again.:: {}", e.getMessage());
            return false;
        }
        return false;
    }

    public ResourceInfo verifyResourceIsSynced(RemoteWebDriver driver, String releaseName, String clusterName, String operationState,
                                               String timeout, String scaleOperation, String... invalidOperationStates) {
        LOGGER.info("Verify that resource can be synced");
        long applicationTimeoutMs = Long.parseLong(timeout) * 1000;
        String rowId = releaseName + "__" + clusterName;
        WebElement tableRow = getTableRowByRowInstanceId(driver, rowId);
        long startTime = System.currentTimeMillis();
        try {
            do {
                String lcmOperation = tableRow.findElement(By.cssSelector("[column=lifecycleOperationType]")).getAttribute("title");
                if (lcmOperation.equals(scaleOperation)) {
                    LOGGER.info("Waiting for resource to complete operation...");
                    waitForElementWithText(driver,
                                           RESOURCE_STATE_ID_SELECTOR + rowId,
                                           operationState,
                                           applicationTimeoutMs,
                                           500,
                                           invalidOperationStates);
                    return TableActions.getResourceInfoByResourceNameAndCluster(driver, releaseName, clusterName);
                }
            } while((System.currentTimeMillis()-startTime)< applicationTimeoutMs);
        } catch (RuntimeException e) {
            LOGGER.error("Sync operation could not be found");
        }
        LOGGER.info("The resource is synced");
        return TableActions.getResourceInfoByResourceNameAndCluster(driver, releaseName, clusterName);
    }
}


