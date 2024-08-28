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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithScaleData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVnfInstanceByLink;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExpectedHttpError;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyExtensionsAreEqual;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfApiClient.executeScaleCnfOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyScaleCnfResponseSuccess;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyScaleInfo;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG;
import static com.ericsson.evnfm.acceptance.utils.HelmUtils.runCommand;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.SCALE;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.ManualUpgrade;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigScale;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class ScaleCnfSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleCnfSteps.class);

    private ScaleCnfSteps() {
    }

    public static void performScaleCnfStep(EvnfmCnf evnfmCnfToScale, User user, final ConfigScale configScale,
                                           ScaleVnfRequest.TypeEnum scaleType,
                                           String aspectId) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToScale, user);

        final ResponseEntity<Void> response = executeScaleCnfOperationRequest(evnfmCnfToScale, user, configScale, scaleType, aspectId);
        executeCommonScaleCnfStepByRequestModelType(evnfmCnfToScale, user, response);
    }

    public static void performScaleCnfStep(EvnfmCnf evnfmCnfToScale, User user, ScaleVnfRequest request) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToScale, user);

        final ResponseEntity<Void> response = executeScaleCnfOperationRequest(evnfmCnfToScale, user, request);
        executeCommonScaleCnfStepByRequestModelType(evnfmCnfToScale, user, response);
    }

    public static void performScaleCnfStepExpectingFailure(EvnfmCnf evnfmCnfToScale, User user, ScaleVnfRequest request) {
        collectCnfInstanceLinksIfNeed(evnfmCnfToScale, user);

        final ResponseEntity<Void> response = executeScaleCnfOperationRequest(evnfmCnfToScale, user, request);
        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = checkResponseAndWaitForOperationCompletion(evnfmCnfToScale, user, response);

        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, SCALE, evnfmCnfToScale.getExpectedOperationState());
    }

    private static void executeCommonScaleCnfStepByRequestModelType(final EvnfmCnf evnfmCnfToScale,
                                                                    final User user,
                                                                    final ResponseEntity<Void> response) {

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = checkResponseAndWaitForOperationCompletion(evnfmCnfToScale, user, response);

        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, SCALE);

        final VnfInstanceResponse actualVnfInstance = getVnfInstanceByLink(evnfmCnfToScale.getVnfInstanceResponseLinks().getSelf().getHref(), user);

        verifyExtensionsAreEqual(evnfmCnfToScale, actualVnfInstance);
        verifyScaleInfo(evnfmCnfToScale, actualVnfInstance);
    }

    private static ResponseEntity<VnfLcmOpOcc> checkResponseAndWaitForOperationCompletion(final EvnfmCnf evnfmCnfToScale,
                                                                            final User user,
                                                                            final ResponseEntity<Void> response) {
        verifyScaleCnfResponseSuccess(response);
        final String operationLink = response.getHeaders().get(HttpHeaders.LOCATION).get(0);
        LOGGER.info(VNF_LCM_OPP_OCC_QUERY_FROM_RESPONSE_LOG, operationLink);

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(evnfmCnfToScale,
                                                                                                       user,
                                                                                                       operationLink,
                                                                                                       null);
        return vnfLcmOpOccResponseEntity;
    }

    public static void performScaleCnfUITestStep(EvnfmCnf evnfmCnfToScale, VnfInstanceLegacyResponse actualVnfInstance) {
        verifyExtensionsAreEqual(evnfmCnfToScale, actualVnfInstance);
        verifyScaleInfo(evnfmCnfToScale, actualVnfInstance);
    }

    public static void executeUpgradeReleaseForScaleCnfStep(EvnfmCnf cnaToScale) {
        copyConfigToWfsPod(cnaToScale);
        for (ManualUpgrade manualUpgrade : cnaToScale.getManualUpgrade()) {
            String chart = EVNFM_INSTANCE.getHelmRegistryUrl() + "/onboarded/charts/" + manualUpgrade.getChartName();
            String values = manualUpgrade.getValues().stream().map(value -> "--set " + value).collect(Collectors.joining(" "));
            String helmUpgradeCommand = String.format("helm upgrade %s %s -n %s --reuse-values %s --kubeconfig %s --wait --debug", manualUpgrade.getReleaseName(), chart, cnaToScale.getNamespace(),
                    values, "/tmp/" + cnaToScale.getCluster().getName());
            String upgradeCommandWithinWfsPod = String.format("kubectl --kubeconfig %s -n %s exec -i"
                            + " $(kubectl --kubeconfig %s -n %s get pods | grep eric-am-common-wfs | awk 'NR==1{print $1}')"
                            + " --"
                            + " /bin/bash -c '%s'", cnaToScale.getCluster().getLocalPath(), EVNFM_INSTANCE.getNamespace(), cnaToScale.getCluster().getLocalPath(),
                    EVNFM_INSTANCE.getNamespace(),
                    helmUpgradeCommand);
            runCommand(upgradeCommandWithinWfsPod, 60, true);
        }
    }

    private static void copyConfigToWfsPod(EvnfmCnf cnaToScale) {
        String clusterLocalPath = cnaToScale.getCluster().getLocalPath();
        String evnfmNamespace = EVNFM_INSTANCE.getNamespace();

        String findWfsPodNameCommand = String.format(
                "kubectl --kubeconfig %s -n %s get pods | grep eric-am-common-wfs | awk 'NR==1{print $1}'",
                clusterLocalPath,
                evnfmNamespace);
        String copyCommand = String.format(
                "kubectl --kubeconfig %s -n %s cp %s $(%s):/tmp/%s",
                clusterLocalPath,
                evnfmNamespace,
                clusterLocalPath,
                findWfsPodNameCommand,
                cnaToScale.getCluster().getName());
        runCommand(copyCommand, 60, true);
    }

    public static void performScaleOutOperation(final EvnfmCnf cnfToInstantiate, final EvnfmCnf cnfAspect1, final ScaleVnfRequest aspect1Request,
                                                User user) {
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfAspect1);
        performScaleCnfStep(cnfToInstantiate, user, aspect1Request);
    }

    public static void performScaleCnfStepExpectingError(EvnfmCnf evnfmCnfToScale,
                                                         ScaleVnfRequest scaleVnfRequest,
                                                         User user) {

        try {
            performScaleCnfStep(evnfmCnfToScale, user, scaleVnfRequest);
        } catch (HttpClientErrorException exception) {
            verifyExpectedHttpError(exception, evnfmCnfToScale.getExpectedError());
        }
    }
}
