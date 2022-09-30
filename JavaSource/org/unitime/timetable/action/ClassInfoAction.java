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
package org.unitime.timetable.action;

import java.util.Date;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ClassInfoForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.course.ui.ClassInfoModel;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Action(value = "classInfo", results = {
		@Result(name = "show", type = "tiles", location = "classInfo.tiles")
	})
@TilesDefinition(name = "classInfo.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Class Assignment"),
		@TilesPutAttribute(name = "body", value = "/tt/info.jsp"),
		@TilesPutAttribute(name = "showMenu", value = "false")
	})
public class ClassInfoAction extends UniTimeAction<ClassInfoForm> {
	private static final long serialVersionUID = 7634412254896426556L;
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected Long classId = null;
	protected String op2 = null;
	protected String time, date, room;
	protected Long deleteId;

	public Long getClassId() { return classId; }
	public void setClassId(Long classId) { this.classId = classId; }
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	
	public String getTime() { return time; }
	public void setTime(String time) { this.time = time; }
	public String getRoom() { return room; }
	public void setRoom(String room) { this.room = room; }
	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
	public Long getDelete() { return deleteId; }
	public void setDelete(Long deleteId) { this.deleteId = deleteId; }
	
	@Override
    public String execute() throws Exception {
		if (form == null) {
			form = new ClassInfoForm();
		}
		form.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
        
		if (op == null) op = form.getOp();
        if (op2 != null && !op2.isEmpty()) op = op2;
        form.setOp(op);

        ClassInfoModel model = (ClassInfoModel)sessionContext.getAttribute(SessionAttribute.ClassInfoModel);
        if (model==null) {
            model = new ClassInfoModel();
            sessionContext.setAttribute(SessionAttribute.ClassInfoModel, model);
            String type = ApplicationProperty.ClassAssignmentStudentConflictsType.value();
            if ("none".equalsIgnoreCase(type)) {
            	model.setShowStudentConflicts(false);
            	model.setUseRealStudents(StudentClassEnrollment.sessionHasEnrollments(sessionContext.getUser().getCurrentAcademicSessionId()));
            } else if ("actual".equalsIgnoreCase(type)) {
            	model.setShowStudentConflicts(true);
            	model.setUseRealStudents(true);
            } else if ("solution".equalsIgnoreCase(type)) {
            	model.setShowStudentConflicts(true);
            	model.setUseRealStudents(false);
            } else {
            	// auto
            	model.setUseRealStudents(StudentClassEnrollment.sessionHasEnrollments(sessionContext.getUser().getCurrentAcademicSessionId()));
            }
        }
        model.setSessionContext(sessionContext);
        
        if (op==null && model.getClass()!=null && classId==null) {
            op="Apply";
        }

        if (MSG.actionFilterApply().equals(op) || "Apply".equals(op)) {
            form.save(request.getSession());
        } else if ("Refresh".equals(op)) {
            form.reset();
        }
        
        form.load(request.getSession());
        form.setModel(model);
        model.apply(request, form);
        
        if (op==null) {
            model.clear(sessionContext.getUser().getExternalUserId());
        } else if ("Apply".equals(op)) {
            model.refreshRooms();
            if (model.isKeepConflictingAssignments()!=form.getKeepConflictingAssignments())
            	model.update();
        }
        
        if (classId!=null) {
            model.setClazz(new Class_DAO().get(classId));
            if (model.getClassAssignment()!=null && (model.getChange()==null || model.getChange().getCurrent(model.getClazz().getClassId())==null)) {
            	model.setTime(model.getClassAssignment().getTimeId());
            }
            form.save(request.getSession());
        }
        
        if (model.getClazz()==null) throw new Exception(MSG.errorNoClassGiven());
        
        sessionContext.checkPermission(model.getClazz().getClazz(), Right.ClassAssignment);
        
        if (RoomAvailability.getInstance()!=null && op==null && !(RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService)) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = DatePattern.getBounds(session.getUniqueId());
            RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],RoomAvailabilityInterface.sClassType, false);
            RoomAvailability.setAvailabilityWarning(request, session, true, true);
        }
        
        if ("Select".equals(op)) {
            synchronized (model) {
                if (time!=null)
                    model.setTime(time);
                if (date!=null)
                    model.setDate(date);
                if (room!=null)
                    model.setRooms(room);
                if (deleteId!=null)
                    model.delete(deleteId);
            }
        }
        
        if ("Type".equals(op)) {
        	String type = request.getParameter("type");
        	if ("actual".equalsIgnoreCase(type)) {
            	model.setUseRealStudents(true);
            } else if ("solution".equalsIgnoreCase(type)) {
            	model.setUseRealStudents(false);
            }
        	model.setClazz(model.getClazz().getClazz());
        	model.update();
        }
        
        if (MSG.actionClassAssign().equals(op) || "Assign".equals(op)) {
            synchronized (model) {
                String message = model.assign(sessionContext);
                if (message==null || message.trim().length()==0) {
                    form.setOp("Close");
                } else {
                    form.setMessage(message);
                }
            }
        }
        
        if ("Lock".equals(op)) {
        	InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(Long.valueOf(request.getParameter("offering")));
        	sessionContext.checkPermission(offering, Right.OfferingCanLock);
        	offering.getSession().lockOffering(offering.getUniqueId());
        }

        if ("Close".equals(op)) {
            form.setOp("Close");
            
        }
        if (form.getOp() == null || form.getOp().equals("Close")){
        	form.setKeepConflictingAssignments(false);
            request.getSession().removeAttribute("ClassInfo.KeepConflictingAssignments");
        }
        
        /*
        BackTracker.markForBack(
                request,
                "ClassInfo.do?ClassId=" + model.getClass().getClassId(),
                "Class Info ("+ model.getClass().getClassName() +")",
                true, false);
        */
        
        return "show";        
    }

}
