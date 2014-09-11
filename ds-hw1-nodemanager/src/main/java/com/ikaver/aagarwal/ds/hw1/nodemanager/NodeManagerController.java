package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessRunnerState;
import com.ikaver.aagarwal.ds.hw1.shared.helpers.ArrayAdditions;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;

/**
 * Parses commands from the client line by line and makes the appropriate API
 * call for each command given.
 */
public class NodeManagerController {
  
  private static final String MIGRATE_COMMAND = "mig";
  private static final String TERMINATE_COMMAND = "terminate";
  private static final String LAUNCH_COMMAND = "launch";
  private static final String PROCESS_RUNNER_INFO_COMMAND = "ps";
  private static final String ADD_PROCESS_RUNNER_COMMAND = "add";
  
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
    else if(commandId.equals(TERMINATE_COMMAND)) {
      this.terminateCommand(tokens);
    }
    else if(commandId.equals(LAUNCH_COMMAND)) {
      this.launchCommand(tokens);
    }
    else if(commandId.equals(PROCESS_RUNNER_INFO_COMMAND)) {
      this.printProcessRunnerState(tokens);
    }
    else if(commandId.equals(ADD_PROCESS_RUNNER_COMMAND)) {
      this.addProcessRunnerCommand(tokens);
    }
    else {
      this.printHelp();
    }
  }
  
  /**
   * Adds a new process runner to the set of available process runner.
   * @param args args[1] should be the socket address of the new node.
   */
  private void addProcessRunnerCommand(String [] args) {
    if(args.length < 2 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    String connectionString = args[1];
    String processRunnerId = null;
    try {
      processRunnerId = this.manager.addProcessRunner(connectionString);
    }
    catch(RemoteException e) {
      logger.error("Bad add process runner", e);
    }
    if(processRunnerId == null) {
      System.out.printf("Failed to add process runner %s\n", connectionString);
    }
    else {
      System.out.printf("Process runner %s added with id = %s\n", connectionString, processRunnerId);
    }
  }
  
  /*
   * Executes a migrate command with the given arguments. If the arguments are 
   * invalid no action occurs.
   * @param args Arguments for the migrate command. 
   *    [ "mig", PROCESS_ID, SRC_PROCESS_RUNNER, DEST_PROCESS_RUNNER]
   */
  private void migrateCommand(String [] args){
    if(args.length < 4 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    try {
      int pid = Integer.parseInt(args[1]);
      String srcProcessRunner = args[2];
      String destProcessRunner = args[3];
      boolean success = false;
      try {
        success = this.manager.migrate(pid, srcProcessRunner, destProcessRunner);
      }
      catch(RemoteException e) {
        logger.error("Migrate failed!", e);
        success = false;
      }
      if(success) {
        System.out.printf("Process %d migrated from %s to %s\n", 
            pid, srcProcessRunner, destProcessRunner);
      }
      else {
        System.out.printf("Couldn't migrate %d from %s to %s\n",
            pid, srcProcessRunner, destProcessRunner);
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
  private void terminateCommand(String [] args) {
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
   * Prints the process runner state for all of the current process runners
   */
  private void printProcessRunnerState(String [] args) {
    try{
      List<ProcessRunnerState> state = this.manager.getProcessRunnerState();
      if(state.size() > 0) {
        for(ProcessRunnerState runner : state) {
          System.out.printf("Process runner %s (%s) : %s\n", runner.getProcessRunnerId(),
              runner.getConnectionStr(),
              runner.getRunningProcesses());
        }
      }
      else {
        System.out.println("No process runners available...");
      }
      
    }
    catch(RemoteException e){
      logger.error("Process runner get state failed!", e);
    }
  }
  
  /**
   * Prints help for the user
   */
  private void printHelp() {
    System.out.println("Invalid command. Use: ");
    System.out.printf("\t%s PROCESS_ID\n", TERMINATE_COMMAND);
    System.out.printf("\t%s CLASS_NAME ARGS\n", LAUNCH_COMMAND);
    System.out.printf("\t%s PROCESS_ID SOURCE_PROCESS_RUNNER DESTINATION_PROCESS_RUNNER\n", MIGRATE_COMMAND);
    System.out.printf("\t%s CONNECTION_STRING\n", ADD_PROCESS_RUNNER_COMMAND);
  }
}
