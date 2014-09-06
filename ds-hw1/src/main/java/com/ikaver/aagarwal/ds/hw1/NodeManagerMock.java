package com.ikaver.aagarwal.ds.hw1;

public class NodeManagerMock implements INodeManager {

  private int currentId;
  
  public int launch(String className, String[] args) {
    return ++this.currentId;
  }

  public boolean migrate(int pid, String sourceNode, String destinationNode) {
    return true;
  }

  public boolean remove(int pid) {
    return true;
  }

}
