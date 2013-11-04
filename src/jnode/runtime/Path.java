package jnode.runtime;

import java.io.File;
import java.util.regex.Pattern;

public class Path {
		
	protected final Pattern trailingSeparators;
	protected final Pattern trailingFileAndSeparator;
	protected final Pattern directory;
	protected final String separator;
	
	public Path() {
		this(File.separator);
	}
	
	public Path(String separator) {
		String qps = Pattern.quote(separator);
		
		this.trailingSeparators = Pattern.compile("(?<!^)" + qps + "*$");
		this.trailingFileAndSeparator = Pattern.compile(qps + "+(?!.*" + qps + ".*).*$");
		this.directory = Pattern.compile(".*" + qps + "(?!$)");
		this.separator = separator;
	}
	
	
	public String resolve(String part, String rest) {
		File path = new File(part);
		File file2 = new File(path, rest);
		return file2.getPath();
	}
	
	public String dirname(String file) {
		// Trim trailing slashes (except a leading slash)
		file = trailingSeparators.matcher(file).replaceFirst("");
		
		String dirname = trailingFileAndSeparator.matcher(file).replaceFirst("");
		
		if (dirname.length() == file.length()) {
			dirname = ".";
		} else if (dirname.isEmpty()){
			dirname = this.separator;
		}
		
		return dirname;
	}
	
	public String basename(String file) {
		// Trim trailing slashes (except a leading slash)
		file = trailingSeparators.matcher(file).replaceFirst("");
		
		return directory.matcher(file).replaceFirst("");
	}
	
	public String basename(String file, String extension) {
		String basename = basename(file);
		if (basename.endsWith(extension)) {
			basename = basename.substring(0, basename.length() - extension.length());
		}
		return basename;
	}
	
}
