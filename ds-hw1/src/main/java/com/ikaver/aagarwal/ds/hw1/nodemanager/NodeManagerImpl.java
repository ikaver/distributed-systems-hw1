package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class NodeManagerImpl implements INodeManager {

  private Lock poolLock;
  private ProcessManagerPool pool;
  private Lock stateLock;
  private ProcessesState state;
  private int currentPID;
  
  private static final int AMOUNT_OF_RETRIES = 5;

  private static final Logger logger 
    = LogManager.getLogger(NodeManagerImpl.class.getName());

  public NodeManagerImpl(ProcessManagerPool pool, Lock poolLock,
      ProcessesState state, Lock stateLock) {
    if(pool == null) {
      throw new NullPointerException("ProcessManagerPool can't be null");
    }
    if(poolLock == null) {
      throw new NullPointerException("Pool Lock can't be null");
    }
    if(state == null) {
      throw new NullPointerException("Processes State can't be null");
    }
    if(stateLock == null) {
      throw new NullPointerException("State Lock can't be null");
    }
    this.currentPID = 0;
    this.pool = pool;
    this.poolLock = poolLock;
    this.state = state;
    this.stateLock = stateLock;
  }

  public int launch(String className, String[] args) throws RemoteException {
    int pid = -1;
    String nodeId = this.chooseNodeFromPool();
    if(nodeId == null) return pid;
    
    int possiblePID = this.getNextPID();
    String connectionStr = this.connectionStringForNode(nodeId);
    IProcessManager processManager 
      = ProcessManagerFactory.processManagerFromConnectionString(connectionStr);
    ProcessLauncher launcher = new ProcessLauncher(AMOUNT_OF_RETRIES);    
    if(launcher.launch(processManager, possiblePID, className, args)) {
      pid = possiblePID;
      this.addProcess(pid, nodeId);
    }
    return pid;
  }
  
  public boolean migrate(int pid, String sourceNode, String destinationNode)
      throws RemoteException {
    String srcConnection = this.connectionStringForNode(sourceNode);
    String destConnection = this.connectionStringForNode(destinationNode);
    if(srcConnection == null || destConnection == null) return false;
    boolean success = false;
    String packedProcess = null;
    IProcessManager srcManager
    = ProcessManagerFactory.processManagerFromConnectionString(srcConnection);
    IProcessManager destManager
    = ProcessManagerFactory.processManagerFromConnectionString(destConnection);
    try {
      if(srcManager != null) packedProcess = srcManager.pack(pid);
    }
    catch(RemoteException e) {
      logger.error("Bad src migration", e);
    }
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
    
    String connectionStr = this.connectionStringForNode(nodeId);
    IProcessManager processManager 
      = ProcessManagerFactory.processManagerFromConnectionString(connectionStr);
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
    this.poolLock.lock();
    this.stateLock.lock();
    List<NodeState> nodes = new LinkedList<NodeState>();
    try{
      for(String node : this.pool.availableNodes()) {
        nodes.add(new NodeState(node, this.state.getProcessList(node)));
      }
    }
    finally {
      this.stateLock.unlock();
      this.poolLock.unlock();
    }
    return nodes;
  }
  
  private String connectionStringForNode(String node) {
    String connectionStr = null;
    this.poolLock.lock();
    try {
      connectionStr = this.pool.connectionForId(node); 
    }
    finally {
      this.poolLock.unlock();
    }
    return connectionStr;
  }
  
  private void addProcess(int pid, String node) {
    this.stateLock.lock();
    try {
      this.state.addProcessToNode(pid, node);
    }
    finally {
      this.stateLock.unlock();
    }
  }
  
  private void removeProcess(int pid, String node) {
    this.stateLock.lock();
    try {
      this.state.removeProcessFromNode(pid, node);
    }
    finally {
      this.stateLock.unlock();
    }
  }
  
  private void moveProcess(int pid, String srcNode, String destNode) {
    this.stateLock.lock();
    try {
      this.state.removeProcessFromNode(pid, srcNode);
      this.state.addProcessToNode(pid, srcNode);
    }
    finally {
      this.stateLock.unlock();
    }
  }
  
  private String nodeForPID(int pid) {
    String nodeId = null;
    this.stateLock.lock();
    try{
      nodeId = this.state.getNodeForPID(pid);
    }
    finally {
      this.stateLock.unlock();
    }
    return nodeId;
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
