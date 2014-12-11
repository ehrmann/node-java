package com.davidehrmann.nodejava.runtime;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Path {

    public final String sep;
    public final String delimiter;
    public final Path posix = null;
    public final Path win32 = null;
    protected final Pattern trailingSeparators;
    protected final Pattern trailingFileAndSeparator;
    protected final Pattern directory;
    protected final String separator;

    public Path() {
        this(File.separator);
    }

    public Path(String separator) {
        String qps = Path.quote(separator);

        this.trailingSeparators = Pattern.compile("(?<!^)" + qps + "*$");
        this.trailingFileAndSeparator = Pattern.compile(qps + "+(?!.*" + qps + ".*).*$");
        this.directory = Pattern.compile(".*" + qps + "(?!$)");
        this.separator = separator;

        this.sep = separator;
        this.delimiter = File.pathSeparator;
    }

    // GNU Classpath added this in 2012
    // See the GNU Classpath implementaton for license terms for *this* method (though it seems to be LGPL-friendly)
    // https://savannah.gnu.org/forum/forum.php?forum_id=7156
    private static String quote(String str) {
        int eInd = str.indexOf("\\E");
        if (eInd < 0) {
            // No need to handle backslashes.
            return "\\Q" + str + "\\E";
        }

        StringBuilder sb = new StringBuilder(str.length() + 16);
        sb.append("\\Q"); // start quote

        int pos = 0;
        do {
            // A backslash is quoted by another backslash;
            // 'E' is not needed to be quoted.
            sb.append(str.substring(pos, eInd))
                    .append("\\E" + "\\\\" + "E" + "\\Q");
            pos = eInd + 2;
        } while ((eInd = str.indexOf("\\E", pos)) >= 0);

        sb.append(str.substring(pos, str.length()))
                .append("\\E"); // end quote
        return sb.toString();
    }

    public String normalize(String p) {
        return null;
    }

    @NodeFunction
    public String join(String path1, String path2, String... pathx) {
        return null;
    }

    // TODO: make this JS varargs
    @NodeFunction
    public String resolve(String part1, String part2) {

        String[] parts = new String[]{part1, part2};
        LinkedList<String> pathList = new LinkedList<String>();

        for (String part : parts) {
            String[] subparts = part.split(Path.quote(separator) + "+");
            if (subparts.length == 0 && part.length() > 0) {
                subparts = new String[]{""};
            }

            for (int i = 0; i < subparts.length; i++) {
                if (i == 0 && subparts[i].isEmpty()) {
                    pathList.clear();
                    pathList.addLast(subparts[i]);
                } else if (".".equals(subparts[i])) {
                } else if ("..".equals(subparts[i])) {
                    if (pathList.isEmpty() || "..".equals(pathList.getLast())) {
                        pathList.addLast(subparts[i]);
                    } else if ("".equals(pathList.getLast())) {
                    } else {
                        pathList.removeLast();
                    }
                } else {
                    pathList.addLast(subparts[i]);
                }
            }
        }

        if (pathList.isEmpty()) {
            return ".";
        }

        if (pathList.equals(Collections.singletonList(""))) {
            return "/";
        }

        StringBuilder sb = new StringBuilder(pathList.size() * 10);

        for (String path : pathList) {
            sb.append(path).append(this.separator);
        }

        return sb.substring(0, sb.length() - 1);
    }

    @NodeFunction
    public boolean isAbsolute(String path) {
        return false;
    }

    @NodeFunction
    public String relative(String from, String to) {
        return null;
    }

    @NodeFunction
    public String dirname(String file) {
        // Trim trailing slashes (except a leading slash)
        file = trailingSeparators.matcher(file).replaceFirst("");

        String dirname = trailingFileAndSeparator.matcher(file).replaceFirst("");

        if (dirname.length() == file.length()) {
            dirname = ".";
        } else if (dirname.isEmpty()) {
            dirname = this.separator;
        }

        return dirname;
    }

    @NodeFunction
    public String basename(String file) {
        // Trim trailing slashes (except a leading slash)
        file = trailingSeparators.matcher(file).replaceFirst("");

        return directory.matcher(file).replaceFirst("");
    }

    @NodeFunction
    public String basename(String file, String extension) {
        String basename = basename(file);
        if (basename.endsWith(extension)) {
            basename = basename.substring(0, basename.length() - extension.length());
        }
        return basename;
    }

    @NodeFunction
    public String extname(String p) {
        return null;
    }

    @NodeFunction
    public Object parse(String pathString) {
        return null;
    }

    @NodeFunction
    public String format(Object pathObject) {
        return null;
    }
}
