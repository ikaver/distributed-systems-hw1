package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class ProcessManagerEntryPoint {

	private static final Logger logger = Logger.getLogger(ProcessManagerEntryPoint.class);

	public static void main(String args[]) {

		IProcessManager manager = null;
		Integer port;
		try {
			port = Integer.valueOf(args[0]);
		} catch(NumberFormatException e) {
			port = 2000;
		}

		logger.info(String.format("Running the process manager @%d", port));

		try {
			manager = new ProcessManagerImpl();
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
