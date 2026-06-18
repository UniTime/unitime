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

import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseNull;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.ManageInstructorListUpdateRequest;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(ManageInstructorListUpdateRequest.class)
public class ManageInstructorListUpdateBackend implements GwtRpcImplementation<ManageInstructorListUpdateRequest, GwtRpcResponseNull>{

	@Override
	public GwtRpcResponseNull execute(ManageInstructorListUpdateRequest request, SessionContext context) {
		context.checkPermission(request.getDepartmentId(), Right.ManageInstructors);
		
		Department dept = DepartmentDAO.getInstance().get(request.getDepartmentId());
		
		DepartmentalInstructorDAO idao = DepartmentalInstructorDAO.getInstance();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		HashSet<Class_> updatedClasses = new HashSet<Class_>(); 
		try {	
			tx = hibSession.beginTransaction();

			if (request.hasUnassignIds()) {
				for (Long instructorId: request.getUnassignIds()) {
					DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(instructorId);
					if (instructor != null && context.hasPermission(instructor, Right.InstructorDelete)) {
                        ChangeLog.addChange(
                                hibSession, 
                                context, 
                                instructor, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.DELETE, 
                                null, instructor.getDepartment());

                        for (ClassInstructor ci: instructor.getClasses()) {
				        	updatedClasses.add(ci.getClassInstructing());
				        	ci.getClassInstructing().getClassInstructors().remove(ci);
				        	hibSession.remove(ci);
				        }
					    
				        for (Assignment a: instructor.getAssignments()) {
				        	a.getInstructors().remove(instructor);
				        	hibSession.merge(a);
				        }
				        instructor.getDepartment().getInstructors().remove(instructor);
				        
						hibSession.remove(instructor);
					}
				}
			}
			
			if (request.hasAssignExternalIds()) {
				for (Staff staff: Staff.getStaffByDept(dept.getDeptCode(), dept.getSessionId())) {
					if (request.hasAssignExternalId(staff.getExternalUniqueId())) {
						DepartmentalInstructor inst = new DepartmentalInstructor();
						inst.setLastName(staff.getLastName());
						inst.setEmail(staff.getEmail());
						inst.setFirstName(staff.getFirstName());
						inst.setMiddleName(staff.getMiddleName());
						inst.setAcademicTitle(staff.getAcademicTitle());
						inst.setExternalUniqueId(staff.getExternalUniqueId());
						inst.setPositionType(staff.getPositionType());
						
						inst.setDepartment(dept);
						dept.getInstructors().add(inst);
                        
                        inst.setIgnoreToFar(Boolean.FALSE);
                        
						hibSession.persist(inst);

                        ChangeLog.addChange(
                                hibSession, 
                                context, 
                                inst, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.CREATE, 
                                null, inst.getDepartment());
					}
				}
			}
			
			tx.commit();
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw new GwtRpcException(e.getMessage(), e);
        }
		return null;
	}

}
