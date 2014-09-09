package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.NodeState;
import com.ikaver.aagarwal.ds.hw1.shared.helpers.ArrayAdditions;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

/**
 * Parses commands from the client line by line and makes the appropriate API
 * call for each command given.
 */
public class NodeManagerController {
  
  private static final String MIGRATE_COMMAND = "mig";
  private static final String KILL_COMMAND = "kill";
  private static final String LAUNCH_COMMAND = "launch";
  private static final String NODE_INFO_COMMAND = "info";
  
  private static final Logger logger 
    = LogManager.getLogger(NodeManagerController.class.getName());
  
  private Scanner scanner;
  private INodeManager manager;
  
  @Inject
  public NodeManagerController(@Named("ControllerInput") InputStream input, 
      INodeManager manager) {
    this.scanner = new Scanner(input);
    this.manager = manager;
  }
  
  /**
   * Reads the input from the input stream line by line, making the 
   * appropriate API calls for each command given.
   */
  public void readInput() {
    while(scanner.hasNextLine()) {
      processLine(scanner.nextLine());
    }
    scanner.close();
  }
  
  /*
   * Process a single command given by the user and calls the appropriate API if
   * the command is valid.
   */
  private void processLine(String line) {
    if(line == null) return;
    String [] tokens = line.trim().split("\\s+");
    if(tokens == null || tokens.length == 0) return;
    String commandId = tokens[0];
    if(commandId.equals(MIGRATE_COMMAND)) {
      this.migrateCommand(tokens);
    }
    else if(commandId.equals(KILL_COMMAND)) {
      this.killCommand(tokens);
    }
    else if(commandId.equals(LAUNCH_COMMAND)) {
      this.launchCommand(tokens);
    }
    else if(commandId.equals(NODE_INFO_COMMAND)) {
      this.printNodeInfoCommand(tokens);
    }
    else {
      this.printHelp();
    }
  }
  
  /*
   * Executes a migrate command with the given arguments. If the arguments are 
   * invalid no action occurs.
   * @param args Arguments for the migrate command. 
   *    [ "m", PROCESS_ID, SOURCE_NODE, DESTINATION_NODE]
   */
  private void migrateCommand(String [] args){
    if(args.length < 4 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    try {
      int pid = Integer.parseInt(args[1]);
      String srcNode = args[2];
      String destNode = args[3];
      boolean success = false;
      try {
        success = this.manager.migrate(pid, srcNode, destNode);
      }
      catch(RemoteException e) {
        logger.error("Migrate failed!", e);
        success = false;
      }
      if(success) {
        System.out.printf("Process %d migrated from %s to %s\n", 
            pid, srcNode, destNode);
      }
      else {
        System.out.printf("Couldn't migrate %d from %s to %s\n",
            pid, srcNode, destNode);
      }
    }
    catch(NumberFormatException e) {
      logger.error("Number to String failed!", e);
      System.out.printf("Bad pid: %s\n", args[1]);
    }
  }
  
  /*
   * Executes a kill command with the given arguments. If the arguments are 
   * invalid no action occurs.
   * @param args Arguments for the kill command. 
   *    ["k", PROCESS_ID]
   */
  private void killCommand(String [] args) {
    if(args.length < 2 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    try {
      int pid = Integer.parseInt(args[1]);
      boolean success = false;
      try{
        success = this.manager.remove(pid);
      }
      catch(RemoteException e) {
        logger.error("Kill process failed", e);
        success = false;
      }
      if(success) {
        System.out.printf("Process %d killed\n", pid);
      }
      else {
        System.out.printf("Couldn't kill process %d\n", pid);
      }
    }
    catch(NumberFormatException e) {
      logger.error("Number to String failed!", e);
      System.out.printf("Bad pid: %s\n", args[1]);
    }

  }
  
  /*
   * Executes a launch command with the given arguments. If the arguments are 
   * invalid no action occurs.
   * @param args Arguments for the kill command. 
   *    ["l", PROCESS_ID]
   */
  private void launchCommand(String [] args) {
    if(args.length < 2 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    int pid = -1;
    String className = args[1];
    String [] processArgs = Arrays.copyOfRange(args, 2, args.length);
    try {
      pid = this.manager.launch(className, processArgs);
    }
    catch(RemoteException e) {
      logger.error("Launch failed", e);
    }
    if(pid > 0) {
      System.out.printf("Launched process with pid = %d\n", pid);
    }
    else {
      System.out.printf("Couldn't launch process %s\n", className);
    }
  }
  
  /*
   * Prints the node state for all of the current nodes
   */
  private void printNodeInfoCommand(String [] args) {
    try{
      for(NodeState node : this.manager.getNodeInformation()) {
        System.out.printf("Node %s : %s\n", node.getNodeId(),
            node.getRunningProcesses());
      }
    }
    catch(RemoteException e){
      logger.error("Node information failed!", e);
    }
  }
  
  private void printHelp() {
    System.out.println("Invalid command. Use: ");
    System.out.printf("\t%s PROCESS_ID\n", KILL_COMMAND);
    System.out.printf("\t%s CLASS_NAME ARGS\n", LAUNCH_COMMAND);
    System.out.printf("\t%s PROCESS_ID SOURCE_NODE DESTINATION_NODE\n", MIGRATE_COMMAND);
  }
}
