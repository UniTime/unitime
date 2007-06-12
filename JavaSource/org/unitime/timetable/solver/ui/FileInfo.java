/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver.ui;

import java.io.Serializable;

import org.dom4j.Element;

/**
 * @author Tomas Muller
 */
public class FileInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	
	private String iName = null;
	
	public FileInfo() {}
	
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public TimetableInfo loadInfo(TimetableInfoFileProxy proxy) throws Exception {
		return proxy.loadFromFile(iName);
	}
	public void saveInfo(TimetableInfo info, TimetableInfoFileProxy proxy) throws Exception {
		proxy.saveToFile(iName, info);
	}
	public void deleteFile(TimetableInfoFileProxy proxy) throws Exception {
		proxy.deleteFile(iName);
	}

	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			iName = root.attributeValue("name");
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addAttribute("name", iName);
	}

	public boolean saveToFile() {
		return false;
	}
}
