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
import java.util.Set;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.coursett.preference.MinMaxPreferenceCombination;
import org.cpsolver.instructor.model.EnrolledClass;
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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributePref;
import org.unitime.timetable.model.InstructorCoursePref;
import org.unitime.timetable.model.InstructorPref;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimePref;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.ClassInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingDatabaseLoader;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingAssignmentsPageRequest.class)
public class TeachingAssignmentsBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<TeachingAssignmentsPageRequest, GwtRpcResponseList<InstructorInfo>> {
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
			Set<String> commonItypes = getCommonItypes();
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			
			GwtRpcResponseList<InstructorInfo> ret = new GwtRpcResponseList<InstructorInfo>();
			org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
			List<DepartmentalInstructor> instructors = null;
			if (request.getDepartmentId() == null) {
				List<Long> departmentIds = new ArrayList<Long>();
				for (Department d: Department.getUserDepartments(context.getUser())) {
					for (DepartmentalInstructor di: d.getInstructors())
						if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
							departmentIds.add(d.getUniqueId());
							break;
						}
				}
				if (departmentIds.isEmpty()) return ret;
				instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
						"select distinct i from DepartmentalInstructor i where " +
						"i.department.uniqueId in :departmentIds and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
						).setParameterList("departmentIds", departmentIds).setString("prohibited", PreferenceLevel.sProhibited).list();
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
