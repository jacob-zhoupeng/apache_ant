/*
 * This class allows the setting of some global variables.
 */
package org.moxy.oak;
public class Preferences{
 /**
  * The debug level the bot should run at this is a number 'tween 0 and 20
  * 0 is the same as not setting it
  * 20 will basically print more information that you can read.
  * set this flag at runtime by passing -DDEBUG_LEVEL=XX
  * where XX is a number.
  * ex: java -DDEBUG_LEVEL=13 org.moxy.oak.Oak
  */
 public static final int DEBUG_LEVEL;

 /**
  * This will be moved into Oak eventually.
  * this is the major release number.
  * This will change with each MAJOR rewrite.
  */
 public static final int major_version = 0;
 /**
  * This is the minor version this will change
  * with each new release.
  */
 public static final int minor_version = 99;
 /**
  * This is the revision number this will
  * change with each new development release.
  * this will be zero for all stable releases.
  */
 public static final int rev_version = 17;
 /**
  * Concatenates all the version int's together to form 
  * a string that looks like: 0.99.16
  */
 public static final String versionNumString = major_version+"."+minor_version+"."+rev_version;
 /**
  * Concatenates all the version int's and 
  * "Oak The Java Bot version: to form a
  * string that looks like:
  * Oak the Java Bot version: 0.99.16
  */
 public static final String versionString = "Oak the Java Bot version: "+versionNumString;

 static{
  int d;  
  try{
   d = Integer.parseInt(System.getProperty("DEBUG_LEVEL"));
  }catch(NumberFormatException nfe){
   d = 0;
  }
  DEBUG_LEVEL = d;
 }
}
