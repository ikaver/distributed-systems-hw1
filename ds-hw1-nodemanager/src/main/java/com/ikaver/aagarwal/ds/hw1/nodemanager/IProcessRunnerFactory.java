package com.ikaver.aagarwal.ds.hw1.nodemanager;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

public interface IProcessRunnerFactory {
  
  public IProcessRunner processRunnerFromConnectionStr(String connectionStr);

}
