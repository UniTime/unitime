/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.solver.ui;

import java.io.Serializable;

import org.dom4j.Element;
import org.unitime.timetable.model.PreferenceLevel;


/**
 * @author Tomas Muller
 */
public class BtbInstructorConstraintInfo implements TimetableInfo, Serializable {
	private static final long serialVersionUID = 2L;
	public static int sVersion = 2; // to be able to do some changes in the future
	public int iPreference = PreferenceLevel.sIntLevelNeutral;
	public Long iInstructorId = null;
	
	public BtbInstructorConstraintInfo() {
		super();
	}
	
	public int getPreference() { return iPreference; }
	public void setPreference(int preference) { iPreference = preference; }
	public Long getInstructorId() { return iInstructorId; }
	public void setInstructorId(Long instructorId) { iInstructorId = instructorId; }
	
	public void load(Element root) throws Exception {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==sVersion) {
			iInstructorId = Long.valueOf(root.elementText("instructor"));
			iPreference = Integer.parseInt(root.elementText("pref"));
		}
	}
	
	public void save(Element root) throws Exception {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addElement("pref").setText(String.valueOf(iPreference));
		root.addElement("instructor").setText(String.valueOf(iInstructorId));
	}

	public boolean saveToFile() {
		return false;
	}
}
