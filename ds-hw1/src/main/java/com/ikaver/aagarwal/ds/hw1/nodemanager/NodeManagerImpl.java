package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerImpl implements INodeManager {

  private ProcessManagerPool pool;
  private int currentPID;
  
  private static final int AMOUNT_OF_RETRIES = 5;

  private static final Logger logger 
    = LogManager.getLogger(NodeManagerImpl.class.getName());

  public NodeManagerImpl(ProcessManagerPool pool) {
    if(pool == null) {
      throw new NullPointerException("ProcessManagerPool can't be null");
    }
    this.currentPID = 0;
    this.pool = pool;
  }

  public int launch(String className, String[] args) throws RemoteException {
    int pid = -1;
    String nodeId = this.chooseNodeFromPool();
    if(nodeId == null) return pid;
    
    int possiblePID = this.getNextPID();
    IProcessManager processManager 
      = ProcessManagerFactory.processManagerFromConnectionString(
          this.pool.connectionForId(nodeId));
    ProcessLauncher launcher = new ProcessLauncher(AMOUNT_OF_RETRIES);    
    if(launcher.launch(processManager, possiblePID, className, args)) {
      pid = possiblePID;
      this.addProcess(pid, nodeId);
    }
    return pid;
  }
  
  public boolean migrate(int pid, String sourceNode, String destinationNode)
      throws RemoteException {
    String srcConnection = this.pool.connectionForId(sourceNode);
    String destConnection = this.pool.connectionForId(destinationNode);
    if(srcConnection == null || destConnection == null) return false;
    String packedProcess = null;
    IProcessManager srcManager
    = ProcessManagerFactory.processManagerFromConnectionString(srcConnection);
    try {
      if(srcManager != null) packedProcess = srcManager.pack(pid);
    }
    catch(RemoteException e) {
      logger.error("Bad src migration", e);
    }
    
    boolean success = false;
    IProcessManager destManager
    = ProcessManagerFactory.processManagerFromConnectionString(destConnection);
    try {
      if(destManager != null) success = destManager.unpack(pid, packedProcess);
    }
    catch(RemoteException e) {
      logger.error("Bad unpack", e);
    }
    if(success) {
      this.moveProcess(pid, sourceNode, destinationNode);
    }
    return success;
  }

  public boolean remove(int pid) throws RemoteException {
    String nodeId = this.nodeForPID(pid);
    if(nodeId == null) return false;
    IProcessManager processManager 
      = ProcessManagerFactory.processManagerFromConnectionString(
          this.pool.connectionForId(nodeId));
    boolean success = false;
    try {
      if(processManager != null) success = processManager.remove(pid);
    }
    catch(RemoteException e) {
      logger.error("Bad unpack", e);
    }
    if(success) {
      this.removeProcess(pid, nodeId);
    }
    return success;
  }

  public List<NodeState> getNodeInformation() throws RemoteException {
    // TODO Auto-generated method stub
    return null;
  }
  
  private void addProcess(int pid, String node) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  private void removeProcess(int pid, String node) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  private void moveProcess(int pid, String srcNode, String destNode) {
    throw new UnsupportedOperationException("Not implemented yet");
  }
  
  private String nodeForPID(int pid) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private int getNextPID() {
    //TODO: write LOCK
    return ++this.currentPID;
  }

  private String chooseNodeFromPool() {
    if(this.pool.size() > 0) {
      return this.pool.availableNodes().iterator().next();
    }
    else {
      return null;
    }
  }
}
