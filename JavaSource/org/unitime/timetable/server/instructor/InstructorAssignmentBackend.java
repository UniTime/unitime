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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAssignmentRequest;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorAssignmentRequest.class)
public class InstructorAssignmentBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<InstructorAssignmentRequest, GwtRpcResponseNull>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public GwtRpcResponseNull execute(InstructorAssignmentRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorSchedulingSolver);
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null) {
			solver.assign(request.getAssignments());
			return new GwtRpcResponseNull();
		}
		
		boolean commit = true;
		Set<InstrOfferingConfig> updateConfigs = new HashSet<InstrOfferingConfig>();
		
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Context cx = new Context(context);
			Suggestion s = new Suggestion();
			for (AssignmentInfo ai: request.getAssignments()) {
				Class_ clazz = Class_DAO.getInstance().get(ai.getRequest().getRequestId());
				if (clazz == null) continue;
				DepartmentalInstructor instructor = (ai.getInstructor() == null ? null : DepartmentalInstructorDAO.getInstance().get(ai.getInstructor().getInstructorId()));
				if (instructor != null)
					s.set(clazz, ai.getIndex(), instructor);
			}
			if (request.isIgnoreConflicts()) {
				for (InstructorAssignment a: s.getAssignments()) {
					assign(hibSession, a, cx, commit);
					if (commit)
						updateConfigs.add(a.getClazz().getSchedulingSubpart().getInstrOfferingConfig());
				}
			} else {
				Set<InstructorAssignment> conflicts = new HashSet<InstructorAssignment>();
				for (InstructorAssignment a: s.getAssignments()) {
					s.computeConflicts(a, conflicts, cx);
				}
				for (InstructorAssignment a: s.getAssignments())
					if (!conflicts.remove(a)) {
						assign(hibSession, a, cx, commit);
						if (commit)
							updateConfigs.add(a.getClazz().getSchedulingSubpart().getInstrOfferingConfig());
					}
				for (InstructorAssignment c: conflicts) {
					unassign(hibSession, c, cx, commit);
					if (commit) updateConfigs.add(c.getClazz().getSchedulingSubpart().getInstrOfferingConfig());
				}
			}
			tx.commit(); tx = null;
		} catch (Exception e) {
			if (tx != null && tx.isActive()) {
				tx.rollback();
				throw new GwtRpcException(e.getMessage(), e);
			}
		}
		
		if (updateConfigs.isEmpty()) {
    		try {
            	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
            	if (className != null && className.trim().length() > 0) {
            			ExternalInstrOfferingConfigAssignInstructorsAction assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).newInstance());
            			for (InstrOfferingConfig ioc: updateConfigs)
            				assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, hibSession);
            	}
    		} catch (Exception e) {
    			Debug.error("Failed to call external action: " + e.getMessage(), e);
    		}
		}
		
		return new GwtRpcResponseNull();
	}
	
	public void assign(org.hibernate.Session hibSession, InstructorAssignment assignment, Context context, boolean commit) {
		List<DepartmentalInstructor> current = getInstructors(assignment.getClazz());
		DepartmentalInstructor previous = (assignment.getIndex() < current.size() ? current.get(assignment.getIndex()) : null);
		replaceInstructor(hibSession, assignment.getClazz(), previous, assignment.getAssigment(), assignment.getIndex(), context, commit);
		for (Class_ parent = assignment.getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
    		if (isToBeIncluded(parent, null))
    			replaceInstructor(hibSession, parent, previous, assignment.getAssigment(), assignment.getIndex(), context, commit);
    	}
		if (previous != null) hibSession.saveOrUpdate(previous);
		if (assignment.getAssigment() != null) hibSession.saveOrUpdate(assignment.getAssigment());
	}
	
	public void unassign(org.hibernate.Session hibSession, InstructorAssignment assignment, Context context, boolean commit) {
		replaceInstructor(hibSession, assignment.getClazz(), assignment.getAssigment(), null, assignment.getIndex(), context, commit);
		for (Class_ parent = assignment.getClazz().getParentClass(); parent != null; parent = parent.getParentClass()) {
    		if (isToBeIncluded(parent, null))
    			replaceInstructor(hibSession, parent, assignment.getAssigment(), null, assignment.getIndex(), context, commit);
    	}
		if (assignment.getAssigment() != null) hibSession.saveOrUpdate(assignment.getAssigment());
	}
	
	public void replaceInstructor(org.hibernate.Session hibSession, Class_ clazz, DepartmentalInstructor oldInstructor, DepartmentalInstructor newInstructor, int index, Context context, boolean commit) {
		if (oldInstructor != null) {
			for (ClassInstructor ci: clazz.getClassInstructors()) {
				if (ci.getInstructor().equals(oldInstructor)) {
					ci.getInstructor().getClasses().remove(ci);
					ci.getClassInstructing().getClassInstructors().remove(ci);
					hibSession.delete(ci);
				}
			}
		}
		if (newInstructor != null) {
			ClassInstructor ci = new ClassInstructor();
			ci.setClassInstructing(clazz);
			ci.setInstructor(newInstructor);
			ci.setTentative(!commit); 
			ci.setLead(true);
			ci.setPercentShare(100 / clazz.effectiveNbrInstructors());
			ci.setAssignmentIndex(index);
			clazz.getClassInstructors().add(ci);
			newInstructor.getClasses().add(ci);
		}
		if (oldInstructor != null || newInstructor != null) {
			ChangeLog.addChange(hibSession, context.getSessionContext(),
					clazz,
					clazz.getClassLabel(hibSession) + ": " + 
					(oldInstructor == null ? "<i>Not Assigned</i>" : oldInstructor.getName(context.getNameFormat())) + " &rarr; " + (newInstructor == null ? "<i>Not Assigned</i>" : newInstructor.getName(context.getNameFormat())),
					ChangeLog.Source.INSTRUCTOR_ASSIGNMENT, ChangeLog.Operation.ASSIGN,
					clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(), clazz.getControllingDept());
		}		
	}

}
