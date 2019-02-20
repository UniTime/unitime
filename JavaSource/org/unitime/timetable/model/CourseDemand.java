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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Section;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.base.BaseCourseDemand;
import org.unitime.timetable.model.dao.CourseDemandDAO;

/**
 * @author Tomas Muller
 */
public class CourseDemand extends BaseCourseDemand implements Comparable {
	private static final long serialVersionUID = 1L;

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
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cd.getUniqueId() == null ? -1 : cd.getUniqueId());
    }
    
    public static List findAll(Long sessionId) {
    	return findAll(CourseDemandDAO.getInstance().getSession(), sessionId);
    }
    
    public static List findAll(org.hibernate.Session hibSession, Long sessionId) {
        return hibSession.
            createQuery("select c from CourseDemand c where c.student.session.uniqueId=:sessionId").
            setLong("sessionId", sessionId.longValue()).
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
}
