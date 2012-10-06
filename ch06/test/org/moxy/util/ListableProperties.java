package org.moxy.util;
import java.io.*;
import java.util.*;
public class ListableProperties{
 private Hashtable list = new Hashtable();
 private Hashtable prop = new Hashtable();

 public ListableProperties(){}

 public String getProperty(String key){
  return getProperty(key,null);
 }

 public String getProperty(String key, String def){
  String s = (String)prop.get(key);
  if(s == null)return def;
  return s;
 } 

 public void setProperty(String key, String value){
  if(value == null){
   prop.remove(key);
   return;
  }
  prop.put(key,value);
 }

 public String[] getList(String key){
  return getList(key,null);
 }
 
 public String[] getList(String key, String[] def){
  try{
   String[] ret = (String[])list.get(key);
   return ret;
  }catch(ClassCastException cce){
   return def;
  }
 }

 public void setList(String key, String[] value){
  if(value == null){
   list.remove(key);
   return;
  }
  list.put(key,value);
 }

 public Enumeration getProperties(){
  return prop.elements();
 }

 public Enumeration getLists(){
  return list.elements();
 }

 public boolean isEmpty(){
  return prop.isEmpty() && list.isEmpty();
 }
 
 public Enumeration getPropertyKeys(){
  return prop.keys();
 }
 
 public Enumeration getListKeys(){
  return list.keys();
 }

 public void load(InputStream in) throws IOException{
  BufferedReader reader = new BufferedReader(new InputStreamReader(in));
  String line = "";
  boolean list=false;
  while(line != null){
   line = reader.readLine();
   if(line==null)continue;
   line = line.trim();
   if(line.equals("[LIST]")){list=true; continue;}
   if(line.equals("[VALUE]")){list=false; continue;}
   if(list){
    String key = line.substring(0,line.lastIndexOf("=")).trim();
    try{
     int size = Integer.parseInt(line.substring(line.lastIndexOf("=")+1).trim());
     String[] value = new String[size];
     for(int x = 0; x<size; x++)
      value[x] = reader.readLine().trim();
     this.list.put(key,value);
    }catch(NumberFormatException nfe){
nfe.printStackTrace();
     throw new IOException(nfe.toString());
    }
    continue;
   }
   //read key/value
   if(line.endsWith("=")){
    String key = line.substring(0,line.length()-1);
    prop.put(key,reader.readLine().trim());
    continue;
   }
   //dunno what the hell we just read so we'll just ignore it.
  }
 }

 public void store(OutputStream out) throws IOException{
  out.write( ("[VALUE]\n").getBytes() );//start the value section
  Enumeration enum = prop.keys();//get the key's for the prop table
  String key;
  while(enum.hasMoreElements()){
   key = (String)enum.nextElement();
   if(prop.get(key) != null){
    out.write( (key+"=\n").getBytes() );//write the key= line
    out.write( ("\t"+((String)prop.get(key))+"\n").getBytes() );//write the value
   }
  }
  //finished the value section 
  key = null; enum = null; //reset variables
  out.write( ("[LIST]\n").getBytes() );//start list section
  enum = list.keys();
  String[] sarry;
  while(enum.hasMoreElements()){
   key = (String)enum.nextElement();
   sarry = (String[])list.get(key);
   if(sarry!=null){
    out.write( (key+"="+sarry.length+"\n").getBytes() );//write key=size line
    for(int x = 0; x<sarry.length; x++)
     out.write( ("\t"+sarry[x]+"\n").getBytes() ); //write the values out
   }
  }

 }

}
   
