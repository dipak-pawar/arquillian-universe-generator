package org.arquillian.universe;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenVersionRangeResult;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.arquillian.universe.Main.LOGGER;


class MavenResolver {

    static Map<String, String> getLatestVersionsOfPomDependency(Map<String, String> config) {
        Map<String, String> latestVersions = new LinkedHashMap<>();
        if (config != null) {
            config.forEach((name, identifier) -> latestVersions.put(name, getLatestVersion(getVersionRange(identifier))));
            LOGGER.info("latest versions of dependency:" + latestVersions);
        }

        return latestVersions;
    }

    private static MavenVersionRangeResult getVersionRange(String identifier) {
        return Maven.resolver().resolveVersionRange(identifier + ":[,)");
    }

    static String getLatestVersion(MavenVersionRangeResult versionRanges) {

        List<MavenCoordinate> filters = versionRanges.getVersions();

        List<MavenCoordinate> filterList = filters.stream().filter(mavenCoordinate -> !mavenCoordinate.getVersion().toLowerCase().contains("snapshot"))
                .collect(Collectors.toList());

        MavenCoordinate latestVersion = filterList.get(filterList.size() - 1);

        return latestVersion.getVersion();
    }
}

