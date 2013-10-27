package jnode.runtime;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mozilla.javascript.JavaScriptException;

public class Mkdirp {

	public String sync(String path) {
		return sync(path, 777);
	}

	public String sync(String path, int mode) {

		int owner = (mode / 100) % 10;
		int everyone = (mode / 1) % 10;
		
		List<File> dirsToCreate = new ArrayList<File>(8);

		File dir = new File(path);

		while (!dir.exists()) {
			dirsToCreate.add(dir);
			dir = dir.getParentFile();
		}
		
		Collections.reverse(dirsToCreate);
		
		for (File dirToCreate : dirsToCreate) {
			if (!dirToCreate.mkdir()) {
				throw new JavaScriptException("Failed to create directory: " + path, "mkdirSync", 1);
			}
		}
		
		for (File dirToCreate : dirsToCreate) {
			dirToCreate.setExecutable((owner & 0x1) != 0, true);
			dirToCreate.setWritable((owner & 0x2) != 0, true);
			dirToCreate.setReadable((owner & 0x4) != 0, true);

			dirToCreate.setExecutable((everyone & 0x1) != 0, false);
			dirToCreate.setWritable((everyone & 0x2) != 0, false);
			dirToCreate.setReadable((everyone & 0x4) != 0, false);
		}

		try {
			return dirsToCreate.isEmpty() ? null : dirsToCreate.get(0).getCanonicalPath();
		} catch (IOException e) {
			throw new JavaScriptException("Failed to create directory: " + path, "mkdirSync", 2);
		}
	}

}
