package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerStateRefreshThread extends Thread {

  private ReadWriteLock poolLock;
  private ProcessManagerPool pool;
  private ReadWriteLock stateLock;
  private ProcessesState state;

  private static final Logger logger 
  = LogManager.getLogger(ProcessLauncher.class.getName());

  public NodeManagerStateRefreshThread(@Named("NMPool") ProcessManagerPool pool, 
      @Named("NMPoolLock") ReadWriteLock poolLock,
      @Named("NMState") ProcessesState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    this.pool = pool;
    this.poolLock = poolLock;
    this.state = state;
    this.stateLock = stateLock;
  }

  public void run(){
    Set<String> unresponsiveNodes = new HashSet<String>();

    for(String nodeId : pool.availableNodes()) {
      IProcessManager manager = null;

      poolLock.readLock().lock();
      try {
        manager = ProcessManagerFactory.processManagerFromConnectionString(
            pool.connectionForId(nodeId)
        );
      }
      finally {
        poolLock.readLock().unlock();
      }

      boolean contactSuccess = false;
      if(manager != null) {
        try {
          NodeState nodeState = manager.getState();
          stateLock.writeLock().lock();
          try {
            state.setProcessList(nodeId, nodeState.getRunningProcesses());
          }
          finally {
            stateLock.writeLock().unlock();
          }
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

    for(String unresponsive : unresponsiveNodes) {
      poolLock.writeLock().lock();
      try {
        pool.remove(unresponsive);
      }
      finally {
        poolLock.writeLock().unlock();
      }
    }
  }


}
