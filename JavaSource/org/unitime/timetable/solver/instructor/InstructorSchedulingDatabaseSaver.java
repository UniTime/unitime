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
import org.cpsolver.instructor.model.Section;
import org.cpsolver.instructor.model.TeachingAssignment;
import org.cpsolver.instructor.model.TeachingRequest;
import org.hibernate.CacheMode;
import org.hibernate.Transaction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
public class InstructorSchedulingDatabaseSaver extends ProblemSaver<TeachingRequest, TeachingAssignment, InstructorSchedulingModel> {
    private String iInstructorFormat;
    private Set<Long> iSolverGroupId = new HashSet<Long>();
    private Set<InstrOfferingConfig> iUpdatedConfigs = new HashSet<InstrOfferingConfig>();
    private Progress iProgress = null;
    private boolean iTentative = true;
    private boolean iIgnoreOtherInstructors = false;

    public InstructorSchedulingDatabaseSaver(Solver solver) {
        super(solver);
        iProgress = Progress.getInstance(getModel());
    	for (Long id: getModel().getProperties().getPropertyLongArry("General.SolverGroupId", null))
    		iSolverGroupId.add(id);
    	iTentative = !getModel().getProperties().getPropertyBoolean("Save.Commit", false);
    	iInstructorFormat = getModel().getProperties().getProperty("General.InstructorFormat", NameFormat.LAST_FIRST.reference());
    	iIgnoreOtherInstructors = getModel().getProperties().getPropertyBoolean("General.IgnoreOtherInstructors", false);
    }

	@Override
	public void save() throws Exception {
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
    	return "<A href='classDetail.do?cid="+clazz.getUniqueId()+"'>"+clazz.getClassLabel()+"</A>";
    }
    
    protected String toHtml(DepartmentalInstructor instructor) {
    	return "<a href='instructorDetail.do?instructorId=" + instructor.getUniqueId() + "&deptId=" + instructor.getDepartment().getUniqueId() + "'>" + instructor.getName(iInstructorFormat) + "</a>";
    }
    
    protected String toHtml(TeachingAssignment assignment) {
    	return "<a href='instructorDetail.do?instructorId=" + assignment.getInstructor().getInstructorId() + "'>" + assignment.getInstructor().getName() + "</a>";
    }
    
    protected String toHtml(TeachingRequest request) {
    	return "<a href='classDetail.do?cid=" + request.getSections().get(0).getSectionId() + "'>" + request.getName() + "</a>";
    }	
	
    protected boolean isToBeIgnored(ClassInstructor ci) {
    	if (ci.isTentative()) return false; // always update tentative relations
    	if (!ci.isLead()) return true; // ignore instructors without conflict check
    	if (!ci.getClassInstructing().isInstructorAssignmentNeeded()) return true; // do not need instructor assignments
    	if (ci.getInstructor().getTeachingPreference() == null || PreferenceLevel.sProhibited.equals(ci.getInstructor().getTeachingPreference().getPrefProlog()) || ci.getInstructor().getMaxLoad() < 0f) {
    		// instructor not enabled for instructor scheduling -- use General.IgnoreOtherInstructors parameter
    		if (iIgnoreOtherInstructors) {
    			iProgress.warn("Instructor " + toHtml(ci.getInstructor()) + " is assigned to " + toHtml(ci.getClassInstructing()) + ", but not allowed for automatic assignment.");
    			return true;
    		} else {
    			iProgress.warn("Instructor " + toHtml(ci.getInstructor()) + " was assigned to " + toHtml(ci.getClassInstructing()) + " that was not allowed for automatic assignment.");
    			return false;
    		}
    	}
    	return false;
    }
	
	protected void saveSolution(org.hibernate.Session hibSession) {
		iProgress.setPhase("Loading instructors ...", 1);
		Map<Long, DepartmentalInstructor> instructors = new HashMap<Long, DepartmentalInstructor>();
		Set<DepartmentalInstructor> changed = new HashSet<DepartmentalInstructor>();
		for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery(
    			"select i from DepartmentalInstructor i, SolverGroup g inner join g.departments d where " +
    			"g.uniqueId in :solverGroupId and i.department = d"
    			).setParameterList("solverGroupId", iSolverGroupId).list()) {
			instructors.put(instructor.getUniqueId(), instructor);
			for (Iterator<ClassInstructor> i = instructor.getClasses().iterator(); i.hasNext(); ) {
				ClassInstructor ci = i.next();
				if (!isToBeIgnored(ci)) {
					if (!ci.isTentative())
						iUpdatedConfigs.add(ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig());
					changed.add(ci.getInstructor());
					ci.getClassInstructing().getClassInstructors().remove(ci);
					i.remove();
					hibSession.delete(ci);
				}
			}
		}
		iProgress.incProgress();
		
		iProgress.setPhase("Loading classes ...", 1);
		Map<Long, Class_> classes = new HashMap<Long, Class_>();
    	for (Class_ clazz: (List<Class_>)hibSession.createQuery(
    			"select c from Class_ c where c.controllingDept.solverGroup.uniqueId in :solverGroupId and c.cancelled = false and " +
    			"(c.teachingLoad is not null or c.schedulingSubpart.teachingLoad is not null) and " +
    			"((c.nbrInstructors is null and c.schedulingSubpart.nbrInstructors > 0) or c.nbrInstructors > 0)")
    			.setParameterList("solverGroupId", iSolverGroupId).list()) {
    		classes.put(clazz.getUniqueId(), clazz);
    	}
    	iProgress.incProgress();
    	
    	iProgress.setPhase("Saving instructor assignments ...", getModel().variables().size());
    	for (TeachingRequest request: getModel().variables()) {
    		iProgress.incProgress();
    		TeachingAssignment assignment = getAssignment().getValue(request);
    		if (assignment == null) continue;
			DepartmentalInstructor instructor = instructors.get(assignment.getInstructor().getInstructorId());
			if (instructor == null) continue;
    		for (Section section: request.getSections()) {
    			Class_ clazz = classes.get(section.getSectionId());
    			if (clazz == null || !clazz.isEnabledForStudentScheduling()) continue;
    			if (!iTentative)
    				iUpdatedConfigs.add(clazz.getSchedulingSubpart().getInstrOfferingConfig());
    			ClassInstructor ci = new ClassInstructor();
    			ci.setClassInstructing(clazz);
    			ci.setInstructor(instructor);
    			ci.setTentative(iTentative);
    			ci.setLead(true);
    			ci.setPercentShare(100 / clazz.effectiveNbrInstructors());
    			clazz.getClassInstructors().add(ci);
    			instructor.getClasses().add(ci);
				changed.add(ci.getInstructor());
    		}
    	}

    	for (DepartmentalInstructor instructor: changed)
    		hibSession.saveOrUpdate(instructor);
	}

}
