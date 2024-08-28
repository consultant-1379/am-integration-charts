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

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getCsar;
import static com.ericsson.evnfm.acceptance.utils.Constants.ALL_DOCKER_IMAGES_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ALL_HELM_CHARTS_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ARTIFACT_RESPONSE_LENGTH;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_PACKAGE_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_CREATE_PACKAGE_URL;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_GET_PACKAGE_BY_ID_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_ONBOARDING_PACKAGES_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ETSI_PACKAGE_URL_QUERY_VNF_IDENTIFIER;
import static com.ericsson.evnfm.acceptance.utils.Constants.LIST_PACKAGES_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.ONBOARDING_HEALTHCHECK_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.RESPONSE_INFO_MESSAGE;
import static com.ericsson.evnfm.acceptance.utils.Constants.RETRIEVE_VNFD_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.SPECIFIC_DOCKER_IMAGE_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.SPECIFIC_HELM_CHART_URI;
import static com.ericsson.evnfm.acceptance.utils.Constants.VNFD_ZIP_RESPONSE_LENGTH;
import static com.ericsson.evnfm.acceptance.utils.Constants.ONBOARDING_ARTIFACT_BY_PATH_URL;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ericsson.amonboardingservice.model.CreateVnfPkgInfoRequest;
import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps;

