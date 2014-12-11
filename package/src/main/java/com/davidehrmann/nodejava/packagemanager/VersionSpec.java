package com.davidehrmann.nodejava.packagemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class VersionSpec {

    //public static VersionSpec ANY = null;

    private static final Pattern PATCH_X_RANGE_PATTERN = Pattern.compile(
            "([1-9]\\d*|0)" +
                    "[.]" + "([1-9]\\d*|0)" +
                    "(?:[.][xX*])?"
    );

    // public static VersionSpec LATEST = null;
    private static final Pattern MINOR_X_RANGE_PATTERN = Pattern.compile(
            "([1-9]\\d*|0)" +
                    "(?:[.][xX*])?" +
                    "(?:[.][xX*])?"
    );
    private static final Pattern MAJOR_X_RANGE_PATTERN = Pattern.compile("[xX*]" + "(?:[.][xX*])?" + "(?:[.][xX*])?");
    private static final Pattern BINARY_OPERATOR_PATTERN = Pattern.compile("\\s*([|]{2}|\\s+)\\s*");
    private static final Pattern HYPHEN_RANGE_PATTERN = Pattern.compile("\\s*-\\s*");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("([<>]=?|=|[~^])\\s*");
    public static VersionSpec LATEST = new VersionSpec() {
        @Override
        public boolean matches(Version ver) {
            return false;
        }
    };

    public static VersionSpec fromString(String versionSpec) {
        if (versionSpec.isEmpty() || versionSpec.trim().isEmpty()) {
            return new Comparator(ComparatorOperator.GTE, new Version(0, 0, 0));
        } else if ("latest".equalsIgnoreCase(versionSpec.trim())) {
            return LATEST;
        }

        List<Object> tokens = new ArrayList<>();

        Matcher matcher = OPERATOR_PATTERN.matcher(versionSpec).useAnchoringBounds(true);

        int end = versionSpec.length();
        int start = 0;
        while (start < end) {
            if (matcher.usePattern(OPERATOR_PATTERN).find(start) && matcher.start() == start) {
                switch (matcher.group(1)) {
                    case ">":
                        tokens.add(ComparatorOperator.GT);
                        break;
                    case ">=":
                        tokens.add(ComparatorOperator.GTE);
                        break;
                    case "<":
                        tokens.add(ComparatorOperator.LT);
                        break;
                    case "<=":
                        tokens.add(ComparatorOperator.LTE);
                        break;
                    case "=":
                        tokens.add(ComparatorOperator.EQ);
                        break;
                    case "~":
                        tokens.add(UnaryRange.TILDE);
                        break;
                    case "^":
                        tokens.add(UnaryRange.CARET);
                        break;
                    default:
                        throw new RuntimeException("Internal bug");
                }
            } else if (matcher.usePattern(Version.VERSION_PATTERN).find(start) && matcher.start() == start) {
                tokens.add(Version.fromString(matcher.group()));
            } else if (matcher.usePattern(PATCH_X_RANGE_PATTERN).find(start) && matcher.start() == start) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                tokens.add(new XRangeVersion(majorVersion, minorVersion));
            } else if (matcher.usePattern(MINOR_X_RANGE_PATTERN).find(start) && matcher.start() == start) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                tokens.add(new XRangeVersion(majorVersion));
            } else if (matcher.usePattern(MAJOR_X_RANGE_PATTERN).find(start) && matcher.start() == start) {
                tokens.add(new XRangeVersion());
            } else if (matcher.usePattern(HYPHEN_RANGE_PATTERN).find(start) && matcher.start() == start) {
                // TODO: make this more elegant?
                tokens.add("-");
            } else if (matcher.usePattern(BINARY_OPERATOR_PATTERN).find(start) && matcher.start() == start) {
                if (matcher.group(1).equals("||")) {
                    tokens.add(BinaryComparatorOperator.UNION);
                } else {
                    tokens.add(BinaryComparatorOperator.INTERSECTION);
                }
            } else {
                throw new IllegalArgumentException("Failed parse version spec '" + versionSpec + "' at char " + start + " : unexpected token");
            }

            start = matcher.end();
        }

        // Bind unary operators
        for (int i = 0; i < tokens.size() - 1; i++) {
            Object o = tokens.get(i);
            if (o instanceof UnaryRange || o instanceof ComparatorOperator) {
                Object next = tokens.get(i + 1);
                if (!(next instanceof Version)) {
                    throw new IllegalArgumentException("Version expected after " + o);
                }

                if (o instanceof UnaryRange) {
                    switch ((UnaryRange) o) {
                        case TILDE:
                            tokens.set(i, new TildeComparator((Version) next));
                            break;
                        case CARET:
                            tokens.set(i, new CaretComparator((Version) next));
                            break;
                        default:
                            throw new RuntimeException("Unsupported UnaryRange: " + o);
                    }
                } else {
                    if (ComparatorOperator.EQ.equals(o)) {
                        tokens.set(i, next);
                    } else {
                        tokens.set(i, new Comparator((ComparatorOperator) o, (Version) next));
                    }
                }

                tokens.set(i + 1, null);
                i += 1;
            }
        }

        Object last = tokens.isEmpty() ? null : tokens.get(tokens.size() - 1);
        if (last instanceof UnaryRange || last instanceof ComparatorOperator) {
            throw new IllegalArgumentException("Version expected after " + last);
        }

        tokens.removeAll(Collections.singleton(null));

        // Bind range operators
        for (int i = 1; i < tokens.size() - 1; i++) {
            if ("-".equals(tokens.get(i))) {
                Object left = tokens.get(i - 1);
                Object right = tokens.get(i + 1);
                if (!(left instanceof Version)) {
                    throw new IllegalArgumentException("Expected xRangeVersion, got '" + left + "'");
                } else if (!(right instanceof Version)) {
                    throw new IllegalArgumentException("Expected xRangeVersion, got '" + right + "'");
                } else {
                    VersionSpec rightVersionSpec;

                    // Since ranges are inclusive, an upper bound of 1.2 is more like <1.3.0.  Handle this.
                    if (right instanceof XRangeVersion) {
                        XRangeVersion rightVersion = (XRangeVersion) right;
                        if (rightVersion.prefixLength == 1) {
                            rightVersionSpec = new Comparator(ComparatorOperator.LT, new Version(rightVersion.major + 1, 0, 0));
                        } else if (rightVersion.prefixLength == 2) {
                            rightVersionSpec = new Comparator(ComparatorOperator.LT, new Version(rightVersion.major, rightVersion.minor + 1, 0));
                        } else if (rightVersion.prefixLength == 3) {
                            rightVersionSpec = new Comparator(ComparatorOperator.LT, new Version(rightVersion.major, rightVersion.minor, rightVersion.patch + 1));
                        } else {
                            rightVersionSpec = new Comparator(ComparatorOperator.GTE, new Version(0, 0, 0));
                        }
                    } else {
                        rightVersionSpec = new Comparator(ComparatorOperator.LTE, (Version) right);
                    }

                    tokens.set(i - 1, null);
                    tokens.set(i + 1, null);
                    tokens.set(i,
                            new BinaryOperation(
                                    new Comparator(ComparatorOperator.GTE, (Version) left),
                                    rightVersionSpec,
                                    BinaryComparatorOperator.INTERSECTION
                            )
                    );
                    i += 2;
                }
            }
        }

        tokens.removeAll(Collections.singleton(null));

        if (tokens.contains("-")) {
            throw new IllegalArgumentException("Unmatched range");
        }

        // Bind intersections
        for (BinaryComparatorOperator operator : new BinaryComparatorOperator[]{BinaryComparatorOperator.INTERSECTION, BinaryComparatorOperator.UNION}) {
            boolean operatorFound;
            do {
                operatorFound = false;
                for (int i = 1; i < tokens.size() - 1; i++) {
                    if (operator.equals(tokens.get(i))) {
                        Object left = tokens.get(i - 1);
                        if (left instanceof XRangeVersion) {
                            XRangeVersion xRangeVersion = (XRangeVersion) left;
                            VersionSpec upperVersionSpec;
                            if (xRangeVersion.prefixLength == 1) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major + 1, 0, 0));
                            } else if (xRangeVersion.prefixLength == 2) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major, xRangeVersion.minor + 1, 0));
                            } else if (xRangeVersion.prefixLength == 3) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major, xRangeVersion.minor, xRangeVersion.patch + 1));
                            } else {
                                upperVersionSpec = new Comparator(ComparatorOperator.GTE, new Version(0, 0, 0));
                            }

                            left = new BinaryOperation(new Comparator(ComparatorOperator.GTE, xRangeVersion), upperVersionSpec, BinaryComparatorOperator.INTERSECTION);
                        } else if (left instanceof Version) {
                            left = new Comparator(ComparatorOperator.EQ, (Version) left);
                        } else if (!(left instanceof VersionSpec)) {
                            throw new IllegalArgumentException("Unexpected token before " + operator + ": '" + left + "'");
                        }

                        Object right = tokens.get(i + 1);
                        if (right instanceof XRangeVersion) {
                            XRangeVersion xRangeVersion = (XRangeVersion) right;
                            VersionSpec upperVersionSpec;
                            if (xRangeVersion.prefixLength == 1) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major + 1, 0, 0));
                            } else if (xRangeVersion.prefixLength == 2) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major, xRangeVersion.minor + 1, 0));
                            } else if (xRangeVersion.prefixLength == 3) {
                                upperVersionSpec = new Comparator(ComparatorOperator.LT, new Version(xRangeVersion.major, xRangeVersion.minor, xRangeVersion.patch + 1));
                            } else {
                                upperVersionSpec = new Comparator(ComparatorOperator.GTE, new Version(0, 0, 0));
                            }

                            right = new BinaryOperation(new Comparator(ComparatorOperator.GTE, xRangeVersion), upperVersionSpec, BinaryComparatorOperator.INTERSECTION);
                        } else if (right instanceof Version) {
                            right = new Comparator(ComparatorOperator.EQ, (Version) right);
                        } else if (!(right instanceof VersionSpec)) {
                            throw new IllegalArgumentException("Unexpected token after " + operator + ": '" + right + "'");
                        }

                        tokens.set(i - 1, null);
                        tokens.set(i + 1, null);
                        tokens.set(i, new BinaryOperation((VersionSpec) left, (VersionSpec) right, operator));

                        operatorFound = true;
                        i += 2;
                    }
                }

                tokens.removeAll(Collections.singleton(null));
            } while (operatorFound);
        }

        if (tokens.size() != 1) {
            throw new IllegalArgumentException("Oops");
        } else if (tokens.get(0) instanceof XRangeVersion) {
            return new XRangeComparator((XRangeVersion) tokens.get(0));
        } else if (tokens.get(0) instanceof Version) {
            return new Comparator(ComparatorOperator.EQ, (Version) tokens.get(0));
        } else if (!(tokens.get(0) instanceof VersionSpec)) {
            throw new IllegalArgumentException("Oops");
        } else {
            return (VersionSpec) tokens.get(0);
        }
    }

    public abstract boolean matches(Version ver);

    protected enum BinaryComparatorOperator {
        UNION,
        INTERSECTION,
    }

    protected enum UnaryRange {
        TILDE,
        CARET,
    }

    protected enum ComparatorOperator {
        LT,
        LTE,
        GT,
        GTE,
        EQ,
    }

    protected static class XRangeVersion extends Version {
        protected final int prefixLength;

        public XRangeVersion() {
            super(0, 0, 0);
            this.prefixLength = 0;
        }

        public XRangeVersion(int major) {
            super(major, 0, 0);
            this.prefixLength = 1;
        }

        public XRangeVersion(int major, int minor) {
            super(major, minor, 0);
            this.prefixLength = 2;
        }

        public XRangeVersion(int major, int minor, int patch) {
            super(major, minor, patch);
            this.prefixLength = 3;
        }

        @Override
        public String toString() {
            if (prefixLength == 0) {
                return "x";
            } else if (prefixLength == 1) {
                return major + ".x.x";
            } else if (prefixLength == 2) {
                return major + "." + minor + ".x";
            } else if (prefixLength == 3) {
                return major + "." + minor + "." + patch;
            } else {
                throw new RuntimeException();
            }
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o) && ((XRangeVersion) o).prefixLength == prefixLength;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), prefixLength);
        }
    }

    protected static class Comparator extends VersionSpec {
        private final ComparatorOperator comparatorOperator;
        private final Version version;

        public Comparator(ComparatorOperator comparatorOperator, Version version) {
            this.comparatorOperator = Objects.requireNonNull(comparatorOperator);
            this.version = Objects.requireNonNull(version);
        }

        @Override
        public boolean matches(Version version) {
            if (version.getPrerelease() != null && this.version.getPrerelease() == null) {
                return false;
            }

            int diff = version.compareTo(this.version);
            switch (this.comparatorOperator) {
                case LT:
                    return diff < 0;
                case LTE:
                    return diff <= 0;
                case GT:
                    return diff > 0;
                case GTE:
                    return diff >= 0;
                case EQ:
                    return diff == 0;
                default:
                    throw new RuntimeException("Unrecognized Operator " + this.comparatorOperator);
            }
        }

        @Override
        public String toString() {
            switch (this.comparatorOperator) {
                case LT:
                    return "<" + this.version;
                case LTE:
                    return "<=" + this.version;
                case GT:
                    return ">" + this.version;
                case GTE:
                    return ">=" + this.version;
                case EQ:
                    return "=" + this.version;
                default:
                    throw new RuntimeException("Unrecognized Operator " + this.comparatorOperator);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || o.getClass() != this.getClass()) {
                return false;
            } else if (o == this) {
                return true;
            } else {
                Comparator comparator = (Comparator) o;
                return this.comparatorOperator.equals(comparator.comparatorOperator) && this.version.equals(comparator.version);
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(comparatorOperator, version);
        }
    }

    protected static class BinaryOperation extends VersionSpec {
        private final BinaryComparatorOperator operator;
        private final VersionSpec leftOperand;
        private final VersionSpec rightOperand;

        public BinaryOperation(VersionSpec leftOperand, VersionSpec rightOperand, BinaryComparatorOperator operator) {
            this.leftOperand = Objects.requireNonNull(leftOperand);
            this.rightOperand = Objects.requireNonNull(rightOperand);
            this.operator = Objects.requireNonNull(operator);
        }

        @Override
        public boolean matches(Version ver) {
            switch (this.operator) {
                case UNION:
                    return this.leftOperand.matches(ver) || this.rightOperand.matches(ver);
                case INTERSECTION:
                    return this.leftOperand.matches(ver) && this.rightOperand.matches(ver);
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public String toString() {
            switch (this.operator) {
                case UNION:
                    return this.leftOperand + " || " + this.rightOperand;
                case INTERSECTION:
                    return this.rightOperand + " " + this.rightOperand;
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(leftOperand, rightOperand, operator);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            } else if (obj == this) {
                return true;
            } else {
                BinaryOperation binaryOperation = (BinaryOperation) obj;
                return leftOperand.equals(binaryOperation.leftOperand) &&
                        rightOperand.equals(binaryOperation.rightOperand) &&
                        operator.equals(binaryOperation.operator);
            }
        }
    }

    protected static class CaretComparator extends VersionSpec {
        protected final Version version;

        public CaretComparator(Version version) {
            this.version = Objects.requireNonNull(version);
        }

        @Override
        public boolean matches(Version ver) {
            if (this.version instanceof XRangeVersion && ((XRangeVersion) this.version).prefixLength < 3) {
                XRangeVersion xRangeVersion = (XRangeVersion) this.version;
                if (ver.prerelease != null) {
                    return false;
                } else if (xRangeVersion.prefixLength == 0) {
                    return true;
                } else if (xRangeVersion.major == 0 && xRangeVersion.prefixLength == 2) {
                    return xRangeVersion.major == ver.major && xRangeVersion.minor == ver.minor;
                } else {
                    return xRangeVersion.major == ver.major && xRangeVersion.minor <= ver.minor;
                }
            } else {
                if (this.version.prerelease != null && ver.prerelease != null) {
                    return this.version.major == ver.major && this.version.minor == ver.minor && this.version.patch == ver.patch;
                } else if (ver.prerelease != null) {
                    return false;
                } else if (this.version.major == 0) {
                    if (this.version.minor == 0) {
                        return this.version.major == ver.major && this.version.minor == ver.minor && this.version.patch == ver.patch;
                    } else {
                        return this.version.major == ver.major && this.version.minor == ver.minor && ver.compareTo(this.version) >= 0;
                    }
                } else {
                    return this.version.major == ver.major && ver.compareTo(this.version) >= 0;
                }
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClass(), this.version);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            } else if (obj == this) {
                return true;
            } else {
                return version.equals(((CaretComparator) obj).version);
            }
        }
    }

    protected static class TildeComparator extends VersionSpec {
        protected final Version version;

        public TildeComparator(Version version) {
            this.version = Objects.requireNonNull(version);
        }

        @Override
        public boolean matches(Version ver) {
            if (this.version instanceof XRangeVersion && ((XRangeVersion) this.version).prefixLength < 3) {
                XRangeVersion xRangeVersion = (XRangeVersion) this.version;
                if (ver.prerelease != null) {
                    return false;
                } else if (xRangeVersion.prefixLength == 0) {
                    return true;
                } else if (xRangeVersion.prefixLength == 1) {
                    return this.version.major == ver.major;
                } else {
                    return this.version.major == ver.major && this.version.minor == ver.minor;
                }
            } else {
                if (this.version.prerelease != null && ver.prerelease != null) {
                    return this.version.major == ver.major && this.version.minor == ver.minor && this.version.patch == ver.patch;
                } else if (ver.prerelease != null) {
                    return false;
                } else {
                    return this.version.major == ver.major && this.version.minor == ver.minor && ver.compareTo(this.version) >= 0;
                }
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClass(), this.version);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            } else if (obj == this) {
                return true;
            } else {
                return version.equals(((TildeComparator) obj).version);
            }
        }
    }

    protected static class XRangeComparator extends VersionSpec {

        private final XRangeVersion xRangeVersion;

        public XRangeComparator(XRangeVersion xRangeVersion) {
            this.xRangeVersion = Objects.requireNonNull(xRangeVersion);
        }

        @Override
        public boolean matches(Version ver) {
            boolean result = true;
            switch (xRangeVersion.prefixLength) {
                case 3:
                    result = ver.getPatch() == this.xRangeVersion.getPatch();
                case 2:
                    result = result && ver.getMinor() == this.xRangeVersion.getMinor();
                case 1:
                    result = result && ver.getMajor() == this.xRangeVersion.getMajor();
            }

            return result;
        }

        @Override
        public String toString() {
            return xRangeVersion.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Objects.equals(xRangeVersion, ((XRangeComparator)o).xRangeVersion);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.getClass(), xRangeVersion.hashCode());
        }
    }
}
