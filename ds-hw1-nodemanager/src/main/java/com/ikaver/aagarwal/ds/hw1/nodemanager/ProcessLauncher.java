package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

/**
 * A simple class that tries to launch a process in a process runner 
 * (amountOfRetries) times before reporting failure.
 */
public class ProcessLauncher {

  private int amountOfRetries;
 
  private static final Logger logger 
  = LogManager.getLogger(ProcessLauncher.class.getName());
  
  public ProcessLauncher(int amountOfRetries) {
    this.amountOfRetries = amountOfRetries;
  }

  public boolean launch(IProcessRunner manager, int pid, String className, String [] args) {
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
      logger.error(String.format("Unable to launch process %s", className));
    }
    return launched;
  }

}
