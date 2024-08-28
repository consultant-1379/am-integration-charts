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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.executeForEachInParallel;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVnfInstanceByLink;
import static com.ericsson.evnfm.acceptance.steps.drac.rest.DracSteps.addNodeTypeToRoleInDracConfig;
import static com.ericsson.evnfm.acceptance.steps.drac.rest.DracSteps.removeNodeTypeFromRoleInDracConfig;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateCnfIdentifierStepExpectingError;
import static com.ericsson.evnfm.acceptance.steps.instantiate.rest.InstantiateCnfSteps.performCreateIdentifierAndInstantiateCnfStep;
import static com.ericsson.evnfm.acceptance.steps.terminate.rest.TerminateCnfSteps.performTerminateOrCleanupCnfStepIfNecessary;
import static com.ericsson.evnfm.acceptance.steps.upgrade.rest.UpgradeCnfSteps.performUpgradeCnfStepExpectingError;
import static com.ericsson.evnfm.acceptance.utils.Constants.EVNFM_BASIC_DOMAIN_ROLE;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.DracAccessTestDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class DracAccessTest extends RestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(DracAccessTest.class);

    @Test(description = "EVNFM_DRAC: Instantiate REST test basic-app negative",
            dataProvider = "getBasicAppToInstantiateWithClusterConfig",
            dataProviderClass = DracAccessTestDataProviders.class,
            priority = 1)
    public void testInstantiateNotAuthorized(EvnfmCnf basicAppToInstantiate, ClusterConfig clusterConfig) throws InterruptedException {
        LOGGER.info("DracAccessTest : Remove node type of CNF : {} from DRAC role {}",
                    basicAppToInstantiate.getVnfInstanceName(),
                    EVNFM_BASIC_DOMAIN_ROLE);
        removeNodeTypeFromRoleInDracConfig(basicAppToInstantiate,
                                           EVNFM_BASIC_DOMAIN_ROLE,
                                           clusterConfig.getLocalPath(),
                                           EVNFM_INSTANCE.getNamespace());
        TimeUnit.SECONDS.sleep(10);
        LOGGER.info("DracAccessTest : Remove node type of CNF : {} from DRAC role {} was completed successfully",
                    basicAppToInstantiate.getVnfInstanceName(),
                    EVNFM_BASIC_DOMAIN_ROLE);

        LOGGER.info("DracAccessTest : starts Create identifier for CNF : {}", basicAppToInstantiate.getVnfInstanceName());
        performCreateCnfIdentifierStepExpectingError(basicAppToInstantiate, user);
        LOGGER.info("DracAccessTest : Create identifier for CNF : {} was completed with failure as expected",
                    basicAppToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_DRAC: Instantiate REST test spider-app positive",
            dataProvider = "getSpiderAppToInstantiate",
            dataProviderClass = DracAccessTestDataProviders.class,
            priority = 2)
    public void testInstantiateSuccessful(EvnfmCnf spiderAppToInstantiate) {
        LOGGER.info("DracAccessTest : starts Instantiate a CNF : {}", spiderAppToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(spiderAppToInstantiate, user);
        cnfs.add(spiderAppToInstantiate);
        LOGGER.info("DracAccessTest : Instantiate a CNF : {} was completed successfully", spiderAppToInstantiate.getVnfInstanceName());
    }

    @Test(description = "EVNFM_DRAC: Upgrade REST test basic-app negative",
            dataProvider = "getBasicAppToUpgrade",
            dataProviderClass = DracAccessTestDataProviders.class,
            priority = 3)
    public void testUpgradeNotAuthorized(EvnfmCnf basicAppToUpgrade) {
        LOGGER.info("DracAccessTest : starts Upgrade a CNF : {}", basicAppToUpgrade.getVnfInstanceName());
        performUpgradeCnfStepExpectingError(basicAppToUpgrade, user);
        LOGGER.info("DracAccessTest : Upgrade a CNF : {} was completed with failure as expected", basicAppToUpgrade.getVnfInstanceName());
    }

    @Test(description = "EVNFM_DRAC: Instantiate REST test basic-app positive",
            dataProvider = "getBasicAppToInstantiateWithClusterConfig",
            dataProviderClass = DracAccessTestDataProviders.class,
            priority = 4)
    public void testAddNodeTypeAndInstantiate(EvnfmCnf basicAppToInstantiate, ClusterConfig clusterConfig) throws InterruptedException {
        LOGGER.info("DracAccessTest : Add node type of CNF : {} to DRAC role {}",
                    basicAppToInstantiate.getVnfInstanceName(),
                    EVNFM_BASIC_DOMAIN_ROLE);
        addNodeTypeToRoleInDracConfig(basicAppToInstantiate, EVNFM_BASIC_DOMAIN_ROLE, clusterConfig.getLocalPath(), EVNFM_INSTANCE.getNamespace());
        TimeUnit.SECONDS.sleep(10);
        LOGGER.info("DracAccessTest : Add node type of CNF : {} to DRAC role {} was completed successfully",
                    basicAppToInstantiate.getVnfInstanceName(),
                    EVNFM_BASIC_DOMAIN_ROLE);

        LOGGER.info("DracAccessTest : starts Instantiate a CNF : {}", basicAppToInstantiate.getVnfInstanceName());
        performCreateIdentifierAndInstantiateCnfStep(basicAppToInstantiate, user);
        cnfs.add(basicAppToInstantiate);
        LOGGER.info("DracAccessTest : Instantiate a CNF : {} was completed successfully", basicAppToInstantiate.getVnfInstanceName());

        LOGGER.info("DracAccessTest : starts Get a CNF : {}", basicAppToInstantiate.getVnfInstanceName());
        getVnfInstanceByLink(basicAppToInstantiate.getVnfInstanceResponseLinks().getSelf().getHref(), user);
        LOGGER.info("DracAccessTest : Get a CNF : {} was completed successfully", basicAppToInstantiate.getVnfInstanceName());
    }

    @AfterClass
    public void cleanupAfterTest(ITestContext iTestContext) {
        LOGGER.info("DracAccessTest : starts cleanup step");

        if (!isTestSuitePassedSuccessfully(iTestContext)) {
            LOGGER.info("DracAccessTest : Test case is failed, CNFs will be saved");
            return;
        }

        executeForEachInParallel(cnfs, cnfToTerminate -> performTerminateOrCleanupCnfStepIfNecessary(cnfToTerminate, user));

        LOGGER.info("DracAccessTest : cleanup step completed successfully");
    }
}
