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

import java.io.OutputStream;
import java.util.Date;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.form.ExamGridForm;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.RoomAvailability;
import org.unitime.timetable.webutil.timegrid.PdfExamGridTable;


/** 
 * @author Tomas Muller
 */
@Service("/examGrid")
public class ExamGridAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ExamGridForm myForm = (ExamGridForm) form;
        // Check Access
		sessionContext.checkPermission(Right.ExaminationTimetable);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        if (op==null && request.getParameter("resource")!=null) op="Change";
        
        if ("Change".equals(op) || "Export PDF".equals(op)) {
        	myForm.save(sessionContext);
        }
        
        myForm.load(sessionContext);
        
        if (myForm.getExamType() == null) {
			TreeSet<ExamType> types = ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId());
			if (!types.isEmpty())
				myForm.setExamType(types.first().getUniqueId());
        }
        
        if ("Cbs".equals(op)) {
            if (request.getParameter("resource")!=null)
                myForm.setResource(Integer.parseInt(request.getParameter("resource")));
            if (request.getParameter("filter")!=null)
                myForm.setFilter(request.getParameter("filter"));
        }
        
        if (RoomAvailability.getInstance()!=null && myForm.getExamType() != null) {
            Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
            Date[] bounds = ExamPeriod.getBounds(session, myForm.getExamType());
            String exclude = ExamTypeDAO.getInstance().get(myForm.getExamType()).getType() == ExamType.sExamTypeFinal ? RoomAvailabilityInterface.sFinalExamType : RoomAvailabilityInterface.sMidtermExamType;
            if (bounds != null) {
            	RoomAvailability.getInstance().activate(session,bounds[0],bounds[1],exclude,false);
            	RoomAvailability.setAvailabilityWarning(request, session, myForm.getExamType(), true, false);
            }
        }
        
        PdfExamGridTable table = new PdfExamGridTable(myForm, sessionContext, examinationSolverService.getSolver());
        
        request.setAttribute("table", table);

        if ("Export PDF".equals(op)) {
        	OutputStream out = ExportUtils.getPdfOutputStream(response, "timetable");
        	table.export(out);
        	out.flush(); out.close();
        	return null;
        }

        myForm.setOp("Change");
        
        LookupTables.setupExamTypes(request, sessionContext.getUser(), DepartmentStatusType.Status.ExamTimetable);
        
        return mapping.findForward("showGrid");
	}

}

