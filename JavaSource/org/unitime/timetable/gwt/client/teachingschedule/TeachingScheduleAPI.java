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
package org.unitime.timetable.gwt.client.teachingschedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.TeachingScheduleMessages;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TeachingScheduleAPI {
	
	public static class GetTeachingSchedule implements GwtRpcRequest<TeachingSchedule> {
		private Long iCourseId = null;
		private String iAttributeType = null;
		public GetTeachingSchedule() {}
		public GetTeachingSchedule(Long courseId, String attributeType) { iCourseId = courseId; iAttributeType = attributeType; }
		
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long id) { iCourseId = id; }
		public String getAttributeType() { return iAttributeType; }
		public void setAttributeType(String type) { iAttributeType = type; }
		public boolean hasAttributeType() { return iAttributeType != null && !iAttributeType.isEmpty(); }
	}
	
	public static class SaveTeachingSchedule implements GwtRpcRequest<GwtRpcResponseNull> {
		private TeachingSchedule iOffering = null;
		public SaveTeachingSchedule() {}
		public SaveTeachingSchedule(TeachingSchedule offering) { iOffering = offering; }
		
		public TeachingSchedule getOffering() { return iOffering; }
		public void setOffering(TeachingSchedule offering) { iOffering = offering; }
	}
	
	public static class DeleteTeachingSchedule implements GwtRpcRequest<GwtRpcResponseNull> {
		private Long iOfferingId = null;
		public DeleteTeachingSchedule() {}
		public DeleteTeachingSchedule(Long offeringId) { iOfferingId = offeringId; }
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
	}
	
	public static class GetInstructorTeachingSchedule implements GwtRpcRequest<Instructor> {
		private Long iInstructorId;
		
		public GetInstructorTeachingSchedule() {}
		public GetInstructorTeachingSchedule(Long id) { iInstructorId = id; }
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long id) { iInstructorId = id; }
	}

	public static class TeachingSchedule implements GwtRpcResponse, Comparable<TeachingSchedule> {
		private Long iOfferingId;
		private Long iCourseId;
		private Long iSubjectAreaId;
		private String iCourseName;
		private List<CourseGroup> iGroups = null;
		private List<Attribute> iAttributes = null;
		private List<Instructor> iInstructors = null;
		private List<Clazz> iClasses = null;
		
		public TeachingSchedule() {}
		
		public Long getOfferingId() { return iOfferingId; }
		public void setOfferingId(Long id) { iOfferingId = id; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long id) { iCourseId = id; }
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		public void setSubjectAreaId(Long id) { iSubjectAreaId = id; }
		public String getCourseName() { return iCourseName; }
		public void setCourseName(String name) { iCourseName = name; }
		
		public boolean hasGroups() { return iGroups != null && !iGroups.isEmpty(); }
		public void addGroup(CourseGroup d) {
			if (iGroups == null) iGroups = new ArrayList<CourseGroup>();
			iGroups.add(d);
		}
		public List<CourseGroup> getGroups() { return iGroups; }
		public CourseGroup getGroup(Long configId, Integer typeId) {
			if (iGroups == null) return null;
			for (CourseGroup d: iGroups)
				if (d.getConfigId().equals(configId) && d.getTypeId().equals(typeId))
					return d;
			return null;
		}
		
		public List<CourseGroupDivision> getDivisions() {
			List<CourseGroupDivision> ret = new ArrayList<CourseGroupDivision>();
			if (hasGroups())
				for (CourseGroup g: getGroups()) {
					if (g.hasDivisions()) {
						int idx = 0;
						for (CourseDivision d: g.getDivisions()) {
							ret.add(new CourseGroupDivision(g, d, idx == 0));
							idx ++;
						}
					} else {
						CourseDivision d = new CourseDivision();
						d.setHours(g.getHours());
						d.setName(g.getType());
						g.addDivision(d);
						ret.add(new CourseGroupDivision(g, d, true));
					}
				}
			return ret;
		}
		
		public boolean hasAttributes() { return iAttributes != null && !iAttributes.isEmpty(); }
		public void addAttribute(Attribute a) {
			if (iAttributes == null) iAttributes = new ArrayList<Attribute>();
			iAttributes.add(a);
		}
		public List<Attribute> getAttributes() { return iAttributes; }
		public Attribute getAttribute(String reference) {
			if (iAttributes == null || reference == null) return null;
			for (Attribute a: iAttributes)
				if (a.getReference().equals(reference)) return a;
			return null;
		}
		
		public boolean hasInstructors() { return iInstructors != null && !iInstructors.isEmpty(); }
		public void addInstructor(Instructor a) {
			if (iInstructors == null) iInstructors = new ArrayList<Instructor>();
			iInstructors.add(a);
		}
		public List<Instructor> getInstructors() { return iInstructors; }
		public Instructor getInstructor(Long id) {
			if (iInstructors == null || id == null) return null;
			for (Instructor i: iInstructors)
				if (i.getInstructorId().equals(id)) return i;
			return null;
		}
		public List<Instructor> getInstructors(String attributeRef) {
			List<Instructor> ret = new ArrayList<Instructor>();
			if (iInstructors != null)
				for (Instructor i: iInstructors)
					if (i.getAttribute(attributeRef) != null) ret.add(i);
			return ret;
		}
		
		public void addClass(Clazz clazz) {
			if (iClasses == null) iClasses = new ArrayList<Clazz>();
			iClasses.add(clazz);
		}
		public List<Clazz> getClasses() {
			updateClasses(false);
			return iClasses;
		}
		public Clazz getClass(CourseGroup g, int classIndex, int groupIndex) {
			if (iClasses == null) return null;
			for (Clazz c: iClasses) {
				if (c.getConfigId().equals(g.getConfigId()) && c.getTypeId().equals(g.getTypeId()) && c.getClassIndex() == classIndex && c.getGroupIndex() == groupIndex)
					return c;
			}
			return null;
		}
		public void updateClasses(boolean sort) {
			if (iClasses == null) iClasses = new ArrayList<Clazz>();
			boolean changed = false;
			// remove redundant
			for (Iterator<Clazz> i = iClasses.iterator(); i.hasNext(); ) {
				Clazz clazz = i.next();
				CourseGroup g = getGroup(clazz.getConfigId(), clazz.getTypeId());
				if (g == null) { i.remove(); continue; }
				if (clazz.getClassIndex() >= g.getNrClasses()) { i.remove(); continue; }
				if (clazz.getGroupIndex() >= g.getNrGroups()) { i.remove(); continue; }
				clazz.updateName(g);
			}
			// add mission
			if (iGroups != null)
				for (CourseGroup g: iGroups) {
					for (int classIndex = 0; classIndex < g.getNrClasses(); classIndex++)
						for (int groupIndex = 0; groupIndex < g.getNrGroups(); groupIndex++)
							if (getClass(g, classIndex, groupIndex) == null) {
								iClasses.add(new Clazz(g, classIndex, groupIndex, g.getClassSuffix(classIndex)));
								changed = true;
							}
				}
			if (changed || sort)
				Collections.sort(iClasses, new Comparator<Clazz>() {
					@Override
					public int compare(Clazz c1, Clazz c2) {
						int i1 = iGroups.indexOf(getGroup(c1.getConfigId(), c1.getTypeId()));
						int i2 = iGroups.indexOf(getGroup(c2.getConfigId(), c2.getTypeId()));
						if (i1 != i2) return (i1 < i2 ? -1 : 1);
						if (c1.getClassIndex() != c2.getClassIndex())
							return (c1.getClassIndex() < c2.getClassIndex() ? -1 : 1);
						return (c1.getGroupIndex() < c2.getGroupIndex() ? -1 : 1);
					}
				});
		}
		
		public TeachingMeeting getMeeting(MeetingAssignment ma) {
			if (iGroups == null || ma == null) return null;
			for (CourseGroup cd: iGroups) {
				if (cd.hasMeetings())
					for (TeachingMeeting m: cd.getMeetings())
						if (m.getClassMeetingId().equals(ma.getClassMeetingId()))
							return m;
			}
			return null;
		}
		
		public List<ValidationError> getDivisionErrors() {
			List<ValidationError> errors = new ArrayList<ValidationError>();
			new CourseDivisionHoursValidator().validate(this, errors);
			new CourseDivisionNameValidator().validate(this, errors);
			return errors;
		}
		
		public List<ValidationError> getAssignmentErrors(Clazz clazz) {
			CourseGroup group = getGroup(clazz.getConfigId(), clazz.getTypeId()); 
			List<ValidationError> errors = new ArrayList<ValidationError>();
			new DivisionAssignmentValidator().validate(this, group, clazz, errors);
			new InstructorAvailabilityValidator().validate(this, group, clazz, errors);
			new ParallelValidator().validate(this, group, clazz, errors);
			new InstructorLoadValidator().validate(this, group, clazz, errors);
			new InstructorAttributeValidator().validate(this, group, clazz, errors);
			return errors;
		}
		
		public List<ValidationError> getAssignmentErrors() {
			List<ValidationError> errors = new ArrayList<ValidationError>();
			new DivisionAssignmentValidator().validate(this, errors);
			new InstructorAvailabilityValidator().validate(this, errors);
			new ParallelValidator().validate(this, errors);
			new InstructorLoadValidator().validate(this, errors);
			new InstructorAttributeValidator().validate(this, errors);
			return errors;
		}
		
		public List<ValidationError> getAllErrors() {
			List<ValidationError> errors = new ArrayList<ValidationError>();
			new CourseDivisionHoursValidator().validate(this, errors);
			new CourseDivisionNameValidator().validate(this, errors);
			new DivisionAssignmentValidator().validate(this, errors);
			new InstructorAvailabilityValidator().validate(this, errors);
			new ParallelValidator().validate(this, errors);
			new InstructorLoadValidator().validate(this, errors);
			new InstructorAttributeValidator().validate(this, errors);
			return errors;
		}

		@Override
		public int compareTo(TeachingSchedule o) {
			int cmp = getCourseName().compareTo(o.getCourseName());
			if (cmp != 0) return cmp;
			return getOfferingId().compareTo(o.getOfferingId());
		}
	}
	
	public static class CourseGroup implements IsSerializable {
		private Integer iTypeId;
		private String iType;
		private Long iConfigId;
		private String iConfigName;
		private int iNrClasses;
		private int iNrGroups = 1;
		private int iHours;
		private List<TeachingMeeting> iMeetings = null;
		private List<CourseDivision> iDivisions = null;
		private Map<Integer, String> iClassSuffixes;
		
		public CourseGroup() {}
		
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long id) { iConfigId = id; }
		public Integer getTypeId() { return iTypeId; }
		public void setTypeId(Integer id) { iTypeId = id; }
		public String getConfigName() { return iConfigName; }
		public void setConfigName(String name) { iConfigName = name; }
		public int getNrClasses() { return iNrClasses; }
		public void setNrClasses(int nrClasses) { iNrClasses = nrClasses; }
		public int getNrGroups() { return iNrGroups; }
		public void setNrGroups(int nrGroups) { iNrGroups = nrGroups; }
		
		public int getHours() { return iHours; }
		public void setHours(int hours) { iHours = hours; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		
		public String getTypeAndConfig() { return getConfigName() != null && !getConfigName().isEmpty() ? getType() + " [" + getConfigName() + "]" : getType(); }
		
		public boolean hasMeetings() { return iMeetings != null && !iMeetings.isEmpty(); }
		public void clearMeetings() { iMeetings = null; }
		public List<TeachingMeeting> getMeetings() { return iMeetings; }
		public TeachingMeeting getMeeting(Long classMeetingId) {
			if (iMeetings == null) return null;
			for (TeachingMeeting m: iMeetings)
				if (m.getClassMeetingId().equals(classMeetingId)) return m;
			return null;
		}
		public void addMeeting(TeachingMeeting meeting) {
			if (iMeetings == null) iMeetings = new ArrayList<TeachingMeeting>();
			iMeetings.add(meeting);
		}
		public Set<TeachingMeeting> getMeetings(int classIndex) {
			Set<TeachingMeeting> ret = new TreeSet<TeachingMeeting>();
			if (iMeetings != null)
				for (TeachingMeeting m: iMeetings)
					if (m.getClassIndex() == classIndex)
						ret.add(m);
			return ret;
		}

		public boolean hasDivisions() { return iDivisions != null && !iDivisions.isEmpty(); }
		public void clearDivisions() { iDivisions = null; }
		public List<CourseDivision> getDivisions() { return iDivisions; }
		public void addDivision(CourseDivision cd) {
			if (iDivisions == null) iDivisions = new ArrayList<CourseDivision>();
			iDivisions.add(cd);
			fixDivisionIndexes();
		}
		public void addDivision(int index, CourseDivision cd) {
			if (iDivisions == null) iDivisions = new ArrayList<CourseDivision>();
			iDivisions.add(index, cd);
			fixDivisionIndexes();
		}
		public void removeDivision(CourseDivision cd) {
			if (iDivisions != null) iDivisions.remove(cd);
			fixDivisionIndexes();
		}
		public void fixDivisionIndexes() {
			if (iDivisions == null) return;
			for (int i = 0; i < iDivisions.size(); i++)
				iDivisions.get(i).setDivisionIndex(i);
		}
		
		@Override
		public int hashCode() {
			return getTypeId().hashCode() ^ getConfigId().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CourseGroup)) return false;
			CourseGroup cg = (CourseGroup)o;
			return getTypeId().equals(cg.getTypeId()) && getConfigId().equals(cg.getConfigId());
		}
		
		public String getClassSuffix(int classIndex) {
			if (iClassSuffixes == null) return null;
			return iClassSuffixes.get(classIndex);
		}
		public void setClassSuffix(int classIndex, String classSuffix) {
			if (classSuffix == null || classSuffix.isEmpty()) return;
			if (iClassSuffixes == null) iClassSuffixes = new HashMap<Integer, String>();
			iClassSuffixes.put(classIndex, classSuffix);
		}
	}
	
	public static class CourseDivision implements IsSerializable {
		private String iName;
		private int iNrParalel = 1;
		private int iHours;
		private String iAttributeRef;
		private int iDivisionIndex = 0;
		
		public CourseDivision() {}
		
		public CourseDivision(CourseDivision d) {
			iName = d.iName;
			iNrParalel = d.iNrParalel;
			iHours = d.iHours;
			iAttributeRef = d.iAttributeRef;
			iDivisionIndex = d.iDivisionIndex;
		}
		
		public String getName() { return iName == null ? "" : iName; }
		public void setName(String name) { iName = name; }

		public int getNrParalel() { return iNrParalel; }
		public void setNrParalel(int nrParalel) { iNrParalel = nrParalel; }
		public int getHours() { return iHours; }
		public void setHours(int hours) { iHours = hours; }
		
		public int getDivisionIndex() { return iDivisionIndex; }
		public void setDivisionIndex(int idx) { iDivisionIndex = idx; }
		
		public boolean hasAttributeRef() { return iAttributeRef != null && !iAttributeRef.isEmpty(); }
		public String getAttributeRef() { return iAttributeRef; }
		public void setAttributeRef(String a) { iAttributeRef = a; }
		
		@Override
		public int hashCode() { return new Integer(iDivisionIndex).hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof CourseDivision)) return false;
			CourseDivision d = (CourseDivision)o;
			return getDivisionIndex() == d.getDivisionIndex();
		}
	}
	
	public static class CourseGroupDivision {
		private CourseDivision iDivision;
		private CourseGroup iGroup;
		private boolean iMaster = false;
		
		public CourseGroupDivision(CourseGroup group, CourseDivision division, boolean master) {
			iDivision = division;
			iGroup = group;
			iMaster = master;
		}
		
		public CourseDivision getDivision() { return iDivision; }
		public CourseGroup getGroup() { return iGroup; }
		
		public boolean isMaster() { return iMaster; }
		public void setMaster(boolean master) { iMaster = master; }
	}
	
	public static class Attribute implements IsSerializable {
		private Long iId;
		private String iReference;
		private String iLabel;
		
		public Attribute() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public String toString() { return getReference(); }
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Attribute)) return false;
			return ((Attribute)o).getId().equals(getId());
		}
	}
	
	public static class GetAttributeTypes implements GwtRpcRequest<GwtRpcResponseList<AttributeType>> {
	}
	
	public static class GetSubjectAreas implements GwtRpcRequest<GwtRpcResponseList<SubjectArea>> {
	}
	
	public static class ListTeachingSchedules implements GwtRpcRequest<GwtRpcResponseList<TeachingSchedule>> {
		private Long iSubjectAreaId;
		public ListTeachingSchedules() {}
		public ListTeachingSchedules(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }
		
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		public void setSubjectAreaId(Long id) { iSubjectAreaId = id; }
	}
	
	public static class AttributeType implements IsSerializable {
		private Long iId;
		private String iReference;
		private String iLabel;
		
		public AttributeType() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public String toString() { return getReference(); }
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof AttributeType)) return false;
			return ((AttributeType)o).getId().equals(getId());
		}
	}
	
	public static class SubjectArea implements IsSerializable, Comparable<SubjectArea> {
		private Long iId;
		private String iReference;
		private String iLabel;
		
		public SubjectArea() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public String getReference() { return iReference; }
		public void setReference(String reference) { iReference = reference; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		
		@Override
		public String toString() { return getReference(); }
		@Override
		public int hashCode() { return getId().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof SubjectArea)) return false;
			return ((SubjectArea)o).getId().equals(getId());
		}
		@Override
		public int compareTo(SubjectArea o) {
			int cmp = getReference().compareTo(o.getReference());
			if (cmp != 0) return cmp;
			return getId().compareTo(o.getId());
		}
	}
	
	public static class GetMeetingAssignments implements GwtRpcRequest<TeachingSchedule> {
		private TeachingSchedule iOffering;
		
		public GetMeetingAssignments() {}
		public GetMeetingAssignments(TeachingSchedule offering) {
			iOffering = offering;
		}
		
		public TeachingSchedule getOffering() { return iOffering; }
		public void setOffering(TeachingSchedule offering) { iOffering = offering; }
	}
	
	public static class Instructor implements GwtRpcResponse, Comparable<Instructor> {
		private Long iInstructorId;
		private String iName;
		private List<Attribute> iAttributes;
		private int iMaxLoad;
		private Map<Integer, Date> iUnavailableDays = new HashMap<Integer, Date>();
		private List<MeetingTime> iProhibitedTimes = new ArrayList<MeetingTime>();
		private List<InstructorMeetingAssignment> iAssignments = new ArrayList<InstructorMeetingAssignment>();
		
		public Instructor() {}
		
		public Long getInstructorId() { return iInstructorId; }
		public void setInstructorId(Long id) { iInstructorId = id; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public int getMaxLoad() { return iMaxLoad; }
		public void setMaxLoad(int load) { iMaxLoad = load; }
		
		public void addUnavailableDay(Date date, int dayOfYear) { iUnavailableDays.put(dayOfYear, date); }
		public void addProhibitedTime(int dow, int startSlot, int endSlot) {
			iProhibitedTimes.add(new MeetingTime(dow, startSlot, endSlot));
		}
		public void addAssignment(InstructorMeetingAssignment m) { iAssignments.add(m); }
		public boolean isAvailable(TeachingMeeting m, int hour) {
			if (iUnavailableDays.containsKey(m.getDayOfYear())) return false; // unavailable day
			for (MeetingTime mt: iProhibitedTimes) {
				if (mt.overlaps(m.getHour(hour))) return false; // prohibited time
			}
			for (InstructorMeetingAssignment a: iAssignments) {
				if (a.getDayOfYear() == m.getDayOfYear() && a.getHours().overlaps(m.getHour(hour))) return false; // teaching something else
			}
			return true;
		}
		public boolean hasAssignments() { return iAssignments != null && !iAssignments.isEmpty(); }
		public List<InstructorMeetingAssignment> getAssignmetns() { return iAssignments; }
		
		public boolean hasAttributes() { return iAttributes != null && !iAttributes.isEmpty(); }
		public void addAttribute(Attribute a) {
			if (iAttributes == null) iAttributes = new ArrayList<Attribute>();
			iAttributes.add(a);
		}
		public List<Attribute> getAttributes() { return iAttributes; }
		public Attribute getAttribute(String reference) {
			if (iAttributes == null || reference == null) return null;
			for (Attribute a: iAttributes)
				if (a.getReference().equals(reference)) return a;
			return null;
		}

		@Override
		public int compareTo(Instructor i) {
			int cmp = getName().compareTo(i.getName());
			if (cmp != 0) return cmp;
			return getInstructorId().compareTo(i.getInstructorId());
		}
		
		public List<ValidationError> getAssignmetnErrors() {
			List<ValidationError> errors = new ArrayList<ValidationError>();
			new InstructorAvailabilityValidator().validate(this, errors);
			new InstructorLoadValidator().validate(this, errors);
			new InstructorAttributeValidator().validate(this, errors);
			return errors;
		}
	}

	public static class TeachingMeeting implements IsSerializable, Comparable<TeachingMeeting> {
		private int iClassIndex;
		
		private Long iClassId;
		private Long iClassMeetingId;
		
		private String iMeetingDate;
		private Date iMeetingTime;
		private int iDayOfYear;
		private List<MeetingTime> iHours;
		private String iLocation;
		
		public TeachingMeeting() {}
		
		public int getClassIndex() { return iClassIndex; }
		public void setClassIndex(int classIndex) { iClassIndex = classIndex; }
		
		public Long getClassId() { return iClassId; }
		public void setClassId(Long classId) { iClassId = classId; }
		public Long getClassMeetingId() { return iClassMeetingId; }
		public void setClassMeetingId(Long id) { iClassMeetingId = id; }
		
		public Date getMeetingTime() { return iMeetingTime; }
		public void setMeetingTime(Date date) { iMeetingTime = date; }
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int dayOfYear) { iDayOfYear = dayOfYear; }
		public String getMeetingDate() { return iMeetingDate; }
		public void setMeetingDate(String formattedDate) { iMeetingDate = formattedDate; }
		
		public String getLocation() { return iLocation == null ? "" : iLocation; }
		public void setLocation(String loc) { iLocation = loc; }
		
		public List<MeetingTime> getHours() { return iHours; }
		public MeetingTime getHour(int hour) { return iHours.get(hour); }
		public boolean hasHours() { return iHours != null && !iHours.isEmpty(); }
		public void addHour(int dow, int startSlot, int endSlot) {
			if (iHours == null) iHours = new ArrayList<MeetingTime>();
			iHours.add(new MeetingTime(dow, startSlot, endSlot));
		}
		public int getStartSlot() {
			if (iHours == null) return 0;
			return iHours.get(0).getStartSlot();
		}
		public int getEndSlot() {
			if (iHours == null) return 0;
			return iHours.get(iHours.size() - 1).getEndSlot();
		}
		
		@Override
		public int compareTo(TeachingMeeting m) {
			if (getDayOfYear() != m.getDayOfYear()) return (getDayOfYear() < m.getDayOfYear() ? -1 : 1);
			if (getStartSlot() != m.getStartSlot()) return (getStartSlot() < m.getStartSlot() ? -1 : 1);
			if (!getLocation().equals(m.getLocation())) return getLocation().compareTo(m.getLocation());
			return getClassMeetingId().compareTo(m.getClassMeetingId());
		}
	}
	
	public static class Clazz implements IsSerializable {
		private List<MeetingAssignment> iMeetingAssignments = null;
		private Integer iTypeId;
		private Long iConfigId;
		private int iClassIndex, iGroupIndex;
		private String iName;
		private String iClassSuffix;
		
		public Clazz() {}
		public Clazz(CourseGroup g, int classIndex, int groupIndex, String classSuffix) {
			iConfigId = g.getConfigId();
			iTypeId = g.getTypeId();
			iClassSuffix = classSuffix;
			iClassIndex = classIndex;
			iGroupIndex = groupIndex;
			iMeetingAssignments = new ArrayList<MeetingAssignment>();
			if (g.hasMeetings())
				for (TeachingMeeting m: g.getMeetings()) {
					if (m.getClassIndex() == classIndex)
						iMeetingAssignments.add(new MeetingAssignment(m));
				}
			updateName(g);
		}
		
		public void updateName(CourseGroup g) {
			if (g.getNrGroups() > 1) {
				iName = g.getType() + " " + g.getConfigName() + (hasClassSuffix() ? getClassSuffix() : ((iClassIndex < 9 ? "0" : "") + (1 + iClassIndex))) + (char)('A' + iGroupIndex);
			} else if (g.getNrClasses() > 1) {
				iName = g.getType() + " " + g.getConfigName() + (hasClassSuffix() ? getClassSuffix() : ((iClassIndex < 9 ? "0" : "") + (1 + iClassIndex)));
			} else {
				iName = g.getType() + " " + g.getConfigName() + (hasClassSuffix() ? getClassSuffix() : "");
			}
		}
		public void setName(String name) { iName = name; }
		public String getName() { return iName; }
		
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long id) { iConfigId = id; }
		public Integer getTypeId() { return iTypeId; }
		public void setTypeId(Integer id) { iTypeId = id; }
		
		public void setClassSuffix(String classSuffix) { iClassSuffix = classSuffix; }
		public String getClassSuffix() { return iClassSuffix; }
		public boolean hasClassSuffix() { return iClassSuffix != null && !iClassSuffix.isEmpty(); }
		
		public int getClassIndex() { return iClassIndex; }
		public void setClassIndex(int classIndex) { iClassIndex = classIndex; }
		public int getGroupIndex() { return iGroupIndex; }
		public void setGroupIndex(int groupIndex) { iGroupIndex = groupIndex; }
		
		public Set<TeachingMeeting> getMeetings(TeachingSchedule offering) {
			CourseGroup g = offering.getGroup(getConfigId(), getTypeId());
			return (g == null ? null : g.getMeetings(getClassIndex()));
		}
		
		public boolean hasMeetingAssignments() { return iMeetingAssignments != null && !iMeetingAssignments.isEmpty(); }
		public void addMeetingAssignment(MeetingAssignment a) {
			if (iMeetingAssignments == null) iMeetingAssignments = new ArrayList<MeetingAssignment>();
			iMeetingAssignments.add(a);
		}
		public List<MeetingAssignment> getMeetingAssignments() { return iMeetingAssignments; }
		public List<MeetingAssignment> getMeetingAssignments(TeachingMeeting m) {
			if (iMeetingAssignments == null || m == null) return null;
			List<MeetingAssignment> ret = new ArrayList<MeetingAssignment>();
			for (MeetingAssignment ma: iMeetingAssignments)
				if (ma.getClassMeetingId().equals(m.getClassMeetingId()))
					ret.add(ma);
			return ret;
		}
		public MeetingAssignment getMeetingAssignment(TeachingMeeting m, int hour) {
			if (iMeetingAssignments == null || m == null) return null;
			for (MeetingAssignment ma: iMeetingAssignments)
				if (ma.getClassMeetingId().equals(m.getClassMeetingId()) && ma.hasHour(hour))
					return ma;
			return null;
		}
		public MeetingAssignment split(MeetingAssignment m, int hour) {
			if (m.getFirstHour() == m.getLastHour()) return null;
			MeetingAssignment ret = new MeetingAssignment(m);
			ret.setHours(hour, m.getLastHour());
			m.setHours(m.getFirstHour(), hour);
			iMeetingAssignments.add(iMeetingAssignments.indexOf(m) + 1, ret); 
			return ret;
		}
	}
	
	public static class MeetingTime implements IsSerializable {
		private int iDayOfWeek;
		private int iStartSlot;
		private int iEndSlot;
		
		public MeetingTime() {}
		public MeetingTime(int dayOfWeek, int startSlot, int endSlot) { iDayOfWeek = dayOfWeek; iStartSlot = startSlot; iEndSlot = endSlot; }
		
		public int getDayOfWeek() { return iDayOfWeek; }
		public void setDayOfWeek(int dow) { iDayOfWeek = dow; }
		public int getStartSlot() { return iStartSlot; }
		public void setStartSlot(int slot) { iStartSlot = slot; }
		public int getLength() { return iEndSlot - iStartSlot; }
		public int getEndSlot() { return iEndSlot; }
		public void setEndSlot(int endSlot) { iEndSlot = endSlot; }
		
		public boolean overlaps(MeetingTime time) {
			return iDayOfWeek == time.iDayOfWeek && (iEndSlot > time.iStartSlot) && (time.iEndSlot > iStartSlot);
		}
		@Override
		public int hashCode() {
			return new Integer(getDayOfWeek() * 82944 + getStartSlot() * 288 + getEndSlot()).hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MeetingTime)) return false;
			MeetingTime t = (MeetingTime)o;
			return t.getDayOfWeek() == getDayOfWeek() && t.getStartSlot() == getStartSlot() && t.getEndSlot() == getEndSlot();
		}
	}
	
	public static class MeetingAssignment implements IsSerializable {
		private Set<Long> iInstructorIds;
		private CourseDivision iDivision;
		private String iNote;
		private Long iClassMeetingId;
		private int iFirstHour = 0, iLastHour = 0;
		
		public MeetingAssignment() {}
		
		public MeetingAssignment(TeachingMeeting m) {
			setClassMeetingId(m.getClassMeetingId());
			if (m.hasHours())
				setHours(0, m.getHours().size() - 1);
		}
		public MeetingAssignment(MeetingAssignment a) {
			iInstructorIds = (a.iInstructorIds == null ? null : new HashSet<Long>(a.iInstructorIds));
			iDivision = a.iDivision;
			iNote = a.iNote;
			iClassMeetingId = a.iClassMeetingId;
			iFirstHour = a.iFirstHour;
			iLastHour = a.iLastHour;
		}
		
		public boolean hasInstructors() { return iInstructorIds != null && !iInstructorIds.isEmpty(); }
		public void addInstructor(Long id) {
			if (iInstructorIds == null) iInstructorIds = new HashSet<Long>();
			iInstructorIds.add(id);
		}
		public void addInstructor(Instructor instructor) { addInstructor(instructor.getInstructorId()); }
		public boolean removeInstructor(Long id) {
			if (iInstructorIds == null) return false;
			return iInstructorIds.remove(id);
		}
		public boolean removeInstructor(Instructor instructor) { return removeInstructor(instructor.getInstructorId()); }
		public Set<Long> getInstructor() { return iInstructorIds; }
		public boolean hasInstructor(Long id) { return iInstructorIds != null && iInstructorIds.contains(id); }
		public boolean hasInstructor(Instructor i) { return hasInstructor(i.getInstructorId()); }
		
		public CourseDivision getDivision() { return iDivision ;}
		public void setDivision(CourseDivision division) { iDivision = division; }
		public boolean hasDivision() { return iDivision != null; }
		public int getDivisionIndex() { return iDivision == null ? -1 : iDivision.getDivisionIndex(); }
		
		public boolean hasAttributeRef() { return iDivision != null && iDivision.hasAttributeRef(); }
		public String getAttributeRef() { return (iDivision == null ? null : iDivision.getAttributeRef()); }
		
		public String getNote() { return iNote ;}
		public void setNote(String note) { iNote = note; }
		public boolean hasNote() { return iNote != null && !iNote.isEmpty(); }
		
		public Long getClassMeetingId() { return iClassMeetingId; }
		public void setClassMeetingId(Long id) { iClassMeetingId = id; }
		
		public int getFirstHour() { return iFirstHour; }
		public int getLastHour() { return iLastHour; }
		public void setHours(int first, int last) { iFirstHour = first; iLastHour = last; }
		
		public boolean hasHour(int hour) {
			return iFirstHour <= hour && hour <= iLastHour;
		}
		public MeetingTime getMeetingTime(TeachingMeeting m) {
			return new MeetingTime(m.getHour(getFirstHour()).getDayOfWeek(), m.getHour(getFirstHour()).getStartSlot(), m.getHour(getLastHour()).getEndSlot());
		}
		
		@Override
		public int hashCode() {
			return new Long(getClassMeetingId() * 24 + iFirstHour).hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof MeetingAssignment)) return false;
			MeetingAssignment ma = (MeetingAssignment)o;
			return ma.getClassMeetingId().equals(getClassMeetingId()) && ma.getFirstHour() == getFirstHour();
		}
		
		public boolean overlaps(TeachingSchedule offering, MeetingAssignment other) {
			TeachingMeeting m1 = offering.getMeeting(this);
			TeachingMeeting m2 = offering.getMeeting(other);
			return (m1.getDayOfYear() == m2.getDayOfYear() && getMeetingTime(m1).overlaps(other.getMeetingTime(m2)));
		}
	}
	
	public static class InstructorMeetingAssignment implements IsSerializable, Comparable<InstructorMeetingAssignment> {
		private String iName;
		private String iType;
		private String iDivision;
		private String iNote;
		private String iGroup;
		private Long iClassMeetingId;
		private String iMeetingDate;
		private Date iMeetingTime;
		private int iDayOfYear;
		private MeetingTime iHours;
		private String iLocation;
		private int iLoad = 0;
		private String iAttributeRef;
		
		public InstructorMeetingAssignment() {}
		
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public String getType() { return iType; }
		public void setType(String type) { iType = type; }
		public String getDivision() { return iDivision; }
		public void setDivision(String division) { iDivision = division; }
		public String getNote() { return iNote; }
		public void setNote(String note) { iNote = note; }
		public String getGroup() { return iGroup; }
		public void setGroup(String group) { iGroup = group; }
		public Long getClassMeetingId() { return iClassMeetingId; }
		public void setClassMeetingId(Long id) { iClassMeetingId = id; }
		public Date getMeetingTime() { return iMeetingTime; }
		public void setMeetingTime(Date date) { iMeetingTime = date; }
		public String getMeetingDate() { return iMeetingDate; }
		public void setMeetingDate(String formattedDate) { iMeetingDate = formattedDate; }
		public int getDayOfYear() { return iDayOfYear; }
		public void setDayOfYear(int day) { iDayOfYear = day; }
		public String getLocation() { return iLocation == null ? "" : iLocation; }
		public void setLocation(String loc) { iLocation = loc; }
		public MeetingTime getHours() { return iHours; }
		public void setHours(MeetingTime hours) { iHours = hours; }
		public int getLoad() { return iLoad; }
		public void setLoad(int load) { iLoad = load; }
		public String getAttributeRef() { return iAttributeRef; }
		public void setAttributeRef(String attribute) { iAttributeRef = attribute; }

		@Override
		public int compareTo(InstructorMeetingAssignment a) {
			if (getDayOfYear() != a.getDayOfYear()) return getDayOfYear() < a.getDayOfYear() ? -1 : 1;
			if (!getMeetingTime().equals(a.getMeetingTime())) return getMeetingTime().compareTo(a.getMeetingTime());
			if (getGroup() != null && a.getGroup() != null) {
				int cmp = getGroup().compareTo(a.getGroup());
				if (cmp != 0) return cmp;
			}
			return getClassMeetingId().compareTo(a.getClassMeetingId());
		}
	}
	
	public static enum ErrorType implements IsSerializable {
		DIVISION_HOURS_BELOW, DIVISION_HOURS_ABOVE, DIVISION_NO_GROUPS,
		DIVISION_NO_NAME, DIVISION_NO_HOURS,
		CLAZZ_NO_DIVISION, CLAZZ_DIVISION_HOURS_BELOW, CLAZZ_DIVISION_HOURS_ABOVE,
		INSTRUCTOR_NOT_AVAILABLE_DAY, INSTRUCTOR_PROHIBITED_TIME, INSTRUCTOR_OTHER_ASSIGNMENT, INSTRUCTOR_CONFLICT,
		PARALLELS_TOO_MANY, PARALLELS_TOO_FEW,
		INSTRUCTOR_HIGH_LOAD, INSTRUCTOR_NO_ATTRIBUTE
		;
	}
	
	public static class ValidationError implements IsSerializable {
		private ErrorType iType;
		private CourseGroup iGroup;
		private String iValue;
		private CourseDivision iDivision = null;
		private MeetingAssignment iMeetingAssignment = null;
		private Clazz iClazz = null;
		private TeachingMeeting iMeeting = null;
		private Instructor iInstructor = null;
		private MeetingTime iMeetingTime = null;
		private InstructorMeetingAssignment iOtherMeetingAssignment = null;
		private Clazz iConflictingClazz = null;
		private TeachingMeeting iConflictingMeeting = null;
		private MeetingAssignment iConflictingMeetingAssignment = null;
		private InstructorMeetingAssignment iInstructorAssignment = null;
		
		public ValidationError() {}
		
		public ValidationError(ErrorType type, Instructor instructor, InstructorMeetingAssignment ma, String value) {
			iType = type;
			iInstructor = instructor;
			iInstructorAssignment = ma;
			iValue = value;
		}
		
		public ValidationError(ErrorType type, Instructor instructor, InstructorMeetingAssignment ma) {
			this(type, instructor, ma, "");
		}
		
		public ValidationError(ErrorType type, Instructor instructor, InstructorMeetingAssignment ma, InstructorMeetingAssignment other) {
			this(type, instructor, ma, "");
			iOtherMeetingAssignment = other;
		}
		
		public ValidationError(ErrorType type, CourseGroup group, String value) {
			iType = type; iGroup = group; iValue = value;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, CourseDivision division) {
			iType = type; iGroup = group; iValue = value; iDivision = division;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz, MeetingAssignment assignment, TeachingMeeting meeting) {
			iType = type; iGroup = group; iValue = value; iDivision = assignment.getDivision(); iMeetingAssignment = assignment; iClazz = clazz; iMeeting = meeting;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz) {
			iType = type; iGroup = group; iValue = value; iClazz = clazz;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, CourseDivision division, Clazz clazz) {
			iType = type; iGroup = group; iValue = value; iDivision = division; iClazz = clazz;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz, MeetingAssignment assignment, TeachingMeeting meeting, Instructor instructor) {
			iType = type; iGroup = group; iValue = value; iDivision = assignment.getDivision(); iClazz = clazz; iMeetingAssignment = assignment; iInstructor = instructor; iMeeting = meeting;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz, MeetingAssignment assignment, TeachingMeeting meeting, Instructor instructor, MeetingTime time) {
			iType = type; iGroup = group; iValue = value; iDivision = assignment.getDivision(); iClazz = clazz; iMeetingAssignment = assignment; iInstructor = instructor; iMeetingTime = time; iMeeting = meeting;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz, MeetingAssignment assignment, TeachingMeeting meeting, Instructor instructor, InstructorMeetingAssignment other) {
			iType = type; iGroup = group; iValue = value; iDivision = assignment.getDivision(); iClazz = clazz; iMeetingAssignment = assignment; iInstructor = instructor; iOtherMeetingAssignment = other; iMeeting = meeting;
		}
		public ValidationError(ErrorType type, CourseGroup group, String value, Clazz clazz, MeetingAssignment assignment, TeachingMeeting meeting, Instructor instructor, Clazz conflict, TeachingMeeting confMeeting, MeetingAssignment other) {
			iType = type; iGroup = group; iValue = value; iDivision = assignment.getDivision(); iClazz = clazz; iMeetingAssignment = assignment; iInstructor = instructor;
			iConflictingMeeting = confMeeting; iConflictingMeetingAssignment = other; iMeeting = meeting; iConflictingClazz = conflict;
		}
		
		public ErrorType getType() { return iType; }
		public void setType(ErrorType type) { iType = type; }
		
		public CourseGroup getGroup() { return iGroup; }
		public void setGroup(CourseGroup group) { iGroup = group; }
		
		public String getValue() { return iValue; }
		public void setValue(String value) { iValue = value; }
		
		public CourseDivision getDivision() { return iDivision; }
		public void setDivision(CourseDivision division) { iDivision = division; }
		
		public MeetingAssignment getMeetingAssignment() { return iMeetingAssignment; }
		public void setMeetingAssignment(MeetingAssignment assignment) { iMeetingAssignment = assignment; }
		
		public Clazz getClazz() { return iClazz; }
		public void setClazz(Clazz clazz) { iClazz = clazz; }
		
		public Instructor getInstructor() { return iInstructor; }
		public void setInstructor(Instructor instructor) { iInstructor = instructor; }
		
		public TeachingMeeting getMeeting() { return iMeeting; }
		public void setMeeting(TeachingMeeting meeting) { iMeeting = meeting; }
		
		public MeetingTime getMeetingTime() { return iMeetingTime; }
		public void setMeetingTime(MeetingTime time) { iMeetingTime = time; }
		
		public InstructorMeetingAssignment getOtherMeetingAssignment() { return iOtherMeetingAssignment; }
		public void setOtherMeetingAssignment(InstructorMeetingAssignment other) { iOtherMeetingAssignment = other; }
		
		public TeachingMeeting getConflictingMeeting() { return iConflictingMeeting; }
		public void setConflictingMeeting(TeachingMeeting meeting) { iConflictingMeeting = meeting; }
		
		public MeetingAssignment getConflictingMeetingAssignment() { return iConflictingMeetingAssignment; }
		public void setConflictingMeetingAssignment(MeetingAssignment ma) { iConflictingMeetingAssignment = ma; }
		
		public Clazz getConflictingClazz() { return iConflictingClazz; }
		public void setConflictingClazz(Clazz clazz) { iConflictingClazz = clazz; }
		
		public String getFormattedMeetingTime(GwtConstants constants) {
			if (getInstructorAssignment() != null)
				return getInstructorAssignment().getName() + " " + getInstructorAssignment().getGroup() + " " +
						getInstructorAssignment().getMeetingDate() + " " + slot2time(constants.useAmPm(), getInstructorAssignment().getHours().getStartSlot()) + " - " + slot2time(constants.useAmPm(), getInstructorAssignment().getHours().getEndSlot());
			return getMeeting().getMeetingDate() + " " + slot2time(constants.useAmPm(), getMeeting().getHour(getMeetingAssignment().getFirstHour()).getStartSlot()) + " - " + slot2time(constants.useAmPm(), getMeeting().getHour(getMeetingAssignment().getLastHour()).getEndSlot());
		}
		
		public String getFormattedConflictingMeetingTime(GwtConstants constants) {
			if (getInstructorAssignment() != null)
				return slot2time(constants.useAmPm(), getInstructorAssignment().getHours().getStartSlot()) + " - " + slot2time(constants.useAmPm(), getInstructorAssignment().getHours().getEndSlot());
			return getConflictingMeeting().getMeetingDate() + " " +
					slot2time(constants.useAmPm(), getConflictingMeeting().getHour(getConflictingMeetingAssignment().getFirstHour()).getStartSlot()) + " - " +
					slot2time(constants.useAmPm(), getConflictingMeeting().getHour(getConflictingMeetingAssignment().getLastHour()).getEndSlot());
		}
		
		public InstructorMeetingAssignment getInstructorAssignment() { return iInstructorAssignment; }
		public void setInstructorAssignment(InstructorMeetingAssignment ia) { iInstructorAssignment = ia; }
		
		public String toString(TeachingScheduleMessages messages, GwtConstants constants) {
			switch (getType()) {
			case DIVISION_HOURS_ABOVE:
				return messages.errorDivisionTooManyHours(getGroup().getTypeAndConfig(), getValue());
			case DIVISION_HOURS_BELOW:
				return messages.errorDivisionTooLittleHours(getGroup().getTypeAndConfig(), getValue());
			case DIVISION_NO_GROUPS:
				return messages.errorDivisionNoGroups(getGroup().getTypeAndConfig());
			case DIVISION_NO_NAME:
				return messages.errorDivisionNoName(getDivision().getDivisionIndex() + 1, getGroup().getTypeAndConfig());
			case DIVISION_NO_HOURS:
				return messages.errorDivisionNoHours(getDivision().getDivisionIndex() + 1, getGroup().getTypeAndConfig());
			case CLAZZ_NO_DIVISION:
				return messages.errorClassHasNoDivision(getFormattedMeetingTime(constants));
			case CLAZZ_DIVISION_HOURS_BELOW:
				return messages.errorClassTooLittleHours(getDivision().getName(), getValue());
			case CLAZZ_DIVISION_HOURS_ABOVE:
				return messages.errorClassTooManyHours(getDivision().getName(), getValue());
			case INSTRUCTOR_NOT_AVAILABLE_DAY:
				if (getInstructorAssignment() != null)
					return messages.errorInstructorNotAvailable(getInstructor().getName(), getInstructorAssignment().getMeetingDate());
				return messages.errorInstructorNotAvailable(getInstructor().getName(), getMeeting().getMeetingDate());
			case INSTRUCTOR_PROHIBITED_TIME:
				return messages.errorInstructorProhibited(getInstructor().getName(), getFormattedMeetingTime(constants));
			case INSTRUCTOR_OTHER_ASSIGNMENT:
				return messages.errorInstructorTeachingConflict(getInstructor().getName(), getFormattedMeetingTime(constants), getOtherMeetingAssignment().getName());
			case INSTRUCTOR_CONFLICT:
				return messages.errorInstructorTeachingConflict(getInstructor().getName(), getFormattedMeetingTime(constants), getConflictingClazz().getName() + " " + getFormattedConflictingMeetingTime(constants));
			case PARALLELS_TOO_FEW:
				return messages.errorClassTooLittleGroups(getMeetingAssignment().getDivision().getName(), getFormattedMeetingTime(constants), getValue());
			case PARALLELS_TOO_MANY:
				return messages.errorClassTooManyGroups(getMeetingAssignment().getDivision().getName(), getFormattedMeetingTime(constants), getValue());
			case INSTRUCTOR_HIGH_LOAD:
				return messages.errorInstructorOverload(getInstructor().getName(), getFormattedMeetingTime(constants), getValue());
			case INSTRUCTOR_NO_ATTRIBUTE:
				return messages.errorInstructorNoAttribute(getInstructor().getName(), getFormattedMeetingTime(constants), getValue());
				
			}
			return getType().name();
		}
	}
	
	public static interface Validator {
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors);
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors);
	}
	
	public static class CourseDivisionHoursValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			int hours = 0;
			if (group.hasDivisions())
				for (CourseDivision division: group.getDivisions()) {
					hours += division.getHours();
				}
			if (hours < group.getHours())
				errors.add(new ValidationError(ErrorType.DIVISION_HOURS_BELOW, group, hours + " < " + group.getHours()));
			if (hours > group.getHours())
				errors.add(new ValidationError(ErrorType.DIVISION_HOURS_ABOVE, group, hours + " > " + group.getHours()));
			if (group.getNrGroups() == 0)
				errors.add(new ValidationError(ErrorType.DIVISION_NO_GROUPS, group, ""));
		}
	}
	
	public static class CourseDivisionNameValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions())
				for (CourseDivision division: group.getDivisions()) {
					if (division.getName() == null || division.getName().isEmpty())
						errors.add(new ValidationError(ErrorType.DIVISION_NO_NAME, group, "", division));
					if (division.getHours() == 0)
						errors.add(new ValidationError(ErrorType.DIVISION_NO_HOURS, group, "", division));
				}
		}
	}
	
	public static class DivisionAssignmentValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions())
				for (int c = 0; c < group.getNrClasses(); c++) {
					for (int g = 0; g < group.getNrGroups(); g++) {
						validate(offering, group, offering.getClass(group, c, g), errors);
					}
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Clazz clazz, Collection<ValidationError> errors) {
			int[] hours = new int[group.getDivisions().size()];
			for (int i = 0; i < hours.length; i++)
				hours[i] = 0;
			for (MeetingAssignment ma: clazz.getMeetingAssignments()) {
				CourseDivision d = ma.getDivision();
				if (d == null) {
					errors.add(new ValidationError(ErrorType.CLAZZ_NO_DIVISION, group, "", clazz, ma, offering.getMeeting(ma)));
					continue;
				} else {
					hours[d.getDivisionIndex()] += (ma.getLastHour() - ma.getFirstHour() + 1);
				}
			}
			for (int i = 0; i < hours.length; i++) {
				CourseDivision div = group.getDivisions().get(i);
				if (hours[i] < div.getHours())
					errors.add(new ValidationError(ErrorType.CLAZZ_DIVISION_HOURS_BELOW, group, hours[i] + " < " + div.getHours(), div, clazz));
				if (hours[i] > div.getHours())
					errors.add(new ValidationError(ErrorType.CLAZZ_DIVISION_HOURS_ABOVE, group, hours[i] + " > " + div.getHours(), div, clazz));
			}
		}
	}
	
	public static class InstructorAvailabilityValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions())
				for (int c = 0; c < group.getNrClasses(); c++) {
					for (int g = 0; g < group.getNrGroups(); g++) {
						validate(offering, group, offering.getClass(group, c, g), errors);
					}
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Clazz clazz, Collection<ValidationError> errors) {
			for (MeetingAssignment ma: clazz.getMeetingAssignments()) {
				TeachingMeeting m = offering.getMeeting(ma);
				if (ma.hasInstructors()) {
					instructors: for (Long instructorId: ma.getInstructor()) {
						Instructor instructor = offering.getInstructor(instructorId);
						if (instructor.iUnavailableDays.containsKey(m.getDayOfYear())) {
							errors.add(new ValidationError(ErrorType.INSTRUCTOR_NOT_AVAILABLE_DAY, group, "", clazz, ma, m, instructor));
							continue instructors;
						}
						for (int i = ma.getFirstHour(); i <= ma.getLastHour(); i++) {
							for (MeetingTime mt: instructor.iProhibitedTimes) {
								if (mt.overlaps(m.getHour(i))) {
									errors.add(new ValidationError(ErrorType.INSTRUCTOR_PROHIBITED_TIME, group, "", clazz, ma, m, instructor, mt));
									continue instructors;		
								}
							}
							for (InstructorMeetingAssignment a: instructor.getAssignmetns()) {
								if (a.getDayOfYear() == m.getDayOfYear() && a.getHours().overlaps(m.getHour(i))) {
									errors.add(new ValidationError(ErrorType.INSTRUCTOR_OTHER_ASSIGNMENT, group, "", clazz, ma, m, instructor, a));
									continue instructors;
								}
							}
						}
						for (Clazz c: offering.getClasses()) {
							for (MeetingAssignment other: c.getMeetingAssignments()) {
								if (other.hasInstructor(instructor) && (!other.equals(ma) || (other.equals(ma) && other.getDivisionIndex() != ma.getDivisionIndex())) && ma.overlaps(offering, other)) {
									errors.add(new ValidationError(ErrorType.INSTRUCTOR_CONFLICT, group, "", clazz, ma, m, instructor, c, offering.getMeeting(other), other));
								}
							}
						}
					}
				}
			}
		}
		
		public void validate(Instructor instructor, Collection<ValidationError> errors) {
			for (InstructorMeetingAssignment ma: instructor.getAssignmetns()) {
				if (ma.getDivision() == null) continue;
				if (instructor.iUnavailableDays.containsKey(ma.getDayOfYear())) {
					errors.add(new ValidationError(ErrorType.INSTRUCTOR_NOT_AVAILABLE_DAY, instructor, ma));
				}
				for (MeetingTime mt: instructor.iProhibitedTimes) {
					if (mt.overlaps(ma.getHours())) {
						errors.add(new ValidationError(ErrorType.INSTRUCTOR_PROHIBITED_TIME, instructor, ma));
					}
				}
				for (InstructorMeetingAssignment a: instructor.getAssignmetns()) {
					if (a.getDayOfYear() == ma.getDayOfYear() && a.getHours().overlaps(ma.getHours())) {
						if (a.getClassMeetingId().equals(ma.getClassMeetingId()) && (a.getDivision() == null ? "" : a.getDivision()).equals(ma.getDivision() == null ? "" : ma.getDivision()))
							continue;
						errors.add(new ValidationError(ErrorType.INSTRUCTOR_OTHER_ASSIGNMENT, instructor, ma, a));
					}
				}
			}
		}
	}
	
	public static class ParallelValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions())
				for (int c = 0; c < group.getNrClasses(); c++) {
					if (group.getNrGroups() > 0)
						validate(offering, group, c, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Clazz clazz, Collection<ValidationError> errors) {
			m: for (TeachingMeeting m: group.getMeetings(clazz.getClassIndex())) {
				for (int hour = 0; hour < m.getHours().size(); hour++) {
					MeetingAssignment ma = clazz.getMeetingAssignment(m, hour);
					if (ma.getDivision() != null) {
						int nrDivs = 1;
						for (int groupIndex = 0; groupIndex < group.getNrGroups(); groupIndex++) {
							if (groupIndex != clazz.getGroupIndex()) {
								MeetingAssignment x = offering.getClass(group, clazz.getClassIndex(), groupIndex).getMeetingAssignment(m, hour);
								if (ma.getDivision().equals(x.getDivision())) nrDivs ++;
							}
						}
						if (nrDivs > ma.getDivision().getNrParalel()) {
							errors.add(new ValidationError(ErrorType.PARALLELS_TOO_MANY, group, nrDivs + " > " + ma.getDivision().getNrParalel(), clazz, ma, offering.getMeeting(ma)));
							continue m;
						}
						if (nrDivs < ma.getDivision().getNrParalel()) {
							errors.add(new ValidationError(ErrorType.PARALLELS_TOO_FEW, group, nrDivs + " < " + ma.getDivision().getNrParalel(), clazz, ma, offering.getMeeting(ma)));
							continue m;
						}
					}
				}
			}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, int classIndex, Collection<ValidationError> errors) {
			for (TeachingMeeting m: group.getMeetings(classIndex)) {
				for (int hour = 0; hour < m.getHours().size(); hour++) {
					List<Clazz>[] nrDivs = new List[group.getDivisions().size()];
					for (int i = 0; i < nrDivs.length; i++) nrDivs[i] = new ArrayList<Clazz>();
					for (int groupIndex = 0; groupIndex < group.getNrGroups(); groupIndex++) {
						Clazz clazz = offering.getClass(group, classIndex, groupIndex);
						MeetingAssignment ma = clazz.getMeetingAssignment(m, hour);
						if (ma.getDivision() != null) nrDivs[ma.getDivision().getDivisionIndex()].add(clazz);
					}
					for (CourseDivision div: group.getDivisions()) {
						if (nrDivs[div.getDivisionIndex()].size() > div.getNrParalel()) {
							for (int i = div.getNrParalel(); i < nrDivs[div.getDivisionIndex()].size(); i++) {
								Clazz clazz = nrDivs[div.getDivisionIndex()].get(i);
								MeetingAssignment ma = clazz.getMeetingAssignment(m, hour);
								errors.add(new ValidationError(ErrorType.PARALLELS_TOO_MANY, group, "", clazz, ma, offering.getMeeting(ma)));
							}
						}
						if (nrDivs[div.getDivisionIndex()].size() < div.getNrParalel()) {
							for (int i = 0; i < nrDivs[div.getDivisionIndex()].size(); i++) {
								Clazz clazz = nrDivs[div.getDivisionIndex()].get(i);
								MeetingAssignment ma = clazz.getMeetingAssignment(m, hour);
								errors.add(new ValidationError(ErrorType.PARALLELS_TOO_FEW, group, "", clazz, ma, offering.getMeeting(ma)));
							}
						}
					}
				}
			}
		}
	}
	
	public static class InstructorLoadValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions())
				for (int c = 0; c < group.getNrClasses(); c++) {
					for (int g = 0; g < group.getNrGroups(); g++) {
						validate(offering, group, offering.getClass(group, c, g), errors);
					}
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Clazz clazz, Collection<ValidationError> errors) {
			for (MeetingAssignment ma: clazz.getMeetingAssignments()) {
				TeachingMeeting m = offering.getMeeting(ma);
				if (ma.hasInstructors()) {
					for (Long instructorId: ma.getInstructor()) {
						Instructor instructor = offering.getInstructor(instructorId);
						Set<String> checked = new HashSet<String>();
						checked.add(ma.getClassMeetingId() + ":" + ma.getFirstHour() + ":" + ma.getLastHour());
						int load = ma.getLastHour() - ma.getFirstHour() + 1;
						for (InstructorMeetingAssignment other: instructor.getAssignmetns()) {
							if (checked.add(other.getClassMeetingId() + ":" + other.getHours().getStartSlot() + ":" + other.getHours().getEndSlot()))
								load += other.getLoad();
						}
						for (Clazz c: offering.getClasses()) {
							for (MeetingAssignment other: c.getMeetingAssignments()) {
								if (other.hasInstructor(instructor) && checked.add(other.getClassMeetingId() + ":" + other.getFirstHour() + ":" + other.getLastHour()))
									load += other.getLastHour() - other.getFirstHour() + 1;
							}
						}
						if (load > instructor.getMaxLoad()) {
							errors.add(new ValidationError(ErrorType.INSTRUCTOR_HIGH_LOAD, group, load + " > " + instructor.getMaxLoad(), clazz, ma, m, instructor));
						}
					}
				}
			}
		}
		
		public void validate(Instructor instructor, Collection<ValidationError> errors) {
			int total = 0, load = 0;
			Set<String> checked = new HashSet<String>();
			for (InstructorMeetingAssignment ma: instructor.getAssignmetns()) {
				String id = ma.getClassMeetingId() + ":" + ma.getHours().getStartSlot() + ":" + ma.getHours().getEndSlot();
				if (checked.add(id)) {
					total += ma.getLoad();
					if (ma.getDivision() == null)
						load += ma.getLoad();
				}
			}
			if (total > instructor.getMaxLoad()) {
				checked.clear();
				for (InstructorMeetingAssignment ma: instructor.getAssignmetns()) {
					if (ma.getDivision() == null) continue;
					String id = ma.getClassMeetingId() + ":" + ma.getHours().getStartSlot() + ":" + ma.getHours().getEndSlot();
					if (checked.add(id))
						load += ma.getLoad();
					if (load > instructor.getMaxLoad())
						errors.add(new ValidationError(ErrorType.INSTRUCTOR_HIGH_LOAD, instructor, ma, total + " > " + instructor.getMaxLoad()));
				}
			}
		}
	}
	
	public static class InstructorAttributeValidator implements Validator {
		@Override
		public void validate(TeachingSchedule offering, Collection<ValidationError> errors) {
			if (offering.hasGroups())
				for (CourseGroup group: offering.getGroups()) {
					validate(offering, group, errors);
				}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Collection<ValidationError> errors) {
			if (group.hasDivisions()) {
				for (int c = 0; c < group.getNrClasses(); c++) {
					for (int g = 0; g < group.getNrGroups(); g++) {
						validate(offering, group, offering.getClass(group, c, g), errors);
					}
				}
			}
		}
		
		public void validate(TeachingSchedule offering, CourseGroup group, Clazz clazz, Collection<ValidationError> errors) {
			for (MeetingAssignment ma: clazz.getMeetingAssignments()) {
				TeachingMeeting m = offering.getMeeting(ma);
				if (ma.hasInstructors()) {
					for (Long instructorId: ma.getInstructor()) {
						Instructor instructor = offering.getInstructor(instructorId);
						if (ma.hasAttributeRef() && instructor.getAttribute(ma.getAttributeRef()) == null)
							errors.add(new ValidationError(ErrorType.INSTRUCTOR_NO_ATTRIBUTE, group, ma.getAttributeRef(), clazz, ma, m, instructor));
					}
				}
			}
		}
		
		public void validate(Instructor instructor, Collection<ValidationError> errors) {
			for (InstructorMeetingAssignment ma: instructor.getAssignmetns())
				if (ma.getAttributeRef() != null && instructor.getAttribute(ma.getAttributeRef()) == null)
					errors.add(new ValidationError(ErrorType.INSTRUCTOR_NO_ATTRIBUTE, instructor, ma, ma.getAttributeRef()));
		}
	}

	public static String slot2time(boolean useAmPm, int slot) {
		int timeSinceMidnight = 5 * slot;
		int hour = timeSinceMidnight / 60;
	    int min = timeSinceMidnight % 60;
	    if (useAmPm)
	    	return (hour==0?12:hour>12?hour-12:hour)+":"+(min<10?"0":"")+min+(hour<24 && hour>=12?"p":"a");
	    else
	    	return hour + ":" + (min < 10 ? "0" : "") + min;
	}
}
