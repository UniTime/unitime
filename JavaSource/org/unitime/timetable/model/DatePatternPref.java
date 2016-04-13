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
import org.unitime.timetable.model.base.BaseDatePatternPref;

/**
 * @author Tomas Muller
 */
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

	public Type getType() { return Type.DATE; }
}
