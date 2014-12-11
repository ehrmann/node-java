package com.davidehrmann.nodejava.runtime;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Console {

    private static final Pattern FORMAT_PATTERN = Pattern.compile("%([sjd%])");
    private static final Pattern JSON_SPECIAL_CHARS = Pattern.compile("([\\p{Cntrl}\\\\/\"\\x08\\f\\n\\r\\t]+)");
    protected volatile PrintStream out;
    protected volatile PrintStream err;

    public Console() {
        this(System.out, System.err);
    }

    public Console(PrintStream out, PrintStream err) {
        if (out == null) {
            throw new RuntimeException("out was null");
        }
        if (err == null) {
            throw new RuntimeException("err was null");
        }

        this.out = out;
        this.err = err;
    }

    public static String format(String format, Object... args) {
        if (args == null || args.length == 0) {
            return format;
        }

        StringBuilder sb = new StringBuilder(format.length() + args.length * 8);

        Matcher matcher = FORMAT_PATTERN.matcher(format);
        int lastEnd = 0;
        int argIndex = 0;
        while (argIndex < args.length && matcher.find()) {
            sb.append(format, lastEnd, matcher.start());

            switch (matcher.group(1)) {
                case "s":
                case "d":
                    sb.append(args[argIndex]);
                    break;
                case "%":
                    sb.append('%');
                    break;
                case "j":
                    buildJson(args[argIndex], new HashSet<Object>(), sb);
                    break;
                default:
                    sb.append('%').append(matcher.group(1));
            }

            argIndex++;
            lastEnd = matcher.end();
        }

        sb.append(format, lastEnd, format.length());

        for (; argIndex < args.length; argIndex++) {
            sb.append(' ');
            // TODO;
            sb.append("TODO: util.inspect");
        }

        return sb.toString();
    }

    protected static void buildJson(Object root, Set<Object> visited, StringBuilder sb) {
        if (!visited.add(root)) {
            throw new IllegalArgumentException();
        }

        if (root == null) {
            sb.append("null");
        } else if (root instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) root).entrySet()) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;

                if (entry.getKey() == null) {
                    sb.append("null: ");
                } else {
                    sb.append('"');
                    buildJson(sb, entry.getKey().toString());
                    sb.append("\": ");
                }

                buildJson(entry.getValue(), visited, sb);
            }
            sb.append('}');
        } else if (root instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object child : (Iterable) root) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                buildJson(child, visited, sb);
            }
            sb.append(']');
        } else if (root instanceof Number) {
            sb.append(root);
        } else if (root instanceof Boolean) {
            sb.append((Boolean) root ? "true" : "false");
        } else if (root instanceof CharSequence) {
            sb.append('"');
            buildJson(sb, (CharSequence) root);
            sb.append('"');
        } else {
            throw new IllegalArgumentException("Bad JSON");
        }
    }

    protected static void buildJson(StringBuilder sb, CharSequence cs) {
        int lastEnd = 0;
        Matcher matcher = JSON_SPECIAL_CHARS.matcher(cs);
        while (matcher.find()) {
            sb.append(cs, lastEnd, matcher.start());
            String special = matcher.group(1);
            for (int i = 0; i < special.length(); i++) {
                switch (special.charAt(i)) {
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\\':
                        sb.append("\\\\");
                        break;
                    case '/':
                        sb.append("\\/");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    default:
                        sb.append('\\').append(String.format("%04x", (int) special.charAt(i)));
                }
            }
            lastEnd = matcher.end();
        }
        sb.append(cs, lastEnd, cs.length());
    }

    @NodeFunction
    public void log(String format, Object... args) {
        this.out.println(format(format, args));
    }

    @NodeFunction
    public void error(String format, Object... args) {
        this.err.println(format(format, args));
    }
}
