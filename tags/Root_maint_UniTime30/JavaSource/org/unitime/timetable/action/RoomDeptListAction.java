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

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.RoomDeptListForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomDeptList" name="roomDeptListForm" input="/admin/roomDeptList.jsp" parameter="doit" scope="request" validate="true"
 * @struts.action-forward name="showRoomDeptList" path="roomDeptListTile"
 */
public class RoomDeptListAction extends Action {

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
		RoomDeptListForm roomDeptListForm = (RoomDeptListForm) form;
		HttpSession webSession = request.getSession();
		
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		buildDeptTable(request, roomDeptListForm);
		return mapping.findForward("showRoomDeptList");
	}
	
	/**
	 * 
	 * @param request
	 * @param roomDeptListForm
	 * @throws Exception
	 */
	private void buildDeptTable(HttpServletRequest request, RoomDeptListForm roomDeptListForm) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		
		WebTable.setOrder(request.getSession(),"roomDeptList.ord",request.getParameter("ord"),1);
		
		WebTable webTable = new WebTable(5, "Room Departments", "roomDeptList.do?ord=%%", new String[] {
				"Dept", "Department Abbreviation", "Room", "Capacity", "Room Availability &amp; Sharing"},
				new String[] { "left", "left", "left", "right", "left" }, new boolean[] {true, true, true, true, true});
		webTable.setRowStyle("white-space:nowrap");
		org.hibernate.Session hibSession = null;
		
		boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
		String timeGridSize = RequiredTimeTable.getTimeGridSize(user);
		
		//get depts owned by user
		Set depts = getDepts(user);
		
		for (Iterator iter = depts.iterator(); iter.hasNext(); ) {
			Department d = (Department)iter.next();
			Set rooms = new TreeSet();				
			
			for (Iterator iterRD = d.getRoomDepts().iterator(); iterRD.hasNext();){
				RoomDept rd = (RoomDept) iterRD.next();
				if (rd.getRoom() instanceof Room)
					rooms.add(rd.getRoom());
			}
			
			if (!rooms.isEmpty()) {
				String rmLabel = "";
				String rmCapacity = "";
				String rmDept = "";
				
				boolean firstRoom = true;
				for (Iterator iterRoom = rooms.iterator(); iterRoom.hasNext();) {
					Room room = (Room) iterRoom.next();
					rmLabel += "<TR><TD nowrap "+(!firstRoom?"style='border-top:black 1px dashed;'":"")+">";
					rmCapacity += "<TR><TD nowrap "+(!firstRoom?"style='border-top:black 1px dashed;'":"")+" align='right'>";
					rmLabel = rmLabel + room.getLabel();
					rmCapacity = rmCapacity + room.getCapacity().toString();
					rmLabel += "<B>&nbsp;</B></TD></TR>";
					rmCapacity += "<B>&nbsp;</B></TD></TR>";
					
				    // get pattern column
				    RequiredTimeTable rtt = room.getRoomSharingTable();
				    rtt.getModel().setDefaultSelection(timeGridSize);
				    File imageFileName = null;
				    try {
				    	imageFileName = rtt.createImage(false);
				    } catch (IOException ex) {
				    	ex.printStackTrace();
				    }
				    
					TreeSet sortedDepts = new TreeSet(new Comparator() {
						public int compare(Object o1, Object o2) {
							Department d1 = (Department)o1;
							Department d2 = (Department)o2;
							return d1.getAbbreviation().compareTo(d2.getAbbreviation());
						}
					});
					for (Iterator iterDept = room.getRoomDepts().iterator(); iterDept.hasNext();) {
						RoomDept rd = (RoomDept) iterDept.next();
						Department department = rd.getDepartment();
						sortedDepts.add(department);
					}
					
					int deptRow =0;
					rmDept += "<TR><TD nowrap valign='center' "+(firstRoom?"":"style='border-top:black 1px dashed' ")+"rowspan='"+Math.max(2,sortedDepts.size())+"'>";
				    if (imageFileName!=null){
				    	rmDept += "<img border='0' src='temp/"+(imageFileName.getName())+"'>&nbsp;&nbsp;";
				    }
					rmDept += "</TD>";
					for (Iterator it = sortedDepts.iterator(); it.hasNext();) {
						Department department = (Department) it.next();
						if (deptRow > 0) {
							rmLabel += "<TR><TD><B>&nbsp;</B></TD></TR>";
							rmCapacity += "<TR><TD><B>&nbsp;</B></TD></TR>";
						} 
						rmDept += (deptRow>0?"<TR>":"");
						rmDept += "<TD nowrap width='100%' style='color:#"+department.getRoomSharingColor(null)+";font-weight:bold;"+(!firstRoom && deptRow==0?"border-top:black 1px dashed;":"")+"'>";
						rmDept += department.getAbbreviation();
						rmDept += "</TD></TR>";
						deptRow++;
					}
					while (deptRow<2) {
						rmLabel += "<TR><TD><B>&nbsp;</B></TD></TR>";
						rmCapacity += "<TR><TD><B>&nbsp;</B></TD></TR>";
						rmDept += "<TR><TD><B>&nbsp;</B></TD></TR>";
						deptRow++;
					}
					firstRoom = false;
				}
					
				webTable.addLine(
						"onClick=\"document.location='roomDeptEdit.do?doit=editRoomDept&id="+ d.getUniqueId() + "';\"",
						new String[] {
							d.getDeptCode(),
							d.getAbbreviation(),
							"<table width='100%' border='0' cellspacing='0' cellpadding='1'>"+rmLabel+"</table>",
							"<table width='100%' border='0' cellspacing='0' cellpadding='1'>"+rmCapacity+"</table>",
							"<table width='100%' border='0' cellspacing='0' cellpadding='1'>"+rmDept+"</table>"}, 
						new Comparable[] {
							d.getDeptCode(),
							d.getAbbreviation(),
							null, null, null}
						);
			} else {
				webTable.addLine(
						"onClick=\"document.location='roomDeptEdit.do?doit=editRoomDept&id="+ d.getUniqueId() + "';\"",
						new String[] {
							d.getDeptCode(),
							d.getAbbreviation(),
							"<I>No Room Currently Assigned</I>",
							"",
							""}, 
						new Comparable[] {
							d.getDeptCode(),
							d.getAbbreviation(),
							null, null, null}
						);					
			}
			request.setAttribute("roomDepts", webTable.printTable(WebTable.getOrder(request.getSession(),"roomDeptList.ord")));
		}
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	private Set getDepts(User user) throws Exception {
		boolean isAdmin = user.getRole().equals(Roles.ADMIN_ROLE);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager manager = tdao.get(new Long(mgrId));
		
		Set depts = new TreeSet();
		if (isAdmin) {
			List list = tdao.getSession().createCriteria(Department.class).add(Restrictions.eq("sessionId", sessionId)).list();
			if (list!=null)
				depts.addAll(list);
		} else {
			Set departments = manager.departmentsForSession(sessionId);
			if (departments!=null)
				depts.addAll(departments);
		}
		
		return depts;
	}
	
}

