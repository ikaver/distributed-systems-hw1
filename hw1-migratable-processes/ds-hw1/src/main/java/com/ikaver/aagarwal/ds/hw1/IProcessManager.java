package com.ikaver.aagarwal.ds.hw1;

public interface IProcessManager {
  /*
   * @description Returns the current state of the node, indicating which 
   * processes are currently running.
   * @return The current node state.
   * @see NodeState
   */
  public NodeState getState();
  /*
   * Suspends the migratable process with the given pid, and afterwards returns 
   * a serialized version of the suspended process.
   * @param pid The process pid
   * @return A serialized version of the suspended process with the given pid.
   * @see MigratableProcess
   */
  public String pack(int pid);
  
  /*
   * Deserializes and runs the migratable process with the given pid on the 
   * current node.
   * @param pid The process pid
   * @param serializedProcess A serialized version of the process that represents
   * its current state.
   */
  public boolean unpack(int pid, String serializedProcess);
  
  /*
   * Terminates the migratable process with the given pid, if its currently 
   * running on this node.
   * @param pid The process pid
   * @return true iff a process with the given pid was successfully terminated.
   */
  public boolean remove(int pid);
  
  /*
   * Launches a migratable process with the given pid, serialized
   * class definition and constructor arguments on this node. 
   * @param pid The id that will be assigned to this process
   * @param classDefinition The serialized class definition of the migratable
   * process
   * @param args The arguments with which the constructor will be invoked
   * @return true iff the process was launched successfully
   */
  public boolean launch(int pid, String classDefinition, String [] args);
}
