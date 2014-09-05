package com.ikaver.aagarwal.ds.hw1;

public interface INodeManager {
  
  public int launch(String className, String [] args);
  public boolean migrate(int pid, int sourceNode, int destinationNode);
  public boolean remove(int pid);
  
}
