package com.ikaver.aagarwal.ds.hw1.nodemanager;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public interface IProcessManagerFactory {
  
  public IProcessManager processManagerFromConnectionString(String connectionStr);

}
