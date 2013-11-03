package jnode;

import java.util.Iterator;

public interface PathResolutionStrategy {

	public Iterator<String> getLookupQueue(String absolutePath);
	
}
