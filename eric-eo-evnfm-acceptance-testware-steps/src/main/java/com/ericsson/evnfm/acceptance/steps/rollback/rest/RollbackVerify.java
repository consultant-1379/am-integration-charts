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
package com.ericsson.evnfm.acceptance.steps.rollback.rest;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmValues;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.getPackageByVnfdIdentifier;
import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.PACKAGE_VNFD_ID_NOT_FOUND;
import static com.ericsson.evnfm.acceptance.utils.Constants.ROLLBACK_LIFECYCLE_OPERATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.STARTS_VERIFICATION_LOG;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.getHelmReleases;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ericsson.amonboardingservice.model.HelmPackage;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class RollbackVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(RollbackVerify.class);

    private static final String SPIDER_APP = "spider-app";

    private static final String CHART = "chart";

    private RollbackVerify() {
    }

    public static void verifyRollbackResponse(ResponseEntity<Void> response) {
        LOGGER.info(STARTS_VERIFICATION_LOG, ROLLBACK_LIFECYCLE_OPERATION, HttpStatus.ACCEPTED);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        LOGGER.info(OPERATION_RESPONSE_VERIFICATION_LOG, ROLLBACK_LIFECYCLE_OPERATION);
    }

    public static void verifyRollbackAtFailure(final EvnfmCnf evnfmCnf, User user) {
        VnfInstanceResponse vnfInstance = getVNFInstanceByRelease(
                EVNFM_INSTANCE.getEvnfmUrl(), evnfmCnf.getVnfInstanceName(), user);
        VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(evnfmCnf.getVnfdId(), user).orElseThrow(
                () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, evnfmCnf.getVnfdId())));

        assertThat(vnfInstance.getVnfdId()).isEqualTo(vnfPkgInfo.getVnfdId());
        assertThat(vnfInstance.getVnfSoftwareVersion()).isEqualTo(vnfPkgInfo.getVnfSoftwareVersion());
        assertThat(vnfInstance.getVnfdVersion()).isEqualTo(vnfPkgInfo.getVnfdVersion());

        List<HelmPackage> helmPackageList = vnfPkgInfo.getHelmPackageUrls()
                .stream()
                .filter(helmPackage -> helmPackage.getChartType().equals(HelmPackage.ChartTypeEnum.CNF))
                .collect(Collectors.toList());

        List<Map<String, String>> helmReleases = getHelmReleases(evnfmCnf.getNamespace(),
                                                                 evnfmCnf.getCluster().getLocalPath());
        assertThat(helmReleases).hasSize(helmPackageList.size());

        List<String> chartNameListFromPackage = helmPackageList.stream()
                .map(helmPackage -> String.format("%s-%s", helmPackage.getChartName(), helmPackage.getChartVersion()))
                .collect(Collectors.toList());
        List<String> chartNameListFromHelmReleases = helmReleases.stream()
                .map(release -> release.get(CHART))
                .collect(Collectors.toList());
        assertThat(chartNameListFromHelmReleases).containsOnlyElementsOf(chartNameListFromPackage);

        verifyHelmHistory(evnfmCnf);
        verifyHelmValues(evnfmCnf);
        verifyNumberOfTargets(evnfmCnf);
    }

    public static void verifyRollback(final EvnfmCnf cnfAfterRollback, User user) {
        VnfInstanceResponse vnfInstance = getVNFInstanceByRelease(
                EVNFM_INSTANCE.getEvnfmUrl(), cnfAfterRollback.getVnfInstanceName(), user);
        VnfPkgInfo vnfPkgInfo = getPackageByVnfdIdentifier(cnfAfterRollback.getVnfdId(), user).orElseThrow(
                () -> new RuntimeException(String.format(PACKAGE_VNFD_ID_NOT_FOUND, cnfAfterRollback.getVnfdId())));

        assertThat(vnfInstance.getVnfdId()).isEqualTo(vnfPkgInfo.getVnfdId());
        assertThat(vnfInstance.getVnfSoftwareVersion()).isEqualTo(vnfPkgInfo.getVnfSoftwareVersion());
        assertThat(vnfInstance.getVnfdVersion()).isEqualTo(vnfPkgInfo.getVnfdVersion());

        List<HelmPackage> helmPackageList = getHelmPackageListFromVnfPkgInfo(vnfPkgInfo);

        List<Map<String, String>> helmReleases = getHelmReleases(cnfAfterRollback.getNamespace(),
                                                                 cnfAfterRollback.getCluster().getLocalPath());
        assertThat(helmReleases).hasSize(helmPackageList.size());

        List<String> chartNameListFromPackage = getChartNameListFromPackage(helmPackageList);

        List<String> chartNameListFromHelmReleases = helmReleases.stream()
                .map(release -> release.get(CHART))
                .collect(Collectors.toList());

        assertThat(chartNameListFromHelmReleases.size()).isEqualTo(chartNameListFromPackage.size());
        assertThat(chartNameListFromHelmReleases).contains(getChartName(chartNameListFromPackage, SPIDER_APP));

        verifyHelmHistory(cnfAfterRollback);

        verifyHelmValues(cnfAfterRollback);
        verifyNumberOfTargets(cnfAfterRollback);
    }

    private static List<HelmPackage> getHelmPackageListFromVnfPkgInfo(VnfPkgInfo vnfPkgInfo) {
        return vnfPkgInfo.getHelmPackageUrls()
                .stream()
                .filter(helmPackage -> helmPackage.getChartType().equals(HelmPackage.ChartTypeEnum.CNF))
                .collect(Collectors.toList());
    }

    private static List<String> getChartNameListFromPackage(List<HelmPackage> helmPackages) {
        return helmPackages.stream()
                .map(helmPackage -> String.format("%s-%s", helmPackage.getChartName(), helmPackage.getChartVersion()))
                .collect(Collectors.toList());
    }

    private static String getChartName(List<String> chartNames, String chart) {
        final Optional<String> chartName = chartNames.stream()
                .filter(s -> s.contains(chart))
                .findFirst();

        return chartName.orElse(null);
    }
}
