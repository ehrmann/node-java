/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.davidehrmann.nodejava.scriptloader;

import com.davidehrmann.nodejava.util.PaddingReader;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

public class PaddingReaderTest {

	@Test
	public void testWrappedExecutableReader() throws IOException {
		final char[] header = "(function() { // ".toCharArray();
		final char[] trailer = "\n})();".toCharArray();
		final char[] matchPrefix = "#!".toCharArray();
		
		String test1 = "#! /bin/sh\nconsole.log('hello');";
		String result1 = "(function() { // #! /bin/sh\nconsole.log('hello');\n})();";
		
		assertEquals(result1, readToString(new PaddingReader(new StringReader(test1), header, trailer, matchPrefix)));
		assertEquals(result1, readToStringBulk(new PaddingReader(new StringReader(test1), header, trailer, matchPrefix)));
		
		
		String test2 = "!# /bin/sh\nconsole.log('hello');";
		String result2 = test2;
		
		assertEquals(result2, readToString(new PaddingReader(new StringReader(test2), header, trailer, matchPrefix)));
		assertEquals(result2, readToStringBulk(new PaddingReader(new StringReader(test2), header, trailer, matchPrefix)));
		
		String test3 = "#";
		String result3 = test3;
		
		assertEquals(result3, readToString(new PaddingReader(new StringReader(test3), header, trailer, matchPrefix)));
		assertEquals(result3, readToStringBulk(new PaddingReader(new StringReader(test3), header, trailer, matchPrefix)));
		
		String test4 = "#!";
		String result4 = "(function() { // #!\n})();";
		
		assertEquals(result4, readToString(new PaddingReader(new StringReader(test4), header, trailer, matchPrefix)));
		assertEquals(result4, readToStringBulk(new PaddingReader(new StringReader(test4), header, trailer, matchPrefix)));
	}
	
	protected static String readToString(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int r;
		while ((r = reader.read()) >= 0) {
			sb.append((char)r);
		}
		return sb.toString();
	}
	
	protected static String readToStringBulk(Reader reader) throws IOException {
		char[] buffer = new char[8];
		StringBuilder sb = new StringBuilder();
		int r;
		while ((r = reader.read(buffer)) >= 0) {
			sb.append(buffer, 0, r);
		}
		return sb.toString();
	}
}
