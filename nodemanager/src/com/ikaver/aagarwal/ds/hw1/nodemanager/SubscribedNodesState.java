package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

public class SubscribedNodesState {

  private HashMap<String, String> nodeIdToConnectionStr;
  private HashMap<String, List<Integer>> nodeIdToPid;
  private HashMap<Integer, String> pidToNodeId;
  
  @Inject
  public SubscribedNodesState() {
    this.nodeIdToConnectionStr = new HashMap<String, String>();
    this.nodeIdToPid = new HashMap<String, List<Integer>>();
    this.pidToNodeId = new HashMap<Integer, String>();
  }
  
  public int nodeCount() {
    return this.nodeIdToConnectionStr.size();
  }
  
  public Set<String> availableNodes() {
    return new HashSet<String>(this.nodeIdToConnectionStr.keySet());
  }
  
  public String connectionStringForNode(String nodeId) {
    return this.nodeIdToConnectionStr.get(nodeId);
  }
  
  public String getNodeForPID(Integer pid) {
    return this.pidToNodeId.get(pid);
  }
  
  public void addNode(String nodeId, String connectionStr) {
    this.nodeIdToConnectionStr.put(nodeId, connectionStr);
  }
  
  public void removeNode(String nodeId) {
    this.nodeIdToConnectionStr.remove(nodeId);
    this.nodeIdToPid.remove(nodeId);
    Set<Integer> nodePID = new HashSet<Integer>();
    for(Integer pid : this.pidToNodeId.keySet()) {
      if(this.pidToNodeId.get(pid).equals(nodeId)) {
        nodePID.add(pid);
      }
    }
    for(Integer pid : nodePID) {
      this.pidToNodeId.remove(pid);
    }
  }
    
  public boolean addProcessToNode(Integer pid, String nodeId) {
    this.pidToNodeId.put(pid, nodeId);
    if(this.nodeIdToPid.containsKey(nodeId)) {
      this.nodeIdToPid.get(nodeId).add(pid);
    }
    else {
      List<Integer> runningProcesses = new LinkedList<Integer>();
      runningProcesses.add(pid);
      this.nodeIdToPid.put(nodeId, runningProcesses);
    }
    return true;
  }
  
  public boolean removeProcessFromCurrentNode(Integer pid) {
    String node = this.pidToNodeId.get(pid);
    return node == null ? false : this.removeProcessFromNode(pid, node);
  }
  
  public List<Integer> getProcessList(String node) {
    return new LinkedList<Integer>(this.nodeIdToPid.get(node));
  }
  
  public void setProcessList(String node, List<Integer> processList) {
    if(this.nodeIdToPid.containsKey(node)) {
      for(Integer pid : this.nodeIdToPid.get(node)) {
        this.pidToNodeId.remove(pid);
      }
    }
    this.nodeIdToPid.put(node, processList);
    for(Integer pid : processList) {
      this.pidToNodeId.put(pid, node);
    }
  }
  
  public void clearProcessList(String node) {
    this.setProcessList(node, new LinkedList<Integer>());
  }
  
  public boolean removeProcessFromNode(Integer pid, String node) {
    boolean foundNode = false;
    if(node != null && node.equals(this.pidToNodeId.get(pid))) {
      this.pidToNodeId.remove(pid);
      this.nodeIdToPid.get(node).remove(pid);
      foundNode = true;
    }
    return foundNode;
  }
  
}
