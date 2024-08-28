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
package com.ericsson.eo.evnfm.acceptance.testng.tests.ui.lcm.backup;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.BackupDataProviders.loadInstantiateConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps.verifyResourceInstantiated;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.UiBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.ResourceInfo;
import com.ericsson.evnfm.acceptance.steps.backup.ui.BackupResourceUISteps;
import com.ericsson.evnfm.acceptance.steps.common.ui.AuthenticationStep;
import com.ericsson.evnfm.acceptance.steps.instantiate.UI.InstantiateUISteps;
import com.ericsson.evnfm.acceptance.steps.terminate.UI.TerminateUISteps;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class BackupUITest extends UiBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(BackupUITest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("BackupUITest : starts setup step");

        LOGGER.info("BackupUITest : load instantiate data");
        EvnfmCnf cnfToInstantiateThroughUI = loadInstantiateConfigData(iTestContext);

        LOGGER.info("BackupUITest : starts Instantiate a CNF : {}", cnfToInstantiateThroughUI.getVnfInstanceName());
        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());

        InstantiateUISteps instantiateUISteps = new InstantiateUISteps();
        ResourceInfo resourceInfo = instantiateUISteps.instantiateResourceStepsUI(driver, cnfToInstantiateThroughUI);
        verifyResourceInstantiated(resourceInfo, cnfToInstantiateThroughUI);
        LOGGER.info("BackupUITest : Instantiate a CNF : {} was completed successfully", cnfToInstantiateThroughUI.getVnfInstanceName());

        cnfs.add(cnfToInstantiateThroughUI);
        LOGGER.info("BackupUITest : setup test was completed successfully");
    }

    @Test(description = "Backup UI Test")
    public void testBackupDeleteCNF(ITestContext iTestContext) throws IOException {
        LOGGER.info("BackupUITest : load modify data");
        EvnfmCnf cnfToInstantiateThroughUI = loadInstantiateConfigData(iTestContext);

        LOGGER.info("BackupUITest : starts Backup CNF operation for CNF : {}", cnfToInstantiateThroughUI.getVnfInstanceName());
        BackupResourceUISteps backupResourceUISteps = new BackupResourceUISteps();
        backupResourceUISteps.backupResourceStepsUI(driver, cnfToInstantiateThroughUI);

        LOGGER.info("BackupUITest : Backup CNF operation for CNF : {} was completed successfully",
                    cnfToInstantiateThroughUI.getVnfInstanceName());
    }

    @AfterClass
    public void shutdown(ITestContext iTestContext) throws IOException {
        LOGGER.info("BackupUITest : starts shutdown step");
        final EvnfmCnf evnfmCnf = loadInstantiateConfigData(iTestContext);
        final VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                 evnfmCnf.getVnfInstanceName(), user);

        if (vnfInstanceByRelease != null) {
            LOGGER.info("BackupUITest : starts Terminate a CNF : {}", evnfmCnf.getVnfInstanceName());
            TerminateUISteps terminateUISteps = new TerminateUISteps();
            terminateUISteps.terminateResourceStepsUI(driver, evnfmCnf);
            AuthenticationStep.logout(driver);
            LOGGER.info("BackupUITest : Terminate a CNF : {} was completed successfully", evnfmCnf.getVnfInstanceName());
        }

        LOGGER.info("BackupUITest : completed successfully");
    }
}
