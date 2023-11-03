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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

import jakarta.persistence.Transient;

/**
 * @author Tomas Muller
 */
public class XSchedulingRule implements Serializable, Externalizable {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iRuleName;
	private String iStudentFilter;
	private String iInstructonalMethod;
	private String iCourseName;
	private String iCourseType;
	private Boolean iDisjunctive;
	private Boolean iAppliesToFilter;
	private Boolean iAppliesToOnline;
	private Boolean iAppliesToBatch;
	private Boolean iAdminOverride;
	private Boolean iAdvisorOverride;

	public XSchedulingRule(StudentSchedulingRule rule) {
		iUniqueId = rule.getUniqueId();
		iRuleName = rule.getRuleName();
		iStudentFilter = rule.getStudentFilter();
		iInstructonalMethod = rule.getInstructonalMethod();
		iCourseName = rule.getCourseName();
		iCourseType = rule.getCourseType();
		iDisjunctive = rule.getDisjunctive();
		iAppliesToFilter = rule.getAppliesToFilter();
		iAppliesToOnline = rule.getAppliesToOnline();
		iAppliesToBatch = rule.getAppliesToBatch();
		iAdminOverride = rule.getAdminOverride();
		iAdvisorOverride = rule.getAdvisorOverride();
	}
	
    public XSchedulingRule(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }

	public Long getUniqueId() { return iUniqueId; }
	public String getRuleName() { return iRuleName; }
	public String getStudentFilter() { return iStudentFilter; }
	public String getInstructonalMethod() { return iInstructonalMethod; }
	public String getCourseName() { return iCourseName; }
	public String getCourseType() { return iCourseType; }
	public Boolean isDisjunctive() { return iDisjunctive; }
	public Boolean isAppliesToFilter() { return iAppliesToFilter; }
	public Boolean isAppliesToOnline() { return iAppliesToOnline; }
	public Boolean isAppliesToBatch() { return iAppliesToBatch; }
	public Boolean isAdminOverride() { return iAdminOverride; }
	public Boolean isAdvisorOverride() { return iAdvisorOverride; }
	
	public boolean hasCourseName() {
		return getCourseName() != null && !getCourseName().isEmpty();
	}
	
	public boolean matchesCourseName(String cn) {
		// not set > always matches
		if (!hasCourseName()) return true;
		if (getCourseName().startsWith("!")) {
			return cn != null && !cn.matches(getCourseName().substring(1));
		}
		return cn != null && cn.matches(getCourseName());
	}
	
	public boolean hasCourseType() {
		return getCourseType() != null && !getCourseType().isEmpty();
	}
	
	public boolean matchesCourseType(String ct) {
		// not set > always matches
		if (!hasCourseType()) return true;
		if (getCourseType().startsWith("!")) {
			return ct != null && !ct.matches(getCourseType().substring(1));
		} else {
			return ct != null && ct.matches(getCourseType());
		}
	}
	
	public boolean matchesCourseType(CourseType ct) {
		return matchesCourseType(ct == null ? null : ct.getReference());
	}
	
	public boolean hasInstructionalMethod() {
		return getInstructonalMethod() != null && !getInstructonalMethod().isEmpty();
	}
	
	public boolean matchesInstructionalMethod(String im) {
		// not set > always matches
		if (!hasInstructionalMethod()) return true;
		if (getInstructonalMethod().startsWith("!")) {
			return im != null && !im.matches(getInstructonalMethod().substring(1));
		} else {
			return im != null && im.matches(getInstructonalMethod());
		}
	}
	
	public boolean matchesInstructionalMethod(InstructionalMethod im) {
		return matchesInstructionalMethod(im == null ? null : im.getReference());
	}
	
	public boolean matchesInstructionalMethod(XInstructionalMethod im) {
		return matchesInstructionalMethod(im == null ? null : im.getReference());
	}
	
