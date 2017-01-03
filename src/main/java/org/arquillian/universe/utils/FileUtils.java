package org.arquillian.universe.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.arquillian.universe.Main.LOGGER;


public class FileUtils {

    public static List<String> getUpdatedDependencyVersion(String fileName, Map<String, String> versions) throws IOException {
        Path filePath = Paths.get(fileName);
        Stream<String> stream = Files.lines(filePath);

        Stream<String> replaced = stream.map(s ->
        {
            if (s.contains("<version.") && !isVersionSame(s, versions)) {
                String latestVersion = getLatestVersionForIdentifier(s, versions);
                if (latestVersion != null) {
                    return s.replaceAll("(?<=>).*(?=<)", latestVersion);
                }
            }
            return s;
        });

        return replaced.collect(Collectors.toList());
    }

    public static void updateFile(File file, List<String> updated) throws IOException {
        Path filePath = Paths.get(file.toString());
        Files.write(filePath, updated);
        LOGGER.info("All dependency's are updated to latest from this file: " + file.getAbsolutePath());
    }

    private static String getLatestVersionForIdentifier(String line, Map<String, String> versions) {
        String latestVersion = null;
        String identifier = getIdentifier(line);

        if (identifier != null) {
            latestVersion = versions.get(identifier);
        }

        return latestVersion;
    }

    private static String getIdentifier(String line) {
        String identifier = null;

        if (line.contains("<version.")) {
            identifier = getFirstMatchedString(line, "(?<=<version[\\W]).*?(?=[\\s]?>)");
        }

        return identifier;
    }

    private static String getVersion(String line) {
        String version = null;

        if (line.contains("<version.")) {
            version = getFirstMatchedString(line, "(?<=>).*(?=<)");
        }

        return version;
    }

    private static boolean isVersionSame(String s, Map<String, String> versions) {
        String version = getLatestVersionForIdentifier(s, versions);
        if (version != null) {
            return version.equals(getVersion(s));
        } else {
            LOGGER.info(getIdentifier(s) + " is listed in properties section of pom.xml, but not found in dependency resources dependency.yml");
            return false;
        }
    }


    private static String getFirstMatchedString(String lineToMatch, String stringPattern) {
        Pattern pattern = Pattern.compile(stringPattern);
        Matcher matcher = pattern.matcher(lineToMatch);

        String dependencyName = null;

        if (matcher.find()) {
            dependencyName = matcher.group();
        }

        return dependencyName;
    }

    public static File createTempFilePath(String name) throws IOException {
        File localPath = File.createTempFile(name, "");

        LOGGER.info("created localpath: " + localPath);

        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        return localPath;
    }

    public static Map<String, String> load(String fileName) {
        Yaml yaml = new Yaml();
        InputStream inStream = FileUtils.class.getClassLoader().getResourceAsStream(fileName);
        Map<String, String> config = (Map<String, String>) yaml.load(inStream);

        return config;
    }
}
