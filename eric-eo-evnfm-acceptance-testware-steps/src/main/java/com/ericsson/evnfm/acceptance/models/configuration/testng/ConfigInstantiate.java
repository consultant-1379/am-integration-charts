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

public class ConfigInstantiate {

    private List<EvnfmCnf> cnfsToInstantiate;

    public List<EvnfmCnf> getCnfsToInstantiate() {
        return cnfsToInstantiate;
    }

    public void setCnfsToInstantiate(final List<EvnfmCnf> cnfsToInstantiate) {
        this.cnfsToInstantiate = cnfsToInstantiate;
    }
}
