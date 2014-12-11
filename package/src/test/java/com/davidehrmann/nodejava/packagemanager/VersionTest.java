package com.davidehrmann.nodejava.packagemanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionTest {

    @Test
    public void testVersionParsing() {
        assertEquals(new Version(1, 2, 3), Version.fromString("1.2.3"));
        assertEquals(new Version(1, 2, 3), Version.fromString("v1.2.3"));
        assertEquals(new Version(1, 2, 3, "beta-9", null), Version.fromString("1.2.3-beta-9"));
        assertEquals(new Version(1, 2, 3, null, "sha1-deadbeef"), Version.fromString("1.2.3+sha1-deadbeef"));
        assertEquals(new Version(1, 2, 3, "1.3-beta-9", "sha1-deadbeef"), Version.fromString("1.2.3-1.3-beta-9+sha1-deadbeef"));

        assertEquals(new Version(1, 2, 3, "resolveDependencies.bar", "baz.quz"), Version.fromString("1.2.3-resolveDependencies.bar+baz.quz"));
        assertEquals(new Version(1, 2, 3, "resolveDependencies.bar.baz", "qux.quux.corge"), Version.fromString("1.2.3-resolveDependencies.bar.baz+qux.quux.corge"));

        assertEquals(new Version(1, 2, 3, "123.aZ0-.1-2", "456.bY1-.3-4"), Version.fromString("1.2.3-123.aZ0-.1-2+456.bY1-.3-4"));

        assertEquals(new Version(2, 0, 0, "rc.0", null), Version.fromString("2.0.0-rc.0"));

        // "2.0.0-rc.01" should fail
    }

    @Test
    public void testComparisons() {

    }

    /*
    @RunWith(Parameterized.class)
    public static class BadPrereleaseTest {
        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"+"}, {"0"}, {"$"}, {"-"},
            });
        }

        @Parameterized.Parameter
        public String prerelease;

        @Test(expected = IllegalArgumentException.class)
        public void testBadPrerelease() {
            new Version(1, 2, 3, prerelease, null);
        }
    }
    */
}
