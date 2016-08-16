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
package org.unitime.timetable.server.instructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.coursett.preference.PreferenceCombination;
import org.cpsolver.coursett.preference.SumPreferenceCombination;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.instructor.model.EnrolledClass;
import org.cpsolver.instructor.model.Preference;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.CourseInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionsResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.model.dao.TeachingRequestDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.instructor.InstructorSchedulingDatabaseLoader;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingBackendHelper {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
    
    protected CourseInfo getCourse(CourseOffering course) {
    	CourseInfo info = new CourseInfo();
    	info.setCourseId(course.getUniqueId());
    	info.setCourseName(course.getCourseName());
    	return info;
    }
    
    protected SectionInfo getSection(TeachingClassRequest r) {
    	Class_ clazz = r.getTeachingClass();
    	SectionInfo section = new SectionInfo();
		CourseOffering course = clazz.getSchedulingSubpart().getControllingCourseOffering();
		String room = null;
		TimeLocation time = null;
		org.unitime.timetable.model.Assignment assignment = clazz.getCommittedAssignment();
		if (assignment != null) {
			time = assignment.getTimeLocation();
			for (Location location: assignment.getRooms()) {
				if (room == null) room = location.getLabel();
				else room += ", " + location.getLabel();
			}
		}
		section.setSectionId(clazz.getUniqueId());
		section.setExternalId(InstructorSchedulingDatabaseLoader.getClassExternalId(course, clazz));
		section.setSectionName(clazz.getClassLabel(course));
		section.setSectionType(clazz.getSchedulingSubpart().getItypeDesc().trim());
		section.setCommon(r.isCommon());
		section.setTime(time != null ? time.getDayHeader() + " " + time.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + time.getEndTimeHeader(CONSTANTS.useAmPm()) : null);
		section.setDate(time != null ? time.getDatePatternName() : null);
		section.setRoom(room);
		return section;
    }
    
    protected InstructorInfo getInstructor(TeachingRequestInfo request, DepartmentalInstructor instructor, String nameFormat) {
		InstructorInfo info = new InstructorInfo();
		info.setInstructorId(instructor.getUniqueId());
		info.setInstructorName(instructor.getName(nameFormat));
		info.setExternalId(instructor.getExternalUniqueId());
		info.setMaxLoad(instructor.getMaxLoad() == null ? 0f : instructor.getMaxLoad());
		if (instructor.getTeachingPreference() == null) {
			info.setTeachingPreference(PreferenceLevel.sProhibited);
			info.setValue("Teaching Preferences", Constants.preference2preferenceLevel(PreferenceLevel.sProhibited));
		} else {
			info.setTeachingPreference(instructor.getTeachingPreference().getPrefProlog());
			if (!PreferenceLevel.sNeutral.equals(info.getTeachingPreference()))
				info.setValue("Teaching Preferences", Constants.preference2preferenceLevel(info.getTeachingPreference()));
		}
		for (InstructorAttribute a: instructor.getAttributes()) {
			AttributeInterface attribute = new AttributeInterface();
			attribute.setId(a.getUniqueId());
			attribute.setName(a.getName());
			AttributeTypeInterface type = new AttributeTypeInterface();
			type.setId(a.getType().getUniqueId());
			type.setLabel(a.getType().getLabel());
			type.setConjunctive(a.getType().isConjunctive());
			type.setRequired(a.getType().isRequired());
			attribute.setType(type);
			info.addAttribute(attribute);
		}
		int[][] slot2pref = new int[Constants.NR_DAYS * Constants.SLOTS_PER_DAY][];
		for (int i = 0; i < slot2pref.length; i++)
			slot2pref[i] = new int[] {0, 0, 0};
		for (org.unitime.timetable.model.Preference p: instructor.getPreferences()) {
			if (p instanceof InstructorCoursePref) {
				InstructorCoursePref cp = (InstructorCoursePref)p;
				info.addCoursePreference(new PreferenceInfo(cp.getCourse().getUniqueId(), cp.getCourse().getCourseName(), cp.getPrefLevel().getPrefProlog()));
				if (request != null && cp.getCourse().getUniqueId().equals(request.getCourse().getCourseId()))
					info.setValue("Course Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
			} else if (p instanceof DistributionPref) {
				DistributionPref dp = (DistributionPref)p;
				info.addDistributionPreference(new PreferenceInfo(dp.getDistributionType().getUniqueId(), dp.getDistributionType().getLabel(), dp.getPrefLevel().getPrefProlog()));
			} else if (p instanceof TimePref) {
				TimePref tp = (TimePref)p;
				info.setAvailability(tp.getPreference());
				for (Preference<TimeLocation> pf: loadTimePreferences((TimePref)p)) {
					PreferenceInfo pi = new PreferenceInfo(new Long(pf.getTarget().hashCode()), pf.getTarget().getLongName(CONSTANTS.useAmPm()), Constants.preferenceLevel2preference(pf.getPreference()));
					pi.setComparable(String.format("%03d:%05d", pf.getTarget().getDayCode(), pf.getTarget().getStartSlot()));
					info.addTimePreference(pi);
					for (Enumeration<Integer> i = pf.getTarget().getSlots(); i.hasMoreElements(); ) {
						int slot = i.nextElement();
						slot2pref[slot][0] = Math.min(slot2pref[slot][0], pf.getPreference());
						slot2pref[slot][1] = Math.max(slot2pref[slot][1], pf.getPreference());
					}
				}
			}
		}
		for (EnrolledClass ec: InstructorSchedulingDatabaseLoader.loadUnavailability(Class_DAO.getInstance().getSession(), instructor)) {
			PreferenceInfo pi = new PreferenceInfo(ec.getClassId(), ec.getLongName(CONSTANTS.useAmPm()), Constants.sPreferenceProhibited);
			pi.setComparable(String.format("%03d:%05d", ec.getDayCode(), ec.getStartSlot()));
			info.addTimePreference(pi);
			for (Enumeration<Integer> i = ec.getSlots(); i.hasMoreElements(); ) {
				int slot = i.nextElement();
				slot2pref[slot][0] = Math.min(slot2pref[slot][0], Constants.sPreferenceLevelProhibited);
				slot2pref[slot][1] = Math.max(slot2pref[slot][1], Constants.sPreferenceLevelProhibited);
				slot2pref[slot][2] = 1;
			}
			ClassInfo ci = new ClassInfo();
			ci.setCourseId(ec.getCourseId()); ci.setCourse(ec.getCourse());
			ci.setClassId(ec.getClassId()); ci.setSection(ec.getSection());
			ci.setExternalId(ec.getExternalId()); ci.setType(ec.getType());
			ci.setInstructor(ec.isInstructor()); ci.setRoom(ec.getRoom());
			ci.setTime(ec.getDayHeader() + " " + ec.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + ec.getEndTimeHeader(CONSTANTS.useAmPm()));
			ci.setDate(ec.getDatePatternName());
			info.addEnrollment(ci);
		}
		StringBuffer pattern = new StringBuffer(slot2pref.length);
		for (int i = 0; i < slot2pref.length; i++) {
			int min = slot2pref[i][0];
			int max = slot2pref[i][1];
			int pref = (max > -min ? max : -min > max ? min : max);
			if (slot2pref[i][2] == 1)
				pattern.append(PreferenceLevel.prolog2char(PreferenceLevel.sNotAvailable));
			else
				pattern.append(PreferenceLevel.prolog2char(Constants.preferenceLevel2preference(pref)));
		}
		info.setAvailability(pattern.toString());
		return info;
    }
    
    protected List<Preference<TimeLocation>> loadTimePreferences(TimePref tp) {
    	List<Preference<TimeLocation>> ret = new ArrayList<Preference<TimeLocation>>();
		TimePatternModel m = tp.getTimePatternModel();
		boolean out[][] = new boolean [m.getNrDays()][m.getNrTimes()];
        for (int i = 0; i < m.getNrDays(); i++)
               for (int j = 0; j < m.getNrTimes(); j++)
            	   out[i][j] = false;
        for (int i = 0; i < m.getNrDays(); i++)
            for (int j = 0; j < m.getNrTimes(); j++) {
         	   if (out[i][j]) continue;
         	   out[i][j] = true;
         	   if (PreferenceLevel.sNeutral.equals(m.getPreference(i, j))) continue;
         	   int endDay = i, endTime = j;
         	   while (endTime + 1 < m.getNrTimes() && !out[i][endTime+1] && m.getPreference(i, endTime+1).equals(m.getPreference(i, j)))
         		   endTime++;
         	   if (i == 0) {
         		   boolean same = true;
         		   for (int k = i; k + 1 < m.getNrDays(); k++) {
             		   for (int x = j; x <= endTime; x++) {
             			   if (!out[k+1][x] && !m.getPreference(i, x).equals(m.getPreference(k+1, x))) {
             				   same = false; break;
             			   }
             			   if (!same) break;
             		   }
             		   if (!same) break;
         		   }
         		   if (same) endDay = m.getNrDays()-1;
         	   }
         	   while (endDay + 1 < m.getNrDays()) {
         		   boolean same = true;
         		   for (int x = j; x <= endTime; x++)
         			   if (!out[endDay+1][x] && !m.getPreference(i, x).equals(m.getPreference(endDay+1, x))) {
         				   same = false; break;
         			   }
         		   if (!same) break;
         		   endDay++;
         	   }
         	   for (int a = i; a <= endDay;a++)
         		   for (int b = j; b <= endTime;b++)
         			   out[a][b] = true;

         	   int dayCode = 0;
         	   for (int a = i; a <= endDay; a++)
         		   dayCode |= m.getDayCode(a);
         	   TimeLocation time =  new TimeLocation(dayCode, m.getStartSlot(j), m.getStartSlot(endTime) - m.getStartSlot(j) + m.getSlotsPerMtg(), 0, 0.0, null, "", null, m.getBreakTime());
         	   ret.add(new Preference<TimeLocation>(time, Constants.preference2preferenceLevel(m.getPreference(i, j))));
            }
        return ret;
    }
    
	protected TeachingRequestInfo getRequest(TeachingRequest tr, String nameFormat, InstructorSchedulingProxy solver) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
    	TeachingRequestInfo request = new TeachingRequestInfo();
    	request.setRequestId(tr.getUniqueId());
    	request.setNrInstructors(tr.getNbrInstructors());
    	request.setCourse(getCourse(tr.getOffering().getControllingCourseOffering()));
    	for (TeachingClassRequest tcr: new TreeSet<TeachingClassRequest>(tr.getClassRequests()))
    		request.addSection(getSection(tcr));
    	request.setLoad(tr.getTeachingLoad());
		for (Iterator it = tr.getPreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
			InstructorPref p = (InstructorPref)it.next();
			request.addInstructorPreference(new PreferenceInfo(p.getInstructor().getUniqueId(), p.getInstructor().getName(nameFormat), p.getPrefLevel().getPrefProlog()));
		}
		for (Iterator it = tr.getPreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
			InstructorAttributePref p = (InstructorAttributePref)it.next();
			request.addAttributePreference(new PreferenceInfo(p.getAttribute().getUniqueId(), p.getAttribute().getName(), p.getPrefLevel().getPrefProlog()));
		}
		int index = 0;
		TreeSet<DepartmentalInstructor> instructors = new TreeSet<DepartmentalInstructor>();
		if (solver != null) {
			TeachingRequestInfo info = solver.getTeachingRequestInfo(tr.getUniqueId());
			if (info != null && info.hasInstructors())
				for (InstructorInfo i: info.getInstructors()) {
					DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(i.getInstructorId(), hibSession);
					if (instructor != null)
						instructors.add(instructor);
				}
		} else {
			instructors.addAll(tr.getAssignedInstructors());
		}
		for (DepartmentalInstructor instructor: instructors) {
    		InstructorInfo info = getInstructor(request, instructor, nameFormat);
    		if (solver != null) {
    			InstructorInfo i = solver.getInstructorInfo(instructor.getUniqueId());
    			info.setAssignedLoad(i == null ? 0f : i.getAssignedLoad());
    		} else {
        		Number load = ((Number)hibSession.createQuery("select sum(r.teachingLoad) from TeachingRequest r inner join r.assignedInstructors i where i.uniqueId = :instructorId")
        				.setLong("instructorId", instructor.getUniqueId()).setCacheable(true).uniqueResult());
            	info.setAssignedLoad(load == null ? 0f : load.floatValue());
    		}
        	request.addInstructor(info);
        	for (Iterator it = tr.getPreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
    			InstructorPref p = (InstructorPref)it.next();
    			if (p.getInstructor().equals(instructor))
    				info.setValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
    		}
    		MinMaxPreferenceCombination attr = new MinMaxPreferenceCombination();
    		for (Iterator it = tr.getPreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
    			InstructorAttributePref p = (InstructorAttributePref)it.next();
    			if (instructor.getAttributes().contains(p.getAttribute()))
    				attr.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
    		}
    		info.setValue("Attribute Preferences", attr.getPreferenceInt());
    		info.setAssignmentIndex(index++);
    	}
		return request;
	}
	
	public InstructorInfo getInstructorInfo(DepartmentalInstructor instructor, String nameFormat) {
		InstructorInfo info = new InstructorInfo();
		info.setInstructorId(instructor.getUniqueId());
		info.setInstructorName(instructor.getName(nameFormat));
		info.setExternalId(instructor.getExternalUniqueId());
		info.setMaxLoad(instructor.getMaxLoad() == null ? 0f : instructor.getMaxLoad());
		for (InstructorAttribute a: instructor.getAttributes()) {
			AttributeInterface attribute = new AttributeInterface();
			attribute.setId(a.getUniqueId());
			attribute.setName(a.getName());
			AttributeTypeInterface type = new AttributeTypeInterface();
			type.setId(a.getType().getUniqueId());
			type.setLabel(a.getType().getLabel());
			type.setConjunctive(a.getType().isConjunctive());
			type.setRequired(a.getType().isRequired());
			attribute.setType(type);
			info.addAttribute(attribute);
		}
		int[][] slot2pref = new int[Constants.NR_DAYS * Constants.SLOTS_PER_DAY][];
		for (int i = 0; i < slot2pref.length; i++)
			slot2pref[i] = new int[] {0, 0, 0};
		for (org.unitime.timetable.model.Preference p: instructor.getPreferences()) {
			if (p instanceof InstructorCoursePref) {
				InstructorCoursePref cp = (InstructorCoursePref)p;
				info.addCoursePreference(new PreferenceInfo(cp.getCourse().getUniqueId(), cp.getCourse().getCourseName(), cp.getPrefLevel().getPrefProlog()));
			} else if (p instanceof DistributionPref) {
				DistributionPref dp = (DistributionPref)p;
				info.addDistributionPreference(new PreferenceInfo(dp.getDistributionType().getUniqueId(), dp.getDistributionType().getLabel(), dp.getPrefLevel().getPrefProlog()));
			} else if (p instanceof TimePref) {
				TimePref tp = (TimePref)p;
				info.setAvailability(tp.getPreference());
				for (Preference<TimeLocation> pf: loadTimePreferences((TimePref)p)) {
					PreferenceInfo pi = new PreferenceInfo(new Long(-pf.getTarget().hashCode()), pf.getTarget().getLongName(CONSTANTS.useAmPm()), Constants.preferenceLevel2preference(pf.getPreference()));
					pi.setComparable(String.format("%03d:%05d", pf.getTarget().getDayCode(), pf.getTarget().getStartSlot()));
					info.addTimePreference(pi);
					for (Enumeration<Integer> i = pf.getTarget().getSlots(); i.hasMoreElements(); ) {
						int slot = i.nextElement();
						slot2pref[slot][0] = Math.min(slot2pref[slot][0], pf.getPreference());
						slot2pref[slot][1] = Math.max(slot2pref[slot][1], pf.getPreference());
					}
				}
			}
		}
		for (EnrolledClass ec: InstructorSchedulingDatabaseLoader.loadUnavailability(Class_DAO.getInstance().getSession(), instructor)) {
				PreferenceInfo pi = new PreferenceInfo(ec.getClassId(), ec.getLongName(CONSTANTS.useAmPm()), Constants.sPreferenceProhibited);
			pi.setComparable(String.format("%03d:%05d", ec.getDayCode(), ec.getStartSlot()));
			info.addTimePreference(pi);
			for (Enumeration<Integer> i = ec.getSlots(); i.hasMoreElements(); ) {
				int slot = i.nextElement();
				slot2pref[slot][0] = Math.min(slot2pref[slot][0], Constants.sPreferenceLevelProhibited);
				slot2pref[slot][1] = Math.max(slot2pref[slot][1], Constants.sPreferenceLevelProhibited);
				slot2pref[slot][2] = 1;
			}
			ClassInfo ci = new ClassInfo();
			ci.setCourseId(ec.getCourseId()); ci.setCourse(ec.getCourse());
			ci.setClassId(ec.getClassId()); ci.setSection(ec.getSection());
			ci.setExternalId(ec.getExternalId()); ci.setType(ec.getType());
			ci.setInstructor(ec.isInstructor()); ci.setRoom(ec.getRoom());
			ci.setTime(ec.getDayHeader() + " " + ec.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + ec.getEndTimeHeader(CONSTANTS.useAmPm()));
			ci.setDate(ec.getDatePatternName());
			info.addEnrollment(ci);
		}
		StringBuffer pattern = new StringBuffer(slot2pref.length);
		for (int i = 0; i < slot2pref.length; i++) {
			int min = slot2pref[i][0];
			int max = slot2pref[i][1];
			int pref = (max > -min ? max : -min > max ? min : max);
			if (slot2pref[i][2] == 1)
				pattern.append(PreferenceLevel.prolog2char(PreferenceLevel.sNotAvailable));
			else
				pattern.append(PreferenceLevel.prolog2char(Constants.preferenceLevel2preference(pref)));
		}
		info.setAvailability(pattern.toString());
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		for (TeachingRequest tr: (List<TeachingRequest>)hibSession.createQuery("select r from TeachingRequest r inner join r.assignedInstructors i where i.uniqueId = :instructorId")
				.setLong("instructorId", instructor.getUniqueId()).setCacheable(true).list()) {
			TeachingRequestInfo request = getRequest(tr, info, nameFormat);
			if (request == null) continue;
			if (instructor.getTeachingPreference() == null) {
				info.setTeachingPreference(PreferenceLevel.sProhibited);
				request.setValue("Teaching Preferences", Constants.preference2preferenceLevel(PreferenceLevel.sProhibited));
				info.addValue("Teaching Preferences", Constants.preference2preferenceLevel(PreferenceLevel.sProhibited));
			} else {
				info.setTeachingPreference(instructor.getTeachingPreference().getPrefProlog());
				if (!PreferenceLevel.sNeutral.equals(info.getTeachingPreference())) {
					request.setValue("Teaching Preferences", Constants.preference2preferenceLevel(info.getTeachingPreference()));
					info.addValue("Teaching Preferences", Constants.preference2preferenceLevel(info.getTeachingPreference()));
				}
			}
			for (org.unitime.timetable.model.Preference p: instructor.getPreferences()) {
				if (p instanceof InstructorCoursePref) {
					InstructorCoursePref cp = (InstructorCoursePref)p;
					if (cp.getCourse().getUniqueId().equals(request.getCourse().getCourseId())) {
						info.addValue("Course Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
						request.setValue("Course Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
					}
				}
			}
			info.addAssignedRequest(request);
		}
		return info;
    }
    
	protected TeachingRequestInfo getRequest(TeachingRequest tr, InstructorInfo instructor, String nameFormat) {
    	TeachingRequestInfo request = new TeachingRequestInfo();
    	request.setRequestId(tr.getUniqueId());
    	request.setNrInstructors(tr.getNbrInstructors());
    	request.setCourse(getCourse(tr.getOffering().getControllingCourseOffering()));
    	for (TeachingClassRequest tcr: new TreeSet<TeachingClassRequest>(tr.getClassRequests()))
    		request.addSection(getSection(tcr));
    	request.setLoad(tr.getTeachingLoad());
		for (Iterator it = tr.getPreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
			InstructorPref p = (InstructorPref)it.next();
			request.addInstructorPreference(new PreferenceInfo(p.getInstructor().getUniqueId(), p.getInstructor().getName(nameFormat), p.getPrefLevel().getPrefProlog()));
			if (p.getInstructor().getUniqueId().equals(instructor.getInstructorId())) {
				request.setValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
				instructor.addValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
			}
		}
		MinMaxPreferenceCombination attr = new MinMaxPreferenceCombination();
		for (Iterator it = tr.getPreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
			InstructorAttributePref p = (InstructorAttributePref)it.next();
			request.addAttributePreference(new PreferenceInfo(p.getAttribute().getUniqueId(), p.getAttribute().getName(), p.getPrefLevel().getPrefProlog()));
			for (AttributeInterface a: instructor.getAttributes())
				if (a.getId().equals(p.getAttribute().getUniqueId())) {
					attr.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
				}
		}
		request.setValue("Attribute Preferences", attr.getPreferenceInt());
		instructor.addValue("Attribute Preferences", attr.getPreferenceInt());
		instructor.setAssignedLoad(instructor.getAssignedLoad() + request.getLoad());
		return request;
	}
	
	protected PreferenceCombination getTimePreference(DepartmentalInstructor instructor, TeachingRequest tr) {
		PreferenceCombination comb = new MinMaxPreferenceCombination();
		List<TimeLocation> noOverlap = new ArrayList<TimeLocation>();
		List<TimeLocation> canOverlap = new ArrayList<TimeLocation>();
		for (TeachingClassRequest tcr: tr.getClassRequests()) {
			Assignment assignment = tcr.getTeachingClass().getCommittedAssignment();
			if (assignment == null) continue;
			if (tcr.isCanOverlap())
				canOverlap.add(assignment.getTimeLocation());
			else
				noOverlap.add(assignment.getTimeLocation());
		}
    	for (Iterator i = instructor.effectivePreferences(TimePref.class).iterator(); i.hasNext();) {
    		TimePref p = (TimePref)i.next();
    		for (Preference<TimeLocation> pref: loadTimePreferences((TimePref)p)) {
				for (TimeLocation time: noOverlap)
					if (time.hasIntersection(pref.getTarget()))
						comb.addPreferenceInt(pref.getPreference());
				for (TimeLocation time: canOverlap)
					if (time.hasIntersection(pref.getTarget()))
						comb.addPreferenceInt(pref.isProhibited() ? Constants.sPreferenceLevelStronglyDiscouraged : pref.getPreference());
			}
		}
		for (EnrolledClass ec: InstructorSchedulingDatabaseLoader.loadUnavailability(Class_DAO.getInstance().getSession(), instructor)) {
			for (TimeLocation time: noOverlap)
				if (time.hasIntersection(ec))
					comb.addPreferenceInt(Constants.sPreferenceLevelProhibited);
			for (TimeLocation time: canOverlap)
				if (time.hasIntersection(ec))
					comb.addPreferenceInt(Constants.sPreferenceLevelStronglyDiscouraged);
		}
		return comb;
	}
	
	protected String getCoursePreference(DepartmentalInstructor instructor, TeachingRequest tr) {
		CourseOffering course = tr.getOffering().getControllingCourseOffering();
		boolean hasRequired = false;
		for (Iterator i = instructor.effectivePreferences(InstructorCoursePref.class).iterator(); i.hasNext();) {
			InstructorCoursePref p = (InstructorCoursePref)i.next();
    		if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) { hasRequired = true; break; }
		}
		for (Iterator i = instructor.effectivePreferences(InstructorCoursePref.class).iterator(); i.hasNext();) {
			InstructorCoursePref p = (InstructorCoursePref)i.next();
			if (p.getCourse().equals(course)) {
				if (hasRequired && !p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) continue;
				return p.getPrefLevel().getPrefProlog();
			}
		}
		if (hasRequired) return Constants.sPreferenceProhibited;
		return Constants.sPreferenceNeutral;
	}
	
	protected String getInstructorPreference(DepartmentalInstructor instructor, TeachingRequest tr) {
		boolean hasRequired = false;
		for (Iterator i = tr.getPreferences(InstructorPref.class).iterator(); i.hasNext();) {
			InstructorPref p = (InstructorPref)i.next();
			if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) { hasRequired = true; break; }
		}
		for (Iterator i = tr.getPreferences(InstructorPref.class).iterator(); i.hasNext();) {
			InstructorPref p = (InstructorPref)i.next();
			if (p.getInstructor().equals(instructor)) {
				if (hasRequired && !p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) continue;
				return p.getPrefLevel().getPrefProlog();
			}
		}
		if (hasRequired) return Constants.sPreferenceProhibited;
		return Constants.sPreferenceNeutral;
	}
	
	protected int getAttributePreference(DepartmentalInstructor instructor, TeachingRequest tr, InstructorAttributeType type) {
		Set<InstructorAttribute> attributes = new HashSet<InstructorAttribute>();
		for (InstructorAttribute a: instructor.getAttributes())
			if (a.getType().equals(type)) attributes.add(a);
        boolean hasReq = false, hasPref = false, needReq = false, hasType = false;
        PreferenceCombination ret = new SumPreferenceCombination();
        for (Iterator i = tr.getPreferences(InstructorAttributePref.class).iterator(); i.hasNext();) {
        	InstructorAttributePref p = (InstructorAttributePref)i.next();
        	if (p.getAttribute().getType().equals(type)) {
        		InstructorAttribute a = ((InstructorAttributePref)p).getAttribute();
        		hasType = true;
                if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) needReq = true;
                if (attributes.contains(a)) {
                    if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceProhibited)) return Constants.sPreferenceLevelProhibited;
                    else if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) hasReq = true;
                    else ret.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
                    hasPref = true;
                } else {
                    if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired) && type.isConjunctive()) return Constants.sPreferenceLevelProhibited;
                }
        	}
        }
        if (needReq && !hasReq) return Constants.sPreferenceLevelProhibited;
        if (type.isRequired() && hasType && !hasPref) return Constants.sPreferenceLevelProhibited;
        return ret.getPreferenceInt();
	}
	
	public PreferenceCombination getAttributePreference(DepartmentalInstructor instructor, TeachingRequest tr, List<InstructorAttributeType> attributeTypes) {
        PreferenceCombination preference = new SumPreferenceCombination();
        for (InstructorAttributeType type: attributeTypes)
        	preference.addPreferenceInt(getAttributePreference(instructor, tr, type));
        return preference;
    }
	
	protected boolean canTeach(DepartmentalInstructor instructor, TeachingRequest request, Context context) {
		if (request.getTeachingLoad() > instructor.getMaxLoad()) return false;
		PreferenceCombination timePref = getTimePreference(instructor, request);
		if (timePref.isProhibited()) return false;
		String coursePref = getCoursePreference(instructor, request);
		if (Constants.sPreferenceProhibited.equals(coursePref)) return false;
		String instrPref = getInstructorPreference(instructor, request);
		if (Constants.sPreferenceProhibited.equals(instrPref)) return false;
		PreferenceCombination attPref = getAttributePreference(instructor, request, context.getAttributeTypes());
		if (attPref.isProhibited()) return false;
		return true;
	}
	
	protected InstructorInfo getInstructor(TeachingRequest tr, TeachingRequestInfo request, DepartmentalInstructor instructor, Context context) {
		InstructorInfo info = getInstructor(request, instructor, context.getNameFormat());
		info.setValue("Time Preferences", getTimePreference(instructor, tr).getPreferenceInt());
		info.setValue("Course Preferences", Constants.preference2preferenceLevel(getCoursePreference(instructor, tr)));
		info.setValue("Instructor Preferences", Constants.preference2preferenceLevel(getInstructorPreference(instructor, tr)));
		info.setValue("Attribute Preferences", getAttributePreference(instructor, tr, context.getAttributeTypes()).getPreferenceInt());
		return info;
	}
	
	public void computeDomainForClass(SuggestionsResponse response, TeachingRequest tr, int index, Context context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		List<DepartmentalInstructor> list = (List<DepartmentalInstructor>)hibSession.createQuery(
    			"select distinct i from DepartmentalInstructor i where " +
    			"i.department.uniqueId = :deptId and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
    			).setLong("deptId", tr.getOffering().getControllingCourseOffering().getDepartment().getUniqueId()).setString("prohibited", PreferenceLevel.sProhibited).list();
		Collections.sort(list);
		for (DepartmentalInstructor instructor: list) {
			if (canTeach(instructor, tr, context)) {
				Suggestion s = new Suggestion();
				s.set(tr, index, instructor);
				response.addDomainValue(s.toInfo(context));
			}
		}
	}
	
	public void computeDomainForInstructor(SuggestionsResponse response, DepartmentalInstructor instructor, TeachingRequest selected, Context context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		List<TeachingRequest> requests = (List<TeachingRequest>)hibSession.createQuery(
				"select r from TeachingRequest r inner join r.offering.courseOfferings co where co.isControl = true and co.subjectArea.department.uniqueId = :deptId order by co.subjectAreaAbbv, co.courseNbr")
				.setLong("deptId", instructor.getDepartment().getUniqueId()).setCacheable(true).list();
		InstructorAssignment selectedAssignment = null;
		if (selected != null) {
			List<DepartmentalInstructor> assigned = new ArrayList<DepartmentalInstructor>(selected.getAssignedInstructors());
			Collections.sort(assigned);
			int index = assigned.indexOf(instructor);
			if (index >= 0)
				selectedAssignment = new InstructorAssignment(selected, index, instructor);
		}
		for (TeachingRequest tr: requests) {
    		if (canTeach(instructor, tr, context)) {
    			TeachingRequestInfo request = getRequest(tr, context.getNameFormat(), null);
    			if (request == null) continue;
    			int maxIndex = (tr.getNbrInstructors() == 1 ? 1 : tr.getAssignedInstructors().size() + 1);
    			for (int index = 0; index < tr.getNbrInstructors() && index < maxIndex; index++) {
    				Suggestion s = new Suggestion();
    				s.set(tr, index, instructor);
    				response.addDomainValue(s.toInfo(context, selectedAssignment));
    			}
			}
		}
	}
	
	public static class Context {
		private String iNameFormat;
		private List<InstructorAttributeType> iAttributeTypes;
		private Suggestion iBase;
		private SessionContext iSessionContext;
		
		Context(SessionContext cx) {
			iSessionContext = cx;
			iNameFormat = UserProperty.NameFormat.get(cx.getUser());
			iAttributeTypes =  (List<InstructorAttributeType>)InstructorAttributeTypeDAO.getInstance().getSession().createQuery("from InstructorAttributeType").setCacheable(true).list();
		}
		
		public String getNameFormat() { return iNameFormat; }
		public List<InstructorAttributeType> getAttributeTypes() { return iAttributeTypes; }
		public Suggestion getBase() { return iBase; }
		public void setBase(Suggestion base) { iBase = base; }
		public SessionContext getSessionContext() { return iSessionContext; }
	}
	
	public class InstructorAssignment {
		private TeachingRequest iRequest;
		private int iIndex;
		private DepartmentalInstructor iInstructor;
		
		InstructorAssignment(TeachingRequest tr, int index, DepartmentalInstructor instructor) {
			iRequest = tr; iIndex = index; iInstructor = instructor;
		}
		
		public TeachingRequest getTeachingRequest() { return iRequest; }
		public int getIndex() { return iIndex; }
		public DepartmentalInstructor getAssigment() { return iInstructor; }
		public void setAssignment(DepartmentalInstructor instructor) { iInstructor = instructor; }
		
		public boolean isExclusive() { return true; }
		public boolean isSameCommon() { return true; }
		
		public AssignmentInfo toInfo(Context context) {
			AssignmentInfo ai = new AssignmentInfo();
			ai.setRequest(getRequest(getTeachingRequest(), context.getNameFormat(), null));
			ai.setIndex(getIndex());
			if (getAssigment() != null)
				ai.setInstructor(getInstructor(getTeachingRequest(), ai.getRequest(), getAssigment(), context));
			return ai;
		}
		
		private boolean overlaps(TeachingRequest other, Context context) {
			for (TeachingClassRequest c1: getTeachingRequest().getClassRequests()) {
				if (c1.isCanOverlap()) continue;
				Assignment a1 = c1.getTeachingClass().getCommittedAssignment();
				if (a1 == null) continue;
				for (TeachingClassRequest c2: other.getClassRequests()) {
					if (c2.isCanOverlap()) continue;
					if (c1.isCommon() && c2.isCommon() && c1.getTeachingClass().equals(c2.getTeachingClass())) continue;
					Assignment a2 = c2.getTeachingClass().getCommittedAssignment();
					if (a2 == null) continue;
					if (a1.getTimeLocation().hasIntersection(a2.getTimeLocation())) return true;
				}
			}
			return false;
		}
		
		public boolean sameCourse(TeachingRequest other, Context context) {
			return getTeachingRequest().getOffering().equals(other.getOffering());
		}
		
		private boolean sameCommon(TeachingRequest other, Context context) {
			if (!sameCourse(other, context)) return false;
			for (TeachingClassRequest c1: getTeachingRequest().getClassRequests()) {
				if (c1.isCommon())
					for (TeachingClassRequest c2: other.getClassRequests()) {
						if (c2.isCommon() && c1.getTeachingClass().getSchedulingSubpart().equals(c2.getTeachingClass().getSchedulingSubpart()) && !c1.getTeachingClass().equals(c2.getTeachingClass())) return false;
					}
			}
			return true;
		}

		public float getLoad(Context context) {
			return getTeachingRequest().getTeachingLoad();
		}
		
		@Override
		public int hashCode() {
			return getTeachingRequest().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof InstructorAssignment)) return false;
			InstructorAssignment a = (InstructorAssignment)o;
			return getTeachingRequest().equals(a.getTeachingRequest()) && getIndex() == a.getIndex();
		}
	}
	
	public class Suggestion {
		List<InstructorAssignment> iAssignments = new ArrayList<InstructorAssignment>();
		
		public void set(TeachingRequest request, int index, DepartmentalInstructor instructor) {
			for (Iterator<InstructorAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				InstructorAssignment other = i.next();
				if (other.getTeachingRequest().equals(request) && other.getIndex() == index) {
					if (instructor != null)
						other.setAssignment(instructor);
					else
						i.remove();
					return;
				}
			}
			if (instructor != null)
				iAssignments.add(new InstructorAssignment(request, index, instructor));
		}
		
		public List<InstructorAssignment> getAssignments() { return iAssignments; }
		
		public DepartmentalInstructor getAssignment(TeachingRequest tr, int index) {
			for (InstructorAssignment a: iAssignments)
				if (a.getTeachingRequest().equals(tr) && a.getIndex() == index) return a.getAssigment();
			List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>(tr.getAssignedInstructors());
			Collections.sort(instructors);
			if (index < instructors.size()) return instructors.get(index);
			return null;
		}
		
		public List<InstructorAssignment> getAssignments(DepartmentalInstructor instructor) {
			List<InstructorAssignment> ret = new ArrayList<InstructorAssignment>();
			for (InstructorAssignment a: iAssignments)
				if (instructor.equals(a.getAssigment())) ret.add(a);
			tr: for (TeachingRequest tr: (List<TeachingRequest>)TeachingRequestDAO.getInstance().getSession().createQuery(
					"select distinct r from TeachingRequest r inner join r.assignedInstructors i where i.uniqueId = :instructorId")
					.setLong("instructorId", instructor.getUniqueId()).setCacheable(true).list()) {
				for (InstructorAssignment a: iAssignments)
					if (a.getTeachingRequest().equals(tr)) continue tr;
				List<DepartmentalInstructor> assignments = new ArrayList<DepartmentalInstructor>(tr.getAssignedInstructors());
				Collections.sort(assignments);
				ret.add(new InstructorAssignment(tr, assignments.indexOf(instructor), instructor));
			}
			return ret;
		}
		
		
		public void computeConflicts(InstructorAssignment assignment, Set<InstructorAssignment> conflicts, Context context) {
			for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
				if (ta.equals(assignment) || conflicts.contains(ta)) continue;
				if (ta.overlaps(assignment.getTeachingRequest(), context)) {
					conflicts.add(ta);
				}
	        }
			
			if (assignment.isExclusive()) {
	            boolean sameCommon = assignment.isSameCommon();
	            for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
	            	if (ta.equals(assignment) || conflicts.contains(ta)) continue;
	            	if (!ta.sameCourse(assignment.getTeachingRequest(), context) || (sameCommon && !ta.sameCommon(assignment.getTeachingRequest(), context)))
	            		conflicts.add(ta);
	            }
	        } else if (assignment.isSameCommon()) {
	        	for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
	            	if (ta.equals(assignment) || conflicts.contains(ta)) continue;
	            	if (ta.sameCourse(assignment.getTeachingRequest(), context) && !ta.sameCommon(assignment.getTeachingRequest(), context))
	            		conflicts.add(ta);
	            }
	        }
			
			float load = assignment.getLoad(context);
			for (InstructorAssignment conflict: conflicts)
				if (assignment.getAssigment().equals(conflict.getAssigment()))
					load -= conflict.getLoad(context);
	        List<InstructorAssignment> adepts = new ArrayList<InstructorAssignment>();
	        List<InstructorAssignment> priority = new ArrayList<InstructorAssignment>();
	        for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
            	if (ta.equals(assignment) || conflicts.contains(ta)) continue;
            	adepts.add(ta);
            	if (!iAssignments.contains(ta)) priority.add(ta);
	            load += ta.getLoad(context);
	        }
	        while (load > assignment.getAssigment().getMaxLoad()) {
	            if (adepts.isEmpty()) {
	                conflicts.add(assignment);
	                break;
	            }
	            if (!priority.isEmpty()) {
	            	InstructorAssignment conflict = ToolBox.random(priority);
		            load -= conflict.getLoad(context);
		            priority.remove(conflict);
		            adepts.remove(conflict);
		            conflicts.add(conflict);
	            } else {
	            	InstructorAssignment conflict = ToolBox.random(adepts);
	            	load -= conflict.getLoad(context);
	            	adepts.remove(conflict);
	            	conflicts.add(conflict);
	            }
	        }
		}
		
		public SuggestionInfo toInfo(Context context) {
			return toInfo(context, null);
		}
		
		public SuggestionInfo toInfo(Context context, InstructorAssignment selected) {
			if (context.getBase() != null)
				for (InstructorAssignment a: context.getBase().iAssignments)
					if (!iAssignments.contains(a) && a.getAssigment() != null) iAssignments.add(a);
			SuggestionInfo si = new SuggestionInfo();
			Set<InstructorAssignment> conflicts = new HashSet<InstructorAssignment>();
			if (selected != null && !iAssignments.contains(selected))
				conflicts.add(selected);
			for (InstructorAssignment a: iAssignments) {
				computeConflicts(a, conflicts, context);
			}
			for (InstructorAssignment a: iAssignments)
				if (!conflicts.remove(a)) si.addAssignment(a.toInfo(context));
			for (InstructorAssignment c: conflicts)
				si.addAssignment(new InstructorAssignment(c.getTeachingRequest(), c.getIndex(), null).toInfo(context));
			for (AssignmentInfo ai: si.getAssignments()) {
				if (ai.getInstructor() != null)
					for (Map.Entry<String, Double> e: ai.getInstructor().getValues().entrySet())
						si.addValue(e.getKey(), e.getValue());
				if (ai.getRequest().getInstructor(ai.getIndex()) != null)
					for (Map.Entry<String, Double> e: ai.getRequest().getInstructor(ai.getIndex()).getValues().entrySet())
						si.addValue(e.getKey(), -e.getValue());
			}
			return si;
		}
	}
}
