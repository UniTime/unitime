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

import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorEditRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorEditResponse;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.PositionTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.JavascriptFunctions;

@GwtRpcImplements(InstructorEditRequest.class)
public class InstructorEditBackend implements GwtRpcImplementation<InstructorEditRequest, InstructorEditResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Override
	public InstructorEditResponse execute(InstructorEditRequest request, SessionContext context) {
		context.hasPermission(request.getInstructorId(), "Instructor", Right.InstructorEdit);
		Long instructorId = request.getInstructorId();
		Long deptId = request.getDepartmentId();
		if (request.getData() != null) {
			instructorId = request.getData().getInstructorId();
			deptId = request.getData().getDepartmentId();
		}
		if (deptId == null) {
			String dept = (String)context.getAttribute(SessionAttribute.DepartmentId);
			if (dept != null) {
				try {
					deptId = Long.valueOf(dept);
				} catch (NumberFormatException e) {}
			}
		}
		
		if (request.getOperation() != null) {
			switch (request.getOperation()) {
			case SAVE:
				saveInstructor(request.getData(), context);
				return request.getData();
			case PREVIOUS:
				saveInstructor(request.getData(), context);
				instructorId = request.getData().getPreviousId();
				break;
			case NEXT:
				saveInstructor(request.getData(), context);
				instructorId = request.getData().getNextId();
				break;
			case DELETE:
				deleteInstructor(instructorId, context);
				return null;
			default:
				// do nothing
			}
		}
		
		return loadInstructor(instructorId, deptId, context);
	}
	
	protected void saveInstructor(InstructorEditResponse data, SessionContext context) {
		context.hasPermission(data.getInstructorId(), Right.InstructorEdit);
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		Transaction tx = null;
		try {	
			tx = hibSession.beginTransaction();
			
			Query<DepartmentalInstructor> q = hibSession.createQuery(
					"from DepartmentalInstructor where externalUniqueId=:puid and department.uniqueId=:deptId" +
					(data.getInstructorId() == null ? "" : " and uniqueId!=:uniqueId")
					, DepartmentalInstructor.class);
			q.setParameter("puid", data.getExternalId());
			q.setParameter("deptId", data.getDepartmentId());
			if (data.getInstructorId() != null)
				q.setParameter("uniqueId", data.getInstructorId());
			if (!q.list().isEmpty())
				throw new GwtRpcException(MSG.errorInstructorIdAlreadyExistsInList());
			
			DepartmentalInstructor inst = null;
			if (data.getInstructorId() != null) {
				inst = DepartmentalInstructorDAO.getInstance().get(data.getInstructorId(), hibSession);
			} else {    
			    inst = new DepartmentalInstructor();
			    inst.setAttributes(new HashSet<InstructorAttribute>());
			    inst.setDepartment(DepartmentDAO.getInstance().get(data.getDepartmentId(), hibSession));
			    inst.getDepartment().getInstructors().add(inst);
			}
			
			inst.setFirstName(data.getFirstName());
			inst.setMiddleName(data.getMiddleName());
			inst.setLastName(data.getLastName());
			inst.setAcademicTitle(data.getAcademicTitle());
			inst.setExternalUniqueId(data.getExternalId());
			inst.setEmail(data.getEmail());
			inst.setPositionType(data.getPositionId() == null ? null : PositionTypeDAO.getInstance().get(data.getPositionId(), hibSession));
			if (data.getNote() != null && data.getNote().length() > 2048)
				data.setNote(data.getNote().substring(0, 2048));
			inst.setNote(data.getNote());
			inst.setIgnoreToFar(data.isIgnoreTooFar());
			inst.setCareerAcct(data.getCareerAcct());
            
            if (inst.getUniqueId() == null)
            	hibSession.persist(inst);
            else
            	hibSession.merge(inst);

            ChangeLog.addChange(
                    hibSession, 
                    context,
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    (data.getInstructorId() == null ? ChangeLog.Operation.CREATE : ChangeLog.Operation.UPDATE), 
                    null, 
                    inst.getDepartment());
            
            tx.commit();

            if (data.getInstructorId() == null)
            	data.setInstructorId(inst.getUniqueId());
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw new GwtRpcException(e.getMessage(), e);
        }
	}

	protected void deleteInstructor(Long instructorId, SessionContext context) {
		context.hasPermission(instructorId, "DepartmentalInstructor", Right.InstructorDelete);
		org.hibernate.Session hibSession = DepartmentalInstructorDAO.getInstance().getSession();
		Transaction tx = null;
		try {	
			tx = hibSession.beginTransaction();
	        DepartmentalInstructor inst = DepartmentalInstructorDAO.getInstance().get(instructorId, hibSession);
	        
            ChangeLog.addChange(
                    hibSession, 
                    context, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    null, 
                    inst.getDepartment());

            HashSet<Class_> updatedClasses = new HashSet<Class_>();
            for (Iterator<ClassInstructor> i=inst.getClasses().iterator();i.hasNext();) {
	        	ClassInstructor ci = i.next();
	        	Class_ c = ci.getClassInstructing();
	        	updatedClasses.add(c);
	        	c.getClassInstructors().remove(ci);
	        	hibSession.remove(ci);
	        }           
            
            for (Iterator<Exam> i=inst.getExams().iterator();i.hasNext();) {
                Exam exam = i.next();
                exam.getInstructors().remove(inst);
                hibSession.merge(exam);
            }
	        
	        for (Iterator<Assignment> i=inst.getAssignments().iterator();i.hasNext();) {
	        	Assignment a = i.next();
	        	a.getInstructors().remove(inst);
	        	hibSession.merge(a);
	        }
	        
	        Department d = null;
	        if (inst.getDepartment()!=null) {
	        	d = inst.getDepartment();
	        }
	        d.getInstructors().remove(inst);
	        
            hibSession.remove(inst);
            
	        tx.commit();
			
            String className = ApplicationProperty.ExternalActionClassEdit.value();
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).getDeclaredConstructor().newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}
			
	        hibSession.clear();
			
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw new GwtRpcException(e.getMessage(), e);
        }
	}

	protected InstructorEditResponse loadInstructor(Long instructorId, Long departmentId, SessionContext context) {
		InstructorEditResponse data = new InstructorEditResponse();
		
		DepartmentalInstructor instructor = (instructorId == null ? null : DepartmentalInstructorDAO.getInstance().get(instructorId));
		if (instructor != null) {
			context.hasPermission(instructor, Right.InstructorEdit);
			data.setInstructorId(instructor.getUniqueId());
			data.setDepartmentId(instructor.getDepartment().getUniqueId());
			data.setExternalId(instructor.getExternalUniqueId());
			data.setFirstName(instructor.getFirstName());
			data.setMiddleName(instructor.getMiddleName());
			data.setLastName(instructor.getLastName());
			data.setAcademicTitle(instructor.getAcademicTitle());
			data.setDepartment(instructor.getDepartment().toString());
			data.setEmail(instructor.getEmail());
			data.setIgnoteTooFar(instructor.getIgnoreToFar());
			data.setPositionId(instructor.getPositionType() == null ? null : instructor.getPositionType().getUniqueId());
			data.setCareerAcct(instructor.getCareerAcct());
			data.setNote(instructor.getNote());
			
			DepartmentalInstructor next = instructor.getNextDepartmentalInstructor(context, Right.InstructorEdit); 
			data.setNextId(next==null ? null : next.getUniqueId());
	        DepartmentalInstructor previous = instructor.getPreviousDepartmentalInstructor(context, Right.InstructorEdit); 
	        data.setPreviousId(previous == null ? null : previous.getUniqueId());
	        data.setCanDelete(context.hasPermission(instructor, Right.InstructorDelete));
	        
	        String nameFormat = UserProperty.NameFormat.get(context.getUser());
	        BackTracker.markForBack(context,
	        		"instructor?id=" + instructor.getUniqueId(),
	        		MSG.backInstructor(instructor.getName(nameFormat)),
	        		true, false);
		} else if (departmentId != null) {
			data.setDepartmentId(departmentId);
			Department dept = DepartmentDAO.getInstance().get(departmentId);
			data.setDepartment(dept.toString());
			context.hasPermission(dept, Right.InstructorAdd);
		} else {
			throw new GwtRpcException(MSG.errorDoesNotExists(MSG.columnInstructor()));
		}
		
		for (PositionType type: PositionType.findAll())
			data.addPosition(type.getUniqueId(), type.getLabel());
		data.setConfirms(JavascriptFunctions.isJsConfirm(context));
		data.setCanEditExternalId(ApplicationProperty.InstructorAllowEditExternalId.isTrue());
		
		return data;
	}

}
