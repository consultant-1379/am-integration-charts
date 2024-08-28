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

import static org.slf4j.LoggerFactory.getLogger;

import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.util.Arrays;

import org.assertj.core.util.Strings;
import org.slf4j.Logger;

public final class HelmCommand {
    // Commands
    public static final String SPACE = " ";
    public static final String HELM = "helm";
    public static final String HELM3 = "helm3";
    public static final String HELM_GET_VALUES = "get values %s";
    public static final String HELM_GET_MANIFEST = "get manifest %s";
    public static final String HELM_HISTORY = "history %s";
    public static final String HELM_LIST = "list";
    public static final String HELM_INSTALL = "install %s %s";
    public static final String HELM_UPGRADE = "upgrade %s %s";
    public static final String HELM_UNINSTALL = "uninstall %s";
    public static final String HELM_REPO_ADD = "repo add %s %s";
    public static final String HELM_REPO_REMOVE = "repo remove %s";
    public static final String HELM_REPO_UPDATE = "repo update";
    //Arguments
    public static final String TIMEOUT_ARG = "--timeout %ss";
    public static final String VERSION_ARG = "--version %s";
    public static final String NAMESPACE_ARG = "--namespace %s";
    public static final String VALUES_ARG = "--values %s";
    public static final String KUBE_CONFIG = "--kubeconfig %s";
    public static final String OUTPUT_ARG = "-o %s"; // Allowed values: table, json, yaml (default table)
    public static final String ALL_ARG = "--all";
    //Flags
    public static final String DEBUG_FLAG = "--debug";
    public static final String DEVEL_FLAG = "--devel";
    public static final String WAIT_FLAG = "--wait";
    public static final String IGNORE_ERROR = "|| true";
    public static final String REUSE_VALUES_FLAG = "--reuse-values";
    public static final String SET_FLAG  = "--set";

    private final StringBuilder command;

    public HelmCommand() {
        this.command = new StringBuilder(EVNFM_INSTANCE.getHelm());
    }

    public HelmCommand(String helm) {
        this.command = new StringBuilder(helm);
    }

    public HelmCommandBuilder getValues(final String release) {
        addArgumentWithValue(this.command, HELM_GET_VALUES, release);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder getManifest(final String release) {
        addArgumentWithValue(this.command, HELM_GET_MANIFEST, release);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder list() {
        addArgumentWithoutValue(this.command, HELM_LIST);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder history(final String release) {
        addArgumentWithValue(this.command, HELM_HISTORY, release);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder addRepo(final String name, final String url) {
        addArgumentWithValue(this.command, HELM_REPO_ADD, name, url);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder removeRepo(final String name) {
        addArgumentWithValue(this.command, HELM_REPO_REMOVE, name);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder updateRepo() {
        addArgumentWithValue(this.command, HELM_REPO_UPDATE);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder install(final String release, final String chart) {
        addArgumentWithValue(this.command, HELM_INSTALL, release, chart);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder upgrade(final String release, final String chart) {
        addArgumentWithValue(this.command, HELM_UPGRADE, release, chart);
        return new HelmCommandBuilder(this.command);
    }

    public HelmCommandBuilder uninstall(final String release) {
        addArgumentWithValue(this.command, HELM_UNINSTALL, release);
        return new HelmCommandBuilder(this.command);
    }

    private static void addArgumentWithValue(StringBuilder command, String argument, Object... values) {
        if (Arrays.stream(values).noneMatch(value -> Strings.isNullOrEmpty(String.valueOf(value)))) {
            command.append(SPACE).append(String.join(SPACE, String.format(argument, values)));
        }
    }

    private static void addArgument(StringBuilder command, String argument, String values) {
            command.append(SPACE).append(argument).append(SPACE).append(values);
    }

    private static void addArgumentWithoutValue(StringBuilder command, String flag) {
        command.append(SPACE).append(String.join(SPACE, flag));
    }

    public enum OutputTypeEnum {
        JSON("json"),
        TABLE("table"),
        YAML("yaml");

        private final String type;

        OutputTypeEnum(final String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static class HelmCommandBuilder {
        private static final Logger LOGGER = getLogger(HelmCommandBuilder.class);
        private static final String COMMAND_TO_EXECUTE = "Command to execute: {}";
        private final StringBuilder command;

        public HelmCommandBuilder(StringBuilder command) {
            this.command = command;
        }

        public HelmCommandBuilder namespace(final String namespace) {
            addArgumentWithValue(this.command, NAMESPACE_ARG, namespace);
            return this;
        }

        public HelmCommandBuilder output(final OutputTypeEnum outputType) {
            addArgumentWithValue(this.command, OUTPUT_ARG, outputType.getType());
            return this;
        }

        public HelmCommandBuilder allValues() {
            addArgumentWithoutValue(this.command, ALL_ARG);
            return this;
        }

        public HelmCommandBuilder kubeconfig(final String kubeconfig) {
            addArgumentWithValue(this.command, KUBE_CONFIG, kubeconfig);
            return this;
        }

        public HelmCommandBuilder timeout(final int timeout) {
            addArgumentWithValue(this.command, TIMEOUT_ARG, timeout);
            return this;
        }

        public HelmCommandBuilder version(final String version) {
            addArgumentWithValue(this.command, VERSION_ARG, version);
            return this;
        }

        public HelmCommandBuilder set(final String value) {
            addArgument(this.command, SET_FLAG, value);
            return this;
        }

        public HelmCommandBuilder values(final String values) {
            addArgumentWithValue(this.command, VALUES_ARG, values);
            return this;
        }

        public HelmCommandBuilder debug() {
            addArgumentWithoutValue(this.command, DEBUG_FLAG);
            return this;
        }

        public HelmCommandBuilder devel() {
            addArgumentWithoutValue(this.command, DEVEL_FLAG);
            return this;
        }

        public HelmCommandBuilder helmWait() {
            addArgumentWithoutValue(this.command, WAIT_FLAG);
            return this;
        }

        public HelmCommandBuilder reuseValues() {
            addArgumentWithoutValue(this.command, REUSE_VALUES_FLAG);
            return this;
        }

        public HelmCommandBuilder ignoreError() {
            addArgumentWithoutValue(this.command, IGNORE_ERROR);
            return this;
        }

        public String build() {
            LOGGER.info(COMMAND_TO_EXECUTE, command);
            return command.toString();
        }
    }
}
