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
package com.ericsson.evnfm.acceptance.models;

import com.ericsson.vnfm.orchestrator.model.URILink;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class VnfInstanceResponseLegacyLinks extends VnfInstanceResponseLinks {

    public VnfInstanceResponseLegacyLinks() {
    }

    /* Automatically maps change_package_info link to change_vnfpkg link if exists to be backward compatible*/
    @JsonAnySetter
    public void changeLink(String linkName, URILink url) {
        if (linkName.equals("change_package_info")) {
            setChangeVnfpkg(url);
        }
    }
}
