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
import com.ikaver.aagarwal.ds.hw1.nodemanager.ProcessManagerFactory;
import com.ikaver.aagarwal.ds.hw1.shared.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerStateRefreshThread implements Runnable {

  private ReadWriteLock stateLock;
  private Set<String> deadNodes;
  private SubscribedNodesState state;

  private static final Logger logger 
  = LogManager.getLogger(NodeManagerStateRefreshThread.class.getName());

  @Inject
  public NodeManagerStateRefreshThread(
      @Named("NMState") SubscribedNodesState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    this.state = state;
    this.stateLock = stateLock;
    this.deadNodes = new HashSet<String>();
  }

  public void run(){
    this.queryAvailableNodes();
    this.queryDeadNodes();
  }
  
  private void queryAvailableNodes() {
    HashMap<String,String> unresponsiveNodes = new HashMap<String, String>();
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
        unresponsiveNodes.put(nodeId, connectionForId);
      }
    }
    this.removeUnresponsiveNodes(unresponsiveNodes);
  }
  
  private void queryDeadNodes() {
    Set<String> backToLifeNodes = new HashSet<String>();
    for(String deadNode : this.deadNodes) {
      IProcessManager manager = ProcessManagerFactory.processManagerFromConnectionString(deadNode);
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
          String newId = this.state.addNode(deadNode);
          backToLifeNodes.add(deadNode);
          System.out.printf("Node %s is back alive with id %s\n", deadNode, newId);
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
    catch(Exception e) {
      logger.error("Bad node state update", e);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  private void removeUnresponsiveNodes(HashMap<String,String> unresponsiveNodes) {
    for(String unresponsive : unresponsiveNodes.keySet()) {
      String connectionStr = unresponsiveNodes.get(unresponsive);      
      System.out.printf("Node %s with id %s is unresponsive, disconnecting...\n",
          connectionStr, unresponsive);  
      this.deadNodes.add(connectionStr);

      this.stateLock.writeLock().lock();
      try {
        this.state.removeNode(unresponsive);
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
