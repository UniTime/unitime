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

import org.cpsolver.coursett.constraint.FlexibleConstraint;
import org.cpsolver.coursett.constraint.GroupConstraint;
import org.cpsolver.coursett.model.Lecture;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.ifs.assignment.Assignment;
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
	public GroupConstraintInfo(Assignment<Lecture, Placement> assignment, GroupConstraint gc) {
		super();
		setPreference(gc.getPrologPreference());
		setIsSatisfied(gc.isSatisfied(assignment));
		setName(gc.getName());
	}
	public GroupConstraintInfo(Assignment<Lecture, Placement> assignment, FlexibleConstraint gc) {
		super();
		setPreference(gc.getPrologPreference());
		setIsSatisfied(gc.isHard() || gc.getContext(assignment).getPreference() <= 0.0);
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
	
	public void load(Element root) {
		int version = Integer.parseInt(root.attributeValue("version"));
		if (version==1) {
			if (root.element("name")!=null)
				iName = root.element("name").getText();
			iPreference = root.element("pref").getText();
			iIsSatisfied = Boolean.valueOf(root.element("isSatisfied").getText()).booleanValue();
            iType = root.elementText("type");
		}
	}
	
	public void save(Element root) {
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
