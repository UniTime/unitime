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
package org.unitime.timetable.solver.instructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.ProblemSaver;
import org.cpsolver.ifs.util.Progress;
import org.cpsolver.instructor.model.InstructorSchedulingModel;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.hibernate.CacheMode;
import org.hibernate.Transaction;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.TeachingClassRequest;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingDatabaseSaver extends ProblemSaver<TeachingRequest.Variable, TeachingAssignment, InstructorSchedulingModel> {
    private String iInstructorFormat;
    private Long iSessionId = null;
    private Set<Long> iSolverGroupId = new HashSet<Long>();
    private Set<InstrOfferingConfig> iUpdatedConfigs = new HashSet<InstrOfferingConfig>();
    private Set<InstructionalOffering> iUpdatedOfferings = new HashSet<InstructionalOffering>();
    private Progress iProgress = null;
    private boolean iTentative = true;
    private boolean iShowClassSuffix = false;

    public InstructorSchedulingDatabaseSaver(Solver solver) {
        super(solver);
        iProgress = Progress.getInstance(getModel());
        iSessionId = getModel().getProperties().getPropertyLong("General.SessionId", (Long)null);
    	for (Long id: getModel().getProperties().getPropertyLongArry("General.SolverGroupId", null))
    		iSolverGroupId.add(id);
    	iTentative = !getModel().getProperties().getPropertyBoolean("Save.Commit", false);
    	iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", NameFormat.LAST_FIRST.reference());
    	iShowClassSuffix = ApplicationProperty.SolverShowClassSufix.isTrue();
    }

	@Override
	public void save() throws Exception {
		ApplicationProperties.setSessionId(iSessionId);
        iProgress.setStatus("Saving solution ...");
        org.hibernate.Session hibSession = new _RootDAO().getSession();
        hibSession.setCacheMode(CacheMode.IGNORE);
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();
            saveSolution(hibSession);
            tx.commit();
            
            if (!iUpdatedConfigs.isEmpty()) {
            	String className = ApplicationProperty.ExternalActionInstrOfferingConfigAssignInstructors.value();
            	if (className != null && className.trim().length() > 0){
                	ExternalInstrOfferingConfigAssignInstructorsAction assignAction = (ExternalInstrOfferingConfigAssignInstructorsAction) (Class.forName(className).newInstance());
                	iProgress.setPhase("Performing external actions ...", iUpdatedConfigs.size());
                	for (InstrOfferingConfig ioc: iUpdatedConfigs) {
                		iProgress.incProgress();
                		assignAction.performExternalInstrOfferingConfigAssignInstructorsAction(ioc, hibSession);
                	}
            	}
        	}
            if (!iUpdatedOfferings.isEmpty()) {
            	String className = ApplicationProperty.ExternalActionCourseOfferingEdit.value();
            	if (className != null && className.trim().length() > 0){
                	ExternalCourseOfferingEditAction editAction = (ExternalCourseOfferingEditAction) (Class.forName(className).newInstance());
                	iProgress.setPhase("Performing external actions ...", iUpdatedOfferings.size());
                	for (InstructionalOffering io: iUpdatedOfferings) {
                		iProgress.incProgress();
                		editAction.performExternalCourseOfferingEditAction(io, hibSession);
                	}
            	}
            }
            
            iProgress.setPhase("Refreshing solution ...", 1);
            try {
            	if (SolverServerImplementation.getInstance() != null)
            		SolverServerImplementation.getInstance().refreshInstructorSolution(iSolverGroupId);
            	iProgress.incProgress();
            } catch (Exception e) {
                iProgress.warn("Unable to refresh solution, reason:" + e.getMessage(),e);
            }
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            iProgress.fatal("Unable to save a solution, reason: "+e.getMessage(),e);
    	} finally {
    		// here we need to close the session since this code may run in a separate thread
    		if (hibSession!=null && hibSession.isOpen()) hibSession.close();
        }
	}
	
    protected String toHtml(Class_ clazz) {
    	return "<A href='classDetail.do?cid="+clazz.getUniqueId()+"'>"+clazz.getClassLabel(iShowClassSuffix)+"</A>";
    }
    
    protected String toHtml(DepartmentalInstructor instructor) {
    	return "<a href='instructorDetail.do?instructorId=" + instructor.getUniqueId() + "&deptId=" + instructor.getDepartment().getUniqueId() + "'>" + instructor.getName(iInstructorFormat) + "</a>";
    }
    
    protected String toHtml(TeachingAssignment assignment) {
    	return "<a href='instructorDetail.do?instructorId=" + assignment.getInstructor().getInstructorId() + "'>" + assignment.getInstructor().getName() + "</a>";
    }
    
    protected String toHtml(TeachingRequest request) {
    	return "<a href='classDetail.do?cid=" + request.getSections().get(0).getSectionId() + "'>" + request.getCourse().getCourseName() + " " + request.getSections() + "</a>";
    }	
	
	protected void saveSolution(org.hibernate.Session hibSession) {
		iProgress.setPhase("Loading instructors ...", 1);
		Map<Long, DepartmentalInstructor> instructors = new HashMap<Long, DepartmentalInstructor>();
		Set<DepartmentalInstructor> changedInstructors = new HashSet<DepartmentalInstructor>();
		Set<InstructionalOffering> changedOfferings = new HashSet<InstructionalOffering>();
		for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery(
    			"select i from DepartmentalInstructor i, SolverGroup g inner join g.departments d where " +
    			"g.uniqueId in :solverGroupId and i.department = d"
    			).setParameterList("solverGroupId", iSolverGroupId).list()) {
			instructors.put(instructor.getUniqueId(), instructor);
			for (Iterator<ClassInstructor> i = instructor.getClasses().iterator(); i.hasNext(); ) {
				ClassInstructor ci = i.next();
				if (ci.getTeachingRequest() != null) {
					iUpdatedConfigs.add(ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig());
					changedInstructors.add(ci.getInstructor());
					ci.getClassInstructing().getClassInstructors().remove(ci);
					i.remove();
					hibSession.delete(ci);
				}
			}
		}
		iProgress.incProgress();
		for (OfferingCoordinator coordinator: (List<OfferingCoordinator>)hibSession.createQuery(
    			"select c from OfferingCoordinator c inner join c.instructor i, SolverGroup g inner join g.departments d where " +
    			"g.uniqueId in :solverGroupId and i.department = d"
    			).setParameterList("solverGroupId", iSolverGroupId).list()) {
			if (coordinator.getTeachingRequest() != null) {
				iUpdatedOfferings.add(coordinator.getOffering());
				changedOfferings.add(coordinator.getOffering());
				coordinator.getOffering().getOfferingCoordinators().remove(coordinator);
				hibSession.delete(coordinator);
			}
		}
		
		iProgress.setPhase("Loading requests ...", 1);
		Map<Long, org.unitime.timetable.model.TeachingRequest> requests = new HashMap<Long, org.unitime.timetable.model.TeachingRequest>();
    	for (org.unitime.timetable.model.TeachingRequest request: (List<org.unitime.timetable.model.TeachingRequest>)hibSession.createQuery(
    			"select r from TeachingRequest r inner join r.offering.courseOfferings co where co.isControl = true and co.subjectArea.department.solverGroup.uniqueId in :solverGroupId")
    			.setParameterList("solverGroupId", iSolverGroupId).list()) {
    		request.getAssignedInstructors().clear();
    		requests.put(request.getUniqueId(), request);
    	}
    	iProgress.incProgress();
    	
    	iProgress.setPhase("Saving instructor assignments ...", getModel().variables().size());
    	for (TeachingRequest.Variable request: getModel().variables()) {
    		iProgress.incProgress();
    		TeachingAssignment assignment = getAssignment().getValue(request);
    		if (assignment == null) continue;
			DepartmentalInstructor instructor = instructors.get(assignment.getInstructor().getInstructorId());
			if (instructor == null) continue;
			org.unitime.timetable.model.TeachingRequest r = requests.get(request.getRequest().getRequestId());
			if (r == null) continue;
			r.getAssignedInstructors().add(instructor);
			if (!iTentative) {
				if (r.isAssignCoordinator()) {
					OfferingCoordinator oc = new OfferingCoordinator();
					oc.setInstructor(instructor);
					oc.setOffering(r.getOffering());
					oc.setResponsibility(r.getResponsibility());
					oc.setTeachingRequest(r);
					r.getOffering().getOfferingCoordinators().add(oc);
					changedOfferings.add(r.getOffering());
					hibSession.save(oc);
					iUpdatedOfferings.add(r.getOffering());
				}
				for (TeachingClassRequest cr: r.getClassRequests()) {
					if (cr.isAssignInstructor()) {
						ClassInstructor ci = new ClassInstructor();
						ci.setClassInstructing(cr.getTeachingClass());
						ci.setInstructor(instructor);
						ci.setLead(cr.isLead());
						ci.setPercentShare(cr.getPercentShare());
						ci.setResponsibility(r.getResponsibility());
						ci.setTeachingRequest(r);
						cr.getTeachingClass().getClassInstructors().add(ci);
		    			instructor.getClasses().add(ci);
						changedInstructors.add(ci.getInstructor());
						hibSession.saveOrUpdate(ci);
						iUpdatedConfigs.add(cr.getTeachingClass().getSchedulingSubpart().getInstrOfferingConfig());
					}
				}
			}
    	}

    	for (org.unitime.timetable.model.TeachingRequest request: requests.values())
    		hibSession.saveOrUpdate(request);
    	for (DepartmentalInstructor instructor: changedInstructors)
    		hibSession.saveOrUpdate(instructor);
    	for (InstructionalOffering offering: changedOfferings)
    		hibSession.saveOrUpdate(offering);
	}

}
