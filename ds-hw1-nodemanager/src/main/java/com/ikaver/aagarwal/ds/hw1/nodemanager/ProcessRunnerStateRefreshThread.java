package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessRunnerState;

/**
 * This class polls the available process runners and queries them for the
 * current process state. If some process runner doesn't respond it adds it
 * to a "deadProcessRunners" set. Periodically this set is checked to see if 
 * any dead process runners come back to life, and if they do they are readded
 * to the available process runners set.
 */
public class ProcessRunnerStateRefreshThread implements Runnable {

  /**
   * Returns a process runner reference given a connection string.
   */
  private IProcessRunnerFactory processManagerFactory;
  /**
   * Lock that protects the SubscribedProcessRunnersState.
   */
  private ReadWriteLock stateLock;
  /**
   * The current state of the available process runners.
   */
  private SubscribedProcessRunnersState state;
  /**
   * Saves the connection strings of the process runners that are not responding to queries.
   */
  private Set<String> deadProcessRunners;

  private static final Logger logger 
  = LogManager.getLogger(ProcessRunnerStateRefreshThread.class.getName());

  @Inject
  public ProcessRunnerStateRefreshThread(
      @Named("ProcessManagerFactory") IProcessRunnerFactory factory,
      @Named("NMState") SubscribedProcessRunnersState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    this.processManagerFactory = factory;
    this.state = state;
    this.stateLock = stateLock;
    this.deadProcessRunners = new HashSet<String>();
  }

  /**
   * Checks the state of the available process runners and checks if any 
   * dead process runners have come back to life. 
   */
  public void run(){
    this.queryAvailableProcessRunners();
    this.queryDeadProcessRunners();
  }
  
  /**
   * Queries the available process runners for the current states of their 
   * running processes and updates the internal state.
   */
  private void queryAvailableProcessRunners() {
    HashMap<String,String> unresponsiveNodes = new HashMap<String, String>();
    Set<String> availableNodes = null;

    this.stateLock.readLock().lock();
    try{
      availableNodes = this.state.availableProcessRunners();
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    
    //for each process runner, check if they are responsive and get the 
    // current state of all of the process currently running.
    for(String processRunnerId : availableNodes) {
      
      String connectionForId = null;
      this.stateLock.readLock().lock();
      try {
        connectionForId = this.state.connectionStringForProcessRunner(processRunnerId);
      }
      finally {
        this.stateLock.readLock().unlock();
      }
      
      //Query for the state and update internal state if possible
      IProcessRunner manager = this.processManagerFactory.processRunnerFromConnectionStr(connectionForId);
      boolean contactSuccess = false;
      if(manager != null) {
        try {
          this.updateNodeState(processRunnerId, manager.getState());
          contactSuccess = true;
        }
        catch(RemoteException e) {
          logger.error("Bad get state", e);
        }
      }
      //Process runner did not respond. Add to "dead" set.
      if(!contactSuccess) {
        unresponsiveNodes.put(processRunnerId, connectionForId);
      }
    }
    //Remove all "dead" nodes from the available set.
    this.removeUnresponsiveNodes(unresponsiveNodes);
  }

  /**
   * Check if any dead process runners have come back to life.
   */
  private void queryDeadProcessRunners() {
    Set<String> backToLifeNodes = new HashSet<String>();
    for(String deadProcessRunner : this.deadProcessRunners) {
      //try to establish contact
      IProcessRunner manager = this.processManagerFactory.processRunnerFromConnectionStr(deadProcessRunner);
      boolean contactSuccess = false;
      if(manager != null) {
        try {
          manager.getState();
          contactSuccess = true;
        }
        catch(RemoteException e) { }
      }
      if(contactSuccess) {
        //contact was made! readd to process list.
        this.stateLock.writeLock().lock();
        try {
          String newId = this.state.addProcessRunner(deadProcessRunner);
          backToLifeNodes.add(deadProcessRunner);
          System.out.printf("Node %s is back alive with id %s\n", deadProcessRunner, newId);
        }
        catch(Exception e) {
          logger.error("Bad node revive", e);
        }
        finally {
          this.stateLock.writeLock().unlock();
        }
      }
    }
    //remove all the "back to life" nodes from the "dead" set.
    this.deadProcessRunners.removeAll(backToLifeNodes);
  }
  
  /**
   * Updates the internal state of the process runner with id (processRunnerId)
   * @param processRunnerId the id of the process runner
   * @param state the current process runner state
   */
  private void updateNodeState(String processRunnerId, ProcessRunnerState state) {
    this.stateLock.writeLock().lock();
    try {
      if(state != null) {
        this.state.setProcessList(processRunnerId, state.getRunningProcesses());
      }
      else {
        this.state.clearProcessList(processRunnerId);
      }
    }
    catch(Exception e) {
      logger.error("Bad node state update", e);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  /**
   * Adds all the unresponsive process runners to a "dead" set.
   * @param unresponsiveProcessRunners A map from process runner connection strings
   * to process runner ids. All of these process runners will be added to the dead list.
   */
  private void removeUnresponsiveNodes(HashMap<String,String> unresponsiveProcessRunners) {
    for(String unresponsive : unresponsiveProcessRunners.keySet()) {
      String connectionStr = unresponsiveProcessRunners.get(unresponsive);      
      System.out.printf("Node %s with id %s is unresponsive, disconnecting...\n",
          connectionStr, unresponsive);  
      this.deadProcessRunners.add(connectionStr);

      this.stateLock.writeLock().lock();
      try {
        //remove from internal state.
        this.state.removeProcessRunner(unresponsive);
      }
      catch(Exception e) {
        logger.error("Bad node remove", e);
      }
      finally {
        this.stateLock.writeLock().unlock();
      }

    }   
  }


}
