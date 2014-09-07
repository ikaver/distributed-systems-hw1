package com.ikaver.aagarwal.ds.hw1.helpers;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;

/**
 * A mock grep process which doesn't do a whole lot of fancy stuff.
 * Useful for testing and some debugging.
 */
public class GrepProcess implements IMigratableProcess {

	private static final long serialVersionUID = 1L;
	private final long randomID;

	public GrepProcess(String[] args) {
		System.out.println("Called with args" + args.toString());
		randomID = System.currentTimeMillis();
	}

	public void run() {
		System.out.println("The process is running." + randomID);
		for (int i = 0; i < 10; i++) {
			System.out.println("Grepping now!" + "with randomID" + ":" + randomID);
		}
	}

	public void suspend() {
		System.out.println("The process is suspended.");		
	}
}
