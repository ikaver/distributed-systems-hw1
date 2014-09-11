package com.ikaver.aagarwal.ds.hw1.nodemanager;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

/**
 * Interface implemented by entities that know how to construct a ProcessRunner
 * given a connection string.
 */
public interface IProcessRunnerFactory {
  
  public IProcessRunner processRunnerFromConnectionStr(String connectionStr);

}
