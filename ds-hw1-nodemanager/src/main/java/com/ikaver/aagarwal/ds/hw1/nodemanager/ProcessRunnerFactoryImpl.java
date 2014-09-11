package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.Definitions;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

public class ProcessRunnerFactoryImpl implements IProcessRunnerFactory {
  
  private static final Logger logger 
    = LogManager.getLogger(ProcessRunnerFactoryImpl.class.getName());
  
  public IProcessRunner processRunnerFromConnectionStr(String connectionStr) {
    if(connectionStr == null) return null;
    String url = String.format(
        "//%s/%s", 
        connectionStr, 
        Definitions.PROCESS_MANAGER_SERVICE
    );
    try {
      return (IProcessRunner) Naming.lookup (url);
    } catch (MalformedURLException e) {
      logger.info("Bad URL", e);
    } catch (RemoteException e) {
      logger.info("Remote connection refused to url "+ connectionStr);
    } catch (NotBoundException e) {
      logger.info("Not bound", e);
    }
    return null;
  }

}
