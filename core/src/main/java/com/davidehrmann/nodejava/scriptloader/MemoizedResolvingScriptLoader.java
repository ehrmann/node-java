package com.davidehrmann.nodejava.scriptloader;

import com.davidehrmann.nodejava.PathResolutionStrategy;
import com.davidehrmann.nodejava.script.CompilationException;
import com.davidehrmann.nodejava.script.ScriptCompiler;
import com.davidehrmann.nodejava.util.Path;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public class MemoizedResolvingScriptLoader<S> implements ResolvingScriptLoader<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoizedResolvingScriptLoader.class);

    protected final Map<String, ResolvedScript<S>> memos = new HashMap<>();
    protected final ScriptLoader scriptLoader;
    protected final PathResolutionStrategy pathResolutionStrategy;
    protected final ScriptCompiler<S> compiler;

    public MemoizedResolvingScriptLoader(ScriptLoader scriptLoader, PathResolutionStrategy pathResolutionStrategy, ScriptCompiler<S> compiler) {
        if (scriptLoader == null) {
            throw new NullPointerException("scriptLoader was null");
        }
        if (pathResolutionStrategy == null) {
            throw new NullPointerException("pathResolutionStrategy was null");
        }

        this.scriptLoader = scriptLoader;
        this.pathResolutionStrategy = pathResolutionStrategy;
        this.compiler = Objects.requireNonNull(compiler);
    }

    protected static String getDir(String pwd, String path) {
        if (!pwd.startsWith("/")) {
            throw new IllegalArgumentException("pwd isn't absolute");
        }

        if (path.indexOf('/') < 0) {
            path = "";
        } else {
            path = path.replaceFirst("/[^/]*$", "");
            if (path.isEmpty()) {
                path = "/";
            }
        }

        LinkedList<String> pwdStack = new LinkedList<>();

        // If path is absolute, ignore pwd
        if (!path.startsWith("/")) {
            pwdStack.addAll(Arrays.asList(pwd.split("/")));
        }

        pwdStack.addAll(Arrays.asList(path.split("/")));


        ListIterator<String> i = pwdStack.listIterator();

        while (i.hasNext()) {
            String dir = i.next();
            if (dir.isEmpty()) {
                i.remove();
            } else if (".".equals(dir)) {
                i.remove();
            } else if ("..".equals(dir)) {
                i.remove();

                if (!i.hasPrevious()) {
                    // FIXME: This probably shouldn't be a JS exception
                    // TODO: log.warn
                    throw new RuntimeException("../ trying to escape sandbox");
                }

                i.previous();
                i.remove();
            }
        }

        StringBuilder sb = new StringBuilder(pwd.length() * 2);
        sb.append('/');

        for (String dir : pwdStack) {
            sb.append(dir).append('/');
        }

        return sb.toString();
    }

    protected static String getFile(String path) {
        return path.replaceFirst(".*[/]", "");
    }

    public ResolvedScript<S> loadScript(String relativePath, String pwd) {
        String absolutePath = Path.canonicalizeAbsolutePath(pwd + "/" + relativePath);
        if (this.memos.containsKey(absolutePath)) {
            return this.memos.get(absolutePath);
        }

        Iterator<String> i = this.pathResolutionStrategy.getLookupQueue(pwd, relativePath);
        while (i.hasNext()) {
            String nextPath = i.next();

            ResolvedScript<S> result = null;
            if (memos.containsKey(nextPath)) {
                result = memos.get(nextPath);
            } else {
                try {
                    try (InputStream in = scriptLoader.loadScript(nextPath)) {
                        if (in == null) {
                            throw new FileNotFoundException();
                        }

                        if (nextPath.endsWith("/package.json")) {
                            ObjectMapper mapper = new ObjectMapper();
                            Package pkg = mapper.readValue(in, Package.class);
                            in.close();

                            if (pkg != null && pkg.main != null) {
                                // TODO: If main doesn't start with "./", should parent paths be search? i.e. can main be outside the module?
                                // Canonicalize main and replace package.json with it
                                String main = Path.canonicalizeAbsolutePath("/" + pkg.main).substring(1);
                                nextPath = nextPath.replaceFirst("[^/]*$", main);

                                try (InputStream in2 = scriptLoader.loadScript(nextPath)) {
                                    S script = compiler.compile(in2, nextPath);
                                    result = new DefaultResolvedScript<>(script, getDir("/", nextPath), getFile(nextPath));
                                }
                            }
                        } else {
                            S script = compiler.compile(in, nextPath);
                            result = new DefaultResolvedScript<>(script, getDir("/", nextPath), getFile(nextPath));
                        }
                    } catch (FileNotFoundException e) {
                        LOGGER.debug("Couldn't load module {} at {}: {}", relativePath, nextPath, e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (CompilationException e) {
                        e.printStackTrace();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                } finally {
                    memos.put(nextPath, result);
                }
            }

            if (result != null) {
                return result;
            }
        }

        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Package {
        @JsonProperty("main")
        public String main;
    }
}
