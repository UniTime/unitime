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

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BasePreference;



/**
 * @author Tomas Muller
 */
public abstract class Preference extends BasePreference implements Comparable {
	private static final long serialVersionUID = 1L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

    /** Blank Pref Value **/
    public static final String BLANK_PREF_VALUE = "-";
    
    public static enum Type {
    	TIME(TimePref.class),
    	ROOM(RoomPref.class),
    	ROOM_GROUP(RoomGroupPref.class),
    	ROOM_FEATURE(RoomFeaturePref.class),
    	BUILDING(BuildingPref.class),
    	DISTRIBUTION(DistributionPref.class),
    	PERIOD(ExamPeriodPref.class),
    	DATE(DatePatternPref.class),
    	ATTRIBUTE(InstructorAttributePref.class),
    	COURSE(InstructorCoursePref.class),
    	INSTRUCTOR(InstructorPref.class)
    	;
    	
    	Class<? extends Preference> iClazz;
    	Type(Class<? extends Preference> clazz) { iClazz = clazz; }
    	public Class<? extends Preference> getImplementation() { return iClazz; }
    	
    	int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags) {
			return (in(flags) ? flags : flags + flag());
		}
		public int clear(int flags) {
			return (in(flags) ? flags - flag() : flags);
		}
		public static int toInt(Type... types) {
			int ret = 0;
			for (Type t: types)
				ret += t.flag();
			return ret;
		}
    }

    /*[CONSTRUCTOR MARKER BEGIN]*/
	public Preference () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Preference (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public String preferenceTitle(String nameFormat) {
		return getPrefLevel().getPrefName()+" "+preferenceText();
	}
	
	public String preferenceTitle() {
		return getPrefLevel().getPrefName()+" "+preferenceText();
	}
	
	public String preferenceHtml(String nameFormat) {
		return preferenceHtml(nameFormat, ApplicationProperty.PreferencesHighlighClassPreferences.isTrue());
	}
	
	public String preferenceHtml(String nameFormat, boolean highlightClassPrefs) {
    	StringBuffer sb = new StringBuffer("<span ");
    	String style = "font-weight:bold;";
		if (this.getPrefLevel().getPrefId().intValue() != 4) {
			style += "color:" + this.getPrefLevel().prefcolor() + ";";
		}
		if (this.getOwner() != null && this.getOwner() instanceof Class_ && highlightClassPrefs) {
			style += "background: #ffa;";
		}
		sb.append("style='" + style + "' ");
		String owner = "";
		if (getOwner() != null && getOwner() instanceof Class_) {
			owner = " (" + MSG.prefOwnerClass() + ")";
		} else if (getOwner() != null && getOwner() instanceof SchedulingSubpart) {
			owner = " (" + MSG.prefOwnerSchedulingSubpart() + ")";
		} else if (getOwner() != null && getOwner() instanceof DepartmentalInstructor) {
			owner = " (" + MSG.prefOwnerInstructor() + ")";
		} else if (getOwner() != null && getOwner() instanceof Exam) {
			owner = " (" + MSG.prefOwnerExamination() + ")";
		} else if (getOwner() != null && getOwner() instanceof Department) {
			owner = " (" + MSG.prefOwnerDepartment() + ")";
		} else if (getOwner() != null && getOwner() instanceof Session) {
			owner = " (" + MSG.prefOwnerSession() + ")";
		}
		sb.append("onmouseover=\"showGwtHint(this, '" + preferenceTitle(nameFormat) + owner + "');\" onmouseout=\"hideGwtHint();\">");
		
		sb.append(this.preferenceAbbv(nameFormat));
		sb.append("</span>");
		return (sb.toString());
	}
    
    public String preferenceText(String nameFormat) {
    	return preferenceText();
    }
    
    public abstract String preferenceText();
    public String preferenceAbbv() {
        return preferenceText();
    }
    public String preferenceAbbv(String nameFormat) {
        return preferenceAbbv();
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
        if(o == null || !(o instanceof Preference))
            throw new RuntimeException("Object must be of type Preference");
        Preference p = (Preference) o;
        int cmp = getClass().getName().compareTo(o.getClass().getName());
        if (cmp!=0) return cmp;
        
        if (this.getUniqueId()==null || p.getUniqueId()==null)
        	return -1;

        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(p.getUniqueId() == null ? -1 : p.getUniqueId());
	}
	
	public boolean appliesTo(PreferenceGroup group) {
		return true;
	}
	
    public boolean weakenHardPreferences() {
    	if (PreferenceLevel.sRequired.equals(getPrefLevel().getPrefProlog()))
    		setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyPreferred));
    	if (PreferenceLevel.sProhibited.equals(getPrefLevel().getPrefProlog()))
    		setPrefLevel(PreferenceLevel.getPreferenceLevel(PreferenceLevel.sStronglyDiscouraged));
    	return true;
    }
    
    public abstract Object clone();
    public abstract boolean isSame(Preference other);
    public abstract Type getType();
}
