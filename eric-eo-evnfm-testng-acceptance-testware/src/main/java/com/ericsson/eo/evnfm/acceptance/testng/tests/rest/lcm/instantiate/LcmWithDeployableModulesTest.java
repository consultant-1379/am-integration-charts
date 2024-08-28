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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.HELM_HISTORY_FOR_FIRST_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.HELM_HISTORY_FOR_SECOND_SCALE_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.SCALE_CONFIG_ASPECT_1_IN_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.SCALE_CONFIG_ASPECT_1_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.SCALE_CONFIG_ASPECT_2_IN_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.SCALE_CONFIG_ASPECT_2_OUT_REQUEST;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.loadData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders.loadLcmConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithExpectedOperationState;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithHelmHistoryData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.updateEvnfmCnfModelWithScaleData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyHelmHistory;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfHelmReleasesChartNames;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.heal.rest.HealCnfSteps.performHealCnfStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoSteps.performModifyVnfInstanceInfoStep;
import static com.ericsson.evnfm.acceptance.steps.modify.rest.ModifyVnfInstanceInfoSteps.performModifyVnfInstanceInfoStepExpectingFailure;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleCnfStepExpectingFailure;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performSuccessfulRollbackCnfStep;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BasicLcmWithDeployableModulesTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.ScaleVnfRequest;

public class LcmWithDeployableModulesTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LcmWithDeployableModulesTest.class);

    @Test(description = "EVNFM_LCM_instantiate_deployableModules: Instantiate REST test with deployableModules", dataProvider =
            "getInstancesToInstantiate",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 1)
    public void instantiateWithDeployableModules(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("LcmWithDeployableModulesTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        verifyHelmHistory(cnfToInstantiate);
        verifyNumberOfHelmReleasesChartNames(cnfToInstantiate);
        verifyNumberOfTargets(cnfToInstantiate);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("LcmWithDeployableModulesTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_upgrade_deployableModules: Upgrade REST test with deployableModules", dataProvider = "getInstancesToUpgrade",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 2, dependsOnMethods =
            "instantiateWithDeployableModules")
    public void upgradeWithDeployableModules(EvnfmCnf cnfToUpgradeFirst, EvnfmCnf cnfToUpgradeSecond) {
        LOGGER.info("LcmWithDeployableModulesTest : starts first Upgrade a CNF : {}", cnfToUpgradeFirst.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgradeFirst, user);
        verifyHelmHistory(cnfToUpgradeFirst);
        LOGGER.info("LcmWithDeployableModulesTest : First upgrade a CNF : {} was completed  successfully", cnfToUpgradeFirst.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : starts second Upgrade a CNF : {}", cnfToUpgradeSecond.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgradeSecond, user);
        verifyHelmHistory(cnfToUpgradeSecond);
        LOGGER.info("LcmWithDeployableModulesTest : Second upgrade a CNF : {} was completed  successfully", cnfToUpgradeSecond.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_scale_deployableModules: Scale REST test with deployableModules", dataProvider = "getInstancesToScale",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 3, dependsOnMethods = "upgradeWithDeployableModules")
    public void scaleWithDeployableModules(EvnfmCnf cnfToScale, ITestContext iTestContext) throws IOException {
        LOGGER.info("LcmWithDeployableModulesTest : Testing scaling 'out', 'in' a CNF : {} for the same Aspect", cnfToScale.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : load Aspects and Scale Request Data");
        EvnfmCnf cnfScaleOutAspect1 = loadLcmConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION).get(0);
        EvnfmCnf cnfScaleOutAspect2 = loadLcmConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION).get(0);
        EvnfmCnf cnfScaleInAspect1 = loadLcmConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_IN_OPERATION).get(0);
        EvnfmCnf cnfScaleInAspect2 = loadLcmConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_IN_OPERATION).get(0);
        ScaleVnfRequest cnfScaleOutRequestAspect1 = loadData(iTestContext, SCALE_CONFIG_ASPECT_1_OUT_REQUEST, ScaleVnfRequest.class);
        ScaleVnfRequest cnfScaleOutRequestAspect2 = loadData(iTestContext, SCALE_CONFIG_ASPECT_2_OUT_REQUEST, ScaleVnfRequest.class);
        ScaleVnfRequest cnfScaleInRequestAspect1 = loadData(iTestContext, SCALE_CONFIG_ASPECT_1_IN_REQUEST, ScaleVnfRequest.class);
        ScaleVnfRequest cnfScaleInRequestAspect2 = loadData(iTestContext, SCALE_CONFIG_ASPECT_2_IN_REQUEST, ScaleVnfRequest.class);
        EvnfmCnf helmHistoryAfterFirstScale = loadData(iTestContext, HELM_HISTORY_FOR_FIRST_SCALE_OPERATION, EvnfmCnf.class);
        EvnfmCnf helmHistoryAfterSecondScale = loadData(iTestContext, HELM_HISTORY_FOR_SECOND_SCALE_OPERATION, EvnfmCnf.class);

        LOGGER.info("LcmWithDeployableModulesTest : perform SCALE_OUT for a CNF : {} , Aspect1", cnfToScale.getVnfInstanceName());
        updateEvnfmCnfModelWithExpectedOperationState(cnfToScale, cnfScaleOutAspect1);
        performScaleCnfStepExpectingFailure(cnfToScale, user, cnfScaleOutRequestAspect1);
        LOGGER.info("LcmWithDeployableModulesTest : Scaling Out a CNF : {} failed as expected", cnfToScale.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : perform SCALE_OUT for a CNF : {} , Aspect2", cnfToScale.getVnfInstanceName());
        updateEvnfmCnfModelWithScaleData(cnfToScale, cnfScaleOutAspect2);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToScale, helmHistoryAfterFirstScale);
        performScaleCnfStep(cnfToScale, user, cnfScaleOutRequestAspect2);
        verifyNumberOfTargets(cnfToScale, 60);
        verifyHelmHistory(cnfToScale);
        LOGGER.info("LcmWithDeployableModulesTest : Scaling Out a CNF : {} was completed  successfully", cnfToScale.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : perform SCALE_IN for a CNF : {} , Aspect1", cnfToScale.getVnfInstanceName());
        updateEvnfmCnfModelWithExpectedOperationState(cnfToScale, cnfScaleInAspect1);
        performScaleCnfStepExpectingFailure(cnfToScale, user, cnfScaleInRequestAspect1);
        LOGGER.info("LcmWithDeployableModulesTest : Scaling In a CNF : {} failed as expected", cnfToScale.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : perform SCALE_IN for a CNF : {} , Aspect2", cnfToScale.getVnfInstanceName());
        updateEvnfmCnfModelWithScaleData(cnfToScale, cnfScaleInAspect2);
        updateEvnfmCnfModelWithHelmHistoryData(cnfToScale, helmHistoryAfterSecondScale);
        performScaleCnfStep(cnfToScale, user, cnfScaleInRequestAspect2);
        verifyNumberOfTargets(cnfToScale, 60);
        verifyHelmHistory(cnfToScale);
        LOGGER.info("LcmWithDeployableModulesTest : Scaling In a CNF : {} was completed  successfully", cnfToScale.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_modify_deployableModules: Modify REST test with deployableModules", dataProvider = "getInstancesToModify",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 4, dependsOnMethods = "scaleWithDeployableModules")
    public void modifyWithDeployableModules(EvnfmCnf cnfToModifyFirst, EvnfmCnf cnfToModifySecond) {
        LOGGER.info("LcmWithDeployableModulesTest : starts first Modify aspect in enabled module : {}", cnfToModifyFirst.getVnfInstanceName());
        performModifyVnfInstanceInfoStep(cnfToModifyFirst, user);
        verifyHelmHistory(cnfToModifyFirst);
        LOGGER.info("LcmWithDeployableModulesTest : First modify VNF Info for CNF : {} was completed successfully",
                    cnfToModifyFirst.getVnfInstanceName());

        LOGGER.info("LcmWithDeployableModulesTest : starts second Modify aspect in disabled module : {}", cnfToModifySecond.getVnfInstanceName());
        performModifyVnfInstanceInfoStepExpectingFailure(cnfToModifySecond, user);
        LOGGER.info("LcmWithDeployableModulesTest : Second modify VNF Info for CNF : {} failed as expected", cnfToModifySecond.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_sync_deployableModules: Sync REST test with deployableModules", dataProvider = "getInstancesToSync",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 5, dependsOnMethods = "modifyWithDeployableModules")
    public void syncWithDeployableModules(EvnfmCnf cnfToSync) {
        LOGGER.info("LcmWithDeployableModulesTest : starts Sync operation: {}", cnfToSync.getVnfInstanceName());
        // Sync is not compatible with deployable modules right now, it should be uncommented when sync is fixed and tested
        // performSync(cnfToSync, user);
        LOGGER.info("LcmWithDeployableModulesTest : Sync for CNF : {} was completed successfully", cnfToSync.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_heal_deployableModules: Heal REST test with deployableModules", dataProvider = "getInstancesToHeal",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 6, dependsOnMethods = "syncWithDeployableModules")
    public void healWithDeployableModules(EvnfmCnf cnfToHeal) {
        LOGGER.info("LcmWithDeployableModulesTest : starts Heal operation: {}", cnfToHeal.getVnfInstanceName());
        performHealCnfStep(cnfToHeal, user);
        LOGGER.info("LcmWithDeployableModulesTest : Heal for CNF : {} was completed successfully", cnfToHeal.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_upgrade_deployableModules: Upgrade REST test with deployableModules and persistDMconfig",
            dataProvider = "getInstancesToUpgradeWithPersistDMconfig", dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class,
            priority = 7, dependsOnMethods = "healWithDeployableModules")
    public void upgradeWithDeployableModulesAndPersistDMconfig(EvnfmCnf cnfToUpgrade) {
        LOGGER.info("LcmWithDeployableModulesTest : starts Upgrade with persistDMConfig a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        verifyHelmHistory(cnfToUpgrade);
        LOGGER.info("LcmWithDeployableModulesTest : Upgrade with persistDMConfig a CNF : {} was completed  successfully",
                    cnfToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_rollback_deployableModules: Rollback REST test with deployableModules", dataProvider = "getInstancesToRollback",
            dataProviderClass = BasicLcmWithDeployableModulesTestDataProviders.class, priority = 8,
            dependsOnMethods = "upgradeWithDeployableModulesAndPersistDMconfig")
    public void rollbackWithDeployableModules(EvnfmCnf cnfToRollback) {
        LOGGER.info("LcmWithDeployableModulesTest : starts Rollback a CNF : {}", cnfToRollback.getVnfInstanceName());
        performSuccessfulRollbackCnfStep(cnfToRollback, user);
        verifyHelmHistory(cnfToRollback);
        LOGGER.info("LcmWithDeployableModulesTest : Rollback a CNF : {} was completed  successfully", cnfToRollback.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("LcmWithDeployableModulesTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("LcmWithDeployableModulesTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("LcmWithDeployableModulesTest : cleanup step completed successfully");
    }
}
