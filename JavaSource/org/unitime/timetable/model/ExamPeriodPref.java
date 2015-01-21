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
import org.unitime.timetable.model.base.BaseExamPeriodPref;




/**
 * @author Tomas Muller
 */
public class ExamPeriodPref extends BaseExamPeriodPref {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamPeriodPref () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamPeriodPref (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public String preferenceText() { 
		return (this.getExamPeriod().getName());
    }

    public String preferenceAbbv() { 
        return (this.getExamPeriod().getAbbreviation());
    }

    public Object clone() {
        ExamPeriodPref pref = new ExamPeriodPref();
    	pref.setPrefLevel(getPrefLevel());
    	pref.setExamPeriod(getExamPeriod());
    	return pref;
    }
    public boolean isSame(Preference other) {
    	if (other==null || !(other instanceof ExamPeriodPref)) return false;
    	return ToolBox.equals(getExamPeriod(),((ExamPeriodPref)other).getExamPeriod());
    }

	public String preferenceTitle() {
		return getPrefLevel().getPrefName()+" "+getExamPeriod().getName();
	}
	
	public int compareTo(Object o) {
	    if (o==null || !(o instanceof ExamPeriodPref)) return super.compareTo(o);
	    ExamPeriodPref p = (ExamPeriodPref)o;
	    int cmp = getExamPeriod().compareTo(p.getExamPeriod());
	    if (cmp!=0) return cmp;
	    return super.compareTo(o);
	}
}
