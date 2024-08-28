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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.instantiate;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.InstantiateNegativeTestDataProviders.loadScaleRequestConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfSteps.performHealCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.executeInstantiateCnfOperationRequestAndVerifyResponse;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.InstantiateNegativeTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class InstantiateNegativeTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateNegativeTest.class);

    @Test(description = "EVNFM_LCM_instantiate_negative: Instantiate REST test negative",
            dataProvider = "getInstancesToInstantiate",
            dataProviderClass = InstantiateNegativeTestDataProviders.class,
            priority = 1)
    public void testInstantiateNegative(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("InstantiateNegativeTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());

        performCreateCnfIdentifierStep(cnfToInstantiate, user);

        final String operationLink = executeInstantiateCnfOperationRequestAndVerifyResponse(cnfToInstantiate, user);

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(cnfToInstantiate, user, operationLink, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, INSTANTIATE, cnfToInstantiate.getExpectedOperationState());

        final VnfInstanceLegacyResponse getVnfInstanceResponse =
                getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(), cnfToInstantiate.getVnfInstanceName(), user);
        assertThat(getVnfInstanceResponse.getInstantiationState()).isEqualTo(NOT_INSTANTIATED);

        cnfs.add(cnfToInstantiate);

        LOGGER.info("InstantiateNegativeTest : Instantiate a CNF : {} was completed with failure as expected", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_upgrade_negative: Upgrade REST test negative",
            dataProvider = "getInstancesToUpgrade",
            dataProviderClass = InstantiateNegativeTestDataProviders.class,
            priority = 2)
    public void testUpgradeNegative(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("InstantiateNegativeTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());

        performUpgradeCnfStepExpectingError(cnfToUpgrade, user);

        LOGGER.info("InstantiateNegativeTest : Upgrade a CNF : {} was completed with failure as expected", cnfToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_scale_negative: Scale REST test negative",
            dataProvider = "getInstancesToScale",
            dataProviderClass = InstantiateNegativeTestDataProviders.class,
            priority = 3)
    public void testScaleNegative(EvnfmCnf cnfToScale, ITestContext iTestContext) throws IOException {
        LOGGER.info("InstantiateNegativeTest : starts Scale a CNF : {}", cnfToScale.getVnfInstanceName());

        final ScaleVnfRequest scaleVnfRequest = loadScaleRequestConfigData(iTestContext);

        performScaleCnfStepExpectingError(cnfToScale, scaleVnfRequest, user);

        LOGGER.info("InstantiateNegativeTest : Scale a CNF : {} was completed with failure as expected", cnfToScale.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_heal_negative: Heal REST test negative",
            dataProvider = "getInstancesToHeal",
            dataProviderClass = InstantiateNegativeTestDataProviders.class,
            priority = 4)
    public void testHealNegative(EvnfmCnf cnfToHeal) {
        LOGGER.info("InstantiateNegativeTest : starts Heal a CNF : {}", cnfToHeal.getVnfInstanceName());

        performHealCnfStepExpectingError(cnfToHeal, user);

        LOGGER.info("InstantiateNegativeTest : Heal a CNF : {} was completed with failure as expected", cnfToHeal.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_terminate_negative: Terminate REST test negative",
            dataProvider = "getInstancesToTerminate",
            dataProviderClass = InstantiateNegativeTestDataProviders.class,
            priority = 5)
    public void testTerminateNegative(EvnfmCnf cnfToTerminate) {
        LOGGER.info("InstantiateNegativeTest : starts Terminate a CNF : {}", cnfToTerminate.getVnfInstanceName());

        performTerminateCnfStepExpectingError(cnfToTerminate, user);

        LOGGER.info("InstantiateNegativeTest : Terminate a CNF : {} was completed with failure as expected", cnfToTerminate.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("InstantiateNegativeTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("InstantiateNegativeTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("InstantiateNegativeTest : cleanup step completed successfully");
    }
}
