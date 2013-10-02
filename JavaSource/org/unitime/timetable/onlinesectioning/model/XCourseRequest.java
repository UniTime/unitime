/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.cpsolver.studentsct.model.Choice;
import net.sf.cpsolver.studentsct.model.Course;

import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

public class XCourseRequest extends XRequest {
	private static final long serialVersionUID = 1L;
	private List<XCourseId> iCourseIds = new ArrayList<XCourseId>();
    private boolean iWaitlist = false;
    private Date iTimeStamp = null;
    private XEnrollment iEnrollment = null;
    private Map<XCourseId, List<XSection>> iSectionWaitlist = null;

    public XCourseRequest() {}
    
    public XCourseRequest(CourseDemand demand, OnlineSectioningHelper helper) {
    	super(demand);
        TreeSet<CourseRequest> crs = new TreeSet<CourseRequest>(new Comparator<CourseRequest>() {
        	public int compare(CourseRequest r1, CourseRequest r2) {
        		return r1.getOrder().compareTo(r2.getOrder());
        	}
		});
        crs.addAll(demand.getCourseRequests());
        for (CourseRequest cr: crs) {
        	XCourseId courseId = new XCourseId(cr.getCourseOffering());
        	iCourseIds.add(courseId);
        	if (cr.getClassWaitLists() != null) {
            	for (ClassWaitList cwl: cr.getClassWaitLists()) {
            		if (iSectionWaitlist == null) iSectionWaitlist = new HashMap<XCourseId, List<XSection>>();
            		List<XSection> sections = iSectionWaitlist.get(courseId);
            		if (sections == null) {
            			sections = new ArrayList<XSection>();
            			iSectionWaitlist.put(courseId, sections);
            		}
            		sections.add(new XSection(cwl.getClazz(), helper));
            	}
            }
        }
        iWaitlist = (demand.isWaitlist() != null && demand.isWaitlist());
        iTimeStamp = (demand.getTimestamp() == null ? new Date() : demand.getTimestamp());
        for (CourseRequest cr: crs) {
    		List<StudentClassEnrollment> enrl = cr.getClassEnrollments();
    		if (!enrl.isEmpty()) {
    			iEnrollment = new XEnrollment(demand.getStudent(), cr.getCourseOffering(), helper, enrl);
    			break;
    		}
        }
    }
    
    public XCourseRequest(Student student, CourseOffering course, int priority, OnlineSectioningHelper helper, Collection<StudentClassEnrollment> classes) {
    	super();
    	iStudentId = student.getUniqueId();
    	iRequestId = -course.getUniqueId();
    	iAlternative = false;
    	iPriority = priority;
    	iCourseIds.add(new XCourseId(course));
        iWaitlist = false;
        if (classes != null && !classes.isEmpty())
        	iEnrollment = new XEnrollment(student, course, helper, classes);
        if (iEnrollment != null)
        	iTimeStamp = iEnrollment.getTimeStamp();
        else
        	iTimeStamp = new Date();
    }
    
    public XCourseRequest(net.sf.cpsolver.studentsct.model.CourseRequest request) {
    	super(request);
    	for (Course course: request.getCourses())
    		iCourseIds.add(new XCourseId(course));
    	iWaitlist = request.isWaitlist();
    	iTimeStamp = request.getTimeStamp() == null ? null : new Date(request.getTimeStamp());
    	iEnrollment = request.getAssignment() == null ? null : new XEnrollment(request.getAssignment());
    }

    /**
     * List of requested courses (in the correct order -- first is the requested
     * course, second is the first alternative, etc.)
     */
    public List<XCourseId> getCourseIds() {
        return iCourseIds;
    }
    
    public boolean hasCourse(Long courseId) {
    	for (XCourseId id: iCourseIds)
    		if (id.getCourseId().equals(courseId)) return true;
    	return false;
    }
    
    public XCourseId getCourseIdByOfferingId(Long offeringId) {
    	for (XCourseId id: iCourseIds)
    		if (id.getOfferingId().equals(offeringId)) return id;
    	return null;
    }

    /**
     * True if the student can be put on a wait-list (no alternative course
     * request will be given instead)
     */
    public boolean isWaitlist() {
        return iWaitlist;
    }
        
    /**
     * Time stamp of the request
     */
    public Date getTimeStamp() {
        return iTimeStamp;
    }
    
    /** Return enrollment, if enrolled */
    public XEnrollment getEnrollment() { return iEnrollment; }

    public void setEnrollment(XEnrollment enrollment) { iEnrollment = enrollment; }
    
    public void setWaitlist(boolean waitlist) { iWaitlist = waitlist; }
    
    public boolean hasSectionWaitlist(XCourseId courseId) {
    	List<XSection> sections = getSectionWaitlist(courseId);
    	return sections != null && !sections.isEmpty();
    }
    
    public List<XSection> getSectionWaitlist(XCourseId courseId) {
    	return iSectionWaitlist == null ? null : iSectionWaitlist.get(courseId);
    }
    
    public void fillChoicesIn(net.sf.cpsolver.studentsct.model.CourseRequest request) {
    	if (iSectionWaitlist != null)
    		for (Map.Entry<XCourseId, List<XSection>> entry: iSectionWaitlist.entrySet()) {
    			Course course = request.getCourse(entry.getKey().getCourseId());
    			if (course != null)
    				for (XSection section: entry.getValue()) {
                        String instructorIds = "";
                        String instructorNames = "";
                        for (XInstructor instructor: section.getInstructors()) {
                        	if (!instructorIds.isEmpty()) {
                        		instructorIds += ":"; instructorNames += ":";
                        	}
                        	instructorIds += instructor.getIntructorId().toString();
                        	instructorNames += instructor.getName() + "|"  + (instructor.getEmail() == null ? "" : instructor.getEmail());
                        }
                        request.getSelectedChoices().add(new Choice(course.getOffering(), section.getInstructionalType(), section.getTime() == null ? null : section.getTime().toTimeLocation(), instructorIds, instructorNames));
    				}
    		}
    }
    
    @Override
    public String toString() {
    	String ret = super.toString();
    	for (Iterator<XCourseId> i = iCourseIds.iterator(); i.hasNext();) {
    		XCourseId c = i.next();
    		ret += " " + c.getCourseName();
    		if (i.hasNext()) ret += ",";
    	}
    	if (isWaitlist())
    		ret += " (w)";
    	return ret;
    }
}
