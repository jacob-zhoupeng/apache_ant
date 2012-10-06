package org.moxy.util;
import java.util.*;
public class ThreadInteruptingVector extends Vector implements Pingable{
 private long threadLife;
 private Ping controler;

 public ThreadInteruptingVector(long threadLife){
  super();
  this.threadLife = threadLife;
  controler = new Ping(this,((long)(threadLife * 0.75)));
  controler.start();
 }

 public synchronized boolean add(Object obj){
  return super.add( new TimeWrapper(obj));
 }

 public synchronized boolean remove(Object obj){
  Enumeration enum = elements();
  TimeWrapper test;
  while(enum.hasMoreElements()){
   test = (TimeWrapper)enum.nextElement();
   if(test.getObject() == obj){
    return super.remove(test);
   }
  }
  return false;
 }

 public synchronized void ping(){
  Enumeration enum = elements();
  TimeWrapper current;
  while(enum.hasMoreElements()){
   current = (TimeWrapper)enum.nextElement();
   if((current != null) && current.getAge() > threadLife){
    ((Thread)current.getObject()).interrupt();
    super.remove(current);
   }
  }
 }

}
