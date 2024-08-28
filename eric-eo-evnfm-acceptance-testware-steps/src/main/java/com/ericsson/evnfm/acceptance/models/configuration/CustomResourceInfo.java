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

import java.util.HashMap;
import java.util.Map;

public class CustomResourceInfo {
    private String crdYamlPath = "src/main/resources/crd/vnfinstanceresponses-crd-v1.yaml";
    private Map<String, Object> crMetadata;

    public CustomResourceInfo() {
    }

    public CustomResourceInfo(final String crdYamlPath, final Map<String, Object> crMetadata) {
        this.crdYamlPath = crdYamlPath;
        this.crMetadata = crMetadata;
    }

    public String getCrdYamlPath() {
        return crdYamlPath;
    }

    public void setCrdYamlPath(final String crdYamlPath) {
        this.crdYamlPath = crdYamlPath;
    }

    public Map<String, Object> getCrMetadata() {
        if (crMetadata == null || crMetadata.isEmpty()) {
            crMetadata = new HashMap<>();
            crMetadata.put("name", "vir.phase1.evnfm.test.e2e.bur");
        } else if(!crMetadata.containsKey("name")) {
            crMetadata.put("name", "vir.phase1.evnfm.test.e2e.bur");
        }
        return crMetadata;
    }

    public void setCrMetadata(final Map<String, Object> crMetadata) {
        this.crMetadata = crMetadata;
    }
}
