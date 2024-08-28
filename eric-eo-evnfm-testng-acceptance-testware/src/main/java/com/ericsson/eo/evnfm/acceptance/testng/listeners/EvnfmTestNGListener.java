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
package com.ericsson.eo.evnfm.acceptance.testng.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class EvnfmTestNGListener implements IInvokedMethodListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvnfmTestNGListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        LOGGER.info("Entering test Class:: {}, Method:: {}",
                method.getTestMethod().getTestClass().getName(),
                method.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        LOGGER.info("Exiting test Class:: {}, Method:: {}, Passed :: {}",
                method.getTestMethod().getTestClass().getName(),
                method.getTestMethod().getMethodName(),
                method.getTestResult().isSuccess());
        if (testResult.getThrowable() != null) {
            String message = String.format("Exception occurred in method :: %s", testResult.getMethod().getMethodName());
            LOGGER.error(message, testResult.getThrowable());
        }
    }
}
