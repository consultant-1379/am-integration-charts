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

import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponse;
import com.ericsson.vnfm.orchestrator.model.VnfInstanceResponseLinks;

public class VnfInstanceLegacyResponse extends VnfInstanceResponse {

    public VnfInstanceLegacyResponse() {
    }

    @Override
    public VnfInstanceResponseLinks getLinks() {
        return super.getLinks();
    }

    public void setLinks(final VnfInstanceResponseLegacyLinks links) {
        super.setLinks(links);
    }
}
