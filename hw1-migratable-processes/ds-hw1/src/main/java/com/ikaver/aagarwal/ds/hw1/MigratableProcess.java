package com.ikaver.aagarwal.ds.hw1;

import java.io.Serializable;

public interface MigratableProcess extends Serializable, Runnable {
  public void suspend();
}
