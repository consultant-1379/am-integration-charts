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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.cismcluster;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders.loadClusterConfigs;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders.loadPackagesToOnboard;
import static com.ericsson.eo.evnfm.acceptance.testng.infrastructure.ClusterUtils.checkNameSpaceExist;
import static com.ericsson.eo.evnfm.acceptance.testng.infrastructure.ClusterUtils.cleanupNamespaces;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.deregisterCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.getClustersList;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.modifyCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.modifyClusterExpectingError;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.registerCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.registerClusterExpectingError;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.searchForClusterWithName;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.updateCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigSteps.updateClusterExpectingError;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigListContainsCluster;
import static com.ericsson.evnfm.acceptance.steps.clusterconfigs.rest.ClusterConfigVerify.verifyClusterConfigListDoesNotContainCluster;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateApiClient.executeInstantiateCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.executeInstantiateCnfOperationRequestAndVerifyResponse;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfVerify.verifyCnfWithUnregisteredClusterWasNotInstantiated;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.createNamespaceIfNotExists;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.deleteNamespace;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.getPodsNamesInNamespaceWithStringInName;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.deletePackageIfPresent;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.onboardPackageIfNotPresent;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performCleanupCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performDeleteCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateAndDeleteIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.CISMClustersDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ClusterConfigStatus;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.fasterxml.jackson.core.JsonProcessingException;

