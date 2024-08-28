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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.TCDataProviders.loadPackagesToOnboard;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateApiClient.executeQueryVnfIdentifierBySelfLinkOperationRequest;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.getVnfInstanceByLink;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.onboardPackageIfNotPresent;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performDeleteCnfIdentifierStep;
import static com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse.InstantiationStateEnum.NOT_INSTANTIATED;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.TCDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class TCLcm extends RestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TCLcm.class);

    @BeforeClass
    public void uploadPackagesIfNotPresent(ITestContext iTestContext) throws IOException {
        loadPackagesToOnboard(iTestContext).forEach(p -> onboardPackageIfNotPresent(p, user, false));
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_7/version/0.1
     */
    @Test(description = "EVNFM_LCM_7 : Create a VNF Identifier over Or-VNFM",
            dataProvider = "getInstantiateConfig",
            dataProviderClass = TCDataProviders.class)
    public void testCreateVnfIdentifier(EvnfmCnf cnf) {
    }

    /**
     * TC descriptions:
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_1/version/1.3
     */
    @Test(description = "EVNFM_LCM_1 : Instantiate a CNF over Or-VNFM",
            dataProvider = "getInstantiateConfig",
            dataProviderClass = TCDataProviders.class)
    public void EVNFM_LCM_1_testInstantiate(EvnfmCnf cnf) {
        LOGGER.info("EVNFM_LCM_1_testInstantiate : starts Instantiate a CNF : {}", cnf.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnf, user);
        cnfs.add(cnf);
        LOGGER.info("EVNFM_LCM_1_testInstantiate : Instantiate a CNF : {} was completed successfully", cnf.getVnfInstanceName());
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_2/version/0.1
     */
    @Test(description = "EVNFM_LCM_2 : Query a CNF over Or-VNFM",
            dataProvider = "getInstantiatedCnfs",
            dataProviderClass = TCDataProviders.class)
    public void testQueryCnf(EvnfmCnf cnf) {
    }

    /**
     * TC descriptions:
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_3/version/1.1
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_11/version/1.1
     */
    @Test(description = "EVNFM_LCM_3 : Upgrade a CNF from an onboarded package over Or-VNFM\n" +
            "EVNFM_LCM_11 : Upgrade a CNF using custom values.yaml via Or-VNFM",
            dataProvider = "getUpgradeConfig",
            dataProviderClass = TCDataProviders.class)
    public void testUpgrade(EvnfmCnf cnf) {
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_10/version/0.1
     */
    @Test(description = "EVNFM_LCM_10 : Query a LCM operation via Or-VNFM",
            dataProvider = "getUpgradedCnfs",
            dataProviderClass = TCDataProviders.class)
    public void testQueryLcmOperation(EvnfmCnf cnf) {
    }

    /**
     * TC descriptions:
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_12/version/1.2
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_13/version/1.2
     */
    @Test(description = "EVNFM_LCM_12 : EVNFM manual scale out of CNF\n" +
            "EVNFM_LCM_13 : EVNFM manual scale in of CNF",
            dataProvider = "getScaleConfig",
            dataProviderClass = TCDataProviders.class)
    public void testScale(EvnfmCnf cnf) {
    }

    /**
     * TC descriptions:
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_4/version/1.1
     * https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_5/version/1.1
     */
    @Test(description = "EVNFM_LCM_4 : Terminate an Instantiated CNF over Or-VNFM\n" +
            "EVNFM_LCM_5 : Terminate an Instantiated CNF over Or-VNFM in a separate cluster",
            dataProvider = "getTerminateConfig",
            dataProviderClass = TCDataProviders.class)
    public void testTerminate(EvnfmCnf cnf) {
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_8/version/0.1
     */
    @Test(description = "EVNFM_LCM_8 : Delete a VNF Identifier over Or-VNFM",
            dataProvider = "getInstantiatedCnfs",
            dataProviderClass = TCDataProviders.class)
    public void testDeleteVnfIdentifier(EvnfmCnf cnf) {
        performCreateCnfIdentifierStep(cnf, user);

        VnfInstanceResponse instance = getVnfInstanceByLink(cnf, user);
        assertThat(instance.getInstantiationState()).isEqualTo(NOT_INSTANTIATED);
        performDeleteCnfIdentifierStep(cnf, user);

        assertThatThrownBy(() -> executeQueryVnfIdentifierBySelfLinkOperationRequest(cnf, user))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("404");
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_LCM_16/version/1.4
     */
    @Test(description = "EVNFM_LCM_16 : Cleanup failed instantiate via Or-VNFM",
            dataProvider = "getCleanupConfig",
            dataProviderClass = TCDataProviders.class)
    public void testCleanupCnf(EvnfmCnf cnf) {
    }
}

