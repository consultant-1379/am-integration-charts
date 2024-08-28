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

import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CLUSTER_CONFIGS;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_INSTANTIATE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.CNF_TO_UPGRADE;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.EXPECTED_ERRORS;
import static com.ericsson.eo.evnfm.acceptance.testng.dataprovider.Constants.PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.FileUtilities.loadYamlConfiguration;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParserWithPrettyDumper;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.addFlowSuffix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.testng.ITestContext;
import org.testng.annotations.DataProvider;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig;
import com.ericsson.vnfm.orchestrator.model.ProblemDetails;
import com.fasterxml.jackson.core.type.TypeReference;

public class CISMClustersDataProviders {
    public static final String DESCRIPTION_LENGTH_EXCEEDED = "descriptionLengthExceeded";
    public static final String SECOND_CLUSTER_CONFIG_NAME = "testConfig.config";
    private static final Yaml YAML;
    private static final String TEST_TARGET_CLUSTER_DATA_PARAMETER = "testTargetClusterData";
    private static final String DEFAULT_TEST_TARGET_CLUSTER_PATH = "release-single/cismCluster/testTargetCluster.yaml";
    private static final String CISM_CLUSTER_DATA_PARAMETER = "cismClusterData";
    private static final String DEFAULT_CISM_CLUSTER_PATH = "release-multi/cismCluster/cismCluster.yaml";
    private static final String TEST_TARGET_CONFIG = "testTargetConfig";
    private static final String VALID_CONFIG = "validConfig";
    private static final String INVALID_CONFIG = "invalidConfig";
    private static final String CONFIG_WITH_EXISTING_NAMESPACE = "configWithExistingNamespace";
    private static final String NOT_VALID = "notValid";
    private static final String ALREADY_EXISTS = "alreadyExists";
    private static final String DEFAULT_CONFIG_DEREGISTRATION = "defaultConfigDeregistration";
    private static final String NOT_EXISTING_NAMESPACE = "notExistingNamespace";
    private static final String CONFIG_NOT_FOUND = "notFoundConfig";
    private static final String CONFIG_NOT_EXIST = "notExistConfig";
    private static final String REQUIRED_NAMESPACES_MISSING = "requiredNamespacesMissing";
    private static final String NOT_VALID_UPDATE = "notValid(update)";
    private static final String NOT_VALID_PATCH = "notValid(patch)";
    private static final String INVALID_CLUSTER_HOST = "invalid.ericsson.se";
    private static final String NAMESPACES_IS_FORBIDDEN = "namespaces is forbidden:";
    private static final String INVALID_CLUSTER_SERVER = String.format("https://%s/k8s/clusters/c-lx48g", INVALID_CLUSTER_HOST);
    private static final String INVALID_USER_TOKEN = "";
    private static final TypeReference<Map<String, ClusterConfig>> CLUSTER_CONFIG_TYPE_REF = new TypeReference<>() {
    };

    static {
        YAML = getYamlParserWithPrettyDumper();
    }

    public static List<EvnfmBasePackage> loadPackagesToOnboard(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        return loadYamlConfiguration(dataFilename, PACKAGES, new TypeReference<>() {
        });
    }

    public static Map<String, ClusterConfig> loadClusterConfigs(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> configsMap = loadYamlConfiguration(dataFilename, CLUSTER_CONFIGS, CLUSTER_CONFIG_TYPE_REF);
        configsMap.values().forEach(TestExecutionGlobalConfig::addFlowSuffix);
        ClusterConfig validClusterConfig = configsMap.get(VALID_CONFIG);
        configsMap.get(INVALID_CONFIG).setLocalPath(generateInvalidClusterConfig(validClusterConfig.getLocalPath()));
        ClusterConfig configWithExistingNamespace = configsMap.get(CONFIG_WITH_EXISTING_NAMESPACE);
        configWithExistingNamespace.setLocalPath(
                generateClusterConfigWithNamespace(validClusterConfig.getLocalPath(),
                                                   configWithExistingNamespace.getNamespace(), CONFIG_WITH_EXISTING_NAMESPACE));
        return configsMap;
    }

