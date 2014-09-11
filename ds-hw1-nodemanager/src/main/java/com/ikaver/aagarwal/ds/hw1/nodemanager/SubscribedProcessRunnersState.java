package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Saves the current state of the cluster. The state contains the ids of all
 * the available process runners and the state of all of the processes that 
 * they are currently running. This class only updates the internal state,
 * it does not do any communication with the process runners whatsoever.
 */
public class SubscribedProcessRunnersState {

  /**
   * A set of connection strings of currently available process runners.
   */
  private Set<String> connectionStrings;
  /**
   * Maps a process runner id to its conenction string
   */
  private HashMap<String, String> processRunnerIdToConnectionStr;
  /**
   * Maps a process runner id to all of the process ids that its currently 
   * running
   */
  private HashMap<String, List<Integer>> processRunnerIdToPid;
  /**
   * Maps a process id to the node in which its currently running.
   */
  private HashMap<Integer, String> pidToProcessRunnerId;
  /**
   * The id that will be assigned to the next process runner that is added.
   */
  private int currentProcessRunnerId;
  
  public SubscribedProcessRunnersState() {
    this.processRunnerIdToConnectionStr = new HashMap<String, String>();
    this.processRunnerIdToPid = new HashMap<String, List<Integer>>();
    this.pidToProcessRunnerId = new HashMap<Integer, String>();
    this.connectionStrings = new HashSet<String>();
    this.currentProcessRunnerId = 0;
  }
  
  /**
   * Returns the amount of available process runners
   * @return the amount of available process runners
   */
  public int processRunnerCount() {
    return this.processRunnerIdToConnectionStr.size();
  }
  
  /**
   * Returns a copy of the set of ids of the currently available process runners.
   * @return a copy of the set of ids of the currently available process runners.
   */
  public Set<String> availableProcessRunners() {
    return new HashSet<String>(this.processRunnerIdToConnectionStr.keySet());
  }
  
  /**
   * Returns the connection string for the process runner with the given id
   * @param processRunnerId the id of the process runner
   * @return the connection string for the process runner with the given id
   */
  public String connectionStringForProcessRunner(String processRunnerId) {
    return this.processRunnerIdToConnectionStr.get(processRunnerId);
  }
  
  /**
   * Returns the id of the process runner currently running the process with
   * pid (pid)
   * @param pid The pid of the process
   * @return the id of the process runner currently running the process with
   * pid (pid) or null if no such process is currently running.
   */
  public String getProcessRunnerForPid(Integer pid) {
    return this.pidToProcessRunnerId.get(pid);
  }
  
  /**
   * Adds a new process runner to the available process runners set, 
   * using the given connection string.
   * @param connectionStr the connection string for the node. Must be != null.
   * @return the process runner id for the given connection string.
   */
  public String addProcessRunner(String connectionStr) throws NullPointerException {
    if(connectionStr == null) {
      throw new NullPointerException("Connection string cannot be null");
    }
    String processRunnerId = null;
    if(!this.connectionStrings.contains(connectionStr)) {
      ++this.currentProcessRunnerId;
      processRunnerId = ""+this.currentProcessRunnerId;
      this.processRunnerIdToConnectionStr.put(processRunnerId, connectionStr);
      this.connectionStrings.add(connectionStr);
      this.processRunnerIdToPid.put(processRunnerId, new LinkedList<Integer>());
    }
    return processRunnerId;
  }
  
