package com.ikaver.aagarwal.ds.hw1.processrunner;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

public class ProcessRunnerEntryPoint {

  private static final Logger logger = Logger.getLogger(ProcessRunnerEntryPoint.class);

  public static void main(String args[]) {

    IProcessRunner manager = null;
    Integer port = -1;
    try {
      port = Integer.parseInt(args[0]);
    } catch(NumberFormatException e) {
      logger.error("Bad port number", e);
    }
    catch(ArrayIndexOutOfBoundsException e) {
      logger.error("Missing port parameter", e);
    }
    
    if(port == -1) {
      System.out.println("Usage: java -jar processRunner.jar <PORT_NUMBER>");
      System.exit(-1);
    }

    try { //special exception handler for registry creation
      LocateRegistry.createRegistry(port); 
      logger.info("RMI registry created.");
    } catch (RemoteException e) {
      //do nothing, error means registry already exists
      logger.warn("RMI was already running.");
    }

    logger.info(String.format("Running the process manager @%d", port));

    try {
      manager = new ProcessRunnerImpl();
    } catch (RemoteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    try {
      Naming.rebind(String.format("//localhost:%d/ProcessManager", port), manager);
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
}
