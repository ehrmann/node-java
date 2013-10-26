package jnode.runtime;

import java.io.File;

public class Path {
	public String resolve(String part, String rest) {
		File path = new File(part);
		File file2 = new File(path, rest);
		return file2.getPath();
	}
	
	public String dirname(String file) {
		return null;
	}
	
	public String basename(String file) {
		return null;
	}
	
	
}
