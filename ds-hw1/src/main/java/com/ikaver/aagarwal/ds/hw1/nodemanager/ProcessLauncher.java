package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class ProcessLauncher {

  private int amountOfRetries;
 
  private static final Logger logger 
  = LogManager.getLogger(NodeManagerImpl.class.getName());
  
  public ProcessLauncher(int amountOfRetries) {
    this.amountOfRetries = amountOfRetries;
  }

  public boolean launch(IProcessManager manager, int pid, String className, String [] args) {
    if(manager == null) return false;
    
    boolean launched = false;
    for(int i = 0; i < this.amountOfRetries && !launched; ++i) {
      try {
        launched = manager.launch(pid, className, args);
        break;
      }
      catch(RemoteException e) {
        logger.error("Bad launch.", e);
      }
    }
    if(!launched){
      logger.error(String.format("Unable to launch process %s in node %s", 
          className, pid));
    }
    return launched;
  }

}
