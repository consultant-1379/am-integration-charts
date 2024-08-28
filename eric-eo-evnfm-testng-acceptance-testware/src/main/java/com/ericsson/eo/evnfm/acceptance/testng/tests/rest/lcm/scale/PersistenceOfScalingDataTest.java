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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_3_OUT_OPERATION;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.PERSIST_SCALE_INFO_PARAM;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.SCALE_CONFIG_ASPECT_1_OUT;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.SCALE_CONFIG_ASPECT_2_OUT;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.SCALE_CONFIG_ASPECT_3_OUT;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.loadAdditionalConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.loadInstantiateConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.loadRollbackConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.loadScaleRequestConfigData;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.PersistenceOfScalingDataProvider.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonVerifications.verifyNumberOfTargets;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.scale.rest.ScaleCnfSteps.performScaleOutOperation;
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

public class PersistenceOfScalingDataTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceOfScalingDataTest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("PersistenceOfScalingDataTest : starts setup step");

        LOGGER.info("PersistenceOfScalingDataTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("PersistenceOfScalingDataTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("PersistenceOfScalingDataTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());

        cnfs.add(cnfToInstantiate);

        LOGGER.info("PersistenceOfScalingDataTest : setup test was completed successfully");
    }

    @Test(description = "performing scale for different aspects to check scale info persistence after upgrade")
    public void endToEndRestTestPersistenceOfScalingData(ITestContext iTestContext) throws IOException {
        LOGGER.info("PersistenceOfScalingDataTest : Testing persistence of scaling");

        LOGGER.info("PersistenceOfScalingDataTest : load instantiate data");
        EvnfmCnf cnfToInstantiate = loadInstantiateConfigData(iTestContext);

        LOGGER.info("PersistenceOfScalingDataTest : load Aspects and ScaleRequest Data");
        EvnfmCnf cnfAspect1 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_1_OUT_OPERATION);
        EvnfmCnf cnfAspect2 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_2_OUT_OPERATION);
        EvnfmCnf cnfAspect3 = loadAdditionalConfigData(iTestContext, ADDITIONAL_CNF_DATA_FOR_SCALE_ASPECT_3_OUT_OPERATION);
        ScaleVnfRequest aspect1Request = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_1_OUT);
        ScaleVnfRequest aspect2Request = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_2_OUT);
        ScaleVnfRequest aspect3Request = loadScaleRequestConfigData(iTestContext, SCALE_CONFIG_ASPECT_3_OUT);

        LOGGER.info("PersistenceOfScalingDataTest : Starts execute 1st SCALE_OUT operation");
        performScaleOutOperation(cnfToInstantiate, cnfAspect1, aspect1Request, user);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        LOGGER.info("PersistenceOfScalingDataTest : 1st SCALE_OUT operation was completed successfully");

        LOGGER.info("PersistenceOfScalingDataTest : Starts execute 2nd SCALE_OUT operation");
        performScaleOutOperation(cnfToInstantiate, cnfAspect2, aspect2Request, user);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        LOGGER.info("PersistenceOfScalingDataTest : 2nd SCALE_OUT operation was completed successfully");

        LOGGER.info("PersistenceOfScalingDataTest : Starts execute 3rd SCALE_OUT operation");
        performScaleOutOperation(cnfToInstantiate, cnfAspect3, aspect3Request, user);
        verifyNumberOfTargets(cnfToInstantiate, 60);
        LOGGER.info("PersistenceOfScalingDataTest : 3rd SCALE_OUT operation was completed successfully");

        LOGGER.info("PersistenceOfScalingDataTest : Successful scale, getting scale info BEFORE upgrade step");
        final List<ScaleInfo> scaleInfoBeforeUpgrade = collectScaleInfo(cnfToInstantiate, user);

        LOGGER.info("PersistenceOfScalingDataTest : load upgrade data");
        EvnfmCnf upgradeCnf = loadUpgradeConfigData(iTestContext);
        LOGGER.info("PersistenceOfScalingDataTest : Perform upgrade step on instance {}", upgradeCnf.getVnfInstanceName());
        performUpgradeCnfStep(upgradeCnf, user);
        LOGGER.info("PersistenceOfScalingDataTest : Successful upgrade of {}", upgradeCnf.getVnfInstanceName());

        LOGGER.info("PersistenceOfScalingDataTest : Successful scale, getting scale info after upgrade step");
        final List<ScaleInfo> scaleInfoAfterUpgrade = collectScaleInfo(upgradeCnf, user);
        boolean isPersistScaleInfo = Boolean.parseBoolean(upgradeCnf.getAdditionalParams().get(PERSIST_SCALE_INFO_PARAM).toString());

        verifyScaleDataPersistenceAfterUpgrade(scaleInfoBeforeUpgrade, scaleInfoAfterUpgrade, isPersistScaleInfo);
        LOGGER.info("PersistenceOfScalingDataTest : test was completed successfully");

        LOGGER.info("PersistenceOfScalingDataTest : load rollback data");
        EvnfmCnf rollbackCnf = loadRollbackConfigData(iTestContext);
        LOGGER.info("PersistenceOfScalingDataTest : Perform rollback step on instance {}", rollbackCnf.getVnfInstanceName());
        performRollbackCnfAfterSuccessfulUpgradeStep(rollbackCnf, rollbackCnf.getSourceVnfdId(), user);
        LOGGER.info("PersistenceOfScalingDataTest : Successful rollback of {}", rollbackCnf.getVnfInstanceName());

        LOGGER.info("PersistenceOfScalingDataTest : Successful scale, getting scale info after rollback step");
        final List<ScaleInfo> scaleInfoAfterRollback = collectScaleInfo(rollbackCnf, user);

        verifyScaleDataPersistenceAfterRollback(scaleInfoAfterUpgrade, scaleInfoAfterRollback);
        LOGGER.info("PersistenceOfScalingDataTest : test was completed successfully");
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("PersistenceOfScalingDataTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("PersistenceOfScalingDataTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("PersistenceOfScalingDataTest : cleanup step completed successfully");
    }
}
