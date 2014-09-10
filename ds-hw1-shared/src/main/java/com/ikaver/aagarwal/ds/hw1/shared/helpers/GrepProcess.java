package com.ikaver.aagarwal.ds.hw1.shared.helpers;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;

import com.ikaver.aagarwal.ds.hw1.shared.IMigratableProcess;
import com.ikaver.aagarwal.ds.hw1.shared.transactionalio.TransactionalFileInputStream;
import com.ikaver.aagarwal.ds.hw1.shared.transactionalio.TransactionalFileOutputStream;

public class GrepProcess implements IMigratableProcess
{
  /**
   * 
   */
  private static final long serialVersionUID = -3893956830773780468L;
  private TransactionalFileInputStream  inFile;
  private TransactionalFileOutputStream outFile;
  private String query;

  private volatile boolean suspending;

  public GrepProcess(String args[]) throws Exception
  {
    if (args.length != 3) {
      System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
      throw new Exception("Invalid Arguments");
    }
    
    query = args[0];
    inFile = new TransactionalFileInputStream(args[1]);
    outFile = new TransactionalFileOutputStream(args[2], false);
  }

  public void run()
  {
    PrintStream out = new PrintStream(outFile);
    DataInputStream in = new DataInputStream(inFile);

    try {
      while (!suspending) {
        String line = in.readLine();

        if (line == null) break;
        
        if (line.contains(query)) {
          out.println(line);
        }
        
        // Make grep take longer so that we don't require extremely large files for interesting results
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // ignore it
        }
      }
    } catch (EOFException e) {
      //End of File
    } catch (IOException e) {
      System.out.println ("GrepProcess: Error: " + e);
    }


    suspending = false;
  }

  public void suspend()
  {
    suspending = true;
    while (suspending);
  }

}