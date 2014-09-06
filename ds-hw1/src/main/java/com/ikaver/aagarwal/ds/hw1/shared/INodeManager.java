package com.ikaver.aagarwal.ds.hw1.shared;

import java.util.List;
import java.rmi.*;

import com.ikaver.aagarwal.ds.hw1.NodeState;

public interface INodeManager extends Remote {
  
  public int launch(String className, String [] args) throws RemoteException;
  public boolean migrate(int pid, String sourceNode, String destinationNode)
      throws RemoteException;
  public boolean remove(int pid) throws RemoteException;;
  public List<NodeState> getNodeInformation() throws RemoteException;
  
}
