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

import static com.ericsson.am.shared.vnfd.utils.Constants.NODE_TYPES_KEY;
import static com.ericsson.evnfm.acceptance.steps.common.rest.CommonHelper.getCsar;
import static com.ericsson.evnfm.acceptance.utils.ParsingUtils.getYamlParser;
import static com.ericsson.evnfm.acceptance.utils.TestExecutionGlobalConfig.EVNFM_INSTANCE;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.yaml.snakeyaml.Yaml;

import com.ericsson.am.shared.vnfd.VnfdUtility;
import com.ericsson.am.shared.vnfd.model.NodeProperties;
import com.ericsson.am.shared.vnfd.model.VnfDescriptorDetails;
import com.ericsson.evnfm.acceptance.exception.PackageParsingException;
import com.ericsson.evnfm.acceptance.models.EvnfmBasePackage;
import com.ericsson.evnfm.acceptance.models.EvnfmDockerImage;
import com.ericsson.evnfm.acceptance.models.EvnfmHelmChart;

public class PackageUtils {
    private static final String RELATIVE_PATH_TO_TOSCA_META_FILE = "TOSCA-Metadata/TOSCA.meta";
    private static final String RELATIVE_PATH_TO_IMAGES_FILE = "Files/images.txt";
    private static final String ENTRY_DEFINITIONS = "Entry-Definitions";
    private static final String CHART_YAML_FILE = "Chart.yaml";
    private static final String NAME_CHART_PROPERTY = "name";
    private static final String VERSION_CHART_PROPERTY = "version";
    private static final String ZIP_EXTENSION = ".zip";
    private static final String CSAR_EXTENSION = ".csar";

    private PackageUtils() {
    }

    public static void loadPackageData(EvnfmBasePackage evnfmBasePackage) throws IOException, InterruptedException {
        String directoryName = UUID.randomUUID().toString();
        Path csarBaseDirectory = Files.createTempDirectory(directoryName);
        FileSystemResource csar = getCsar(evnfmBasePackage, EVNFM_INSTANCE.getCsarDownloadPath());
        ZipUtils.unzipToDirectory(csar.getFile().toPath(), csarBaseDirectory);
        if (csar.getFilename().endsWith(ZIP_EXTENSION)) {
            ZipUtils.unzipToDirectory(new File(csarBaseDirectory + File.separator +
                                                       evnfmBasePackage.getPackageName()
                                                               .substring(evnfmBasePackage.getPackageName()
                                                                                  .lastIndexOf(File.separator) + 1)
                                                               .replace(ZIP_EXTENSION,
                                                                        CSAR_EXTENSION)).toPath(),
                                      csarBaseDirectory);
        }
        try {
            final Path toscaMetaPath = csarBaseDirectory.resolve(RELATIVE_PATH_TO_TOSCA_META_FILE);
            final Map<String, Path> mapOfPaths = parseFileToMapOfPaths(csarBaseDirectory, toscaMetaPath);
            final VnfDescriptorDetails vnfDescriptorDetails = getDescriptorDetails(mapOfPaths.get(ENTRY_DEFINITIONS));
            final List<EvnfmHelmChart> evnfmHelmCharts = getHelmPackage(vnfDescriptorDetails, csarBaseDirectory);
            final List<EvnfmDockerImage> evnfmDockerImages = getDockerImages(csarBaseDirectory.resolve(RELATIVE_PATH_TO_IMAGES_FILE));
            evnfmBasePackage.setVnfdId(vnfDescriptorDetails.getVnfDescriptorId());
            evnfmBasePackage.setVnfdFile(mapOfPaths.get(ENTRY_DEFINITIONS).getFileName().toString());
            evnfmBasePackage.setNumberOfCharts(evnfmHelmCharts.size());
            evnfmBasePackage.setSkipImageUpload(evnfmDockerImages.isEmpty());
            evnfmBasePackage.setCharts(evnfmHelmCharts);
            evnfmBasePackage.setImages(evnfmDockerImages);
        } finally {
            FileUtils.deleteDirectory(csarBaseDirectory.toFile());
        }
    }

    public static String getChartYamlProperty(Path pathToChart, String property) {
        try (InputStream chartInputStream = Files.newInputStream(pathToChart);
                TarArchiveInputStream tarInputStream = new TarArchiveInputStream(new GZIPInputStream(chartInputStream))) {
            return getPropertyFromChartYamlFile(property, tarInputStream);
        } catch (IOException e) {
            throw new PackageParsingException(String.format("Failed to extract property '%s' from Chart.yaml in Helm Chart '%s'",
                                                            property,
                                                            pathToChart));
        }
    }

    public static VnfDescriptorDetails getDescriptorDetails(final Path vnfdPath) {
        JSONObject vnfd = VnfdUtility.validateYamlCanBeParsed(vnfdPath);
        JSONObject nodeType = vnfd.getJSONObject(NODE_TYPES_KEY);
        VnfDescriptorDetails details = VnfdUtility.buildVnfDescriptorDetails(vnfd);
        NodeProperties nodeProperties = VnfdUtility.validateAndGetNodeProperties(nodeType);
        details.setVnfDescriptorId((String) (nodeProperties.getDescriptorId().getDefaultValue()));
        return details;
    }

