package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.rmi.Remote;
import java.rmi.RemoteException;

import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;

/**
 * Interface for handling process state changes.
 */
public interface ProcessNotificationStateHandler extends Remote {
	// Handler for updating the state of the process with a given pid and state.
	public void updateProcessState(int pid, ProcessState state) throws RemoteException;
}
