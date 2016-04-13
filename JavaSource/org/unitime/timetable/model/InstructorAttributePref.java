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
import org.unitime.timetable.model.base.BaseInstructorAttributePref;

public class InstructorAttributePref extends BaseInstructorAttributePref {
	private static final long serialVersionUID = 1096113835301473978L;

	public InstructorAttributePref() {
		super();
	}

	@Override
	public String preferenceText() {
		return getAttribute().getName();
	}

	@Override
	public String preferenceAbbv() { 
        return getAttribute().getCode();
    }

	@Override
	public Object clone() {
		InstructorAttributePref pref = new InstructorAttributePref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setAttribute(getAttribute());
    	return pref;
	}

	@Override
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof InstructorAttributePref)) return false;
    	return ToolBox.equals(getAttribute(),((InstructorAttributePref)other).getAttribute());
    }

	@Override
	public String preferenceTitle() {
		return getPrefLevel().getPrefName() + " " + getAttribute().getName();
	}

	@Override
	public int compareTo(Object o) {
	    if (o==null || !(o instanceof InstructorAttributePref)) return super.compareTo(o);
	    InstructorAttributePref p = (InstructorAttributePref)o;
	    int cmp = getAttribute().compareTo(p.getAttribute());
	    if (cmp!=0) return cmp;
	    return super.compareTo(o);
	}
	
	public Type getType() { return Type.ATTRIBUTE; }
}
