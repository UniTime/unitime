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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.instructor.model.EnrolledClass;
import org.cpsolver.instructor.model.Preference;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.CourseInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.comparators.InstructorComparator;
import org.unitime.timetable.model.dao.Class_DAO;
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
    	if (commonItypes.contains(clazz.getSchedulingSubpart().getItype().getSis_ref())) return true;
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
	
	protected Set<String> getCommonItypes() {
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
}
