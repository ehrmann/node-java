package jnode.runtime;

import java.io.File;

public class Path {
	public String resolve(String part, String rest) {
		File path = new File(part);
		File file2 = new File(path, rest);
		return file2.getPath();
	}
	
	public String dirname(String file) {
		// Trim trailing slashes (except a leading slash)
		file = file.replaceFirst("(?<!^)/*$", "");
		
		String dirname = file.replaceFirst("/+[^/]*$", "");
		
		if (dirname.length() == file.length()) {
			dirname = ".";
		} else if (dirname.isEmpty()){
			dirname = "/";
		}
		
		return dirname;
	}
	
	public String basename(String file) {
		// Trim trailing slashes (except a leading slash)
		file = file.replaceFirst("(?<!^)/*$", "");
		
		return file.replaceFirst(".*[/](?!$)", "");
	}
	
	public String basename(String file, String extension) {
		String basename = basename(file);
		if (basename.endsWith(extension)) {
			basename = basename.substring(0, basename.length() - extension.length());
		}
		return basename;
	}
	
}