    private static String resolveConfig(ITestContext iTestContext) {
        String dataFilename = iTestContext.getCurrentXmlTest().getParameter(CISM_CLUSTER_DATA_PARAMETER);
        return Objects.isNull(dataFilename) ? DEFAULT_CISM_CLUSTER_PATH : dataFilename;
    }

    private static String generateClusterConfigWithNamespace(String validClusterConfigPath, String namespace, String testName) {
        try {
            Map<String, Object> configData = YAML.load(Files.newInputStream(Paths.get(validClusterConfigPath)));

            setNamespaceToContext(configData, namespace);

            String invalidClusterConfigPath = getPathToGeneratedClusterConfig(validClusterConfigPath, testName);
            YAML.dump(configData, new FileWriter(invalidClusterConfigPath));

            return invalidClusterConfigPath;
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "Failed to generate cluster config with namespace from %s", validClusterConfigPath), e);
        }
    }

    private static String generateInvalidClusterConfig(String validClusterConfigPath) {
        try {
            Map<String, Object> configData = YAML.load(Files.newInputStream(Paths.get(validClusterConfigPath)));

            setInvalidClusterServer(configData);
            setInvalidUserToken(configData);

            String invalidClusterConfigPath = getPathToGeneratedClusterConfig(validClusterConfigPath, INVALID_CONFIG);
            YAML.dump(configData, new FileWriter(invalidClusterConfigPath));

            return invalidClusterConfigPath;
        } catch (Exception e) {
            throw new RuntimeException(String.format(
                    "Failed to generate invalid cluster config from %s", validClusterConfigPath), e);
        }
    }

    private static void setNamespaceToContext(Map<String, Object> configData, String namespace) {
        List<Map<String, Object>> clusters = (List<Map<String, Object>>) configData.get("contexts");
        for (Map<String, Object> contexts : clusters) {
            Map<String, Object> clusterMap = (Map<String, Object>) contexts.get("context");
            clusterMap.put("namespace", namespace);
        }
    }

    private static void setInvalidClusterServer(Map<String, Object> configData) {
        List<Map<String, Object>> clusters = (List<Map<String, Object>>) configData.get("clusters");
        for (Map<String, Object> cluster : clusters) {
            Map<String, Object> clusterMap = (Map<String, Object>) cluster.get("cluster");
            clusterMap.put("server", INVALID_CLUSTER_SERVER);
        }
    }

    private static void setInvalidUserToken(Map<String, Object> configData) {
        List<Map<String, Object>> users = (List<Map<String, Object>>) configData.get("users");
        for (Map<String, Object> user : users) {
            Map<String, Object> userMap = (Map<String, Object>) user.get("user");
            userMap.put("token", INVALID_USER_TOKEN);
        }
    }

    private static String getPathToGeneratedClusterConfig(String validClusterConfigPath, String suffix) {
        Path validConfigPath = Paths.get(validClusterConfigPath);
        Path validConfigFilename = validConfigPath.getFileName();
        Path validConfigParent = validConfigPath.getParent();
        String invalidConfigFilename = String.format("%s%s", suffix, validConfigFilename.toString());
        if (validConfigParent == null) {
            return invalidConfigFilename;
        } else {
            return Paths.get(validConfigParent.toString(), invalidConfigFilename).toString();
        }
    }

    public static ClusterConfig loadInitialClusterConfig(ITestContext iTestContext) throws IOException {
        String dataFilenameFromConfig = iTestContext.getCurrentXmlTest().getParameter(TEST_TARGET_CLUSTER_DATA_PARAMETER);
        String dataFilename = Objects.isNull(dataFilenameFromConfig) ? DEFAULT_TEST_TARGET_CLUSTER_PATH : dataFilenameFromConfig;

        Map<String, ClusterConfig> clusterConfigs = loadYamlConfiguration(dataFilename, CLUSTER_CONFIGS, CLUSTER_CONFIG_TYPE_REF);

        return clusterConfigs.get(TEST_TARGET_CONFIG);
    }

    @DataProvider
    public Object[] clusterConfigTestDataTarget(ITestContext iTestContext) throws IOException {
        return new Object[] { loadInitialClusterConfig(iTestContext) };
    }

    @DataProvider
    public Object[] clusterConfigTestDataRegister(ITestContext iTestContext) throws IOException {
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        return new Object[] { clusterConfigs.get(VALID_CONFIG) };
    }

    @DataProvider
    public Object[][] clusterConfigDataDuplicateNegative(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        ClusterConfig duplicateClusterConfig = clusterConfigs.get(VALID_CONFIG);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(ALREADY_EXISTS);
        expectedError.setDetail(String.format(expectedError.getDetail(), duplicateClusterConfig.getName()));
        return new Object[][] { { duplicateClusterConfig, expectedError } };
    }

    @DataProvider
    public Object[][] clusterConfigDataInvalidNegative(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        ClusterConfig invalidClusterConfig = clusterConfigs.get(INVALID_CONFIG);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(NOT_VALID);
        expectedError.setDetail(String.format(expectedError.getDetail(), INVALID_CLUSTER_HOST));
        return new Object[][] { { invalidClusterConfig, expectedError } };
    }

    @DataProvider
    public Object[] clusterConfigWithExistingNamespace(ITestContext iTestContext) throws IOException {
        return new Object[] { loadClusterConfigs(iTestContext).get(CONFIG_WITH_EXISTING_NAMESPACE) };
    }

    @DataProvider
    public Object[][] clusterConfigTestDataLCM(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        List<EvnfmCnf> packagesInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        EvnfmCnf packageUpgrade = loadYamlConfiguration(dataFilename, CNF_TO_UPGRADE, EvnfmCnf.class);
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), addFlowSuffix(packagesInstantiate.get(0)), addFlowSuffix(packageUpgrade) } };
    }

    @DataProvider
    public Object[][] clusterConfigNegativeInvalidClusterTestDataLCM(ITestContext iTestContext) throws IOException {
        String clusterName = "unregistered";
        String dataFilename = resolveConfig(iTestContext);
        EvnfmCnf cnfToInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, EvnfmCnf.class);
        var unregisteredCluster = new ClusterConfig();
        unregisteredCluster.setName(clusterName);
        cnfToInstantiate.setCluster(unregisteredCluster);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(CONFIG_NOT_FOUND);
        expectedError.setDetail(String.format(expectedError.getDetail(), clusterName));
        return new Object[][] { { addFlowSuffix(cnfToInstantiate), expectedError } };
    }

    @DataProvider
    public Object[][] clusterConfigNegativeOperationFailedTestDataLCM(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        List<EvnfmCnf> packagesInstantiate = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), addFlowSuffix(packagesInstantiate.get(1)) } };
    }

    @DataProvider
    public Object[] clusterUpdateConfigDataInvalidNegative(ITestContext iTestContext) throws IOException {
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(NOT_VALID_UPDATE);
        expectedError.setDetail(String.format(expectedError.getDetail(), INVALID_CLUSTER_HOST));
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), clusterConfigs.get(INVALID_CONFIG), expectedError } };
    }

    @DataProvider
    public Object[] clusterModifyConfigDataInvalidNegative(ITestContext iTestContext) throws IOException {
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        String dataFilename = resolveConfig(iTestContext);

        String clusterConfigPartialInvalid = "{" +
                " \"description\": \"Modified description\",\n" +
                " \"isDefault\": false,\n" +
                " \"clusterConfig\": {\n" +
                " \"users\": [{\n" +
                " \"name\": \"user1\"\n" +
                "  }]\n" +
                " }" +
                "}";

        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(NOT_VALID_PATCH);
        expectedError.setDetail(String.format(expectedError.getDetail(), NAMESPACES_IS_FORBIDDEN));
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), clusterConfigPartialInvalid, expectedError } };
    }

    @DataProvider
    public Object[] clusterUpdateConfigDataNotExistNegative(ITestContext iTestContext) throws IOException {
        final String clusterName = "unregistered";
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(CONFIG_NOT_EXIST);
        expectedError.setDetail(String.format(expectedError.getDetail(), clusterName));
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), expectedError, clusterName } };
    }

    @DataProvider
    public Object[][] clusterUpdateConfigDataNotSkipVerificationPositive(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        final List<EvnfmCnf> cnfs = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        EvnfmCnf packageInstantiate = cnfs.get(0);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(REQUIRED_NAMESPACES_MISSING);
        expectedError.setDetail(String.format(expectedError.getDetail(), packageInstantiate.getNamespace()));
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), addFlowSuffix(packageInstantiate), expectedError } };
    }

    @DataProvider
    public Object[][] clusterModifyConfigTooLongDescriptionNegative(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        final List<EvnfmCnf> cnfs = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        EvnfmCnf packageInstantiate = cnfs.get(0);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(DESCRIPTION_LENGTH_EXCEEDED);
        expectedError.setDetail(String.format(expectedError.getDetail(), packageInstantiate.getNamespace()));

        String patchFields = "{ \n"
                + "\t\"description\" : \"Invalid cluster configuration description: The description of a cluster configuration cannot be more than "
                + "two hundred fifty ( 250 ) characters long. If it is more than two hundred fifty( 250 ) characters, it will be a BAD REQUEST to "
                + "the web service and this modification will be rejected.So, please do not try to modify the description of your cluster "
                + "configuration with a description having more than two hundred fifty ( 250 ) characters\",\n"
                + "\t\"isDefault\"\t  : false\n"
                + "}";

        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), expectedError, patchFields } };
    }

    @DataProvider
    public Object[][] clusterModifyConfigDataNotSkipVerificationPositive(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        final List<EvnfmCnf> cnfs = loadYamlConfiguration(dataFilename, CNF_TO_INSTANTIATE, new TypeReference<>() {
        });
        EvnfmCnf packageInstantiate = cnfs.get(0);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(REQUIRED_NAMESPACES_MISSING);
        expectedError.setDetail(String.format(expectedError.getDetail(), packageInstantiate.getNamespace()));

        String patchFields = "{ \n"
                + "\t\"description\" : \"modified Cluster description\",\n"
                + "\t\"isDefault\"\t  : false\n"
                + "}";

        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), addFlowSuffix(packageInstantiate), expectedError, patchFields } };
    }

    @DataProvider
    public Object[] clusterModifyConfigDataNotExistNegative(ITestContext iTestContext) throws IOException {
        final String clusterName = "unregistered";
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });
        ProblemDetails expectedError = expectedErrors.get(CONFIG_NOT_EXIST);
        expectedError.setDetail(String.format(expectedError.getDetail(), clusterName));

        String patchFields = "{ \n"
                + "\"description\" : \"modified Cluster description\"\n"
                + "}";
        return new Object[][] { { clusterConfigs.get(VALID_CONFIG), expectedError, clusterName, patchFields } };
    }

    @DataProvider
    public Object[][] clusterConfigDataSecondCluster(ITestContext iTestContext) throws IOException {
        String dataFilename = resolveConfig(iTestContext);
        Map<String, ClusterConfig> clusterConfigs = loadClusterConfigs(iTestContext);
        ClusterConfig secondClusterConfig = clusterConfigs.get(VALID_CONFIG);
        Map<String, ProblemDetails> expectedErrors = loadYamlConfiguration(dataFilename, EXPECTED_ERRORS, new TypeReference<>() {
        });

        ProblemDetails expectedError = expectedErrors.get(DEFAULT_CONFIG_DEREGISTRATION);
        expectedError.setDetail(String.format(expectedError.getDetail(), secondClusterConfig.getName()));

        ClusterConfig defaultConfig = loadInitialClusterConfig(iTestContext);
        return new Object[][] { { secondClusterConfig, expectedError, defaultConfig } };
    }

    @DataProvider
    public Object[] clusterConfigDataGenerateNewConfig(ITestContext iTestContext) throws IOException {
        ClusterConfig clusterConfig = loadInitialClusterConfig(iTestContext);
        File newFile = new File(clusterConfig.getLocalPath());
        String newClusterConfigContent = Files.readString(newFile.toPath());
        String pwd = newFile.getParent();
        String newFileName = pwd + File.separator + SECOND_CLUSTER_CONFIG_NAME;
        Files.write(Path.of(newFileName), newClusterConfigContent.getBytes(StandardCharsets.UTF_8));
        clusterConfig.setLocalPath(newFileName);
        clusterConfig.setDescription("Cluster config that will be used as target for tests");
        return new Object[][] { { clusterConfig } };
    }
}
