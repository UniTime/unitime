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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Enrollment;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCourseRequest.XCourseRequestSerializer.class)
public class XCourseRequest extends XRequest {
	private static final long serialVersionUID = 1L;
	private List<XCourseId> iCourseIds = new ArrayList<XCourseId>();
    private boolean iWaitlist = false;
    private Date iTimeStamp = null;
    private XEnrollment iEnrollment = null;
    private Map<XCourseId, List<XWaitListedSection>> iSectionWaitlist = null;
    private Map<XCourseId, byte[]> iOptions = null;
    private String iMessage = null;

    public XCourseRequest() {}
    
    public XCourseRequest(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
    
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
            		if (iSectionWaitlist == null) iSectionWaitlist = new HashMap<XCourseId, List<XWaitListedSection>>();
            		List<XWaitListedSection> sections = iSectionWaitlist.get(courseId);
            		if (sections == null) {
            			sections = new ArrayList<XWaitListedSection>();
            			iSectionWaitlist.put(courseId, sections);
            		}
            		sections.add(new XWaitListedSection(cwl, helper));
            	}
            }
        	for (CourseRequestOption option: cr.getCourseRequestOptions()) {
        		if (OnlineSectioningLog.CourseRequestOption.OptionType.ORIGINAL_ENROLLMENT.getNumber() == option.getOptionType()) {
        			if (iOptions == null) iOptions = new HashMap<XCourseId, byte[]>();
        			iOptions.put(courseId, option.getValue());
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
        if (demand.getEnrollmentMessages() != null) {
        	StudentEnrollmentMessage message = null;
        	for (StudentEnrollmentMessage m: demand.getEnrollmentMessages()) {
        		if (message == null || message.getOrder() < m.getOrder() || (message.getOrder() == m.getOrder() && message.getTimestamp().before(m.getTimestamp()))) {
        			message = m;
        		}
        	}
        	if (message != null)
        		iMessage = message.getMessage();
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
    
    public XCourseRequest(org.cpsolver.studentsct.model.CourseRequest request, Enrollment enrollment) {
    	super(request);
    	for (Course course: request.getCourses())
    		iCourseIds.add(new XCourseId(course));
    	iWaitlist = request.isWaitlist();
    	iTimeStamp = request.getTimeStamp() == null ? null : new Date(request.getTimeStamp());
    	iEnrollment = enrollment == null ? null : new XEnrollment(enrollment);
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
    
    public Integer getEnrolledCourseIndex() {
    	if (iEnrollment == null) return null;
    	for (int i = 0; i < iCourseIds.size(); i++)
    		if (iCourseIds.get(i).getCourseId().equals(iEnrollment.getCourseId())) return i;
    	return -1;
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
    	List<XWaitListedSection> sections = getSectionWaitlist(courseId);
    	return sections != null && !sections.isEmpty();
    }
    
    public List<XWaitListedSection> getSectionWaitlist(XCourseId courseId) {
    	return (iSectionWaitlist == null ? null : iSectionWaitlist.get(courseId));
    }
    
    public OnlineSectioningLog.CourseRequestOption getOptions(Long offeringId) {
    	if (iOptions == null) return null;
    	XCourseId courseId = getCourseIdByOfferingId(offeringId);
    	if (courseId == null) return null;
    	byte[] option = iOptions.get(courseId);
    	if (option != null) {
    		try {
    			return OnlineSectioningLog.CourseRequestOption.parseFrom(option);
    		} catch (InvalidProtocolBufferException e) {}    		
    	}
    	return null;
    }
    
    public void fillChoicesIn(org.cpsolver.studentsct.model.CourseRequest request) {
    	if (iSectionWaitlist != null)
    		for (Map.Entry<XCourseId, List<XWaitListedSection>> entry: iSectionWaitlist.entrySet()) {
    			Course course = request.getCourse(entry.getKey().getCourseId());
    			if (course != null)
    				for (XSection section: entry.getValue())
                        request.getSelectedChoices().add(new Choice(course.getOffering(), section.getInstructionalType(), section.getTime() == null || section.getTime().getDays() == 0 ? null : section.getTime().toTimeLocation(), section.getInstructorIds(), section.getInstructorNames()));
    		}
    }
    
    public String getEnrollmentMessage() { return iMessage; }
    
    public void setEnrollmentMessage(String message) { iMessage = message; }
    
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
    	ret += " (" + getRequestId() + ")";
    	return ret;
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	
    	int nrCourses = in.readInt();
    	iCourseIds.clear();
    	for (int i = 0; i < nrCourses; i++)
    		iCourseIds.add(new XCourseId(in));
    	
    	iWaitlist = in.readBoolean();
    	iTimeStamp = (in.readBoolean() ? new Date(in.readLong()) : null);
    	iEnrollment = (in.readBoolean() ? new XEnrollment(in) : null);
    	
    	int nrWaitlists = in.readInt();
    	if (nrWaitlists == 0)
    		iSectionWaitlist = null;
    	else {
    		iSectionWaitlist = new HashMap<XCourseId, List<XWaitListedSection>>();
    		for (int i = 0; i < nrWaitlists; i++) {
    			Long courseId = in.readLong();
				int nrSections = in.readInt();
				List<XWaitListedSection> sections = new ArrayList<XWaitListedSection>(nrSections);
				for (int j = 0; j < nrSections; j++)
					sections.add(new XWaitListedSection(in));
				for (XCourseId course: iCourseIds)
    				if (course.getCourseId().equals(courseId)) {
    					iSectionWaitlist.put(course, sections); break;
    				}
    		}
    	}
    	
        int nrOptions = in.readInt();
        if (nrOptions == 0)
        	iOptions = null;
        else {
        	iOptions = new HashMap<XCourseId, byte[]>();
        	for (int i = 0; i < nrOptions; i++) {
        		Long courseId = in.readLong();
        		byte[] data = new byte[in.readInt()];
        		in.read(data);
				for (XCourseId course: iCourseIds)
    				if (course.getCourseId().equals(courseId)) {
    					iOptions.put(course, data);
    					break;
    				}
        	}
        }
        
        iMessage = (String)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iCourseIds.size());
		for (XCourseId course: iCourseIds)
			course.writeExternal(out);
		
		out.writeBoolean(iWaitlist);
		
		out.writeBoolean(iTimeStamp != null);
		if (iTimeStamp != null)
			out.writeLong(iTimeStamp.getTime());
		
		out.writeBoolean(iEnrollment != null);
		if (iEnrollment != null)
			iEnrollment.writeExternal(out);

		out.writeInt(iSectionWaitlist == null ? 0 : iSectionWaitlist.size());
		if (iSectionWaitlist != null)
			for (Map.Entry<XCourseId, List<XWaitListedSection>> entry: iSectionWaitlist.entrySet()) {
				out.writeLong(entry.getKey().getCourseId());
				out.writeInt(entry.getValue().size());
				for (XWaitListedSection section: entry.getValue()) {
					section.writeExternal(out);
				}
			}
		
		out.writeInt(iOptions == null ? 0 : iOptions.size());
		if (iOptions != null)
			for (Map.Entry<XCourseId, byte[]> entry: iOptions.entrySet()) {
				out.writeLong(entry.getKey().getCourseId());
				byte[] value = entry.getValue();
				out.writeInt(value.length);
				out.write(value);
			}
		
		out.writeObject(iMessage);
	}
	
	public static class XCourseRequestSerializer implements Externalizer<XCourseRequest> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCourseRequest object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCourseRequest readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCourseRequest(input);
		}
	}
}
