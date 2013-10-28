package org.apache.xmlbeans;

public class XmlCursor {

	public enum TokenType {
		START,
		TEXT,
		ATTR,
		PROCINST,
		COMMENT,
		STARTDOC,
		;
		
		public int intValue() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isStartdoc() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isEnddoc() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isStart() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isEnd() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isComment() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isContainer() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isAttr() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isNone() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isNamespace() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isText() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isProcinst() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public boolean isAnyAttr() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
	}
	
	public static class XmlBookmark {
		public XmlCursor createCursor() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public Object getKey() {
			throw new RuntimeException("XmlBeans placeholder called");
		}
		
		public XmlCursor toBookmark(XmlCursor a) {
			throw new RuntimeException("XmlBeans placeholder called");
		}
	}
}
