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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.cpsolver.studentsct.model.AcademicAreaCode;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;

import org.infinispan.marshall.Externalizer;
import org.infinispan.marshall.SerializeWith;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
@SerializeWith(XStudent.XStudentSerializer.class)
public class XStudent extends XStudentId implements Externalizable {
	private static final long serialVersionUID = 1L;
    private List<XAcademicAreaCode> iAcadAreaClassifs = new ArrayList<XAcademicAreaCode>();
    private List<XAcademicAreaCode> iMajors = new ArrayList<XAcademicAreaCode>();
    private List<String> iGroups = new ArrayList<String>();
    private List<String> iAccomodations = new ArrayList<String>();
    private List<XRequest> iRequests = new ArrayList<XRequest>();
    private String iStatus = null;
    private String iEmail = null;
    private Date iEmailTimeStamp = null;

    public XStudent() {
    	super();
    }
    
    public XStudent(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }

    public XStudent(Student student, OnlineSectioningHelper helper, BitSet freeTimePattern) {
    	super(student, helper);
    	iStatus = student.getSectioningStatus() == null ? null : student.getSectioningStatus().getReference();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getScheduleEmailedDate() == null ? null : student.getScheduleEmailedDate();
        for (AcademicAreaClassification aac: student.getAcademicAreaClassifications()) {
        	iAcadAreaClassifs.add(new XAcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(), aac.getAcademicClassification().getCode()));
            for (PosMajor major: aac.getAcademicArea().getPosMajors())
            	if (student.getPosMajors().contains(major))
            		iMajors.add(new XAcademicAreaCode(aac.getAcademicArea().getAcademicAreaAbbreviation(), major.getCode()));
        }
        for (StudentGroup group: student.getGroups())
        	iGroups.add(group.getGroupAbbreviation());
        for (StudentAccomodation accomodation: student.getAccomodations())
        	iAccomodations.add(accomodation.getAbbreviation());
        
		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		demands.addAll(student.getCourseDemands());
        for (CourseDemand cd: demands) {
            if (cd.getFreeTime() != null) {
            	iRequests.add(new XFreeTimeRequest(cd, freeTimePattern));
            } else if (!cd.getCourseRequests().isEmpty()) {
            	iRequests.add(new XCourseRequest(cd, helper));
            }
        }
        
