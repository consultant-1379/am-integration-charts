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
package com.ericsson.evnfm.acceptance.steps.onboarding.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

import static com.ericsson.amonboardingservice.model.VnfPkgInfo.OnboardingStateEnum.CREATED;
import static com.ericsson.amonboardingservice.model.VnfPkgInfo.OnboardingStateEnum.ONBOARDED;
import static com.ericsson.amonboardingservice.model.VnfPkgInfo.OnboardingStateEnum.PROCESSING;
import static com.ericsson.amonboardingservice.model.VnfPkgInfo.OnboardingStateEnum.UPLOADING;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.delay;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.dockerImageByNameAndTagRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.dockerImagesRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.getArtifactContentByPath;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.healthcheckRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.helmChartByNameRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.helmChartsRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.vnfPackageByIdRequest;
import static com.ericsson.evnfm.acceptance.utils.Constants.CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.JSON_PROCESSION_EXCEPTION_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.PackageUtils.getDescriptorDetails;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.amonboardingservice.model.HelmPackage;
import com.ericsson.amonboardingservice.model.ProblemDetails;
import com.ericsson.amonboardingservice.model.VnfPackageArtifactInfo;
import com.ericsson.amonboardingservice.model.VnfPackageSoftwareImageInfo;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmDockerImage;
import com.ericsson.evnfm.acceptance.models.EvnfmHelmChart;
import com.ericsson.evnfm.acceptance.models.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class OnboardingVerify {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingVerify.class);

    private static final JsonMapper JSON_MAPPER;

    static {
        JSON_MAPPER = JsonMapper.builder().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
    }

    private OnboardingVerify() {
    }

    /**
     * Verify the onboarding service is available
     *
     * @param onboardingHealthTimeout Timeout for the healthcheck request
     */
    public static void verifyHealthcheck(long onboardingHealthTimeout, User user) {
        try {
            await().atMost(onboardingHealthTimeout, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(
                    () -> isOnboardingServiceHealthy(Objects.requireNonNull(healthcheckRequest(user).getBody()))
            );
        } catch (Exception e) {
            LOGGER.error("Onboarding Service is not available");
            fail("Onboarding Service is not available");
        }
        LOGGER.info("Onboarding Service is healthy");
    }

    public static void verifyHelmRegistryHealthcheck(long registryHealthTimeout, User user) {
        try {
            await().atMost(registryHealthTimeout, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(
                    () -> helmChartsRequest(user).getStatusCode().is2xxSuccessful()
            );
        } catch (Exception e) {
            LOGGER.error("Helm Registry Service is not available");
            fail("Helm Registry Service is not available");
        }
        LOGGER.info("Helm Registry Service is healthy");
    }

    public static void verifyContainerRegistryHealthcheck(long registryHealthTimeout, User user) {
        try {
            await().atMost(registryHealthTimeout, TimeUnit.SECONDS).pollInterval(5, TimeUnit.SECONDS).until(
                    () -> {
                        HttpStatusCode status = dockerImagesRequest(user).getStatusCode();
                        return status.is2xxSuccessful() || HttpStatus.NOT_FOUND == status;
                    }
            );
        } catch (HttpClientErrorException exception) {
            if (HttpStatus.NOT_FOUND != exception.getStatusCode()) {
                fail("Container Registry Service is not available");
            }
        } catch (Exception e) {
            LOGGER.error("Container Registry Service is not available");
            fail("Container Registry Service is not available");
        }
        LOGGER.info("Container Registry Service is healthy");
    }

    /**
     * Verify the packageId was created in the onboarding service
     *
     * @param responseEntity Response from the create indentifier REST request
     * @param packageInfo    Package details
     */
    public static void verifyPackageCreated(ResponseEntity<VnfPkgInfo> responseEntity, VnfPkgInfo packageInfo) {
        assertThat(responseEntity.getStatusCode())
                .withFailMessage("Unable to create package: %s", packageInfo)
                .isEqualTo(HttpStatus.CREATED);
        assertThat(packageInfo)
                .withFailMessage("Create package response is null")
                .isNotNull();
        assertThat(packageInfo.getOnboardingState())
                .withFailMessage("New created package is not in CREATED state")
                .isEqualTo(VnfPkgInfo.OnboardingStateEnum.CREATED);
        verifyPackageForCreatedState(packageInfo);
    }

    private static void verifyPackageForCreatedState(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package id"))
                .isNotBlank();
        assertThat(packageInfo.getOperationalState())
                .withFailMessage("Created package has invalid operation state")
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);
        assertThat(packageInfo.getUsageState())
                .withFailMessage("Created package has invalid usage state")
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        verifySelfLink(packageInfo);
    }

    public static void verifyPackageOnboarded(ResponseEntity<String> onboardPackageResponse,
                                              EvnfmBasePackage evnfmPackage,
                                              String packageId, User user, boolean minimalVerification) {
        assertThat(onboardPackageResponse.getStatusCode())
                .withFailMessage("Package was not onboarded successfully: %s", onboardPackageResponse.getBody())
                .isEqualTo(HttpStatus.ACCEPTED);

        StopWatch stopwatch = StopWatch.createStarted();
        VnfPkgInfo.OnboardingStateEnum onboardingState = null;
        try {
            while (stopwatch.getTime(TimeUnit.MINUTES) < evnfmPackage.getTimeOut()) {
                ResponseEntity<String> packageInfoResponse = vnfPackageByIdRequest(packageId, user);
                assertThat(packageInfoResponse.getStatusCode())
                        .withFailMessage("Package details could not be retrieved: %s", packageInfoResponse.getBody())
                        .isEqualTo(HttpStatus.OK);
                assertThat(packageInfoResponse.getBody())
                        .withFailMessage("Response body for package %s can't be null", packageId)
                        .isNotNull();

                JSONObject packageResponse = new JSONObject(packageInfoResponse.getBody());
                if (packageResponse.has("id")) {
                    VnfPkgInfo packageInfo = JSON_MAPPER.readValue(packageInfoResponse.getBody(), VnfPkgInfo.class);
                    onboardingState = packageInfo.getOnboardingState();

                    if (CREATED.equals(onboardingState)) {
                        verifyPackageForCreatedState(packageInfo);
                    } else if (UPLOADING.equals(onboardingState)) {
                        verifyFieldsForUploadingState(packageInfo);
                    } else if (PROCESSING.equals(onboardingState)) {
                        verifyFieldsForProcessingState(packageInfo);
                    } else if (ONBOARDED.equals(onboardingState)) {
                        if (minimalVerification) {
                            verifyMinimalFieldsForOnboardedState(packageInfo, evnfmPackage.isSkipImageUpload(), user);
                        } else {
                            verifyFieldsForOnboardedState(evnfmPackage, packageInfo, user);
                        }
                        LOGGER.info("Package {} onboarded successfully", packageId);
                        return;
                    } else {
                        fail("Unknown package state %s", onboardingState);
                    }
                } else {
                    ProblemDetails errorDetails = JSON_MAPPER.readValue(packageInfoResponse.getBody(), ProblemDetails.class);
                    assertThat(errorDetails)
                            .withFailMessage("Problem details can't be null")
                            .isNotNull();
                    assertThat(errorDetails.getDetail())
                            .withFailMessage("Details can't be null or empty")
                            .isNotBlank();
                    LOGGER.error("Could not onboard package: {}", errorDetails.getDetail());
                    fail(String.format("Unable to onboard package %s, in the provided time %s, state is %s and error details %s",
                                       packageId, evnfmPackage.getTimeOut(), onboardingState, errorDetails.getDetail()));
                    return;
                }
                // delay here to prevent logs being printed every 2 second for the duration of the onboarding process
                // - else the logs will get clogged up
                delay(10000);
            }
        } catch (IOException e) {
            fail("IO error occurred: %s", e.getMessage());
        } finally {
            delay(2000);
        }
        fail(String.format("Unable to onboard package %s, in the provided time %s, and state is %s",
                           packageId, evnfmPackage.getTimeOut(), onboardingState));
    }

    /**
     * Verify fields for a package that is in "Onboarded" state
     *
     * @param packageInfo Details of the package that is being uploaded
     * @param user        User used for authentication in EVNFM
     */
    public static void verifyFieldsForOnboardedState(EvnfmBasePackage evnfmPackage, VnfPkgInfo packageInfo, User user) {
        assertThat(packageInfo.getId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package id"))
                .isNotBlank();
        assertThat(packageInfo.getUsageState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Usage state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.ENABLED);
        assertThat(packageInfo.getVnfdId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Identifier"))
                .isNotBlank();
        assertThat(packageInfo.getVnfProvider())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Provider"))
                .isNotBlank();
        assertThat(packageInfo.getVnfProductName())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Product name"))
                .isNotBlank();
        assertThat(packageInfo.getVnfSoftwareVersion())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Software Version"))
                .isNotBlank();
        assertThat(packageInfo.getVnfdVersion())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Version"))
                .isNotBlank();
        assertThat(packageInfo.getPackageSecurityOption())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package security version"))
                .isNotNull()
                .isEqualTo(evnfmPackage.getPackageSecurityOption());
        if (!evnfmPackage.isSkipImageUpload()) {
            assertThat(packageInfo.getSoftwareImages())
                    .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "SoftwareImages"))
                    .isNotNull()
                    .isNotEmpty();
            verifyDockerImagesUploaded(packageInfo.getSoftwareImages(), user);
        }

        assertThat(packageInfo.getHelmPackageUrls())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage Urls"))
                .isNotEmpty();

        for (HelmPackage helmPackage : packageInfo.getHelmPackageUrls()) {
            assertThat(helmPackage.getChartUrl()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage url"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartName()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage name"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartVersion()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage version"))
                    .isNotEmpty();
            assertThat(helmPackage.getChartType()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage type"))
                    .isNotNull();
            assertThat(helmPackage.getPriority()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage priority"))
                    .isNotNull();
        }

        verifyHelmChartsUploaded(packageInfo.getHelmPackageUrls(), user);
        verifySelfLink(packageInfo);
    }

    /**
     * Verify  minimal fields for a package that is in "Onboarded" state
     *
     * @param packageInfo      Details of the package that is being uploaded
     * @param imagelessPackage True for a package that does not contain docker images
     * @param user             User used for authentication in EVNFM
     */
    public static void verifyMinimalFieldsForOnboardedState(VnfPkgInfo packageInfo, boolean imagelessPackage, User user) {
        assertThat(packageInfo.getId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package id"))
                .isNotBlank();
        assertThat(packageInfo.getUsageState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Usage state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.ENABLED);
        assertThat(packageInfo.getVnfdId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Identifier"))
                .isNotBlank();
        assertThat(packageInfo.getVnfProvider())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Provider"))
                .isNotBlank();
        assertThat(packageInfo.getVnfProductName())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Product name"))
                .isNotBlank();
        assertThat(packageInfo.getVnfSoftwareVersion())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Software Version"))
                .isNotBlank();
        assertThat(packageInfo.getVnfdVersion())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnf Version"))
                .isNotBlank();
        if (!imagelessPackage) {
            assertThat(packageInfo.getSoftwareImages())
                    .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "SoftwareImages"))
                    .isNotNull()
                    .isNotEmpty();
            verifyDockerImagesUploaded(packageInfo.getSoftwareImages(), user);
        }

        assertThat(packageInfo.getHelmPackageUrls())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage Urls"))
                .isNotEmpty();

        for (HelmPackage helmPackage : packageInfo.getHelmPackageUrls()) {
            assertThat(helmPackage.getChartUrl()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage url"))
                    .isNotEmpty();
            assertThat(helmPackage.getPriority()).withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "HelmPackage priority"))
                    .isNotNull();
        }

        verifySelfLink(packageInfo);
    }

    /**
     * Verify fields for a package that is in "Processing" state
     *
     * @param packageInfo Details of the package that is being uploaded
     */
    public static void verifyFieldsForProcessingState(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package id"))
                .isNotBlank();
        assertThat(packageInfo.getUsageState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Usage state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);

        verifySelfLink(packageInfo);
    }

    /**
     * Verify fields for a package that is in "Uploading" state
     *
     * @param packageInfo Details of the package that is being uploaded
     */
    public static void verifyFieldsForUploadingState(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getId())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package id"))
                .isNotBlank();
        assertThat(packageInfo.getUsageState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Usage state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.UsageStateEnum.NOT_IN_USE);
        assertThat(packageInfo.getOperationalState())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Operation state"))
                .isNotNull()
                .isEqualTo(VnfPkgInfo.OperationalStateEnum.DISABLED);

        verifySelfLink(packageInfo);
    }

    /**
     * Verify a package does not exist in the Onboarding service
     *
     * @param packageId            Onboarded id of a package
     * @param listPackagesResponse Response from list packages API
     */
    public static void verifyPackageDeleted(String packageId, ResponseEntity<String> listPackagesResponse) {
        assertThat(listPackagesResponse.getStatusCode())
                .withFailMessage("Could not get list of onboarded VNF packages: %s", listPackagesResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(listPackagesResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Packages info"))
                .isNotBlank();
        try {
            List<VnfPkgInfo> packageInfos = JSON_MAPPER.readValue(listPackagesResponse.getBody(), new TypeReference<>() {
            });
            for (VnfPkgInfo packageInfo : packageInfos) {
                if (packageInfo.getId().equals(packageId)) {
                    fail("VNF package %s is present in list packages response after being deleted", packageId);
                }
            }
        } catch (JsonProcessingException jpe) {
            fail(JSON_PROCESSION_EXCEPTION_MESSAGE, jpe.getMessage());
        }
    }

    public static void verifyListPackagesResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<String> listPackagesResponse, User user) {
        assertThat(listPackagesResponse.getStatusCode())
                .withFailMessage("Could not get list of onboarded VNF packages: %s", listPackagesResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(listPackagesResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Packages info"))
                .isNotBlank();
        try {
            List<VnfPkgInfo> packageInfos = JSON_MAPPER.readValue(listPackagesResponse.getBody(), new TypeReference<>() {
            });
            boolean foundMatchingPackage = false;
            for (VnfPkgInfo packageInfo : packageInfos) {
                if (packageInfo.getId().equals(evnfmPackage.getOnboardedId())) {
                    foundMatchingPackage = true;
                    verifyFieldsForOnboardedState(evnfmPackage, packageInfo, user);
                }
            }
            if (!foundMatchingPackage) {
                fail("Did not find matching Vnf package info for id: %s", evnfmPackage.getOnboardedId());
            }
        } catch (JsonProcessingException jpe) {
            fail(JSON_PROCESSION_EXCEPTION_MESSAGE, jpe.getMessage());
        }
    }

    public static void verifyPackageInfoResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<String> packageInfoResponse, User user) {
        assertThat(packageInfoResponse.getStatusCode())
                .withFailMessage("Could not retrieve details of onboarded package: %s", packageInfoResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(packageInfoResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Package info"))
                .isNotBlank();

        try {
            VnfPkgInfo packageInfo = JSON_MAPPER.readValue(packageInfoResponse.getBody(), VnfPkgInfo.class);
            assertThat(packageInfo.getId())
                    .withFailMessage("Id of the returned package does not match the expected package id : %s != %s",
                                     packageInfo.getId(), evnfmPackage.getOnboardedId())
                    .isEqualTo(evnfmPackage.getOnboardedId());
            verifyFieldsForOnboardedState(evnfmPackage, packageInfo, user);
        } catch (JsonProcessingException jpe) {
            fail(JSON_PROCESSION_EXCEPTION_MESSAGE, jpe.getMessage());
        }
    }

    public static void verifyHelmChartsResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<String> helmChartsResponse) {
        assertThat(helmChartsResponse.getStatusCode())
                .withFailMessage("Could not retrieve details of onboarded helm charts: %s", helmChartsResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(helmChartsResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Charts info"))
                .isNotBlank();

        verifyAlphabeticalOrder(helmChartsResponse.getBody());
        JSONObject chartsInfo = new JSONObject(helmChartsResponse.getBody());

        for (EvnfmHelmChart chart : evnfmPackage.getCharts()) {
            boolean foundMatch = false;
            for (String chartName : chartsInfo.keySet()) {
                if (chartName.equals(chart.getName())) {
                    JSONArray chartsArray = chartsInfo.getJSONArray(chartName);
                    for (int i = 0; i < chartsArray.length(); ++i) {
                        JSONObject chartInfo = chartsArray.getJSONObject(i);
                        if (chartInfo.getString("version").equals(chart.getVersion())) {
                            foundMatch = true;
                            break;
                        }
                    }
                }
            }
            if (!foundMatch) {
                fail("Could not find a matching chart for name %s and tag %s", chart.getName(), chart.getVersion());
            }
        }
    }

    public static void verifyHelmChartByNameResponse(EvnfmHelmChart chart, ResponseEntity<String> helmChartByNameResponse) {
        assertThat(helmChartByNameResponse.getStatusCode())
                .withFailMessage("Helm chart details could not be retrieved: %s", helmChartByNameResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(helmChartByNameResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Chart info"))
                .isNotBlank();

        JSONArray chartsInfo = new JSONArray(helmChartByNameResponse.getBody());
        boolean foundMatch = false;
        for (int i = 0; i < chartsInfo.length(); ++i) {
            JSONObject chartInfo = chartsInfo.getJSONObject(i);
            String name = chartInfo.getString("name");
            String version = chartInfo.getString("version");
            if (name.equals(chart.getName()) && version.equals(chart.getVersion())) {
                foundMatch = true;
                break;
            }
        }
        if (!foundMatch) {
            fail("Could not find a matching chart for name %s and tag %s", chart.getName(), chart.getVersion());
        }
    }

    public static void verifyListDockerImagesResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<String> listDockerImagesResponse) {
        assertThat(listDockerImagesResponse.getStatusCode())
                .withFailMessage("Docker images could not be retrieved: %s", listDockerImagesResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(listDockerImagesResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Docker images info"))
                .isNotBlank();

        JSONObject dockerImagesInfo = new JSONObject(listDockerImagesResponse.getBody());
        for (EvnfmDockerImage evnfmDockerImage : evnfmPackage.getImages()) {
            verifyDockerImagePresent(evnfmDockerImage, dockerImagesInfo);
        }
    }

    public static void verifyDockerImageByNameAndTagResponse(ResponseEntity<String> dockerImageResponse,
                                                             EvnfmDockerImage evnfmDockerImage) {
        assertThat(dockerImageResponse.getStatusCode())
                .withFailMessage("Could not retrieve docker image details: %s", dockerImageResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(dockerImageResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Docker image info"))
                .isNotBlank();

        JSONObject dockerImagesInfo = new JSONObject(dockerImageResponse.getBody());
        verifyDockerImagePresent(evnfmDockerImage, dockerImagesInfo);
    }

    public static void verifyTextVnfdResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<String> textVnfdResponse) {
        assertThat(textVnfdResponse.getStatusCode())
                .withFailMessage("Could not retrieve package VNFD: %s", textVnfdResponse.getBody())
                .isEqualTo(HttpStatus.OK);
        assertThat(textVnfdResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "VNFD"))
                .isNotBlank();
    }

    public static void verifyZipVnfdResponse(EvnfmBasePackage evnfmPackage, ResponseEntity<byte[]> zipVnfdResponse) {
        assertThat(zipVnfdResponse.getStatusCode())
                .withFailMessage("Could not retrieve package vnfd: %s", new String(zipVnfdResponse.getBody()))
                .isEqualTo(HttpStatus.OK);
        assertThat(zipVnfdResponse.getBody())
                .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Vnfd archive"))
                .isNotNull();
    }

    public static void verifyAlphabeticalOrder(String chartsInfo) {
        String chartNamesArray = toArrayOfProperties(chartsInfo);
        LOGGER.info("Chart names: {}", chartNamesArray);
        JSONArray chartNames = new JSONArray(chartNamesArray);
        for (int i = 0; i < chartNames.length() - 1; ++i) {
            String currentName = chartNames.getString(i);
            String nextName = chartNames.getString(i + 1);
            if (currentName.compareTo(nextName) > 0) {
                fail("Chart names are not in alphabetical order, %s -> %s", currentName, nextName);
            }
        }
    }

    public static void verifyVnfdContainsCorrectDescriptorId(EvnfmBasePackage evnfmPackage, Path vnfdPath) {
        VnfDescriptorDetails details = getDescriptorDetails(vnfdPath);
        assertThat(details.getVnfDescriptorId())
                .withFailMessage("Expected vnfd id : %s, actual vnfd id : %s", evnfmPackage.getVnfdId(), details.getVnfDescriptorId())
                .isEqualTo(details.getVnfDescriptorId());
    }

    /**
     * Checks that:
     * - vnfd file is present in the unpacked ar,chive
     * - vnfd id corresponds to the one in the package
     * - all imported files in vnfd are present in the unpacked archive
     */
    public static void verifyVnfdArchive(EvnfmBasePackage evnfmPackage, Path vnfdArchiveBaseDirectory) {
        Path vnfdPath = Paths.get(vnfdArchiveBaseDirectory.toString(), evnfmPackage.getVnfdFile());
        assertThat(vnfdPath.toFile().exists())
                .withFailMessage("Vnfd file %s is not present in the unpacked csar", evnfmPackage.getVnfdFile())
                .isTrue();
        verifyVnfdContainsCorrectDescriptorId(evnfmPackage, vnfdPath);
        verifyAllImportArePresentInVnfdArchive(vnfdPath, vnfdArchiveBaseDirectory);
    }

    private static void verifyAllImportArePresentInVnfdArchive(Path vnfdPath, Path vnfdArchiveBaseDirectory) {
        List<String> vnfdImports = getImportsFromVnfd(vnfdPath);
        for (String vnfdImport : vnfdImports) {
            Path vnfdImportPath = vnfdArchiveBaseDirectory.resolve(vnfdImport);
            if (Files.exists(vnfdImportPath)) {
                LOGGER.info("Import {} is present in the unpacked vnfd archive {}", vnfdImport, vnfdPath);
            } else {
                fail("Import %s is not present in unpacked vnfd archive %s", vnfdImport, vnfdPath);
            }
        }
        LOGGER.info("Successfully verified imports for vnfd {}", vnfdPath);
    }

    private static List<String> getImportsFromVnfd(Path vnfdPath) {
        String importsKey = "imports";
        try {
            byte[] vnfdBytes = Files.readAllBytes(vnfdPath);

            Yaml yamlParser = getYamlParser();
            Map<String, Object> vnfdMap = yamlParser.load(new ByteArrayInputStream(vnfdBytes));
            JSONObject vnfdJson = new JSONObject(vnfdMap);

            if (vnfdJson.has(importsKey)) {
                List<String> imports = vnfdJson.getJSONArray(importsKey).toList().stream()
                        .map(Object::toString).collect(Collectors.toList());
                LOGGER.info("Successfully loaded import from vnfd {}, imports are: {}", vnfdPath, imports);
                return imports;
            } else {
                LOGGER.info("Vnfd {} does not have imports section", vnfdPath);
            }
            return Collections.emptyList();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to load imports from vnfd %s", vnfdPath), e);
        }
    }

    private static String toArrayOfProperties(String chartsInfo) {
        BitSet charIsDeleted = new BitSet(chartsInfo.length());
        int semicolonIndex = chartsInfo.indexOf(':');
        while (semicolonIndex != -1) {
            int openBracketIndex = chartsInfo.indexOf('[', semicolonIndex);
            int closingBracketIndex = closingBracketIndex(chartsInfo, openBracketIndex);
            charIsDeleted.set(semicolonIndex, closingBracketIndex + 1);
            semicolonIndex = chartsInfo.indexOf(':', closingBracketIndex + 1);
        }
        return removeDeleteChars(chartsInfo, charIsDeleted);
    }

    private static String removeDeleteChars(String json, BitSet charIsDeleted) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < json.length(); ++i) {
            if (!charIsDeleted.get(i)) {
                if (json.charAt(i) == ':') {
                    result.append(',');
                } else {
                    result.append(json.charAt(i));
                }
            }
        }
        result.setCharAt(result.indexOf("{"), '[');
        result.setCharAt(result.lastIndexOf("}"), ']');
        return result.toString();
    }

    private static int closingBracketIndex(String json, int openBracketIndex) {
        int bracketsBalance = 0;
        for (int i = openBracketIndex; i < json.length(); ++i) {
            if (json.charAt(i) == '[') {
                ++bracketsBalance;
            } else if (json.charAt(i) == ']') {
                --bracketsBalance;
            }
            if (bracketsBalance == 0) {
                return i;
            }
        }
        fail("Could not find closing square bracket for open bracket at index %d", openBracketIndex);
        return -1;
    }

    private static boolean isOnboardingServiceHealthy(final String responseBody) {
        return responseBody.contains("\"status\":\"UP\"") && responseBody.contains("\"ping\":{\"status\":\"UP\"}");
    }

    private static void verifyDockerImagePresent(EvnfmDockerImage evnfmDockerImage, JSONObject dockerImagesInfo) {
        JSONArray projects = dockerImagesInfo.getJSONArray("projects");
        boolean foundMatch = false;
        for (int i = 0; i < projects.length() && !foundMatch; ++i) {
            JSONObject project = projects.getJSONObject(i);
            if (project.getString("name").equals(evnfmDockerImage.getProjectName())) {
                JSONArray images = project.getJSONArray("images");
                for (int j = 0; j < images.length() && !foundMatch; ++j) {
                    JSONObject image = images.getJSONObject(j);
                    if (image.getString("name").equals(evnfmDockerImage.getImageName())) {
                        JSONArray tags = image.getJSONArray("tags");
                        List<String> tagsList = new ArrayList<>(tags.length());
                        for (int k = 0; k < tags.length(); ++k) {
                            tagsList.add(tags.getString(k));
                        }
                        if (tagsList.containsAll(evnfmDockerImage.getTags())) {
                            foundMatch = true;
                        } else {
                            fail("Docker images info does not contain all tags for project name: %s, image name: %s and tags: %s",
                                 evnfmDockerImage.getProjectName(), evnfmDockerImage.getImageName(), evnfmDockerImage.getTags());
                        }
                    }
                }
            }
        }
        if (!foundMatch) {
            fail("Did not find matching docker images for project name: %s, image name: %s and tags: %s",
                 evnfmDockerImage.getProjectName(), evnfmDockerImage.getImageName(), evnfmDockerImage.getTags());
        }
    }

    private static void verifyDockerImagesUploaded(List<VnfPackageSoftwareImageInfo> dockerImages, User user) {
        dockerImages.forEach(dockerImage -> {
            assertThat(dockerImage.getImagePath())
                    .withFailMessage(String.format(CANT_BE_NULL_OR_EMPTY_ERROR_MESSAGE, "Image path"))
                    .isNotBlank();
            String projectNameWithImageName = dockerImage.getName();
            String dockerImageName = projectNameWithImageName.substring(projectNameWithImageName.lastIndexOf('/') + 1);
            verifyDockerImageInRegistry(dockerImageName, dockerImage.getVersion(), user);
        });
    }

    private static void verifyDockerImageInRegistry(String imageName, String imageTag, User user) {
        try {
            ResponseEntity<String> response = dockerImageByNameAndTagRequest(imageName, imageTag, user);
            assertThat(response.getStatusCode())
                    .withFailMessage("Docker image details could not be retrieved: %s", response.getBody())
                    .isEqualTo(HttpStatus.OK);
        } catch (HttpStatusCodeException hsce) {
            fail(String.format("Unable to find the docker image with name %s and tag %s in the registry", imageName, imageTag));
        }
    }

    private static void verifyHelmChartsUploaded(List<HelmPackage> helmPackages, User user) {
        helmPackages.stream().map(HelmPackage::getChartName).forEach(helmChartName -> verifyHelmChartInRegistry(helmChartName, user));
    }

    private static void verifyHelmChartInRegistry(String helmChartName, User user) {
        try {
            ResponseEntity<String> response = helmChartByNameRequest(helmChartName, user);
            assertThat(response.getStatusCode())
                    .withFailMessage("Helm chart details could not be retrieved: %s", response.getBody())
                    .isEqualTo(HttpStatus.OK);
        } catch (HttpStatusCodeException hsce) {
            fail(String.format("Unable to find the HelmChart %s in the registry", helmChartName));
        }
    }

    private static void verifySelfLink(VnfPkgInfo packageInfo) {
        assertThat(packageInfo.getLinks())
                .withFailMessage("Created package has no links object")
                .isNotNull();
        assertThat(packageInfo.getLinks().getSelf())
                .withFailMessage("Created package has no self link")
                .isNotNull();
        assertThat(packageInfo.getLinks().getSelf().getHref())
                .withFailMessage("Created package has no href link")
                .isNotNull();

        String href = packageInfo.getLinks().getSelf().getHref();
        String pkgId = StringUtils.substring(href, StringUtils.lastIndexOf(href, "/") + 1);
        assertThat(pkgId)
                .withFailMessage("PackageIds in the response and the href link does not match")
                .isEqualTo(packageInfo.getId());
    }

    public static void verifyAdditionalValuesIfPresent(VnfPkgInfo packageInfo, User user) {
        List<VnfPackageArtifactInfo> artifactsInfo = packageInfo.getAdditionalArtifacts();
        if (artifactsInfo == null) {
            LOGGER.info("No additional artifacts present in package with VNFD ID {}", packageInfo.getVnfdId());
            return;
        }
        List<VnfPackageArtifactInfo> additionalValues = artifactsInfo
                .stream().filter(artifact -> artifact.getArtifactPath().startsWith("Definitions/OtherTemplates/") &&
                        artifact.getArtifactPath().endsWith(".yaml")
                        && !artifact.getArtifactPath().contains("scaling_mapping")).collect(Collectors.toList());
        if (!additionalValues.isEmpty()) {
            List<String> helmPackages = getFullHelmChartNames(packageInfo.getHelmPackageUrls());
            for (VnfPackageArtifactInfo valuesArtifact : additionalValues) {
                String valuesName = getValuesNameOfFullPath(valuesArtifact.getArtifactPath());
                assertThat(helmPackages.stream().anyMatch(helmPackageName -> helmPackageName.equals(valuesName)))
                        .withFailMessage("Additional values.yaml does not have corresponding helm package")
                        .isEqualTo(true);
                verifyAdditionalValuesContentIsNotEmpty(packageInfo, valuesArtifact.getArtifactPath(), user);
            }
        } else {
            LOGGER.info("No additional values present in package with VNFD ID {}", packageInfo.getVnfdId());
        }
    }

    private static void verifyAdditionalValuesContentIsNotEmpty(VnfPkgInfo packageInfo, String additionalValuesPath, User user) {
        try {
            ResponseEntity<byte[]> response = getArtifactContentByPath(packageInfo.getId(), additionalValuesPath, user);
            assertThat(response.getStatusCode())
                    .withFailMessage("Content could not be retrieved for artifact: %s", additionalValuesPath)
                    .isEqualTo(HttpStatus.OK);
            assertThat(response.getBody())
                    .withFailMessage("Additional values.yaml cannot be empty: %s", additionalValuesPath)
                    .isNotEmpty();
        } catch (HttpStatusCodeException hsce) {
            fail(String.format("Unable to find the artifact %s in the package", additionalValuesPath));
        }
    }

    private static List<String> getFullHelmChartNames(List<HelmPackage> helmPackages) {
        StringBuilder sb = new StringBuilder();
        List<String> chartNames = new ArrayList<>();
        for (HelmPackage helmPackage : helmPackages) {
            sb.append(helmPackage.getChartName())
                    .append("-")
                    .append(helmPackage.getChartVersion());
            chartNames.add(sb.toString());
            sb.setLength(0);
        }
        return chartNames;
    }

    private static String getValuesNameOfFullPath(String artifactPath) {
        return artifactPath.substring(27, artifactPath.indexOf(".yaml"));
    }
}
