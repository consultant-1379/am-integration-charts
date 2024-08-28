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
package com.ericsson.evnfm.acceptance.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CREATE_IDENTIFER_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.UPGRADE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.getAppPackageCompositeName;
import static com.ericsson.evnfm.acceptance.steps.CleanUpSteps.cleanUpResources;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.checkIfScaleCanBePerformed;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.getCurrentValues;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScale;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScaleReset;
import static com.ericsson.evnfm.acceptance.steps.rest.ClusterConfigSteps.uploadClusterConfig;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.uniquelyIdentifyReleaseAndNamespace;
import static com.ericsson.evnfm.acceptance.steps.rest.InstantiateSteps.performInstantiate;
import static com.ericsson.evnfm.acceptance.steps.rest.InstantiateSteps.performNegativeInstantiate;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.deleteAndVerifyPackageById;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getGatewayUrl;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getRestInstantiatePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getRestUpgradePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getUiInstantiatePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getUiUpgradePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getVnfdPackagesInfoWithEtsiApi;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.initializeOnboardedPackages;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.onboardCsars;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.updateOnboardedPackagesInfo;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.verifyAllTestPackagesRemoved;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.verifyIfPackageOnboarded;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.verifyPackageUsageState;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.verifyVNFDInZipFile;
import static com.ericsson.evnfm.acceptance.steps.rest.ScaleSteps.performScaleRest;
import static com.ericsson.evnfm.acceptance.steps.rest.ScaleSteps.scaleVnfTest;
import static com.ericsson.evnfm.acceptance.steps.rest.TerminateSteps.performTerminateAndDeleteIdentifier;
import static com.ericsson.evnfm.acceptance.steps.rest.UpgradeSteps.performUpgrade;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.createIdentifier;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.deleteVnfIdentifier;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.getResourceScaleInfo;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.instantiate;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfInstance;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfModel;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfOperation;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfState;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.upgradeVnf;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.verifyInstanceResponseMatchesSystem;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.verifyScaleDataPersistence;
import static com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPISteps.createCustomResource;
import static com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPISteps.createOrReplaceCustomResourceDefinition;
import static com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPISteps.deleteCustomResource;
import static com.ericsson.evnfm.acceptance.steps.ui.resources.ScaleResourceSteps.performScaleUI;
import static com.ericsson.evnfm.acceptance.steps.ui.resources.ScaleResourceSteps.scaleResource;
import static com.ericsson.evnfm.acceptance.utils.Constants.BUR;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLUSTER;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.Constants.ONBOARDING;
import static com.ericsson.evnfm.acceptance.utils.Constants.REGRESSION;
import static com.ericsson.evnfm.acceptance.utils.Constants.REST;
import static com.ericsson.evnfm.acceptance.utils.Constants.UI;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.CHANGE_VNFPKG;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.GenericKubernetesResource;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.TestAdapter;
import com.ericsson.evnfm.acceptance.api.ui.model.ResourceInfo;
import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.extensions.SkipOnFailuresInEnclosingClass;
import com.ericsson.evnfm.acceptance.extensions.SkipOnFailuresInEnclosingClassExtension;
import com.ericsson.evnfm.acceptance.models.CleanUp;
import com.ericsson.evnfm.acceptance.models.EvnfmPackage;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCustomResource;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigMapVerify;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigOnboarding;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigRollback;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigTerminate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigUpgrade;
import com.ericsson.evnfm.acceptance.models.configuration.CustomResourceInfo;
import com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPISteps;
import com.ericsson.evnfm.acceptance.steps.ui.resources.InstantiateResourceSteps;
import com.ericsson.evnfm.acceptance.steps.ui.resources.OnboardedPackagesStep;
import com.ericsson.evnfm.acceptance.steps.ui.resources.TerminateResourceStep;
import com.ericsson.evnfm.acceptance.steps.ui.resources.UpgradeResourceSteps;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;

