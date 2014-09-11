package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class SubscribedProcessRunnersState {

  private Set<String> connectionStrings;
  private HashMap<String, String> processRunnerIdToConnectionStr;
  private HashMap<String, List<Integer>> processRunnerIdToPid;
  private HashMap<Integer, String> pidToProcessRunnerId;
  private int currentId;
  
  public SubscribedProcessRunnersState() {
    this.processRunnerIdToConnectionStr = new HashMap<String, String>();
    this.processRunnerIdToPid = new HashMap<String, List<Integer>>();
    this.pidToProcessRunnerId = new HashMap<Integer, String>();
    this.connectionStrings = new HashSet<String>();
    this.currentId = 0;
  }
  
  public int processRunnerCount() {
    return this.processRunnerIdToConnectionStr.size();
  }
  
  public Set<String> availableProcessRunners() {
    return new HashSet<String>(this.processRunnerIdToConnectionStr.keySet());
  }
  
  public String connectionStringForProcessRunner(String processRunner) {
    return this.processRunnerIdToConnectionStr.get(processRunner);
  }
  
  public String getProcessRunnerForPid(Integer pid) {
    return this.pidToProcessRunnerId.get(pid);
  }
  
  /**
   * Adds a new process runner to the available process runners set, 
   * using the given connection string.
   * @param connectionStr the connection string for the node
   * @return the process runner id for the given connection string.
   */
  public String addProcessRunner(String connectionStr) throws NullPointerException {
    if(connectionStr == null) {
      throw new NullPointerException("Connection string cannot be null");
    }
    String processRunnerId = null;
    if(!this.connectionStrings.contains(connectionStr)) {
      ++this.currentId;
      processRunnerId = ""+this.currentId;
      this.processRunnerIdToConnectionStr.put(processRunnerId, connectionStr);
      this.connectionStrings.add(connectionStr);
      this.processRunnerIdToPid.put(processRunnerId, new LinkedList<Integer>());
    }
    return processRunnerId;
  }
  
  public void removeProcessRunner(String processRunnerId) throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    this.connectionStrings.remove(this.processRunnerIdToConnectionStr.get(processRunnerId));
    this.processRunnerIdToConnectionStr.remove(processRunnerId);
    this.processRunnerIdToPid.remove(processRunnerId);
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
    
  public boolean addProcessToProcessRunner(Integer pid, String processRunnerId) throws NullPointerException{
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
  
  public boolean removeProcessFromCurrentProcessRunner(Integer pid) throws NullPointerException {
    if(pid == null) {
      throw new NullPointerException("pid cannot be null");
    }
    String node = this.pidToProcessRunnerId.get(pid);
    return node == null ? false : this.removeProcessFromProcessRunner(pid, node);
  }
  
  public List<Integer> getProcessList(String processRunnerId) throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    List<Integer> processList = this.processRunnerIdToPid.get(processRunnerId);
    return processList == null ? null : new LinkedList<Integer>(processList);
  }
  
  public void setProcessList(String processRunnerId, List<Integer> processList) throws NullPointerException {
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
  
  public void clearProcessList(String processRunnerId) throws NullPointerException {
    if(processRunnerId == null) {
      throw new NullPointerException("Process runner id cannot be null");
    }
    this.setProcessList(processRunnerId, new LinkedList<Integer>());
  }
  
  public boolean removeProcessFromProcessRunner(Integer pid, String processRunnerId) throws NullPointerException {
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
