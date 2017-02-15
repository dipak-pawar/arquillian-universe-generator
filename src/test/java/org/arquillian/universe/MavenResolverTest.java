package org.arquillian.universe;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.MavenVersionRangeResult;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.arquillian.universe.MavenResolver.getLatestVersion;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class MavenResolverTest {

    private MavenResolverSystem resolverSpy;

    @Mock
    MavenVersionRangeResult versionRanges;


    @Before
    public void setUp() {

        List<MavenCoordinate> list = new ArrayList<>();

        MavenCoordinate mavenCoordinate1 = Mockito.mock(MavenCoordinate.class);
        when(mavenCoordinate1.getVersion()).thenReturn("1.0.0");
        list.add(mavenCoordinate1);
        MavenCoordinate mavenCoordinate2 = Mockito.mock(MavenCoordinate.class);
        when(mavenCoordinate2.getVersion()).thenReturn("2.0.0");
        list.add(mavenCoordinate2);
        MavenCoordinate mavenCoordinate3 = Mockito.mock(MavenCoordinate.class);
        when(mavenCoordinate3.getVersion()).thenReturn("3.0.0-SNAPSHOT");
        list.add(mavenCoordinate3);

        when(versionRanges.getVersions()).thenReturn(list);

        resolverSpy = Mockito.spy(Maven.resolver());
        doReturn(versionRanges).when(resolverSpy).resolveVersionRange("g:a:[,)");

    }


    @Test
    public void should_get_latest_version() {
        //given
        Map<String, String> config = new LinkedHashMap<>();
        config.put("foo", "g:a");

        //when
        String latestVersion = getLatestVersion(resolverSpy.resolveVersionRange("g:a:[,)"));

        //then
        Assert.assertNotNull(latestVersion);
        Assert.assertEquals(latestVersion, "2.0.0");

    }

}