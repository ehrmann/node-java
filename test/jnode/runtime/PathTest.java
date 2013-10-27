package jnode.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PathTest {

	@Test
	public void testDirname() {
		Path path = new Path();
		
		assertEquals(".", path.dirname("."));
		assertEquals("/", path.dirname("/"));
		assertEquals("/", path.dirname("/foo"));
		assertEquals("/", path.dirname("/foo/"));
		assertEquals("/foo", path.dirname("/foo/bar//"));
		assertEquals("/foo/bar/..", path.dirname("/foo/bar/..//../"));
		assertEquals(".", path.dirname("foo"));
		assertEquals(".", path.dirname("./foo"));
		assertEquals("foo/bar/..", path.dirname("foo/bar/..//../"));
		assertEquals(".", path.dirname("./"));
	}
	
	@Test
	public void testBasename() {
		Path path = new Path();
		
		assertEquals(".", path.basename("."));
		assertEquals(".", path.basename("./"));
		assertEquals("/", path.basename("/"));
		assertEquals(".", path.basename("/./"));
		assertEquals("..", path.basename("/../"));
		assertEquals("..", path.basename("/..//"));
		assertEquals(".", path.basename("/.//"));
		
		assertEquals("foo", path.basename("foo"));
		assertEquals("foo", path.basename("/foo"));
		assertEquals("bar", path.basename("foo/bar"));
		assertEquals("bar", path.basename("foo/bar//"));
		
		
		assertEquals("..", path.basename("/..///", "//"));
		assertEquals("bar", path.basename("foo/bar.html", ".html"));
	}

}
