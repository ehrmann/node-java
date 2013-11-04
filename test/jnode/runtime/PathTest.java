package jnode.runtime;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PathTest {

	@Test
	public void testUnixDirname() {
		Path path = new Path("/");
		
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
	public void testUnixBasename() {
		Path path = new Path("/");
		
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

	@Test
	public void testUnixResolve() {
		Path path = new Path("/");
		
		assertEquals("/tmp/outfile", path.resolve("/home/dehrmann", "/tmp/outfile"));
		assertEquals("/home/dehrmann/outfile", path.resolve("/home/dehrmann", "outfile"));
		assertEquals("/home/outfile", path.resolve("/home/dehrmann", "../outfile"));
		assertEquals("/outfile", path.resolve("/", "outfile"));
		assertEquals("/outfile", path.resolve("/", "../outfile"));
		assertEquals("../../outfile", path.resolve(".", "../foo/../../outfile"));
	}
	
	public void testWinDirname() {
		Path path = new Path("\\");
		
		assertEquals(".", path.dirname("."));
		assertEquals("\\", path.dirname("\\"));
		assertEquals("\\", path.dirname("\\foo"));
		assertEquals("\\", path.dirname("\\foo\\"));
		assertEquals("\\foo", path.dirname("\\foo\\bar\\\\"));
		assertEquals("\\foo\\bar\\..", path.dirname("\\foo\\bar\\..\\\\..\\"));
		assertEquals(".", path.dirname("foo"));
		assertEquals(".", path.dirname(".\\foo"));
		assertEquals("foo\\bar\\..", path.dirname("foo\\bar\\..\\\\..\\"));
		assertEquals(".", path.dirname(".\\"));
	}
	
	@Test
	public void testWinBasename() {
		Path path = new Path("\\");
		
		assertEquals(".", path.basename("."));
		assertEquals(".", path.basename(".\\"));
		assertEquals("\\", path.basename("\\"));
		assertEquals(".", path.basename("\\.\\"));
		assertEquals("..", path.basename("\\..\\"));
		assertEquals("..", path.basename("\\..\\\\"));
		assertEquals(".", path.basename("\\.\\\\"));
		
		assertEquals("foo", path.basename("foo"));
		assertEquals("foo", path.basename("\\foo"));
		assertEquals("bar", path.basename("foo\\bar"));
		assertEquals("bar", path.basename("foo\\bar\\\\"));
		
		
		assertEquals("..", path.basename("\\..\\\\\\", "\\\\"));
		assertEquals("bar", path.basename("foo\\bar.html", ".html"));
	}
}
