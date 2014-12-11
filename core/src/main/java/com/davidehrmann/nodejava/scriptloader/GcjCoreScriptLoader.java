package com.davidehrmann.nodejava.scriptloader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: this is really more like a URLScriptLoader
public class GcjCoreScriptLoader implements ScriptLoader {

    // Forward slash is "safe" because this is for the entire path, not just a single element
    private static final Pattern UNSAFE_PATH_CHARS = Pattern.compile("[^a-zA-Z0-9/.*_-]*");

    @Override
    public InputStream loadScript(String canonicalPath) throws IOException {
        canonicalPath = escapePath(canonicalPath);
        URL url = new URL("core:/" + canonicalPath);

        InputStream in = url.openStream();
        if (in == null) {
            throw new FileNotFoundException("URL " + "core:/" + canonicalPath + " not found");
        }

        return in;
    }

    protected String escapePath(String path) {
        StringBuilder sb = new StringBuilder(path.length() * 2);
        Matcher matcher = UNSAFE_PATH_CHARS.matcher(path);
        int lastEnd = 0;

        try {
            while (matcher.find()) {
                sb.append(path, lastEnd, matcher.start());
                sb.append(URLEncoder.encode(matcher.group(0), "UTF-8"));
                lastEnd = matcher.end();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        if (lastEnd == 0) {
            return path;
        } else {
            sb.append(path, lastEnd, path.length());
            return sb.toString();
        }
    }
}
