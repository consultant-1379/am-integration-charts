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
package com.ericsson.evnfm.acceptance.steps.scale.rest;

import static java.util.stream.Collectors.toMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.HttpHeaders.LOCATION;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfApiClient.getVnfcScaleInfo;
import static com.ericsson.evnfm.acceptance.utils.Constants.GET_VNFC_SCALE_INFO_PARAMS;
import static com.ericsson.evnfm.acceptance.utils.Constants.HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.ericsson.vnfm.orchestrator.model.VnfcScaleInfo;

public class ScaleCnfVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleCnfVerify.class);

    private ScaleCnfVerify() {
    }

    public static void verifyScaleCnfResponseSuccess(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.SCALE, HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(HttpStatus.ACCEPTED).isEqualTo(response.getStatusCode());
        assertThat(response.getHeaders()).isNotNull();
        final List<String> headers = response.getHeaders().get(LOCATION);
        assertThat(CollectionUtils.isEmpty(headers)).withFailMessage(HEADER_LOCATION_IS_NOT_PRESENT_ERROR_MESSAGE).isFalse();
        assertThat(headers.get(0)).isNotNull();

        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, VnfLcmOpOcc.OperationEnum.SCALE);
    }

    public static void verifyCnfIsScaled(VnfInstanceResponse vnfInstanceResponse) {
        assertThat(vnfInstanceResponse).isNotNull();
        assertThat(vnfInstanceResponse.getInstantiatedVnfInfo()).isNotNull();
        assertThat(vnfInstanceResponse.getInstantiatedVnfInfo().getScaleStatus()).isNotNull();
    }

    public static void verifyScaleInfo(EvnfmCnf evnfmCnf, VnfInstanceResponse vnfInstanceResponse) {
        LOGGER.info("Starts verifying ScaleInfo");
        if (evnfmCnf.getScaleInfo() == null) {
            return;
        }

        final Map<String, Integer> actualScalingInfo = vnfInstanceResponse.getInstantiatedVnfInfo()
                .getScaleStatus()
                .stream()
                .collect(toMap(ScaleInfo::getAspectId, ScaleInfo::getScaleLevel));
        final Map<String, Integer> expectedScaleInfo = evnfmCnf.getScaleInfo();

        assertThat(actualScalingInfo).isEqualTo(expectedScaleInfo);

        LOGGER.info("Verifying ScaleInfo completed successfully");
    }

    public static void verifyScaleDataPersistenceAfterUpgrade(List<ScaleInfo> scaleInfoBeforeUpgrade,
                                                              List<ScaleInfo> scaleInfoAfterUpgrade,
                                                              boolean isPersistScaleInfo) {

        final Map<String, Integer> aspectIdsToLevelAfterUpgrade = scaleInfoAfterUpgrade.stream()
                .collect(toMap(ScaleInfo::getAspectId, ScaleInfo::getScaleLevel));

        final Map<String, Integer> aspectIdsToLevelExpected = scaleInfoBeforeUpgrade.stream()
                .filter(scaleInfo -> aspectIdsToLevelAfterUpgrade.containsKey(scaleInfo.getAspectId()))
                .collect(toMap(ScaleInfo::getAspectId, scaleInfo -> isPersistScaleInfo ? scaleInfo.getScaleLevel() : 0));

        assertThat(aspectIdsToLevelAfterUpgrade).containsAllEntriesOf(aspectIdsToLevelExpected);
    }

    public static void verifyScaleDataPersistenceAfterRollback(List<ScaleInfo> scaleInfoBeforeRollback, List<ScaleInfo> scaleInfoAfterRollback) {
        verifyScaleDataPersistenceAfterUpgrade(scaleInfoBeforeRollback, scaleInfoAfterRollback, true);
    }

    public static List<ScaleInfo> collectScaleInfo(final EvnfmCnf upgradeCnf, final User user) {
        final VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                 upgradeCnf.getVnfInstanceName(), user);

        verifyCnfIsScaled(vnfInstanceByRelease);
        final List<ScaleInfo> actualScaleStatus = vnfInstanceByRelease.getInstantiatedVnfInfo().getScaleStatus();
        return actualScaleStatus;
    }

    public static void verifyReplicaCountInDatabaseForAspect(User user, final String vnfInstanceName, String aspect,
                                                             Integer expectedReplicaCount, String... target) {
        Optional<VnfInstanceResponse> vnfInstanceResponse = Optional.ofNullable(getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                                        vnfInstanceName, user));
        String queryParams = String
                .format(GET_VNFC_SCALE_INFO_PARAMS, "1", aspect, "SCALE_OUT");
        List<VnfcScaleInfo> vnfcScaleInfoAspect = getVnfcScaleInfo(user, vnfInstanceResponse.get().getId(), queryParams);
        List<VnfcScaleInfo> nonMatchingTargets =
                vnfcScaleInfoAspect.stream().filter(vnfcScaleInfo -> !Objects.equals(vnfcScaleInfo.getCurrentReplicaCount(), expectedReplicaCount)
                                && new HashSet<>(Arrays.asList(target)).contains(vnfcScaleInfo.getVnfcName()))
                        .collect(Collectors.toList());
        verifyNonMatchingReplicas(nonMatchingTargets, expectedReplicaCount);
    }

    public static void verifyNonMatchingReplicas(List<VnfcScaleInfo> nonMatchingTargets, int expectedReplicaCountAspect1) {
        if (!nonMatchingTargets.isEmpty()) {
            nonMatchingTargets.forEach(target -> fail(String.format(
                    "Replica count for %s is %s, but has to be %s. \n", target.getVnfcName(), target.getCurrentReplicaCount(),
                    expectedReplicaCountAspect1
            )));
        }
    }
}
