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
package com.ericsson.evnfm.acceptance.utils;

import com.ericsson.amonboardingservice.model.AppPackageResponse;

public class Constants {
    private Constants() {
    }

    // Execution environment details
    public static final String EVNFM_URL_PROPERTY = "evnfmUrl";
    public static final String EVNFM_USERNAME_PROPERTY = "userName";
    public static final String EVNFM_USERPASS_PROPERTY = "userPassword";
    public static final String IDAM_URL_PROPERTY = "idamUrl";
    public static final String IDAM_ADMIN_USER_PROPERTY = "idamAdminUser";
    public static final String IDAM_ADMIN_PASSWORD_PROPERTY = "idamAdminPassword";
    public static final String IDAM_CLIENT_SECRET_PROPERTY = "idamClientSecret";
    public static final String IDAM_CLIENT_ID_PROPERTY = "idamClientId";
    public static final String IDAM_REALM_PROPERTY = "idamRealm";
    public static final String TEST_DATA_FOLDER_PROPERTY = "testDataFolder";
    public static final String CSAR_DOWNLOAD_PATH_PROPERTY = "csarDownloadPath";
    public static final String HELM_REGISTRY_URL = "helmRegistryUrl";

    // Kubectl Commands
    public static final String KUBECTL = "kubectl get ";
    public static final String RESOURCE_NAMESPACE = "%s -n %s ";
    public static final String KUBECONFIG = "--kubeconfig %s ";
    public static final String NAMESPACE_ARG = "--namespace %s ";
    public static final String DEPLOYMENT = "deployment ";
    public static final String STATEFULSET = "statefulSet ";
    public static final String REPLICASET = "replicaset ";
    public static final String DEFAULT = "default";
    public static final String DELETE_NAMESPACE = "kubectl delete namespace %s " + KUBECONFIG;
    public static final String SCALE_COMMAND = "kubectl scale --replicas=%s deployment/%s " + KUBECONFIG + NAMESPACE_ARG;

    // Test tags
    public static final String PERFORMANCE = "performance";
    public static final String REGRESSION = "regression";
    public static final String REST = "rest";
    public static final String UI = "ui";
    public static final String CLUSTER = "cluster";
    public static final String ONBOARDING = "onboarding";
    public static final String DELETE_PACKAGES = "delete-packages";
    public static final String BUR = "bur";

    // Argument options
    public static final String TYPE_FLAG = "-t";
    public static final String PHASE_FLAG = "-p";
    public static final String FILE_FLAG = "-f";

    // Pod status
    public static final String RUNNING = "Running";
    public static final String TERMINATING = "Terminating";
    public static final String POD_LABEL_NAME = "app.kubernetes.io/name";

    // Query\Path Variables
    public static final String VNF_INSTANCE_ID = "{vnfInstanceId}";
    public static final String VNF_INSTANCE_NAME = "<vnfInstanceName>";
    public static final String SLASH_STRING_PATH_PARAMETER = "/%s";

    // Authorization
    public static final String TOKEN_URI_RESOURCE = "/auth/v1";

