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
package com.ericsson.evnfm.acceptance.steps.rest;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.checkIfScaleCanBePerformed;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.getCurrentValues;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScale;
import static com.ericsson.evnfm.acceptance.steps.KubernetesSteps.verifyScaleReset;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getRestInstantiatePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getUiUpgradePackage;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.getVnfcScaleInfo;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryClusterName;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryInstantiationState;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfModel;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfOperation;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.queryVnfState;
import static com.ericsson.evnfm.acceptance.steps.rest.VnfmOrchestratorSteps.scaleVnf;
import static com.ericsson.evnfm.acceptance.steps.rest.kubernetes.KubernetesAPISteps.verifyPodsCountByStatus;
import static com.ericsson.evnfm.acceptance.utils.Constants.DEPLOYMENT;
import static com.ericsson.evnfm.acceptance.utils.Constants.POD_LABEL_NAME;
import static com.ericsson.evnfm.acceptance.utils.Constants.REST;
import static com.ericsson.evnfm.acceptance.utils.Constants.RUNNING;
import static com.ericsson.evnfm.acceptance.utils.Constants.TERMINATING;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.SCALE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;
import com.ericsson.vnfm.orchestrator.model.VnfcScaleInfo;

public class ScaleSteps {

    private static Logger LOGGER = LoggerFactory.getLogger(ScaleSteps.class);

    public static void performScaleRest(final ConfigScale configScale,
                                        final VnfInstanceResponseLinks vnfInstanceResponseLinks, ConfigInstantiate configInstantiate,
                                        final ConfigCluster configCluster, final String testType, String aspectId) {
        if (checkIfScaleCanBePerformed(configScale)) {
            Map<String, Map<String, String>> currentValues = getCurrentValues(configScale, configInstantiate,
                                                                                  configCluster);
            scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_OUT", testType, aspectId);
            Map<String, Map<String, String>> scaledValues = getCurrentValues(configScale, configInstantiate,
                                                                                 configCluster);
            verifyScale(configScale, scaledValues);
            verifyScaledPods(configCluster.getExternalConfigFile(), configScale.getApplicationTimeout(),
                             configInstantiate.getNamespace(), configScale.getDeployments());
            scaleVnfTest(configScale, vnfInstanceResponseLinks, "SCALE_IN", testType, aspectId);
            Map<String, Map<String, String>> resetValues = getCurrentValues(configScale, configInstantiate,
                                                                                configCluster);
            verifyScaleReset(currentValues, resetValues);
            verifyScaledPods(configCluster.getExternalConfigFile(), configScale.getApplicationTimeout(),
                             configInstantiate.getNamespace(), currentValues.get(DEPLOYMENT));
        } else {
            LOGGER.info("Scale config missing mandatory params skipping scale\n");
        }
    }

    public static void scaleVnfTest(final ConfigScale configScale,
                                    final VnfInstanceResponseLinks vnfInstanceResponseLinks, final String scaleType, final String testType, String aspectId) {
        String scaleHeader = scaleVnf(vnfInstanceResponseLinks, configScale, scaleType, aspectId);
        queryVnfOperation(scaleHeader, configScale.getExpectedOperationState(), configScale.getApplicationTimeout(),
                          SCALE);
        queryVnfState(vnfInstanceResponseLinks.getSelf().getHref(), "STARTED", 20, 3000);
        Optional<AppPackageResponse> scalePackage = getScaledPackage(testType);
        queryVnfModel(vnfInstanceResponseLinks.getSelf().getHref(), scalePackage.get().getAppDescriptorId());
        queryInstantiationState(vnfInstanceResponseLinks.getSelf().getHref(),
                                VnfInstanceResponse.InstantiationStateEnum.INSTANTIATED.name(),
                                20,
                                3000);
        queryClusterName(vnfInstanceResponseLinks.getSelf().getHref(), "default", 20, 3000);
    }

    public static Optional<AppPackageResponse> getScaledPackage(final String testType) {
        if (testType.equals(REST)) {
            return getRestInstantiatePackage();
        }
        return getUiUpgradePackage();
    }

    public static void queryVnfcScaleInfo(final String appPkgId, final ConfigScale configScale, final String scaleType) {
        String queryParams = String
                .format("?aspectId=%s&type=%s&numberOfSteps=%s", configScale.getAspectId(), scaleType,
                        configScale.getNumberOfSteps());
        Optional<VnfcScaleInfo> vnfcScaleInfo = getVnfcScaleInfo(appPkgId, queryParams);
        assertThat(vnfcScaleInfo.isPresent()).withFailMessage("Vnfc scale info was not present in the response")
                .isTrue();
    }

    private static void verifyScaledPods(String configCluster, String timeout, String namespace, Map<String, String> expectedValues) {
        if (!CollectionUtils.isEmpty(expectedValues)) {
            expectedValues.forEach((key, value) -> {
                Map<String, String> labels = new HashMap<>();
                labels.put(POD_LABEL_NAME, key);
                verifyPodsCountByStatus(configCluster, timeout, namespace, RUNNING, labels, Integer.parseInt(value));
                verifyPodsCountByStatus(configCluster, timeout, namespace, TERMINATING, labels, 0);
            });
        }
    }
}
