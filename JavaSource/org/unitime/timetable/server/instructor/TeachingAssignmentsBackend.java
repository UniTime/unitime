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
import java.util.BitSet;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.instructor.model.Preference;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
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
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimePatternModel;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.CourseInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.SectionInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingAssignmentsPageRequest.class)
public class TeachingAssignmentsBackend implements GwtRpcImplementation<TeachingAssignmentsPageRequest, GwtRpcResponseList<InstructorInfo>> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public GwtRpcResponseList<InstructorInfo> execute(TeachingAssignmentsPageRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorSchedulingSolver);
		context.setAttribute(SessionAttribute.DepartmentId, request.getDepartmentId() == null ? "-1" : String.valueOf(request.getDepartmentId()));
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null)
			return new GwtRpcResponseList<InstructorInfo>(solver.getInstructors(request.getDepartmentId()));
		else {
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
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			
			GwtRpcResponseList<InstructorInfo> ret = new GwtRpcResponseList<InstructorInfo>();
			org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
			List<DepartmentalInstructor> instructors = null;
			if (request.getDepartmentId() == null) {
				instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
						"select distinct i from DepartmentalInstructor i where " +
				    	"i.department.session.uniqueId = :sessionId and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
						).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setString("prohibited", PreferenceLevel.sProhibited).list();
			} else {
				instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
						"select distinct i from DepartmentalInstructor i where " +
				    	"i.department.uniqueId = :departmentId and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
						).setLong("departmentId", request.getDepartmentId()).setString("prohibited", PreferenceLevel.sProhibited).list();
			}
	    	for (DepartmentalInstructor instructor: instructors) {
	    		ret.add(getInstructorInfo(instructor, nameFormat, commonItypes));
	    	}
	    	Collections.sort(ret);
	    	return ret;
		}
	}
	
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
		section.setExternalId(clazz.getExternalId(course));
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
    
    protected List<DepartmentalInstructor> getInstructors(Class_ clazz) {
    	List<DepartmentalInstructor> instructors = new ArrayList<DepartmentalInstructor>();
    	for (ClassInstructor ci: clazz.getClassInstructors()) {
    		if (!isToBeIgnored(ci)) {
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
    
    protected InstructorInfo getInstructorInfo(DepartmentalInstructor instructor, String nameFormat, Set<String> commonItypes) {
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
		for (Preference<TimeLocation> pf: loadUnavailability(instructor)) {
			PreferenceInfo pi = new PreferenceInfo(new Long(pf.getTarget().hashCode()), pf.getTarget().getLongName(CONSTANTS.useAmPm()), Constants.preferenceLevel2preference(pf.getPreference()));
			pi.setComparable(String.format("%03d:%05d", pf.getTarget().getDayCode(), pf.getTarget().getStartSlot()));
			info.addTimePreference(pi);
			for (Enumeration<Integer> i = pf.getTarget().getSlots(); i.hasMoreElements(); ) {
				int slot = i.nextElement();
				slot2pref[slot][0] = Math.min(slot2pref[slot][0], pf.getPreference());
				slot2pref[slot][1] = Math.max(slot2pref[slot][1], pf.getPreference());
				slot2pref[slot][2] = 1;
			}
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
    
    protected List<Preference<TimeLocation>> loadUnavailability(DepartmentalInstructor di) {
    	List<Preference<TimeLocation>> ret = new ArrayList<Preference<TimeLocation>>();
    	if (di.getExternalUniqueId() != null) {
    		List<StudentClassEnrollment> enrollments = (List<StudentClassEnrollment>)Class_DAO.getInstance().getSession().createQuery(
    				"from StudentClassEnrollment e where e.student.session.uniqueId = :sessionId and e.student.externalUniqueId = :externalId and e.clazz.cancelled = false"
    				).setLong("sessionId", di.getDepartment().getSessionId()).setString("externalId", di.getExternalUniqueId()).setCacheable(true).list();
    		for (StudentClassEnrollment enrollment: enrollments) {
    			org.unitime.timetable.model.Assignment assignment = enrollment.getClazz().getCommittedAssignment();
    			if (assignment != null) {
    				DatePattern datePattern = assignment.getDatePattern();
    				TimeLocation time = new TimeLocation(
    						assignment.getDays().intValue(),
    						assignment.getStartSlot().intValue(),
    						assignment.getSlotPerMtg(),
    						0,0,
    						enrollment.getClazz().getUniqueId(),
    						enrollment.getClazz().getClassLabel(enrollment.getCourseOffering(), true),
    						(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
    						assignment.getBreakTime()
    						);
    				ret.add(new Preference<TimeLocation>(time, Constants.sPreferenceLevelProhibited));
    			}
    		}
    		List<ClassInstructor> classInstructors = (List<ClassInstructor>)Class_DAO.getInstance().getSession().createQuery(
    				"from ClassInstructor ci where ci.instructor.externalUniqueId = :externalId and ci.instructor.department.session.uniqueId = :sessionId and " +
    				"ci.instructor.department.uniqueId != :departmentId and ci.tentative = false and ci.lead = true and ci.classInstructing.cancelled = false"
    				).setLong("sessionId", di.getDepartment().getSessionId()).setString("externalId", di.getExternalUniqueId()).setLong("departmentId", di.getDepartment().getUniqueId()).setCacheable(true).list();
    		for (ClassInstructor ci: classInstructors) {
        		org.unitime.timetable.model.Assignment assignment = ci.getClassInstructing().getCommittedAssignment();
        		if (assignment != null) {
        			DatePattern datePattern = assignment.getDatePattern();
    				TimeLocation time = new TimeLocation(
    						assignment.getDays().intValue(),
    						assignment.getStartSlot().intValue(),
    						assignment.getSlotPerMtg(),
    						0,0,
    						ci.getClassInstructing().getUniqueId(),
    						ci.getClassInstructing().getClassLabel(true),
    						(datePattern == null ? new BitSet() : datePattern.getPatternBitSet()),
    						assignment.getBreakTime()
    						);
    				ret.add(new Preference<TimeLocation>(time, Constants.sPreferenceLevelProhibited));
    			}
    		}
    	}
    	for (ClassInstructor ci: di.getClasses()) {
    		if (ci.isTentative() || !ci.isLead() || ci.getClassInstructing().isCancelled() || ci.getClassInstructing().isInstructorAssignmentNeeded()) continue;
    		org.unitime.timetable.model.Assignment assignment = ci.getClassInstructing().getCommittedAssignment();
    		if (assignment != null) {
				ret.add(new Preference<TimeLocation>(assignment.getTimeLocation(), Constants.sPreferenceLevelProhibited));
			}
    	}
    	return ret;
    }
	
	protected TeachingRequestInfo getRequestForClass(Class_ clazz, InstructorInfo instructor, Set<String> commonItypes, String nameFormat) {
    	// int nrInstructors = nrInstructorsNeeded(clazz);
    	// if (nrInstructors <= 0) return null;
    	TeachingRequestInfo request = new TeachingRequestInfo();
    	request.setRequestId(clazz.getUniqueId());
    	request.setInstructorIndex(0);
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
