package com.ikaver.aagarwal.ds.hw1;

import java.io.Serializable;
import java.util.List;

// TODO(ikaver, ankit): Extract it to an interface.
public class NodeState implements Serializable {

  private static final long serialVersionUID = -3222854152850012659L;
  private String nodeId;
  private List<Integer> runningProcesses;

  public NodeState(String nodeId, List<Integer> runningProcesses) {
    if(runningProcesses == null) {
      throw new NullPointerException("List of running processes can't be null");
    }
    this.nodeId = nodeId;
    this.runningProcesses = runningProcesses;
  }

  public String getNodeId() {
    return nodeId;
  }

  public List<Integer> getRunningProcesses() {
    return runningProcesses;
  }

}
