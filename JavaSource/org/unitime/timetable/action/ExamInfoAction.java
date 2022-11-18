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
import org.unitime.localization.messages.ExaminationMessages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ExamInfoForm;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo;
import org.unitime.timetable.solver.exam.ui.ExamInfoModel;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
@Action(value = "examInfo", results = {
		@Result(name = "show", type = "tiles", location = "examInfo.tiles")
	})
@TilesDefinition(name = "examInfo.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Examination Assignment"),
		@TilesPutAttribute(name = "body", value = "/exam/info.jsp"),
		@TilesPutAttribute(name = "showMenu", value = "false"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "exams")
	})
public class ExamInfoAction extends UniTimeAction<ExamInfoForm> {
	private static final long serialVersionUID = 3810998750494935506L;
	protected static final ExaminationMessages MSG = Localization.create(ExaminationMessages.class);
	
	private Long examId;
	private Long period;
	private String room;
	private Integer suggestion;
	private Long delete;
	public Long getExamId() { return examId; }
	public void setExamId(Long examId) { this.examId = examId; }
	public Long getPeriod() { return period; }
	public void setPeriod(Long period) { this.period = period; }
	public String getRoom() { return room; }
	public void setRoom(String room) { this.room = room; }
	public Integer getSuggestion() { return suggestion; }
	public void setSuggestion(Integer suggestion) { this.suggestion = suggestion; }
	public Long getDelete() { return delete; }
	public void setDelete(Long delete) { this.delete = delete; }
	
    public String execute() throws Exception {
    	if (form == null) {
	    	form = new ExamInfoForm();
	    	form.reset();
	    }
	    
    	if (form.getOp() != null) op = form.getOp();
    	
        ExamInfoModel model = (ExamInfoModel)sessionContext.getAttribute(SessionAttribute.ExamInfoModel);
        if (model==null) {
            model = new ExamInfoModel();
            sessionContext.setAttribute(SessionAttribute.ExamInfoModel, model);
        }

        if (op == null && model.getExam() != null && examId == null) {
            op = MSG.buttonApply();
        }

        if (MSG.buttonApply().equals(op)) {
            form.save(request.getSession());
        } else if (MSG.buttonRefresh().equals(op)) {
            form.reset();
        }
        
        form.load(request.getSession());
        form.setModel(model);
        model.apply(request, form);
        
        if (op==null) {
            model.clear(sessionContext.getUser());
        } else if (MSG.buttonApply().equals(op)) {
            model.refreshRooms();
            model.refreshSuggestions();
        } if (MSG.buttonSearchDeeper().equals(op)) {
            form.setDepth(form.getDepth()+1);
            form.save(request.getSession());
            model.refreshSuggestions();
        } else if (MSG.buttonSearchLonger().equals(op)) {
            form.setTimeout(2*form.getTimeout());
            form.save(request.getSession());
            model.refreshSuggestions();
        }
        
        model.setSolver(WebSolver.getExamSolver(request.getSession()));
        
        if (examId != null) {
            model.setExam(new ExamDAO().get(examId));
            form.save(request.getSession());
        }
        
        if (model.getExam()==null) throw new Exception("No exam given.");
        
        sessionContext.checkPermission(model.getExam().getExam(), Right.ExaminationAssignment);
        form.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
        form.setExamTypeId(model.getExam().getExamTypeId());
        
        if (RoomAvailability.getInstance()!=null && op==null) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = ExamPeriod.getBounds(session, model.getExam().getExamType().getUniqueId());
            String exclude = model.getExam().getExamType().getReference();
            RoomAvailability.getInstance().activate(session.getUniqueId(),bounds[0],bounds[1],exclude,false);
            RoomAvailability.setAvailabilityWarning(request, session, model.getExam().getExamType().getUniqueId(), true, true);
        }
        
        if ("Select".equals(op)) {
            synchronized (model) {
                if (period != null)
                    model.setPeriod(period);
                if (room != null)
                    model.setRooms(room);
                if (suggestion != null)
                    model.setSuggestion(suggestion);
                if (delete != null)
                    model.delete(delete);
            }
        }
        
        if (MSG.buttonAssign().equals(op)) {
            synchronized (model) {
                String message = model.assign();
                if (message==null || message.trim().length()==0) {
                    form.setOp("Close");
                } else {
                    form.setMessage(message);
                }
            }
        }

        if (MSG.buttonClose().equals(op) || "Close".equals(op)) {
            form.setOp("Close");
        }
        
        return "show";
    }

    public void printCbsHeader() {
    	ExamConflictStatisticsInfo.printHtmlHeader(getPageContext().getOut());
    }
    
    public void printCbs() {
    	form.getModel().getCbs().printHtml(
    			getPageContext().getOut(),
    			form.getModel().getExam().getExamId(),
    			1.0,
    			ExamConflictStatisticsInfo.TYPE_CONSTRAINT_BASED,
    			true); 
    }
}