    // E-VNFM Onboarding URIs
    public static final String ONBOARDING_HEALTHCHECK_URI = "/vnfm/onboarding/actuator/health";
    public static final String ETSI_CREATE_PACKAGE_URL = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages";
    public static final String ETSI_GET_PACKAGE_BY_ID_URI = ETSI_CREATE_PACKAGE_URL + SLASH_STRING_PATH_PARAMETER;
    public static final String ETSI_PACKAGE_URL_QUERY_VNF_IDENTIFIER = ETSI_CREATE_PACKAGE_URL + "?filter=(eq,vnfdId,%s)";
    public static final String ETSI_ONBOARDING_PACKAGES_URI = ETSI_GET_PACKAGE_BY_ID_URI + "/package_content";
    public static final String LIST_PACKAGES_URI = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages";
    public static final String DELETE_PACKAGE_URI = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages";
    public static final String ALL_HELM_CHARTS_URI = "/vnfm/onboarding/api/v1/charts";
    public static final String SPECIFIC_HELM_CHART_URI = ALL_HELM_CHARTS_URI + SLASH_STRING_PATH_PARAMETER;
    public static final String ALL_DOCKER_IMAGES_URI = "/vnfm/onboarding/api/v1/images";
    public static final String SPECIFIC_DOCKER_IMAGE_URI = ALL_DOCKER_IMAGES_URI + SLASH_STRING_PATH_PARAMETER + SLASH_STRING_PATH_PARAMETER;
    public static final String RETRIEVE_VNFD_URI = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/%s/vnfd";
    public static final String ONBOARDING_V1_API_URI = "/vnfm/onboarding/api/v1";
    public static final String ADD_NODE_TO_ENM_URI = "/vnflcm/v1/vnf_instances/%s/addNode";
    public static final String DELETE_NODE_FROM_ENM_URI = "/vnflcm/v1/vnf_instances/%s/deleteNode";
    public static final String GET_RESOURCE_BY_ID_URI = "/vnflcm/v1/resources/%s";
    public static final String GET_RESOURCES_URI = "/vnflcm/v1/resources";

    public static final String ONBOARDING_ARTIFACT_BY_PATH_URL = "/vnfm/onboarding/api/vnfpkgm/v1/vnf_packages/%s/artifacts/%s";

    // Orchestrator URIs
    public static final String COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS = "/vnflcm/v1/vnf_instances";
    public static final String ETSI_VNF_INSTANCE_URL_QUERY_RELEASE = COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS + "?filter=(eq,vnfInstanceName,%s)";
    public static final String ETSI_VNF_INSTANCE_URL_QUERY_PACKAGE_ID = COMMON_URI_FOR_VNF_INSTANCE_OPERATIONS + "?filter=(eq,vnfPkgId,%s)";

    public static final String UPGRADE_CNF_CHANGE_VNFGPKG = "/%s/change_vnfpkg";

    public static final String CLUSTER_CONFIGS_URI = "/vnflcm/v1/clusterconfigs";

    public static final String ETSI_LIFECYCLE_OCC_URI = "/vnflcm/v1/vnf_lcm_op_occs";
    public static final String QUERY_VNF_LCM_OP_OCC_BY_VNF_INSTANCE_ID = ETSI_LIFECYCLE_OCC_URI + "?filter=(eq,vnfInstanceId,%s)";
    public static final String GET_VNF_LCM_OP_OCC_BY_VNF_INSTANCE_ID = ETSI_LIFECYCLE_OCC_URI + SLASH_STRING_PATH_PARAMETER;
    public static final String HEAL_VNF_LCM_OPP_URI_SUFFIX = "/heal";
    public static final String ROLLBACK_VNF_LCM_OPP_URI_SUFFIX = "/rollback";
    public static final String ROLLBACK_FROM_FAILURE_URI = GET_VNF_LCM_OP_OCC_BY_VNF_INSTANCE_ID + ROLLBACK_VNF_LCM_OPP_URI_SUFFIX;
    public static final String QUERY_VNFC_SCALE_INFO_URL = "/vnflcm/v1/resources/%s/vnfcScaleInfo%s";
    public static final String BACKUP_URL = "/vnflcm/v1/vnf_instances/%s/backups";
    public static final String GET_BACKUP_SCOPES_URL = "/vnflcm/v1/vnf_instances/%s/backup/scopes";
    public static final String CLEANUP_URL = "/cleanup";

    public static final String TRUSTED_CERTIFICATES_ENDPOINT = "/certm/nbi/v2/trusted-certificates/%s";
    public static final String BRO_STATUS_ENDPOINT = "/backup-restore/v3/health";

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
    public static final String TEST_NAMESPACE = "testing-namespace-only-for-test-connectivity-test";
    public static final String CLUSTER_NAME = "clusterName";
    public static final String DRAC_ENABLED = "dracEnabled";

