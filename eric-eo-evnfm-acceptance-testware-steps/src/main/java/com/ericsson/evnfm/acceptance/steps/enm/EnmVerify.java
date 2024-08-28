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
package com.ericsson.evnfm.acceptance.steps.enm;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.evnfm.acceptance.steps.backup.rest.BackupAPIClient.queryVnfInstanceId;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonRequests.getResourceByVnfInstanceId;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.User;
import com.ericsson.vnfm.orchestrator.model.ResourceResponse;

public final class EnmVerify {

    private EnmVerify() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EnmVerify.class);

    @SuppressWarnings("unchecked")
    public static void verifyInstantiateOssParameters(EvnfmCnf evnfmCnf, String vnfInstanceId, User user) {
        ResourceResponse response = getResourceByVnfInstanceId(EVNFM_INSTANCE.getEvnfmUrl(), vnfInstanceId, user);
        Map<String, String> instantiateOssTopology = evnfmCnf.getInstantiateOssTopology();
        Map<String, Map<String, String>> propertiesModels = (Map<String, Map<String, String>>) response.getInstantiateOssTopology();
        for (Map.Entry<String, Map<String, String>> property : propertiesModels.entrySet()) {
            String expectedValue = property.getValue().get("defaultValue");
            String expectedKey = property.getKey();
            String actualValue = instantiateOssTopology.get(expectedKey);
            if (expectedKey.equals("networkElementPassword")) {
                String encodedOssTopologyPassword = Base64.getEncoder().encodeToString(actualValue.getBytes(StandardCharsets.UTF_8));
                assertThat(expectedValue).isEqualTo(encodedOssTopologyPassword);
            } else {
                assertThat(expectedValue).isEqualTo(actualValue);
            }
        }
        LOGGER.info("Verified Instantiate oss topology");
    }

    public static void verifyAddedToOss(EvnfmCnf evnfmCnf, User user, boolean addedToOss) {
        String vnfInstanceId = queryVnfInstanceId(evnfmCnf.getVnfInstanceName(), user);
        ResourceResponse response = getResourceByVnfInstanceId(EVNFM_INSTANCE.getEvnfmUrl(), vnfInstanceId, user);
        assertThat(response.getAddedToOss())
                .withFailMessage("Expected that %s is added to OSS: %s".formatted(evnfmCnf.getVnfInstanceName(), addedToOss))
                .isEqualTo(addedToOss);
        LOGGER.info("Verified addedToOss : {}", addedToOss);
    }
}
