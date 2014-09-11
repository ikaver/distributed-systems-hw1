package com.ikaver.aagarwal.ds.hw1.shared;

import java.io.Serializable;

/**
 * Represents a migratable process, basically a process that can be suspended
 * at any point of its execution. The suspend method is called as a notice to 
 * the process, the process will be suspended right after suspend returns.
 */
public interface IMigratableProcess extends Serializable, Runnable {
  /**
   * @description The suspend method is called as a notice to 
   * the process, the process will be suspended right after suspend returns.
   */
  public void suspend();
}
