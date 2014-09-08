package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class ProcessManagerEntryPoint {

	public static void main(String args[]) {

		IProcessManager manager = null;
		try {
			manager = new ProcessManagerImpl();
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			Naming.rebind("//localhost:2000/ProcessManager", manager);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
