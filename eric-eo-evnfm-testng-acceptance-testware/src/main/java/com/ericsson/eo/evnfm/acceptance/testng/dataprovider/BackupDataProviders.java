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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.IOException;
import java.util.Objects;

import org.testng.ITestContext;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class BackupDataProviders {
    private static final String BACKUP_UI_DATA_PARAMETER = "backupData";
    private static final String DEFAULT_BACKUP_UI_DATA_PATH = "backup/backup.yaml";

    public static EvnfmCnf loadInstantiateConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);

        addFlowSuffix(cnfToInstantiate);
        cnfToInstantiate.getAdditionalParams()
                .computeIfPresent("bro_endpoint_url",
                                  (key, value) -> value.toString().replace("<namespace>", cnfToInstantiate.getNamespace()));
        return cnfToInstantiate;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(BACKUP_UI_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_BACKUP_UI_DATA_PATH : dataFilename;
    }

    public static BackupRequest loadBackupRequestConfigData(ITestContext iTestContext, String key) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, key, BackupRequest.class);
    }
}
