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

import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static java.util.Objects.requireNonNull;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.TOKEN_URI_RESOURCE;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.X_LOGIN;
import static com.ericsson.evnfm.acceptance.common.TestwareConstants.X_PASSWORD;
import static com.ericsson.evnfm.acceptance.steps.CleanUpSteps.createAndAddCleanUp;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getGatewayUrl;
import static com.ericsson.evnfm.acceptance.steps.rest.OnboardingSteps.getPackageToBeUsed;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import com.ericsson.amonboardingservice.model.AppPackageResponse;
import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.models.CleanUp;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigCluster;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigInstantiate;

public class CommonSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSteps.class);
    private static String KEYCLOAK_USERNAME = ConfigurationProvider.getGeneralConfig().getVnfmUsername();
    private static String KEYCLOAK_PASSWORD = ConfigurationProvider.getGeneralConfig().getVnfmPassword();

    /**
     * Method to retrieve a JSESSIONID token.
     *
     * @param keycloakUsername
     *         Username matching keycloak secret username
     * @param keycloakPassword
     *         Password matching keycloak secret password
     *
     * @return JSESSIONID in String format
     */
    public static String retrieveToken(String keycloakUsername, String keycloakPassword, boolean retry) {
        try {
            String gatewayUrl = getGatewayUrl();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set(X_LOGIN, keycloakUsername);
            httpHeaders.set(X_PASSWORD, keycloakPassword);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(httpHeaders);
            LOGGER.debug("Retrieving Session token from {}\n", gatewayUrl);
            final ResponseEntity<String> responseEntity = getRestRetryTemplate().execute(context -> getRestTemplate()
                    .postForEntity(gatewayUrl + TOKEN_URI_RESOURCE, requestEntity, String.class));
            assertThat(responseEntity.getStatusCode().value())
                    .withFailMessage("Token could not be retrieved successfully from: %s\nDetail: %s", gatewayUrl,
                            responseEntity.getBody()).isEqualTo(200);
            LOGGER.debug("Successfully retrieved session token from {}\n", gatewayUrl);
            return responseEntity.getBody();
        } catch (Exception e) {
            if(retry) {
                LOGGER.warn("Token could not be retrieved successfully. Logging error and retrying", e);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    LOGGER.warn("Sleep was interrupted: ", ie);
                }
                return retrieveToken(keycloakUsername, keycloakPassword, false);
            } else {
                throw e;
            }
        }
    }

    public static HttpHeaders getHttpHeadersWithToken() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("cookie", "JSESSIONID=" + retrieveToken(KEYCLOAK_USERNAME, KEYCLOAK_PASSWORD, true));
        return httpHeaders;
    }

    public static FileSystemResource getFileResource(final String fileToLocate) {
        LOGGER.info("File to locate is {}", fileToLocate);
        File file = new File(fileToLocate);
        if(!file.exists()){
            LOGGER.error("The file {} does not exist", file.getAbsolutePath());
        }
        if (file.isFile()) {
            LOGGER.info("Full path to file is: {}", file.getAbsolutePath());
            return new FileSystemResource(file);
        }
        ClassLoader classLoader = OnboardingSteps.class.getClassLoader();
        file = new File(requireNonNull(classLoader.getResource(fileToLocate)).getFile());
        LOGGER.info("Full path to file is: {}", file.getAbsolutePath());
        return new FileSystemResource(file);
    }

    public static void delay(final long timeInMillis) {
        try {
            LOGGER.debug("Sleeping for {} milliseconds\n", timeInMillis);
            Thread.sleep(timeInMillis);
        } catch (final InterruptedException e) {
            LOGGER.debug("Caught InterruptedException: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static ResponseEntity returnResponseEntityWithLogs(String url, HttpMethod httpMethod, HttpEntity httpEntity, Class responseClass) {
        ResponseEntity responseEntity = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(url, httpMethod, httpEntity, responseClass));
        LOGGER.debug(responseEntity.toString());
        return responseEntity;
    }

    public static void uniquelyIdentifyReleaseAndNamespace(final ConfigCluster configCluster,
                                                           final ConfigInstantiate configInstantiate,
                                                           List<CleanUp> releasesToCleanUp,
                                                           final String layer, final String defaultValue,
                                                           final AppPackageResponse appPackageResponse) {
        int numberCharts = getPackageToBeUsed(appPackageResponse).getNumberCharts();
        configInstantiate.setReleaseName(configInstantiate.getReleaseName() + layer);
        final String namespace =
                configInstantiate.getNamespace() == null ? defaultValue : configInstantiate.getNamespace() + layer;
        configInstantiate.setNamespace(namespace);
        if (numberCharts == 1) {
            createAndAddCleanUp(configCluster, configInstantiate, releasesToCleanUp,"");
        } else {
            for (int i = numberCharts; i > 0; i--) {
                createAndAddCleanUp(configCluster, configInstantiate, releasesToCleanUp,"-" + i);
            }
        }
    }

}
