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
package org.unitime.timetable.model;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseInstructorPref;
import org.unitime.timetable.util.NameFormat;

public class InstructorPref extends BaseInstructorPref {
	private static final long serialVersionUID = 6242763326071980017L;

	public InstructorPref() {
		super();
	}
	
	@Override
	public String preferenceText() {
		return NameFormat.LAST_FIRST_MIDDLE_TITLE.format(getInstructor());
	}
	
	@Override
	public String preferenceText(String nameFormat) {
		if (nameFormat == null)
			return NameFormat.LAST_FIRST_MIDDLE_TITLE.format(getInstructor());
		else
			return getInstructor().getName(nameFormat);
	}


	@Override
	public String preferenceAbbv() {
		return NameFormat.LAST_INITIAL.format(getInstructor());
    }

	@Override
	public String preferenceAbbv(String nameFormat) {
		if (nameFormat == null)
			return NameFormat.LAST_INITIAL.format(getInstructor());
		else
			return getInstructor().getName(nameFormat);
    }

	@Override
	public Object clone() {
		InstructorPref pref = new InstructorPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setInstructor(getInstructor());
    	return pref;
	}

	@Override
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof InstructorPref)) return false;
    	return ToolBox.equals(getInstructor(),((InstructorPref)other).getInstructor());
    }

	@Override
	public String preferenceTitle() {
		return getPrefLevel().getPrefName() + " " + preferenceText();
	}
	
	@Override
	public String preferenceTitle(String nameFormat) {
		return getPrefLevel().getPrefName() + " " + preferenceText(nameFormat);
	}

	@Override
	public int compareTo(Object o) {
	    if (o==null || !(o instanceof InstructorPref)) return super.compareTo(o);
	    InstructorPref p = (InstructorPref)o;
	    int cmp = getInstructor().compareTo(p.getInstructor());
	    if (cmp!=0) return cmp;
	    return super.compareTo(o);
	}
	
	@Override
	public boolean weakenHardPreferences() {
		return true;
	}
}
