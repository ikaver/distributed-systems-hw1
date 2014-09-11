package com.ikaver.aagarwal.ds.hw1.nodemanager;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.INodeManager;
import com.ikaver.aagarwal.ds.hw1.shared.IProcessRunner;
import com.ikaver.aagarwal.ds.hw1.shared.ProcessRunnerState;
import com.ikaver.aagarwal.ds.hw1.shared.helpers.MathHelper;

/**
 * A Node Manager is a “master” node that manages other nodes of the system. 
 * The Node Manager can ask its slave nodes (which we call Process Runners) to 
 * launch and terminate processes. Additionally, it can request a slave node to
 * suspend a process, serialize it and send it through the network and 
 * afterwards forward the serialized process to a different node to resume 
 * its execution.
 */
public class NodeManagerImpl implements INodeManager {
  
  /**
   * Returns a process runner reference given a connection string.
   */
  private IProcessRunnerFactory processRunnerFactory;
  /**
   * Lock that protects the SubscribedProcessRunnersState.
   */
  private ReadWriteLock stateLock;
  /**
   * The current state of the available process runners.
   */
  private SubscribedProcessRunnersState state;
  /**
   * Pid that will be assigned to the next process.
   */
  private int currentPID;

  private static final int AMOUNT_OF_RETRIES = 5;

  private static final Logger logger 
  = LogManager.getLogger(NodeManagerImpl.class.getName());

  @Inject
  public NodeManagerImpl(
      @Named("ProcessManagerFactory") IProcessRunnerFactory factory,
      @Named("NMState") SubscribedProcessRunnersState state, 
      @Named("NMStateLock") ReadWriteLock stateLock) {
    if(state == null) {
      throw new NullPointerException("Processes State can't be null");
    }
    if(stateLock == null) {
      throw new NullPointerException("State Lock can't be null");
    }
    if(factory == null) {
      throw new NullPointerException("Process manager factory can't be null");
    }
    this.currentPID = 0;
    this.state = state;
    this.stateLock = stateLock;
    this.processRunnerFactory = factory;
  }

  /**
   * Adds a process runner with the given connection string to the set of 
   * "available process runners". 
   * @param connectionString the socket address for the process runner
   * @return The process runner id if the node was added successfully, else null
   * @throws RemoteException
   */
  public String addProcessRunner(String connectionString) {
    String id = null;
   
    boolean communicationSuccess = true;
    IProcessRunner processManager 
    = this.processRunnerFactory.processRunnerFromConnectionStr(connectionString);
    try {
      if(processManager != null) processManager.start();
    }
    catch(RemoteException e) {
      logger.error("Couldn't communicate with new node", e);
      communicationSuccess = false;
    }
    
    //add process runner if we could communicate with it successfully.
    if(communicationSuccess) {
      this.stateLock.writeLock().lock();
      try{
        id = this.state.addProcessRunner(connectionString);
      }
      finally {
        this.stateLock.writeLock().unlock();
      }
    }
    return id;
  }

  /**
   * Launches the process with the given class name and arguments in one of 
   * the available process runners.
   * @param className The full class name of the process to run.
   * @param args The arguments for the process
   * @return The process id > 0 if the process was launched successfully, else
   * it returns -1.
   * @throws RemoteException
   */  
  public int launch(String className, String[] args) {
    int pid = -1;
    String processRunnerId = this.chooseProcessRunnerFromPool();
    if(processRunnerId == null) return pid;

    int possiblePID = this.getNextPID();
    String connectionStr = this.connectionStrForProcessRunner(processRunnerId);
    IProcessRunner processManager 
    = this.processRunnerFactory.processRunnerFromConnectionStr(connectionStr);
    ProcessLauncher launcher = new ProcessLauncher(AMOUNT_OF_RETRIES);    
    if(launcher.launch(processManager, possiblePID, className, args)) {
      pid = possiblePID;
      this.addProcess(pid, processRunnerId);
    }
    return pid;
  }

  /**
   * Migrates the process with the given pid from the process runner with id
   * srcProcessRunner to the process runner with id destProcessRunner
   * @param pid The pid of the process
   * @param srcProcessRunner The process runner currently running the process
   * @param destProcessRunner The process runner that will continue running
   * the process
   * @return true iff successful.
   * @throws RemoteException
   */
  public boolean migrate(int pid, String srcProcessRunner, String destProcessRunner) {
    String srcConnection = this.connectionStrForProcessRunner(srcProcessRunner);
    String destConnection = this.connectionStrForProcessRunner(destProcessRunner);
    if(srcConnection == null || destConnection == null) return false;
    boolean success = false;
    IMigratableProcess packedProcess = null;
    IProcessRunner srcRunner
    = this.processRunnerFactory.processRunnerFromConnectionStr(srcConnection);
    IProcessRunner destRunner
    = this.processRunnerFactory.processRunnerFromConnectionStr(destConnection);
    try {
      if(srcRunner != null) packedProcess = srcRunner.pack(pid);
    }
    catch(RemoteException e) {
      logger.error("Bad src migration", e);
    }
    try {
      if(destRunner != null && packedProcess != null) {
        success = destRunner.unpack(pid, packedProcess);
      }
    }
    catch(RemoteException e) {
      logger.error("Bad unpack", e);
    }
    if(success) {
      this.moveProcess(pid, srcProcessRunner, destProcessRunner);
    }
    return success;
  }

