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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.lcm.terminate;

import static com.ericsson.eo.evnfm.acceptance.testng.infrastructure.ClusterUtils.checkNameSpaceExist;
import static com.ericsson.eo.evnfm.acceptance.testng.infrastructure.ClusterUtils.cleanupResources;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateAndDeleteIdentifierStep;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.TerminatePositiveDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class TerminatePositiveTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TerminatePositiveTest.class);

    @Test(description = "Instantiate csars",
            dataProvider = "getInstancesToInstantiate", dataProviderClass = TerminatePositiveDataProviders.class,
            priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) throws IOException {
        LOGGER.info("TerminatePositiveTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        LOGGER.info("TerminatePositiveTest : Instantiate a CNF : {} was completed successfully", cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_LCM_Terminate: Terminate a CNF",
            dataProvider = "getInstancesToTerminate", dataProviderClass = TerminatePositiveDataProviders.class,
            priority = 2)
    public void testTerminate(EvnfmCnf cnfToTerminate) {
        if (cnfToTerminate.isCleanUpResources()) {
            LOGGER.info("TerminatePositiveTest : starts manually cleanup resources for CNF {}", cnfToTerminate.getVnfInstanceName());
            cleanupResources(cnfToTerminate.getCluster().getLocalPath(), cnfToTerminate.getNamespace());

            LOGGER.info("TerminatePositiveTest : starts Terminate request for CNF {}", cnfToTerminate.getVnfInstanceName());
            performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);
            LOGGER.info("TerminatePositiveTest : successfully completed Terminate for CNF {}", cnfToTerminate.getVnfInstanceName());
        } else {
            LOGGER.info("TerminatePositiveTest : starts Terminate request for CNF {}", cnfToTerminate.getVnfInstanceName());
            performTerminateAndDeleteIdentifierStep(cnfToTerminate, user);

            checkNameSpaceExist(cnfToTerminate);
            cleanupResources(cnfToTerminate.getCluster().getLocalPath(), cnfToTerminate.getNamespace());
            LOGGER.info("TerminatePositiveTest : successfully completed Terminate for CNF {}", cnfToTerminate.getVnfInstanceName());
        }
    }
}
