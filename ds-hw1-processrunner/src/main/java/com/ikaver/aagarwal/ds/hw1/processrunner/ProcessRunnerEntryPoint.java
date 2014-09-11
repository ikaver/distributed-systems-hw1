package com.ikaver.aagarwal.ds.hw1.processrunner;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;

import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;

/**
 * Entry point of the Process Runner program. Starts the RMI registry and the 
 * process manager service.
 */
public class ProcessRunnerEntryPoint {

  private static final Logger logger = Logger.getLogger(ProcessRunnerEntryPoint.class);

  public static void main(String args[]) {

    //Get the port number from the program arguments.
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

    //Start RMI registry
    try { 
      LocateRegistry.createRegistry(port); 
      logger.info("RMI registry created.");
    } catch (RemoteException e) {
      logger.warn("RMI was already running.");
    }

    logger.info(String.format("Running the process manager @%d", port));

    //Initiate the manager and start 
    IProcessRunner manager = null;
    try {
      manager = new ProcessRunnerImpl();
    } catch (RemoteException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    try {
      Naming.rebind(String.format("//:%d/ProcessManager", port), manager);
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }
}