  /**
   * Removes the process runner with the given id from the internal state. This
   * also deletes all of its currently running processes.
   * @param processRunnerId the id of the process runner to delete. 
   * Must be != null.
   * @throws NullPointerException
   */
  public void removeProcessRunner(String processRunnerId) 
      throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    //delete this node from internal states
    this.connectionStrings.remove(this.processRunnerIdToConnectionStr.get(processRunnerId));
    this.processRunnerIdToConnectionStr.remove(processRunnerId);
    this.processRunnerIdToPid.remove(processRunnerId);
    //delete all pids associated to this node
    Set<Integer> pidsToRemove = new HashSet<Integer>();
    for(Integer pid : this.pidToProcessRunnerId.keySet()) {
      if(this.pidToProcessRunnerId.get(pid).equals(processRunnerId)) {
        pidsToRemove.add(pid);
      }
    }
    for(Integer pid : pidsToRemove) {
      this.pidToProcessRunnerId.remove(pid);
    }
  }
    
  /**
   * Associates the process with the given pid to the process runner with id
   * processRunnerId in the internal state.
   * @param pid the pid of the process. Must be != null.
   * @param processRunnerId the id of the process runner. Must be != null.
   * @return true iff the pid was associated successfully.
   * @throws NullPointerException
   */
  public boolean addProcessToProcessRunner(Integer pid, String processRunnerId) 
      throws NullPointerException{
    if(pid == null) {
      throw new NullPointerException("PID cannot be null");
    }
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    this.pidToProcessRunnerId.put(pid, processRunnerId);
    this.processRunnerIdToPid.get(processRunnerId).add(pid);
    return true;
  }
  
  /**
   * Removes the process with pid (pid) form the currently running processes
   * list.
   * @param pid The pid of the process. Must be != null.
   * @return true iff the process was removed successfully.
   * @throws NullPointerException
   */
  public boolean removeProcessFromCurrentProcessRunner(Integer pid) 
      throws NullPointerException {
    if(pid == null) {
      throw new NullPointerException("pid cannot be null");
    }
    String node = this.pidToProcessRunnerId.get(pid);
    return node == null ? false : this.removeProcessFromProcessRunner(pid, node);
  }
  
  /**
   * Returns a copy of the process list for the process runner with id 
   * processRunnerId.
   * @param processRunnerId The id of the process runner. Must be != null.
   * @return a copy of the process list for the process runner with id 
   * processRunnerId, or null if no such process runner exists.
   * @throws NullPointerException
   */
  public List<Integer> getProcessList(String processRunnerId) 
      throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    List<Integer> processList = this.processRunnerIdToPid.get(processRunnerId);
    return processList == null ? null : new LinkedList<Integer>(processList);
  }
  
  /**
   * Sets the process list of the given process runner to the new given list.
   * @param processRunnerId the id of the process runner. Must be != null.
   * @param processList The updated process runner state. Must be != null.
   * @throws NullPointerException
   */
  public void setProcessList(String processRunnerId, List<Integer> processList) 
      throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    if(processList == null) {
      throw new NullPointerException("Process list cannot be null");
    }
    
    if(this.processRunnerIdToPid.containsKey(processRunnerId)) {
      for(Integer pid : this.processRunnerIdToPid.get(processRunnerId)) {
        this.pidToProcessRunnerId.remove(pid);
      }
    }
    this.processRunnerIdToPid.put(processRunnerId, processList);
    for(Integer pid : processList) {
      this.pidToProcessRunnerId.put(pid, processRunnerId);
    }
  }
  
  /**
   * Removes all processes from the process list of the process runner with id
   * (processRunnerId)
   * @param processRunnerId the id of the process runner. Must be != null.
   * @throws NullPointerException
   */
  public void clearProcessList(String processRunnerId) throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    this.setProcessList(processRunnerId, new LinkedList<Integer>());
  }
  
  /**
   * Removes the process with pid (pid) from the process list of the process
   * runner with id (processRunnerId)
   * @param pid the id of the process. Must be != null.
   * @param processRunnerId the id of the process runner. Must be != null.
   * @return true if the process was removed, false otherwise.
   * @throws NullPointerException
   */
  public boolean removeProcessFromProcessRunner(Integer pid, String processRunnerId) 
      throws NullPointerException {
    if(pid == null) {
      throw new NullPointerException("pid cannot be null");
    }
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    boolean foundProcessRunner = false;
    if(processRunnerId != null && processRunnerId.equals(this.pidToProcessRunnerId.get(pid))) {
      this.pidToProcessRunnerId.remove(pid);
      this.processRunnerIdToPid.get(processRunnerId).remove(pid);
      foundProcessRunner = true;
    }
    return foundProcessRunner;
  }
  
}
