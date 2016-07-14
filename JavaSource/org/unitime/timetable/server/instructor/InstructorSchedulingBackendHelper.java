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
import org.unitime.timetable.model.ClassInstructor;
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
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.instructor.InstructorSchedulingDatabaseLoader;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingBackendHelper {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

    protected int nrInstructorsNeeded(Class_ clazz) {
    	int nrChildInstructors = 0;
    	for (Class_ child: clazz.getChildClasses()) {
    		nrChildInstructors += nrInstructorsNeeded(child); 
    	}
    	return Math.max(0, (clazz.isInstructorAssignmentNeeded() ? clazz.effectiveNbrInstructors() : 0) - nrChildInstructors);
    }
    
    protected CourseInfo getCourse(CourseOffering course) {
    	CourseInfo info = new CourseInfo();
    	info.setCourseId(course.getUniqueId());
    	info.setCourseName(course.getCourseName());
    	return info;
    }
    
    protected SectionInfo getSection(Class_ clazz) {
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
		section.setCommon(!clazz.isInstructorAssignmentNeeded());
		section.setTime(time != null ? time.getDayHeader() + " " + time.getStartTimeHeader(CONSTANTS.useAmPm()) + " - " + time.getEndTimeHeader(CONSTANTS.useAmPm()) : null);
		section.setDate(time != null ? time.getDatePatternName() : null);
		section.setRoom(room);
		return section;
    }
    
    protected boolean isToBeIncluded(Class_ clazz, Set<String> commonItypes) {
    	if (clazz.isCancelled()) return false;
    	if (clazz.isInstructorAssignmentNeeded()) return true;
    	if (commonItypes != null && commonItypes.contains(clazz.getSchedulingSubpart().getItype().getSis_ref())) return true;
    	return false;
    }
    
    protected boolean isToBeIgnored(ClassInstructor ci) {
    	if (ci.isTentative()) return false;
    	if (!ci.isLead()) return true;
    	return false;
    }
    
    public List<DepartmentalInstructor> getInstructors(Class_ clazz) {
    	List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>();
    	
    	InstructorComparator ic = new InstructorComparator(); ic.setCompareBy(ic.COMPARE_BY_INDEX);
    	TreeSet<ClassInstructor> sortedInstructors = new TreeSet(ic);
    	sortedInstructors.addAll(clazz.getClassInstructors());
    	for (ClassInstructor ci: sortedInstructors) {
    		if (!isToBeIgnored(ci) && (ci.getInstructor().getTeachingPreference() != null && !ci.getInstructor().getTeachingPreference().getPrefProlog().equals(PreferenceLevel.sProhibited))) {
    			instructors.add(ci.getInstructor());
    		}
    	}
    	for (Class_ child: clazz.getChildClasses()) {
    		if (child.isCancelled() || !child.isInstructorAssignmentNeeded()) continue;
    		for (ClassInstructor ci: child.getClassInstructors())
        		if (!isToBeIgnored(ci)) instructors.remove(ci.getInstructor());
    	}
    	return instructors;
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
    
	protected TeachingRequestInfo getRequestForClass(Class_ clazz, Set<String> commonItypes, String nameFormat) {
    	int nrInstructors = nrInstructorsNeeded(clazz);
    	if (nrInstructors <= 0) return null;
    	List<DepartmentalInstructor> instructors = getInstructors(clazz);
    	if (instructors.size() > nrInstructors) nrInstructors = instructors.size();
    	TeachingRequestInfo request = new TeachingRequestInfo();
    	request.setRequestId(clazz.getUniqueId());
    	request.setNrInstructors(nrInstructors);
    	request.setCourse(getCourse(clazz.getSchedulingSubpart().getControllingCourseOffering()));
    	request.addSection(getSection(clazz));
    	request.setLoad(clazz.effectiveTeachingLoad());
    	Set<SchedulingSubpart> checked = new HashSet<SchedulingSubpart>();
    	checked.add(clazz.getSchedulingSubpart());
    	for (Class_ parent = clazz.getParentClass(); parent != null; parent = parent.getParentClass()) {
    		checked.add(parent.getSchedulingSubpart());
    		if (isToBeIncluded(parent, commonItypes)) {
    			request.addSection(getSection(parent));
    			if (parent.isInstructorAssignmentNeeded()) request.setLoad(request.getLoad() + parent.effectiveTeachingLoad());
    		}
    	}
    	for (SchedulingSubpart other: clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
    		if (checked.contains(other)) continue;
    		if (commonItypes.contains(other.getItype().getSis_ref()) && !other.isInstructorAssignmentNeeded()) {
    			for (Class_ c: other.getClasses())
    				request.addSection(getSection(c));
    		}
    	}
		for (Iterator it = clazz.effectivePreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
			InstructorPref p = (InstructorPref)it.next();
			request.addInstructorPreference(new PreferenceInfo(p.getInstructor().getUniqueId(), p.getInstructor().getName(nameFormat), p.getPrefLevel().getPrefProlog()));
		}
		for (Iterator it = clazz.effectivePreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
			InstructorAttributePref p = (InstructorAttributePref)it.next();
			request.addAttributePreference(new PreferenceInfo(p.getAttribute().getUniqueId(), p.getAttribute().getName(), p.getPrefLevel().getPrefProlog()));
		}
    	for (int i = 0; i < instructors.size(); i++) {
    		DepartmentalInstructor instructor = instructors.get(i);
        	if (instructor != null) {
        		InstructorInfo info = getInstructor(request, instructor, nameFormat);
        		float load = 0f;
        		for (ClassInstructor ci: instructor.getClasses()) {
        			if (!ci.isLead() || ci.getClassInstructing().isCancelled() || !ci.getClassInstructing().isInstructorAssignmentNeeded()) continue;
        			if (ci.isLead() && !ci.getClassInstructing().isCancelled() && ci.getClassInstructing().isInstructorAssignmentNeeded())
        				load += ci.getClassInstructing().effectiveTeachingLoad();
        		}
        		info.setAssignedLoad(load);
        		request.addInstructor(info);
        		for (Iterator it = clazz.effectivePreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
        			InstructorPref p = (InstructorPref)it.next();
        			if (p.getInstructor().equals(instructor))
        				info.setValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
        		}
        		MinMaxPreferenceCombination attr = new MinMaxPreferenceCombination();
        		for (Iterator it = clazz.effectivePreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
        			InstructorAttributePref p = (InstructorAttributePref)it.next();
        			if (instructor.getAttributes().contains(p.getAttribute()))
        				attr.addPreferenceProlog(p.getPrefLevel().getPrefProlog());
        		}
        		info.setValue("Attribute Preferences", attr.getPreferenceInt());
        	}
    	}
		return request;
	}
	
	protected static Set<String> getCommonItypes() {
		Set<String> commonItypes = new HashSet<String>();
		SolverParameterDef param = SolverParameterDef.findByNameType("General.CommonItypes", SolverType.INSTRUCTOR);
		if (param != null) {
			if (param.getDefault() != null && !param.getDefault().isEmpty()) {
				for (String itype: param.getDefault().split(","))
	    			if (!itype.isEmpty()) commonItypes.add(itype);
			}
		} else {
			commonItypes.add("lec");
		}
		return commonItypes;
	}

	public InstructorInfo getInstructorInfo(DepartmentalInstructor instructor, String nameFormat, Set<String> commonItypes) {
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
		ci: for (ClassInstructor ci: instructor.getClasses()) {
			if (isToBeIgnored(ci) || !ci.getClassInstructing().isInstructorAssignmentNeeded()) continue;
			Class_ clazz = ci.getClassInstructing();
			for (Class_ child: clazz.getChildClasses()) {
	    		if (child.isCancelled() || !child.isInstructorAssignmentNeeded()) continue;
	    		for (ClassInstructor x: child.getClassInstructors())
	        		if (!isToBeIgnored(x) && x.getInstructor().equals(instructor)) continue ci;
	    	}
			TeachingRequestInfo request = getRequestForClass(clazz, info, commonItypes, nameFormat);
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
    
	protected TeachingRequestInfo getRequestForClass(Class_ clazz, InstructorInfo instructor, Set<String> commonItypes, String nameFormat) {
    	TeachingRequestInfo request = new TeachingRequestInfo();
    	request.setRequestId(clazz.getUniqueId());
    	request.setNrInstructors(nrInstructorsNeeded(clazz));
    	request.setCourse(getCourse(clazz.getSchedulingSubpart().getControllingCourseOffering()));
    	request.addSection(getSection(clazz));
    	if (clazz.isInstructorAssignmentNeeded())
    		request.setLoad(clazz.effectiveTeachingLoad());
    	Set<SchedulingSubpart> checked = new HashSet<SchedulingSubpart>();
    	checked.add(clazz.getSchedulingSubpart());
    	for (Class_ parent = clazz.getParentClass(); parent != null; parent = parent.getParentClass()) {
    		checked.add(parent.getSchedulingSubpart());
    		if (isToBeIncluded(parent, commonItypes)) {
    			request.addSection(getSection(parent));
    			if (parent.isInstructorAssignmentNeeded()) request.setLoad(request.getLoad() + parent.effectiveTeachingLoad());
    		}
    	}
    	for (SchedulingSubpart other: clazz.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
    		if (checked.contains(other)) continue;
    		if (commonItypes.contains(other.getItype().getSis_ref()) && !other.isInstructorAssignmentNeeded()) {
    			for (Class_ c: other.getClasses())
    				request.addSection(getSection(c));
    		}
    	}
		for (Iterator it = clazz.effectivePreferences(InstructorPref.class).iterator(); it.hasNext(); ) {
			InstructorPref p = (InstructorPref)it.next();
			request.addInstructorPreference(new PreferenceInfo(p.getInstructor().getUniqueId(), p.getInstructor().getName(nameFormat), p.getPrefLevel().getPrefProlog()));
			if (p.getInstructor().getUniqueId().equals(instructor.getInstructorId())) {
				request.setValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
				instructor.addValue("Instructor Preferences", Constants.preference2preferenceLevel(p.getPrefLevel().getPrefProlog()));
			}
		}
		MinMaxPreferenceCombination attr = new MinMaxPreferenceCombination();
		for (Iterator it = clazz.effectivePreferences(InstructorAttributePref.class).iterator(); it.hasNext(); ) {
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
	
	protected PreferenceCombination getTimePreference(DepartmentalInstructor instructor, Class_ request, Set<String> commonItypes) {
		PreferenceCombination comb = new MinMaxPreferenceCombination();
		List<TimeLocation> noOverlap = new ArrayList<TimeLocation>();
		List<TimeLocation> canOverlap = new ArrayList<TimeLocation>();
		
		Assignment assignment = request.getCommittedAssignment();
		if (assignment != null) noOverlap.add(assignment.getTimeLocation());
		Set<SchedulingSubpart> checked = new HashSet<SchedulingSubpart>();
    	checked.add(request.getSchedulingSubpart());
    	for (Class_ parent = request.getParentClass(); parent != null; parent = parent.getParentClass()) {
    		checked.add(parent.getSchedulingSubpart());
    		if (isToBeIncluded(parent, commonItypes)) {
    			Assignment a = parent.getCommittedAssignment();
    			if (a != null) noOverlap.add(a.getTimeLocation());
    		}
    	}
    	for (SchedulingSubpart other: request.getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
    		if (checked.contains(other)) continue;
    		if (commonItypes != null && commonItypes.contains(other.getItype().getSis_ref()) && !other.isInstructorAssignmentNeeded()) {
    			for (Class_ c: other.getClasses()) {
    				Assignment a = c.getCommittedAssignment();
    				if (a != null) {
        				if (other.getClasses().size() > 1)
        					canOverlap.add(a.getTimeLocation());
        				else
        					noOverlap.add(a.getTimeLocation());
    				}
    			}
    		}
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
	
	protected float getLoad(Class_ request, Set<String> commonItypes) {
		float load = request.effectiveTeachingLoad();
    	for (Class_ parent = request.getParentClass(); parent != null; parent = parent.getParentClass())
    		if (isToBeIncluded(parent, commonItypes) && parent.isInstructorAssignmentNeeded())
    			load += parent.effectiveTeachingLoad();
    	return load;
	}
	
	protected String getCoursePreference(DepartmentalInstructor instructor, Class_ request) {
		CourseOffering course = request.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getControllingCourseOffering();
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
	
	protected String getInstructorPreference(DepartmentalInstructor instructor, Class_ request) {
		boolean hasRequired = false;
		for (Iterator i = request.effectivePreferences(InstructorPref.class).iterator(); i.hasNext();) {
			InstructorPref p = (InstructorPref)i.next();
			if (p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) { hasRequired = true; break; }
		}
		for (Iterator i = request.effectivePreferences(InstructorPref.class).iterator(); i.hasNext();) {
			InstructorPref p = (InstructorPref)i.next();
			if (p.getInstructor().equals(instructor)) {
				if (hasRequired && !p.getPrefLevel().getPrefProlog().equals(Constants.sPreferenceRequired)) continue;
				return p.getPrefLevel().getPrefProlog();
			}
		}
		if (hasRequired) return Constants.sPreferenceProhibited;
		return Constants.sPreferenceNeutral;
	}
	
	protected int getAttributePreference(DepartmentalInstructor instructor, Class_ request, InstructorAttributeType type) {
		Set<InstructorAttribute> attributes = new HashSet<InstructorAttribute>();
		for (InstructorAttribute a: instructor.getAttributes())
			if (a.getType().equals(type)) attributes.add(a);
        boolean hasReq = false, hasPref = false, needReq = false, hasType = false;
        PreferenceCombination ret = new SumPreferenceCombination();
        for (Iterator i = request.effectivePreferences(InstructorAttributePref.class).iterator(); i.hasNext();) {
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
	
	public PreferenceCombination getAttributePreference(DepartmentalInstructor instructor, Class_ request, List<InstructorAttributeType> attributeTypes) {
        PreferenceCombination preference = new SumPreferenceCombination();
        for (InstructorAttributeType type: attributeTypes)
        	preference.addPreferenceInt(getAttributePreference(instructor, request, type));
        return preference;
    }
	
	protected boolean canTeach(DepartmentalInstructor instructor, Class_ request, Context context) {
		if (getLoad(request, context.getCommonItypes()) > instructor.getMaxLoad()) return false;
		PreferenceCombination timePref = getTimePreference(instructor, request, context.getCommonItypes());
		if (timePref.isProhibited()) return false;
		String coursePref = getCoursePreference(instructor, request);
		if (Constants.sPreferenceProhibited.equals(coursePref)) return false;
		String instrPref = getInstructorPreference(instructor, request);
		if (Constants.sPreferenceProhibited.equals(instrPref)) return false;
		PreferenceCombination attPref = getAttributePreference(instructor, request, context.getAttributeTypes());
		if (attPref.isProhibited()) return false;
		return true;
	}
	
	protected InstructorInfo getInstructor(Class_ clazz, TeachingRequestInfo request, DepartmentalInstructor instructor, Context context) {
		InstructorInfo info = getInstructor(request, instructor, context.getNameFormat());
		info.setValue("Time Preferences", getTimePreference(instructor, clazz, context.getCommonItypes()).getPreferenceInt());
		info.setValue("Course Preferences", Constants.preference2preferenceLevel(getCoursePreference(instructor, clazz)));
		info.setValue("Instructor Preferences", Constants.preference2preferenceLevel(getInstructorPreference(instructor, clazz)));
		info.setValue("Attribute Preferences", getAttributePreference(instructor, clazz, context.getAttributeTypes()).getPreferenceInt());
		return info;
	}
	
	public void computeDomainForClass(SuggestionsResponse response, Class_ clazz, int index, Context context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		List<DepartmentalInstructor> list = (List<DepartmentalInstructor>)hibSession.createQuery(
    			"select distinct i from DepartmentalInstructor i where " +
    			"i.department.uniqueId = :deptId and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
    			).setLong("deptId", clazz.getControllingDept().getUniqueId()).setString("prohibited", PreferenceLevel.sProhibited).list();
		Collections.sort(list);
		for (DepartmentalInstructor instructor: list) {
			if (canTeach(instructor, clazz, context)) {
				Suggestion s = new Suggestion();
				s.set(clazz, index, instructor);
				response.addDomainValue(s.toInfo(context));
			}
		}
	}
	
	public void computeDomainForInstructor(SuggestionsResponse response, DepartmentalInstructor instructor, Class_ selected, Context context) {
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		List<Class_> classes = (List<Class_>)hibSession.createQuery(
				"select distinct c from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings co " +
				"left join fetch c.schedulingSubpart as ss left join fetch c.classInstructors as ci left join fetch ci.instructor as di " +
				"left join fetch c.preferences as cp left join fetch ss.preferences as sp left join fetch di.preferences as dip " +
				"where co.subjectArea.department.uniqueId in :deptId and co.isControl = true and c.cancelled = false and " +
				"(c.teachingLoad is not null or c.schedulingSubpart.teachingLoad is not null) and " +
				"((c.nbrInstructors is null and c.schedulingSubpart.nbrInstructors > 0) or c.nbrInstructors > 0)")
				.setLong("deptId", instructor.getDepartment().getUniqueId()).setCacheable(true).list();
		ClassComparator cmp = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
		Collections.sort(classes, cmp);
		InstructorAssignment selectedAssignment = null;
		if (selected != null) {
			int index = getInstructors(selected).indexOf(instructor);
			if (index >= 0)
				selectedAssignment = new InstructorAssignment(selected, index, instructor);
		}
		for (Class_ clazz: classes) {
			if (!clazz.isInstructorAssignmentNeeded()) continue;
			int nrInstructors = nrInstructorsNeeded(clazz);
	    	if (nrInstructors <= 0) continue;
    		if (canTeach(instructor, clazz, context)) {
    			TeachingRequestInfo request = getRequestForClass(clazz, context.getCommonItypes(), context.getNameFormat());
    			if (request == null) continue;
    			int maxIndex = (nrInstructors == 1 ? 1 : getInstructors(clazz).size() + 1);
    			for (int index = 0; index < nrInstructors && index < maxIndex; index++) {
    				Suggestion s = new Suggestion();
    				s.set(clazz, index, instructor);
    				response.addDomainValue(s.toInfo(context, selectedAssignment));
    			}
			}
		}
	}
	
	public static class Context {
		private String iNameFormat;
		private Set<String> iCommonItypes;
		private List<InstructorAttributeType> iAttributeTypes;
		private Suggestion iBase;
		private SessionContext iSessionContext;
		
		Context(SessionContext cx) {
			iSessionContext = cx;
			iNameFormat = UserProperty.NameFormat.get(cx.getUser());
			iCommonItypes = new HashSet<String>();
			SolverParameterDef param = SolverParameterDef.findByNameType("General.CommonItypes", SolverType.INSTRUCTOR);
			if (param != null) {
				if (param.getDefault() != null && !param.getDefault().isEmpty()) {
					for (String itype: param.getDefault().split(","))
		    			if (!itype.isEmpty()) iCommonItypes.add(itype);
				}
			} else {
				iCommonItypes.add("lec");
			}
			iAttributeTypes =  (List<InstructorAttributeType>)InstructorAttributeTypeDAO.getInstance().getSession().createQuery("from InstructorAttributeType").setCacheable(true).list();
		}
		
		public String getNameFormat() { return iNameFormat; }
		public Set<String> getCommonItypes() { return iCommonItypes; }
		public List<InstructorAttributeType> getAttributeTypes() { return iAttributeTypes; }
		public Suggestion getBase() { return iBase; }
		public void setBase(Suggestion base) { iBase = base; }
		public SessionContext getSessionContext() { return iSessionContext; }
	}
	
	public class InstructorAssignment {
		private Class_ iClazz;
		private int iIndex;
		private DepartmentalInstructor iInstructor;
		
		InstructorAssignment(Class_ clazz, int index, DepartmentalInstructor instructor) {
			iClazz = clazz; iIndex = index; iInstructor = instructor;
		}
		
		public Class_ getClazz() { return iClazz; }
		public int getIndex() { return iIndex; }
		public DepartmentalInstructor getAssigment() { return iInstructor; }
		public void setAssignment(DepartmentalInstructor instructor) { iInstructor = instructor; }
		
		public boolean isExclusive() { return true; }
		public boolean isSameCommon() { return true; }
		
		public AssignmentInfo toInfo(Context context) {
			AssignmentInfo ai = new AssignmentInfo();
			ai.setRequest(getRequestForClass(getClazz(), context.getCommonItypes(), context.getNameFormat()));
			ai.setIndex(getIndex());
			if (getAssigment() != null)
				ai.setInstructor(getInstructor(getClazz(), ai.getRequest(), getAssigment(), context));
			return ai;
		}
		
		private boolean overlaps(Class_ clazz, Context context) {
			Assignment otherAssignment = clazz.getCommittedAssignment();
			if (otherAssignment == null) return false;
			Assignment assignment = getClazz().getCommittedAssignment();
			if (assignment != null && assignment.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation())) return true;
			Set<SchedulingSubpart> checked = new HashSet<SchedulingSubpart>();
	    	checked.add(getClazz().getSchedulingSubpart());
	    	for (Class_ parent = getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
	    		checked.add(parent.getSchedulingSubpart());
	    		if (isToBeIncluded(parent, context.getCommonItypes())) {
	    			if (!parent.isInstructorAssignmentNeeded() && parent.equals(clazz)) continue;
	    			Assignment a = parent.getCommittedAssignment();
	    			if (a != null && a.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation())) return true;
	    		}
	    	}
	    	for (SchedulingSubpart other: getClazz().getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
	    		if (checked.contains(other)) continue;
	    		if (context.getCommonItypes() != null && context.getCommonItypes().contains(other.getItype().getSis_ref()) && !other.isInstructorAssignmentNeeded() && other.getClasses().size() == 1) {
	    			for (Class_ c: other.getClasses()) {
	    				Assignment a = c.getCommittedAssignment();
	    				if (a != null && a.getTimeLocation().hasIntersection(otherAssignment.getTimeLocation())) return true;
	    			}
	    		}
	    	}
	    	return false;
		}
		
		public boolean overlaps(InstructorAssignment assignment, Context context) {
			if (overlaps(assignment.getClazz(), context)) return true;
			Set<SchedulingSubpart> checked = new HashSet<SchedulingSubpart>();
	    	checked.add(assignment.getClazz().getSchedulingSubpart());
	    	for (Class_ parent = assignment.getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
	    		checked.add(parent.getSchedulingSubpart());
	    		if (isToBeIncluded(parent, context.getCommonItypes()) && overlaps(parent, context)) return true;
	    	}
	    	for (SchedulingSubpart other: assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getSchedulingSubparts()) {
	    		if (checked.contains(other)) continue;
	    		if (context.getCommonItypes() != null && context.getCommonItypes().contains(other.getItype().getSis_ref()) && !other.isInstructorAssignmentNeeded() && other.getClasses().size() == 1) {
	    			for (Class_ c: other.getClasses()) {
	    				if (overlaps(c, context)) return true;
	    			}
	    		}
	    	}
	    	return false;
		}
		
		public boolean sameCourse(InstructorAssignment assignment, Context context) {
			return getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().equals(assignment.getClazz().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());
		}
		
		private boolean sameCommon(Class_ commonClazz, Context context) {
			for (Class_ parent = getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
	    		if (isToBeIncluded(parent, context.getCommonItypes()) && parent.equals(commonClazz)) return true;
	    	}
			return false;
		}
		
		public boolean sameCommon(InstructorAssignment assignment, Context context) {
			if (!sameCourse(assignment, context)) return false;
			for (Class_ parent = assignment.getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
				if (isToBeIncluded(parent, context.getCommonItypes()) && !parent.isInstructorAssignmentNeeded() && !sameCommon(parent, context)) return false;
			}
			return true;
		}
		
		public float getLoad(Context context) {
			float load = getClazz().effectiveTeachingLoad();
	    	for (Class_ parent = getClazz().getParentClass(); parent != null; parent = parent.getParentClass())
	    		if (isToBeIncluded(parent, context.getCommonItypes()) && parent.isInstructorAssignmentNeeded())
	    			load += parent.effectiveTeachingLoad();
	    	return load;	
		}
		
		@Override
		public int hashCode() {
			return getClazz().hashCode();
		}
		
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof InstructorAssignment)) return false;
			InstructorAssignment a = (InstructorAssignment)o;
			return getClazz().equals(a.getClazz()) && getIndex() == a.getIndex();
		}
	}
	
	public class Suggestion {
		List<InstructorAssignment> iAssignments = new ArrayList<InstructorAssignment>();
		
		public void set(Class_ clazz, int index, DepartmentalInstructor instructor) {
			for (Iterator<InstructorAssignment> i = iAssignments.iterator(); i.hasNext(); ) {
				InstructorAssignment other = i.next();
				if (other.getClazz().equals(clazz) && other.getIndex() == index) {
					if (instructor != null)
						other.setAssignment(instructor);
					else
						i.remove();
					return;
				}
			}
			if (instructor != null)
				iAssignments.add(new InstructorAssignment(clazz, index, instructor));
		}
		
		public List<InstructorAssignment> getAssignments() { return iAssignments; }
		
		public DepartmentalInstructor getAssignment(Class_ clazz, int index) {
			for (InstructorAssignment a: iAssignments)
				if (a.getClazz().equals(clazz) && a.getIndex() == index) return a.getAssigment();
			List<DepartmentalInstructor> instructors = getInstructors(clazz);
			if (index < instructors.size()) return instructors.get(index);
			return null;
		}
		
		public List<InstructorAssignment> getAssignments(DepartmentalInstructor instructor) {
			List<InstructorAssignment> ret = new ArrayList<InstructorAssignment>();
			for (InstructorAssignment a: iAssignments)
				if (instructor.equals(a.getAssigment())) ret.add(a);
			ci: for (ClassInstructor ci: instructor.getClasses()) {
				if (isToBeIgnored(ci)) continue;
				int nrInstructors = nrInstructorsNeeded(ci.getClassInstructing());
				if (nrInstructors <= 0) continue;
				List<DepartmentalInstructor> assignments = getInstructors(ci.getClassInstructing());
				int index = assignments.indexOf(instructor);
				if (index < 0) continue;
				for (InstructorAssignment a: iAssignments)
					if (a.getClazz().equals(ci.getClassInstructing()) && index == a.getIndex()) continue ci;
				ret.add(new InstructorAssignment(ci.getClassInstructing(), index, instructor));
			}
			return ret;
		}
		
		
		public void computeConflicts(InstructorAssignment assignment, Set<InstructorAssignment> conflicts, Context context) {
			for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
				if (ta.equals(assignment) || conflicts.contains(ta)) continue;
				if (ta.overlaps(assignment, context)) {
					conflicts.add(ta);
				}
	        }
			
			if (assignment.isExclusive()) {
	            boolean sameCommon = assignment.isSameCommon();
	            for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
	            	if (ta.equals(assignment) || conflicts.contains(ta)) continue;
	            	if (!ta.sameCourse(assignment, context) || (sameCommon && !ta.sameCommon(assignment, context)))
	            		conflicts.add(ta);
	            }
	        } else if (assignment.isSameCommon()) {
	        	for (InstructorAssignment ta: getAssignments(assignment.getAssigment())) {
	            	if (ta.equals(assignment) || conflicts.contains(ta)) continue;
	            	if (ta.sameCourse(assignment, context) && !ta.sameCommon(assignment, context))
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
				si.addAssignment(new InstructorAssignment(c.getClazz(), c.getIndex(), null).toInfo(context));
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
