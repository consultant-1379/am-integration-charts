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
package com.ericsson.eo.evnfm.acceptance.testng.tests.rest.onboarding;

import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.createPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.deleteOnboardedPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listDockerImageByNameAndTag;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listOnboardedDockerImages;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listOnboardedHelmCharts;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listOnboardedHelmChartsByName;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listOnboardedVnfPackageByName;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.listOnboardedVnfPackages;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.onboardPackage;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.validateTextVnfd;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingSteps.validateZipVnfd;
import static com.ericsson.evnfm.acceptance.steps.onboarding.rest.OnboardingVerify.verifyHealthcheck;

import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.TCDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.RestBase;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;

public class OnboardingTests extends RestBase {
    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_01/version/1.1
     */
    @Test(description = "EVNFM_OB_01 : It shall be possible to onboard a CNF CSAR package",
            dataProvider = "getOnboardingConfig",
            dataProviderClass = TCDataProviders.class)
    public void testOnboardCNFPackage(EvnfmBasePackage evnfmPackage) {
        verifyHealthcheck(240, user);
        String filename = evnfmPackage.getPackageName().substring(evnfmPackage.getPackageName().lastIndexOf("/") + 1);
        String packageId = createPackage(evnfmPackage.getTimeOut(), evnfmPackage.isSkipImageUpload(), user, filename);
        onboardPackage(evnfmPackage, packageId, user, false);
        evnfmPackage.setOnboardedId(packageId);
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_02/version/1.1
     */
    @Test(description = "EVNFM_OB_02 : View the details of all onboarded CSAR Packages",
            dataProvider = "getOnboardedPackages",
            dataProviderClass = TCDataProviders.class)
    public void testViewDetailsOfAllOnboardedPackages(EvnfmBasePackage onboardedPackage) {
        listOnboardedVnfPackages(onboardedPackage, user);
        listOnboardedVnfPackageByName(onboardedPackage, user);
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_04/version/1.1
     */
    @Test(description = "EVNFM_OB_04 : List onboarded helm charts from registry",
            dataProvider = "getOnboardedPackages",
            dataProviderClass = TCDataProviders.class)
    public void testListOnboardedHelmCharts(EvnfmBasePackage evnfmPackage) {
        listOnboardedHelmCharts(evnfmPackage, user);
        listOnboardedHelmChartsByName(evnfmPackage, user);
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_05/version/1.1
     */
    @Test(description = "EVNFM_OB_05 : List onboarded docker images from registry",
            dataProvider = "getOnboardedPackages",
            dataProviderClass = TCDataProviders.class)
    public void testListOnboardedDockerImages(EvnfmBasePackage evnfmBasePackage) {
        if (!evnfmBasePackage.isSkipImageUpload()) {
            listOnboardedDockerImages(evnfmBasePackage, user);
            listDockerImageByNameAndTag(evnfmBasePackage, user);
        }
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_06/version/1.0
     */
    @Test(description = "EVNFM_OB_06 : Retrieve the VNFD of an onboarded CSAR from the DB",
            dataProvider = "getOnboardedPackages",
            dataProviderClass = TCDataProviders.class)
    public void testRetrieveVnfdOfOnboardedPackages(EvnfmBasePackage evnfmBasePackage) {
        validateTextVnfd(evnfmBasePackage, user);
        validateZipVnfd(evnfmBasePackage, user);
    }

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_03/version/1.2
     */
    @Test(description = "EVNFM_OB_03 : Delete a CSAR Package",
            dataProvider = "getOnboardedPackages",
            dataProviderClass = TCDataProviders.class)
    public void testDeleteCsarPackage(EvnfmBasePackage evnfmPackage) {
        String packageId = evnfmPackage.getOnboardedId();
        String vnfdId = evnfmPackage.getVnfdId();
        deleteOnboardedPackage(packageId,vnfdId, user);
    }
}
