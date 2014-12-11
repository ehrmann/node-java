package com.davidehrmann.nodejava.packagemanager;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {
    protected static final Pattern VERSION_PATTERN = Pattern.compile(
            "v?([1-9]\\d*|0)" + "[.]" +
                    "([1-9]\\d*|0)" + "[.]" +
                    "([1-9]\\d*|0)" +
                    "(?:-((?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*)(?:[.](?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*))*))?" +
                    "(?:[+]((?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*)(?:[.](?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*))*))?"
    );
    protected static final Pattern PRERELEASE_BUILD_METADATA_PATTERN = Pattern.compile("(?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*)(?:[.](?:0|[1-9][0-9]*|[0-9A-Za-z-]*[a-zA-Z-][0-9A-Za-z-]*))*");
    protected final int major;
    protected final int minor;
    protected final int patch;
    protected final String prerelease;
    protected final String buildMetadata;

    public Version(int major, int minor, int patch) {
        this(major, minor, patch, null, null);
    }

    public Version(int major, int minor, int patch, String prerelease, String buildMetadata) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Negative version number not allowed");
        }
        if (prerelease != null && !PRERELEASE_BUILD_METADATA_PATTERN.matcher(prerelease).matches()) {
            throw new IllegalArgumentException("Failed to parse pre-release string '" + prerelease + "'");
        }
        if (buildMetadata != null && !PRERELEASE_BUILD_METADATA_PATTERN.matcher(buildMetadata).matches()) {
            throw new IllegalArgumentException("Failed to parse build metadata string '" + buildMetadata + "'");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;

        this.buildMetadata = buildMetadata;
        this.prerelease = prerelease;
    }

    public static Version fromString(String ver) {
        Matcher matcher = VERSION_PATTERN.matcher(ver);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Failed to parse version string: " + ver);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        String prerelease = matcher.group(4);
        String buildMetadata = matcher.group(5);

        return new Version(major, minor, patch, prerelease, buildMetadata);
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String getPrerelease() {
        return prerelease;
    }

    public String getBuildMetadata() {
        return buildMetadata;
    }

    @Override
    public int compareTo(Version version) {
        int diff;

        diff = this.major - version.major;
        if (diff != 0) {
            return diff;
        }

        diff = this.minor - version.minor;
        if (diff != 0) {
            return diff;
        }

        diff = this.patch - version.patch;
        if (diff != 0) {
            return diff;
        }

        if (this.prerelease == null && version.prerelease != null) {
            return 1;
        } else if (this.prerelease != null && version.prerelease == null) {
            return -1;
        } else if (this.prerelease != null) {
            diff = this.prerelease.compareTo(version.prerelease);
            if (diff != 0) {
                return diff;
            }
        }

        if (this.buildMetadata == null && version.buildMetadata != null) {
            return -1;
        } else if (this.buildMetadata != null && version.buildMetadata == null) {
            return 1;
        } else if (this.buildMetadata != null) {
            return this.buildMetadata.compareTo(version.buildMetadata);
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        String result = major + "." + minor + "." + patch;
        if (prerelease != null) {
            result = result + "-" + prerelease;
        }
        if (buildMetadata != null) {
            result = result + "+" + buildMetadata;
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;
        return major == version.major && minor == version.minor && patch == version.patch &&
                Objects.equals(buildMetadata, version.buildMetadata) && Objects.equals(prerelease, version.prerelease);
    }

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, prerelease, buildMetadata);
    }
}
