/* Copyright (C) 2000 Christiaan Welvaart
   This file is part of the OAK Configuration Library.

   The OAK Configuration Library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Library General Public License as
   published by the Free Software Foundation; either version 2 of the
   License, or (at your option) any later version.

   The OAK Configuration Library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Library General Public License for more details.

   You should have received a copy of the GNU Library General Public
   License along with the OAK Configuration Library; see the file COPYING.LIB.  If not,
   write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.  */

package com.cjw.util;

import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Container;
import java.util.Vector;
import java.awt.Dimension;

public class TopMinimalLayout implements LayoutManager
{
	public TopMinimalLayout()
	{
	}
	
	public void addLayoutComponent(String  name, Component  comp)
	{
	}
	
	public void layoutContainer(Container parent)
	{
		int i, x, y;
		Component comp;
		Dimension dim;
		
		x = 3;
		y = 3;
		for (i = 0; i < parent.getComponentCount(); i++)
		 {
				parent.setSize(preferredLayoutSize(parent));
				comp = parent.getComponent(i);
				dim = comp.getPreferredSize();
				comp.setBounds(x, y, dim.width, dim.height);
				x += dim.width;
		 }
	}
	
	public Dimension minimumLayoutSize(Container parent)
	{
		int i, width, height;
		Dimension dim;
		Component comp;
		
		width = 0;
		height = 0;
		for (i = 0; i < parent.getComponentCount(); i++)
		 {
				dim = parent.getComponent(i).getMinimumSize();
				if (dim.height > height)
					height = dim.height;
				width += dim.width;
		 }
		
		return new Dimension(width + 6, height + 6);
	}
	
	public Dimension preferredLayoutSize(Container parent)
	{
		int i, width, height;
		Dimension dim;
		Component comp;
		
		width = 0;
		height = 0;
		for (i = 0; i < parent.getComponentCount(); i++)
		 {
				dim = parent.getComponent(i).getPreferredSize();
				if (dim.height > height)
					height = dim.height;
				width += dim.width;
		 }
		
		return new Dimension(width + 6, height + 6);
	}
	
	public void removeLayoutComponent(Component  comp)
	{
	}

}
