package jnode.scriptloader;

import java.io.IOException;
import java.io.InputStream;

public class ClasspathScriptLoader extends InputStreamScriptLoader {

	protected final ClassLoader classLoader;
	
	public ClasspathScriptLoader(ClassLoader classLoader) {
		if (classLoader == null) {
			throw new NullPointerException("classLoader was null");
		}
		
		this.classLoader = classLoader;
	}

	@Override
	protected InputStream getInputStream(String absolutePath) throws IOException {
		if (absolutePath.length() == 0) {
			throw new IOException("Empty absolutePath");
		} else if (absolutePath.length() == 1) {
			throw new IOException("No file in absolutePath");
		} else {
			// Trip the leading forward slash from the absolute path
			absolutePath = absolutePath.substring(1);
			
			// Open the resource and return the stream
			InputStream in = this.classLoader.getResourceAsStream(absolutePath);
			
			if (in == null) {
				throw new IOException("Couldn't find file '" + absolutePath + "' on the classpath");
			}
			
			return in;
		}
	}

}
