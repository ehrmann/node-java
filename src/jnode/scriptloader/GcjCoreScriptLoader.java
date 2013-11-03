package jnode.scriptloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// TODO: this is really more like a URLScriptLoader
public class GcjCoreScriptLoader extends InputStreamScriptLoader {

	@Override
	protected InputStream getInputStream(String absolutePath) throws IOException {
		
		// FIXME: does absolutePath need to be escaped?
		URL url = new URL("core:/" + absolutePath);
		
		InputStream in = url.openStream();
		
		if (in == null) {
			throw new IOException("URL " + "core:/" + absolutePath + " not found");
		}
		
		return in;
	}

}
