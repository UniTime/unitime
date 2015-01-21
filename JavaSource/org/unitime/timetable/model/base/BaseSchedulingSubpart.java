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
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
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
