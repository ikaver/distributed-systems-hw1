package com.ikaver.aagarwal.ds.hw1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class NodeManagerMock implements INodeManager {

  private HashMap<Integer, String> processIdToNode;
  private HashMap<String, List<Integer>> nodeToProcessId;
  private int currentId;
  private int currentNode;
  
  private static final int AMOUNT_OF_NODES = 3;
  
  public NodeManagerMock() {
    this.processIdToNode = new HashMap<Integer, String>();
    this.nodeToProcessId = new HashMap<String, List<Integer>>();
    this.currentNode = 0;
  }
  
  public int launch(String className, String[] args) {
    ++this.currentId;
    this.addProcessToNode(this.currentId, ""+this.currentNode);
    this.moveToNextNode();
    return this.currentId;
  }

  public boolean migrate(int pid, String sourceNode, String destinationNode) {
    if(this.removeProcessFromNode(pid, sourceNode)) {
      return this.addProcessToNode(pid, destinationNode);
    }
    return false;
  }

  public boolean remove(int pid) {
    return this.removeProcessFromCurrentNode(pid);
  }
  
  public List<NodeState> getNodeInformation() {
    List<NodeState> nodes = new LinkedList<NodeState>();
    for(String node : this.nodeToProcessId.keySet()) {
      nodes.add(new NodeState(node, this.nodeToProcessId.get(node)));
    }
    return nodes;
  }
  
  private boolean addProcessToNode(Integer pid, String nodeId) {
    this.processIdToNode.put(pid, nodeId);
    if(this.nodeToProcessId.containsKey(nodeId)) {
      this.nodeToProcessId.get(nodeId).add(pid);
    }
    else {
      List<Integer> runningProcesses = new LinkedList<Integer>();
      runningProcesses.add(pid);
      this.nodeToProcessId.put(nodeId, runningProcesses);
    }
    return true;
  }
  
  private boolean removeProcessFromCurrentNode(Integer pid) {
    String node = this.processIdToNode.get(pid);
    return node == null ? false : this.removeProcessFromNode(pid, node);
  }
  
  private boolean removeProcessFromNode(Integer pid, String node) {
    boolean foundNode = false;
    if(this.processIdToNode.get(pid).equals(node)) {
      this.processIdToNode.remove(pid);
      this.nodeToProcessId.get(node).remove(pid);
      foundNode = true;
    }
    return foundNode;
  }
  
  private void moveToNextNode() {
    if(this.currentNode == AMOUNT_OF_NODES-1) {
      this.currentNode = 0;
    }
    else {
      ++this.currentNode;
    }
  }

}
