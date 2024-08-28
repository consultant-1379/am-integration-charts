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
package com.ericsson.evnfm.acceptance.models.configuration.testng;

import java.util.List;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;

public class ConfigUpgrade {

    private List<EvnfmCnf> cnfsToUpgrade;

    public List<EvnfmCnf> getCnfsToUpgrade() {
        return cnfsToUpgrade;
    }

    public void setCnfsToUpgrade(final List<EvnfmCnf> cnfsToUpgrade) {
        this.cnfsToUpgrade = cnfsToUpgrade;
    }
}
