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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.evnfmreleaseflow.upgrade;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.EVNFMReleaseFlowDataProviders.loadUpgradeConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performCleanupCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateAndDeleteIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStep;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.EVNFMReleaseFlowDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.Base;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class UpgradeRestTest extends Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpgradeRestTest.class);

    @BeforeClass
    public void setUp(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeRestTest : starts setup step");

        LOGGER.info("UpgradeRestTest : load Instantiated data from Phase 1");
        List<EvnfmCnf> cnfFromPhase1List = loadUpgradeConfigData(iTestContext);

        for (EvnfmCnf cnfFromPhase1 : cnfFromPhase1List) {
            LOGGER.info("UpgradeRestTest : check that CNF {} from Phase 1 is exist", cnfFromPhase1.getVnfInstanceName());
            final VnfInstanceLegacyResponse vnfInstanceByRelease = getVNFInstanceByRelease(
                    EVNFM_INSTANCE.getEvnfmUrl(), cnfFromPhase1.getVnfInstanceName(), user);
            assertThat(vnfInstanceByRelease).isNotNull();
            cnfs.add(cnfFromPhase1);
            LOGGER.info("UpgradeRestTest : setup test was completed successfully");
        }
    }

    @Test(description = "Upgrade package", dataProviderClass = EVNFMReleaseFlowDataProviders.class,
            dataProvider = "loadUpgradeConfigDataProvider")
    public void upgradeCNF(EvnfmCnf cnfToUpgrade) {

        LOGGER.info("UpgradeRestTest : starts Upgrade a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performUpgradeCnfStep(cnfToUpgrade, user);
        LOGGER.info("UpgradeRestTest : Upgrade a CNF : {} completed successfully", cnfToUpgrade.getVnfInstanceName());

        LOGGER.info("UpgradeRestTest : starts Terminate a CNF : {}", cnfToUpgrade.getVnfInstanceName());
        performTerminateAndDeleteIdentifierStep(cnfToUpgrade, user);
        LOGGER.info("UpgradeRestTest : Terminate for CNF : {} was completed successfully", cnfToUpgrade.getVnfInstanceName());
    }

    @AfterClass
    public void terminateCNF(ITestContext iTestContext) throws IOException {
        LOGGER.info("UpgradeRestTest : starts shutdown step");
        List<EvnfmCnf> cnfToTerminateList = loadUpgradeConfigData(iTestContext);

        for (EvnfmCnf cnfToTerminate : cnfToTerminateList) {
            final VnfInstanceLegacyResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                           cnfToTerminate.getVnfInstanceName(), user);

            if (vnfInstanceByRelease != null) {
                if (Objects.equals(vnfInstanceByRelease.getInstantiationState(), VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED)) {
                    LOGGER.info("UpgradeRestTest : starts Cleanup of a failed CNF : {}", cnfToTerminate.getVnfInstanceName());
                    performCleanupCnfStep(cnfToTerminate, user);
                } else {
                    LOGGER.info("UpgradeRestTest : starts Terminate a CNF : {}", cnfToTerminate.getVnfInstanceName());
                    performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);
                    LOGGER.info("UpgradeRestTest : Terminate a CNF : {} was completed successfully", cnfToTerminate.getVnfInstanceName());
                }
            }
        }

        LOGGER.info("UpgradeRestTest : shutdown step completed successfully");
    }
}
