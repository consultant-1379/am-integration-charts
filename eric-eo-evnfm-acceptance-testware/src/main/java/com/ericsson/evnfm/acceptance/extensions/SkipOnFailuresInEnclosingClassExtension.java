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
package com.ericsson.evnfm.acceptance.extensions;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.ModifierSupport.isNotStatic;

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestWatcher;

public class SkipOnFailuresInEnclosingClassExtension implements TestWatcher, ExecutionCondition {

    private static boolean isInnerClass(Class<?> clazz) {
        return isNotStatic(clazz) && clazz.isMemberClass();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        // Track failures by test class name instead of test Class in order to
        // avoid holding onto Class references in the root ExecutionContext.Store
        // for the duration of the test suite.
        getStore(context).put(context.getRequiredTestClass().getName(), true);
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        if (isInnerClass(testClass) && isAnnotated(testClass, SkipOnFailuresInEnclosingClass.class)) {
            ExtensionContext.Store store = getStore(context);
            String enclosingClassName = testClass.getDeclaringClass().getName();
            boolean failureRecordedInEnclosingClass = getOrElse(store, enclosingClassName, boolean.class, false);
            if (failureRecordedInEnclosingClass) {
                return disabled("Failures detected in enclosing test class: " + enclosingClassName);
            }
        }
        return enabled("No failing tests in enclosing class");
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(Namespace.create(getClass()));
    }

    private <V> V getOrElse(Store store, Object key, Class<V> requiredType, V defaultValue) {
        V value = store.get(key, requiredType);
        return (value != null ? value : defaultValue);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        // ignored event
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        // ignored event
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        // ignored event
    }
}
