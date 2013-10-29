package jnode;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class GcjCoreFileLoader implements FileLoader {

	public InputStream getInputStream(String filename) {
		InputStream in = null;
		try {
			in = (new URL("core:/" + filename)).openStream();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
		
		return in;
	}

}
