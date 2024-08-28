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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;

public class ProcessExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutor.class);

    // For long running commands inheritIO should be set to true
    public ProcessExecutorResponse executeProcess(String command, int timeOut, boolean inheritIO)
    throws IOException, InterruptedException {
        return executeProcess(command, null, Collections.emptyList(), timeOut, inheritIO);
    }

    public ProcessExecutorResponse executeProcess(String command, String workdir, List<String> sensitiveData, int timeOut, boolean inheritIO)
    throws IOException, InterruptedException {
        List<String> commandsToExecute = constructFullCommand(command, sensitiveData);
        final ProcessBuilder pb = new ProcessBuilder(commandsToExecute);

        if (workdir != null) {
            pb.directory(new File(workdir));
        }
        if (inheritIO) {
            pb.inheritIO();
        }

        final File subProcessOutput = createTmpHelmLogsFile();
        pb.redirectOutput(subProcessOutput);

        Process process = null;
        ProcessExecutorResponse processExecutorResponse;
        try {
            process = pb.start();
            boolean isCmdExecSuccess = process.waitFor(timeOut, TimeUnit.SECONDS);
            if (!isCmdExecSuccess) {
                throw new InterruptedException("command timed out");
            }
            processExecutorResponse = processOutput(process, subProcessOutput);
        } finally {
            if (process != null) {
                process.destroy();
            }
            subProcessOutput.delete();
        }
        return processExecutorResponse;
    }

    private static File createTmpHelmLogsFile() {
        try {
            return File.createTempFile("helm-output", null);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create temporary file for helm output", e);
        }
    }

    private static ProcessExecutorResponse processOutput(final Process process, final File subProcessOutput) throws IOException {
        final ProcessExecutorResponse processExecutorResponse = new ProcessExecutorResponse();
        processExecutorResponse.setExitValue(process.exitValue());

        final BufferedReader inputStreamReader = new BufferedReader(new FileReader(subProcessOutput));
        final BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        try (inputStreamReader; errorStreamReader) {
            String cmdOutput = collectResponse(inputStreamReader);
            processExecutorResponse.setCommandOutput(cmdOutput);

            String cmdError = collectResponse(errorStreamReader);
            processExecutorResponse.setCommandError(cmdError);
        }
        LOGGER.info("ProcessExecutorResponse :: {} ", processExecutorResponse);
        return processExecutorResponse;
    }

    private static String collectResponse(final BufferedReader inputStreamReader) {
        return inputStreamReader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private static List<String> constructFullCommand(final String command, final List<String> sensitiveData) {
        List<String> commandsToExecute = new ArrayList<>();
        if (SystemUtils.IS_OS_WINDOWS) {
            commandsToExecute.add("powershell.exe");
        } else {
            commandsToExecute.add("bash");
            commandsToExecute.add("-c");
        }
        commandsToExecute.add(command);

        List<String> commandWithoutSensitiveData = new ArrayList<>(commandsToExecute);
        commandWithoutSensitiveData.add(getCommandWithoutSensitiveData(command, sensitiveData));
        LOGGER.debug("Commands to execute :: {}", commandWithoutSensitiveData);

        return commandsToExecute;
    }

    private static String getCommandWithoutSensitiveData(final String command, final List<String> sensitiveData) {
        String result = command;
        for (String sensitiveDatum : sensitiveData) {
            result = result.replaceAll(sensitiveDatum, "*****");
        }
        return result;
    }
}
