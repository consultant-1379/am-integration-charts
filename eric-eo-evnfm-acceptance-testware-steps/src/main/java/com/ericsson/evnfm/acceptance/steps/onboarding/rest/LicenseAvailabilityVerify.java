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

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ClusterConfig;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;
import com.ericsson.evnfm.acceptance.utils.ProcessExecutor;
import com.fasterxml.jackson.core.type.TypeReference;

public class LicenseAvailabilityVerify {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseAvailabilityVerify.class);

    private static final String LICENSE_PERMISSIONS_URL = "http://eric-eo-lm-consumer/lc/v1/cvnfm/permissions";
    private static final int DEFAULT_LICENSE_AVAILABILITY_TIMEOUT = 300;
    private static final Set<String> EXPECTED_LICENSES = Set.of("onboarding", "lcm_operations", "enm_integration", "cluster_management");

    private static final ProcessExecutor executor = new ProcessExecutor();

    private LicenseAvailabilityVerify() {
    }

    public static void verifyLicensesAvailable(final ClusterConfig config) throws InterruptedException {
        LOGGER.info("Waiting for the licenses to become available");
        StopWatch stopwatch = StopWatch.createStarted();
        while (stopwatch.getTime(TimeUnit.SECONDS) < DEFAULT_LICENSE_AVAILABILITY_TIMEOUT) {
            Set<String> grantedPermissions = getGrantedPermissionsFromLicenseConsumer(config);
            LOGGER.info("List of granted permissions from License Consumer {}", grantedPermissions);
            if (grantedPermissions.isEmpty() || !grantedPermissions.containsAll(EXPECTED_LICENSES)) {
                LOGGER.info("Licenses are not available yet, continue to wait and retry");
            } else {
                LOGGER.info("License are already available");
                return;
            }
            TimeUnit.SECONDS.sleep(2);
        }
        fail(String.format("Licenses did not become available in the provided time: %s seconds", DEFAULT_LICENSE_AVAILABILITY_TIMEOUT));
    }

    private static Set<String> getGrantedPermissionsFromLicenseConsumer(final ClusterConfig config) {
        String command = String.format("kubectl --kubeconfig %s -n %s exec -i"
                                               + " $(kubectl --kubeconfig %s -n %s get pods | grep onboarding | awk '{print $1}')"
                                               + " -c eric-am-onboarding-service"
                                               + " --"
                                               + " /bin/bash -c 'curl -s %s'",
                                       config.getLocalPath(),
                                       EVNFM_INSTANCE.getNamespace(),
                                       config.getLocalPath(),
                                       EVNFM_INSTANCE.getNamespace(),
                                       LICENSE_PERMISSIONS_URL);
        try {
            ProcessExecutorResponse response = executor.executeProcess(command, 30, false);

            if (response.getExitValue() != 0) {
                return Set.of();
            }

            return getObjectMapper().readValue(response.getCommandOutput(), new TypeReference<>() {
            });
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Failed to get granted permissions from License Consumer", e);
            return Set.of();
        }
    }
}
