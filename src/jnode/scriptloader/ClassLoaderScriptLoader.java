package jnode.scriptloader;

import org.mozilla.javascript.Script;

public class ClassLoaderScriptLoader implements ScriptLoader {

	protected final ClassLoader classLoader;
	
	public ClassLoaderScriptLoader(ClassLoader classLoader) {
		if (classLoader == null) {
			throw new NullPointerException("classLoader was null");
		}
		
		this.classLoader = classLoader;
	}

	@Override
	public Script loadScript(String absolutePath) {
		
		// Convert the path name to a class name.
		// This is slightly modified logic from the Rhino code.  The Rhino code appends a
		// sequence ID to the class name
        String baseName = "c";
        if (absolutePath.length() > 0) {
        	baseName = absolutePath.replaceAll("\\W", "_");
        	if (!Character.isJavaIdentifierStart(baseName.charAt(0))) {
        		baseName = "_" + baseName;
        	}
        }
        
        // Now try to load and instantiate the class
        Script result = null;
        try {
			Class<?> jsClass = Class.forName("org.mozilla.javascript.gen." + baseName);
			if (Script.class.isAssignableFrom(jsClass)) {
				@SuppressWarnings("unchecked")
				Class<Script> jsScriptClass = (Class<Script>)jsClass;
				result = jsScriptClass.newInstance();
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Failed to use precompiled class; " + e);
		} catch (InstantiationException e) {
			System.err.println("Failed to use precompiled class; " + e);
		} catch (IllegalAccessException e) {
			System.err.println("Failed to use precompiled class; "+ e);
		}
        
        return result;
	}

}
