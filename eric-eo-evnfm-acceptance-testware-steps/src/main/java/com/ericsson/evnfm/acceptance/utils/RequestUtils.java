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

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import io.fabric8.kubernetes.client.KubernetesClientException;

public class RequestUtils {
    private static final int MAXIMUM_ATTEMPTS = 3;
    private static final int CSAR_DOWNLOADING_MAXIMUM_ATTEMPTS = 5;
    private static final long RETRY_INTERVAL_IN_MILLISECONDS = 3_000L;
    private static final long CSAR_DOWNLOADING_RETRY_INTERVAL_IN_MILLISECONDS = 30_000L;

    public static RestTemplate getRestTemplate() {
        return new RestTemplateBuilder()
                .requestFactory(HttpComponentsClientHttpRequestFactory.class)
                .setBufferRequestBody(false)
                .build();
    }

    public static RetryTemplate getRestRetryTemplate() {
        return getRetryTemplate(getRestExceptionToRetry());
    }

    public static RetryTemplate getCsarDowloadingFromARMRetryTemplate() {
        return getCsarDowloadingRetryTemplate(getRestExceptionToRetry());
    }

    public static RetryTemplate getK8sRetryTemplate() {
        return getRetryTemplate(getK8sExceptionToRetry());
    }

    private static RetryTemplate getRetryTemplate(Map<Class<? extends Throwable>, Boolean> exceptionsToRetry) {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(MAXIMUM_ATTEMPTS, exceptionsToRetry);
        retryPolicy.setMaxAttempts(MAXIMUM_ATTEMPTS);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(RETRY_INTERVAL_IN_MILLISECONDS);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    private static RetryTemplate getCsarDowloadingRetryTemplate(Map<Class<? extends Throwable>, Boolean> exceptionsToRetry) {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(CSAR_DOWNLOADING_MAXIMUM_ATTEMPTS, exceptionsToRetry);
        retryPolicy.setMaxAttempts(CSAR_DOWNLOADING_MAXIMUM_ATTEMPTS);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(CSAR_DOWNLOADING_RETRY_INTERVAL_IN_MILLISECONDS);

        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    private static Map<Class<? extends Throwable>, Boolean> getRestExceptionToRetry() {
        Map<Class<? extends Throwable>, Boolean> result = new HashMap<>();
        result.put(HttpClientErrorException.Forbidden.class, true);
        result.put(HttpServerErrorException.InternalServerError.class, true);
        result.put(ResourceAccessException.class, true);
        result.put(SocketTimeoutException.class, true);
        return result;
    }

    private static Map<Class<? extends Throwable>, Boolean> getK8sExceptionToRetry() {
        Map<Class<? extends Throwable>, Boolean> result = new HashMap<>();
        result.put(KubernetesClientException.class, true);
        return result;
    }

    /**
     * Error handler to bypass throwing of exception on 4xx/5xx response
     */
    private static class SkipErrorHandling implements ResponseErrorHandler {
        @Override
        public boolean hasError(final ClientHttpResponse response) {
            return false;
        }

        @Override
        public void handleError(final ClientHttpResponse response) {
        }
    }
}
