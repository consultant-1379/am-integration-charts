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
package com.ericsson.evnfm.acceptance.steps.common.rest;

import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.SECONDS;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.awaitility.Awaitility.await;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectAndMapVnfControlledScaling;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectDay0VerificationInfo;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.convertListIntoString;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.delay;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.getPodsNamesInNamespaceWithStringInName;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.verifySecretsNotPresentInNamespace;
import static com.ericsson.evnfm.acceptance.steps.kubernetes.rest.KubernetesSteps.verifySecretsPresentInNamespace;
import static com.ericsson.evnfm.acceptance.utils.Constants.INVALID_SCALE_INFO_EXTENSIONS_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.getHelmHistoryForRelease;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.getHelmReleases;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.getReleaseValues;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.HelmHistory;
import com.ericsson.evnfm.acceptance.models.HttpResponse;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class CommonVerifications {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonVerifications.class);

    private static final String HELM_RELEASE_NAME = "name";
    private static final String HELM_RELEASE_CHART = "chart";
    private static final String HELM_HISTORY_REVISION = "revision";
    private static final String HELM_HISTORY_DESCRIPTION = "description";

    private CommonVerifications() {
    }

    //========================================== SCALE INFO EXTENSIONS VERIFICATIONS ===================================================//

    public static void verifyExtensionsAreEqual(EvnfmCnf evnfmCnf, VnfInstanceResponse actualVnfInstance) {
        LOGGER.info("Starts verifying Extensions");
        if (evnfmCnf.getExtensions() == null) {
            return;
        }

        final Map<String, Object> convertedExpectedExtensions = collectAndMapVnfControlledScaling(evnfmCnf.getExtensions());
        final Map<String, Object> convertedActualExtensions = collectAndMapVnfControlledScaling(actualVnfInstance.getExtensions());

        final boolean isExpectedAndActualExtensionsAreEquals =
                isExpectedAndActualVnfInfoMapsAreEquals(convertedExpectedExtensions,
                                                        convertedActualExtensions);

        String actualExtensions = convertListIntoString(convertedActualExtensions.entrySet());
        String expectedExtensions = convertListIntoString(convertedExpectedExtensions.entrySet());

        Assertions.assertThat(isExpectedAndActualExtensionsAreEquals)
                .withFailMessage(format(INVALID_SCALE_INFO_EXTENSIONS_ERROR_MESSAGE, "Extensions", actualExtensions, expectedExtensions))
                .isTrue();

        LOGGER.info("Verifying Extensions completed successfully");
    }

    public static <T> boolean isExpectedAndActualVnfInfoMapsAreEquals(Map<String, T> expectedCnfInfo, Map<String, T> actualCnfInfo) {
        final boolean isEmptyActualCnfData = CollectionUtils.isEmpty(actualCnfInfo);
        final boolean isEmptyExpectedCnfData = CollectionUtils.isEmpty(expectedCnfInfo);

        Assertions.assertThat(isEmptyActualCnfData).isEqualTo(isEmptyExpectedCnfData);

        if (isEmptyActualCnfData && isEmptyExpectedCnfData) {
            return true;
        }

        Assertions.assertThat(actualCnfInfo.size()).isEqualTo(expectedCnfInfo.size());

        return expectedCnfInfo.entrySet().containsAll(actualCnfInfo.entrySet());
    }

    //========================================== VNF LCM OPERATION OCCURRENCE VERIFICATIONS ===================================================//

    public static void verifyResultOfVnfLcmOppOcc(ResponseEntity<VnfLcmOpOcc> vnfLcmOppOccResult,
                                                  final VnfLcmOpOcc.OperationEnum expectedOperationName,
                                                  final String expectedOperationStatus) {

        Assertions.assertThat(vnfLcmOppOccResult.getBody()).isNotNull();
        final VnfLcmOpOcc body = vnfLcmOppOccResult.getBody();

        LOGGER.info("Verify operation result for operation with ID {} and VNF ID {}, expected operationName : {}, expected operation Status: {}."
                            + "Actual operationName : {}, Actual operation Status: {}",
                    body.getId(), body.getVnfInstanceId(), expectedOperationName, expectedOperationStatus, body.getOperation(),
                    body.getOperationState());

        LOGGER.info("Life cycle operation {} finished with state :: {} for url :: {}", body.getOperation(),
                    body.getOperationState(), body.getLinks().getSelf().getHref());

        Assertions.assertThat(body.getOperation()).isEqualTo(expectedOperationName);

        if (expectedOperationStatus != null) {
            Assertions.assertThat(body.getOperationState().toString())
                    .withFailMessage(format("For operation with ID %s and VNF ID %s actual %s and expected status %s are NOT equals",
                                            body.getId(), body.getVnfInstanceId(), body.getOperationState(), expectedOperationStatus))
                    .isEqualToIgnoringCase(expectedOperationStatus);
        }
    }

    public static void verifyResultOfVnfLcmOppOcc(ResponseEntity<VnfLcmOpOcc> vnfLcmOppOccResult,
                                                  final VnfLcmOpOcc.OperationEnum expectedOperationName) {
        verifyResultOfVnfLcmOppOcc(vnfLcmOppOccResult, expectedOperationName, VnfLcmOpOcc.OperationStateEnum.COMPLETED.toString());
    }

    //========================================== DAY 0 SECRETS VERIFICATIONS ===================================================//

    public static boolean verifyDay0SecretsWhileVnfLcmOperationIsInProgress(Day0SecretVerificationInfo day0Info, boolean secretsVerified,
                                                                            int applicationTimeout) {
        if (secretsVerified) {
            return true;
        } else if (day0Info != null) {
            await().atMost(applicationTimeout, SECONDS).pollInterval(10, SECONDS).until(() -> verifySecretsPresentInNamespace(day0Info));
            LOGGER.info("Successfully verified secrets for {} in {} namespace",
                        day0Info.getVnfInstanceName(), day0Info.getNamespace());
            return true;
        }
        return false;
    }

    public static void verifyDay0SecretsAfterVnfLcmOperationWasCompleted(Day0SecretVerificationInfo day0Info, boolean secretsVerified) {
        if (day0Info != null) {
            Assertions.assertThat(secretsVerified).withFailMessage("Failed to verify secrets for %s in %s namespace",
                                                                   day0Info.getVnfInstanceName(), day0Info.getNamespace()).isTrue();
            delay(3000);
            verifySecretsNotPresentInNamespace(day0Info);
        }
    }

    public static void verifyDay0SecretsCreated(EvnfmCnf cnf, long totalTime, long interval) {
        Day0SecretVerificationInfo verificationInfo = collectDay0VerificationInfo(cnf);
        if (verificationInfo == null) {
            return;
        }
        LOGGER.info("Verifying secrets are created in {} for {}", cnf.getNamespace(), cnf.getVnfInstanceName());
        long startTime = System.currentTimeMillis();
        boolean secretsVerified = false;
        while ((System.currentTimeMillis() - startTime) < totalTime) {
            secretsVerified = verifySecretsPresentInNamespace(verificationInfo);
            if (secretsVerified) {
                LOGGER.info("Successfully verified secrets for {} in {} namespace",
                            verificationInfo.getVnfInstanceName(), verificationInfo.getNamespace());
                break;
            }
            delay(interval);
        }

        Assertions.assertThat(secretsVerified)
                .withFailMessage("Failed to verify secrets for %s in %s namespace",
                                 verificationInfo.getVnfInstanceName(), verificationInfo.getNamespace())
                .isTrue();
    }

    public static void verifyDay0SecretsDeleted(final EvnfmCnf cnf) {
        Day0SecretVerificationInfo verificationInfo = collectDay0VerificationInfo(cnf);
        if (verificationInfo == null) {
            return;
        }
        LOGGER.info("Verifying secrets are deleted in {} for {}", cnf.getNamespace(), cnf.getVnfInstanceName());
        verifySecretsNotPresentInNamespace(verificationInfo);
    }

    //========================================== PODS (TARGETS) VERIFICATIONS ===================================================//

    public static void verifyNumberOfTargets(EvnfmCnf evnfmCnf) {
        if (evnfmCnf.getTargets() != null) {
            LOGGER.info("Verifying number of pods");

            evnfmCnf.getTargets().forEach((key, expectedValue) -> verifyNumberOfPods(key, expectedValue, evnfmCnf));
        }
    }

    public static void verifyNumberOfTargets(EvnfmCnf evnfmCnf, long timeoutSeconds) {
        if (evnfmCnf.getTargets() != null) {
            LOGGER.info("Verifying number of pods after the allocated time " + timeoutSeconds + "s");

            evnfmCnf.getTargets().forEach(
                    (key, expectedValue) -> await().atMost(timeoutSeconds, SECONDS).untilAsserted(
                            () -> verifyNumberOfPods(key, expectedValue, evnfmCnf)));
        }
    }

    private static void verifyNumberOfPods(String namePrefix, Integer expectedNumber, EvnfmCnf evnfmCnf) {
        LOGGER.info("Verifying that pod with prefix {} has {} replicas for {} instance",
                    namePrefix, expectedNumber, evnfmCnf.getVnfInstanceName());
        Integer actualNumber = getPodsNamesInNamespaceWithStringInName(evnfmCnf.getCluster().getLocalPath(), evnfmCnf.getNamespace(), namePrefix)
                .size();

        Assertions.assertThat(actualNumber)
                .withFailMessage("For pod: %s Expected pod count is %d but actual is :: %d", namePrefix, expectedNumber, actualNumber)
                .isEqualTo(expectedNumber);
    }

    //========================================== HELM HISTORY VERIFICATIONS ===================================================//

    public static void verifyHelmHistory(final EvnfmCnf cnf) {
        List<Map<String, String>> helmReleases = getHelmReleases(cnf.getNamespace(), cnf.getCluster().getLocalPath());

        for (Map<String, String> helmRelease : helmReleases) {
            final Optional<List<HelmHistory>> expectedReleaseHistory = findExpectedHelmHistoryForRelease(helmRelease.get(HELM_RELEASE_CHART), cnf);

            assertThat(expectedReleaseHistory)
                    .withFailMessage(() -> format("Unknown helm chart %s in release %s. Expected charts are: %s",
                                                  helmRelease.get(HELM_RELEASE_CHART),
                                                  helmRelease.get(HELM_RELEASE_NAME),
                                                  cnf.getExpectedHelmHistory().keySet()))
                    .isNotEmpty();

            verifyHelmReleaseHistory(helmRelease.get(HELM_RELEASE_NAME), cnf, expectedReleaseHistory.get());
        }
    }

    public static void verifyNumberOfHelmReleasesChartNames(final EvnfmCnf cnf) {
        List<Map<String, String>> helmReleases = getHelmReleases(cnf.getNamespace(), cnf.getCluster().getLocalPath());

        Set<String> helmReleasesChartNames = helmReleases.stream()
                .map(helmRelease -> helmRelease.get(HELM_RELEASE_CHART))
                .collect(Collectors.toSet());
        assertThat(helmReleasesChartNames.size()).isEqualTo(cnf.getExpectedHelmHistory().keySet().size());
    }

    private static Optional<List<HelmHistory>> findExpectedHelmHistoryForRelease(final String chartName, final EvnfmCnf cnf) {
        return cnf.getExpectedHelmHistory().entrySet().stream()
                .filter(entry -> chartName.startsWith(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private static void verifyHelmReleaseHistory(final String releaseName, final EvnfmCnf cnf, final List<HelmHistory> expectedReleaseHistory) {
        List<Map<String, String>> actualHelmHistory = getHelmHistoryForRelease(releaseName,
                                                                               cnf.getNamespace(),
                                                                               cnf.getCluster().getLocalPath());

        assertThat(actualHelmHistory).extracting(HELM_HISTORY_REVISION, HELM_HISTORY_DESCRIPTION)
                .containsExactlyElementsOf(mapToHistoryRecordTuples(expectedReleaseHistory));
    }

    private static List<Tuple> mapToHistoryRecordTuples(final List<HelmHistory> expectedReleaseHistory) {
        return expectedReleaseHistory.stream()
                .map(helmHistory -> tuple(helmHistory.getRevision(), helmHistory.getDescription()))
                .collect(Collectors.toList());
    }
    //========================================== VALUES VERIFICATIONS ===================================================//

    public static void verifyHelmValues(EvnfmCnf cnf) {
        List<Map<String, Object>> rollbackReleaseValues = getReleaseValues(cnf);
        Map<String, Object> expectedHelmValues = cnf.getExpectedHelmValues();
        rollbackReleaseValues.forEach(entry -> assertThat(entry).containsAllEntriesOf(expectedHelmValues));
    }

    public static void verifyNotContainHelmValues(EvnfmCnf previousCnf, EvnfmCnf currentCnf) {
        List<Map<String, Object>> values = getReleaseValues(currentCnf);
        Map<String, Object> notExpectedHelmValues = previousCnf.getExpectedHelmValues();
        if (notExpectedHelmValues != null) {
            values.forEach(entry -> {
                notExpectedHelmValues.entrySet().forEach(notExpectedHelmValue -> {
                    assertThat(entry).doesNotContain(notExpectedHelmValue);
                });
            });
        }
    }

    public static void verifyHelmValues(EvnfmCnf previousCnf, EvnfmCnf currentCnf) {
        List<Map<String, Object>> values = getReleaseValues(currentCnf);
        Map<String, Object> expectedHelmValues = currentCnf.getExpectedHelmValues();
        Map<String, Object> notExpectedHelmValues = previousCnf.getExpectedHelmValues();
        values.forEach(entry -> assertThat(entry).containsAllEntriesOf(expectedHelmValues));
        if (notExpectedHelmValues != null) {
            values.forEach(entry -> {
                notExpectedHelmValues.entrySet().forEach(notExpectedHelmValue -> {
                    assertThat(entry).doesNotContain(notExpectedHelmValue);
                });
            });
        }
    }

    public static void verifyFirstChartName(EvnfmCnf cnf) {
        assertThat(getHelmReleases(cnf.getNamespace(), cnf.getCluster().getLocalPath()).stream().findFirst().get()
                           .get(HELM_RELEASE_NAME).equals(cnf.getVnfInstanceName()));
    }

    public static void verifyExpectedHttpError(HttpClientErrorException exception, HttpResponse expectedError) {
        assertThat(exception.getStatusCode()).isEqualTo(expectedError.getStatusCode());
        assertThat(exception.getResponseBodyAsString()).contains(expectedError.getMessage());
    }
}