    private static List<EvnfmHelmChart> getHelmPackage(VnfDescriptorDetails vnfDescriptorDetails, Path csarBaseDirectory) {
        return vnfDescriptorDetails.getHelmCharts().stream().map(chart -> {
            Path chartPath = csarBaseDirectory.resolve(chart.getPath());
            EvnfmHelmChart evnfmHelmChart = new EvnfmHelmChart();
            evnfmHelmChart.setName(getChartYamlProperty(chartPath, NAME_CHART_PROPERTY));
            evnfmHelmChart.setVersion(getChartYamlProperty(chartPath, VERSION_CHART_PROPERTY));
            evnfmHelmChart.setChartType(EvnfmHelmChart.ChartType.valueOf(chart.getChartType().name()));
            return evnfmHelmChart;
        }).collect(Collectors.toList());
    }

    private static List<EvnfmDockerImage> getDockerImages(final Path imagesFilePath) throws IOException {
        List<String> imagesInfo = Files.lines(imagesFilePath).collect(Collectors.toList());
        Map<String, Map<String, List<String>>> parsedImagesInfo = new HashMap<>();
        for (String imageInfo : imagesInfo) {
            String[] nameAndTag = imageInfo.split(":");
            if (nameAndTag.length == 2) {
                String name = nameAndTag[0];
                String tag = nameAndTag[1];
                String[] tagParts = name.split("/");
                if (tagParts.length >= 2) {
                    String projectName = tagParts[1];
                    String imageName = tagParts[tagParts.length - 1];
                    parsedImagesInfo.compute(projectName, (key, value) -> {
                        if (value == null) {
                            Map<String, List<String>> imageNameToTags = new HashMap<>();
                            List<String> tags = new ArrayList<>();
                            tags.add(tag);
                            imageNameToTags.put(imageName, tags);
                            return imageNameToTags;
                        } else {
                            value.putIfAbsent(imageName, new ArrayList<>());
                            value.get(imageName).add(tag);
                            return value;
                        }
                    });
                } else {
                    throw new PackageParsingException(String.format("Docker images name %s is invalid", name));
                }
            } else {
                throw new PackageParsingException(String.format("Docker image name %s is invalid", imageInfo));
            }
        }
        List<EvnfmDockerImage> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<String>>> projectNameEntry : parsedImagesInfo.entrySet()) {
            String projectName = projectNameEntry.getKey();
            for (Map.Entry<String, List<String>> imageNameEntry : projectNameEntry.getValue().entrySet()) {
                EvnfmDockerImage evnfmDockerImage = new EvnfmDockerImage();
                evnfmDockerImage.setProjectName(projectName);
                evnfmDockerImage.setImageName(imageNameEntry.getKey());
                evnfmDockerImage.setTags(imageNameEntry.getValue());
                result.add(evnfmDockerImage);
            }
        }
        return result;
    }

    private static String getPropertyFromChartYamlFile(final String prop, final TarArchiveInputStream tarInput) throws IOException {
        TarArchiveEntry currentEntry;
        while ((currentEntry = (TarArchiveEntry) tarInput.getNextEntry()) != null) {
            final String name = currentEntry.getName();
            if (isAtTopLevel(name) && name.endsWith(CHART_YAML_FILE)) {
                return getPropFromFile(prop, tarInput);
            }
        }
        return null;
    }

    private static boolean isAtTopLevel(final String name) {
        return StringUtils.countMatches(name, "/") == 1;
    }

    private static String getPropFromFile(final String property, final TarArchiveInputStream tarInput) {
        Yaml chartYaml = getYamlParser();
        Map<String, Object> loadedYaml = chartYaml.load(tarInput);
        if (!loadedYaml.containsKey(property)) {
            throw new IllegalArgumentException(String.format("Failed to find property with name '%s'", property));
        }
        Object propertyValue = loadedYaml.get(property);
        if (!(propertyValue instanceof String)) {
            throw new IllegalArgumentException(String.format("Property with name '%s' is not of String type", property));
        }
        return (String) propertyValue;
    }

    private static Map<String, Path> parseFileToMapOfPaths(final Path unpackedDirectory, final Path toscaMetaPath) {
        try (Stream<String> lines = Files.lines(toscaMetaPath)) {
            return lines
                    .map(line -> line.split(":"))
                    .filter(lineParts -> lineParts.length == 2)
                    .collect(toStringPathPair(unpackedDirectory))
                    .entrySet()
                    .stream()
                    .filter(filePath -> Files.exists(filePath.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (IOException e) {
            throw new PackageParsingException(String.format("The reading of the TOSCA.meta file failed because of %s", e.getMessage()), e);
        }
    }

    private static Collector<String[], ?, Map<String, Path>> toStringPathPair(final Path unpackedDirectory) {
        return Collectors.toMap(e -> e[0].trim(), e -> unpackedDirectory.resolve(e[1].trim()));
    }
}
