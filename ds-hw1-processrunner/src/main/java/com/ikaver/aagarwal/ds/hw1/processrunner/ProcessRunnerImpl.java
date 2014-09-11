package com.ikaver.aagarwal.ds.hw1.processrunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

/**
 * Describes a process runner, an entity capable of launching, packing, 
 * unpacking and terminating processes, as well as reporting the state
 * of the processes that its running periodically.
 */
@Singleton
public class ProcessRunnerImpl extends UnicastRemoteObject 
    implements IProcessRunner, ProcessNotificationStateHandler {

  private static final long serialVersionUID = -8398758641188170913L;

	private final ConcurrentHashMap<Integer, Thread> pidThreadMap 
	= new ConcurrentHashMap<Integer, Thread>();
	private final ConcurrentHashMap<Integer, IMigratableProcess> pidProcessMap 
	= new ConcurrentHashMap<Integer, IMigratableProcess>();

	private final Logger logger = Logger.getLogger(ProcessRunnerImpl.class);
	private final String PROCESS_MANAGER_ID = UUID.randomUUID().toString();

	@Inject
	public ProcessRunnerImpl() throws RemoteException {
		// Empty constructor for now.. Will add more stuff if needed.
	}

  /**
   * start() is called as soon as the user asks for this process 
   * runner to be added to the Node Manager. This should clear the state of 
   * the process runner before returning.
   * @throws RemoteException
   */
	public void start() {
	  Set<Integer> pids = new HashSet<Integer>(pidThreadMap.keySet());
	  for (Integer pid: pids) {
	    remove(pid);
	  }
	}
	
  /**
   * Returns the current state of the node, indicating which 
   * processes are currently running.
   * @return The current node state.
   * @see ProcessRunnerState
   */
  public synchronized ProcessRunnerState getState() {
		List<Integer> pids = new ArrayList<Integer>(pidProcessMap.keySet());
		// We are passing a random node id to the server id. The server may have a 
		// different naming convention for this slave node and hence 
		// can(will) choose to ignore it (as in the current implementation).
		ProcessRunnerState state = new ProcessRunnerState(PROCESS_MANAGER_ID, pids);
		return state;
	}
  /**
   * Suspends the migratable process with the given pid, and afterwards returns 
   * a serialized version of the suspended process.
   * @param pid The process pid
   * @return A serialized version of the suspended process with the given pid.
   * @see IMigratableProcess
   */
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

  /**
   * Deserializes and runs the migratable process with the given pid on the 
   * current node.
   * @param pid The process pid
   * @param serializedProcess A serialized version of the process that represents
   * its current state.
   */
	public boolean unpack(int pid, IMigratableProcess process) {
		if (process != null) {
			startProcess(pid, process);
			return true;
		} else {
			// Attempting to unpack a null process.
			return false;
		}
	}

  /**
   * Terminates the migratable process with the given pid, if its currently 
   * running on this node.
   * @param pid The process pid
   * @return true iff a process with the given pid was successfully terminated.
   */
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

  /**
   *  Launches a migratable process with the given pid, serialized
   * class definition and constructor arguments on this node. 
   * @param pid The id that will be assigned to this process
   * @param classDefinition The serialized class definition of the migratable
   * process
   * @param args The arguments with which the constructor will be invoked
   * @return true iff the process was launched successfully
   */
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

	/**
	 * Starts the process with the given pid
	 * @param pid the pid of the process
	 * @param migratableProcess process to start
	 */
	private void startProcess(int pid, IMigratableProcess migratableProcess) {
		// Running the new process now.
		ProcessThread thread = new ProcessThread(pid, migratableProcess,
				this);
		pidThreadMap.put(pid, thread);
		pidProcessMap.put(pid, migratableProcess);
		thread.start();
	}

	/**
	 * Updates the state of the process with given pid
	 */
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
