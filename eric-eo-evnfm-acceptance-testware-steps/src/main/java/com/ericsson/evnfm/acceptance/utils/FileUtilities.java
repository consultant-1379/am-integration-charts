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
package com.ericsson.evnfm.acceptance.utils;

import static java.util.Objects.requireNonNull;

import static org.assertj.core.api.Assertions.fail;
import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.utils.Constants.TEST_DATA_FOLDER_PROPERTY;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookupFactory;
import org.slf4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Strings;

public final class FileUtilities {
    private static final Logger LOGGER = getLogger(FileUtilities.class);

    public static final String CLUSTER_CONFIGS_PATH = "../testng/clusterConfigs";

    private FileUtilities() {
    }

    public static FileSystemResource getFileFromFileSystem(final String fileToLocate) {
        LOGGER.info("File to locate is {}", fileToLocate);
        File file = new File(fileToLocate);
        if (!file.exists()) {
            LOGGER.error("The file {} does not exist", file.getAbsolutePath());
        }
        if (!file.isFile()) {
            LOGGER.error("The file {} is not an actual file", file.getAbsolutePath());
        }
        LOGGER.info("Full path to file is: {}", file.getAbsolutePath());
        return new FileSystemResource(file);
    }

    public static String getFileContentFromInputStream(InputStream inputStream) {
        StringBuilder resultStringBuilder = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read file content", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close stream or buffer", e);
            }
        }

        return resultStringBuilder.toString();
    }

    public static <T> T loadYamlConfiguration(String dataFilename, Class<T> configClass) throws IOException {
        var contents = getConfigContent(dataFilename);
        return getYamlParser().loadAs(contents, configClass);
    }

    public static <T> T loadYamlConfiguration(String dataFilename, TypeReference<T> configClass) throws IOException {
        var contents = getConfigContent(dataFilename);
        return getObjectMapper().readValue(contents, configClass);
    }

    public static <T> T loadYamlConfiguration(String dataFilename, String key, Class<T> configClass) throws IOException {
        var contents = getConfigContent(dataFilename);
        final Map<String, Object> configMap = getYamlParser().load(contents);
        return getObjectMapper().convertValue(configMap.get(key), configClass);
    }

    public static <T> T loadYamlConfiguration(String dataFilename, String key, TypeReference<T> configClass) throws IOException {
        var contents = getConfigContent(dataFilename);
        final Map<String, Object> configMap = getYamlParser().load(contents);
        return getObjectMapper().convertValue(configMap.get(key), configClass);
    }

    public static String getConfigContent(final String dataFilename) throws IOException {
        return setEnvVariables(readTestDataResource(dataFilename));
    }

    private static String readTestDataResource(final String dataFilename) throws IOException {
        try (final InputStream inputStream = resolveTestDataResource(dataFilename)) {
            return new String(inputStream.readAllBytes());
        }
    }

    private static InputStream resolveTestDataResource(String fileName) throws IOException {
        if (Strings.isNullOrEmpty(System.getProperty(TEST_DATA_FOLDER_PROPERTY))) {
            return new ClassPathResource(Paths.get("testData", fileName).toString()).getInputStream();
        } else {
            return Files.newInputStream(Paths.get(System.getProperty(TEST_DATA_FOLDER_PROPERTY), fileName));
        }
    }

    public static String setEnvVariables(final String content) {
        // StringSubstitutor will replace any environment variable if they have the format in the file: ${ENV_VAR:-default}
        var stringSubstitutor = new StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup());
        stringSubstitutor.setEnableUndefinedVariableException(true);
        stringSubstitutor.setEnableSubstitutionInVariables(true);
        try {
            return stringSubstitutor.replace(content);
        } catch (IllegalArgumentException e) {
            fail(String.format("Failure when reading values from %s :: %s", content, e.getMessage()));
        }
        return "";
    }

    public static Optional<String> getKubeConfigFromResources(final String clusterName) {
        File[] clusterConfigList = new File(CLUSTER_CONFIGS_PATH).listFiles();
        Map<String, String> configFiles = Arrays.stream(requireNonNull(clusterConfigList))
                .collect(Collectors.toMap(File::getName, File::getAbsolutePath));
        return Optional.ofNullable(configFiles.get(clusterName.toLowerCase()));
    }
}
