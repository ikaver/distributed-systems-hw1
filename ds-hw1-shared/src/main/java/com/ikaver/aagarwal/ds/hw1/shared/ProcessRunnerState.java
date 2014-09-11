package com.ikaver.aagarwal.ds.hw1.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the current state of a process runner. 
 */
public class ProcessRunnerState implements Serializable {

  private static final long serialVersionUID = -3222854152850012659L;
  /**
   * The id given to this process runner.
   */
  private String processRunnerId;
  /**
   * A list of pids of the processes currently running.
   */
  private List<Integer> runningProcesses;

  public ProcessRunnerState(String nodeId, List<Integer> runningProcesses) {
    this.processRunnerId = nodeId;
    if(runningProcesses == null) {
      this.runningProcesses = new LinkedList<Integer>();
    }
    else {
      this.runningProcesses = runningProcesses;
    }
  }

  public String getProcessRunnerId() {
    return processRunnerId;
  }

  public List<Integer> getRunningProcesses() {
    return runningProcesses;
  }

}
