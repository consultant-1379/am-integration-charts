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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigParent {

    @JsonProperty("general")
    private ConfigGeneral configGeneral;

    @JsonProperty("onboarding")
    private ConfigOnboarding configOnboarding;

    @JsonProperty("instantiate")
    private ConfigInstantiate configInstantiate;

    @JsonProperty("upgrade")
    private ConfigUpgrade configUpgrade;

    @JsonProperty("rollback")
    private ConfigRollback configRollback;

    @JsonProperty("scale")
    private ConfigScale configScale;

    @JsonProperty("terminate")
    private ConfigTerminate configTerminate;

    @JsonProperty("cluster")
    private ConfigCluster configCluster;

    @JsonProperty("customresource")
    private ConfigCustomResource configCustomResource;

    public ConfigGeneral getConfigGeneral() {
        return configGeneral;
    }

    public void setConfigGeneral(ConfigGeneral configGeneral) {
        this.configGeneral = configGeneral;
    }

    public ConfigOnboarding getConfigOnboarding() {
        return configOnboarding;
    }

    public void setConfigOnboarding(ConfigOnboarding configOnboarding) {
        this.configOnboarding = configOnboarding;
    }

    public ConfigInstantiate getConfigInstantiate() {
        return configInstantiate;
    }

    public void setConfigInstantiate(ConfigInstantiate configInstantiate) {
        this.configInstantiate = configInstantiate;
    }

    public ConfigUpgrade getConfigUpgrade() {
        return configUpgrade;
    }

    public ConfigRollback getConfigRollback() {
        return configRollback;
    }

    public void setConfigUpgrade(ConfigUpgrade configUpgrade) {
        this.configUpgrade = configUpgrade;
    }

    public ConfigTerminate getConfigTerminate() {
        return configTerminate;
    }

    public void setConfigTerminate(ConfigTerminate configTerminate) {
        this.configTerminate = configTerminate;
    }

    public ConfigCluster getConfigCluster() {
        return configCluster;
    }

    public void setConfigCluster(ConfigCluster configCluster) {
        this.configCluster = configCluster;
    }

    public ConfigScale getConfigScale() {
        return configScale;
    }

    public void setConfigScale(final ConfigScale configScale) {
        this.configScale = configScale;
    }

    public ConfigCustomResource getConfigCustomResource() {
        return configCustomResource;
    }

    public void setConfigCustomResource(final ConfigCustomResource configCustomResource) {
        this.configCustomResource = configCustomResource;
    }
}
