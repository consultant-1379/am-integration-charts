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
package com.ericsson.evnfm.acceptance.steps.common.rest;

import static java.util.Objects.requireNonNull;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getVNFInstanceByRelease;
import static com.ericsson.evnfm.acceptance.utils.Constants.APPLICATION_TIME_OUT;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLEAN_UP_RESOURCES;
import static com.ericsson.evnfm.acceptance.utils.Constants.SKIP_VERIFICATION;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNF_CONTROLLED_SCALING;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getCsarDowloadingFromARMRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.AssertionsForClassTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.RetryCallback;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.configuration.Day0SecretVerificationInfo;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonHelper.class);

    private CommonHelper() {
    }

    /**
     * Create file out of map content and return file resource
     *
     * @param valuesMap content of values.yaml
     * @return the file as a FileSystemResource
     */
    public static FileSystemResource getFileResource(final Map<String, Object> valuesMap) {
        Path tempPathToValues = writeStringToValuesFile(valuesMap);
        LOGGER.info("Values file path is {}", tempPathToValues.toAbsolutePath());
        return new FileSystemResource(tempPathToValues);
    }

    private static Path writeStringToValuesFile(final Map<String, Object> values) {
        Path valuesFile = createTempPath("values", "yaml");
        String valuesString = convertMapToYamlFormat(values);
        try {
            Files.write(valuesFile, valuesString.getBytes(StandardCharsets.UTF_8));
            return valuesFile;
        } catch (IOException e) {
            throw new RuntimeException("Unable to store values file", e);
        }
    }

    private static Path createTempPath(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, "." + suffix);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create temporary file", e);
        }
    }

    public static String convertMapToYamlFormat(final Map<String, Object> values) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setWidth(Integer.MAX_VALUE);
        Yaml yaml = new Yaml(options);
        return yaml.dump(values);
    }

    /**
     * First checks the uri, if it is a local path, test uses the file in local path
     * if URI is remote address,
     * Downloads the file from specified URI and saves it to downloadPath
     * If file exists with the same name overrides it.
     *
     * @param evnfmBasePackage Evnfm base package model
     * @param downloadPath     the path of the file will be saved
     * @return the file as a FileSystemResource
     */
    public static FileSystemResource getCsar(final EvnfmBasePackage evnfmBasePackage, String downloadPath) {
        String packageName = evnfmBasePackage.getPackageName();
        FileSystemResource csar = null;
        try {
            URL host = new URL(packageName);
            csar = downloadFile(host, downloadPath);
        } catch (MalformedURLException e) {
            File fileLocal = new File(evnfmBasePackage.getPackageName());
            assertThat(fileLocal.exists()).withFailMessage("Csar does not exists in the file system or URL is malformed for %s :: %s", packageName,
                                                           e.getMessage()).isTrue();
            LOGGER.info("The file {} exists and it will be used to onboarded :: {}", fileLocal.getName(), packageName);
            csar = new FileSystemResource(fileLocal);
        }
        evnfmBasePackage.setPackagePath(requireNonNull(csar).getPath());
        return csar;
    }

    /**
     * Utility delay method
     *
     * @param timeInMillis delay duration in milliseconds
     */
    public static void delay(final long timeInMillis) {
        try {
            LOGGER.debug("Sleeping for {} milliseconds\n", timeInMillis);
            Thread.sleep(timeInMillis);
        } catch (final InterruptedException e) {
            LOGGER.debug("Caught InterruptedException: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static <T> void executeForEachInParallel(final Collection<T> objects, final Consumer<T> action) {
        final ExecutorService executor = Executors.newFixedThreadPool(objects.size());

        objects.forEach(obj -> executor.submit(() -> action.accept(obj)));

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (final InterruptedException e) {
            LOGGER.debug("Caught InterruptedException: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static Day0SecretVerificationInfo collectDay0VerificationInfo(final EvnfmCnf cnf) {
        if (cnf.getConfigDay0() == null) {
            return null;
        }
        return new Day0SecretVerificationInfo(
                cnf.getConfigDay0(),
                cnf.getVnfInstanceName(),
                cnf.getNamespace(),
                cnf.getCluster().getLocalPath()
        );
    }

    private static FileSystemResource downloadFile(final URL uri, final String downloadPath) {
        String fileName = FilenameUtils.getName(uri.toString());
        String filePath = downloadPath + System.getProperty("file.separator") + fileName;
        LOGGER.info("Check if file already downloaded :: {}", filePath);
        File fileRemote = new File(filePath);
        if (fileRemote.isFile()) {
            LOGGER.info("The file {} exists, it will be skipped for download", fileRemote.getAbsolutePath());
            return new FileSystemResource(fileRemote);
        } else {
            LOGGER.info("The file {} does not exist, it will be downloaded from {}", fileRemote.getAbsolutePath(), uri);
            try {
                LOGGER.info("Started to download file {} from {}", fileRemote.getName(), uri);
                getCsarDowloadingFromARMRetryTemplate().execute((RetryCallback<Void, Throwable>) context -> {
                    FileUtils.copyURLToFile(uri, fileRemote, 5000, 5000);
                    return null;
                });
            } catch (Throwable e) {
                LOGGER.error("File {} cannot be downloaded from {}, due to :: {}", fileRemote.getName(), uri, e);
                if (fileRemote.delete()) {
                    LOGGER.info("Cleaning up... :: {}", fileRemote.getName());
                }
                Assertions.fail(String.format("File %s cannot be downloaded from %s, due to :: %s", fileRemote.getName(), uri, e));
            }
            LOGGER.info("File: {} successfully downloaded from {}", fileRemote, uri);
        }
        logFilePath(fileRemote.getAbsolutePath());
        return new FileSystemResource(fileRemote);
    }

    private static void logFilePath(final String absolutePath) {
        LOGGER.info("Full path to file is: {}", absolutePath);
    }

    public static HttpHeaders createHeaders(final User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(APPLICATION_JSON);
        return httpHeaders;
    }

    public static String getReasonPhrase(HttpStatusCode httpStatus) {
        if (httpStatus instanceof HttpStatus hs) {
            return hs.getReasonPhrase();
        }

        return String.valueOf(httpStatus.value());
    }

    public static <T> Map<String, Object> collectAndMapVnfControlledScaling(T vnfInstance) {
        final ObjectMapper mapper = getObjectMapper();
        final Map<String, Object> actualExtensionsResponseConvertedIntoMap = (Map<String, Object>) mapper.convertValue(vnfInstance, Map.class);

        AssertionsForClassTypes.assertThat(actualExtensionsResponseConvertedIntoMap.isEmpty()).isFalse();

        final Object vnfControlledScaling = actualExtensionsResponseConvertedIntoMap.get(VNF_CONTROLLED_SCALING);
        if (vnfControlledScaling != null) {
            return (Map<String, Object>) mapper.convertValue(vnfControlledScaling, Map.class);
        }

        return actualExtensionsResponseConvertedIntoMap;
    }

    public static String convertListIntoString(final Collection<?> list) {
        return list.stream().map(Object::toString).collect(Collectors.joining());
    }

    public static Map<String, Object> buildCommonAdditionalParamsMap(final EvnfmCnf evnfmCnf) {
        var additionalParamsOptional = Optional.ofNullable(evnfmCnf.getAdditionalParams());
        Map<String, Object> additionalParams = additionalParamsOptional.orElseGet(HashMap::new);
        additionalParams.put(APPLICATION_TIME_OUT, evnfmCnf.getApplicationTimeout());
        additionalParams.put(CLEAN_UP_RESOURCES, evnfmCnf.isCleanUpResources());
        additionalParams.put(SKIP_VERIFICATION, evnfmCnf.isSkipVerification());
        additionalParams.values().removeAll(Collections.singleton(null));
        return additionalParams;
    }

    public static void updateEvnfmCnfModelWithScaleData(EvnfmCnf evnfmCnf, EvnfmCnf expectedEvnfmCnfDataToScale) {
        evnfmCnf.setAspectToScale(expectedEvnfmCnfDataToScale.getAspectToScale());
        evnfmCnf.setStepsToScale(expectedEvnfmCnfDataToScale.getStepsToScale());
        evnfmCnf.getScaleInfo().putAll(expectedEvnfmCnfDataToScale.getScaleInfo());
        evnfmCnf.setTargets(expectedEvnfmCnfDataToScale.getTargets());
    }

    public static void updateEvnfmCnfModelWithHelmHistoryData(EvnfmCnf evnfmCnf, EvnfmCnf expectedEvnfmCnfHelmHistoryData) {
        evnfmCnf.setExpectedHelmHistory(expectedEvnfmCnfHelmHistoryData.getExpectedHelmHistory());
    }

    public static void updateEvnfmCnfModelWithExpectedOperationState(EvnfmCnf evnfmCnf, EvnfmCnf expectedEvnfmCnfExpectedOperationStateData) {
        evnfmCnf.setExpectedOperationState(expectedEvnfmCnfExpectedOperationStateData.getExpectedOperationState());
    }

    //========================================== CNF INSTANCE LINKS ===================================================//

    public static void collectCnfInstanceLinksIfNeed(EvnfmCnf cnf, User user) {
        if (Objects.isNull(cnf.getVnfInstanceResponseLinks())) {
            VnfInstanceResponse vnfInstanceByRelease = getVNFInstanceByRelease(EVNFM_INSTANCE.getEvnfmUrl(), cnf.getVnfInstanceName(), user);
            assertThat(vnfInstanceByRelease)
                    .withFailMessage("Couldn't find VNF instance with release name %s",
                                     cnf.getVnfInstanceName()).isNotNull();
            cnf.setVnfInstanceResponseLinks(vnfInstanceByRelease.getLinks());
        }
    }

    public static Map<String, Object> buildCleanupAdditionalParamsMap(final EvnfmCnf evnfmCnf) {
        Map<String, Object> additionalParams = new HashMap<>();
        int applicationTimeOut = evnfmCnf.getApplicationTimeout() == null ? 600 : Integer.parseInt(evnfmCnf.getApplicationTimeout());
        additionalParams.put(APPLICATION_TIME_OUT, applicationTimeOut);
        return additionalParams;
    }
}
