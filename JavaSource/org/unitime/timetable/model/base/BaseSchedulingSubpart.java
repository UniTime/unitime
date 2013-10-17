/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PreferenceGroup;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseSchedulingSubpart extends PreferenceGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer iMinutesPerWk;
	private Boolean iAutoSpreadInTime;
	private Boolean iStudentAllowOverlap;
	private String iSchedulingSubpartSuffixCache;
	private String iCourseName;
	private Integer iLimit;
	private Long iUniqueIdRolledForwardFrom;

	private Session iSession;
	private ItypeDesc iItype;
	private SchedulingSubpart iParentSubpart;
	private InstrOfferingConfig iInstrOfferingConfig;
	private DatePattern iDatePattern;
	private Set<SchedulingSubpart> iChildSubparts;
	private Set<Class_> iClasses;
	private Set<CourseCreditUnitConfig> iCreditConfigs;

	public static String PROP_MIN_PER_WK = "minutesPerWk";
	public static String PROP_AUTO_TIME_SPREAD = "autoSpreadInTime";
	public static String PROP_STUDENT_ALLOW_OVERLAP = "studentAllowOverlap";
	public static String PROP_SUBPART_SUFFIX = "schedulingSubpartSuffixCache";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseSchedulingSubpart() {
		initialize();
	}

	public BaseSchedulingSubpart(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Integer getMinutesPerWk() { return iMinutesPerWk; }
	public void setMinutesPerWk(Integer minutesPerWk) { iMinutesPerWk = minutesPerWk; }

	public Boolean isAutoSpreadInTime() { return iAutoSpreadInTime; }
	public Boolean getAutoSpreadInTime() { return iAutoSpreadInTime; }
	public void setAutoSpreadInTime(Boolean autoSpreadInTime) { iAutoSpreadInTime = autoSpreadInTime; }

	public Boolean isStudentAllowOverlap() { return iStudentAllowOverlap; }
	public Boolean getStudentAllowOverlap() { return iStudentAllowOverlap; }
	public void setStudentAllowOverlap(Boolean studentAllowOverlap) { iStudentAllowOverlap = studentAllowOverlap; }

	public String getSchedulingSubpartSuffixCache() { return iSchedulingSubpartSuffixCache; }
	public void setSchedulingSubpartSuffixCache(String schedulingSubpartSuffixCache) { iSchedulingSubpartSuffixCache = schedulingSubpartSuffixCache; }

	public String getCourseName() { return iCourseName; }
	public void setCourseName(String courseName) { iCourseName = courseName; }

	public Integer getLimit() { return iLimit; }
	public void setLimit(Integer limit) { iLimit = limit; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public ItypeDesc getItype() { return iItype; }
	public void setItype(ItypeDesc itype) { iItype = itype; }

	public SchedulingSubpart getParentSubpart() { return iParentSubpart; }
	public void setParentSubpart(SchedulingSubpart parentSubpart) { iParentSubpart = parentSubpart; }

	public InstrOfferingConfig getInstrOfferingConfig() { return iInstrOfferingConfig; }
	public void setInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig) { iInstrOfferingConfig = instrOfferingConfig; }

	public DatePattern getDatePattern() { return iDatePattern; }
	public void setDatePattern(DatePattern datePattern) { iDatePattern = datePattern; }

	public Set<SchedulingSubpart> getChildSubparts() { return iChildSubparts; }
	public void setChildSubparts(Set<SchedulingSubpart> childSubparts) { iChildSubparts = childSubparts; }
	public void addTochildSubparts(SchedulingSubpart schedulingSubpart) {
		if (iChildSubparts == null) iChildSubparts = new HashSet<SchedulingSubpart>();
		iChildSubparts.add(schedulingSubpart);
	}

	public Set<Class_> getClasses() { return iClasses; }
	public void setClasses(Set<Class_> classes) { iClasses = classes; }
	public void addToclasses(Class_ class_) {
		if (iClasses == null) iClasses = new HashSet<Class_>();
		iClasses.add(class_);
	}

	public Set<CourseCreditUnitConfig> getCreditConfigs() { return iCreditConfigs; }
	public void setCreditConfigs(Set<CourseCreditUnitConfig> creditConfigs) { iCreditConfigs = creditConfigs; }
	public void addTocreditConfigs(CourseCreditUnitConfig courseCreditUnitConfig) {
		if (iCreditConfigs == null) iCreditConfigs = new HashSet<CourseCreditUnitConfig>();
		iCreditConfigs.add(courseCreditUnitConfig);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SchedulingSubpart)) return false;
		if (getUniqueId() == null || ((SchedulingSubpart)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SchedulingSubpart)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SchedulingSubpart["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "SchedulingSubpart[" +
			"\n	AutoSpreadInTime: " + getAutoSpreadInTime() +
			"\n	DatePattern: " + getDatePattern() +
			"\n	InstrOfferingConfig: " + getInstrOfferingConfig() +
			"\n	Itype: " + getItype() +
			"\n	MinutesPerWk: " + getMinutesPerWk() +
			"\n	ParentSubpart: " + getParentSubpart() +
			"\n	SchedulingSubpartSuffixCache: " + getSchedulingSubpartSuffixCache() +
			"\n	StudentAllowOverlap: " + getStudentAllowOverlap() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