public class CISMClusterTest extends RestBase {

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        loadPackagesToOnboard(iTestContext).forEach(p -> onboardPackageIfNotPresent(p, user, false));
        loadClusterConfigs(iTestContext).forEach((testName, clusterConfig) -> {
            if (clusterConfig.getNamespace() != null && clusterConfig.isCreateNamespace()) {
                createNamespaceIfNotExists(clusterConfig.getLocalPath(), clusterConfig.getNamespace());
            }
        });
    }

    @Test(description = "CISM Cluster 001: Register/Update/Deregister Config", dataProvider = "clusterConfigTestDataRegister",
            dataProviderClass = CISMClustersDataProviders.class)
    public void registerUpdateDeregisterClusterConfigPositiveTest(ClusterConfig clusterConfig) {
        registerUpdateDeregisterTestHelper(clusterConfig);
    }

    @Test(description = "CISM Cluster 002: expecting state changes after life-cycle operations",
            dataProvider = "clusterConfigTestDataLCM", dataProviderClass = CISMClustersDataProviders.class)
    public void clusterConfigLifecyclePositiveTest(ClusterConfig clusterConfig, EvnfmCnf packageInstantiate, EvnfmCnf packageUpgrade) {

        String clusterConfigPath = clusterConfig.getLocalPath();
        packageInstantiate.setCluster(clusterConfig);
        packageUpgrade.setCluster(clusterConfig);

        registerCluster(clusterConfig, user);

        ClusterConfig registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.NOT_IN_USE.name());

        performCreateIdentifierAndInstantiateCnfStep(packageInstantiate, user);

        List<String> podsOnTargetCluster = getPodsNamesInNamespaceWithStringInName(clusterConfigPath,
                                                                                   packageInstantiate.getNamespace(),
                                                                                   packageInstantiate.getPods().get(0));
        assertThat(podsOnTargetCluster.size() > 0)
                .withFailMessage("Pod is absent on target cluster")
                .isTrue();

        registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.IN_USE.name());

        performUpgradeCnfStep(packageUpgrade, user);

        registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.IN_USE.name());

        performTerminateAndDeleteIdentifierStep(packageUpgrade, user);
        registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.NOT_IN_USE.name());

        deregisterCluster(clusterConfig, user);
        verifyClusterConfigListDoesNotContainCluster(getClustersList(user), clusterConfig);
    }

    @Test(description = "CISM Cluster 003: Expecting error during duplicate cluster registration request",
            dataProvider = "clusterConfigDataDuplicateNegative", dataProviderClass = CISMClustersDataProviders.class)
    public void registerClusterDuplicateNegativeTest(
            ClusterConfig clusterConfig, ProblemDetails expectedError) throws JsonProcessingException {
        registerCluster(clusterConfig, user);
        registerClusterExpectingError(clusterConfig, expectedError, user);
        deregisterCluster(clusterConfig, user);
    }

    @Test(description = "CISM Cluster 004: Expecting error registering cluster with invalid data",
            dataProvider = "clusterConfigDataInvalidNegative", dataProviderClass = CISMClustersDataProviders.class)
    public void registerInvalidClusterNegativeTest(
            ClusterConfig clusterConfig, ProblemDetails expectedError) throws JsonProcessingException {
        registerClusterExpectingError(clusterConfig, expectedError, user);
    }

    @Test(description = "CISM Cluster 005: Register/Update/Deregister Config with namespace",
            dataProvider = "clusterConfigWithExistingNamespace", dataProviderClass = CISMClustersDataProviders.class)
    public void registerUpdateDeregisterClusterConfigWithNamespacePositiveTest(ClusterConfig clusterConfig) {
        registerUpdateDeregisterTestHelper(clusterConfig);
    }

    /**
     * Instantiate the Cnf and expect client/server error response
     * due to request built with cluster reference to unregistered valid cluster
     *
     * @param cnfToInstantiate Cnf instantiate details
     * @param expectedError    Error object expected to receive
     *                         <p>
     *                         Example response:
     *                         {@code {"type":"Not Found","title":"URI does not exist","status":404,"detail":"Cluster config file invalid not
     *                         found","instance":""} }
     */
    @Test(description = "CISM Cluster 007: expecting http error at life-cycle operation execution during instantiation with invalid cluster config",
            dataProvider = "clusterConfigNegativeInvalidClusterTestDataLCM", dataProviderClass = CISMClustersDataProviders.class)
    public void clusterConfigLifecycleNegativeInvalidClusterTest(EvnfmCnf cnfToInstantiate,
                                                                 ProblemDetails expectedError) throws JsonProcessingException {

        //setup
        performCreateCnfIdentifierStep(cnfToInstantiate, user);

        try {
            executeInstantiateCnfOperationRequest(cnfToInstantiate, user);
        } catch (HttpClientErrorException e) {
            final ProblemDetails actualBadRequest = getObjectMapper().readValue(e.getResponseBodyAsString(), ProblemDetails.class);
            verifyCnfWithUnregisteredClusterWasNotInstantiated(actualBadRequest, expectedError);
        } finally {
            //cleanup
            performDeleteCnfIdentifierStep(cnfToInstantiate, user);
        }
    }

    @Test(description = "CISM Cluster 008: Expecting error update cluster with invalid data", dataProvider = "clusterUpdateConfigDataInvalidNegative",
            dataProviderClass = CISMClustersDataProviders.class)
    public void updateInvalidClusterNegativeTest(ClusterConfig validClusterConfig, ClusterConfig invalidClusterConfig,
                                                 ProblemDetails expectedError) throws JsonProcessingException {
        registerCluster(validClusterConfig, user);
        updateClusterExpectingError(invalidClusterConfig, expectedError, user, validClusterConfig.getName(), false);
        deregisterCluster(validClusterConfig, user);
    }

    @Test(description = "CISM Cluster 009: Expecting error update cluster not exist", dataProvider = "clusterUpdateConfigDataNotExistNegative",
            dataProviderClass = CISMClustersDataProviders.class)
    public void invalidClusterConfigNotExistNegativeTest(ClusterConfig validClusterConfig, ProblemDetails expectedError, String clusterName)
    throws JsonProcessingException {
        updateClusterExpectingError(validClusterConfig, expectedError, user, clusterName, false);
    }

    @Test(description = "CISM Cluster 010: Expecting error update with skipSameClusterVerification = false and success with "
            + "skipSameClusterVerification = true",
            dataProvider = "clusterUpdateConfigDataNotSkipVerificationPositive", dataProviderClass = CISMClustersDataProviders.class)
    public void updateClusterConfigPositiveTestSkipVerification(ClusterConfig clusterConfig, EvnfmCnf packageInstantiate,
                                                                ProblemDetails expectedError) throws JsonProcessingException {
        packageInstantiate.setCluster(clusterConfig);
        registerCluster(clusterConfig, user);
        performCreateIdentifierAndInstantiateCnfStep(packageInstantiate, user);

        cleanupNamespaces(List.of(packageInstantiate), user);

        checkNameSpaceExist(packageInstantiate);

        updateClusterExpectingError(clusterConfig, expectedError, user, clusterConfig.getName(), false);

        updateCluster(clusterConfig, user, true);
        verifyClusterConfigListContainsCluster(getClustersList(user), clusterConfig);

        performTerminateAndDeleteIdentifierStep(packageInstantiate, user);
        deregisterCluster(clusterConfig, user);
    }

    private void registerUpdateDeregisterTestHelper(ClusterConfig clusterConfig) {
        registerCluster(clusterConfig, user);
        verifyClusterConfigListContainsCluster(getClustersList(user), clusterConfig);
        updateCluster(clusterConfig, user, false);
        verifyClusterConfigListContainsCluster(getClustersList(user), clusterConfig);
        deregisterCluster(clusterConfig, user);
        verifyClusterConfigListDoesNotContainCluster(getClustersList(user), clusterConfig);
    }

    @Test(description = "CISM Cluster 011: Expecting error too long description", dataProvider =
            "clusterModifyConfigTooLongDescriptionNegative",
            dataProviderClass = CISMClustersDataProviders.class)
    public void modifyInvalidClusterNegativeTest(ClusterConfig clusterConfig, ProblemDetails expectedError, String patchFields) throws JsonProcessingException {
        registerCluster(clusterConfig, user);

        modifyClusterExpectingError(patchFields, clusterConfig.getName(), user, expectedError, false);

        deregisterCluster(clusterConfig, user);
    }

    @Test(description = "CISM Cluster 012: Expecting error modify with skipSameClusterVerification = false and success with "
            + "skipSameClusterVerification = true",
            dataProvider = "clusterModifyConfigDataNotSkipVerificationPositive", dataProviderClass = CISMClustersDataProviders.class)
    public void modifyClusterConfigPositiveTestSkipVerification(ClusterConfig clusterConfig, EvnfmCnf packageInstantiate,
                                                                ProblemDetails expectedError, String patchFields)
    throws JsonProcessingException {
        // register cluster
        packageInstantiate.setCluster(clusterConfig);
        registerCluster(clusterConfig, user);
        performCreateIdentifierAndInstantiateCnfStep(packageInstantiate, user);

        cleanupNamespaces(List.of(packageInstantiate), user);

        checkNameSpaceExist(packageInstantiate);

        modifyClusterExpectingError(patchFields, clusterConfig.getName(), user, expectedError, false);

        modifyCluster(patchFields, clusterConfig, user, true);

        // deregister cluster
        performTerminateAndDeleteIdentifierStep(packageInstantiate, user);
        deregisterCluster(clusterConfig, user);
    }

    @Test(description = "CISM Cluster 013: Expecting error modify cluster config with non-existent cluster",
            dataProvider = "clusterModifyConfigDataNotExistNegative", dataProviderClass = CISMClustersDataProviders.class)
    public void modifyNonexistentClusterConfigNegativeTest(ClusterConfig clusterConfig,
                                                           ProblemDetails expectedError,
                                                           String clusterName,
                                                           String patchFields) throws JsonProcessingException {

        modifyClusterExpectingError(patchFields, clusterName, user, expectedError, true);
    }

    @Test(description = "CISM Cluster 014: Expecting error modify cluster with invalid data", dataProvider = "clusterModifyConfigDataInvalidNegative",
            dataProviderClass = CISMClustersDataProviders.class)
    public void modifyWithInvalidClusterConfigDataNegativeTest(ClusterConfig validClusterConfig,
                                                               String patchFields,
                                                               ProblemDetails expectedError) throws JsonProcessingException {
        registerCluster(validClusterConfig, user);

        modifyClusterExpectingError(patchFields, validClusterConfig.getName(), user, expectedError, false);

        deregisterCluster(validClusterConfig, user);
    }

    @Test(description = "CISM Cluster 015: expecting state changes after failed life-cycle operations",
            dataProvider = "clusterConfigNegativeOperationFailedTestDataLCM", dataProviderClass = CISMClustersDataProviders.class)
    public void clusterConfigLifecycleNegativeOperationFailedTest(ClusterConfig clusterConfig, EvnfmCnf packageInstantiate) {
        packageInstantiate.setCluster(clusterConfig);

        registerCluster(clusterConfig, user);

        ClusterConfig registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.NOT_IN_USE.name());

        performCreateCnfIdentifierStep(packageInstantiate, user);

        final String operationLink = executeInstantiateCnfOperationRequestAndVerifyResponse(packageInstantiate, user);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(packageInstantiate, user, operationLink, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, INSTANTIATE, packageInstantiate.getExpectedOperationState());

        registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.IN_USE.name());

        performCleanupCnfStep(packageInstantiate, user);

        registeredClusterConfig = searchForClusterWithName(clusterConfig.getName(), user);
        assertThat(registeredClusterConfig.getStatus()).isEqualTo(ClusterConfigStatus.NOT_IN_USE.name());

        deregisterCluster(clusterConfig, user);
        verifyClusterConfigListDoesNotContainCluster(getClustersList(user), clusterConfig);
    }

    @AfterClass
    public void cleanUpAfterTest(ITestContext iTestContext) throws IOException {
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        List<String> registeredClusterConfigsNames = getClustersList(user).stream()
                .map(ClusterConfig::getName).collect(Collectors.toList());
        clusterConfigs.forEach((testName, clusterConfig) -> {
            if (registeredClusterConfigsNames.contains(clusterConfig.getName())) {
                deregisterCluster(clusterConfig, user);
            }
            if (clusterConfig.getNamespace() != null && clusterConfig.isCreateNamespace()) {
                // returns false if namespace did not exist
                deleteNamespace(clusterConfig.getLocalPath(), clusterConfig.getNamespace());
            }
        });
        List<EvnfmBasePackage> packagesToOnboard = loadPackagesToOnboard(iTestContext);
        packagesToOnboard.forEach(pkg -> deletePackageIfPresent(pkg, user));
    }
}
