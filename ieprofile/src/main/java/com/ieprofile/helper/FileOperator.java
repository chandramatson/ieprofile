/**
 * 
 */
package com.ieprofile.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author chandrasekharpappala
 * 
 */
public class FileOperator {
	//method for saving attachment on local disk
	public static String saveFile(String filename, InputStream input) {
	String strDirectory = "/testing/";  
	try{
	// Create one directory
	boolean success = (new File(strDirectory)).mkdir();
	if (success) {
	System.out.println("Directory: " 
	 + strDirectory + " created");
	}
	} catch (Exception e) {//Catch exception if any
	  System.err.println("Error: " + e.getMessage());
	}
	      String path = strDirectory+"\\" + filename;
	      try {
	          byte[] attachment = new byte[input.available()];
	          input.read(attachment);
	          File file = new File(path);
	          FileOutputStream out = new FileOutputStream(file);
	          out.write(attachment);
	          input.close();
	          out.close();
	          return path;
	      } catch (IOException e) {
	          e.printStackTrace();
	      }
	      return path;
	  }
}
