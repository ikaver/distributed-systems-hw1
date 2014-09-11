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

public class NodeManagerImpl implements INodeManager {

  private IProcessRunnerFactory processRunnerFactory;
  private ReadWriteLock stateLock;
  private SubscribedProcessRunnersState state;
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

  private void addProcess(int pid, String processRunnerId) {
    this.stateLock.writeLock().lock();
    try {
      this.state.addProcessToProcessRunner(pid, processRunnerId);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }

  private void removeProcess(int pid) {
    this.stateLock.writeLock().lock();
    try {
      this.state.removeProcessFromCurrentProcessRunner(pid);
    }
    finally {
      this.stateLock.writeLock().unlock();
    }
  }

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

  private int getNextPID() {
    return ++this.currentPID;
  }

  /*
   * Chooses a process runner from the connection pool. 
   * Currently, the process runners are chosen randomly.
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
