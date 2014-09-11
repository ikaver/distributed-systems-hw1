package com.ikaver.aagarwal.ds.hw1.shared;

import java.util.List;
import java.rmi.*;


public interface INodeManager extends Remote {
  
  public String addProcessRunner(String connectionString) throws RemoteException;
  public int launch(String className, String [] args) throws RemoteException;
  public boolean migrate(int pid, String srcProcessRunner, String destProcessRunner)
      throws RemoteException;
  public boolean remove(int pid) throws RemoteException;;
  public List<ProcessRunnerState> getProcessRunnerState() throws RemoteException;
  
}
