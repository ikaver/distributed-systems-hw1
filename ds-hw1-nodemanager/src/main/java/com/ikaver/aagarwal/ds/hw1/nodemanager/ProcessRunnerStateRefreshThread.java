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

public class ProcessRunnerStateRefreshThread implements Runnable {

  private IProcessRunnerFactory processManagerFactory;
  private ReadWriteLock stateLock;
  private Set<String> deadNodes;
  private SubscribedProcessRunnersState state;

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
    this.deadNodes = new HashSet<String>();
  }

  public void run(){
    this.queryAvailableProcessRunners();
    this.queryDeadProcessRunners();
  }
  
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
    
    for(String processRunnerId : availableNodes) {
      
      String connectionForId = null;
      this.stateLock.readLock().lock();
      try {
        connectionForId = this.state.connectionStringForProcessRunner(processRunnerId);
      }
      finally {
        this.stateLock.readLock().unlock();
      }
      
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
      if(!contactSuccess) {
        unresponsiveNodes.put(processRunnerId, connectionForId);
      }
    }
    this.removeUnresponsiveNodes(unresponsiveNodes);
  }
  
  private void queryDeadProcessRunners() {
    Set<String> backToLifeNodes = new HashSet<String>();
    for(String deadProcessRunner : this.deadNodes) {
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
    this.deadNodes.removeAll(backToLifeNodes);
  }
  
  private void updateNodeState(String processRunnerId, ProcessRunnerState nodeState) {
    this.stateLock.writeLock().lock();
    try {
      if(nodeState != null) {
        this.state.setProcessList(processRunnerId, nodeState.getRunningProcesses());
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
  
  private void removeUnresponsiveNodes(HashMap<String,String> unresponsiveProcessRunners) {
    for(String unresponsive : unresponsiveProcessRunners.keySet()) {
      String connectionStr = unresponsiveProcessRunners.get(unresponsive);      
      System.out.printf("Node %s with id %s is unresponsive, disconnecting...\n",
          connectionStr, unresponsive);  
      this.deadNodes.add(connectionStr);

      this.stateLock.writeLock().lock();
      try {
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
