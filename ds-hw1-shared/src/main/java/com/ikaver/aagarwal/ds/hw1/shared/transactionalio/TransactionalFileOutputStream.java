package com.ikaver.aagarwal.ds.hw1.shared.transactionalio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable{

  private static final long serialVersionUID = 6197467219240327863L;
  
  private File file;
  private long offset;

  public TransactionalFileOutputStream(File file, boolean append) throws IOException {
    this.file = file;
    this.offset = append ? file.length() : 0;
    FileOutputStream stream = new FileOutputStream(this.file, append);
    stream.close();
  }

  @Override
  public void write(int b) throws IOException {
    FileOutputStream stream = new FileOutputStream(this.file, true);
    byte [] array = new byte[1];
    array[0] = new Integer(b).byteValue();
    stream.write(array, (int)this.offset, 1);
    stream.close();
    ++this.offset;
  }

}
