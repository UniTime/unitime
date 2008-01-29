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

import net.sf.cpsolver.coursett.constraint.GroupConstraint;

import org.dom4j.Element;


/**
 * @author Tomas Muller
 */
public class GroupConstraintInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 1L;
	public static int sVersion = 1; // to be able to do some changes in the future
	public String iPreference = "0";
	public boolean iIsSatisfied = false;
	public String iName = null;
    public String iType = null;
	
	public GroupConstraintInfo() {
		super();
	}
	public GroupConstraintInfo(GroupConstraint gc) {
		super();
		setPreference(gc.getPrologPreference());
		setIsSatisfied(gc.isSatisfied());
		setName(gc.getName());
	}
	
	public String getPreference() { return iPreference; }
	public void setPreference(String preference) { iPreference = preference; }
	public boolean isSatisfied() { return iIsSatisfied; }
	public void setIsSatisfied(boolean isSatisfied) { iIsSatisfied = isSatisfied; }
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
    public String getType() { return iType; }
    public void setType(String type) { iType = type; }
	
	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			if (root.element("name")!=null)
				iName = root.element("name").getText();
			iPreference = root.element("pref").getText();
			iIsSatisfied = Boolean.valueOf(root.element("isSatisfied").getText()).booleanValue();
            iType = root.elementText("type");
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		if (iName!=null)
			root.addElement("name").setText(iName);
		root.addElement("pref").setText(iPreference);
        if (iType!=null)
            root.addElement("type").setText(iType);
		root.addElement("isSatisfied").setText(String.valueOf(iIsSatisfied));
	}

	public boolean saveToFile() {
		return false;
	}
}
