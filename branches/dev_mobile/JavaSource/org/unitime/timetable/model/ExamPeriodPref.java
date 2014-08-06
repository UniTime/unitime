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
