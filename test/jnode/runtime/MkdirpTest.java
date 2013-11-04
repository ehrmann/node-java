package jnode.runtime;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

public class MkdirpTest {

	private Context cx = Context.enter();
	private Scriptable scope = cx.initStandardObjects();
	private Mkdirp mkdirp = new Mkdirp(cx, scope);
	
	@Rule
    public TemporaryFolder folder = new TemporaryFolder();
	
	@Test
	public void testOneMkdirp() throws IOException {

		File dir = folder.newFolder();
		String toCreate = dir.getCanonicalPath() + File.separator + "foo";
		
		assertFalse((new File(toCreate)).exists());
		mkdirp.sync(toCreate);
		assertTrue((new File(toCreate)).exists());
	}
	
	@Test
	public void testTwoMkdirp() throws IOException {

		File dir = folder.newFolder();
		String toCreate = dir.getCanonicalPath() + File.separator + "foo" + File.separator + "bar";
		
		assertFalse((new File(toCreate)).exists());
		mkdirp.sync(toCreate);
		assertTrue((new File(toCreate)).exists());
	}
	
	@Test
	public void testThreeMkdirp() throws IOException {

		File dir = folder.newFolder();
		String toCreate = dir.getCanonicalPath() + File.separator + "foo" + File.separator + "bar" + File.separator + "baz";
		
		assertFalse((new File(toCreate)).exists());
		mkdirp.sync(toCreate);
		assertTrue((new File(toCreate)).exists());
	}
	
}
