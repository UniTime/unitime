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
import java.util.Iterator;
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
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
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
		
		Boolean commit = null;
		Set<DepartmentalInstructor> updateInstructors = new HashSet<DepartmentalInstructor>();
		Set<InstrOfferingConfig> updateConfigs = new HashSet<InstrOfferingConfig>();
		Set<InstructionalOffering> updateOfferings = new HashSet<InstructionalOffering>();
		
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			Context cx = new Context(context, solver);
			Suggestion s = new Suggestion();
			for (AssignmentInfo ai: request.getAssignments()) {
				TeachingRequest tr = TeachingRequestDAO.getInstance().get(ai.getRequest().getRequestId());
				if (tr == null) continue;
				if (commit == null)
					commit = Department.isInstructorSchedulingCommitted(tr.getOffering().getDepartment().getUniqueId());
				DepartmentalInstructor instructor = (ai.getInstructor() == null ? null : DepartmentalInstructorDAO.getInstance().get(ai.getInstructor().getInstructorId()));
				if (instructor != null) {
					InstructorInfo prev = ai.getRequest().getInstructor(ai.getIndex());
					s.set(tr, ai.getIndex(), instructor, prev == null ? null : DepartmentalInstructorDAO.getInstance().get(prev.getInstructorId()));
				}
			}
			Set<InstructorAssignment> conflicts = new HashSet<InstructorAssignment>();
			if (!request.isIgnoreConflicts()) 
				for (InstructorAssignment a: s.getAssignments())
					s.computeConflicts(a, conflicts, cx);

			for (InstructorAssignment a: s.getAssignments())
				unassign(hibSession, a.getTeachingRequest(), a.getCurrentAssignment(), commit);
			for (InstructorAssignment c: conflicts) {
				unassign(hibSession, c.getTeachingRequest(), c.getAssigment(), commit);
				if (commit && c.getAssigment() != null) {
					updateInstructors.add(c.getAssigment());
					if (c.getTeachingRequest().isAssignCoordinator())
						updateOfferings.add(c.getTeachingRequest().getOffering());
					for (TeachingClassRequest tcr: c.getTeachingRequest().getClassRequests()) {
						if (tcr.isAssignInstructor()) {
							updateConfigs.add(tcr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig()); break;
						}
					}
				}
			}
			for (InstructorAssignment a: s.getAssignments()) {
				assign(hibSession, a.getTeachingRequest(), a.getAssigment(), commit);
				if (commit) {
					if (a.getCurrentAssignment() != null) updateInstructors.add(a.getCurrentAssignment());
					if (a.getAssigment() != null) updateInstructors.add(a.getAssigment());
					if (a.getTeachingRequest().isAssignCoordinator())
						updateOfferings.add(a.getTeachingRequest().getOffering());
					for (TeachingClassRequest tcr: a.getTeachingRequest().getClassRequests()) {
						if (tcr.isAssignInstructor()) {
							updateConfigs.add(tcr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig()); break;
						}
					}
				}
			}
			
			for (DepartmentalInstructor instructor: updateInstructors)
				hibSession.saveOrUpdate(instructor);
			
			for (InstructorAssignment c: conflicts)
				changelog(hibSession, c.getTeachingRequest(), c.getAssigment(), null, cx);
			for (InstructorAssignment a: s.getAssignments())
				changelog(hibSession, a.getTeachingRequest(), a.getCurrentAssignment(), a.getAssigment(), cx);
			
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
	
	protected void unassign(org.hibernate.Session hibSession, TeachingRequest request, DepartmentalInstructor instructor, boolean commit) {
		if (instructor == null) return;
		request.getAssignedInstructors().remove(instructor);
		if (commit) {
			if (request.isAssignCoordinator()) {
				for (Iterator<OfferingCoordinator> i = request.getOffering().getOfferingCoordinators().iterator(); i.hasNext(); ) {
					OfferingCoordinator oc = i.next();
					if (request.equals(oc.getTeachingRequest()) && instructor.equals(oc.getInstructor())) {
						Debug.info(request.getOffering().getCourseName() + ": UNASSIGN " + instructor.getNameLastFirst());
						i.remove();
						hibSession.delete(oc);
					}
				}
			}
			for (Iterator<ClassInstructor> i = instructor.getClasses().iterator(); i.hasNext(); ) {
				ClassInstructor ci = i.next();
				if (request.equals(ci.getTeachingRequest())) {
					Debug.info(ci.getClassInstructing().getClassLabel(hibSession) + ": UNASSIGN " + instructor.getNameLastFirst());
					ci.getClassInstructing().getClassInstructors().remove(ci);
					i.remove();
					hibSession.delete(ci);
					break;
				}
			}
		}
	}
	
	protected void assign(org.hibernate.Session hibSession, TeachingRequest request, DepartmentalInstructor instructor, boolean commit) {
		if (instructor == null) return;
		request.getAssignedInstructors().add(instructor);
		if (commit) {
			if (request.isAssignCoordinator()) {
				Debug.info(request.getOffering().getCourseName() + ": ASSIGN " + instructor.getNameLastFirst());
				OfferingCoordinator oc = new OfferingCoordinator();
				oc.setInstructor(instructor);
				oc.setOffering(request.getOffering());
				oc.setResponsibility(request.getResponsibility());
				oc.setTeachingRequest(request);
				oc.setPercentShare(request.getPercentShare());
				request.getOffering().getOfferingCoordinators().add(oc);
				hibSession.save(oc);
			}
			for (TeachingClassRequest cr: request.getClassRequests()) {
				if (cr.isAssignInstructor()) {
					Debug.info(cr.getTeachingClass().getClassLabel(hibSession) + ": ASSIGN " + instructor.getNameLastFirst());
					ClassInstructor ci = new ClassInstructor();
					ci.setClassInstructing(cr.getTeachingClass());
					ci.setInstructor(instructor);
					ci.setLead(cr.isLead());
					ci.setPercentShare(cr.getPercentShare());
					ci.setResponsibility(request.getResponsibility());
					ci.setTeachingRequest(request);
					cr.getTeachingClass().getClassInstructors().add(ci);
					instructor.getClasses().add(ci);
					hibSession.saveOrUpdate(ci);
				}
			}
		}
	}
	
	protected void changelog(org.hibernate.Session hibSession, TeachingRequest request, DepartmentalInstructor oldInstructor, DepartmentalInstructor newInstructor, Context context) {
		if (oldInstructor != null || newInstructor != null) {
			if (request.isAssignCoordinator()) {
				CourseOffering co = request.getOffering().getControllingCourseOffering();
				ChangeLog.addChange(hibSession, context.getSessionContext(),
						co,
						co.getCourseName() + ": " + 
						(oldInstructor == null ? "<i>Not Assigned</i>" : oldInstructor.getName(context.getNameFormat())) + " &rarr; " + (newInstructor == null ? "<i>Not Assigned</i>" : newInstructor.getName(context.getNameFormat())),
						ChangeLog.Source.INSTRUCTOR_ASSIGNMENT, ChangeLog.Operation.ASSIGN,
						co.getSubjectArea(), co.getDepartment());
			}
			for (TeachingClassRequest cr: request.getClassRequests()) {
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
