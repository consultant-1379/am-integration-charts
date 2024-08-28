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
package com.ericsson.evnfm.acceptance.steps.certificates;

import com.ericsson.evnfm.acceptance.models.TrustedCertificateManagementDetails;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesRequest;
import com.ericsson.evnfm.acceptance.models.TrustedCertificatesResponse;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.evnfm.acceptance.steps.common.rest.LoginSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static com.ericsson.evnfm.acceptance.utils.Constants.OPERATION_RESPONSE_STATUS_LOG;
import static com.ericsson.evnfm.acceptance.utils.Constants.TRUSTED_CERTIFICATES_ENDPOINT;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestRetryTemplate;
import static com.ericsson.evnfm.acceptance.utils.RequestUtils.getRestTemplate;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

public class CertificateManagementApiClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateManagementApiClient.class);

    public static ResponseEntity<TrustedCertificatesResponse> listTrustedCertificates(final String certificatesList, final User user) {
        String trustedCertificatesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(TRUSTED_CERTIFICATES_ENDPOINT, certificatesList);

        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<TrustedCertificatesResponse> certificatesResponse = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(trustedCertificatesUrl, HttpMethod.GET, httpEntity, TrustedCertificatesResponse.class));
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, "Get list of certificates", certificatesResponse.getStatusCode());

        return certificatesResponse;
    }

    public static ResponseEntity<TrustedCertificateManagementDetails> installTrustedCertificates(
            final String certificatesList, final TrustedCertificatesRequest certificatesRequest, final User user) {
        final String trustedCertificatesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(TRUSTED_CERTIFICATES_ENDPOINT, certificatesList);

        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final HttpEntity<TrustedCertificatesRequest> requestHttpEntity = new HttpEntity<>(certificatesRequest, httpHeaders);

        final ResponseEntity<TrustedCertificateManagementDetails> certificatesResponse = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(trustedCertificatesUrl, HttpMethod.PUT, requestHttpEntity, TrustedCertificateManagementDetails.class));
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, "Install certificates", certificatesResponse.getStatusCode());
        return certificatesResponse;
    }

    public static ResponseEntity<TrustedCertificateManagementDetails> deleteTrustedCertificates(final String certificatesList, final User user) {
        final String trustedCertificatesUrl = EVNFM_INSTANCE.getEvnfmUrl() + String.format(TRUSTED_CERTIFICATES_ENDPOINT, certificatesList);

        HttpHeaders httpHeaders = LoginSteps.getHttpHeadersWithToken(user);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<TrustedCertificateManagementDetails> certificatesDetails = getRestRetryTemplate().execute(
                context -> getRestTemplate().exchange(trustedCertificatesUrl, HttpMethod.DELETE, httpEntity, TrustedCertificateManagementDetails.class));
        LOGGER.info(OPERATION_RESPONSE_STATUS_LOG, "Delete certificates", certificatesDetails.getStatusCode());
        return certificatesDetails;
    }
}
