package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessState;

@Singleton
public class ProcessManagerImpl implements IProcessManager, ProcessNotificationStateHandler {

	private final HashMap<Integer, Thread> pidProcessMap =
			new HashMap<Integer, Thread>();

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

			Constructor<IMigratableProcess> constructor = process.getConstructor(String[].class);
			IMigratableProcess newMigratableProcess = constructor.newInstance(new Object[]{args});

			// Running the new process now.
			ProcessThread thread = new ProcessThread(pid, newMigratableProcess, this);
			pidProcessMap.put(pid, thread);
			thread.start();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return true;
	}

	public void updateProcessState(int pid, ProcessState state) {
		if (state == ProcessState.DEAD) {
			pidProcessMap.remove(pid);
			System.out.println("Process with pid:" + pid + "is no longer running.");
		}
	}
}