@Tag(REGRESSION)
@ExtendWith(SkipOnFailuresInEnclosingClassExtension.class)
class EndToEndAcceptanceTest extends TestAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndToEndAcceptanceTest.class);
    private static final List<CleanUp> releasesToCleanUp = new ArrayList<>();
    private static final String REST_TEST = "rest-test";
    private static final String UI_TEST = "ui-test";
    private static final String BUR_TEST = "bur-test";
    private static final String RELEASE_NAME_MESSAGE = "Release name will be: {}";

    @AfterAll
    static void verifyAllTestPackagesDeleted() {
        if (ConfigurationProvider.getTestInfo().getPhase() != 1) {
            verifyAllTestPackagesRemoved();
        }
    }

    @Tag(CLUSTER)
    @ParameterizedTest
    @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#clusterConfigurationData")
    void uploadClusterConfigFileTest(ConfigCluster configCluster) {
        if (StringUtils.isEmpty(configCluster.getExternalConfigFile())) {
            LOGGER.info("No config file specified skipping upload\n");
        } else {
            String configFile = configCluster.getExternalConfigFile();
            LOGGER.info("uploading config file {}\n", configFile);
            uploadClusterConfig(configFile);
        }
    }

    @Tag(ONBOARDING)
    @DisplayName("EVNFM_OB_01 : It shall be possible to onboard a CNF CSAR package")
    @MethodSource("com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps#csarPackagesConfig")
    void onboardingAcceptanceTest(ConfigOnboarding configOnboarding) {
        onboardCsars(configOnboarding);
    }

    @Disabled("This test has been moved to BasicOnboardingTest#testVnfdZipOnboardedCorrectly() " +
            "and is executed as part of TestNG release.xml test suit")
    @Tag(ONBOARDING)
    @MethodSource("com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps#getFirstPackageId")
    void getVnfdByZipSingleYaml(String packageId) throws IOException, InterruptedException {
        byte[] responseBody = getVnfdPackagesInfoWithEtsiApi(packageId, "application/zip");
        verifyVNFDInZipFile(responseBody, packageId, false);
    }

    @Disabled("This test has been moved to BasicOnboardingTest#testVnfdZipOnboardedCorrectly() " +
            "and is executed as part of TestNG release.xml test suit")
    @Tag(ONBOARDING)
    @MethodSource("com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps#getPackageWithImportsInVnfd")
    void getVnfdByZipMultiYaml(String packageId) throws IOException, InterruptedException {
        byte[] responseBody = getVnfdPackagesInfoWithEtsiApi(packageId, "application/zip");
        verifyVNFDInZipFile(responseBody, packageId, true);
    }

    @Tag(DELETE_PACKAGES)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class DeleteAllTestPackagesTest {

        @BeforeAll
        void updateOnboardedPackagesForCleanUp() {
            updateOnboardedPackagesInfo(ConfigurationProvider.getOnboardingConfig());
        }

        @ParameterizedTest
        @Execution(ExecutionMode.SAME_THREAD)
        @MethodSource("com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps#csarPackageIDsFromTestList")
        void deleteAndVerifyAllTestPackagesTest(String packageID) throws JsonProcessingException {
            deleteAndVerifyPackageById(packageID);
        }
    }

    @SkipOnFailuresInEnclosingClass
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class EndToEndVnfLifeCycleAcceptanceTest {

        @BeforeAll
        void initPackages() {
            LOGGER.info("initialising packages \n");
            initializeOnboardedPackages();
        }

        @AfterAll
        void cleanUp() throws IOException, InterruptedException {
            if (ConfigurationProvider.getTestInfo().getPhase() != 1) {
                cleanUpResources(releasesToCleanUp);
            }
        }

        @Disabled("This test has been moved to ScaleSuccessTest.java and is executed as part of TestNG release.xml test suit")
        @Tag(REST)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#restTestConfigurationData")
        void endToEndRestTestAllOpsSameClusterNormalVerification(ConfigInstantiate configInstantiate,
                                                                 ConfigUpgrade configUpgrade,
                                                                 ConfigTerminate configTerminate,
                                                                 ConfigCluster configCluster,
                                                                 ConfigScale configScale) throws IOException, InterruptedException {

            ConfigInstantiate configInstantiateWithoutCluster = configInstantiate.clone();
            boolean persistScaleInfo = false;
            LOGGER.info(RELEASE_NAME_MESSAGE, configInstantiateWithoutCluster.getReleaseName());
            Optional<AppPackageResponse> instantiatePackage = getRestInstantiatePackage();
            if (instantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, configInstantiateWithoutCluster, releasesToCleanUp, "-rest-default",
                                                    REST_TEST, instantiatePackage.get());
                //creating id
                VnfInstanceResponseLinks vnfInstanceResponseLinks = createIdentifier(getGatewayUrl() + CREATE_IDENTIFER_URI,
                                                                                     instantiatePackage.get().getAppDescriptorId(),
                                                                                     configInstantiateWithoutCluster);
                //sending instantiate request
                String instantiateHeader = instantiate(vnfInstanceResponseLinks, configInstantiateWithoutCluster,
                                                       false, configInstantiateWithoutCluster.getAdditionalAttributesFile());

                //waiting for instantiation to complete
                queryVnfOperation(instantiateHeader, configInstantiateWithoutCluster.getExpectedOperationState(),
                                  configInstantiateWithoutCluster.getApplicationTimeOut(), INSTANTIATE);

                String vnfInstanceUrl = vnfInstanceResponseLinks.getSelf().getHref();
                VnfInstanceResponse instantiatedPackage = queryVnfInstance(vnfInstanceUrl);
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.IN_USE);
                KubernetesAPISteps.verifyAnnotations(instantiatedPackage,
                                                     configInstantiateWithoutCluster.getNamespace(),
                                                     configCluster.getExternalConfigFile());

                List<ConfigMapVerify> configMapVerifies = KubernetesAPISteps.verifyConfigMaps(
                        configInstantiateWithoutCluster.getConfigurations(),
                        configInstantiateWithoutCluster.getAdditionalAttributes(),
                        configInstantiateWithoutCluster.getNamespace(),
                        configCluster.getExternalConfigFile());
                configMapVerifies.forEach(configMapVerify -> assertThat(configMapVerify.getAttributeData()).isEqualTo(configMapVerify
                                                                                                                              .getConfigmapData()));

                performScaleRest(configScale, vnfInstanceResponseLinks, configInstantiateWithoutCluster, configCluster, REST, "Aspect1");
                scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_OUT", REST, "Aspect1");
                List<ScaleInfo> scaleInfoBeforeUpgrade = getResourceScaleInfo(vnfInstanceUrl);
                Optional<AppPackageResponse> upgradePackage = getRestUpgradePackage();
                if (upgradePackage.isPresent()) {
                    performUpgrade(configUpgrade,
                                   vnfInstanceResponseLinks,
                                   upgradePackage,
                                   configUpgrade.getAdditionalAttributesFile(),
                                   REST,
                                   persistScaleInfo);
                    VnfInstanceResponse upgradedPackage = queryVnfInstance(vnfInstanceUrl);
                    List<ScaleInfo> scaleInfoAfterUpgrade = getResourceScaleInfo(vnfInstanceUrl);
                    verifyScaleDataPersistence(persistScaleInfo, scaleInfoBeforeUpgrade, scaleInfoAfterUpgrade);
                    verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
                    verifyPackageUsageState(upgradePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.IN_USE);
                    KubernetesAPISteps.verifyAnnotations(upgradedPackage,
                                                         configInstantiateWithoutCluster.getNamespace(),
                                                         configCluster.getExternalConfigFile());

                    List<ConfigMapVerify> upgradeConfigMapVerifies = KubernetesAPISteps.verifyConfigMaps(
                            configUpgrade.getConfigurations(),
                            configUpgrade.getAdditionalAttributes(),
                            configInstantiateWithoutCluster.getNamespace(),
                            configCluster.getExternalConfigFile());
                    upgradeConfigMapVerifies.forEach(configMapVerify -> assertThat(configMapVerify.getAttributeData()).isEqualTo(configMapVerify.getConfigmapData()));
                } else {
                    LOGGER.info("Upgrade package not uploaded, skipping upgrade");
                }
                performTerminateAndDeleteIdentifier(configTerminate, vnfInstanceResponseLinks);
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
                verifyPackageUsageState(upgradePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
            } else {
                LOGGER.info("Instantiate package not uploaded, skipping instantiate, scale, upgrade and terminate");
                fail("Instantiate package not uploaded, skipping test");
            }
        }

        @Disabled("This test has been moved to PersistenceOfScalingDataTest#endToEndRestTestPersistenceOfScalingData() " +
                "and is executed as part of TestNG release.xml test suit")
        @Tag(REST)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#restTestConfigurationData")
        void endToEndRestTestPersistenceOfScalingData(ConfigInstantiate configInstantiate,
                                                      ConfigUpgrade configUpgrade,
                                                      ConfigTerminate configTerminate,
                                                      ConfigCluster configCluster,
                                                      ConfigScale configScale) throws IOException {

            ConfigInstantiate configInstantiateWithoutCluster = configInstantiate.clone();
            LOGGER.info(RELEASE_NAME_MESSAGE, configInstantiateWithoutCluster.getReleaseName());
            Optional<AppPackageResponse> instantiatePackage = getRestInstantiatePackage();
            boolean persistScaleInfo = true;
            if (instantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, configInstantiateWithoutCluster, releasesToCleanUp, "-rest-persist",
                                                    REST_TEST, instantiatePackage.get());
                //creating id
                VnfInstanceResponseLinks vnfInstanceResponseLinks = createIdentifier(getGatewayUrl() + CREATE_IDENTIFER_URI,
                                                                                     instantiatePackage.get().getAppDescriptorId(),
                                                                                     configInstantiateWithoutCluster);
                //sending instantiate request
                String instantiateHeader = instantiate(vnfInstanceResponseLinks, configInstantiateWithoutCluster,
                                                       false, configInstantiateWithoutCluster.getAdditionalAttributesFile());
                String vnfInstanceUrl = vnfInstanceResponseLinks.getSelf().getHref();
                queryVnfOperation(instantiateHeader, configInstantiateWithoutCluster.getExpectedOperationState(),
                                  configInstantiateWithoutCluster.getApplicationTimeOut(), INSTANTIATE);
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.IN_USE);
                // performing scale for different aspects to check scale info persistence after upgrade
                scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_OUT", REST, "Aspect1");
                scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_OUT", REST, "Aspect2");
                scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_OUT", REST, "Aspect3");
                List<ScaleInfo> scaleInfoBeforeUpgrade = getResourceScaleInfo(vnfInstanceUrl);
                Optional<AppPackageResponse> upgradePackage = getRestUpgradePackage();
                if (upgradePackage.isPresent()) {
                    performUpgrade(configUpgrade,
                                   vnfInstanceResponseLinks,
                                   upgradePackage,
                                   configUpgrade.getAdditionalAttributesFile(),
                                   REST,
                                   persistScaleInfo);
                    verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
                    verifyPackageUsageState(upgradePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.IN_USE);
                    List<ScaleInfo> scaleInfoAfterUpgrade = getResourceScaleInfo(vnfInstanceUrl);
                    verifyScaleDataPersistence(persistScaleInfo, scaleInfoBeforeUpgrade, scaleInfoAfterUpgrade);
                } else {
                    LOGGER.info("Upgrade package not uploaded, skipping upgrade");
                }

                performTerminateAndDeleteIdentifier(configTerminate, vnfInstanceResponseLinks);
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
                verifyPackageUsageState(upgradePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
            } else {
                LOGGER.info("Instantiate package not uploaded, skipping instantiate, scale, upgrade and terminate");
                fail("Instantiate package not uploaded, skipping test");
            }
        }

        @Disabled("This test has been moved to CISMClusterTest#clusterConfigLifecycleNegativeTest() " +
                "and is executed as part of TestNG release.xml test suit")
        @Tag(REST)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#restTestConfigurationData")
        void endToEndRestTestNegativeInvalidCluster(ConfigInstantiate configInstantiate,
                                                    ConfigUpgrade configUpgrade,
                                                    ConfigTerminate configTerminate,
                                                    ConfigCluster configCluster,
                                                    ConfigScale configScale) throws JsonProcessingException {
            ConfigInstantiate configInstantiateWithNonExistentCluster = configInstantiate.clone();
            configInstantiateWithNonExistentCluster.setCluster("bad_cluster");
            LOGGER.info(RELEASE_NAME_MESSAGE, configInstantiateWithNonExistentCluster.getReleaseName());
            Optional<AppPackageResponse> instantiatePackage = getRestInstantiatePackage();
            if (instantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, configInstantiateWithNonExistentCluster, releasesToCleanUp, "-rest-neg",
                                                    REST_TEST, instantiatePackage.get());
                VnfInstanceResponseLinks vnfInstanceResponseLinks = performNegativeInstantiate(
                        configInstantiateWithNonExistentCluster, instantiatePackage, false, configInstantiate.getAdditionalAttributesFile());
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.IN_USE);
                deleteVnfIdentifier(vnfInstanceResponseLinks);
                verifyPackageUsageState(instantiatePackage.get().getAppPkgId(), VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
            } else {
                LOGGER.info("Instantiate package not uploaded, skipping instantiate, scale, upgrade and terminate");
                fail("Instantiate package not uploaded, skipping test");
            }
        }

        @Disabled("This test has been moved to MultipleScaleOperationTest#testMultipleScaleOperationWithAnnotationVerification() " +
                "and is executed as part of TestNG release.xml test suit")
        @Tag(UI)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#uiTestConfigurationData")
        void endToEndUITestAllOpsSeparateClusterAnnotationVerification(ConfigGeneral configGeneral,
                                                                       ConfigInstantiate configInstantiate,
                                                                       ConfigUpgrade configUpgrade,
                                                                       ConfigRollback configRollback,
                                                                       ConfigScale configScale,
                                                                       ConfigTerminate configTerminate,
                                                                       final ConfigCluster configCluster)
        throws IOException, InterruptedException {

            ConfigInstantiate otherCluster = configInstantiate.clone();
            LOGGER.info(RELEASE_NAME_MESSAGE, otherCluster.getReleaseName());
            Optional<AppPackageResponse> instantiatePackage = getUiInstantiatePackage();
            Optional<AppPackageResponse> upgradePackage = getUiUpgradePackage();
            if (instantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, otherCluster, releasesToCleanUp, "-ui-external", UI_TEST,
                                                    instantiatePackage.get());
                LOGGER.info("UI Package to instantiate: {}", instantiatePackage);
                List<String> expectedPackageNames = Collections
                        .singletonList(getAppPackageCompositeName(instantiatePackage.get()));
                OnboardedPackagesStep.checkPackageNames(driver, configGeneral, expectedPackageNames);
                LOGGER.info("Starting instantiation of package");
                ResourceInfo instantiateInfo = InstantiateResourceSteps
                        .instantiatePackage(driver, configGeneral, otherCluster, instantiatePackage.get());
                //TODO SM-56781 and SM-56782 are needed in order to implement testing of files in configmap data
                if (configInstantiate.getConfigurations() != null) {
                    LOGGER.warn("Verifying configmaps is not supported for UI tests currently");
                }
                Map<String, Map<String, String>> beforeScaleValues = getCurrentValues(configScale, otherCluster,
                                                                                      configCluster);
                LOGGER.info("Finished instantiation, performing scale...");
                performScaleUI(driver, configGeneral, configScale, otherCluster, configCluster);
                LOGGER.info("Performing scale-out only to check scaling data is not persisted after Upgrade...");
                scaleResource(driver, configGeneral, otherCluster, configScale, "scaleOut");
                if (upgradePackage.isPresent()) {
                    LOGGER.info("Starting upgrade...");
                    ResourceInfo upgradeResourceInfo = UpgradeResourceSteps
                            .upgradeResource(driver, configGeneral, otherCluster, configUpgrade, upgradePackage.get(), false);
                    assertThat(upgradeResourceInfo.getOperationState())
                            .isEqualToIgnoringCase(configUpgrade.getExpectedOperationState());
                    LOGGER.info("Finished upgrade...");
                    LOGGER.info("Verifying that scaling info was not persisted");
                    Map<String, Map<String, String>> afterUpgradeValues = getCurrentValues(configScale, otherCluster,
                                                                                           configCluster);
                    verifyScaleReset(beforeScaleValues, afterUpgradeValues);
                    LOGGER.info("Skipping rollback steps...");
                    // Disabling below until new rollback functionality with patterns is complete.
                    // Needs to be enabled and verified as part of SM-98909
                    //                    LOGGER.info("Starting rollback...");
                    //                    ResourceInfo rollbackResourceInfo = RollbackResourceSteps.rollbackResource(driver, configGeneral,
                    //                    otherCluster, configUpgrade, configRollback);
                    //                    assertThat(rollbackResourceInfo.getPackageVersion().equals(instantiateInfo.getPackageVersion()));
                    //                    assertThat(rollbackResourceInfo.getSoftwareVersion().equals(instantiateInfo.getSoftwareVersion()));
                    //                    LOGGER.info("Finished rollback...");
                } else {
                    LOGGER.info("Upgrade package not uploaded, skipping upgrade and rollback");
                }
                LOGGER.info("Terminating resource...");
                TerminateResourceStep.terminateResource(driver, configGeneral, otherCluster, configTerminate);
                LOGGER.info("Finished terminating...end of test");
            } else {
                LOGGER.info("Instantiate package not uploaded, skipping tests");
                fail("Instantiate package not uploaded, skipping tests");
            }
        }

        @Disabled("This test has been moved to PersistenceOfScalingDataUITest#endToEndUITestPersistenceOfScalingData() " +
                "and is executed as part of TestNG release.xml test suit")
        @Tag(UI)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#uiTestConfigurationData")
        void endToEndUITestTestPersistenceOfScalingData(ConfigGeneral configGeneral,
                                                        ConfigInstantiate configInstantiate,
                                                        ConfigUpgrade configUpgrade,
                                                        ConfigRollback configRollback,
                                                        ConfigScale configScale,
                                                        ConfigTerminate configTerminate,
                                                        final ConfigCluster configCluster)
        throws IOException, InterruptedException {

            ConfigInstantiate otherCluster = configInstantiate.clone();
            LOGGER.info(RELEASE_NAME_MESSAGE, otherCluster.getReleaseName());
            Optional<AppPackageResponse> instantiatePackage = getUiInstantiatePackage();
            Optional<AppPackageResponse> upgradePackage = getUiUpgradePackage();
            if (instantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, otherCluster, releasesToCleanUp, "-ui-persist", UI_TEST,
                                                    instantiatePackage.get());
                LOGGER.info("UI Package to instantiate: {}", instantiatePackage);
                List<String> expectedPackageNames = Collections
                        .singletonList(getAppPackageCompositeName(instantiatePackage.get()));
                OnboardedPackagesStep.checkPackageNames(driver, configGeneral, expectedPackageNames);
                LOGGER.info("Starting instantiation of package");
                InstantiateResourceSteps.instantiatePackageWithoutAdditionalParams(driver, configGeneral, otherCluster, instantiatePackage.get());
                LOGGER.info("Finished instantiation, performing scale...");
                if (checkIfScaleCanBePerformed(configScale)) {
                    scaleResource(driver, configGeneral, otherCluster, configScale, "scaleOut");
                } else {
                    LOGGER.info("Scale config missing mandatory params skipping scale\n");
                }
                if (upgradePackage.isPresent()) {
                    LOGGER.info("Starting upgrade...");
                    ResourceInfo upgradeResourceInfo = UpgradeResourceSteps
                            .upgradePackageWithoutAdditionalParams(driver, configGeneral, otherCluster, configUpgrade, upgradePackage.get(), true);
                    assertThat(upgradeResourceInfo.getOperationState())
                            .isEqualToIgnoringCase(configUpgrade.getExpectedOperationState());
                    LOGGER.info("Finished upgrade...");
                } else {
                    LOGGER.info("Upgrade package not uploaded, skipping upgrade");
                }
                LOGGER.info("Verifying if scaling info was persisted");
                Map<String, Map<String, String>> scaledValues = getCurrentValues(configScale, otherCluster,
                                                                                 configCluster);
                verifyScale(configScale, scaledValues);
                LOGGER.info("Terminating resource...");
                TerminateResourceStep.terminateResource(driver, configGeneral, otherCluster, configTerminate);
                LOGGER.info("Finished terminating...end of test");
            } else {
                LOGGER.info("Instantiate package not uploaded, skipping tests");
                fail("Instantiate package not uploaded, skipping tests");
            }
        }

        @Disabled("This test has been moved to resources/suites/phase1.xml and resources/suites/phase2.xml TestNG suites")
        @Tag(BUR)
        @Execution(ExecutionMode.SAME_THREAD)
        @ParameterizedTest
        @MethodSource("com.ericsson.evnfm.acceptance.common.TestDataProvider#burConfigurationData")
        void endToEndBURTest(ConfigInstantiate configInstantiate, ConfigUpgrade configUpgrade, ConfigScale configScale,
                             ConfigTerminate configTerminate, final ConfigCluster configCluster, ConfigOnboarding configOnboarding,
                             ConfigCustomResource configCustomResource)
        throws IOException, InterruptedException {
            int phase = ConfigurationProvider.getTestInfo().getPhase();
            LOGGER.info("Executing phase {} of BUR tests", phase);
            Optional<AppPackageResponse> phase1InstantiatePackage = getRestInstantiatePackage();
            Optional<AppPackageResponse> phase2UpgradePackage = getRestUpgradePackage();

            ConfigInstantiate configInstantiateWithoutCluster = configInstantiate.clone();
            LOGGER.info(RELEASE_NAME_MESSAGE, configInstantiateWithoutCluster.getReleaseName());
            Optional<CustomResourceInfo> customResourceInfo = configCustomResource.getCustomResourceInfoList().stream().findFirst();
            if (!customResourceInfo.isPresent()) {
                LOGGER.info("Cannot create CRD. Custom resource info not found");
                fail("Cannot create CRD. Custom resource info not found");
            }
            CustomResourceDefinition customResourceDefinition = createOrReplaceCustomResourceDefinition(configCluster.getExternalConfigFile(),
                                                                                                        customResourceInfo.get());
            if (phase == 1 && phase1InstantiatePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, configInstantiateWithoutCluster, releasesToCleanUp, "-bur-default",
                                                    BUR_TEST, phase1InstantiatePackage.get());
                LOGGER.info("Performing clean up of BUR releases before executing Phase 1 of BUR");
                cleanUpResources(releasesToCleanUp);
                LOGGER.info("Clean up of BUR resources completed");
                VnfInstanceResponseLinks vnfInstanceResponseLinks = performInstantiate(configInstantiateWithoutCluster,
                                                                                       phase1InstantiatePackage,
                                                                                       true,
                                                                                       configInstantiate.getAdditionalAttributesFile());
                performScaleRest(configScale, vnfInstanceResponseLinks, configInstantiateWithoutCluster, configCluster, REST, "Aspect1");

                String vnfInstanceUrl = vnfInstanceResponseLinks.getSelf().getHref();

                queryVnfState(vnfInstanceResponseLinks.getSelf().getHref(), "STARTED", 20, 3000);
                VnfInstanceResponse vnfInstanceResource = queryVnfInstance(vnfInstanceUrl);
                createCustomResource(configCluster.getExternalConfigFile(),
                                     configInstantiateWithoutCluster.getNamespace(),
                                     customResourceDefinition, customResourceInfo.get(), vnfInstanceResource);
            } else if (phase == 2 && phase2UpgradePackage.isPresent()) {
                uniquelyIdentifyReleaseAndNamespace(configCluster, configInstantiateWithoutCluster, releasesToCleanUp, "-bur-default",
                                                    BUR_TEST, phase2UpgradePackage.get());

                Optional<GenericKubernetesResource> vnfInstanceResponseResource =
                        Optional.ofNullable(KubernetesAPISteps.getCustomResource(configCluster.getExternalConfigFile(),
                                                                                 configInstantiateWithoutCluster.getNamespace(),
                                                                                 customResourceDefinition, customResourceInfo.get()));

                if (vnfInstanceResponseResource.isPresent()) {
                    final ObjectMapper mapper = getObjectMapper();
                    final Object vnfInstanceResponseSpec = vnfInstanceResponseResource.get().get("spec");
                    LOGGER.info("Vir response is: {}", vnfInstanceResponseSpec);
                    VnfInstanceResponse crdVnfInstanceResponse;
                    try {
                        crdVnfInstanceResponse = mapper.convertValue(vnfInstanceResponseSpec, VnfInstanceResponse.class);
                    } catch (IllegalArgumentException iae) {
                        if (iae.getMessage().contains("Unrecognized field \"change_package_info\"")) {
                            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            crdVnfInstanceResponse = mapper.convertValue(vnfInstanceResponseSpec, VnfInstanceResponse.class);
                            final ObjectNode node = mapper.convertValue(vnfInstanceResponseSpec, ObjectNode.class);
                            String changePackageInfo = node.get("_links").get("change_package_info").get("href").toString();
                            LOGGER.info("Found change_package_info: {}", changePackageInfo);
                            // Updating to use change_vnfpkg and removing double quotes
                            String updatedLink = changePackageInfo.replace("change_package_info", "change_vnfpkg").replace("\"", "");
                            URILink changeVnfpkg = new URILink().href(updatedLink);
                            crdVnfInstanceResponse.getLinks().setChangeVnfpkg(changeVnfpkg);
                            LOGGER.info("Updated change_package_info link to change_vnfpkg: {}", changeVnfpkg);
                        } else {
                            throw iae;
                        }
                    }
                    LOGGER.info("Vnf State after mapping is: {}", crdVnfInstanceResponse.getInstantiatedVnfInfo().getVnfState());
                    VnfInstanceResponseLinks vnfInstanceResponseLinks = crdVnfInstanceResponse.getLinks();
                    verifyInstanceResponseMatchesSystem(crdVnfInstanceResponse, vnfInstanceResponseLinks);

                    String vnfdId = crdVnfInstanceResponse.getVnfdId();
                    Optional<EvnfmPackage> upgradePackage = configOnboarding.getPackages(ConfigurationProvider.getTestInfo())
                            .stream()
                            .filter(p -> p.getOperation().equalsIgnoreCase(UPGRADE))
                            .filter(p -> p.getPackageBeingUpgraded().equalsIgnoreCase(vnfdId)).findFirst();
                    upgradePackage.ifPresent(aPackage -> verifyIfPackageOnboarded(aPackage.getVnfdId()));

                    String upgradeHeader = upgradeVnf(vnfInstanceResponseLinks,
                                                      configUpgrade,
                                                      configUpgrade.getAdditionalAttributesFile(),
                                                      REST,
                                                      false);
                    queryVnfOperation(upgradeHeader, configUpgrade.getExpectedOperationState(),
                                      configUpgrade.getApplicationTimeOut(), CHANGE_VNFPKG);
                    upgradePackage.ifPresent(aPackage -> queryVnfModel(vnfInstanceResponseLinks.getSelf().getHref(),
                                                                       aPackage.getVnfdId()));
                    deleteCustomResource(configCluster.getExternalConfigFile(),
                                         configInstantiateWithoutCluster.getNamespace(),
                                         customResourceDefinition, customResourceInfo.get());
                    performTerminateAndDeleteIdentifier(configTerminate, vnfInstanceResponseLinks);
                } else {
                    LOGGER.info("Phase 1 package not found, skipping phase 2 upgrade");
                    fail("Phase 1 package not found, skipping phase 2 upgrade");
                }
            } else {
                String message = phase == 1 ?
                        "Phase 1 instantiate package not onboarded, skipping tests" :
                        "Phase 2 upgrade package not onboarded, skipping tests";
                LOGGER.info(message);
                fail(message);
            }
        }
    }
    /*
                                      DO NOT PUT STEPS IN THIS CLASS!
    */
}
