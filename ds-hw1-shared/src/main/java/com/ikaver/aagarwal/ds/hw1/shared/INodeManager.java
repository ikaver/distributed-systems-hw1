package com.ikaver.aagarwal.ds.hw1.shared;

import java.util.List;
import java.rmi.*;


public interface INodeManager extends Remote {
  
  public String addNode(String connectionString) throws RemoteException;
  public int launch(String className, String [] args) throws RemoteException;
  public boolean migrate(int pid, String sourceNode, String destinationNode)
      throws RemoteException;
  public boolean remove(int pid) throws RemoteException;;
  public List<NodeState> getNodeInformation() throws RemoteException;
  
}
