package jnode.runtime;

import java.io.PrintStream;

public class Console {

	protected volatile PrintStream out;
	protected volatile PrintStream err;
	
	public Console() {
		this(System.out, System.err);
	}
	
	public Console(PrintStream out, PrintStream err) {
		if (out == null) {
			throw new RuntimeException("out was null");
		}
		if (err == null) {
			throw new RuntimeException("err was null");
		}
		
		this.out = out;
		this.err = err;
	}
	
	public void log(Object o) {
		this.out.println(o);
	}
	
	public void error(Object o) {
		this.out.println(o);
	}
}
