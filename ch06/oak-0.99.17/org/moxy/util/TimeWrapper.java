package org.moxy.util;
public class TimeWrapper{
 private long age;
 private Object obj;
 
 public TimeWrapper(Object obj){
  this.obj = obj;
 }

 private void updateAge(){
  age = System.currentTimeMillis();
 }

 public long getAge(){
  return (System.currentTimeMillis() - age);
 }

 public Object getObject(){
  updateAge();
  return(obj);
 }

}

