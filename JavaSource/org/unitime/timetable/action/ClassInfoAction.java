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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.form.ClassInfoForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.course.ui.ClassInfoModel;
import org.unitime.timetable.util.DefaultRoomAvailabilityService;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
@Service("/classInfo")
public class ClassInfoAction extends Action {
	
	@Autowired SessionContext sessionContext;
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ClassInfoForm myForm = (ClassInfoForm) form;
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0) {
        	op = request.getParameter("op2");
        	myForm.setOp(op);
        }

        ClassInfoModel model = (ClassInfoModel)request.getSession().getAttribute("ClassInfo.model");
        if (model==null) {
            model = new ClassInfoModel();
            request.getSession().setAttribute("ClassInfo.model", model);
        }
        model.setSessionContext(sessionContext);
        
        if (op==null && model.getClass()!=null && request.getParameter("classId")==null) {
            op="Apply";
        }

        if ("Apply".equals(op)) {
            myForm.save(request.getSession());
        } else if ("Refresh".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        myForm.load(request.getSession());
        myForm.setModel(model);
        model.apply(request, myForm);
        
        if (op==null) {
            model.clear(sessionContext.getUser().getExternalUserId());
        } else if ("Apply".equals(op)) {
            model.refreshRooms();
            if (model.isKeepConflictingAssignments()!=myForm.getKeepConflictingAssignments())
            	model.update();
        }
        
        if (request.getParameter("classId")!=null) {
            model.setClazz(new Class_DAO().get(Long.valueOf(request.getParameter("classId"))));
            if (model.getClassAssignment()!=null && (model.getChange()==null || model.getChange().getCurrent(model.getClazz().getClassId())==null)) {
            	model.setTime(model.getClassAssignment().getTimeId());
            }
            myForm.save(request.getSession());
        }
        
        if (model.getClazz()==null) throw new Exception("No class given.");
        
        sessionContext.checkPermission(model.getClazz().getClazz(), Right.ClassAssignment);
        
        if (RoomAvailability.getInstance()!=null && op==null && !(RoomAvailability.getInstance() instanceof DefaultRoomAvailabilityService)) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = DatePattern.getBounds(session.getUniqueId());
            RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],RoomAvailabilityInterface.sClassType, false);
            RoomAvailability.setAvailabilityWarning(request, session, true, true);
        }
        
        if ("Select".equals(op)) {
            synchronized (model) {
                if (request.getParameter("time")!=null)
                    model.setTime(request.getParameter("time"));
                if (request.getParameter("date")!=null)
                    model.setDate(request.getParameter("date"));
                if (request.getParameter("room")!=null)
                    model.setRooms(request.getParameter("room"));
                if (request.getParameter("delete")!=null)
                    model.delete(Long.valueOf(request.getParameter("delete")));
            }
        }
        
        if ("Assign".equals(op)) {
            synchronized (model) {
                String message = model.assign(sessionContext);
                if (message==null || message.trim().length()==0) {
                    myForm.setOp("Close");
                } else {
                    myForm.setMessage(message);
                }
            }
        }
        
        if ("Lock".equals(op)) {
        	InstructionalOffering offering = InstructionalOfferingDAO.getInstance().get(Long.valueOf(request.getParameter("offering")));
        	sessionContext.checkPermission(offering, Right.OfferingCanLock);
        	offering.getSession().lockOffering(offering.getUniqueId());
        }

        if ("Close".equals(op)) {
            myForm.setOp("Close");
            
        }
        if (myForm.getOp() == null || myForm.getOp().equals("Close")){
        	myForm.setKeepConflictingAssignments(false);
            request.getSession().removeAttribute("ClassInfo.KeepConflictingAssignments");
        }
        
        /*
        BackTracker.markForBack(
                request,
                "ClassInfo.do?ClassId=" + model.getClass().getClassId(),
                "Class Info ("+ model.getClass().getClassName() +")",
                true, false);
        */
        
        return mapping.findForward("show");        
    }

}
