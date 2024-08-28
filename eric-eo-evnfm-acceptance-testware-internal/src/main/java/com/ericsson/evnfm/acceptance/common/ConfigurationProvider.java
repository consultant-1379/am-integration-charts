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
package com.ericsson.evnfm.acceptance.common;

import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getObjectMapper;
import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.File;
import java.io.IOException;

import org.springframework.core.io.FileSystemResource;

import com.ericsson.evnfm.acceptance.models.configuration.ConfigGeneral;
import com.ericsson.evnfm.acceptance.models.configuration.ConfigParent;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationProvider {

    private static ConfigGeneral configGeneral;
    private static boolean preconfigured = false;

    public static ConfigGeneral getGeneralConfig() {
        return configGeneral;
    }

    public static boolean isPreconfigured() {
        return preconfigured;
    }

    public static void setConfiguration(final String configFile) throws IOException {
        if (configFile != null) {
            final FileSystemResource fileResource = getFileResource(configFile);
            File resourceFile = fileResource.getFile();

            JsonMapper mapper = JsonMapper.builder()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true).build();
            ConfigParent configParent = mapper.readValue(resourceFile, ConfigParent.class);

            configGeneral = configParent.getConfigGeneral();
            preconfigured = true;
        }
    }

    private static FileSystemResource getFileResource(final String fileToLocate) {
        File file = new File(fileToLocate);
        if (file.isFile()) {
            return new FileSystemResource(file);
        }
        ClassLoader classLoader = ConfigurationProvider.class.getClassLoader();
        file = new File(requireNonNull(classLoader.getResource(fileToLocate)).getFile());
        return new FileSystemResource(file);
    }
}
