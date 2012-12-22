/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.action;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomFeatureListForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 06-27-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomFeatureSearch" name="roomFeatureListForm" input="/admin/roomFeatureSearch.jsp" scope="request"
 * @struts.action-forward name="roomFeatureList" path="/roomFeatureList.do"
 * @struts.action-forward name="showRoomFeatureSearch" path="roomFeatureSearchTile"
 * @struts.action-forward name="showRoomFeatureList" path="roomFeatureListTile"
 */
@Service("/roomFeatureSearch")
public class RoomFeatureSearchAction extends Action {
	
	@Autowired SessionContext sessionContext;

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		RoomFeatureListForm roomFeatureListForm = (RoomFeatureListForm) form;
		
		//Check permissions
		sessionContext.checkPermission(Right.RoomFeatures);
		
		String deptCode = roomFeatureListForm.getDeptCodeX();
		if (deptCode==null) {
			deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
		}
		if (deptCode==null) {
			deptCode = request.getParameter("default");
		    if (deptCode != null)
		    	sessionContext.setAttribute(SessionAttribute.DepartmentCodeRoom, deptCode);
		}
		
		if (deptCode != null && !deptCode.isEmpty() &&
				("All".equals(deptCode) || deptCode.matches("Exam[0-9]*") || sessionContext.hasPermission(deptCode, "Department", Right.RoomFeatures))) {
			roomFeatureListForm.setDeptCodeX(deptCode);
			
			if ("Export PDF".equals(request.getParameter("op"))) {
				sessionContext.checkPermission(Right.RoomFeaturesExportPdf);
				OutputStream out = ExportUtils.getPdfOutputStream(response, "roomFeatures");
				RoomFeatureListAction.printPdfFeatureTable(out, sessionContext, roomFeatureListForm);
				out.flush(); out.close();
				return null;
			}

			return mapping.findForward("roomFeatureList");
		} else {
			
			if (sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").size() == 1) {
				roomFeatureListForm.setDeptCodeX(sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").get(0).getQualifierReference());
				return mapping.findForward("roomFeatureList");
			}
			
			LookupTables.setupDepartments(request, sessionContext, true);
			LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
			
			return mapping.findForward("showRoomFeatureSearch");
		}
	}

}

