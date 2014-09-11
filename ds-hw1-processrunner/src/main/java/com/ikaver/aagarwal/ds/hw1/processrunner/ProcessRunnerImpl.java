package com.ikaver.aagarwal.ds.hw1.processrunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessRunnerState;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessNotificationStateHandler;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;
import com.ikaver.aagarwal.ds.hw1.processrunner.ProcessThread;

@Singleton
public class ProcessRunnerImpl extends UnicastRemoteObject 
    implements IProcessRunner, ProcessNotificationStateHandler {

  private static final long serialVersionUID = -8398758641188170913L;

	private final ConcurrentHashMap<Integer, Thread> pidThreadMap = new ConcurrentHashMap<Integer, Thread>();
	private final ConcurrentHashMap<Integer, IMigratableProcess> pidProcessMap = new ConcurrentHashMap<Integer, IMigratableProcess>();

	private final Logger logger = Logger.getLogger(ProcessRunnerImpl.class);
	
	private final String PROCESS_MANAGER_ID = UUID.randomUUID().toString();

	@Inject
	public ProcessRunnerImpl() throws RemoteException {
		// Empty constructor for now.. Will add more stuff if needed.
	}

	/**
	 * The implemention should register/bind the process manager task to
	 * necessary port.
	 */
	public void run() {
	}

	public ProcessRunnerState getState() {
		List<Integer> pids = new ArrayList<Integer>(pidProcessMap.keySet());
		// We are passing a random node id to the server id. The server may have a 
		// different naming convention for this slave node and hence 
		// can(will) choose to ignore it (as in the current implementation).
		ProcessRunnerState state = new ProcessRunnerState(PROCESS_MANAGER_ID, pids);
		return state;
	}

	public synchronized IMigratableProcess pack(int pid) {
		IMigratableProcess process = pidProcessMap.get(pid);
		if (process != null) {
			// Try to suspend the process and return the state.
			process.suspend();
			pidProcessMap.remove(pid);
			pidThreadMap.remove(pid);
		}
		return process;
	}

	public boolean unpack(int pid, IMigratableProcess process) {
		if (process != null) {
			startProcess(pid, process);
			return true;
		} else {
			// Attempting to unpack a null process.
			return false;
		}
	}

	public boolean remove(int pid) {
		// Synchronzied on this object since we don't want to fuck up the state of the
		// data structure containing the process information.
		synchronized (this) {
			Thread maybeRunningThread = pidThreadMap.get(pid);
			if (maybeRunningThread != null && maybeRunningThread.isAlive()) {
				IMigratableProcess runningProcess = pidProcessMap.get(pid);
				// Send a suspend signal to the thread so that it can cleanup it's state, if any.
				runningProcess.suspend();
				return true;
			} else {
				return false;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public boolean launch(int pid, String classDefinition, String[] args) {
		try {
			Class<IMigratableProcess> process = ((Class<IMigratableProcess>) Class
					.forName(classDefinition));

			Constructor<IMigratableProcess> constructor = process
					.getConstructor(String[].class);
			IMigratableProcess migratableProcess = constructor
					.newInstance(new Object[] { args });

			startProcess(pid, migratableProcess);
		} catch (ClassNotFoundException e) {
			logger.warn("Class not found", e);
			return false;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return false;
		} catch (SecurityException e) {
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void startProcess(int pid, IMigratableProcess migratableProcess) {
		// Running the new process now.
		ProcessThread thread = new ProcessThread(pid, migratableProcess,
				this);
		pidThreadMap.put(pid, thread);
		pidProcessMap.put(pid, migratableProcess);
		thread.start();
	}

	public void updateProcessState(int pid, ProcessState state) {
		if (state == ProcessState.DEAD) {
			// pid may have already been removed. However, this doesn't quite
			// bother us.
			Thread thread = pidThreadMap.remove(pid);
			pidProcessMap.remove(pid);
			if (thread != null) {
				logger.info(String.format("Process with pid :%d has finished.",
						pid));
			} else {
				logger.info(String
						.format("Process with pid: %d is already dead., pid"));
			}
		}
	}
}