public class OnboardingApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingApiClient.class);

    private static final String ONBOARDING_TIMEOUT_PARAMETER = "onboarding.timeout";
    private static final String SKIP_IMAGE_UPLOAD_PARAMETER = "skipImageUpload";
    private static final String FILENAME_PARAMETER = "fileName";

    private OnboardingApiClient() {
    }

    public static ResponseEntity<String> healthcheckRequest(User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String healthCheckUrl = EVNFM_INSTANCE.getEvnfmUrl() + ONBOARDING_HEALTHCHECK_URI;
        LOGGER.info("Performing Onboarding Service health check: {}\n", healthCheckUrl);
        final ResponseEntity<String> healthcheckInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(healthCheckUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, healthcheckInfo);
        return healthcheckInfo;
    }

    public static ResponseEntity<VnfPkgInfo> etsiCreatePackageRequest(Integer timeOut, boolean skipImageUpload, User user, String filename) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        CreateVnfPkgInfoRequest packageRequest = createPackageRequest(timeOut, skipImageUpload, filename);
        HttpEntity<CreateVnfPkgInfoRequest> requestEntity = new HttpEntity<>(packageRequest, httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + ETSI_CREATE_PACKAGE_URL;
        LOGGER.info("Creating package: {}", packagesUrl);
        final ResponseEntity<VnfPkgInfo> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.POST, requestEntity, VnfPkgInfo.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> onboardPackageRequest(EvnfmBasePackage evnfmPackage, String packageId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        FileSystemResource csar = getCsar(evnfmPackage, EVNFM_INSTANCE.getCsarDownloadPath());
        body.add("file", csar);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, httpHeaders);

        LOGGER.info("CHECK CONNECTION TO ONBOARDING SERVICE");
        ResponseEntity<String> getResponse = vnfPackageByIdRequest(packageId, user);
        LOGGER.info("Read information about an individual VNF package {}", getResponse.getBody());

        final String onboardingUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ETSI_ONBOARDING_PACKAGES_URI, packageId);
        LOGGER.info("Onboarding Csar {} to {}", evnfmPackage.getPackageName(), onboardingUrl);
        LOGGER.info("ONBOARDING CSAR REQUEST ENTITY BODY {}", requestEntity.getBody());
        File file = new File(csar.getPath());
        if (!file.exists() || !file.isFile()) {
            LOGGER.info("Onboarding Csar file is not file or not exist {}", csar.getPath());
        }
        final ResponseEntity<String> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(onboardingUrl, HttpMethod.PUT, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> onboardPackageInputStreamRequest(EvnfmBasePackage evnfmPackage, String packageId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        final FileSystemResource csar = getCsar(evnfmPackage, EVNFM_INSTANCE.getCsarDownloadPath());
        HttpEntity<FileSystemResource> requestEntity = new HttpEntity<>(csar, httpHeaders);

        final String onboardingUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ETSI_ONBOARDING_PACKAGES_URI, packageId);
        LOGGER.info("Onboarding Csar {} to {}", evnfmPackage.getPackageName(), onboardingUrl);
        ResponseEntity<String> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(onboardingUrl, HttpMethod.PUT, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> vnfPackagesRequest(User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + LIST_PACKAGES_URI;
        LOGGER.info("Querying for onboarded VNF packages info: {}", packagesUrl);
        final ResponseEntity<String> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> vnfPackageByIdRequest(String packageId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ETSI_GET_PACKAGE_BY_ID_URI, packageId);
        LOGGER.info("Querying for onboarded VNF package info: {}", packagesUrl);
        final ResponseEntity<String> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> vnfPackagesByVnfdRequest(String vnfdId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesByVnfdIdsUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ETSI_PACKAGE_URL_QUERY_VNF_IDENTIFIER, vnfdId);
        LOGGER.info("Querying for VNFD ids of onboarded VNF packages: {}", packagesByVnfdIdsUrl);
        final ResponseEntity<String> packagesInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesByVnfdIdsUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packagesInfo);
        return packagesInfo;
    }

    public static ResponseEntity<String> helmChartsRequest(User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String helmChartsUrl = EVNFM_INSTANCE.getEvnfmUrl() + ALL_HELM_CHARTS_URI;
        LOGGER.info("Querying for onboarded helm charts info: {}", helmChartsUrl);
        final ResponseEntity<String> chartsInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(helmChartsUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, chartsInfo);
        return chartsInfo;
    }

    public static ResponseEntity<String> helmChartByNameRequest(String name, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String helmChartUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(SPECIFIC_HELM_CHART_URI, name);
        LOGGER.info("Querying for onboarded helm chart info: {}", helmChartUrl);
        final ResponseEntity<String> chartInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(helmChartUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, chartInfo);
        return chartInfo;
    }

    public static ResponseEntity<String> dockerImagesRequest(User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String helmChartsUrl = EVNFM_INSTANCE.getEvnfmUrl() + ALL_DOCKER_IMAGES_URI;
        LOGGER.info("Querying for onboarded docker images info: {}", helmChartsUrl);
        final ResponseEntity<String> imagesInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(helmChartsUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, imagesInfo);
        return imagesInfo;
    }

    public static ResponseEntity<String> dockerImageByNameAndTagRequest(String name, String tag, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String helmChartsUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(SPECIFIC_DOCKER_IMAGE_URI, name, tag);
        LOGGER.info("Querying for onboarded docker image info: {}", helmChartsUrl);
        final ResponseEntity<String> imageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(helmChartsUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, imageInfo);
        return imageInfo;
    }

    public static ResponseEntity<String> deletePackageRequest(String packageId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + DELETE_PACKAGE_URI + "/" + packageId;
        LOGGER.info("Deleting VNF package: {}", packagesUrl);
        final ResponseEntity<String> packageInfo = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.DELETE, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, packageInfo);
        return packageInfo;
    }

    public static ResponseEntity<String> retrieveVnfdAsTextRequest(String pkgId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setAccept(Collections.singletonList(MediaType.TEXT_PLAIN));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(RETRIEVE_VNFD_URI, pkgId);
        LOGGER.info("Querying package VNFD: {}", packagesUrl);
        final ResponseEntity<String> vnfdResponse = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.GET, requestEntity, String.class));
        LOGGER.info(RESPONSE_INFO_MESSAGE, vnfdResponse);
        return vnfdResponse;
    }

    public static ResponseEntity<byte[]> retrieveVnfdAsZipRequest(String pkgId, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setAccept(Collections.singletonList(new MediaType("application", "zip")));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String packagesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(RETRIEVE_VNFD_URI, pkgId);
        LOGGER.info("Querying package VNFD: {}", packagesUrl);
        final ResponseEntity<byte[]> vnfd = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(packagesUrl, HttpMethod.GET, requestEntity, byte[].class));
        LOGGER.info(VNFD_ZIP_RESPONSE_LENGTH, vnfd.getBody().length);
        return vnfd;
    }

    public static ResponseEntity<byte[]> getArtifactContentByPath(String pkgId, String artifactPath, User user) {
        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setAccept(Collections.singletonList(new MediaType("text", "plain")));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);

        final String artifactByPathUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(ONBOARDING_ARTIFACT_BY_PATH_URL, pkgId, artifactPath);
        LOGGER.info("Querying artifact for package {}", artifactByPathUrl);
        final ResponseEntity<byte[]> artifact = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(artifactByPathUrl, HttpMethod.GET, requestEntity, byte[].class));
        LOGGER.info(ARTIFACT_RESPONSE_LENGTH, artifact.getBody().length);
        return artifact;
    }

    private static CreateVnfPkgInfoRequest createPackageRequest(Integer timeOut, boolean skipImageUpload, String filename) {
        CreateVnfPkgInfoRequest packageRequest = new CreateVnfPkgInfoRequest();
        Map<String, Object> userDefinedData = new HashMap<>();
        if (timeOut != null) {
            userDefinedData.put(ONBOARDING_TIMEOUT_PARAMETER, timeOut);
        }
        if (skipImageUpload) {
            userDefinedData.put(SKIP_IMAGE_UPLOAD_PARAMETER, skipImageUpload);
        }
        if (StringUtils.isNotEmpty(filename)) {
            userDefinedData.put(FILENAME_PARAMETER, filename);
        }
        if (!userDefinedData.isEmpty()) {
            packageRequest.setUserDefinedData(userDefinedData);
        }
        return packageRequest;
    }
}
