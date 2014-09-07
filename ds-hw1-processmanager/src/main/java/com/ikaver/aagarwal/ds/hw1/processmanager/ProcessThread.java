package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.rmi.RemoteException;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;

/**
 * A thread for each process which is run by the process manager.
 */
class ProcessThread extends Thread {

	private final IMigratableProcess process;
	private final Integer pid;
	private final ProcessNotificationStateHandler processNotificationStateHandler;

	public ProcessThread(Integer pid, IMigratableProcess process,
			ProcessNotificationStateHandler processNotificationStateHandler) {
		this.process = process;
		this.pid = pid;
		this.processNotificationStateHandler = processNotificationStateHandler;
	}

	@Override
	public void run() {
		process.run();
		try {
			processNotificationStateHandler.updateProcessState(pid,
					ProcessState.DEAD);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}