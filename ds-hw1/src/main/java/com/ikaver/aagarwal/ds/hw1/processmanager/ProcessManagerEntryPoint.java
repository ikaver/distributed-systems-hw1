package com.ikaver.aagarwal.ds.hw1.processmanager;


public class ProcessManagerEntryPoint {

	public static void main(String args[]) {
		ProcessManagerImpl impl = new ProcessManagerImpl();
		for (int i = 0; i < 100; i++) {
			impl.launch(i,
					"com.ikaver.aagarwal.ds.hw1.helpers.GrepProcess",
					args);
		}
	}
}
