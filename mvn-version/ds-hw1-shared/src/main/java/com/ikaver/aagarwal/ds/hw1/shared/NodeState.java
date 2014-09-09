package com.ikaver.aagarwal.ds.hw1.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

// TODO(ikaver, ankit): Extract it to an interface.
public class NodeState implements Serializable {

  private static final long serialVersionUID = -3222854152850012659L;
  private String nodeId;
  private List<Integer> runningProcesses;

  public NodeState(String nodeId, List<Integer> runningProcesses) {
    this.nodeId = nodeId;
    if(runningProcesses == null) {
      this.runningProcesses = new LinkedList<Integer>();
    }
    else {
      this.runningProcesses = runningProcesses;
    }
  }

  public String getNodeId() {
    return nodeId;
  }

  public List<Integer> getRunningProcesses() {
    return runningProcesses;
  }

}
