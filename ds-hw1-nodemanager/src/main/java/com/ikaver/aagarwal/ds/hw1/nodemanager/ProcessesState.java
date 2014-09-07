package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ProcessesState {

  private HashMap<String, List<Integer>> nodeIdToPid;
  private HashMap<Integer, String> pidToNodeId;
  
  public ProcessesState() {
    this.nodeIdToPid = new HashMap<String, List<Integer>>();
    this.pidToNodeId = new HashMap<Integer, String>();
  }
  
  public String getNodeForPID(Integer pid) {
    return this.pidToNodeId.get(pid);
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
    return this.nodeIdToPid.get(node);
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