package jnode;

import java.io.File;
import java.io.IOException;

public class Process {

	public String[] argv;
	
	public Process(String[] argv) {
		this.argv = argv;
	}
	
	public void on(Object event, Object callback) {
		System.out.println("");
		// Function
	}
	
	public String[] getArgv() {
		return this.argv;
	}
	
	public String cwd() {
		try {
			return new File(".").getCanonicalPath();
		} catch (IOException e) {
		return null;
		}
	}
	
}
