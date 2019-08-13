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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Section;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.ClassWaitList;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentClassPref;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentInstrMthPref;
import org.unitime.timetable.model.StudentSectioningPref;
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
    private Map<XCourseId, List<XPreference>> iPreferences = null;
    private String iMessage = null;
    private Map<XCourseId, XOverride> iOverrides = null;
    private boolean iCritical = false;

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
        	if (cr.getOverrideExternalId() != null) {
        		if (iOverrides == null) iOverrides = new HashMap<XCourseId, XOverride>();
        		iOverrides.put(courseId, new XOverride(cr.getOverrideExternalId(), cr.getOverrideTimeStamp(), cr.getOverrideStatus()));
        	}
        	if (cr.getPreferences() != null && !cr.getPreferences().isEmpty()) {
        		List<XPreference> prefs = new ArrayList<XPreference>();
        		for (StudentSectioningPref p: cr.getPreferences())
        			prefs.add(new XPreference(cr, p));
        		if (iPreferences == null) iPreferences = new HashMap<XCourseId, List<XPreference>>();
        		iPreferences.put(courseId, prefs);
        	}
        }
        if (helper.isAlternativeCourseEnabled() && crs.size() == 1 && !demand.isAlternative()) {
        	CourseOffering co = crs.first().getCourseOffering();
        	CourseOffering alternative = co.getAlternativeOffering();
        	if (alternative != null) {
        		// Make sure that the alternative course is not already requested
            	for (CourseDemand d: demand.getStudent().getCourseDemands()) {
            		if (d.getFreeTime() != null) continue;
            		for (CourseRequest r: d.getCourseRequests()) {
            			if (alternative.equals(r.getCourseOffering())) { alternative = null; break; }
            			if (!d.isAlternative() && d.getPriority() < demand.getPriority() && d.getCourseRequests().size() == 1 && alternative.equals(r.getCourseOffering().getAlternativeOffering())) { alternative = null; break; }
            		}
            		if (alternative == null) break;
            	}
        	}
    		if (alternative != null) {
            	iCourseIds.add(new XCourseId(alternative));
            	List<StudentClassEnrollment> enrl = alternative.getClassEnrollments(demand.getStudent());
            	if (!enrl.isEmpty())
            		iEnrollment = new XEnrollment(demand.getStudent(), alternative, helper, enrl);
    		}
        }
        iWaitlist = (demand.isWaitlist() != null && demand.isWaitlist());
        if (demand.getCriticalOverride() != null)
        	iCritical = demand.getCriticalOverride();
        else
        	iCritical = (demand.isCritical() != null && demand.isCritical());
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
        iCritical = false;
        if (classes != null && !classes.isEmpty())
        	iEnrollment = new XEnrollment(student, course, helper, classes);
        if (iEnrollment != null)
        	iTimeStamp = iEnrollment.getTimeStamp();
        else
        	iTimeStamp = new Date();
    }
    
    public XCourseRequest(Student student, XCourseId course, int priority, XEnrollment enrollment) {
    	super();
    	iStudentId = student.getUniqueId();
    	iRequestId = -course.getCourseId();
    	iAlternative = false;
    	iPriority = priority;
    	iCourseIds.add(course);
        iWaitlist = false;
        iCritical = false;
        iEnrollment = enrollment;
        if (iEnrollment != null)
        	iTimeStamp = iEnrollment.getTimeStamp();
        else
        	iTimeStamp = new Date();
    }
    
    public XCourseRequest(XCourseRequest request, XEnrollment enrollment) {
    	super(request);
    	iCourseIds.addAll(request.getCourseIds());
    	iWaitlist = request.isWaitlist();
    	iCritical = request.isCritical();
    	iTimeStamp = request.getTimeStamp();
    	iEnrollment = enrollment;
    	if (request.iSectionWaitlist != null)
    		iSectionWaitlist = new HashMap<XCourseId, List<XWaitListedSection>>(request.iSectionWaitlist);
    	if (request.iOptions != null)
    		iOptions = new HashMap<XCourseId, byte[]>(request.iOptions);
    	if (request.iPreferences != null)
    		iPreferences = new HashMap<XCourseId, List<XPreference>>(request.iPreferences);
    	if (request.iOverrides != null)
    		iOverrides = new HashMap<XCourseId, XOverride>(request.iOverrides);
    	iMessage = request.getEnrollmentMessage();
    }
    
    public XCourseRequest(org.cpsolver.studentsct.model.CourseRequest request, Enrollment enrollment) {
    	super(request);
    	for (Course course: request.getCourses())
    		iCourseIds.add(new XCourseId(course));
    	iWaitlist = request.isWaitlist();
    	iCritical = request.isCritical();
    	iTimeStamp = request.getTimeStamp() == null ? null : new Date(request.getTimeStamp());
    	iEnrollment = enrollment == null ? null : new XEnrollment(enrollment);

    	if (!request.getSelectedChoices().isEmpty() || !request.getRequiredChoices().isEmpty()) {
        	for (Course course: request.getCourses()) {
        		List<XPreference> prefs = new ArrayList<XPreference>();
        		
            	Set<Long> im = new HashSet<Long>();
            	for (Choice choice: request.getSelectedChoices()) {
            		if (!course.getOffering().equals(choice.getOffering())) continue;
            		if (choice.getSectionId() != null) {
            			Section section = choice.getOffering().getSection(choice.getSectionId());
            			if (section != null)
            				prefs.add(new XPreference(section, course, false));
            		} else if (choice.getConfigId() != null) {
            			for (Config config: choice.getOffering().getConfigs()) {
            				if (choice.getConfigId().equals(config.getId()) && config.getInstructionalMethodId() != null && im.add(config.getInstructionalMethodId())) {
            					prefs.add(new XPreference(XPreferenceType.INSTR_METHOD, config.getInstructionalMethodId(), config.getInstructionalMethodName(), false));
            				}
            			}
            		}
            	}
            	for (Choice choice: request.getRequiredChoices()) {
            		if (!course.getOffering().equals(choice.getOffering())) continue;
            		if (choice.getSectionId() != null) {
            			Section section = choice.getOffering().getSection(choice.getSectionId());
            			if (section != null)
            				prefs.add(new XPreference(section, course, true));
            		} else if (choice.getConfigId() != null) {
            			for (Config config: choice.getOffering().getConfigs()) {
            				if (choice.getConfigId().equals(config.getId()) && config.getInstructionalMethodId() != null && im.add(config.getInstructionalMethodId())) {
            					prefs.add(new XPreference(XPreferenceType.INSTR_METHOD, config.getInstructionalMethodId(), config.getInstructionalMethodName(), true));
            				}
            			}
            		}
            	}
            	if (!prefs.isEmpty()) {
            		if (iPreferences == null) iPreferences = new HashMap<XCourseId, List<XPreference>>();
            		iPreferences.put(new XCourseId(course), prefs);
            	}
        	}
    	}
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
    
    public boolean hasCourseName(String course) {
    	for (XCourseId id: iCourseIds)
    		if (id.getCourseName().equals(course)) return true;
    	return false;
    }
    
    public XCourseId getCourseName(String course) {
    	for (XCourseId id: iCourseIds)
    		if (id.getCourseName().equals(course)) return id;
    	return null;
    }
    
    public boolean isPrimary(Long courseId) {
    	return !iCourseIds.isEmpty() && iCourseIds.get(0).getCourseId().equals(courseId);
    }
    
    public boolean isPrimary(XCourseId courseId) {
    	return !iCourseIds.isEmpty() && iCourseIds.get(0).equals(courseId);
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
    
    public boolean isCritical() {
    	return iCritical;
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
    
    public void setCritical(boolean critical) { iCritical = critical; }
    
    public boolean hasSectionWaitlist(XCourseId courseId) {
    	List<XWaitListedSection> sections = getSectionWaitlist(courseId);
    	return sections != null && !sections.isEmpty();
    }
    
    public List<XWaitListedSection> getSectionWaitlist(XCourseId courseId) {
    	return (iSectionWaitlist == null ? null : iSectionWaitlist.get(courseId));
    }
    
    public XOverride getOverride(XCourseId courseId) {
    	return (iOverrides == null ? null : iOverrides.get(courseId));
    }
    public void setOverride(XCourseId courseId, XOverride override) {
    	if (iOverrides == null) iOverrides = new HashMap<XCourseId, XOverride>();
    	if (override == null)
    		iOverrides.remove(courseId);
    	else
    		iOverrides.put(courseId, override);
    }
    public boolean hasOverrides() { return iOverrides != null && !iOverrides.isEmpty(); }
    public Map<XCourseId, XOverride> getOverrides() { return iOverrides; }
    
    public Integer getOverrideStatus(XCourseId courseId) {
    	XOverride override = (iOverrides == null ? null : iOverrides.get(courseId));
    	return (override == null ? null : override.getStatus());
    }

    public Date getOverrideTimeStamp(XCourseId courseId) {
    	XOverride override = (iOverrides == null ? null : iOverrides.get(courseId));
    	return (override == null ? null : override.getTimeStamp());
    }

    public String getOverrideExternalId(XCourseId courseId) {
    	XOverride override = (iOverrides == null ? null : iOverrides.get(courseId));
    	return (override == null ? null : override.getExternalId());
    }
    
    public boolean isOverridePending(XCourseId courseId) {
    	XOverride override = (iOverrides == null ? null : iOverrides.get(courseId));
    	return (override == null || override.getStatus() == null ? false : override.getStatus().intValue() == CourseRequestOverrideStatus.PENDING.ordinal());
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
    
    public List<XPreference> getPreferences(XCourseId courseId) {
    	if (iPreferences == null) return null;
    	return iPreferences.get(courseId);
    }
    
    public void fillChoicesIn(org.cpsolver.studentsct.model.CourseRequest request) {
    	if (iSectionWaitlist != null)
    		for (Map.Entry<XCourseId, List<XWaitListedSection>> entry: iSectionWaitlist.entrySet()) {
    			Course course = request.getCourse(entry.getKey().getCourseId());
    			if (course != null)
    				for (XSection section: entry.getValue()) {
    					List<Instructor> instructors = null;
    					if (!section.getInstructors().isEmpty()) {
    						instructors = new ArrayList<Instructor>();
    						for (XInstructor i: section.getInstructors())
    							instructors.add(new Instructor(i.getIntructorId(), i.getExternalId(), i.getName(), i.getEmail()));
    					}
                        request.getSelectedChoices().add(new Choice(course.getOffering(), section.getInstructionalType(), section.getTime() == null || section.getTime().getDays() == 0 ? null : section.getTime().toTimeLocation(), instructors));
    				}
    		}
    	if (iPreferences != null) {
    		for (Map.Entry<XCourseId, List<XPreference>> entry: iPreferences.entrySet()) {
    			Course course = request.getCourse(entry.getKey().getCourseId());
    			if (course != null) {
    				for (XPreference p: entry.getValue()) {
    					switch (p.getType()) {
    					case INSTR_METHOD:
    						for (Config config: course.getOffering().getConfigs())
    							if (config.getInstructionalMethodId() != null && config.getInstructionalMethodId().equals(p.getUniqueId()))
    								(p.isRequired() ? request.getRequiredChoices() : request.getSelectedChoices()).add(new Choice(config));
    						break;
    					case SECTION:
    						Section section = course.getOffering().getSection(p.getUniqueId());
							if (section != null) {
								if (p.isRequired()) {
									Section x = section;
									while (x != null) {
										request.getRequiredChoices().add(new Choice(x)); x = x.getParent();
									}
									request.getRequiredChoices().add(new Choice(section.getSubpart().getConfig()));
								} else {
									request.getSelectedChoices().add(new Choice(section));
								}
							}
							break;
						}
    				}
    			}
    		}
    	}
    }
    
    public void fillPreferencesIn(RequestedCourse rc, XCourseId courseId) {
    	List<XPreference> prefs = getPreferences(courseId);
    	if (prefs != null)
    		for (XPreference p: prefs) {
    			switch (p.getType()) {
				case INSTR_METHOD:
					rc.setSelectedIntructionalMethod(p.getUniqueId(), p.getLabel(), p.isRequired(), true);
					break;
				case SECTION:
					rc.setSelectedClass(p.getUniqueId(), p.getLabel(), p.isRequired(), true);
					break;
				}
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
        
        int nrCoursePrefs = in.readInt();
        if (nrCoursePrefs == 0)
        	iPreferences = null;
        else {
        	iPreferences = new HashMap<XCourseId, List<XPreference>>();
        	for (int i = 0; i < nrCoursePrefs; i++) {
        		Long courseId = in.readLong();
        		int nbrPrefs = in.readInt();
        		List<XPreference> prefs = new ArrayList<XPreference>(nbrPrefs);
        		for (int j = 0; j < nbrPrefs; j++)
        			prefs.add(new XPreference(in));
				for (XCourseId course: iCourseIds)
    				if (course.getCourseId().equals(courseId)) {
    					iPreferences.put(course, prefs);
    					break;
    				}
        	}
        }
        
        iMessage = (String)in.readObject();
        
        int nrOverrides = in.readInt();
        if (nrOverrides == 0)
        	iOverrides = null;
        else {
        	iOverrides = new HashMap<XCourseId, XOverride>();
        	for (int i = 0; i < nrOverrides; i++) {
        		Long courseId = in.readLong();
        		XOverride override = new XOverride(in);
        		for (XCourseId course: iCourseIds)
    				if (course.getCourseId().equals(courseId)) {
    					iOverrides.put(course, override);
    					break;
    				}
        	}
        }
        
        iCritical = in.readBoolean();
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
		
		out.writeInt(iPreferences == null ? 0 : iPreferences.size());
		if (iPreferences != null)
			for (Map.Entry<XCourseId, List<XPreference>> entry: iPreferences.entrySet()) {
				out.writeLong(entry.getKey().getCourseId());
				out.writeInt(entry.getValue().size());
				for (XPreference p: entry.getValue())
					p.writeExternal(out);
			}
		
		out.writeObject(iMessage);
		
		out.writeInt(iOverrides == null ? 0 : iOverrides.size());
		if (iOverrides != null)
			for (Map.Entry<XCourseId, XOverride> entry: iOverrides.entrySet()) {
				out.writeLong(entry.getKey().getCourseId());
				entry.getValue().writeExternal(out);
			}
		
		out.writeBoolean(iCritical);
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
	
	public static enum XPreferenceType {
		SECTION,
		INSTR_METHOD,
		;
	}
	
	public static class XPreference implements Externalizable {
		private boolean iRequired = false;
		private Long iId = null;
		private String iLabel = null;
		private XPreferenceType iType = null;
		
		public XPreference(CourseRequest cr, StudentSectioningPref p) {
			iRequired = p.isRequired();
			if (p instanceof StudentClassPref) {
				StudentClassPref scp = (StudentClassPref)p;
				iId = scp.getClazz().getUniqueId();
				iLabel = scp.getClazz().getClassPrefLabel(cr.getCourseOffering());
				iType = XPreferenceType.SECTION;
			} else {
				StudentInstrMthPref imp = (StudentInstrMthPref)p;
				iId = imp.getInstructionalMethod().getUniqueId();
				iLabel = imp.getInstructionalMethod().getLabel();
				iType = XPreferenceType.INSTR_METHOD;
			}
		}
		
		public XPreference(XPreferenceType type, Long id, String label, boolean required) {
			iType = type;
			iId = id;
			iLabel = label;
			iRequired = required;
		}
		
		public XPreference(Section a, Course c, boolean required) {
			iType = XPreferenceType.SECTION;
			iId = a.getId();
			iLabel = a.getName(c.getId());
			if (iLabel.length() <= 4)
				iLabel = a.getSubpart().getName() + " " + iLabel;
			iRequired = required;
		}
		
		public XPreference(ObjectInput in) throws IOException, ClassNotFoundException {
			readExternal(in);
		}
		
		public boolean isRequired() { return iRequired; }
		public Long getUniqueId() { return iId; }
		public String getLabel() { return iLabel; }
		public XPreferenceType getType() { return iType; }

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(iType.ordinal());
			out.writeBoolean(iRequired);
			out.writeLong(iId);
			out.writeObject(iLabel);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			iType = XPreferenceType.values()[in.readInt()];
			iRequired = in.readBoolean();
			iId = in.readLong();
			iLabel = (String)in.readObject();
		}
	}
}
