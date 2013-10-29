package jnode;

import java.io.InputStream;

public class ClassloaderFileLoader implements FileLoader {

	protected final ClassLoader classloader;
	
	public ClassloaderFileLoader(ClassLoader classloader) {
		if (classloader == null) {
			throw new NullPointerException("classloader was null");
		}
		
		this.classloader = classloader;
	}
	
	public InputStream getInputStream(String filename) {
		return classloader.getResourceAsStream(filename);
	}

}
