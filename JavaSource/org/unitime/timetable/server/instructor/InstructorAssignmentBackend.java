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
import java.util.HashSet;
import java.util.Iterator;
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
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.TeachingRequestDAO;
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
		Set<InstructionalOffering> updateOfferings = new HashSet<InstructionalOffering>();
		
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Context cx = new Context(context);
			Suggestion s = new Suggestion();
			for (AssignmentInfo ai: request.getAssignments()) {
				TeachingRequest tr = TeachingRequestDAO.getInstance().get(ai.getRequest().getRequestId());
				if (tr == null) continue;
				DepartmentalInstructor instructor = (ai.getInstructor() == null ? null : DepartmentalInstructorDAO.getInstance().get(ai.getInstructor().getInstructorId()));
				if (instructor != null)
					s.set(tr, ai.getIndex(), instructor);
			}
			if (request.isIgnoreConflicts()) {
				for (InstructorAssignment a: s.getAssignments()) {
					assign(hibSession, a, cx, commit);
					if (commit) {
						if (a.getTeachingRequest().isAssignCoordinator())
							updateOfferings.add(a.getTeachingRequest().getOffering());
						for (TeachingClassRequest tcr: a.getTeachingRequest().getClassRequests()) {
							if (tcr.isAssignInstructor()) {
								updateConfigs.add(tcr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig()); break;
							}
						}
					}
				}
			} else {
				Set<InstructorAssignment> conflicts = new HashSet<InstructorAssignment>();
				for (InstructorAssignment a: s.getAssignments()) {
					s.computeConflicts(a, conflicts, cx);
				}
				for (InstructorAssignment a: s.getAssignments())
					if (!conflicts.remove(a)) {
						assign(hibSession, a, cx, commit);
						if (commit) {
							if (a.getTeachingRequest().isAssignCoordinator())
								updateOfferings.add(a.getTeachingRequest().getOffering());
							for (TeachingClassRequest tcr: a.getTeachingRequest().getClassRequests()) {
								if (tcr.isAssignInstructor()) {
									updateConfigs.add(tcr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig()); break;
								}
							}
						}					}
				for (InstructorAssignment c: conflicts) {
					unassign(hibSession, c, cx, commit);
					if (commit) {
						if (c.getTeachingRequest().isAssignCoordinator())
							updateOfferings.add(c.getTeachingRequest().getOffering());
						for (TeachingClassRequest tcr: c.getTeachingRequest().getClassRequests()) {
							if (tcr.isAssignInstructor()) {
								updateConfigs.add(tcr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig()); break;
							}
						}
					}
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
		
		if (updateOfferings.isEmpty()) {
    		try {
            	String className = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
            	if (className != null && className.trim().length() > 0) {
            			ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className).newInstance());
            			for (InstructionalOffering io: updateOfferings)
            				editAction.performExternalCourseOfferingEditAction(io, hibSession);
            	}
    		} catch (Exception e) {
    			Debug.error("Failed to call external action: " + e.getMessage(), e);
    		}
		}
		
		return new GwtRpcResponseNull();
	}
	
	public void assign(org.hibernate.Session hibSession, InstructorAssignment assignment, Context context, boolean commit) {
		List<DepartmentalInstructor> current = new ArrayList<DepartmentalInstructor>(assignment.getTeachingRequest().getAssignedInstructors());
		Collections.sort(current);
		DepartmentalInstructor previous = (assignment.getIndex() < current.size() ? current.get(assignment.getIndex()) : null);
		replaceInstructor(hibSession, assignment.getTeachingRequest(), previous, assignment.getAssigment(), assignment.getIndex(), context, commit);
		if (previous != null) hibSession.saveOrUpdate(previous);
		if (assignment.getAssigment() != null) hibSession.saveOrUpdate(assignment.getAssigment());
	}
	
	public void unassign(org.hibernate.Session hibSession, InstructorAssignment assignment, Context context, boolean commit) {
		replaceInstructor(hibSession, assignment.getTeachingRequest(), assignment.getAssigment(), null, assignment.getIndex(), context, commit);
		if (assignment.getAssigment() != null) hibSession.saveOrUpdate(assignment.getAssigment());
	}
	
	public void replaceInstructor(org.hibernate.Session hibSession, TeachingRequest tr, DepartmentalInstructor oldInstructor, DepartmentalInstructor newInstructor, int index, Context context, boolean commit) {
		if (oldInstructor != null) {
			tr.getAssignedInstructors().remove(oldInstructor);
		}
		if (newInstructor != null) {
			tr.getAssignedInstructors().add(newInstructor);
		}
		hibSession.saveOrUpdate(tr);
		if (commit) {
			if (tr.isAssignCoordinator()) {
				if (oldInstructor != null) {
					for (Iterator<OfferingCoordinator> i = tr.getOffering().getOfferingCoordinators().iterator(); i.hasNext(); ) {
						OfferingCoordinator oc = i.next();
						if (tr.equals(oc.getTeachingRequest()) && oldInstructor.equals(oc.getInstructor())) {
							i.remove();
							hibSession.delete(oc);
						}
					}
				}
				if (newInstructor != null) {
					OfferingCoordinator oc = new OfferingCoordinator();
					oc.setInstructor(newInstructor);
					oc.setOffering(tr.getOffering());
					oc.setResponsibility(tr.getResponsibility());
					oc.setTeachingRequest(tr);
					tr.getOffering().getOfferingCoordinators().add(oc);
					hibSession.save(oc);
				}
			}
			if (oldInstructor != null)
				for (Iterator<ClassInstructor> i = oldInstructor.getClasses().iterator(); i.hasNext(); ) {
					ClassInstructor ci = i.next();
					if (tr.equals(ci.getTeachingRequest())) {
						ci.getClassInstructing().getClassInstructors().remove(ci);
						i.remove();
						hibSession.delete(ci);
					}
				}
			if (newInstructor != null)
				for (TeachingClassRequest cr: tr.getClassRequests()) {
					if (cr.isAssignInstructor()) {
						ClassInstructor ci = new ClassInstructor();
						ci.setClassInstructing(cr.getTeachingClass());
						ci.setInstructor(newInstructor);
						ci.setLead(cr.isLead());
						ci.setPercentShare(cr.getPercentShare());
						ci.setResponsibility(tr.getResponsibility());
						ci.setTeachingRequest(tr);
						cr.getTeachingClass().getClassInstructors().add(ci);
						newInstructor.getClasses().add(ci);
						hibSession.saveOrUpdate(ci);
					}
				}
		}
		if (oldInstructor != null || newInstructor != null) {
			if (tr.isAssignCoordinator()) {
				CourseOffering co = tr.getOffering().getControllingCourseOffering();
				ChangeLog.addChange(hibSession, context.getSessionContext(),
						co,
						co.getCourseName() + ": " + 
						(oldInstructor == null ? "<i>Not Assigned</i>" : oldInstructor.getName(context.getNameFormat())) + " &rarr; " + (newInstructor == null ? "<i>Not Assigned</i>" : newInstructor.getName(context.getNameFormat())),
						ChangeLog.Source.INSTRUCTOR_ASSIGNMENT, ChangeLog.Operation.ASSIGN,
						co.getSubjectArea(), co.getDepartment());
			}
			for (TeachingClassRequest cr: tr.getClassRequests()) {
				if (cr.isAssignInstructor()) {
					Class_ clazz = cr.getTeachingClass();
					ChangeLog.addChange(hibSession, context.getSessionContext(),
							clazz,
							clazz.getClassLabel(hibSession) + ": " + 
							(oldInstructor == null ? "<i>Not Assigned</i>" : oldInstructor.getName(context.getNameFormat())) + " &rarr; " + (newInstructor == null ? "<i>Not Assigned</i>" : newInstructor.getName(context.getNameFormat())),
							ChangeLog.Source.INSTRUCTOR_ASSIGNMENT, ChangeLog.Operation.ASSIGN,
							clazz.getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering().getSubjectArea(), clazz.getControllingDept());
				}
			}
		}		
	}

}
