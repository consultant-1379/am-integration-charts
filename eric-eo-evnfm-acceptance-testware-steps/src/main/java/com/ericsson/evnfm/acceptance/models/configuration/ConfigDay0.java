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
package com.ericsson.evnfm.acceptance.models.configuration;

import java.util.List;
import java.util.Map;

public class ConfigDay0 {
    private List<SecretData> secrets;

    public List<SecretData> getSecrets() {
        return secrets;
    }

    public void setSecrets(final List<SecretData> secrets) {
        this.secrets = secrets;
    }

    public static class SecretData {
        private String name;
        private Map<String, String> data;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }
}
