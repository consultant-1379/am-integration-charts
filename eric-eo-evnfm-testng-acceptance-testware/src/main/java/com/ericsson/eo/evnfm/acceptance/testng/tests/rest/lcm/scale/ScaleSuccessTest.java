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

package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.scale;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.PERSIST_SCALE_INFO_PARAM;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_FIRST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_SECOND;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.HELM_HISTORY_FOR_FIFTH_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.HELM_HISTORY_FOR_FIRST_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.HELM_HISTORY_FOR_FOURTH_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.HELM_HISTORY_FOR_SECOND_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.HELM_HISTORY_FOR_THIRD_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.SCALE_CONFIG_ASPECT_1_IN_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.SCALE_CONFIG_ASPECT_1_OUT_REQUEST_FIRST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.SCALE_CONFIG_ASPECT_1_OUT_REQUEST_SECOND;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.SCALE_CONFIG_ASPECT_2_IN_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.SCALE_CONFIG_ASPECT_2_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.loadAdditionalConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.loadHelmHistoryConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.loadScaleRequestConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.ScaleSuccessDataProviders.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithHelmHistoryData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithScaleData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.collectScaleInfo;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyScaleDataPersistenceAfterRollback;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfVerify.verifyScaleDataPersistenceAfterUpgrade;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performRollbackCnfAfterSuccessfulUpgradeStep;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleInfo;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class ScaleSuccessTest extends RestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScaleSuccessTest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("ScaleSuccessTest : starts instantiation step");

        LOGGER.info("ScaleSuccessTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("ScaleSuccessTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("ScaleSuccessTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : setup test was completed successfully");
    }

    @Test(description = "EVNFM_LCM_scale_success: scale OUT, scale IN and again scale OUT for aspect 1", priority = 1)
    public void testScaleOutInOutForAspect1(ITestContext iTestContext) throws IOException {
        LOGGER.info("ScaleSuccessTest : Testing scale 'out', 'in', 'out' for the Aspect 1");

        LOGGER.info("ScaleSuccessTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("ScaleSuccessTest : load Aspects and Scale Request Data");
        EvnfmCnf cnfScaleOutAspect1First = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_FIRST);
        EvnfmCnf cnfSaleInAspect1 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION);
        EvnfmCnf cnfScaleOutAspect1Second = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION_SECOND);
        ScaleVnfRequest cnfScaleOutRequestAspect1First = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_1_OUT_REQUEST_FIRST);
        ScaleVnfRequest cnfScaleInRequestAspect1 = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_1_IN_REQUEST);
        ScaleVnfRequest cnfScaleOutRequestAspect1Second = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_1_OUT_REQUEST_SECOND);
        EvnfmCnf helmHistoryAfterFirstScale = loadHelmHistoryConfigData(iTestContext, HELM_HISTORY_FOR_FIRST_SCALE_OPERATION);
        EvnfmCnf helmHistoryAfterSecondScale = loadHelmHistoryConfigData(iTestContext, HELM_HISTORY_FOR_SECOND_SCALE_OPERATION);
        EvnfmCnf helmHistoryAfterThirdScale = loadHelmHistoryConfigData(iTestContext, HELM_HISTORY_FOR_THIRD_SCALE_OPERATION);

        LOGGER.info("ScaleSuccessTest : perform 1st SCALE_OUT VNF-3GPP_5G-UE-AUTH^");
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfScaleOutAspect1First);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToInstantiate, helmHistoryAfterFirstScale);
        performScaleCnfStep(cnfToInstantiate, user, cnfScaleOutRequestAspect1First);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        verifyHelmHistory(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : perform SCALE_IN VNF-3GPP_5G-UE-AUTH^");
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfSaleInAspect1);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToInstantiate, helmHistoryAfterSecondScale);
        performScaleCnfStep(cnfToInstantiate, user, cnfScaleInRequestAspect1);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        verifyHelmHistory(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : perform 2nd SCALE_OUT VNF-3GPP_5G-UE-AUTH^");
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfScaleOutAspect1Second);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToInstantiate, helmHistoryAfterThirdScale);
        performScaleCnfStep(cnfToInstantiate, user, cnfScaleOutRequestAspect1Second);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        verifyHelmHistory(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : Scale Aspect 1 is successful");
    }

    @Test(description = "EVNFM_LCM_scale_success: Upgrade without persisting scale info, then Rollback",
            priority = 2, dependsOnMethods = "testScaleOutInOutForAspect1")
    public void testUpgradeWithoutPersistingScaleInfoAndRollback(ITestContext iTestContext) throws IOException {
        LOGGER.info("ScaleSuccessTest : Testing Upgrade with persistScaleInfo=false, then Rollback");

        LOGGER.info("ScaleSuccessTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("ScaleSuccessTest : Getting scale info BEFORE upgrade step");
        final List<ScaleInfo> scaleInfoBeforeUpgrade = collectScaleInfo(cnfToInstantiate, user);

        LOGGER.info("ScaleSuccessTest : Starts upgrade a CNF {}", cnfToInstantiate.getVnfInstanceName());

        LOGGER.info("ScaleSuccessTest : load upgrade data");
        EvnfmCnf cnfToUpgrade = loadUpgradeConfigData(iTestContext);
        performUpgradeCnfStep(cnfToUpgrade, user);
        LOGGER.info("ScaleSuccessTest : Upgrade step completed successfully for CNF {}", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("ScaleSuccessTest : Getting scale info AFTER upgrade step");
        final List<ScaleInfo> scaleInfoAfterUpgrade = collectScaleInfo(cnfToUpgrade, user);

        LOGGER.info("ScaleSuccessTest : Starts verification for CNF {}", cnfToUpgrade.getVnfInstanceName());
        boolean isPersistScaleInfo = Boolean.parseBoolean(cnfToUpgrade.getAdditionalParams().get(PERSIST_SCALE_INFO_PARAM).toString());
        verifyScaleDataPersistenceAfterUpgrade(scaleInfoBeforeUpgrade, scaleInfoAfterUpgrade, isPersistScaleInfo);
        LOGGER.info("ScaleSuccessTest : Verification completed successfully for CNF {}", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("ScaleSuccessTest : starts Rollback After Successful Upgrade a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performRollbackCnfAfterSuccessfulUpgradeStep(cnfToInstantiate, cnfToUpgrade.getVnfdId(), user);
        LOGGER.info("ScaleSuccessTest : Rollback After Successful Upgrade a CNF : {} completed successfully", cnfToInstantiate.getVnfInstanceName());

        LOGGER.info("ScaleSuccessTest : Getting scale info AFTER rollback step");
        final List<ScaleInfo> scaleInfoAfterRollback = collectScaleInfo(cnfToUpgrade, user);

        LOGGER.info("ScaleSuccessTest : Starts verification for CNF {}", cnfToUpgrade.getVnfInstanceName());
        verifyScaleDataPersistenceAfterRollback(scaleInfoBeforeUpgrade, scaleInfoAfterRollback);
        LOGGER.info("ScaleSuccessTest : Verification completed successfully for CNF {}", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("ScaleSuccessTest : Upgrade and Rollback is successful");
    }

    @Test(description = "EVNFM_LCM_scale_success: scale OUT, scale IN for aspect 2",
            priority = 3, dependsOnMethods = "testUpgradeWithoutPersistingScaleInfoAndRollback")
    public void testScaleOutInForAspect2(ITestContext iTestContext) throws IOException {
        LOGGER.info("ScaleSuccessTest : Testing scale 'out', 'in' for the Aspect 2");

        LOGGER.info("ScaleSuccessTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("ScaleSuccessTest : load Aspects and Scale Request Data");
        EvnfmCnf cnfScaleOutAspect2 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION);
        EvnfmCnf cnfSaleInAspect2 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION);
        ScaleVnfRequest cnfScaleOutRequestAspect2 = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_2_OUT_REQUEST);
        ScaleVnfRequest cnfScaleInRequestAspect2 = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_2_IN_REQUEST);
        EvnfmCnf helmHistoryAfterFourthScale = loadHelmHistoryConfigData(iTestContext, HELM_HISTORY_FOR_FOURTH_SCALE_OPERATION);
        EvnfmCnf helmHistoryAfterFifthScale = loadHelmHistoryConfigData(iTestContext, HELM_HISTORY_FOR_FIFTH_SCALE_OPERATION);

        LOGGER.info("ScaleSuccessTest : perform 1st SCALE_OUT UA-VNF-14PP#");
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfScaleOutAspect2);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToInstantiate, helmHistoryAfterFourthScale);
        performScaleCnfStep(cnfToInstantiate, user, cnfScaleOutRequestAspect2);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        verifyHelmHistory(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : perform SCALE_IN UA-VNF-14PP#");
        updateEvnfmCnfModelWithScaleData(cnfToInstantiate, cnfSaleInAspect2);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToInstantiate, helmHistoryAfterFifthScale);
        performScaleCnfStep(cnfToInstantiate, user, cnfScaleInRequestAspect2);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        verifyHelmHistory(cnfToInstantiate);

        LOGGER.info("ScaleSuccessTest : Scale Aspect 2 is successful");
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("ScaleSuccessTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("ScaleSuccessTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("ScaleSuccessTest : cleanup step completed successfully");
    }
}
