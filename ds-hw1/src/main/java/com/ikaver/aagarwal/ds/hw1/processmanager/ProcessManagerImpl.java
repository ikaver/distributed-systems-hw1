package com.ikaver.aagarwal.ds.hw1.processmanager;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.google.inject.Inject;
import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class ProcessManagerImpl implements IProcessManager {

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
			IMigratableProcess newMigratableProcess = constructor.newInstance(args);

			// Running the new process now.
			Thread thread = new Thread(newMigratableProcess);
			pidProcessMap.put(pid, thread);
			thread.start();

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
}
