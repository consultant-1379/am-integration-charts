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

public class ProcessExecutorResponse {

    private int exitValue;
    private String commandOutput;
    private String commandError;

    public int getExitValue() {
        return exitValue;
    }

    public void setExitValue(int exitValue) {
        this.exitValue = exitValue;
    }

    public String getCommandOutput() {
        return commandOutput;
    }

    public void setCommandOutput(String commandOutput) {
        this.commandOutput = commandOutput;
    }

    public String getCommandError() {
        return commandError;
    }

    public void setCommandError(String commandError) {
        this.commandError = commandError;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("exitValue=").append(exitValue)
                .append(", commandOutput=").append(commandOutput)
                .append(", commandError=").append(commandError)
                .append("}")
                .toString();
    }
}