	public boolean matchesCourse(XCourseId course, org.hibernate.Session hibSession) {
		if (isDisjunctive()) {
			if (hasCourseName() && matchesCourseName(course.getCourseName())) return true;
			if (hasCourseType() && matchesCourseType(course.getType())) return true;
			if (hasInstructionalMethod()) {
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), hibSession);
				if (offering != null)
					for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
						if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) return true;	
					}
			}
			return false;
		} else {
			if (hasCourseName() && !matchesCourseName(course.getCourseName())) return false;
			if (hasCourseType() && !matchesCourseType(course.getType())) return false;
			if (hasInstructionalMethod()) {
				InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(course.getOfferingId(), hibSession);
				boolean hasMatchingConfig = false;
				if (offering != null)
					for (InstrOfferingConfig config: offering.getInstrOfferingConfigs()) {
						if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) {
							hasMatchingConfig = true;	
							break;
						}
					}
				if (!hasMatchingConfig) return false;
			}
			return true;
		}
	}
	
	public boolean matchesCourse(XCourseId course, OnlineSectioningServer server) {
		if (isDisjunctive()) {
			if (hasCourseName() && matchesCourseName(course.getCourseName())) return true;
			if (hasCourseType() && matchesCourseType(course.getType())) return true;
			if (hasInstructionalMethod()) {
				XOffering offering = server.getOffering(course.getOfferingId());
				if (offering != null)
					for (XConfig config: offering.getConfigs()) {
						if (matchesInstructionalMethod(config.getInstructionalMethod())) return true;	
					}
			}
			return false;
		} else {
			if (hasCourseName() && !matchesCourseName(course.getCourseName())) return false;
			if (hasCourseType() && !matchesCourseType(course.getType())) return false;
			if (hasInstructionalMethod()) {
				XOffering offering = server.getOffering(course.getOfferingId());
				boolean hasMatchingConfig = false;
				if (offering != null)
					for (XConfig config: offering.getConfigs()) {
						if (matchesInstructionalMethod(config.getInstructionalMethod())) {
							hasMatchingConfig = true;	
							break;
						}
					}
				if (!hasMatchingConfig) return false;
			}
			return true;
		}
	}
	
	public boolean matchesCourse(CourseOffering course) {
		if (isDisjunctive()) {
			if (hasCourseName() && matchesCourseName(course.getCourseName())) return true;
			if (hasCourseType() && matchesCourseType(course.getCourseType())) return true;
			if (hasInstructionalMethod()) {
				for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
					if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) return true;	
				}
			}
			return false;
		} else {
			if (hasCourseName() && !matchesCourseName(course.getCourseName())) return false;
			if (hasCourseType() && !matchesCourseType(course.getCourseType())) return false;
			if (hasInstructionalMethod()) {
				boolean hasMatchingConfig = false;
				for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
					if (matchesInstructionalMethod(config.getEffectiveInstructionalMethod())) {
							hasMatchingConfig = true;	
							break;
						}
					}
				if (!hasMatchingConfig) return false;
			}
			return true;
		}
	}
	
	transient Query iQuery = null;
	@Transient
	public Query getStudentQuery() {
		if (iQuery == null)
			iQuery = new Query(getStudentFilter());
		return iQuery;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(iUniqueId);
		out.writeObject(iRuleName);
		out.writeObject(iStudentFilter);
		out.writeObject(iInstructonalMethod);
		out.writeObject(iCourseName);
		out.writeObject(iCourseType);
		out.writeObject(iDisjunctive);
		out.writeObject(iAppliesToFilter);
		out.writeObject(iAppliesToOnline);
		out.writeObject(iAppliesToBatch);
		out.writeObject(iAdminOverride);
		out.writeObject(iAdvisorOverride);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = (Long)in.readObject();
		iRuleName = (String)in.readObject();
		iStudentFilter = (String)in.readObject();
		iInstructonalMethod = (String)in.readObject();
		iCourseName = (String)in.readObject();
		iCourseType = (String)in.readObject();
		iDisjunctive = (Boolean)in.readObject();
		iAppliesToFilter = (Boolean)in.readObject();
		iAppliesToOnline = (Boolean)in.readObject();
		iAppliesToBatch = (Boolean)in.readObject();
		iAdminOverride = (Boolean)in.readObject();
		iAdvisorOverride = (Boolean)in.readObject();
	}
}
