package jnode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JNodePathResolutionStrategy implements PathResolutionStrategy {
	
	public Iterator<String> getLookupQueue(String absolutePath) {
		List<String> queue = new ArrayList<String>(3);

		// TODO: Make sure this follows the require spec.

		if (absolutePath.endsWith(".js")) {
			queue.add(absolutePath);
		} else {
			queue.add(absolutePath + ".js");
			queue.add(absolutePath + "/index.js");
			queue.add(absolutePath);

			// TODO: package.json
		}
		
		return queue.iterator();
	}

}
