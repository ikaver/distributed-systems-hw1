package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerStateRefreshThread implements Runnable {

  private ReadWriteLock stateLock;
  private SubscribedNodesState state;

  private static final Logger logger 
  = LogManager.getLogger(ProcessLauncher.class.getName());

  @Inject
  public NodeManagerStateRefreshThread(
      @Named("NMState") SubscribedNodesState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    this.state = state;
    this.stateLock = stateLock;
  }

  public void run(){
    Set<String> unresponsiveNodes = new HashSet<String>();
    Set<String> availableNodes = null;

    this.stateLock.readLock().lock();
    try{
      availableNodes = this.state.availableNodes();
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    
    for(String nodeId : availableNodes) {
      
      String connectionForId = null;
      this.stateLock.readLock().lock();
      try {
        connectionForId = this.state.connectionStringForNode(nodeId);
      }
      finally {
        this.stateLock.readLock().unlock();
      }
      
      IProcessManager manager = ProcessManagerFactory.processManagerFromConnectionString(connectionForId);
      boolean contactSuccess = false;
      if(manager != null) {
        try {
          this.updateNodeState(nodeId, manager.getState());
          contactSuccess = true;
        }
        catch(RemoteException e) {
          logger.error("Bad get state", e);
        }
      }
      if(!contactSuccess) {
        unresponsiveNodes.add(nodeId);
      }
    }
    this.removeUnresponsiveNodes(unresponsiveNodes);
  }
  
  private void updateNodeState(String nodeId, NodeState nodeState) {
    this.stateLock.writeLock().lock();
    try {
      if(nodeState != null) {
        this.state.setProcessList(nodeId, nodeState.getRunningProcesses());
      }
      else {
        this.state.clearProcessList(nodeId);
      }
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  private void removeUnresponsiveNodes(Set<String> unresponsiveNodes) {
    for(String unresponsive : unresponsiveNodes) {
      this.stateLock.writeLock().lock();
      try {
        System.out.printf("Node %s is unresponsive, disconnecting...\n", unresponsive);        
        this.state.removeNode(unresponsive);
      }
      finally {
        this.stateLock.writeLock().unlock();
      }
    }   
  }


}
