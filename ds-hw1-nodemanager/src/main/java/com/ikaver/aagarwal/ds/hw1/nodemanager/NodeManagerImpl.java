package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;
import com.ikaver.aagarwal.ds.hw1.shared.helpers.MathHelper;

public class NodeManagerImpl implements INodeManager {

  private ReadWriteLock stateLock;
  private SubscribedNodesState state;
  private int currentPID;
  
  private static final int AMOUNT_OF_RETRIES = 5;

  private static final Logger logger 
    = LogManager.getLogger(NodeManagerImpl.class.getName());
  
  @Inject
  public NodeManagerImpl(
      @Named("NMState") SubscribedNodesState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    if(state == null) {
      throw new NullPointerException("Processes State can't be null");
    }
    if(stateLock == null) {
      throw new NullPointerException("State Lock can't be null");
    }
    this.currentPID = 0;
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
    IMigratableProcess packedProcess = null;
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
      if(destManager != null && packedProcess != null) {
        success = destManager.unpack(pid, packedProcess);
      }
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
      logger.error("Bad remove", e);
    }
    if(success) {
      this.removeProcess(pid);
    }
    return success;
  }

  public List<NodeState> getNodeInformation() throws RemoteException {
    this.stateLock.readLock().lock();
    List<NodeState> nodes = new LinkedList<NodeState>();
    try{
      for(String node : this.state.availableNodes()) {
        nodes.add(new NodeState(node, this.state.getProcessList(node)));
      }
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return nodes;
  }
  
  private String connectionStringForNode(String node) {
    String connectionStr = null;
    this.stateLock.readLock().lock();
    try {
      connectionStr = this.state.connectionStringForNode(node); 
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return connectionStr;
  }
  
  private void addProcess(int pid, String node) {
    this.stateLock.writeLock().lock();
    try {
      this.state.addProcessToNode(pid, node);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  private void removeProcess(int pid) {
    this.stateLock.writeLock().lock();
    try {
      this.state.removeProcessFromCurrentNode(pid);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  private void moveProcess(int pid, String srcNode, String destNode) {
    this.stateLock.writeLock().lock();
    try {
      this.state.removeProcessFromCurrentNode(pid);
      this.state.addProcessToNode(pid, srcNode);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }
  
  private String nodeForPID(int pid) {
    String nodeId = null;
    this.stateLock.readLock().lock();
    try{
      nodeId = this.state.getNodeForPID(pid);
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return nodeId;
  }

  private int getNextPID() {
    //TODO: write LOCK
    return ++this.currentPID;
  }

  /*
   * Chooses a node from the connection pool. Currently, the nodes are chosen
   * randomly.
   */
  private String chooseNodeFromPool() {
    String selectedNode = null;
    this.stateLock.readLock().lock();
    try {
      String [] nodes = new String[this.state.nodeCount()];
      if(nodes.length > 0) {
        this.state.availableNodes().toArray(nodes);
        int randomIndex = MathHelper.randomIntInRange(0, this.state.nodeCount());
        selectedNode = nodes[randomIndex];
      }
      else {
        logger.warn("Couldn't find an available node");
      }
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return selectedNode;
  }
}
