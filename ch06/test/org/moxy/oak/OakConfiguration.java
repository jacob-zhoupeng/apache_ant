package org.moxy.oak;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.moxy.util.ListableProperties;
import org.moxy.util.config.*;

public class OakConfiguration implements org.moxy.util.config.ConfigurationSystem
{
	private File propsFile;
	private ListableProperties props;

		public OakConfiguration()
	{
		String s;
		s = System.getProperty("user.home");
		if(!s.endsWith(System.getProperty("file.separator")))
		{
			s = s+System.getProperty("file.separator");
		}
		s+=".OakTJB";
		propsFile = new File(s,"oak.properties");
		props = new ListableProperties();
		try
		{
			props.load(new FileInputStream(propsFile));
		}
		catch(IOException e)
		{
			throw new java.lang.OutOfMemoryError("hahaha");
		}
	}
	
	public GroupItem[] getFirstGroup()
	{
		return OakPreferences.getConfigurables(1);
	}

	public GroupItem[] getGroup(String className, int page, String id)
	{
		Class targetClass;
		Method method;
		Class[] params;
		Object[] args;
		
		if (className.startsWith("@"))
			className = getPreference(constructID(getPreviousID(id), className.substring(1)));
		
		try
		{
			targetClass = Class.forName(className);
			params = new Class[1];
			params[0] = Integer.TYPE;
			method = targetClass.getMethod("getConfigurables", params);
			args = new Object[1];
			args[0] = new Integer(page);
			return (GroupItem[])method.invoke(null, args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void setPreference(String id, String value)
	{
		props.setProperty(id, value);
        try
		{
			props.store(new FileOutputStream(propsFile));
		} catch (IOException ioe)
		{
			System.out.println("Error occured while saving configuration file. " +
							   "Possible persistant data corruption.");
		}
	}

	public void setListPreference(String id, String[] values)
	{
		props.setList(id, values);
		try
		{
			props.store(new FileOutputStream(propsFile));
		} catch (IOException ioe)
		{
			System.out.println("Error occured while saving configuration file. " +
							   "Possible persistant data corruption.");
		}
	}

	public String getPreference(String id)
	{
		System.out.println("OakConfiguration::getPreference called with id=" + id);
		return props.getProperty(id, null);
	}

	public String[] getListPreference(String id)
	{
		System.out.println("OakConfiguration::getListPreference called with id=" + id);
		return props.getList(id, null);
	}

	public String constructID(String base, String next)
	{
		if (base.length() == 0)
			return next;
		return base + "." + next;
	}

	public String getPreviousID(String id)
	{
		int index;
		
		index = id.lastIndexOf('.');
		if (index < 0)
			return null; // ??
		
		return id.substring(0, index);
	}

}
