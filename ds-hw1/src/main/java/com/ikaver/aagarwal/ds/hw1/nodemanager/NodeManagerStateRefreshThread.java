package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerStateRefreshThread extends Thread {

  private Lock poolLock;
  private ProcessManagerPool pool;
  private Lock stateLock;
  private ProcessesState state;

  private static final Logger logger 
  = LogManager.getLogger(ProcessLauncher.class.getName());

  public NodeManagerStateRefreshThread(ProcessManagerPool pool, Lock poolLock,
      ProcessesState state, Lock stateLock) {
    this.pool = pool;
    this.poolLock = poolLock;
    this.state = state;
    this.stateLock = stateLock;
  }

  public void run(){
    Set<String> unresponsiveNodes = new HashSet<String>();

    for(String nodeId : pool.availableNodes()) {
      IProcessManager manager = null;

      poolLock.lock();
      try {
        manager = ProcessManagerFactory.processManagerFromConnectionString(
            pool.connectionForId(nodeId)
        );
      }
      finally {
        poolLock.unlock();
      }

      boolean contactSuccess = false;
      if(manager != null) {
        try {
          NodeState nodeState = manager.getState();
          stateLock.lock();
          try {
            state.setProcessList(nodeId, nodeState.getRunningProcesses());
          }
          finally {
            stateLock.unlock();
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
      poolLock.lock();
      try {
        pool.remove(unresponsive);
      }
      finally {
        poolLock.unlock();
      }
    }
  }


}
