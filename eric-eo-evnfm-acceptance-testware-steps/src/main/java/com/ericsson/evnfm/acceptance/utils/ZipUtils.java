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

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.evnfm.acceptance.exception.ZipExtractionException;
import com.ericsson.evnfm.acceptance.models.ProcessExecutorResponse;

public class ZipUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);
    private static final int DEFAULT_UNZIP_TIMEOUT = 300;

    private ZipUtils() {
    }

    public static void unzipToDirectory(Path zipFile, Path destinationDirectory) throws IOException, InterruptedException {
        LOGGER.info("Unzipping {} to {}", zipFile, destinationDirectory);
        String unzipCommand = String.format("unzip %s -d %s", zipFile, destinationDirectory);
        ProcessExecutorResponse response = new ProcessExecutor().executeProcess(unzipCommand, DEFAULT_UNZIP_TIMEOUT, false);
        if (response.getExitValue() != 0) {
            throw new ZipExtractionException(String.format("Failed to unzip %s to %s with error %s",
                                                           zipFile, destinationDirectory, response));
        }
    }
}

