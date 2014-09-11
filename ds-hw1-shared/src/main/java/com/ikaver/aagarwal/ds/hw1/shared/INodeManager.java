package com.ikaver.aagarwal.ds.hw1.shared;

import java.util.List;
import java.rmi.*;

/**
 * A Node Manager is a “master” node that manages other nodes of the system. 
 * The Node Manager can ask its slave nodes (which we call Process Runners) to 
 * launch and terminate processes. Additionally, it can request a slave node to
 * suspend a process, serialize it and send it through the network and 
 * afterwards forward the serialized process to a different node to resume 
 * its execution.
 */
public interface INodeManager extends Remote {
  
  /**
   * Adds a process runner with the given connection string to the set of 
   * "available process runners". 
   * @param connectionString the socket address for the process runner
   * @return The process runner id if the node was added successfully, else null
   * @throws RemoteException
   */
  public String addProcessRunner(String connectionString) throws RemoteException;
  /**
   * Launches the process with the given class name and arguments in one of 
   * the available process runners.
   * @param className The full class name of the process to run.
   * @param args The arguments for the process
   * @return The process id > 0 if the process was launched successfully, else
   * it returns -1.
   * @throws RemoteException
   */
  public int launch(String className, String [] args) throws RemoteException;
  /**
   * Migrates the process with the given pid from the process runner with id
   * srcProcessRunner to the process runner with id destProcessRunner
   * @param pid The pid of the process
   * @param srcProcessRunner The process runner currently running the process
   * @param destProcessRunner The process runner that will continue running
   * the process
   * @return true iff successful.
   * @throws RemoteException
   */
  public boolean migrate(int pid, String srcProcessRunner, String destProcessRunner)
      throws RemoteException;
  /**
   * Terminates the process with the given pid
   * @param pid The pid of the process that will be terminated
   * @return true iff successful
   * @throws RemoteException
   */
  public boolean remove(int pid) throws RemoteException;
  /**
   * Returns a list of the states of all the currently available process runners.
   * @return The list of the states of all the currently available process runners.
   * @throws RemoteException
   */
  public List<ProcessRunnerState> getProcessRunnerState() throws RemoteException;
  
}
