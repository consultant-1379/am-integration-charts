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

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

import static com.ericsson.evnfm.acceptance.common.TestwareConstants.FILE_FLAG;

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
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import com.ericsson.evnfm.acceptance.common.ConfigurationProvider;
import com.ericsson.evnfm.acceptance.tests.EndToEndAcceptanceTest;

public class EvnfmTestRunner {

    public static void main(String[] args) throws IOException {
        checkForInvalidArguments(args);
        ConfigurationProvider.setConfiguration(getConfigFile(args));
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(
                        selectPackage("com.ericsson.evnfm.acceptance.tests"),
                        selectClass(EndToEndAcceptanceTest.class)
                )
                .build();

        Launcher launcher = LauncherFactory.create();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);

        TestExecutionSummary summary = listener.getSummary();
        System.out.println("");
        System.out.println("----------E-VNFM Internal Test Summary--------");
        System.out.println("Total tests executed: " + summary.getTestsFoundCount());
        System.out.println("Total tests passed: " + summary.getTestsSucceededCount());
        System.out.println("Total tests failed: " + summary.getTestsFailedCount());
        System.out.println("Total tests skipped: " + summary.getTestsSkippedCount());
        System.out.println("----------------------------------------------");

        if (!summary.getFailures().isEmpty()) {
            Map<String, List<String>> tests = new HashMap<>();
            getTestFailures(summary, tests);
            printTestFailures(tests);

            System.out.println("");
            System.out.println("Failed tests exceptions:");
            summary.getFailures()
                    .forEach(failure -> {
                        System.out.println("Failure: " + failure.getException() + System.lineSeparator() + " stacktrace " + ExceptionUtils
                                .getStackTrace(failure.getException()));
                    });
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

    private static String getConfigFile(String[] args) {
        List<String> argumentsList = Arrays.asList(args);
        return argumentsList.get(argumentsList.indexOf(FILE_FLAG) + 1);
    }

    private static void checkForInvalidArguments(final String[] args) {
        if (args.length % 2 == 1 || args.length < 2) {
            throw new IllegalArgumentException("Missing argument or invalid options");
        }

        if (!isInArguments(args, FILE_FLAG)) {
            throw new IllegalArgumentException("Missing the configuration option: '-f configuration.json'");
        }
    }

    private static boolean isInArguments(final String[] args, String... argument) {
        return Arrays.asList(args).containsAll(Arrays.asList(argument));
    }
}
