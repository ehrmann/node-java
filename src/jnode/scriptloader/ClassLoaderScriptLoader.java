package jnode.scriptloader;

import jnode.JSCompiler;

import org.mozilla.javascript.Script;

public class ClassLoaderScriptLoader implements ScriptLoader {

	protected final ClassLoader classLoader;
	
	public ClassLoaderScriptLoader(ClassLoader classLoader) {
		if (classLoader == null) {
			throw new NullPointerException("classLoader was null");
		}
		
		this.classLoader = classLoader;
	}

	public Script loadScript(String absolutePath) {
		
		// Trim out the leading forward slash
		absolutePath = absolutePath.substring(1);
        
        // Now try to load and instantiate the class
        Script result = null;
        try {
			Class<?> jsClass = Class.forName(JSCompiler.getClassName(absolutePath));
			if (Script.class.isAssignableFrom(jsClass)) {
				@SuppressWarnings("unchecked")
				Class<Script> jsScriptClass = (Class<Script>)jsClass;
				result = jsScriptClass.newInstance();
			}
		} catch (ClassNotFoundException e) {
			// The path resolver might be trying different things, so this error could be benign
		} catch (InstantiationException e) {
			// TODO: this is a real error; the class is very much a Rhino JS class, but it's not acting like one.
			System.err.println("Failed to use precompiled class; " + e);
		} catch (IllegalAccessException e) {
			// TODO: this is a real error; the class is very much a Rhino JS class, but it's not acting like one.
			System.err.println("Failed to use precompiled class; "+ e);
		}
        
        return result;
	}

}