  /**
   * Terminates the process with the given pid
   * @param pid The pid of the process that will be terminated
   * @return true iff successful
   * @throws RemoteException
   */
  public boolean remove(int pid) {
    String processRunnerId = this.processRunnerForPid(pid);
    if(processRunnerId == null) return false;

    String connectionStr = this.connectionStrForProcessRunner(processRunnerId);
    IProcessRunner processManager 
    = this.processRunnerFactory.processRunnerFromConnectionStr(connectionStr);
    boolean success = false;
    try {
      if(processManager != null) success = processManager.remove(pid);
    }
    catch(RemoteException e) {
      logger.error("Bad remove", e);
    }
    if(success) {
      this.removeProcess(pid);
    }
    return success;
  }

  /**
   * Returns a list of the states of all the currently available process runners.
   * @return The list of the states of all the currently available process runners.
   * @throws RemoteException
   */
  public List<ProcessRunnerState> getProcessRunnerState() {
    this.stateLock.readLock().lock();
    List<ProcessRunnerState> processRunners = new LinkedList<ProcessRunnerState>();
    try{
      for(String processRunnerId : this.state.availableProcessRunners()) {
        processRunners.add(new ProcessRunnerState(processRunnerId, 
            this.state.getProcessList(processRunnerId)));
      }
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return processRunners;
  }

  /**
   * Gets the connection string for the process runner with the given id
   * @param processRunnerId Id of the process runner
   * @return The connection string for the process runner with the given id
   * or null if no such process runner exists.
   */
  private String connectionStrForProcessRunner(String processRunnerId) {
    String connectionStr = null;
    this.stateLock.readLock().lock();
    try {
      connectionStr = this.state.connectionStringForProcessRunner(processRunnerId); 
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return connectionStr;
  }

  /**
   * Adds the process with the given pid to the running processes list.
   * @param pid The pid of the process
   * @param processRunnerId The id of the process runner thats running the process
   */
  private void addProcess(int pid, String processRunnerId) {
    this.stateLock.writeLock().lock();
    try {
      this.state.addProcessToProcessRunner(pid, processRunnerId);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }

  /**
   * Removes the process with the given pid from the running processes list.
   * @param pid the pid of the process runner to remove.
   */
  private void removeProcess(int pid) {
    this.stateLock.writeLock().lock();
    try {
      this.state.removeProcessFromCurrentProcessRunner(pid);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }

  /**
   * Removes the process with pid from the srcProcessRunner process list
   * and moves it to the destinationProcessRunner process list. 
   * This only modifies internal state.
   * @param pid The pid of the process to move
   * @param srcProcessRunner The process runner that was running the process
   * @param destinationProcessRunner The new process runner for the process
   */
  private void moveProcess(int pid, String srcProcessRunner, String destinationProcessRunner) {
    this.stateLock.writeLock().lock();
    try {
      this.state.removeProcessFromCurrentProcessRunner(pid);
      this.state.addProcessToProcessRunner(pid, destinationProcessRunner);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }

  /**
   * Returns the id of the process runner where the process with pid (pid) is 
   * currently running.
   * @param pid The pid of the process
   * @return the id of the process runner where the process with pid (pid) is 
   * currently running, or null if no such process exists.
   */
  private String processRunnerForPid(int pid) {
    String processRunner = null;
    this.stateLock.readLock().lock();
    try{
      processRunner = this.state.getProcessRunnerForPid(pid);
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return processRunner;
  }

  /**
   * Advances the current pid counter by one and returns it/
   * @return the next pid.
   */
  private int getNextPID() {
    return ++this.currentPID;
  }

  /**
   * Chooses a process runner from the connection pool. 
   * Currently, the process runners are chosen randomly.
   * @return the process runner id or null if there are no available
   * process runners
   */
  private String chooseProcessRunnerFromPool() {
    String selectedProcessRunner = null;
    this.stateLock.readLock().lock();
    try {
      String [] processRunnersIds = new String[this.state.processRunnerCount()];
      if(processRunnersIds.length > 0) {
        this.state.availableProcessRunners().toArray(processRunnersIds);
        int randomIndex = MathHelper.randomIntInRange(0, this.state.processRunnerCount());
        selectedProcessRunner = processRunnersIds[randomIndex];
      }
      else {
        logger.warn("Couldn't find an available process runner");
      }
    }
    finally {
      this.stateLock.readLock().unlock();
    }
    return selectedProcessRunner;
  }
}
