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

import java.util.ArrayList;
import java.util.List;

public class EvnfmTestInfo {
    private List<String> tags = new ArrayList<>();
    private String type;
    private int phase;

    public void addTags(String tag) {
        this.tags.add(tag);
    }

    public List<String> getTags() {
        return tags;
    }

    public String getType() {
        return type.toLowerCase();
    }

    public int getPhase() {
        return phase;
    }

    public void setTags(final List<String> flag) {
        this.tags = flag;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setPhase(final String phase) {
        this.phase = Integer.parseInt(phase);
    }

    public EvnfmTestInfo() {
    }
}