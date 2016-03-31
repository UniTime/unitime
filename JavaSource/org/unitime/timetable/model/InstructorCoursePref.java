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
import org.unitime.timetable.model.base.BaseInstructorCoursePref;

public class InstructorCoursePref extends BaseInstructorCoursePref {
	private static final long serialVersionUID = 2827106681341764953L;

	public InstructorCoursePref() {
		super();
	}
	
	@Override
	public String preferenceText() {
		return getCourse().getCourseName();
	}

	@Override
	public String preferenceAbbv() { 
        return getCourse().getCourseName();
    }

	@Override
	public Object clone() {
		InstructorCoursePref pref = new InstructorCoursePref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setCourse(getCourse());
    	return pref;
	}

	@Override
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof InstructorCoursePref)) return false;
    	return ToolBox.equals(getCourse(),((InstructorCoursePref)other).getCourse());
    }

	@Override
	public String preferenceTitle() {
		return getPrefLevel().getPrefName() + " " + getCourse().getCourseName();
	}

	@Override
	public int compareTo(Object o) {
	    if (o==null || !(o instanceof InstructorCoursePref)) return super.compareTo(o);
	    InstructorCoursePref p = (InstructorCoursePref)o;
	    int cmp = getCourse().compareTo(p.getCourse());
	    if (cmp!=0) return cmp;
	    return super.compareTo(o);
	}

}
