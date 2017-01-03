package org.arquillian.universe.utils;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class FileUtilsTest {
    private static final String FILE_NAME = "src/test/resources/pom.xml";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void should_update_dependency_version_to_latest() throws IOException {
        //given
        final Map<String, String> map = getDependency();

        //when
        List<String> updatedList = FileUtils.getUpdatedDependencyVersion(FILE_NAME, map);

        //then
        assertThat(updatedList).contains(getNewDependency());
        assertThat(updatedList).doesNotContain(getOldDependency());

    }

    @Test
    public void should_write_updated_version_to_file() throws IOException {
        //given
        final Map<String, String> map = getDependency();


        //when
        List<String> updatedList = FileUtils.getUpdatedDependencyVersion(FILE_NAME, map);
        File updatedPom = folder.newFile("pom.xml");
        FileUtils.updateFile(updatedPom, updatedList);
        final String s = org.apache.commons.io.FileUtils.readFileToString(updatedPom);

        //then
        assertThat(s).contains(getNewDependency());
        assertThat(s).doesNotContain(getOldDependency()[0]);
        assertThat(s).doesNotContain(getOldDependency()[1]);
    }

    @Test
    public void should_load_from_resources() throws Exception {
        Map<String, String> dependencyIdentifier = FileUtils.load("dependency.yml");

        assertThat(dependencyIdentifier).contains(
                entry("arquillian_core", "org.jboss.arquillian.core:arquillian-core-parent"),
                entry("arquillian_drone", "org.jboss.arquillian.extension:arquillian-drone-aggregator"));
    }

    private Map<String, String> getDependency() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("arquillian_core", "1.1.12.Final");
        map.put("arquillian_pact", "1.0.0.Alpha2");
        map.put("arquillian_governor", "1.0.3.Final");

        return map;
    }

    private String[] getNewDependency() {
        String[] s = {"    <version.arquillian_core>1.1.12.Final</version.arquillian_core>",
                "    <version.arquillian_pact>1.0.0.Alpha2</version.arquillian_pact>"};

        return s;
    }

    private String[] getOldDependency() {
        String[] s = {"    <version.arquillian_core>1.1.11.Final</version.arquillian_core>",
                "    <version.arquillian_pact>1.0.0.Alpha1</version.arquillian_pact>"};
        return s;
    }
}