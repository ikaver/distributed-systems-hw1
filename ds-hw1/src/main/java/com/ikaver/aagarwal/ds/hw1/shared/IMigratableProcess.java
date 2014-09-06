package com.ikaver.aagarwal.ds.hw1.shared;

import java.io.Serializable;

public interface IMigratableProcess extends Serializable, Runnable {

  public void suspend();
}
