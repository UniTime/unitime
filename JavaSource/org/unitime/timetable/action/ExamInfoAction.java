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
import org.unitime.timetable.form.ExamInfoForm;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
@Service("/examInfo")
public class ExamInfoAction extends Action {
	
	@Autowired SessionContext sessionContext;
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamInfoForm myForm = (ExamInfoForm) form;
        
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        ExamInfoModel model = (ExamInfoModel)request.getSession().getAttribute("ExamInfo.model");
        if (model==null) {
            model = new ExamInfoModel();
            request.getSession().setAttribute("ExamInfo.model", model);
        }

        if (op==null && model.getExam()!=null && request.getParameter("examId")==null) {
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
            model.clear(sessionContext.getUser());
        } else if ("Apply".equals(op)) {
            model.refreshRooms();
            model.refreshSuggestions();
        } if ("Search Deeper".equals(op)) {
            myForm.setDepth(myForm.getDepth()+1);
            myForm.save(request.getSession());
            model.refreshSuggestions();
        } else if ("Search Longer".equals(op)) {
            myForm.setTimeout(2*myForm.getTimeout());
            myForm.save(request.getSession());
            model.refreshSuggestions();
        }
        
        model.setSolver(WebSolver.getExamSolver(request.getSession()));
        
        if (request.getParameter("examId")!=null) {
            model.setExam(new ExamDAO().get(Long.valueOf(request.getParameter("examId"))));
            myForm.save(request.getSession());
        }
        
        if (model.getExam()==null) throw new Exception("No exam given.");
        
        sessionContext.checkPermission(model.getExam().getExam(), Right.ExaminationAssignment);
        myForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
        myForm.setExamTypeId(model.getExam().getExamTypeId());
        
        if (RoomAvailability.getInstance()!=null && op==null) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = ExamPeriod.getBounds(session, model.getExam().getExamType().getUniqueId());
            String exclude = model.getExam().getExamType().getReference();
            RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],exclude,false);
            RoomAvailability.setAvailabilityWarning(request, session, model.getExam().getExamType().getUniqueId(), true, true);
        }
        
        if ("Select".equals(op)) {
            synchronized (model) {
                if (request.getParameter("period")!=null)
                    model.setPeriod(Long.valueOf(request.getParameter("period")));
                if (request.getParameter("room")!=null)
                    model.setRooms(request.getParameter("room"));
                if (request.getParameter("suggestion")!=null)
                    model.setSuggestion(Integer.parseInt(request.getParameter("suggestion")));
                if (request.getParameter("delete")!=null)
                    model.delete(Long.valueOf(request.getParameter("delete")));
            }
        }
        
        if ("Assign".equals(op)) {
            synchronized (model) {
                String message = model.assign();
                if (message==null || message.trim().length()==0) {
                    myForm.setOp("Close");
                } else {
                    myForm.setMessage(message);
                }
            }
        }

        if ("Close".equals(op)) {
            myForm.setOp("Close");
        }
        
        /*
        BackTracker.markForBack(
                request,
                "examInfo.do?examId=" + model.getExam().getExamId(),
                "Exam Info ("+ model.getExam().getExamName() +")",
                true, false);
        */
        
        return mapping.findForward("show");        
    }

}
