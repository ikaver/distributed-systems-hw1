package com.ikaver.aagarwal.ds.hw1.shared.helpers;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;

/**
 * A mock test process that prints the numbers from 100000 to 1.
 * Useful for testing and debugging.
 */
public class PrinterProcess implements IMigratableProcess {

  private static final long serialVersionUID = 1L;
  private final long randomID;
  private volatile boolean suspending = false;
  private int amountOfPrints;

  private int TOTAL_AMOUNT_OF_PRINTS = 100000;
  private int WAIT_TIME_PER_PRINT_IN_MS = 1000;

  public PrinterProcess(String[] args) {
    amountOfPrints = TOTAL_AMOUNT_OF_PRINTS;
    System.out.println("Called with args" + args.toString());
    randomID = System.currentTimeMillis();
  }

  public void run() {
    System.out.println("The process is running." + randomID);
    while( amountOfPrints > 0 && !suspending) {
      System.out.println("Grepping now!" + "with randomID" + ":" + randomID +
          ".This is the ith invocation:" + amountOfPrints);
      amountOfPrints--;
      try {
        Thread.sleep(WAIT_TIME_PER_PRINT_IN_MS);
      } catch (InterruptedException e) {
      }
    }

    suspending = false;
  }

  public void suspend() {
    suspending = true;
    while (suspending);
    System.out.println("The process is suspended.");		
  }
}
