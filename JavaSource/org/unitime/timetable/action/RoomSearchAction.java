/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.action;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
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
 */
public class RoomSearchAction extends Action {

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
		
		//Check permissions
		HttpSession httpSession = request.getSession();
		if (!Web.isLoggedIn(httpSession)) {
			throw new Exception("Access Denied.");
		}

		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		
		// Check if dept code saved to session
		Object dc = roomListForm.getDeptCodeX();
		if (dc==null)
		    dc = httpSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME);
		if (dc==null) {
		    dc = request.getParameter("default");
		    if (dc!=null)
		        httpSession.setAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME, dc);
		}
		String deptCode = "";
		
		// Dept code is saved to the session - go to instructor list
		if (dc != null && !Constants.BLANK_OPTION_VALUE.equals(dc)) {
			deptCode = dc.toString();
			roomListForm.setDeptCodeX(deptCode);
			if (!roomListForm.getDeptCodeX().equalsIgnoreCase("All") && !roomListForm.getDeptCodeX().equalsIgnoreCase("Exam") && !roomListForm.getDeptCodeX().equalsIgnoreCase("EExam")) {
				if (Session.getCurrentAcadSession(user).getRoomsFast(new String[] {roomListForm.getDeptCodeX()}).size() == 0) {
					ActionMessages errors = new ActionMessages();
					errors.add("searchResult", new ActionMessage("errors.generic", "No rooms for the selected department were found."));
					saveErrors(request, errors);
				}
			}
			
			int examType = -1;
			if ("Exam".equals(roomListForm.getDeptCodeX())) examType = Exam.sExamTypeFinal;
			if ("EExam".equals(roomListForm.getDeptCodeX())) examType = Exam.sExamTypeMidterm;
			
			if ("Export PDF".equals(request.getParameter("op"))) {
				RoomListAction.buildPdfWebTable(request, roomListForm, "yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_ROOMS_FEATURES_ONE_COLUMN)),
				        examType);
			}
			
			return mapping.findForward("roomList");
		} else {
            // No session attribute found - Load dept code
			LookupTables.setupDeptsForUser(request, user, sessionId, true);
            
            TimetableManager owner = new TimetableManagerDAO().get(Long.valueOf((String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME)));
            
            if (owner.isExternalManager()) {
                Set depts = Department.findAllOwned(sessionId, owner, true);
                if (depts.size()==1) {
                    roomListForm.setDeptCodeX(((Department)depts.iterator().next()).getDeptCode());
                    httpSession.setAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME, roomListForm.getDeptCodeX());
                    return mapping.findForward("roomList");
                }
            }
			
			return mapping.findForward("showRoomSearch");
		}

	}
	
}

