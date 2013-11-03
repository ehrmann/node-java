package jnode;

import java.util.Collections;
import java.util.Iterator;

public final class SimplePathResolutionStrategy implements PathResolutionStrategy {

	public static final PathResolutionStrategy INSTANCE = new SimplePathResolutionStrategy();
	
	private SimplePathResolutionStrategy() { }
	
	public Iterator<String> getLookupQueue(String absolutePath) {
		return Collections.singletonList(absolutePath).iterator();
	}

}
