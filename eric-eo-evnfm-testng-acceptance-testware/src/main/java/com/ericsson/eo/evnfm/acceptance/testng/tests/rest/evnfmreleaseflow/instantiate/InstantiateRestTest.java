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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.evnfmreleaseflow.instantiate;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.EVNFMReleaseFlowDataProviders.loadInstantiateConfigData;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.executeCreateVnfIdentifierExpectingError;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateAndDeleteIdentifierStep;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.EVNFMReleaseFlowDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.Base;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.JsonProcessingException;

public class InstantiateRestTest extends Base {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateRestTest.class);

    @Test(description = "Instantiate package", dataProviderClass = EVNFMReleaseFlowDataProviders.class,
            dataProvider = "loadInstantiateConfigDataProvider")
    public void instantiateCNF(EvnfmCnf cnfToInstantiate) {
        LOGGER.info("InstantiateRestTest : load instantiate data");
        LOGGER.info("InstantiateRestTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("InstantiateRestTest : Instantiate for CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
        cnfs.add(cnfToInstantiate);
    }

    @Test
    public void terminateCNF(ITestContext iTestContext) throws IOException {

        LOGGER.info("InstantiateRestTest : starts shutdown step");
        List<EvnfmCnf> cnfToTerminateList = loadInstantiateConfigData(iTestContext);

        for (EvnfmCnf cnfToTerminate : cnfToTerminateList) {
            final VnfInstanceLegacyResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(),
                                                                                           cnfToTerminate.getVnfInstanceName(), user);
            if (vnfInstanceByRelease != null) {
                LOGGER.info("InstantiateRestTest : starts Terminate a CNF : {}", cnfToTerminate.getVnfInstanceName());
                performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);
                LOGGER.info("InstantiateRestTest : Terminate a CNF : {} was completed successfully", cnfToTerminate.getVnfInstanceName());
            }
        }

        LOGGER.info("InstantiateRestTest : shutdown step completed successfully");
    }

    @Test(description = "Instantiate package", dataProviderClass = EVNFMReleaseFlowDataProviders.class,
            dataProvider = "loadInstantiateConfigDataProviderExpectingError")
    public void instantiateCnfWithoutDomainRoles(EvnfmCnf cnfToInstantiate, ProblemDetails expectedError) throws JsonProcessingException {
        LOGGER.info("InstantiateRestTest : Create CNF Identifier without Domain Role");
        executeCreateVnfIdentifierExpectingError(cnfToInstantiate, expectedError, user);
        LOGGER.info("InstantiateRestTest : Create CNF Identifier is forbidden - User is not authorized");
    }
}