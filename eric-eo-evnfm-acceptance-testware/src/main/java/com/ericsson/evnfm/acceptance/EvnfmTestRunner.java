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
package com.ericsson.evnfm.acceptance;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import static com.ericsson.evnfm.acceptance.utils.Constants.BUR;
import static com.ericsson.evnfm.acceptance.utils.Constants.CLUSTER;
import static com.ericsson.evnfm.acceptance.utils.Constants.DELETE_PACKAGES;
import static com.ericsson.evnfm.acceptance.utils.Constants.FILE_FLAG;
import static com.ericsson.evnfm.acceptance.utils.Constants.ONBOARDING;
import static com.ericsson.evnfm.acceptance.utils.Constants.PERFORMANCE;
import static com.ericsson.evnfm.acceptance.utils.Constants.PHASE_FLAG;
import static com.ericsson.evnfm.acceptance.utils.Constants.REGRESSION;
import static com.ericsson.evnfm.acceptance.utils.Constants.REST;
import static com.ericsson.evnfm.acceptance.utils.Constants.TYPE_FLAG;
import static com.ericsson.evnfm.acceptance.utils.Constants.UI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.models.configuration.EvnfmTestInfo;

public class EvnfmTestRunner {

    public static void main(String[] args) throws IOException {
        LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("com.ericsson.evnfm.acceptance.tests")
                );
        EvnfmTestInfo testInfo = getTestInfo(args);
        ConfigurationProvider.setTestInfo(testInfo);

        System.out.println("Test type will be executed: " + testInfo.getType());
        if (testInfo.getType().equalsIgnoreCase(BUR)){
            System.out.printf("Phase %s will be executed.%n", testInfo.getPhase());
        }
        System.out.println("Test tags will be executed: " + Arrays.toString(testInfo.getTags().toArray()));
        requestBuilder.filters(TagFilter.includeTags(testInfo.getTags()));
        LauncherDiscoveryRequest request = requestBuilder.build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        System.out.println("TEST INFO");
        System.out.println("---------");
        System.out.println("Phase: " + testInfo.getPhase());
        System.out.println("Test type: " + testInfo.getType());
        for(String tag : testInfo.getTags()) {
            System.out.println("Tags: " + tag);
        }
        System.out.println("---------");

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("");
        System.out.println("----------E-VNFM Test Summary---------------");
        System.out.println("Total tests executed: " + summary.getTestsFoundCount());
        System.out.println("Total tests passed: " + summary.getTestsSucceededCount());
        System.out.println("Total tests failed: " + summary.getTestsFailedCount());
        System.out.println("Total tests skipped: " + summary.getTestsSkippedCount());
        System.out.println("--------------------------------------------");

        if (!summary.getFailures().isEmpty()) {
            Map<String, List<String>> tests = new HashMap<>();
            getTestFailures(summary, tests);
            printTestFailures(tests);

            System.out.println("");
            System.out.println("Failed tests exceptions:");
            summary.getFailures()
                    .forEach(failure -> System.out.println(
                            "Failure: " + failure.getException() + System.lineSeparator() + " stacktrace " + ExceptionUtils
                                    .getStackTrace(failure.getException())));
        }

        if (summary.getTestsFoundCount() == 0 || summary.getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }

    private static void printTestFailures(Map<String, List<String>> tests) {
        System.out.println("");
        System.out.println("Failed tests:");
        tests.forEach((key, testMethods) -> {
            String[] className = key.split("\\.");
            String parentName = className[className.length - 1];
            System.out.println("\t" + parentName);
            AtomicInteger count = new AtomicInteger(1);
            testMethods.forEach(methodName -> {
                System.out.println("\t\t" + count + ". " + methodName);
                count.getAndIncrement();
            });
        });
    }

    private static void getTestFailures(TestExecutionSummary summary, Map<String, List<String>> tests) {
        summary.getFailures()
                .forEach(failure -> {
                    if (failure != null && failure.getTestIdentifier() != null && failure.getTestIdentifier().getSource().isPresent()) {
                        TestSource testSource = failure.getTestIdentifier().getSource().get();

                        if (testSource instanceof MethodSource) {
                            MethodSource methodSource = (MethodSource) testSource;

                            Optional<String> first = tests.keySet().stream().filter(item -> methodSource.getClassName().equals(item)).findFirst();
                            if (!first.isPresent()) {
                                List<String> methods = new ArrayList<>();
                                String methodName = methodSource.getMethodName();
                                methods.add(methodName);
                                tests.put(methodSource.getClassName(), methods);
                            }
                            String methodName = methodSource.getMethodName();
                            if (first.isPresent()) {
                                String className = first.get();
                                List<String> testMethods = tests.get(className);
                                if (!testMethods.contains(methodName)) {
                                    testMethods.add(methodName);
                                    tests.put(className, testMethods);
                                }
                            }
                        }
                    }
                });
    }

    private static EvnfmTestInfo getTestInfo(final String[] args) throws IOException {
        EvnfmTestInfo testInfo = new EvnfmTestInfo();
        List<String> commonTests = Arrays.asList(CLUSTER, ONBOARDING, DELETE_PACKAGES);

        checkForInvalidArguments(args);

        //default flow without options.
        if (isRegression(args)) {
            ConfigurationProvider.setConfiguration(getConfigFile(args));
            testInfo.getTags().addAll(commonTests);
            testInfo.getTags().addAll(Arrays.asList(REST, UI));
            testInfo.setType(REGRESSION);
            return testInfo;
        }

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                String flag = args[i];
                String value = args[i + 1];
                switch (flag) {
                    case FILE_FLAG:
                        try {
                            ConfigurationProvider.setConfiguration(value);
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Invalid configuration file.", e);
                        }
                        break;
                    case TYPE_FLAG:
                        if (value.equalsIgnoreCase(PERFORMANCE)) {
                            testInfo.addTags(value);
                            testInfo.setType(PERFORMANCE);
                            return testInfo;
                        }
                        testInfo.setType(value);
                        testInfo.getTags().addAll(commonTests);
                        testInfo.addTags(value);
                        break;
                    case PHASE_FLAG:
                        setPhase(testInfo, value);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid option flag: " + flag + ", available options are: -f, -t, -p");
                }
            }
        }

        return testInfo;
    }

    private static void setPhase(final EvnfmTestInfo testInfo, final String value) {
        testInfo.setPhase(value);
        if (testInfo.getPhase() == 1) {
            testInfo.getTags().remove(DELETE_PACKAGES);
        }
    }

    private static boolean isRegression(final String[] args) {
        return isInArguments(args, REGRESSION) || args.length == 2;
    }

    private static String getConfigFile(String[] args) {
        List<String> argumentsList = Arrays.asList(args);
        return argumentsList.get(argumentsList.indexOf(FILE_FLAG) + 1);
    }

    private static void checkForInvalidArguments(final String[] args) {
        if (args.length % 2 == 1 || args.length < 2) {
            System.out.println("Number of provided args is " + args.length);
            throw new IllegalArgumentException("Number of provided args is incorrect: " + args.length + ". Must be odd and greater than 1");
        }

        if (!isInArguments(args, FILE_FLAG)) {
            throw new IllegalArgumentException("Missing the configuration option: '-f configuration.json'");
        }

        if (isInArguments(args, BUR) && !isInArguments(args, "-p")) {
            throw new IllegalArgumentException("Provide a phase number with '-p' flag (1 or 2). Usage: '-p 1'");
        }
    }

    private static boolean isInArguments(final String[] args, String... argument) {
        return Arrays.asList(args).containsAll(Arrays.asList(argument));
    }
}
