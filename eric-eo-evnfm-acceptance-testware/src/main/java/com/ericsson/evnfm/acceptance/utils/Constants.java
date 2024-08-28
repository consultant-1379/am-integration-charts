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
package com.ericsson.evnfm.acceptance.utils;

public class Constants {
    private Constants() {
    }

    // Kubectl Commands
    public static final String KUBECONFIG = "--kubeconfig %s ";
    public static final String DEPLOYMENT = "deployment ";
    public static final String STATEFULSET = "statefulSet ";
    public static final String REPLICASET = "replicaset ";
    public static final String DELETE_NAMESPACE = "kubectl delete namespace %s " + KUBECONFIG;

    // Test tags
    public static final String PERFORMANCE = "performance";
    public static final String REGRESSION = "regression";
    public static final String REST = "rest";
    public static final String UI = "ui";
    public static final String CLUSTER = "cluster";
    public static final String ONBOARDING = "onboarding";
    public static final String DELETE_PACKAGES = "delete-packages";
    public static final String BUR = "bur";

    // Argument options
    public static final String TYPE_FLAG = "-t";
    public static final String PHASE_FLAG = "-p";
    public static final String FILE_FLAG = "-f";

    // Pod status
    public static final String RUNNING = "Running";
    public static final String TERMINATING = "Terminating";
    public static final String POD_LABEL_NAME = "app.kubernetes.io/name";

}
