package com.ikaver.aagarwal.ds.hw1.shared.transactionalio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Class that allows migratable processes to write files. It will maintain all 
 * the information required in order to continue performing operations on the file, 
 * even if the process is transferred to another node.
 */
public class TransactionalFileOutputStream extends OutputStream implements Serializable{

  private static final long serialVersionUID = 6197467219240327863L;
  
  private File file;
  
  public TransactionalFileOutputStream(String file) throws IOException {
    this(file, false);
  }

  public TransactionalFileOutputStream(String file, boolean append) throws IOException {
    this.file = new File(file);
    FileOutputStream stream = new FileOutputStream(this.file, append);
    stream.close();
  }

  @Override
  public void write(int b) throws IOException {
    FileOutputStream stream = new FileOutputStream(this.file, true);
    stream.write(b);
    stream.close();
  }

}
