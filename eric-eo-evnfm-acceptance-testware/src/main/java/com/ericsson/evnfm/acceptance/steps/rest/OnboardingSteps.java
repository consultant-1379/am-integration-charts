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
package com.ericsson.evnfm.acceptance.steps.rest;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static java.nio.file.Files.walk;
import static java.util.Collections.reverseOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.http.MediaType.parseMediaType;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ALL_HELM_CHARTS_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.DELETE_PACKAGE_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ETSI_CREATE_PACKAGE_URL;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ETSI_GET_PACKAGE_BY_ID_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ETSI_ONBOARDING_PACKAGES_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ONBOARDING_HEALTHCHECK_URI;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.ONBOARDING_PACKAGES_URI;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getCsar;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.delay;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.getHttpHeadersWithToken;
import static com.ericsson.evnfm.acceptance.steps.rest.CommonSteps.returnResponseEntityWithLogs;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.amonboardingservice.model.CreateVnfPkgInfoRequest;
import com.ericsson.amonboardingservice.model.HelmPackage;
import com.ericsson.amonboardingservice.model.ProblemDetails;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.models.EvnfmPackage;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigOnboarding;
import com.ericsson.evnfm.acceptance.utils.ZipUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OnboardingSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingSteps.class);
    private static String GATEWAY_URL = ConfigurationProvider.getGeneralConfig().getApiGatewayHost();
    private static Optional<AppPackageResponse> REST_INSTANTIATE_PACKAGE;
    private static Optional<AppPackageResponse> REST_UPGRADE_PACKAGE;
    private static Optional<AppPackageResponse> UI_INSTANTIATE_PACKAGE;
    private static Optional<AppPackageResponse> UI_UPGRADE_PACKAGE;
    private static List<AppPackageResponse> onboardedPackages;
    private static List<AppPackageResponse> packagesFromTest = new ArrayList<>();
    private static HashMap<String, EvnfmPackage> packagesToBeUsed = new HashMap<>();
    private static String CSAR_DOWNLOAD_PATH = ConfigurationProvider.getOnboardingConfig().getCsarDownloadPath();

    /**
     * Method to onboard a CSAR package.
     *
     * @param configOnboarding Config information about the packages to onboard
     */
    public static void onboardCsars(final ConfigOnboarding configOnboarding) {
        verifyHealthcheck(configOnboarding.getOnboardingHealthTimeout());
        final AtomicLong sleepCounter = new AtomicLong();
        configOnboarding.getPackages(ConfigurationProvider.getTestInfo()).parallelStream()
                .forEach(pkgToOnboard -> {
                    try {
                        Thread.sleep(1000 * sleepCounter.getAndIncrement());
                    } catch (InterruptedException exception) {
                        LOGGER.info("Thread interrupted during parallel onboarding :: {}", pkgToOnboard.getPackageName());
                        fail("Thread interrupted during parallel onboarding :: {}", exception.getMessage());
                    }
                    String packageId = createPackage(pkgToOnboard.getTimeOut(), pkgToOnboard.isSkipImageUpload());
                    onboardPackage(pkgToOnboard, packageId);
                    getOnboardedPackagesInfoWithEtsiApi(packageId, pkgToOnboard.getTimeOut(), pkgToOnboard.isSkipImageUpload());
                    addPackages(packageId, pkgToOnboard);
                });
    }

    private static synchronized void addPackages(String packageId, EvnfmPackage pkgToOnboard) {
        if (!packagesToBeUsed.containsKey(packageId)) {
            LOGGER.info("Putting package in packagesToBeUsed with id: {} Package is: {}", packageId, pkgToOnboard);
            packagesToBeUsed.put(packageId, pkgToOnboard);
        }
    }

    /**
     * Method to onboard a CSAR package.
     *
     * @param csarPackage Package information about the csar to onboard
     */
    public static void onboardCsars(final EvnfmPackage csarPackage) {
        Integer timeOut = csarPackage.getTimeOut();
        boolean skipImageUpload = csarPackage.isSkipImageUpload();
        String packageId = createPackage(timeOut, skipImageUpload);
        LOGGER.info("Package created with ID : {}", packageId);
        onboardPackage(csarPackage, packageId);
        getOnboardedPackagesInfoWithEtsiApi(packageId, timeOut, skipImageUpload);
    }

    private static String createPackage(Integer timeOut, boolean skipImageUpload) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        final String packagesUrl = GATEWAY_URL + ETSI_CREATE_PACKAGE_URL;
        CreateVnfPkgInfoRequest packageRequest = createPackageRequest(timeOut, skipImageUpload);
        HttpEntity<CreateVnfPkgInfoRequest> requestEntity = new HttpEntity<>(packageRequest, httpHeaders);
        final ResponseEntity<VnfPkgInfo> responseEntity = getRestRetryTemplate().execute(context -> getRestTemplate()
                .exchange(packagesUrl, HttpMethod.POST, requestEntity, VnfPkgInfo.class));
        VnfPkgInfo packageInfo = responseEntity.getBody();
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Unable to create package request due to %s", packageInfo).isEqualTo(HttpStatus.CREATED.value());
        assertThat(packageInfo).withFailMessage("Create package response is null").isNotNull();
        assertThat(packageInfo.getOnboardingState())
                .withFailMessage("New Created package is not in CREATED state")
                .isEqualTo(VnfPkgInfo.OnboardingStateEnum.CREATED);
        assertThat(packageInfo.getId()).withFailMessage("Created package has an invalid id")
                .isNotBlank();
        assertThat(packageInfo.getOperationalState()).withFailMessage("Created package has invalid operation state")
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);
        assertThat(packageInfo.getUsageState())
                .withFailMessage("Created package has invalid usage state").isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        verifySelfLink(packageInfo);
        return packageInfo.getId();
    }

    private static CreateVnfPkgInfoRequest createPackageRequest(Integer timeOut, boolean skipImageUpload) {
        CreateVnfPkgInfoRequest packageRequest = new CreateVnfPkgInfoRequest();
        Map<String, Object> userDefinedData = new HashMap<>();
        if (timeOut != null) {
            userDefinedData.put("onboarding.timeout", timeOut);
        }
        if (skipImageUpload) {
            userDefinedData.put("skipImageUpload", skipImageUpload);
        }
        if (!userDefinedData.isEmpty()) {
            packageRequest.setUserDefinedData(userDefinedData);
        }
        return packageRequest;
    }

    private static void verifyHealthcheck(final long onboardingHealthTimeout) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final String healthCheckUrl = GATEWAY_URL + ONBOARDING_HEALTHCHECK_URI;
        LOGGER.info("Performing Onboarding Service health check {}\n", healthCheckUrl);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timeout = now.plusSeconds(onboardingHealthTimeout);
        boolean onboardingHealthy = false;
        int count = 0;
        while (now.isBefore(timeout)) {
            try {
                final ResponseEntity<String> responseEntity =
                        returnResponseEntityWithLogs(healthCheckUrl, HttpMethod.GET, requestEntity, String.class);
                final String responseBody = responseEntity.getBody();
                if (count == 0) {
                    LOGGER.info("Onboarding health check response is: {}", responseBody);
                }
                if (onboardingIsHealthy(responseBody)) {
                    onboardingHealthy = true;
                    break;
                }
            } catch (HttpStatusCodeException e) {
                LOGGER.info("Onboarding service not available yet.");
            } finally {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Ignore
                }
                now = LocalDateTime.now();
                count++;
            }
        }
        if (!onboardingHealthy) {
            fail("Onboarding health check did not pass in {} seconds", onboardingHealthTimeout);
        }
        LOGGER.info("Onboarding Service is healthy\n");
    }

    private static boolean onboardingIsHealthy(final String responseBody) {
        return responseBody.contains("\"Helm Registry\":\"UP\"") && responseBody.contains("\"Docker Registry\":\"UP\"");
    }

    /**
     * Method to get vnfd.
     *
     * @param packageId the package Id to get.
     * @param type      the media type of the accept header
     */
    public static byte[] getVnfdPackagesInfoWithEtsiApi(String packageId, String type) {
        LOGGER.info("Querying for package with ID : {} and by type : {} ", packageId, type);
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        httpHeaders.setAccept(Collections.singletonList(parseMediaType(type)));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final String packagesUrl = GATEWAY_URL + String.format(ETSI_GET_PACKAGE_BY_ID_URI, packageId + "/vnfd");
        final ResponseEntity<byte[]> responseEntity = getRestRetryTemplate().execute(context -> getRestTemplate()
                .exchange(packagesUrl, HttpMethod.GET, requestEntity, byte[].class));
        byte[] body = responseEntity.getBody();
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Failed to get vnfd: %s \nDetail: %s", packagesUrl,
                                 responseEntity.getStatusCode()).isEqualTo(200);
        assertThat(body).withFailMessage("Get response body for %s can't be null", packageId)
                .isNotNull();
        return body;
    }

    public static void verifyVNFDInZipFile(final byte[] responseBody, final String packageId, boolean verifyImports)
    throws IOException, InterruptedException {
        String directoryName = UUID.randomUUID().toString();
        String vnfdId = packagesToBeUsed.get(packageId).getVnfdId();
        Path tempDirectory = Files.createTempDirectory(directoryName);
        try {
            Path zipFile = Files.createTempFile(tempDirectory, "vnfd", ".zip");
            Files.write(zipFile, responseBody);
            LOGGER.info("Going to unPack archive entries in {}:", zipFile);
            ZipUtils.unzipToDirectory(zipFile, tempDirectory);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(zipFile.getParent(), "*.{yml,yaml}")) {
                boolean vnfdIdPresentInFiles;
                Path pathVNFD = getVNFDPath(stream);
                assertThat(pathVNFD).withFailMessage("Path to VNFD is null").isNotNull();
                if (verifyImports) {
                    vnfdIdPresentInFiles = isDescriptorIdPresentInYAML(Files.readAllBytes(pathVNFD), vnfdId);
                    List<String> imports = getImportsFromVNDF(pathVNFD);
                    boolean allImportsArePresentInZip = imports
                            .stream()
                            .map(tempDirectory::resolve)
                            .peek(p -> LOGGER.info("Import path is {}", p))
                            .allMatch(Files::exists);
                    assertThat(allImportsArePresentInZip && vnfdIdPresentInFiles)
                            .withFailMessage(String.format("VNFD imports files are not found. vnfdIdPresentInFiles %s "
                                    + "allImportsArePresentInZip %s", vnfdIdPresentInFiles, allImportsArePresentInZip))
                            .isTrue();
                } else {
                    LOGGER.info("Reading the vnfd file {}:", pathVNFD);
                    checkYamlFile(Files.readAllBytes(pathVNFD), vnfdId);
                }
            }
        } finally {
            deleteDirectory(tempDirectory);
        }
    }

    private static Path getVNFDPath(DirectoryStream<Path> files) throws IOException {
        for (Path file : files) {
            LOGGER.info("VNFD Path is {}", file);
            if (isDescriptorIdFieldPresentInYAML(Files.readAllBytes(file))) {
                LOGGER.info("{} is a vnfd file", file);
                return file;
            } else {
                LOGGER.info("{} is not a VNFD file", file);
            }
        }
        LOGGER.info("VNFD Path not found");
        return null;
    }

    private static void deleteDirectory(final Path tempDirectory) {
        if (tempDirectory != null && tempDirectory.toFile().exists()) {
            try (Stream<Path> walk = walk(tempDirectory)) {
                walk.sorted(reverseOrder()).forEach(file -> {
                    try {
                        Files.delete(file);
                    } catch (IOException e) {
                        LOGGER.error(e.toString(), e);
                    }
                });
            } catch (IOException e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    private static void checkYamlFile(final byte[] file, String vnfdId) {
        final Yaml yaml = getYamlParser();
        final Map<String, Object> responseMap = yaml.load(new ByteArrayInputStream(file));
        JSONObject jsonObject = new JSONObject(responseMap);
        JSONObject nodeTypes = jsonObject.getJSONObject("node_types");
        Iterator<String> keys = nodeTypes.keys();
        JSONObject properties = nodeTypes.getJSONObject(keys.next()).getJSONObject("properties");
        LOGGER.info("Properties are: {}", properties);
        Map<String, Object> stringObjectMap = properties.toMap();
        Object descriptorId = stringObjectMap.get("descriptor_id");
        assertThat(descriptorId.toString()).contains(vnfdId);
    }

    private static List<String> getImportsFromVNDF(Path path) throws IOException {
        final byte[] file = Files.readAllBytes(path);
        final Yaml yaml = getYamlParser();
        List<String> imports = null;
        final Map<String, Object> responseMap = yaml.load(new ByteArrayInputStream(file));
        JSONObject jsonObject = new JSONObject(responseMap);
        try {
            imports = jsonObject.getJSONArray("imports").toList().stream().map(Object::toString).collect(Collectors.toList());
            LOGGER.debug("{} contain imports", path.getFileName());
        } catch (Exception e) {
            LOGGER.debug("{} vnfd file does not contain imports", path);
        }
        LOGGER.info("Imports are: {}", imports);
        return imports;
    }

    private static boolean isDescriptorIdPresentInYAML(final byte[] file, String vnfdId) {
        final Yaml yaml = getYamlParser();
        final Map<String, Object> responseMap = yaml.load(new ByteArrayInputStream(file));
        String descriptorId = "";
        JSONObject jsonObject = new JSONObject(responseMap);
        try {
            JSONObject nodeTypes = jsonObject.getJSONObject("node_types");
            Iterator<String> keys = nodeTypes.keys();
            JSONObject properties = nodeTypes.getJSONObject(keys.next()).getJSONObject("properties");
            Map<String, Object> stringObjectMap = properties.getJSONObject("descriptor_id").toMap();
            descriptorId = (String) stringObjectMap.get("default");
        } catch (Exception e) {
            LOGGER.debug("Yaml file does not contain vnfd descriptor", e);
            return false;
        }

        LOGGER.info("Verifying expected vnfdId {} is equal to {}", vnfdId, descriptorId);
        return vnfdId.equals(descriptorId);
    }

    private static boolean isDescriptorIdFieldPresentInYAML(final byte[] file) {
        final Yaml yaml = getYamlParser();
        final Map<String, Object> responseMap = yaml.load(new ByteArrayInputStream(file));
        JSONObject jsonObject = new JSONObject(responseMap);
        try {
            JSONObject nodeTypes = jsonObject.getJSONObject("node_types");
            Iterator<String> keys = nodeTypes.keys();
            JSONObject properties = nodeTypes.getJSONObject(keys.next()).getJSONObject("properties");
            properties.getJSONObject("descriptor_id");
        } catch (Exception e) {
            LOGGER.info("Yaml file does not contain vnfd descriptor");
            return false;
        }

        return true;
    }

    private static ResponseEntity<String> getPackageResponseEntity(String packageId) {
        LOGGER.info("Querying for package with ID : {}", packageId);
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final String packagesUrl = GATEWAY_URL + String.format(ETSI_GET_PACKAGE_BY_ID_URI, packageId);
        final ResponseEntity<String> responseEntity = getRestRetryTemplate().execute(context -> getRestTemplate()
                .exchange(packagesUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info("Vnf Package Info is: {}", responseEntity);
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Package details could not be retrieved from: %s\nDetail: %s", packagesUrl,
                        responseEntity.getBody()).isEqualTo(200);
        assertThat(responseEntity.getBody()).withFailMessage("Get response body for %s can't be null", packageId).isNotNull();

        return responseEntity;
    }

    private static VnfPkgInfo getPackageInfo(String packageId) throws JsonProcessingException {
        ResponseEntity<String> responseEntity = getPackageResponseEntity(packageId);

        return getObjectMapper().readValue(responseEntity.getBody(), VnfPkgInfo.class);
    }

    private static VnfPkgInfo getPackageInfo(ResponseEntity<String> packageResponse) throws JsonProcessingException {
        return getObjectMapper().readValue(packageResponse.getBody(), VnfPkgInfo.class);
    }

    private static ProblemDetails getErrorDetails(ResponseEntity<String> packageResponse) throws JsonProcessingException {
        return getObjectMapper().readValue(packageResponse.getBody(), ProblemDetails.class);
    }

    private static void getOnboardedPackagesInfoWithEtsiApi(String packageId, Integer timeout, boolean skipImageUpload) {
        StopWatch stopwatch = StopWatch.createStarted();
        int onboardingTimeOut = timeout == null ? 16 : (timeout + 1);
        VnfPkgInfo.OnboardingStateEnum onboardingState = null;
        while (stopwatch.getTime(TimeUnit.MINUTES) < onboardingTimeOut) {
            try {
                ResponseEntity<String> packageResponseEntity = getPackageResponseEntity(packageId);
                JSONObject packageResponse = new JSONObject(packageResponseEntity.getBody());
                if (packageResponse.has("id")) {
                    VnfPkgInfo packageInfo = getPackageInfo(packageResponseEntity);
                    onboardingState = packageInfo.getOnboardingState();
                    switch (onboardingState) {
                        case UPLOADING: {
                            verifyFieldsForUploadingState(packageInfo);
                            continue;
                        }
                        case PROCESSING: {
                            verifyFieldsForProcessingState(packageId, packageInfo);
                            continue;
                        }
                        case ONBOARDED: {
                            LOGGER.info("Package {} onboarded successfully", packageId);
                            verifyFieldsForOnboardedState(packageId, skipImageUpload);
                            return;
                        }
                    }
                } else {
                    ProblemDetails errorDetails = getErrorDetails(packageResponseEntity);
                    assertThat(errorDetails).withFailMessage("Problem details can't be null").isNotNull();
                    assertThat(errorDetails.getDetail()).withFailMessage("details can't be null or empty")
                            .isNotBlank();
                    LOGGER.error("Unable to onboard a package due to {}", errorDetails.getDetail());
                    fail(String.format("Unable to onboard a package %s, in the provided time %s, state is %s and error " +
                            "details %s", packageId, onboardingTimeOut, onboardingState, errorDetails.getDetail()));
                    return;
                }
            } catch (IOException ioe) {
                LOGGER.error("IO error occurred : {}", ioe.getMessage());
                break;
            } finally {
                delay(2000);
            }
            // delay here to prevent logs being printed every 2 second for the duration of the onboarding process - else the logs will get clogged up
            delay(10000);
        }
        fail(String.format("Unable to onboard package %s, in the provided time %s, and state is %s", packageId, onboardingTimeOut, onboardingState));
    }

    private static void verifyFieldsForOnboardedState(String packageId, boolean skipImageUpload)
    throws JsonProcessingException {
        /*
            Performing pause and re-fetching packageInfo
            There appears to be a timing issue with a package being marked as onboarded but with an OperationState as DISABLED
         */
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Ignore
        }
        ResponseEntity<String> packageResponseEntity = getPackageResponseEntity(packageId);
        VnfPkgInfo packageInfo = getPackageInfo(packageResponseEntity);
        LOGGER.info("Package {} was onboarded successfully", packageId);
        LOGGER.info("Vnf Package Info is: {}", packageInfo);
        assertThat(packageInfo.getId()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Package Id")).isNotBlank();
        assertThat(packageInfo.getUsageState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Usage state")).isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState()).withFailMessage(
                String.format("Operation state was not %s. It was %s", VnfPkgInfo.OperationalStateEnum.ENABLED, packageInfo.getOperationalState()))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.ENABLED);
        assertThat(packageInfo.getVnfdId()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Vnf Identifier")).isNotBlank();
        assertThat(packageInfo.getVnfProvider()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Vnf Provider")).isNotBlank();
        assertThat(packageInfo.getVnfProductName()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Product name"))
                .isNotBlank();
        assertThat(packageInfo.getVnfSoftwareVersion()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Vnf Software Version"))
                .isNotBlank();
        assertThat(packageInfo.getVnfdVersion()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Vnf Version")).isNotBlank();
        if (!skipImageUpload) {
            assertThat(packageInfo.getSoftwareImages()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "SoftwareImages"))
                    .isNotNull()
                    .isNotEmpty();
        }
        assertThat(packageInfo.getHelmPackageUrls()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "HelmPackage Urls"))
                .isNotEmpty();

        for (HelmPackage helmPackage : packageInfo.getHelmPackageUrls()) {
            assertThat(helmPackage.getChartUrl()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "HelmPackage url"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartName()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "HelmPackage name"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartVersion()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "HelmPackage version"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartType()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "HelmPackage type"))
                    .isNotNull();
        }

        verifySelfLink(packageInfo);
        verifyHelmChartsUploaded(packageId);
    }

    private static void verifyFieldsForProcessingState(String packageId, VnfPkgInfo packageInfo) throws JsonProcessingException {
        assertThat(packageInfo.getId()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Package Id")).isNotBlank();
        assertThat(packageInfo.getUsageState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Usage state")).isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);

        verifySelfLink(packageInfo);
    }

    private static void verifyFieldsForUploadingState(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getId()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Package Id")).isNotBlank();
        assertThat(packageInfo.getUsageState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Usage state")).isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);

        verifySelfLink(packageInfo);
    }

    public static void verifyPackageUsageState(String packageId, VnfPkgInfo.UsageStateEnum state) throws JsonProcessingException {
        VnfPkgInfo packageInfo = getPackageInfo(packageId);
        assertThat(packageInfo.getUsageState()).withFailMessage(String.format(CANT_BE_NULL_AND_EMPTY_ERROR_MESSAGE, "Usage state")).isNotNull();
        assertThat(packageInfo.getUsageState()).isEqualTo(state);
    }

    private static void verifySelfLink(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getLinks()).withFailMessage("Created package has no links object").isNotNull();
        assertThat(packageInfo.getLinks().getSelf()).withFailMessage("Created package has no self link").isNotNull();
        assertThat(packageInfo.getLinks().getSelf().getHref()).withFailMessage("Created package has no href link").isNotNull();

        String href = packageInfo.getLinks().getSelf().getHref();
        String pkgId = StringUtils.substring(href, StringUtils.lastIndexOf(href, "/") + 1);
        assertThat(pkgId).withFailMessage("PackageIds in the response and the href link does not match").isEqualTo(packageInfo.getId());
    }

    private static void verifyHelmChartsUploaded(String packageId) throws JsonProcessingException {
        List<String> helmChartUrls = getHelmChartUrlsOfPackage(packageId);
        verifyHelmChartsInRegistry(helmChartUrls);
    }

    private static void verifyHelmChartsInRegistry(List<String> helmChartUrls) {
        helmChartUrls.forEach(helmChartUrl -> {
            try {
                ResponseEntity<String> response = getRestRetryTemplate()
                        .execute(context -> getRestTemplate().getForEntity(helmChartUrl, String.class));
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            } catch (HttpStatusCodeException hsce) {
                fail(String.format("Unable to find the HelmChart %s", helmChartUrl));
            }
        });
    }

    public static void initializeOnboardedPackages() {
        final ResponseEntity<String> responseEntity = getPackagesInfo();
        try {
            determinePackages(responseEntity);
        } catch (IOException e) {
            LOGGER.error("Error retrieving onboarded package details", e);
        }
    }

    /**
     * Method to update Onboarded packages for cleanup
     */
    public static void updateOnboardedPackagesInfo(ConfigOnboarding configOnboarding) {
        ResponseEntity<String> packagesInfo = getPackagesInfo();
        onboardedPackages = getPackagesList(packagesInfo);
        configOnboarding.getPackages()
                .forEach(aPackageFromConfig -> {
                    Optional<AppPackageResponse> onboardedPackage = onboardedPackages.stream()
                            .filter(onboardedPkg -> onboardedPkg.getAppDescriptorId().equalsIgnoreCase(aPackageFromConfig.getVnfdId()))
                            .findFirst();

                    onboardedPackage.ifPresent(appPackageResponse -> packagesFromTest.add(appPackageResponse));
                });
    }

    private static ResponseEntity<String> getPackagesInfo() {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
        final String packagesUrl = GATEWAY_URL + ONBOARDING_PACKAGES_URI;
        final ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(packagesUrl, HttpMethod.GET, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Package details could not be retrieved from: %s\nDetail: %s", packagesUrl,
                                 responseEntity.getBody()).isEqualTo(200);
        return responseEntity;
    }

    private static List<AppPackageResponse> getPackagesList(ResponseEntity<String> responseEntity) {
        final ObjectMapper mapper = getObjectMapper();
        List<AppPackageResponse> packageResponseList = new ArrayList<>();
        try {
            final JsonNode jsonNode = mapper.readTree(responseEntity.getBody());
            final JsonNode packages = jsonNode.get("packages");
            packageResponseList = Arrays.asList(mapper.readValue(packages.toString(), AppPackageResponse[].class))
                    .stream()
                    .filter(packageResponse -> packageResponse.getAppDescriptorId() != null)
                    .collect(Collectors.toList());
        } catch (JsonProcessingException j) {
            fail("failed to get packages", j);
        }

        return packageResponseList;
    }

    private static void determinePackages(ResponseEntity<String> responseEntity) throws IOException {
        onboardedPackages = getPackagesList(responseEntity);
        if (onboardedPackages.isEmpty()) {
            LOGGER.error("There are no onboarded packages");
            fail("There are no onboarded packages");
        } else {
            decidePackageForOperations();
        }
    }

    public static EvnfmPackage getPackageToBeUsed(AppPackageResponse pkg) {
        return packagesToBeUsed.get(pkg.getAppPkgId());
    }

    private static void decidePackageForOperations() {
        int phase = ConfigurationProvider.getTestInfo().getPhase();
        REST_INSTANTIATE_PACKAGE = onboardedPackages.stream()
                .filter(pkgOnboarded -> getPackageToBeUsed(pkgOnboarded) != null)
                .filter(pkgOnboarded -> phase == 0 || phase == getPackageToBeUsed(pkgOnboarded).getPhase())
                .filter(pkgOnboarded -> "instantiate".equals(getPackageToBeUsed(pkgOnboarded).getOperation()))
                .filter(pkgOnboarded -> "rest".equals(getPackageToBeUsed(pkgOnboarded).getTestType()))
                .findFirst();
        REST_UPGRADE_PACKAGE = onboardedPackages.stream()
                .filter(pkgOnboarded -> getPackageToBeUsed(pkgOnboarded) != null)
                .filter(pkgOnboarded -> phase == 0 || phase == getPackageToBeUsed(pkgOnboarded).getPhase())
                .filter(pkgOnboarded -> "upgrade".equals(getPackageToBeUsed(pkgOnboarded).getOperation()))
                .filter(pkgOnboarded -> "rest".equals(getPackageToBeUsed(pkgOnboarded).getTestType()))
                .findFirst();
        UI_INSTANTIATE_PACKAGE = onboardedPackages.stream()
                .filter(pkgOnboarded -> getPackageToBeUsed(pkgOnboarded) != null)
                .filter(pkgOnboarded -> phase == 0 || phase == getPackageToBeUsed(pkgOnboarded).getPhase())
                .filter(pkgOnboarded -> "instantiate".equals(getPackageToBeUsed(pkgOnboarded).getOperation()))
                .filter(pkgOnboarded -> "ui".equals(getPackageToBeUsed(pkgOnboarded).getTestType()))
                .findFirst();
        UI_UPGRADE_PACKAGE = onboardedPackages.stream()
                .filter(pkgOnboarded -> getPackageToBeUsed(pkgOnboarded) != null)
                .filter(pkgOnboarded -> phase == 0 || phase == getPackageToBeUsed(pkgOnboarded).getPhase())
                .filter(pkgOnboarded -> "upgrade".equals(getPackageToBeUsed(pkgOnboarded).getOperation()))
                .filter(pkgOnboarded -> "ui".equals(getPackageToBeUsed(pkgOnboarded).getTestType()))
                .findFirst();
        REST_INSTANTIATE_PACKAGE.ifPresent(restInstantiatePackage -> LOGGER.info("REST_INSTANTIATE_PACKAGE is :: {} ",
                                                                                 restInstantiatePackage.getAppDescriptorId()));
        REST_UPGRADE_PACKAGE.ifPresent(restUpgradePackage -> LOGGER.info("REST_UPGRADE_PACKAGE is :: {} ",
                                                                         restUpgradePackage.getAppDescriptorId()));
        UI_INSTANTIATE_PACKAGE.ifPresent(uiInstantiatePackage -> LOGGER.info("UI_INSTANTIATE_PACKAGE is :: {} ",
                                                                             uiInstantiatePackage.getAppDescriptorId()));
        UI_UPGRADE_PACKAGE.ifPresent(uiUpgradePackage -> LOGGER.info("UI_UPGRADE_PACKAGE is :: {} ",
                                                                     uiUpgradePackage.getAppDescriptorId()));
    }

    private static void onboardPackage(final EvnfmPackage csar, String packageId) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", getCsar(csar, CSAR_DOWNLOAD_PATH));
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);
        final String onboardingUrl = GATEWAY_URL + String.format(ETSI_ONBOARDING_PACKAGES_URI, packageId);
        LOGGER.info("Onboarding Csar {} to {}\n", csar.getPackageName(), onboardingUrl);
        final ResponseEntity<String> responseEntity = returnResponseEntityWithLogs(onboardingUrl, HttpMethod.PUT, requestEntity, String.class);
        assertThat(responseEntity.getStatusCode().value())
                .withFailMessage("Package was not successfully onboarded: %s", responseEntity.getBody()).isEqualTo(202);
        LOGGER.info("Request to onboard CSAR {} was accepted\n", csar.getPackageName());
    }

    /**
     * Method to delete a package by ID.
     */
    public static void deleteAndVerifyPackageById(String packageID) throws JsonProcessingException {
        LOGGER.debug("Deleting package with id: " + packageID);
        AppPackageResponse appPackageResponse = packagesFromTest.stream()
                .filter(packageResponse -> packageID.equals(packageResponse.getAppPkgId()))
                .findFirst()
                .orElse(null);

        if (appPackageResponse == null) {
            LOGGER.info(String.format("Error retrieving package with ID %s from onboarded packages.", packageID));
            fail(String.format("Step failed, could not retrieve package with ID %s from onboarded packages.%n", packageID));
        } else {
            if (appPackageResponse.getUsageState().name().equals("NOT_IN_USE")) {
                final List<String> helmChartUrls = getHelmChartUrlsOfPackage(packageID);
                assertThat(makeDeletePackageRequest(packageID).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThatThrownBy(() -> makeGetPackageRequest(packageID))
                        .isInstanceOf(HttpClientErrorException.class).hasMessageContaining("404 Not Found");
                verifyHelmChartsNotFound(helmChartUrls);
                LOGGER.info("Successfully deleted package with id: " + packageID);
            } else if (appPackageResponse.getUsageState().name().equals("IN_USE")) {
                LOGGER.info(String.format("Package with ID %s has usage state of IN_USE, skipping delete...", packageID));
                assertThatThrownBy(() -> makeDeletePackageRequest(packageID))
                        .isInstanceOf(HttpClientErrorException.class)
                        .hasMessageContaining("409 Conflict");
                assertThat(makeGetPackageRequest(packageID).getStatusCode()).isEqualTo(HttpStatus.OK);
            }
        }
    }

    /**
     * Method to make a delete request to the delete package URI.
     */
    private static ResponseEntity<String> makeDeletePackageRequest(String packageID) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        final String packagesUrl = GATEWAY_URL + DELETE_PACKAGE_URI + "/" + packageID;
        return returnResponseEntityWithLogs(packagesUrl, HttpMethod.DELETE, new HttpEntity<>(httpHeaders), String.class);
    }

    /**
     * Method to make a get package by ID request to the onboarded packages URI.
     */
    private static ResponseEntity<String> makeGetPackageRequest(String packageID) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        final String packagesUrl = GATEWAY_URL + ONBOARDING_PACKAGES_URI + "/" + packageID;
        return returnResponseEntityWithLogs(packagesUrl, HttpMethod.GET, new HttpEntity<>(httpHeaders), String.class);
    }

    public static void verifyIfPackageOnboarded(String vnfdId) {
        Optional<AppPackageResponse> packageToCheck = onboardedPackages.stream()
                .filter(packageOnboarded -> packageOnboarded.getAppDescriptorId().equalsIgnoreCase(vnfdId))
                .findFirst();
        if (packageToCheck.isPresent()) {
            String packageId = packageToCheck.get().getAppPkgId();
            ResponseEntity<String> responseEntity = makeGetPackageRequest(packageId);
            assertThat(responseEntity.getStatusCode().value())
                    .withFailMessage("Package is not found: %s", responseEntity.getBody()).isEqualTo(200);
        } else {
            fail("Package is not found.");
        }
    }

    public static void verifyAllTestPackagesRemoved() {
        packagesFromTest.forEach(packageResponse -> {
            assertThatThrownBy(() -> makeGetPackageRequest(packageResponse.getAppPkgId()))
                    .isInstanceOf(HttpClientErrorException.class)
                    .hasMessageContaining("404 Not Found");

            List<String> helmChartUrls = new ArrayList<>();
            try {
                helmChartUrls = getHelmChartUrlsOfPackage(packageResponse.getAppPkgId());
            } catch (JsonProcessingException e) {
                fail("failed to get helm chart", e);
            } catch (HttpClientErrorException httpClientErrorException) {
                assertThat(httpClientErrorException.getStatusCode().value()).isEqualTo(404);
                assertThat(httpClientErrorException.getMessage()).containsIgnoringCase("404 Not Found");
            }

            if (!helmChartUrls.isEmpty()) {
                verifyHelmChartsNotFound(helmChartUrls);
            }
        });
    }

    /**
     * Method to get the Helm chart url by package ID.
     */
    private static List<String> getHelmChartUrlsOfPackage(String packageID) throws JsonProcessingException {
        List<String> helmChartUrls = new ArrayList<>();
        final JSONObject packageGetResponseJson = new JSONObject(makeGetPackageRequest(packageID).getBody());
        LOGGER.info("Package Info is: {}", packageGetResponseJson);
        JSONArray helmPackageUrls = packageGetResponseJson.getJSONArray("helmPackageUrls");
        List<HelmPackage> helmPackages = getObjectMapper().readValue(helmPackageUrls.toString(), new TypeReference<List<HelmPackage>>() {
        });
        helmPackages.forEach(helmPackage -> helmChartUrls.add(GATEWAY_URL + ALL_HELM_CHARTS_URI + "/" + helmPackage.getChartName()));
        return helmChartUrls;
    }

    /**
     * Method to verify Helm charts do not exist.
     *
     * @param helmChartUrls
     */
    private static void verifyHelmChartsNotFound(List<String> helmChartUrls) {
        HttpHeaders httpHeaders = getHttpHeadersWithToken();
        HttpEntity<Void> request = new HttpEntity<>(httpHeaders);

        helmChartUrls.forEach(helmChartUrl -> {
            try {
                returnResponseEntityWithLogs(helmChartUrl, HttpMethod.GET, request, String.class);
            } catch (HttpStatusCodeException hsce) {
                assertThat(hsce.getStatusCode().value()).withFailMessage("Failed to delete helm chart at %s",
                                                                    helmChartUrl).isEqualTo(404);
            }
        });
    }

    public static String getGatewayUrl() {
        return GATEWAY_URL;
    }

    public static Optional<AppPackageResponse> getRestInstantiatePackage() {
        return REST_INSTANTIATE_PACKAGE;
    }

    public static Optional<AppPackageResponse> getRestUpgradePackage() {
        return REST_UPGRADE_PACKAGE;
    }

    public static Optional<AppPackageResponse> getUiInstantiatePackage() {
        return UI_INSTANTIATE_PACKAGE;
    }

    public static Optional<AppPackageResponse> getUiUpgradePackage() {
        return UI_UPGRADE_PACKAGE;
    }

    /**
     * Data Provider for onboarding of CSAR packages
     *
     * @return List of CSAR packages
     */
    public static Stream csarPackagesConfig() {
        return Stream.of(ConfigurationProvider.getOnboardingConfig());
    }

    /**
     * Data Provider for onboarding of CSAR packages
     *
     * @return List of CSAR packages
     */
    public static Stream<Arguments> csarPackagesList() {
        List<EvnfmPackage> packages = ConfigurationProvider.getOnboardingConfig().getPackages();
        return packages.stream().map(Arguments::of);
    }

    /**
     * Data Provider for deleting all packages
     *
     * @return List of CSAR package IDs
     */
    public static List<String> csarPackageIDsFromTestList() {
        return packagesFromTest.stream().map(AppPackageResponse::getAppPkgId).collect(Collectors.toList());
    }

    /**
     * Data Provider for getting package by vnfd
     *
     * @return List containing the first CSAR package ID
     */
    public static Stream<String> getFirstPackageId() {
        return packagesToBeUsed.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> e.getValue().getNumberCharts() != 2)
                .peek(e -> LOGGER.info("Package Id is: {} and is Multi Yaml is: {}", e.getValue().getVnfdId()
                        , e.getValue().isMultiYaml()))
                .filter(e -> !e.getValue().isMultiYaml())
                .peek(e -> LOGGER.info("Package to be used is: {}", e.getValue().getVnfdId()))
                .map(Map.Entry::getKey)
                .limit(1);
    }

    /**
     * Data Provider for getting package by vnfd
     *
     * @return List containing the first CSAR package ID
     */
    public static Stream<String> getPackageWithImportsInVnfd() throws IOException {
        return packagesToBeUsed.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .filter(e -> e.getValue().isMultiYaml())
                .map(Map.Entry::getKey)
                .limit(1);
    }
}

