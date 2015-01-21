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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomGroupListForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 06-28-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomGroupSearch" name="roomGroupListForm" input="/admin/roomGroupSearch.jsp" scope="request"
 * @struts.action-forward name="showRoomGroupList" path="roomGroupListTile"
 * @struts.action-forward name="roomGroupList" path="/roomGroupList.do""
 * @struts.action-forward name="showRoomGroupSearch" path="roomGroupSearchTile"
 *
 * @author Tomas Muller
 */
@Service("/roomGroupSearch")
public class RoomGroupSearchAction extends Action {
	
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
		RoomGroupListForm roomGroupListForm = (RoomGroupListForm) form;
		
		//Check permissions
		sessionContext.checkPermission(Right.RoomGroups);

		String deptCode = roomGroupListForm.getDeptCodeX();
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
			roomGroupListForm.setDeptCodeX(deptCode);
			
			if ("Export PDF".equals(request.getParameter("op"))) {
				sessionContext.checkPermission(Right.RoomGroupsExportPdf);
				OutputStream out = ExportUtils.getPdfOutputStream(response, "roomGroups");
				RoomGroupListAction.printPdfGroupTable(out, sessionContext, roomGroupListForm);
				out.flush(); out.close();
				return null;
			}
			
			return mapping.findForward("roomGroupList");
		} else {
			
			if (sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").size() == 1) {
				roomGroupListForm.setDeptCodeX(sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").get(0).getQualifierReference());
				
				return mapping.findForward("roomGroupList");
			}
			
			LookupTables.setupDepartments(request, sessionContext, true);
			LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
			
			return mapping.findForward("showRoomGroupSearch");
		}
	}

}

