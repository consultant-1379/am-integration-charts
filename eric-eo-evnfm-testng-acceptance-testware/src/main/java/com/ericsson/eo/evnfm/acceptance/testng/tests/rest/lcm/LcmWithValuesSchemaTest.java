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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.pollingVnfLcmOperationOccurrence;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyResultOfVnfLcmOppOcc;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.executeInstantiateCnfOperationRequestAndVerifyResponse;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED;
import static com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc.OperationEnum.INSTANTIATE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.LcmWithValuesSchemaTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;

public class LcmWithValuesSchemaTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LcmWithValuesSchemaTest.class);

    @Test(description = "EVNFM_LCM_values_schema: Instantiate REST test with values schema positive",
            dataProvider = "getInstancesToInstantiatePositive",
            dataProviderClass = LcmWithValuesSchemaTestDataProviders.class)
    public void testInstantiatePositive(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("LcmWithValuesSchemaTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("LcmWithValuesSchemaTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_values_schema: Instantiate REST test with values schema negative",
            dataProvider = "getInstancesToInstantiateNegative",
            dataProviderClass = LcmWithValuesSchemaTestDataProviders.class)
    public void testInstantiateNegative(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("LcmWithValuesSchemaTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());

        performCreateCnfIdentifierStep(cnfToInstantiate, user);

        final String operationLink = executeInstantiateCnfOperationRequestAndVerifyResponse(cnfToInstantiate, user);

        final ResponseEntity<VnfLcmOpOcc> vnfLcmOpOccResponseEntity = pollingVnfLcmOperationOccurrence(cnfToInstantiate, user, operationLink, null);
        verifyResultOfVnfLcmOppOcc(vnfLcmOpOccResponseEntity, INSTANTIATE, cnfToInstantiate.getExpectedOperationState());

        final VnfInstanceLegacyResponse getVnfInstanceResponse =
                getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(), cnfToInstantiate.getVnfInstanceName(), user);
        assertThat(getVnfInstanceResponse.getInstantiationState()).isEqualTo(NOT_INSTANTIATED);

        cnfs.add(cnfToInstantiate);

        LOGGER.info("LcmWithValuesSchemaTest : Instantiate a CNF : {} was completed with failure as expected",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("LcmWithValuesSchemaTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("LcmWithValuesSchemaTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("LcmWithValuesSchemaTest : cleanup step completed successfully");
    }
}
