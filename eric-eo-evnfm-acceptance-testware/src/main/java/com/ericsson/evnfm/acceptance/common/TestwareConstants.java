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
package com.ericsson.evnfm.acceptance.common;

import com.ericsson.amonboardingservice.model.AppPackageResponse;

public class TestwareConstants {

    // E-VNFM URIs
    public static final String ONBOARDING_HEALTHCHECK_URI = "/vnfm/onboarding/actuator/health";
    public static final String ONBOARDING_PACKAGES_URI = "/vnfm/onboarding/api/v1/packages";
    public static final String ETSI_CREATE_PACKAGE_URL = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages";
    public static final String ETSI_GET_PACKAGE_BY_ID_URI = ETSI_CREATE_PACKAGE_URL + "/%s";
    public static final String ETSI_ONBOARDING_PACKAGES_URI = ETSI_GET_PACKAGE_BY_ID_URI + "/package_content";
    public static final String CREATE_IDENTIFER_URI = "/vnflcm/v1/vnf_instances";
    public static final String VNF_INSTANCE_ID = "{vnfInstanceId}";
    public static final String QUERY_VNF_INSTANCE_URI = CREATE_IDENTIFER_URI + "/" + VNF_INSTANCE_ID;
    public static final String CLUSTER_UPLOAD_URI = "/vnfm/wfs/api/lcm/v2/cluster";
    public static final String TOKEN_URI_RESOURCE = "/auth/v1";
    public static final String DELETE_PACKAGE_URI = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages";
    public static final String ALL_HELM_CHARTS_URI = "/vnfm/onboarding/api/v1/charts";
    public static final String QUERY_VNFC_SCALE_INFO_URL = "/vnflcm/v1/resources/%s/vnfcScaleInfo%s";
    public static final String GET_RESOURCE_INFO_URL = "/vnflcm/v1/resources/%s";
    public static final String CLEAN_UP_URL = "/cleanup";
    public static final String API_GATEWAY_HOST = "http://localhost:8080";

    // Headers
    public static final String COOKIE = "cookie";
    public static final String JSESSIONID = "JSESSIONID=";
    public static final String X_LOGIN = "X-login";
    public static final String X_PASSWORD = "X-password";

    // E-VNFM REST
    public static final String CLEAN_UP_RESOURCES = "cleanUpResources";
    public static final String APPLICATION_TIME_OUT = "applicationTimeOut";
    public static final String SKIP_VERIFICATION = "skipVerification";
    public static final String NAMESPACE = "namespace";
    public static final String PERSIST_SCALE_INFO = "persistScaleInfo";


    // E-VNFM UI
    public static final String UI_ROOT_PATH = "/vnfm/";
    public static final String WEBDRIVER_GECKO_DRIVER = "webdriver.gecko.driver";
    public static final String INSTANTIATE = "Instantiate";
    public static final String UPGRADE = "Upgrade";
    public static final String UPGRADE_OPERATION = "Change_vnfpkg";
    public static final String ROLLBACK_OPERATION = "Change_vnfpkg";
    public static final String COMPONENTS = "Components";
    public static final String PACKAGE_NAME_ID = "appCompositeName";
    public static final String SCALE_OPERATION = "Scale";

    //E-VNFM USER DETAILS
    public static final String EVNFM_USERNAME = "evnfmAllAccess";
    public static final String EVNFM_PASSWORD = "bKiA1I0KKv1Ji9JiMIz6!#";
    public static final String EVNFM_SUPER_USER_ROLE = "E-VNFM Super User Role";
    public static final String EVNFM_UI_USER_ROLE = "E-VNFM UI User Role";

    // UI Page Names
    public static final String PACKAGES = "Packages";
    public static final String RESOURCES = "Resources";
    public static final String RESOURCE_DETAILS = "Resource Details";
    public static final String OPERATIONS = "Operations";
    public static final String GENERAL_INFORMATION = "General information";

    // UI Text Field Titles
    public static final String RESOURCE_NAME_FIELD = "Resource instance name";
    public static final String DESCRIPTION_FIELD = "Description";
    public static final String APPLICATION_TIMEOUT_FIELD = "Application timeout";

    // UI Context Menu Items
    public static final String GO_TO_DETAILS_PAGE_MENU_ITEM = "Go to details page";

    // UI Button Items in Dialog Box
    public static final String RESOURCE_LIST_ITEM = "See Resource list";
    public static final String OPERATION_LIST_ITEM = "See Operation list";
    public static final String TERMINATE_MENU_ITEM = "Terminate";
    public static final String ROLLBACK_MENU_ITEM = "Rollback";
    public static final String SCALE_MENU_ITEM = "Scale";

    // UI Wizard Dialog Box Titles
    public static final String INSTANTIATE_STARTED = "Instantiate operation started";
    public static final String UPGRADE_STARTED = "Upgrade operation started";

    // UI Wizard Step Titles
    public static final String PACKAGE_SELECTION = "Package selection";
    public static final String INFRASTRUCTURE = "Infrastructure";
    public static final String GENERAL_ATTRIBUTES = "General attributes";
    public static final String ADDITIONAL_ATTRIBUTES = "Additional attributes";
    public static final String SUMMARY = "Summary";

    // UI Css Selectors
    // Input Types
    public static final String RADIO_BUTTON = "eui-base-v0-radio-button";
    public static final String DROPDOWN = "e-generic-dropdown";
    public static final String TEXTFIELD = "eui-base-v0-text-field";
    public static final String DATE_PICKER = "e-generic-datepicker";
    public static final String CHECKBOX = "eui-base-v0-checkbox";
    public static final String TEXTAREA = "eui-base-v0-textarea";
    public static final String FILE_INPUT = "eui-base-v0-file-input";
    public static final String PASSWORD_FIELD = "eui-base-v0-password-field";
    public static final String CLUSTER_COMBOBOX = "e-generic-combo-box#cluster-name";
    public static final String CLUSTER_COMBOBOX_ID = "cluster-name";

    // Table
    public static final String TABLE_ROW_SELECTOR = "e-generic-table tr:nth-child(";
    public static final String TAB = "eui-layout-v0-tab";

    // Other
    public static final String CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE = "%s can't be null or empty";

    public static String getAppPackageCompositeName(AppPackageResponse appPackageResponse) {
        return String.format("%s.%s.%s.%s",
                             appPackageResponse.getAppProvider(),
                             appPackageResponse.getAppProductName(),
                             appPackageResponse.getAppSoftwareVersion(),
                             appPackageResponse.getDescriptorVersion());
    }

}
