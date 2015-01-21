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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomListForm;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 06-06-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomSearch" name="roomListForm" input="/admin/roomSearch.jsp" scope="request"
 * @struts.action-forward name="showRoomList" path="roomListTile"
 * @struts.action-forward name="roomList" path="/roomList.do"
 * @struts.action-forward name="showRoomSearch" path="roomSearchTile"
 *
 * @author Tomas Muller
 */
@Service("/roomSearch")
public class RoomSearchAction extends Action {
	
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
	 * @throws Exception 
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		RoomListForm roomListForm = (RoomListForm) form;
		
		sessionContext.checkPermission(Right.Rooms);
		roomListForm.load(request.getSession());
		
		String deptCode = roomListForm.getDeptCodeX();
		if (deptCode==null) {
			deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
		}
		if (deptCode==null) {
			deptCode = request.getParameter("default");
		    if (deptCode != null)
		    	sessionContext.setAttribute(SessionAttribute.DepartmentCodeRoom, deptCode);
		}
		
		if (deptCode != null && !deptCode.isEmpty() && ("All".equals(deptCode) || deptCode.matches("Exam[0-9]*") || sessionContext.hasPermission(deptCode, "Department", Right.Rooms))) {
			roomListForm.setDeptCodeX(deptCode);
			return mapping.findForward("roomList");
			
		} else {
			if (sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").size() == 1) {
				roomListForm.setDeptCodeX(sessionContext.getUser().getCurrentAuthority().getQualifiers("Department").get(0).getQualifierReference());
				return mapping.findForward("roomList");
			}
			
			LookupTables.setupDepartments(request, sessionContext, true);
			LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
			
			return mapping.findForward("showRoomSearch");
		}

	}
	
}

