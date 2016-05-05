/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
	
	public void load(Element root) {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==sVersion) {
			iInstructorId = Long.valueOf(root.elementText("instructor"));
			iPreference = Integer.parseInt(root.elementText("pref"));
		}
	}
	
	public void save(Element root) {
		root.addAttribute("version", String.valueOf(sVersion));
		root.addElement("pref").setText(String.valueOf(iPreference));
		root.addElement("instructor").setText(String.valueOf(iInstructorId));
	}

	public boolean saveToFile() {
		return false;
	}
}
