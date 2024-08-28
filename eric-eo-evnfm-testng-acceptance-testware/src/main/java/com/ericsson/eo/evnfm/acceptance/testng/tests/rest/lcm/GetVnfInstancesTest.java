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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.VnfInstancesDataProviders.loadClusterConfig;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.VnfInstancesDataProviders.loadOnboardedPackages;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.collectCnfInstanceLinksIfNeed;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.getVnfInstanceByLink;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.QueryInstanceSteps.queryAllLcmOccurrences;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.QueryInstanceSteps.queryAllLcmOperationsPageByPage;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.QueryInstanceSteps.queryAllVNFInstances;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.QueryInstanceSteps.queryAllVNFInstancesPageByPage;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.VnfInstanceVerify.verifyLegacyCnfInstanceReturnedInResponse;
import static com.ericsson.evnfm.acceptance.steps.vnfInstance.VnfInstanceVerify.verifyRel4CnfInstanceResponse;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.VnfInstancesDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;

public class GetVnfInstancesTest extends RestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetVnfInstancesTest.class);

    @Test(description = "Instantiate legacy and rel4 csars",
            dataProvider = "loadInstantiateConfigData",
            dataProviderClass = VnfInstancesDataProviders.class, priority = 1)
    public void testInstantiate(EvnfmCnf cnfToInstantiate) throws IOException {
        LOGGER.info("GetVnfInstancesTest : starts Instantiate a CNF : {}", cnfToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(cnfToInstantiate, user);
        cnfs.add(cnfToInstantiate);
        LOGGER.info("GetVnfInstancesTest : Instantiate a CNF : {} was completed successfully",
                    cnfToInstantiate.getVnfInstanceName());
    }

    @Test(description = "Get vnfInstance by instanceId for rel.4 and legacy Csar", dataProvider = "loadInstantiateConfigData",
            dataProviderClass = VnfInstancesDataProviders.class,
            priority = 2)
    public void testGetVnfInstanceByIdREST(EvnfmCnf cnfToQuery) {
        collectCnfInstanceLinksIfNeed(cnfToQuery, user);

        if (cnfToQuery.getVnfInstanceName().contains("rel4")) {

            LOGGER.info("GetVnfInstancesTest : starts getting rel4 CNF instance : {}", cnfToQuery.getVnfInstanceName());
            VnfInstanceResponse vnfInstanceResponse = getVnfInstanceByLink(cnfToQuery, user);
            verifyRel4CnfInstanceResponse(vnfInstanceResponse, cnfToQuery.getCluster().getLocalPath(), cnfToQuery.getNamespace());
            LOGGER.info("GetVnfInstancesTest : getting rel4 CNF instance was completed : {}", cnfToQuery.getVnfInstanceName());

        } else if (cnfToQuery.getVnfInstanceName().contains("legacy")) {

            LOGGER.info("GetVnfInstancesTest : starts getting legacy CNF instance : {}", cnfToQuery.getVnfInstanceName());
            VnfInstanceResponse vnfInstanceResponse = getVnfInstanceByLink(cnfToQuery, user);
            verifyLegacyCnfInstanceReturnedInResponse(vnfInstanceResponse, cnfToQuery.getCluster().getLocalPath());
            LOGGER.info("GetVnfInstancesTest : getting legacy CNF instance was completed : {}", cnfToQuery.getVnfInstanceName());

        }
    }

    @Test(description = "Query Vnf Instances, LCM operations and resources for rel4 and legacy Csars", priority = 3)
    public void testQueryREST(ITestContext iTestContext) throws IOException {
        List<EvnfmBasePackage> evnfmPackages = loadOnboardedPackages(iTestContext);
        ClusterConfig clusterConfig = loadClusterConfig(iTestContext);

        LOGGER.info("GetVnfInstancesTest : starts getting CNF instances page by page");
        queryAllVNFInstancesPageByPage(user, evnfmPackages, clusterConfig.getLocalPath());

        LOGGER.info("GetVnfInstancesTest : starts getting LCM operations page by page");
        queryAllLcmOperationsPageByPage(user);

        LOGGER.info("GetVnfInstancesTest : starts getting all CNF instances with max page size: 100");
        queryAllVNFInstances(EVNFM_INSTANCE.getEvnfmUrl(), user, "size=100");

        LOGGER.info("GetVnfInstancesTest : starts getting all LCM operations with max page size: 100");
        queryAllLcmOccurrences(EVNFM_INSTANCE.getEvnfmUrl(), user, "size=100");

        LOGGER.info("GetVnfInstancesTest : testQueryREST is completed");
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("GetVnfInstancesTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("GetVnfInstancesTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("GetVnfInstancesTest : cleanup step completed successfully");
    }
}