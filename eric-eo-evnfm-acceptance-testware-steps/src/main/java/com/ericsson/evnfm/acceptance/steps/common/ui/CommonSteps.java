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
package com.ericsson.evnfm.acceptance.steps.common.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.EvnfmCnf;
import com.ericsson.evnfm.acceptance.models.UI.page.ComponentsTab;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourceDetailsPage;
import com.ericsson.evnfm.acceptance.models.UI.page.ResourcesPage;

public class CommonSteps {
    private CommonSteps() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonSteps.class);

    public static void verifyComponents(RemoteWebDriver driver, EvnfmCnf evnfmCnf) {
        LOGGER.info("Opening Components tab in Resource Details for:: {}", evnfmCnf.getVnfInstanceName());
        ResourcesPage resourcesPage = new ResourcesPage();
        resourcesPage.verifyResourcesPageIsOpened(driver);
        ResourceDetailsPage resourceDetailsPage = resourcesPage.openResourceDetailsPage(driver, evnfmCnf.getVnfInstanceName(),
                                                                                        evnfmCnf.getCluster().getUIName(),
                                                                                        evnfmCnf.getApplicationTimeout());
        resourceDetailsPage.verifyDetailsPageIsOpened(driver);
        ComponentsTab componentsTab = resourceDetailsPage.openComponentsTab(driver);
        componentsTab.verifyComponentsTabIsOpened(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10L));
        int totalPods = evnfmCnf.getTargets().values().stream().mapToInt(Integer::intValue).sum();
        LOGGER.info("Total number of pods : " + totalPods);

        List<String> components = wait
                .until(item -> componentsTab.getAllComponents(driver, 10, totalPods))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase("Running"))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        assertThat(components).hasSize(totalPods);

        components.forEach(component -> assertThat(component)
                .matches(pod -> evnfmCnf.getTargets().keySet().stream().anyMatch(pod::contains),
                         String.format("%s does not match anything from targets :: %s", component, evnfmCnf.getTargets().keySet())));
    }
}
