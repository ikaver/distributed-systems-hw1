package com.ikaver.aagarwal.ds.hw1.processrunner;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

public class ProcessRunnerEntryPoint {

	private static final Logger logger = Logger.getLogger(ProcessRunnerEntryPoint.class);

	public static void main(String args[]) {

		IProcessRunner manager = null;
		Integer port = -1;
		try {
			port = Integer.valueOf(args[0]);
		} catch(NumberFormatException e) {
			System.out.println("Usage: java -jar processRunner.jar <PORT_NUMBER>");
			System.exit(1);
		}

		logger.info(String.format("Running the process manager @%d", port));

		try {
			manager = new ProcessRunnerImpl();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Naming.rebind(String.format("//localhost:%d/ProcessManager", port), manager);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
