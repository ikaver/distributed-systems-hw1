package com.ikaver.aagarwal.ds.hw1;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Scanner;

import com.google.inject.Inject;
import com.ikaver.aagarwal.ds.hw1.helpers.ArrayAdditions;

/*
 * Parses commands from the client line by line and makes the appropriate API
 * call for each command given.
 */
public class NodeManagerController {
  
  private static final String MIGRATE_COMMAND = "m";
  private static final String KILL_COMMAND = "k";
  private static final String LAUNCH_COMMAND = "l";
  
  private Scanner scanner;
  private INodeManager manager;
  
  @Inject
  public NodeManagerController(InputStream input, INodeManager manager) {
    this.scanner = new Scanner(input);
    this.manager = manager;
  }
  
  /*
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
  private void migrateCommand(String [] args) {
    if(args.length < 4 || ArrayAdditions.contains(args, null)) {
      this.printHelp();
      return;
    }
    try {
      int pid = Integer.parseInt(args[1]);
      String srcNode = args[2];
      String destNode = args[3];
      if(this.manager.migrate(pid, srcNode, destNode)) {
        System.out.printf("Process %d migrated from %s to %s\n", pid, srcNode, destNode);
      }
    }
    catch(NumberFormatException e) {
      e.printStackTrace();
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
      if(this.manager.remove(pid)) {
        System.out.printf("Process %d killed\n", pid);
      }
      else {
        System.out.printf("Couldn't kill process %d\n", pid);
      }
    }
    catch(NumberFormatException e) {
      e.printStackTrace();
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
    String className = args[1];
    String [] processArgs = Arrays.copyOfRange(args, 2, args.length);
    int pid = this.manager.launch(className, processArgs);
    if(pid > 0) {
      System.out.printf("Launched process with pid = %d\n", pid);
    }
    else {
      System.out.printf("Couldn't launch process %s\n", className);
    }
  }
  
  private void printHelp() {
    System.out.println("Invalid command. Use: ");
    System.out.println("\tk PROCESS_ID");
    System.out.println("\tl CLASS_NAME ARGS");
    System.out.println("\tm PROCESS_ID SOURCE_NODE DESTINATION_NODE");
  }
}
