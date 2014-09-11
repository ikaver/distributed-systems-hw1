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
   * The connection string for the process runner
   */
  private String connectionStr;
  /**
   * A list of pids of the processes currently running.
   */
  private List<Integer> runningProcesses;

  public ProcessRunnerState(String nodeId, String connectionStr, List<Integer> runningProcesses) {
    this.processRunnerId = nodeId;
    this.connectionStr = connectionStr;
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
  
  public String getConnectionStr() {
    return connectionStr;
  }

  public List<Integer> getRunningProcesses() {
    return runningProcesses;
  }

}
