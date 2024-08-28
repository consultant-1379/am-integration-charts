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

import static org.assertj.core.api.Assertions.fail;

import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getAllVNFInstancesByPackageId;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getLcmOccurrencesByVnfInstanceId;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.deletePackageRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.dockerImageByNameAndTagRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.dockerImagesRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.etsiCreatePackageRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.helmChartByNameRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.helmChartsRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.onboardPackageInputStreamRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.onboardPackageRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.retrieveVnfdAsTextRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.retrieveVnfdAsZipRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.vnfPackageByIdRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.vnfPackagesByVnfdRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingApiClient.vnfPackagesRequest;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyDockerImageByNameAndTagResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyHelmChartByNameResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyHelmChartsResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyListDockerImagesResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyListPackagesResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyPackageCreated;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyPackageDeleted;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyPackageInfoResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyPackageOnboarded;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyTextVnfdResponse;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyVnfdContainsCorrectDescriptorId;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyZipVnfdResponse;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import com.ericsson.amonboardingservice.model.VnfPkgInfo;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmDockerImage;
import com.ericsson.evnfm.acceptance.models.EvnfmHelmChart;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.models.VnfInstanceLegacyResponse;
import com.ericsson.evnfm.acceptance.utils.ZipUtils;
import com.ericsson.vnfm.orchestrator.model.VnfLcmOpOcc;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class OnboardingSteps {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingSteps.class);
    private static final int BUFFER_SIZE = 4096;

    private OnboardingSteps() {
    }

    /**
     * Creates package using ETSI API.
     *
     * @param timeOut
     *         timeout for the create package request
     * @param skipImageUpload
     *         skip pushing of docker images - used for imagesless CSAR
     * @param user
     *         EVNFM user used for authentication
     *
     * @return The Id created for the package
     */
    public static String createPackage(Integer timeOut, boolean skipImageUpload, User user, String filename) {
        ResponseEntity<VnfPkgInfo> responseEntity = etsiCreatePackageRequest(timeOut, skipImageUpload, user, filename);
        VnfPkgInfo packageInfo = responseEntity.getBody();
        verifyPackageCreated(responseEntity, packageInfo);
        return packageInfo.getId();
    }

    public static void onboardPackage(EvnfmBasePackage evnfmPackage, User user, boolean minimalVerification) {
        String filename = evnfmPackage.getPackageName().substring(evnfmPackage.getPackageName().lastIndexOf("/") + 1);
        String packageId = createPackage(evnfmPackage.getTimeOut(), evnfmPackage.isSkipImageUpload(), user, filename);
        ResponseEntity<String> onboardPackageResponse = onboardPackageRequest(evnfmPackage, packageId, user);
        verifyPackageOnboarded(onboardPackageResponse, evnfmPackage, packageId, user, minimalVerification);
    }

    public static void onboardPackage(EvnfmBasePackage evnfmPackage, String packageId, User user, boolean minimalVerification) {
        boolean isInputStreamRequest = BooleanUtils.toBooleanDefaultIfNull(evnfmPackage.isInputStreamRequest(), false);
        ResponseEntity<String> onboardPackageResponse;
        if (isInputStreamRequest) {
            onboardPackageResponse = onboardPackageInputStreamRequest(evnfmPackage, packageId, user);
        } else {
            onboardPackageResponse = onboardPackageRequest(evnfmPackage, packageId, user);
        }
        verifyPackageOnboarded(onboardPackageResponse, evnfmPackage, packageId, user, minimalVerification);
        listOnboardedHelmCharts(evnfmPackage, user);
        listOnboardedHelmChartsByName(evnfmPackage, user);
    }

    public static void onboardPackageIfNotPresent(EvnfmBasePackage evnfmPackage, User user, boolean minimalVerification) {
        List<String> onboardedPackagesVnfdIds = getOnboardedVnfPackages(user)
                .stream()
                .map(EvnfmBasePackage::getVnfdId)
                .collect(Collectors.toList());
        if (!onboardedPackagesVnfdIds.contains(evnfmPackage.getVnfdId())) {
            onboardPackage(evnfmPackage, user, minimalVerification);
        }
    }

    public static void listOnboardedVnfPackages(EvnfmBasePackage evnfmPackage, User user) {
        ResponseEntity<String> packagesInfoResponse = vnfPackagesRequest(user);
        verifyListPackagesResponse(evnfmPackage, packagesInfoResponse, user);
    }

    public static List<EvnfmBasePackage> getOnboardedVnfPackages(User user) {
        ResponseEntity<String> packagesInfoResponse = vnfPackagesRequest(user);
        try {
            return getObjectMapper().readValue(packagesInfoResponse.getBody(), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            fail("Failed during reading package information response", e);
        }
        return Collections.emptyList();
    }

    /**
     * check if package is already onboarded
     *
     * @param vnfdId
     *         The Id of the package that has been onboarded
     * @param user
     *         The user information to use when making the request
     *
     * @return Http response detailing the result of the request
     */
    public static Optional<VnfPkgInfo> getPackageByVnfdIdentifier(String vnfdId, User user) {
        final ResponseEntity<String> responseEntity = vnfPackagesByVnfdRequest(vnfdId, user);
        try {
            List<VnfPkgInfo> packageInfos = getObjectMapper().readValue(responseEntity.getBody(), new TypeReference<>() {
            });

            if (!packageInfos.isEmpty()) {
                LOGGER.info("Package has already been onboarded {}", vnfdId);
                return Optional.of(packageInfos.get(0));
            }
        } catch (JsonProcessingException e) {
            LOGGER.info("Could not parse json packages");
        }
        return Optional.empty();
    }

    public static void listOnboardedVnfPackageByName(EvnfmBasePackage evnfmPackage, User user) {
        ResponseEntity<String> packageInfoResponse = vnfPackageByIdRequest(evnfmPackage.getOnboardedId(), user);
        verifyPackageInfoResponse(evnfmPackage, packageInfoResponse, user);
    }

    public static void listOnboardedHelmCharts(EvnfmBasePackage evnfmBasePackage, User user) {
        ResponseEntity<String> helmChartsResponse = helmChartsRequest(user);
        verifyHelmChartsResponse(evnfmBasePackage, helmChartsResponse);
    }

    public static void listOnboardedHelmChartsByName(EvnfmBasePackage evnfmBasePackage, User user) {
        for (EvnfmHelmChart chart : evnfmBasePackage.getCharts()) {
            ResponseEntity<String> helmChartByNameResponse = helmChartByNameRequest(chart.getName(), user);
            verifyHelmChartByNameResponse(chart, helmChartByNameResponse);
        }
    }

    public static void listOnboardedDockerImages(EvnfmBasePackage evnfmBasePackage, User user) {
        ResponseEntity<String> listDockerImagesResponse = dockerImagesRequest(user);
        verifyListDockerImagesResponse(evnfmBasePackage, listDockerImagesResponse);
    }

    public static void listDockerImageByNameAndTag(EvnfmBasePackage evnfmBasePackage, User user) {
        for (EvnfmDockerImage evnfmDockerImage : evnfmBasePackage.getImages()) {
            for (String tag : evnfmDockerImage.getTags()) {
                ResponseEntity<String> listDockerImageByNameAndTag = dockerImageByNameAndTagRequest(evnfmDockerImage.getImageName(), tag, user);
                verifyDockerImageByNameAndTagResponse(listDockerImageByNameAndTag, evnfmDockerImage);
            }
        }
    }

    public static void deletePackageIfPresent(EvnfmBasePackage evnfmPackage, User user) {
        Optional<VnfPkgInfo> vnfPkgInfoOpt = getPackageByVnfdIdentifier(evnfmPackage.getVnfdId(), user);
        if (vnfPkgInfoOpt.isPresent()) {
            VnfPkgInfo vnfPkgInfo = vnfPkgInfoOpt.get();
            deleteOnboardedPackage(vnfPkgInfo.getId(), vnfPkgInfo.getVnfdId(), user);
        } else {
            LOGGER.info("Package with vnfd is {} is not onboarded", evnfmPackage.getVnfdId());
        }
    }

    public static void deleteOnboardedPackage(String packageId, String vnfdId, User user) {
        try {
            deletePackageRequest(packageId, user);
        } catch (HttpStatusCodeException ex) {
            if (getPackageByVnfdIdentifier(vnfdId, user).isPresent()) {

                final List<VnfInstanceLegacyResponse> allVNFInstances = getAllVNFInstancesByPackageId(packageId, user);
                List<String> failedInstancesInfo = new ArrayList<>(allVNFInstances.size());
                for (VnfInstanceLegacyResponse instance : allVNFInstances) {
                    List<VnfLcmOpOcc> operations = getLcmOccurrencesByVnfInstanceId(EVNFM_INSTANCE.getEvnfmUrl(), instance.getId(), user);
                    String operationInfo = operations.stream().max(Comparator.comparing(VnfLcmOpOcc::getStateEnteredTime))
                            .map(op -> String.format("{%s, %s, %s}", op.getId(), op.getOperation(), op.getOperationState()))
                            .orElse("none");
                    failedInstancesInfo.add(String.format("{%s, %s, last operation: %s}",
                            instance.getId(), instance.getVnfInstanceName(), operationInfo));
                }

                fail("Package %s with VNFD ID %s couldn't be deleted due to existing VNF resources associated with this package : [%s]", packageId,
                     vnfdId, String.join(", ", failedInstancesInfo));
            }
        }

        ResponseEntity<String> packagesInfoResponse = vnfPackagesRequest(user);
        verifyPackageDeleted(packageId, packagesInfoResponse);
    }

    public static void validateTextVnfd(EvnfmBasePackage evnfmPackage, User user) {
        ResponseEntity<String> textVnfdResponse = retrieveVnfdAsTextRequest(evnfmPackage.getOnboardedId(), user);
        verifyTextVnfdResponse(evnfmPackage, textVnfdResponse);
        Path vnfdPath = storeVnfd(evnfmPackage, textVnfdResponse.getBody());
        verifyVnfdContainsCorrectDescriptorId(evnfmPackage, vnfdPath);
    }

    public static void validateZipVnfd(EvnfmBasePackage evnfmPackage, User user) {
        ResponseEntity<byte[]> zipVnfdResponse = retrieveVnfdAsZipRequest(evnfmPackage.getOnboardedId(), user);
        verifyZipVnfdResponse(evnfmPackage, zipVnfdResponse);
        Path vnfdPath = storeZipVnfd(evnfmPackage, zipVnfdResponse.getBody());
        verifyVnfdContainsCorrectDescriptorId(evnfmPackage, vnfdPath);
    }

    /**
     * Saves vnfd zip archive, unpacks the archive and returns base directory of the unpacked archive.
     */
    public static Path unpackVnfdArchive(EvnfmBasePackage evnfmPackage, byte[] vnfdZip) throws IOException, InterruptedException {
        String unpackedVnfdDirectoryName = UUID.randomUUID().toString();
        Path unpackedVnfdDirectoryPath = Files.createTempDirectory(unpackedVnfdDirectoryName);
        Path vnfdZipArchivePath = Files.createTempFile(unpackedVnfdDirectoryPath, evnfmPackage.getVnfdId(), ".zip");
        Files.write(vnfdZipArchivePath, vnfdZip);
        ZipUtils.unzipToDirectory(vnfdZipArchivePath, unpackedVnfdDirectoryPath);
        return unpackedVnfdDirectoryPath;
    }

    private static Path storeZipVnfd(EvnfmBasePackage evnfmPackage, byte[] vnfdZip) {
        byte[] vnfdBytes = getVnfdBytes(vnfdZip, evnfmPackage.getVnfdFile());
        String vnfd = new String(vnfdBytes, StandardCharsets.US_ASCII);
        return storeVnfd(evnfmPackage, vnfd);
    }

    private static Path storeVnfd(EvnfmBasePackage evnfmPackage, String vnfd) {
        try {
            File storedVnfd = File.createTempFile("storedVnfd", ".tmp");
            LOGGER.info("Storing vnfd for {} package in {}", evnfmPackage.getPackageName(), storedVnfd);

            try (FileWriter writer = new FileWriter(storedVnfd)) {
                writer.write(vnfd);
            }
            return storedVnfd.toPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store vnfd to file", e);
        }
    }

    private static byte[] getVnfdBytes(byte[] zippedVnfdBytes, String vnfdFileName) {
        try (ZipInputStream inputStream = new ZipInputStream(new ByteArrayInputStream(zippedVnfdBytes))) {
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                if (entry.getName().equals(vnfdFileName)) {
                    return getVnfdBytes(inputStream);
                }
                inputStream.closeEntry();
            }
            throw new RuntimeException(String.format("Could not find vnfd file %s in vnfd archive", vnfdFileName));
        } catch (IOException e) {
            throw new RuntimeException("Failed to get vnfd bytes from archive", e);
        }
    }

    private static byte[] getVnfdBytes(ZipInputStream inputStream) throws IOException {
        ByteArrayDataOutput result = ByteStreams.newDataOutput();
        byte[] buffer = new byte[BUFFER_SIZE];
        int readBytes;
        while ((readBytes = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, readBytes);
        }
        return result.toByteArray();
    }
}
