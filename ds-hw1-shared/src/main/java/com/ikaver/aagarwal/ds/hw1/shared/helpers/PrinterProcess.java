package com.ikaver.aagarwal.ds.hw1.shared.helpers;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;

/**
 * A mock grep process which doesn't do a whole lot of fancy stuff.
 * Useful for testing and some debugging.
 */
public class PrinterProcess implements IMigratableProcess {

	private static final long serialVersionUID = 1L;
	private final long randomID;
	private volatile boolean suspending = false;
	private int NUM_TRIES = 100000;

	public PrinterProcess(String[] args) {
		System.out.println("Called with args" + args.toString());
		randomID = System.currentTimeMillis();
	}

	public void run() {
		System.out.println("The process is running." + randomID);
		while( NUM_TRIES > 0 && !suspending) {
			System.out.println("Grepping now!" + "with randomID" + ":" + randomID +
					".This is the ith invocation:" + NUM_TRIES);
			NUM_TRIES--;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		
		suspending = false;
	}

	public void suspend() {
		suspending = true;
        while (suspending);
		System.out.println("The process is suspended.");		
	}
}
