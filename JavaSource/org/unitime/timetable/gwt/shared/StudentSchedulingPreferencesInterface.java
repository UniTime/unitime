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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingPreferencesInterface implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;
	private ClassModality iClassModality = ClassModality.DiscouragedOnline;
	private ScheduleGaps iScheduleGaps = ScheduleGaps.NoPreference;
	private boolean iAllowClassDates = true;
	private boolean iAllowRequireOnline = true;
	private Date iClassDateFrom, iClassDateTo;
	private String iCustomNote = null;
	
	public StudentSchedulingPreferencesInterface() {}
	public StudentSchedulingPreferencesInterface(StudentSchedulingPreferencesInterface pref) {
		iClassModality = pref.iClassModality;
		iScheduleGaps = pref.iScheduleGaps;
		iAllowClassDates = pref.iAllowClassDates;
		iAllowRequireOnline = pref.iAllowRequireOnline;
		iClassDateFrom = pref.iClassDateFrom;
		iClassDateTo = pref.iClassDateTo;
		iCustomNote = pref.iCustomNote;
	}
	
	public ClassModality getClassModality() { return iClassModality; }
	public void setClassModality(ClassModality cm) { iClassModality = cm; }
	
	public ScheduleGaps getScheduleGaps() { return iScheduleGaps; }
	public void setScheduleGaps(ScheduleGaps sg) { iScheduleGaps = sg; }
	
	public boolean isAllowClassDates() { return iAllowClassDates; }
	public void setAllowClassDates(boolean acd) { iAllowClassDates = acd; }
	public boolean isAllowRequireOnline() { return iAllowRequireOnline; }
	public void setAllowRequireOnline(boolean reqOnline) { iAllowRequireOnline = reqOnline; }
	
	public Date getClassDateFrom() { return iClassDateFrom; }
	public void setClassDateFrom(Date fromDate) { iClassDateFrom = fromDate; }
	
	public Date getClassDateTo() { return iClassDateTo; }
	public void setClassDateTo(Date toDate) { iClassDateTo = toDate; }
	
	public boolean hasCustomNote() { return iCustomNote != null && !iCustomNote.isEmpty(); }
	public String getCustomNote() { return iCustomNote; }
	public void setCustomNote(String note) { iCustomNote = note; }
	
	public static enum ClassModality implements IsSerializable, Serializable {
		NoPreference, DiscouragedOnline, PreferredOnline, RequiredOnline,
		;
	}
	
	public static enum ScheduleGaps implements IsSerializable, Serializable {
		NoPreference, PreferBackToBack, DiscourageBackToBack,
		;
	}
}
