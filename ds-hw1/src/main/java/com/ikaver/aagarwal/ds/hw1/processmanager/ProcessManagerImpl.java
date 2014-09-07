package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;

@Singleton
public class ProcessManagerImpl extends UnicastRemoteObject 
    implements IProcessManager, ProcessNotificationStateHandler {

  private static final long serialVersionUID = -8398758641188170913L;

	private final ConcurrentHashMap<Integer, Thread> pidThreadMap = new ConcurrentHashMap<Integer, Thread>();
	private final ConcurrentHashMap<Integer, IMigratableProcess> pidProcessMap = new ConcurrentHashMap<Integer, IMigratableProcess>();

	private final Logger logger = Logger.getLogger(ProcessManagerImpl.class);

	@Inject
	public ProcessManagerImpl() throws RemoteException {
		// Empty constructor for now.. Will add more stuff if needed.
	}

	/**
	 * The implemention should register/bind the process manager task to
	 * necessary port.
	 */
	public void run() {
	}

	public NodeState getState() {
		return null;
	}

	public String pack(int pid) {
		return null;
	}

	public boolean unpack(int pid, String serializedProcess) {
		return false;
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

			// Running the new process now.
			ProcessThread thread = new ProcessThread(pid, migratableProcess,
					this);
			pidThreadMap.put(pid, thread);
			thread.start();
		} catch (ClassNotFoundException e) {
			logger.warn(String.format("Unable to locate class %s.",
					classDefinition));
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
