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
package com.ericsson.eo.evnfm.acceptance.testng.tests.ui.lcm.onboarding;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Map;

import org.testng.annotations.Test;

import com.ericsson.eo.evnfm.acceptance.testng.dataprovider.e2e.BasicOnboardingUIDataProviders;
import com.ericsson.eo.evnfm.acceptance.testng.infrastructure.OnboardingUiBase;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.steps.common.ui.AuthenticationStep;
import com.ericsson.evnfm.acceptance.steps.onboarding.ui.OnboardUISteps;

public class OnboardUITest extends OnboardingUiBase {

    /**
     * TC description: https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_OB_07/version/0.1
     */

    @Test(description = "EVNFM_OB_07 : Onboard a package via the UI",
            dataProvider = "getPackagesToOnboard",
            dataProviderClass = BasicOnboardingUIDataProviders.class)
    public void testOnboardCNFPackageViaUI(EvnfmBasePackage packageToOnboard) {
        AuthenticationStep.login(driver, user.getUsername(), user.getPassword(), EVNFM_INSTANCE.getEvnfmUrl());
        OnboardUISteps onboardUISteps = new OnboardUISteps();
        onboardUISteps.onboardPackageUI(driver, packageToOnboard);
        /**
         * TC : https://taftm.seli.wh.rnd.internal.ericsson.com/#tm/viewTC/EVNFM_UI_TOSCA_O_3/version/0.1
         * description: EVNFM_UI_TOSCA_O_3 : Verify supported operations TAB
         */
        final Map<String, String> expectedOperationStatus = packageToOnboard.getSupportedOperations();
        final Map<String, String> actualSupportedOperationsStatus = onboardUISteps.getSupportedOperationsStatus(driver, user, packageToOnboard);
        onboardUISteps.verifySupportedOperationsStatus(expectedOperationStatus, actualSupportedOperationsStatus);

        /**
         * description: delete a package via the UI
         */
        onboardUISteps.deletePackageUI(driver, user, packageToOnboard);
        AuthenticationStep.logout(driver);
    }
}
