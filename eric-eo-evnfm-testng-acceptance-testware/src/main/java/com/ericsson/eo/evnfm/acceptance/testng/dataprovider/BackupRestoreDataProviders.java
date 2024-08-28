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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_HEAL;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;

import com.ericsson.evnfm.acceptance.models.BackupRequest;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.fasterxml.jackson.core.type.TypeReference;

public class BackupRestoreDataProviders {
    private static final String CNF_HEAL_DATA_PARAMETER = "cnfHealData";
    private static final String DEFAULT_HEAL_DATA_PATH = "heal/backupRestore.yaml";
    private static final String BACKUP_CONFIG_DEFAULT_SCOPE = "backupRequest";

    @DataProvider(parallel = true)
    public static Object[] getInstancesToInstantiate(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        cnfsToInstantiate.forEach(TestExecutionGlobalConfig::addFlowSuffix);
        cnfsToInstantiate.forEach(cnfToInstantiate -> cnfToInstantiate.getAdditionalParams()
                .computeIfPresent("bro_endpoint_url",
                                  (key, value) -> value.toString().replace("<namespace>", cnfToInstantiate.getNamespace())));
        cnfsToInstantiate.forEach(cnfToInstantiate -> cnfToInstantiate.getAdditionalParams()
                .computeIfPresent("global.hosts.bro",
                                  (key, value) -> value.toString().replace("<namespace>", cnfToInstantiate.getNamespace())));

        return cnfsToInstantiate.toArray();
    }

    @DataProvider(parallel = true)
    public static Object[] getBackupConfigData(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<BackupRequest> backupDefaultRequest = loadYamlConfiguration(dataFilename, BACKUP_CONFIG_DEFAULT_SCOPE, new TypeReference<>() {
        });
        backupDefaultRequest.forEach(TestExecutionGlobalConfig::addFlowSuffix);

        return backupDefaultRequest.toArray();
    }

    @DataProvider(parallel = true)
    public Object[] getInstancesToHeal(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        List<EvnfmCnf> cnfsToHeal = loadYamlConfiguration(dataFilename, CNF_TO_HEAL, new TypeReference<>() {
        });
        cnfsToHeal.forEach(TestExecutionGlobalConfig::addFlowSuffix);

        return cnfsToHeal.toArray();
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CNF_HEAL_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_HEAL_DATA_PATH : dataFilename;
    }
}
