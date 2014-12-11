package com.davidehrmann.nodejava.packagemanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VersionSpecTest {
    @Test
    public void testSimpleVersions() {
        VersionSpec spec = VersionSpec.fromString("3.2.1");

        assertTrue(spec.matches(Version.fromString("3.2.1")));
        assertFalse(spec.matches(Version.fromString("3.2.0")));
        assertFalse(spec.matches(Version.fromString("3.2.2")));

        spec = VersionSpec.fromString("=3.2.1");

        assertTrue(spec.matches(Version.fromString("3.2.1")));
        assertFalse(spec.matches(Version.fromString("3.2.0")));
        assertFalse(spec.matches(Version.fromString("3.2.2")));
    }

    @Test
        public void testGTERanges() {
        VersionSpec spec = VersionSpec.fromString(">=1.2.7");
        assertTrue(spec.matches(Version.fromString("1.2.7")));
        assertTrue(spec.matches(Version.fromString("1.2.8")));
        assertTrue(spec.matches(Version.fromString("2.5.3")));
        assertTrue(spec.matches(Version.fromString("1.3.9")));
        assertFalse(spec.matches(Version.fromString("1.2.6")));
        assertFalse(spec.matches(Version.fromString("1.1.0")));
    }

    @Test
    public void testGTRanges() {
        VersionSpec spec = VersionSpec.fromString(">1.2.7");
        assertTrue(spec.matches(Version.fromString("1.2.8")));
        assertTrue(spec.matches(Version.fromString("2.5.3")));
        assertTrue(spec.matches(Version.fromString("1.3.9")));
        assertFalse(spec.matches(Version.fromString("1.2.6")));
        assertFalse(spec.matches(Version.fromString("1.1.0")));
        assertFalse(spec.matches(Version.fromString("1.2.7")));
    }

    @Test
    public void testLTERanges() {
        VersionSpec spec = VersionSpec.fromString("<=1.2.7");
        assertTrue(spec.matches(Version.fromString("1.2.6")));
        assertTrue(spec.matches(Version.fromString("1.1.0")));
        assertTrue(spec.matches(Version.fromString("1.2.7")));
        assertFalse(spec.matches(Version.fromString("1.2.8")));
        assertFalse(spec.matches(Version.fromString("2.5.3")));
        assertFalse(spec.matches(Version.fromString("1.3.9")));

        // TODO: Behavior for <=1.2.x isn't defined
    }

    @Test
    public void testLTRanges() {
        VersionSpec spec = VersionSpec.fromString("<1.2.7");
        assertTrue(spec.matches(Version.fromString("1.2.6")));
        assertTrue(spec.matches(Version.fromString("1.1.0")));
        assertFalse(spec.matches(Version.fromString("1.2.7")));
        assertFalse(spec.matches(Version.fromString("1.2.8")));
        assertFalse(spec.matches(Version.fromString("2.5.3")));
        assertFalse(spec.matches(Version.fromString("1.3.9")));
    }

    @Test
    public void testHyphenRanges() {
        VersionSpec spec = VersionSpec.fromString("1.2.3 - 1.2.5");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.4")));
        assertTrue(spec.matches(Version.fromString("1.2.5")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("1.2.6")));
    }

    @Test
    public void testHyphenXRanges() {
        VersionSpec spec = VersionSpec.fromString("1 - 2.3.4");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertTrue(spec.matches(Version.fromString("2.0.0")));
        assertTrue(spec.matches(Version.fromString("2.3.4")));
        assertFalse(spec.matches(Version.fromString("2.3.5")));
        assertFalse(spec.matches(Version.fromString("0.0.9")));

        spec = VersionSpec.fromString("1.2 - 2.3.4");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertTrue(spec.matches(Version.fromString("2.0.0")));
        assertTrue(spec.matches(Version.fromString("2.3.4")));
        assertFalse(spec.matches(Version.fromString("2.3.5")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));

        spec = VersionSpec.fromString("1.2.3 - 2.3");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.4")));
        assertTrue(spec.matches(Version.fromString("2.3.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("2.4.0")));

        spec = VersionSpec.fromString("1.2.3 - 2");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.4")));
        assertTrue(spec.matches(Version.fromString("2.3.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("3.0.0")));

        spec = VersionSpec.fromString("1.2.x - 2.3.4");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("2.3.4")));
        assertFalse(spec.matches(Version.fromString("2.3.5")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));

        spec = VersionSpec.fromString("1.2.3 - 2.x.x");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("2.3.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("3.0.0")));
    }

    @Test
    public void testXRanges() {
        VersionSpec spec;

        spec = VersionSpec.fromString("*");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("9.9.9")));

        spec = VersionSpec.fromString("");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("9.9.9")));

        spec = VersionSpec.fromString("x");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("9.9.9")));

        spec = VersionSpec.fromString("X");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("9.9.9")));

        spec = VersionSpec.fromString("x.X.*");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("9.9.9")));

        spec = VersionSpec.fromString("1.x");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("0.9.9")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));
        assertFalse(spec.matches(Version.fromString("2.0.0-alpha.2")));

        spec = VersionSpec.fromString("1.2.*");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));

        spec = VersionSpec.fromString("1.2.X");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));
        assertFalse(spec.matches(Version.fromString("1.3.0-beta")));
    }

    @Test
    public void testTildeRanges() {
        VersionSpec spec;

        spec = VersionSpec.fromString("~1.2.3");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));

        spec = VersionSpec.fromString("~1.2");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));

        spec = VersionSpec.fromString("~1");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));
        assertFalse(spec.matches(Version.fromString("0.9.9")));

        spec = VersionSpec.fromString("~0.2.3");
        assertTrue(spec.matches(Version.fromString("0.2.3")));
        assertTrue(spec.matches(Version.fromString("0.2.9")));
        assertFalse(spec.matches(Version.fromString("0.2.2")));
        assertFalse(spec.matches(Version.fromString("0.3.0")));

        spec = VersionSpec.fromString("~0.2");
        assertTrue(spec.matches(Version.fromString("0.2.0")));
        assertTrue(spec.matches(Version.fromString("0.2.9")));
        assertFalse(spec.matches(Version.fromString("0.1.9")));
        assertFalse(spec.matches(Version.fromString("0.3.0")));

        spec = VersionSpec.fromString("~0");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("0.9.9")));
        assertFalse(spec.matches(Version.fromString("1.0.0")));

        spec = VersionSpec.fromString("~1.2.3-beta.2");
        assertTrue(spec.matches(Version.fromString("1.2.3-beta.2")));
        assertTrue(spec.matches(Version.fromString("1.2.3-beta.4")));
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));
        assertFalse(spec.matches(Version.fromString("1.2.4-beta.2")));
    }

    @Test
    public void testCaretRange() {
        VersionSpec spec;

        spec = VersionSpec.fromString("^1.2.3");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));

        spec = VersionSpec.fromString("^0.2.3");
        assertTrue(spec.matches(Version.fromString("0.2.3")));
        assertTrue(spec.matches(Version.fromString("0.2.9")));
        assertFalse(spec.matches(Version.fromString("0.2.2")));
        assertFalse(spec.matches(Version.fromString("0.3.0")));

        spec = VersionSpec.fromString("^0.0.3");
        assertTrue(spec.matches(Version.fromString("0.0.3")));
        assertFalse(spec.matches(Version.fromString("0.0.2")));
        assertFalse(spec.matches(Version.fromString("0.0.4")));
        assertFalse(spec.matches(Version.fromString("0.0.3-beta.2")));

        spec = VersionSpec.fromString("^1.2.3-beta.2");
        assertTrue(spec.matches(Version.fromString("1.2.3-beta.2")));
        assertTrue(spec.matches(Version.fromString("1.2.3-beta.4")));
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("1.2.4-beta.4")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));

        spec = VersionSpec.fromString("^0.0.3-beta");
        assertTrue(spec.matches(Version.fromString("0.0.3")));
        assertTrue(spec.matches(Version.fromString("0.0.3-pr.2")));
        assertFalse(spec.matches(Version.fromString("0.0.2")));
        assertFalse(spec.matches(Version.fromString("0.0.4")));

        spec = VersionSpec.fromString("^1.2.x");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));

        spec = VersionSpec.fromString("^0.0.x");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("0.0.9")));
        assertFalse(spec.matches(Version.fromString("1.1.0")));

        spec = VersionSpec.fromString("^0.0");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("0.0.9")));
        assertFalse(spec.matches(Version.fromString("1.1.0")));

        spec = VersionSpec.fromString("^1.x");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertFalse(spec.matches(Version.fromString("0.9.9")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));

        spec = VersionSpec.fromString("^0.x");
        assertTrue(spec.matches(Version.fromString("0.0.0")));
        assertTrue(spec.matches(Version.fromString("0.9.9")));
        assertFalse(spec.matches(Version.fromString("1.0.0")));

        spec = VersionSpec.fromString("^2.0.0-rc.0");
    }

    @Test
    public void testIntersections() {
        VersionSpec spec;

        spec = VersionSpec.fromString(">=1.2.3 <2.3.4");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("2.3.3")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("2.3.4")));

        spec = VersionSpec.fromString(">=1.2.1 <2.3.8 >=1.2.2 <=2.3.6");
        assertTrue(spec.matches(Version.fromString("1.2.2")));
        assertTrue(spec.matches(Version.fromString("2.3.6")));
        assertFalse(spec.matches(Version.fromString("1.2.1")));
        assertFalse(spec.matches(Version.fromString("2.3.7")));

        spec = VersionSpec.fromString("1.x 1.2.3");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("1.2.4")));

        spec = VersionSpec.fromString("x x.x x.x.x 1.x 1.2.x");
        assertTrue(spec.matches(Version.fromString("1.2.0")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertFalse(spec.matches(Version.fromString("1.1.9")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));
    }

    @Test
    public void testUnions() {
        VersionSpec spec;

        spec = VersionSpec.fromString("1.2.3 || 1.2.5");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.5")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("2.3.4")));
        assertFalse(spec.matches(Version.fromString("2.3.6")));

        spec = VersionSpec.fromString("1.x || 2.4.x");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertTrue(spec.matches(Version.fromString("2.4.0")));
        assertTrue(spec.matches(Version.fromString("2.4.9")));
        assertFalse(spec.matches(Version.fromString("0.9.9")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));
        assertFalse(spec.matches(Version.fromString("2.3.9")));
        assertFalse(spec.matches(Version.fromString("2.5.0")));

        spec = VersionSpec.fromString("1.x || 2.4.x || 3.5.6 - 3.5.8 || 0.0.1");
        assertTrue(spec.matches(Version.fromString("1.0.0")));
        assertTrue(spec.matches(Version.fromString("1.9.9")));
        assertTrue(spec.matches(Version.fromString("2.4.0")));
        assertTrue(spec.matches(Version.fromString("2.4.9")));
        assertTrue(spec.matches(Version.fromString("3.5.6")));
        assertTrue(spec.matches(Version.fromString("3.5.8")));
        assertTrue(spec.matches(Version.fromString("0.0.1")));
        assertFalse(spec.matches(Version.fromString("2.0.0")));
        assertFalse(spec.matches(Version.fromString("2.3.9")));
        assertFalse(spec.matches(Version.fromString("2.5.0")));
        assertFalse(spec.matches(Version.fromString("3.5.5")));
        assertFalse(spec.matches(Version.fromString("2.5.9")));
        assertFalse(spec.matches(Version.fromString("0.0.2")));
    }

    @Test
    public void testOperatorPrecedence() {
        VersionSpec spec;

        spec = VersionSpec.fromString("x 1.2.3 1.x 1.2.x 1.2.2 - 1.2.3 || >=1.2.5 <1.3 || >2.4.0 <=3 || ^4.2 || ~6.2");
        assertTrue(spec.matches(Version.fromString("1.2.3")));
        assertTrue(spec.matches(Version.fromString("1.2.5")));
        assertTrue(spec.matches(Version.fromString("1.2.9")));
        assertTrue(spec.matches(Version.fromString("2.4.1")));
        assertTrue(spec.matches(Version.fromString("3.0.0")));
        assertTrue(spec.matches(Version.fromString("4.2.0")));
        assertTrue(spec.matches(Version.fromString("4.9.9")));
        assertTrue(spec.matches(Version.fromString("6.2.0")));
        assertTrue(spec.matches(Version.fromString("6.2.9")));
        assertFalse(spec.matches(Version.fromString("1.2.2")));
        assertFalse(spec.matches(Version.fromString("1.2.4")));
        assertFalse(spec.matches(Version.fromString("1.2.4")));
        assertFalse(spec.matches(Version.fromString("1.3.0")));
        assertFalse(spec.matches(Version.fromString("2.4.0")));
        assertFalse(spec.matches(Version.fromString("3.0.1")));
        assertFalse(spec.matches(Version.fromString("4.1.9")));
        assertFalse(spec.matches(Version.fromString("5.0.0")));
        assertFalse(spec.matches(Version.fromString("6.1.9")));
        assertFalse(spec.matches(Version.fromString("6.3.0")));
    }

    @Test
    public void testEquals() {
        assertEquals(VersionSpec.fromString("1.1.2"), VersionSpec.fromString("1.1.2"));
    }

    // TODO: more prerelease tests. test ASCII sort order.
}
