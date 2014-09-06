package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.Definitions;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessManager;

public class ProcessManagerFactory {
  
  private static final Logger logger 
    = LogManager.getLogger(ProcessManagerFactory.class.getName());
  
  public static IProcessManager processManagerFromConnectionString(String connectionStr) {
    String url = String.format(
        "//%s/%s", 
        connectionStr, 
        Definitions.PROCESS_MANAGER_SERVICE
    );
    try {
      return (IProcessManager) Naming.lookup (url);
    } catch (MalformedURLException e) {
      logger.error("Bad URL", e);
    } catch (RemoteException e) {
      logger.error("Remote exception", e);
    } catch (NotBoundException e) {
      logger.error("Not bound", e);
    }
    return null;
  }

}
