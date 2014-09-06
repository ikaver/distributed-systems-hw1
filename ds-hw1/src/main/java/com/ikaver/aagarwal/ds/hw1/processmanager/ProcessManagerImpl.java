package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;

@Singleton
public class ProcessManagerImpl implements IProcessManager,
		ProcessNotificationStateHandler {

	private final ConcurrentHashMap<Integer, Thread> pidProcessMap = new ConcurrentHashMap<Integer, Thread>();

	private final Logger logger = Logger.getLogger(ProcessManagerImpl.class);

	@Inject
	public ProcessManagerImpl() {
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
		return false;
	}

	@SuppressWarnings("unchecked")
	public boolean launch(int pid, String classDefinition, String[] args) {
		try {
			Class<IMigratableProcess> process = ((Class<IMigratableProcess>) Class
					.forName(classDefinition));

			Constructor<IMigratableProcess> constructor = process
					.getConstructor(String[].class);
			IMigratableProcess newMigratableProcess = constructor
					.newInstance(new Object[] { args });

			// Running the new process now.
			ProcessThread thread = new ProcessThread(pid, newMigratableProcess,
					this);
			pidProcessMap.put(pid, thread);
			thread.start();
		} catch (ClassNotFoundException e) {
			logger.warn(String.format("Unable to locate class %s.",
					classDefinition));
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
			// pid may have already beeb removed. However, this doesn't quite
			// bother us.
			Thread thread = pidProcessMap.remove(pid);
			if (thread != null) {
				logger.info(String.format("Process with pid :%d has finished.", pid));
			} else {
				logger.info(String.format("Process with pid: %d is already dead., pid"));
			}
		}
	}
}