    //E-VNFM USER DETAILS
    public static final String EVNFM_SUPER_USER_ROLE = "E-VNFM Super User Role";
    public static final String EVNFM_UI_USER_ROLE = "E-VNFM UI User Role";
    public static final String EVNFM_SPIDER_A_DOMAIN_ROLE = "E-VNFM_Spider_A Domain Role";
    public static final String EVNFM_SPIDER_B_DOMAIN_ROLE = "E-VNFM_Spider_B Domain Role";
    public static final String EVNFM_SPIDER_DM_DOMAIN_ROLE = "E-VNFM_Spider_DM Domain Role";
    public static final String EVNFM_SPIDER_REL4_DOMAIN_ROLE = "E-VNFM_Spider_Rel4 Domain Role";
    public static final String EVNFM_BASIC_DOMAIN_ROLE = "E-VNFM_Basic Domain Role";
    public static final String MULTI_A_DOMAIN_ROLE = "Multi A Domain Role";
    public static final String MULTI_B_DOMAIN_ROLE= "Multi B Domain Role";

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
    public static final String SYNC_OPERATION = "Sync";
    public static final String VNF_CONTROLLED_SCALING = "vnfControlledScaling";

    // UI Page Names
    public static final String PACKAGES = "Packages";
    public static final String PACKAGE_DETAILS = "Package Details";
    public static final String RESOURCES = "Resources";
    public static final String RESOURCE_DETAILS = "Resource Details";
    public static final String OPERATIONS = "Operations";
    public static final String CISM_CLUSTERS = "CISM Clusters";

    // UI Text Field Titles
    public static final String RESOURCE_NAME_FIELD = "Resource instance name";
    public static final String DESCRIPTION_FIELD = "Description";
    public static final String APPLICATION_TIMEOUT_FIELD = "Application timeout";

    // UI Context Menu Items
    public static final String GO_TO_DETAILS_PAGE_MENU_ITEM = "Go-to-details-page";
    public static final String TERMINATE_MENU_ITEM = "Terminate";
    public static final String BACKUP_MENU_ITEM = "Backup";
    public static final String ROLLBACK_MENU_ITEM = "Rollback";
    public static final String SCALE_MENU_ITEM = "Scale";
    public static final String SYNC_MENU_ITEM = "Sync";
    public static final String UPGRADE_MENU_ITEM = "Upgrade";
    public static final String DELETE_MENU_ITEM = "Delete";
    public static final String MODIFY_VNF_INFO_MENU_ITEM = "Modify-VNF-Information";
    public static final String DEREGISTER_CLUSTER_MENU_ITEM = "Deregister-cluster";
    public static final String DELETE_PACKAGE_MENU_ITEM = "Delete-package";
    public static final String PACKAGE_DETAILS_MENU_ITEM = "Go-to-details-page";

    // UI Button Items in Dialog Box
    public static final String RESOURCE_LIST_ITEM = "See Resource list";
    public static final String OPERATION_LIST_ITEM = "See Operation list";

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
    public static final String KEY_MAP_CARD_GROUP = "e-generic-key-map-card-group";
    // Table
    public static final String TABLE_ROW_SELECTOR = "e-generic-table tr:nth-child(";
    public static final String TAB = "eui-layout-v0-tab";

    // Other
    public static final String CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE = "%s can't be null or empty";
    public static final String RESPONSE_INFO_MESSAGE = "Response: {}";
    public static final String VNFD_ZIP_RESPONSE_LENGTH = "Received vnfd byte array of length {}";
    public static final String JSON_PROCESSION_EXCEPTION_MESSAGE = "An error occurred while parsing json: %s";
    public static final String HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE = "Header 'Location' should not be null or empty";
    public static final String INVALID_SCALE_INFO_EXTENSIONS_ERROR_MESSAGE = "%s in actual response is different from expected : "
            + "\nACTUAL : %s\n , \nEXPECTED : %s\n";
    public static final String TERMINATE_REQUEST_WAS_NOT_ACCEPTED = "Terminate request was not accepted: %s";
    public static final String FAILED_TO_DELETE_IDENTIFIER = "Failed to delete identifier: %s";
    public static final String FAILED_TO_FIND_LCM_OP_FAILED_TEMP = "Could not find operation in FAILED_TEMP state to rollback for instance:: %s";
    public static final String PACKAGE_VNFD_ID_NOT_FOUND = "Package with vnfd id %s not found";
    public static final String GET_VNFC_SCALE_INFO_PARAMS = "?numberOfSteps=%s&aspectId=%s&type=%s";
    public static final String CLEANUP = "CLEANUP";
    public static final String CLEAN_UP_CNF_ERROR = "Resources will not be cleaned up; last operation on instance was not a failed INSTANTIATE or "
            + "TERMINATE";
    public static final String ARTIFACT_RESPONSE_LENGTH = "Received artifact byte array of length {}";