        Map<CourseOffering, List<StudentClassEnrollment>> unmatchedCourses = new HashMap<CourseOffering, List<StudentClassEnrollment>>();
        for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
        	if (getRequestForCourse(enrollment.getCourseOffering().getUniqueId()) != null) continue;
        	List<StudentClassEnrollment> classes = unmatchedCourses.get(enrollment.getCourseOffering());
        	if (classes == null) {
        		classes = new ArrayList<StudentClassEnrollment>();
        		unmatchedCourses.put(enrollment.getCourseOffering(), classes);
        	}
        	classes.add(enrollment);
        }
        if (!unmatchedCourses.isEmpty()) {
        	int priority = 0;
        	for (XRequest request: iRequests)
        		if (!request.isAlternative() && request.getPriority() > priority) priority = request.getPriority();
            for (CourseOffering course: new TreeSet<CourseOffering>(unmatchedCourses.keySet())) {
            	List<StudentClassEnrollment> classes = unmatchedCourses.get(course);
            	iRequests.add(new XCourseRequest(student, course, ++priority, helper, classes));
            }
        }
        
        Collections.sort(iRequests);
        
    }
    
    public XStudent(XStudent student) {
    	super(student);
    	iStatus = student.getStatus();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getEmailTimeStamp();
    	iAcadAreaClassifs.addAll(student.getAcademicAreaClasiffications());
    	iMajors.addAll(student.getMajors());
    	iGroups.addAll(student.getGroups());
    	iAccomodations.addAll(student.getAccomodations());
    	iRequests.addAll(student.getRequests());
    }
    
    public XStudent(XStudent student, Collection<CourseDemand> demands, OnlineSectioningHelper helper, BitSet freeTimePattern) {
    	super(student);
    	iStatus = student.getStatus();
    	iEmail = student.getEmail();
    	iEmailTimeStamp = student.getEmailTimeStamp();
    	iAcadAreaClassifs.addAll(student.getAcademicAreaClasiffications());
    	iMajors.addAll(student.getMajors());
    	iGroups.addAll(student.getGroups());
    	iAccomodations.addAll(student.getAccomodations());

    	if (demands != null)
        	for (CourseDemand cd: demands) {
                if (cd.getFreeTime() != null) {
                	iRequests.add(new XFreeTimeRequest(cd, freeTimePattern));
                } else if (!cd.getCourseRequests().isEmpty()) {
                	iRequests.add(new XCourseRequest(cd, helper));
                }
            }
    	
    	Collections.sort(iRequests);
    }
    
    public static List<XRequest> loadRequests(Student student, OnlineSectioningHelper helper, BitSet freeTimePattern) {
    	List<XRequest> requests = new ArrayList<XRequest>();
		TreeSet<CourseDemand> demands = new TreeSet<CourseDemand>(new Comparator<CourseDemand>() {
			public int compare(CourseDemand d1, CourseDemand d2) {
				if (d1.isAlternative() && !d2.isAlternative()) return 1;
				if (!d1.isAlternative() && d2.isAlternative()) return -1;
				int cmp = d1.getPriority().compareTo(d2.getPriority());
				if (cmp != 0) return cmp;
				return d1.getUniqueId().compareTo(d2.getUniqueId());
			}
		});
		demands.addAll(student.getCourseDemands());
    	for (CourseDemand cd: demands) {
            if (cd.getFreeTime() != null) {
            	requests.add(new XFreeTimeRequest(cd, freeTimePattern));
            } else if (!cd.getCourseRequests().isEmpty()) {
            	requests.add(new XCourseRequest(cd, helper));
            }
        }
        Collections.sort(requests);
        return requests;
    }
    
    public XStudent(net.sf.cpsolver.studentsct.model.Student student) {
    	super(student);
    	iStatus = student.getStatus();
    	iEmailTimeStamp = (student.getEmailTimeStamp() == null ? null : new Date(student.getEmailTimeStamp()));
    	for (AcademicAreaCode aac: student.getAcademicAreaClasiffications())
    		iAcadAreaClassifs.add(new XAcademicAreaCode(aac.getArea(), aac.getCode()));
    	for (AcademicAreaCode aac: student.getMajors())
    		iMajors.add(new XAcademicAreaCode(aac.getArea(), aac.getCode()));
    	for (AcademicAreaCode aac: student.getMinors()) {
    		if ("A".equals(aac.getArea()))
				iAccomodations.add(aac.getCode());
			else
				iGroups.add(aac.getCode());
    	}
    	for (Request request: student.getRequests()) {
    		if (request instanceof FreeTimeRequest) {
    			iRequests.add(new XFreeTimeRequest((FreeTimeRequest)request));
    		} else if (request instanceof CourseRequest) {
    			iRequests.add(new XCourseRequest((CourseRequest)request));
    		}
    	}
    }

    public XCourseRequest getRequestForCourse(Long courseId) {
    	for (XRequest request: iRequests)
    		if (request instanceof XCourseRequest && ((XCourseRequest)request).hasCourse(courseId))
    			return (XCourseRequest)request;
    	return null;
    }

    /**
     * List of academic area - classification codes ({@link AcademicAreaCode})
     * for the given student
     */
    public List<XAcademicAreaCode> getAcademicAreaClasiffications() {
        return iAcadAreaClassifs;
    }

    /**
     * List of major codes ({@link AcademicAreaCode}) for the given student
     */
    public List<XAcademicAreaCode> getMajors() {
        return iMajors;
    }

    /**
     * List of group codes for the given student
     */
    public List<String> getGroups() {
        return iGroups;
    }

    /**
     * List of group codes for the given student
     */
    public List<String> getAccomodations() {
        return iAccomodations;
    }
        
    /**
     * Get student status (online sectioning only)
     */
    public String getStatus() { return iStatus; }
    /**
     * Set student status
     */
    public void setStatus(String status) { iStatus = status; }
    
    /**
     * Get last email time stamp (online sectioning only)
     */
    public Date getEmailTimeStamp() { return iEmailTimeStamp; }
    /**
     * Set last email time stamp
     */
    public void setEmailTimeStamp(Date emailTimeStamp) { iEmailTimeStamp = emailTimeStamp; }
    
    public List<XRequest> getRequests() { return iRequests; }
    
    public String getEmail() { return iEmail; }
    
    /**
     * True if the given request can be assigned to the student. A request
     * cannot be assigned to a student when the student already has the desired
     * number of requests assigned (i.e., number of non-alternative course
     * requests).
     **/
    public boolean canAssign(XCourseRequest request) {
        if (request.getEnrollment() != null)
            return true;
        int alt = 0;
        boolean found = false;
        for (XRequest r : iRequests) {
            if (r.equals(request)) found = true;
            boolean course = (r instanceof XCourseRequest);
            boolean assigned = (!course || ((XCourseRequest)r).getEnrollment() != null || r.equals(request));
            boolean waitlist = (course && ((XCourseRequest)r).isWaitlist());
            if (r.isAlternative()) {
                if (assigned || (!found && waitlist))
                    alt--;
            } else {
                if (course && !waitlist && !assigned)
                    alt++;
            }
        }
        return (alt >= 0);
    }
    
    @Override
    public String toString() {
    	return getName() + " (" + getExternalId() + ")";
    }

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		
		int nrAcadAreClassifs = in.readInt();
		iAcadAreaClassifs.clear();
		for (int i = 0; i < nrAcadAreClassifs; i++)
			iAcadAreaClassifs.add(new XAcademicAreaCode(in));
		
		int nrMajors = in.readInt();
		iMajors.clear();
		for (int i = 0; i < nrMajors; i++)
			iMajors.add(new XAcademicAreaCode(in));
		
		int nrGroups = in.readInt();
		iGroups.clear();
		for (int i = 0; i < nrGroups; i++)
			iGroups.add((String)in.readObject());
		
		int nrAccomodations = in.readInt();
		iAccomodations.clear();
		for (int i = 0; i < nrAccomodations; i++)
			iAccomodations.add((String)in.readObject());
		
		int nrRequests = in.readInt();
		iRequests.clear();
		for (int i = 0; i < nrRequests; i++)
			iRequests.add(in.readBoolean() ? new XCourseRequest(in) : new XFreeTimeRequest(in));
		
		iStatus = (String)in.readObject();
		iEmail = (String)in.readObject();
		iEmailTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iAcadAreaClassifs.size());
		for (XAcademicAreaCode aac: iAcadAreaClassifs)
			aac.writeExternal(out);
		
		out.writeInt(iMajors.size());
		for (XAcademicAreaCode major: iMajors)
			major.writeExternal(out);
		
		out.writeInt(iGroups.size());
		for (String group: iGroups)
			out.writeObject(group);
		
		out.writeInt(iAccomodations.size());
		for (String accomodation: iAccomodations)
			out.writeObject(accomodation);
		
		out.writeInt(iRequests.size());
		for (XRequest request: iRequests)
			if (request instanceof XCourseRequest) {
				out.writeBoolean(true);
				((XCourseRequest)request).writeExternal(out);
			} else {
				out.writeBoolean(false);
				((XFreeTimeRequest)request).writeExternal(out);
			}
		
		out.writeObject(iStatus);
		out.writeObject(iEmail);
		
		out.writeBoolean(iEmailTimeStamp != null);
		if (iEmailTimeStamp != null)
			out.writeLong(iEmailTimeStamp.getTime());
	}
	
	public static class XStudentSerializer implements Externalizer<XStudent> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XStudent object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XStudent readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XStudent(input);
		}
	}
}