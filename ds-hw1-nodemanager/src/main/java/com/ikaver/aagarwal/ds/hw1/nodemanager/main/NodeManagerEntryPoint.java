package com.ikaver.aagarwal.ds.hw1.nodemanager.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ikaver.aagarwal.ds.hw1.nodemanager.NodeManagerController;
import com.ikaver.aagarwal.ds.hw1.nodemanager.ProcessRunnerStateRefreshThread;
import com.ikaver.aagarwal.ds.hw1.shared.Definitions;

import static java.util.concurrent.TimeUnit.*;

public class NodeManagerEntryPoint {

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new NodeManagerModule());
    NodeManagerController controller = injector
        .getInstance(NodeManagerController.class);
    ProcessRunnerStateRefreshThread refresh = injector
        .getInstance(ProcessRunnerStateRefreshThread.class);

    // TODO: may need to clean up scheduler code
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(refresh, 0,
        Definitions.WAIT_TIME_IN_SECONDS_FOR_NODE_MANAGER_REFRESH, SECONDS);
    controller.readInput();
    scheduler.shutdown();
  }

}