    // Logging common messages
    public static final String STARTS_PERFORMING_OPERATION_LOG = "Starts performing operation {} for CNF with release name {}";
    public static final String REQUEST_BODY_LOG = "For operation {} request body is: {}";
    public static final String OPERATION_RESPONSE_STATUS_LOG = "Operation {} performed with status {}";
    public static final String STARTS_PERFORMING_LCM_OPERATION_LOG = "Starts performing operation {} for Operation Occurrence Id {}";
    public static final String PERFORMING_GET_INSTANCE_REQUEST_LOG = "Starts performing get request for instance link {}";
    public static final String VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG = "Received next URL from response for query VNF LCM Operation Occurrence "
            + "operation : {}";
    public static final String STARTS_VERIFICATION_LOG = "Starts verification operation {} with expected status : {}";
    public static final String OPERATION_RESPONSE_VERIFICATION_LOG = "Response of operation {} verified successfully";

    public static String getAppPackageCompositeName(AppPackageResponse appPackageResponse) {
        return String.format("%s.%s.%s.%s",
                             appPackageResponse.getAppProvider(),
                             appPackageResponse.getAppProductName(),
                             appPackageResponse.getAppSoftwareVersion(),
                             appPackageResponse.getDescriptorVersion());
    }

    // keycloak login
    public static final String KEYCLOAK_USERNAME_ID = "username";
    public static final String KEYCLOAK_PASSWORD_ID = "password";
    public static final String KEYCLOAK_LOGIN_BUTTON = "kc-login-input";

    // UI logout
    public static final String LOGOUT_USER_PANEL = "div[data-payload*=user-logout-panel]";
    public static final String LOGOUT_USER_BUTTON = "e-user-logout-panel eui-base-v0-button";

    // Lifecycle Operation States
    public static final String FAILED = "FAILED";
    public static final String FAILED_TEMP = "FAILED_TEMP";
    public static final String ROLLED_BACK = "ROLLED_BACK";
    public static final String CREATE_IDENTIFIER_LIFECYCLE_OPERATION = "CREATE_IDENTIFIER";
    public static final String DELETE_IDENTIFIER_LIFECYCLE_OPERATION = "DELETE_IDENTIFIER";
    public static final String ROLLBACK_LIFECYCLE_OPERATION = "ROLLBACK";
    public static final String GET_INSTANCE_OPERATION = "GET_INSTANCE";
    public static final String QUERY_INSTANCES_OPERATION = "QUERY_INSTANCES";
    public static final String[] INVALID_OPERATION_STATES_INSTANTIATE = new String[] { FAILED, FAILED_TEMP, ROLLED_BACK };
    public static final String[] INVALID_OPERATION_STATES_ROLLBACK = new String[] { FAILED, FAILED_TEMP };
    public static final String[] INVALID_OPERATION_STATES_SCALE = new String[] { FAILED, ROLLED_BACK };
    public static final String[] INVALID_OPERATION_STATES_MODIFY_VNF_INFO = INVALID_OPERATION_STATES_INSTANTIATE;

    public static final String INTERMEDIATE_CA_CERTIFICATE = "intermediate-ca.crt";
    public static final String CA_SECRET_NAME = "iam-cacert-secret";
    public static final String CA_CERTIFICATE_SECRET_PROPERTY = "tls.crt";

    public static final String CISM_CLUSTER_PREFIX = "CISM_cluster:";
}
