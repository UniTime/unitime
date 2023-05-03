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


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.Section;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.StudentSectioningStatus.Option;
import org.unitime.timetable.model.base.BaseCourseDemand;
import org.unitime.timetable.model.dao.CourseDemandDAO;

/**
 * @author Tomas Muller
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "course_demand")
public class CourseDemand extends BaseCourseDemand implements Comparable {
	private static final long serialVersionUID = 1L;
	
	public static enum Critical {
		NORMAL(RequestPriority.Normal),
		CRITICAL(RequestPriority.Critical),
		IMPORTANT(RequestPriority.Important),
		VITAL(RequestPriority.Vital),
		LC(RequestPriority.LC),
		;
		
		RequestPriority iPriority;
		Critical(RequestPriority rp) {
			iPriority = rp;
		}
		
		public RequestPriority toRequestPriority() { return iPriority; }
		public static Critical fromRequestPriority(RequestPriority rp) {
			if (rp == null) return Critical.NORMAL;
			for (Critical c: Critical.values())
				if (c.toRequestPriority() == rp) return c;
			return Critical.NORMAL;
		}
		public static Critical fromText(String text) {
			if ("Critical".equalsIgnoreCase(text))
				return Critical.CRITICAL;
			else if ("Important".equalsIgnoreCase(text))
				return Critical.IMPORTANT;
			else if ("Vital".equalsIgnoreCase(text))
				return Critical.VITAL;
			else if ("LC".equalsIgnoreCase(text))
				return Critical.LC;
			else
				return Critical.NORMAL;
		}
	}

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseDemand () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseDemand (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public int compareTo(Object o) {
        if (o==null || !(o instanceof CourseDemand)) return -1;
        CourseDemand cd = (CourseDemand)o;
        int cmp = (isAlternative().booleanValue() == cd.isAlternative().booleanValue() ? 0 : isAlternative().booleanValue() ? 1 : -1);
        if (cmp!=0) return cmp;
        cmp = getPriority().compareTo(cd.getPriority());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? Long.valueOf(-1) : getUniqueId()).compareTo(cd.getUniqueId() == null ? -1 : cd.getUniqueId());
    }
    
    public static List findAll(Long sessionId) {
    	return findAll(CourseDemandDAO.getInstance().getSession(), sessionId);
    }
    
    public static List<CourseDemand> findAll(org.hibernate.Session hibSession, Long sessionId) {
        return hibSession.
            createQuery("select c from CourseDemand c where c.student.session.uniqueId=:sessionId", CourseDemand.class).
            setParameter("sessionId", sessionId.longValue()).
            list(); 
    }
    
    public void updatePreferences(org.cpsolver.studentsct.model.CourseRequest request, org.hibernate.Session hibSession) {
    	if (getCourseRequests() == null || getCourseRequests().isEmpty()) return;
    	if (!request.getSelectedChoices().isEmpty() || !request.getRequiredChoices().isEmpty()) {
        	for (Course course: request.getCourses()) {
        		RequestedCourse rc = new RequestedCourse();
            	Set<Long> im = new HashSet<Long>();
            	for (Choice choice: request.getSelectedChoices()) {
            		if (!course.getOffering().equals(choice.getOffering())) continue;
            		if (choice.getSectionId() != null) {
            			Section section = choice.getOffering().getSection(choice.getSectionId());
            			if (section != null)
            				rc.setSelectedClass(section.getId(), section.getName(course.getId()), false, true); 
            		} else if (choice.getConfigId() != null) {
            			for (Config config: choice.getOffering().getConfigs()) {
            				if (choice.getConfigId().equals(config.getId()) && config.getInstructionalMethodId() != null && im.add(config.getInstructionalMethodId())) {
            					rc.setSelectedIntructionalMethod(config.getInstructionalMethodId(), config.getInstructionalMethodName(), false, true);
            				}
            			}
            		}
            	}
            	for (Choice choice: request.getRequiredChoices()) {
            		if (!course.getOffering().equals(choice.getOffering())) continue;
            		if (choice.getSectionId() != null) {
            			Section section = choice.getOffering().getSection(choice.getSectionId());
            			if (section != null)
            				rc.setSelectedClass(section.getId(), section.getName(course.getId()), true, true);
            		} else if (choice.getConfigId() != null) {
            			for (Config config: choice.getOffering().getConfigs()) {
            				if (choice.getConfigId().equals(config.getId()) && config.getInstructionalMethodId() != null && im.add(config.getInstructionalMethodId())) {
            					rc.setSelectedIntructionalMethod(config.getInstructionalMethodId(), config.getInstructionalMethodName(), true, true);
            				}
            			}
            		}
            	}
            	for (CourseRequest cr: getCourseRequests())
            		if (cr.getCourseOffering().getUniqueId().equals(course.getId()))
            			cr.updatePreferences(rc, hibSession);
        	}
    	}
    }
    
	@Transient
    public CourseOffering getFirstChoiceCourseOffering() {
    	CourseRequest ret = null;
    	for (CourseRequest cr: getCourseRequests()) {
    		if (ret == null || cr.getOrder() < ret.getOrder()) ret = cr;
    	}
    	return (ret == null ? null : ret.getCourseOffering());
    }
    
	@Transient
    public Critical getEffectiveCritical() {
    	if (getCriticalOverride() != null)
    		return Critical.values()[getCriticalOverride()];
    	if (getCritical() != null)
    		return Critical.values()[getCritical()];
    	return Critical.NORMAL;
    }
    
	@Transient
    public boolean isCriticalOrImportant() {
    	switch (getEffectiveCritical()) {
    	case CRITICAL:
    		return true;
    	case IMPORTANT:
    		return true;
    	case VITAL:
    		return true;
    	default:
    		return false;
    	}
    }
    
    public boolean effectiveNoSub() {
    	if (getNoSub() == null) return false;
    	if (getNoSub()) {
        	StudentSectioningStatus status = getStudent().getEffectiveStatus();
        	return (status == null || status.hasOption(Option.nosubs));
    	}
    	return false;
    }
    
    public boolean effectiveWaitList() {
    	if (getWaitlist() == null) return false;
    	if (getWaitlist()) {
        	StudentSectioningStatus status = getStudent().getEffectiveStatus();
        	if (status == null || status.hasOption(Option.waitlist)) {
        		CourseRequest firstRequest = null;
        		if (getCourseRequests() != null)
        			for (CourseRequest cr: getCourseRequests()) {
        				if (firstRequest == null || firstRequest.getOrder() > cr.getOrder())
        					firstRequest = cr;
        			}
        		return firstRequest != null && firstRequest.getCourseOffering().getInstructionalOffering().effectiveWaitList();
        	}
    	}
    	return false;
    }
    
    public boolean isEnrolled(boolean checkSectionSwap) {
    	for (CourseRequest cr: getCourseRequests())
        	for (StudentClassEnrollment e: getStudent().getClassEnrollments())
    			if (cr.getCourseOffering().equals(e.getCourseOffering())) {
    				if (checkSectionSwap && e.getCourseOffering().equals(cr.getCourseDemand().getWaitListSwapWithCourseOffering()) && !cr.isRequired())
    					return false; // section swap which requirements have not been met -> considered not wait-listed
    				return true;
    			}
    	return false;
    }
    
	@Transient
    public CourseOffering getEnrolledCourse() {
    	for (CourseRequest cr: getCourseRequests())
        	for (StudentClassEnrollment e: getStudent().getClassEnrollments())
    			if (cr.getCourseOffering().equals(e.getCourseOffering())) return cr.getCourseOffering();
    	return null;
    }
    
	@Transient
    public boolean isEnrolledExceptForWaitListSwap() {
    	for (CourseRequest cr: getCourseRequests()) {
    		if (cr.getCourseOffering().equals(getWaitListSwapWithCourseOffering())) continue;
        	for (StudentClassEnrollment e: getStudent().getClassEnrollments())
    			if (cr.getCourseOffering().equals(e.getCourseOffering())) return true;
    	}
    	return false;
    }
    
    public boolean isWaitListOrNoSub(WaitListMode wlMode) {
    	if (wlMode == WaitListMode.WaitList) {
    		return getWaitlist() != null && getWaitlist().booleanValue();
    	} else if (wlMode == WaitListMode.NoSubs) {
    		return getNoSub() != null && getNoSub().booleanValue();
    	} else {
    		return false;
    	}
    }
    
    public CourseRequest getCourseRequest(Long courseOfferingId) {
    	if (courseOfferingId == null || getCourseRequests() == null || getCourseRequests().isEmpty()) return null;
    	for (CourseRequest cr: getCourseRequests())
    		if (cr.getCourseOffering().getUniqueId().equals(courseOfferingId)) return cr;
    	return null;
    }
}
