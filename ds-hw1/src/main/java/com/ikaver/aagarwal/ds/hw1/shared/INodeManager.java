package com.ikaver.aagarwal.ds.hw1.shared;

import java.util.List;

import com.ikaver.aagarwal.ds.hw1.NodeState;

public interface INodeManager {
  
  public int launch(String className, String [] args);
  public boolean migrate(int pid, String sourceNode, String destinationNode);
  public boolean remove(int pid);
  public List<NodeState> getNodeInformation();
  
}
