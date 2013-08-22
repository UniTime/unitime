/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import net.sf.cpsolver.ifs.util.ToolBox;

import org.unitime.timetable.model.base.BaseDatePatternPref;

public class DatePatternPref extends BaseDatePatternPref {
	private static final long serialVersionUID = 1L;

	public DatePatternPref() {
		super();
	}

	@Override
	public String preferenceText() {
		return(this.getDatePattern().getName());
	}

	@Override
	public Object clone() {
		DatePatternPref pref = new DatePatternPref();
	 	pref.setPrefLevel(getPrefLevel());
	 	pref.setDatePattern(getDatePattern());
	 	return pref;
	}

	@Override
	public boolean isSame(Preference other) {
		if (other==null || !(other instanceof DatePatternPref)) return false;
    	return ToolBox.equals(getDatePattern(),((DatePatternPref)other).getDatePattern());
	}
	
	@Override
	public boolean appliesTo(PreferenceGroup group) {
		DatePattern dp = group.effectiveDatePattern();
		return dp != null && dp.findChildren().contains(getDatePattern());
	}
	
	@Override
	public int compareTo(Object o) {
		if (o instanceof DatePatternPref) {
			int cmp = getDatePattern().compareTo(((DatePatternPref)o).getDatePattern());
			if (cmp != 0) return cmp;
		}
		return super.compareTo(o);
	}

}
