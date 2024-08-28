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
package com.ericsson.eo.evnfm.acceptance.testng.dataprovider;

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CLUSTER_CONFIG;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;

public class BackupRestoreOrchestratorDataProvider {

    private static final String BRO_DATA_PARAMETER = "backupRestoreOrchestrator";
    private static final String DEFAULT_BRO_DATA_PATH = "release-multi/bro/bro.yaml";

    private static ClusterConfig loadClusterConfig(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, CLUSTER_CONFIG, ClusterConfig.class);
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BRO_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BRO_DATA_PATH : dataFilename;
    }

    @DataProvider
    public Object[] testAllBackupRestoreAgentsRegisteredData(ITestContext iTestContext) throws IOException {
        return new Object[] { loadClusterConfig(iTestContext) };
    }
}
