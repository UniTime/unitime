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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomDeptEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.comparators.RoomTypeComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.util.Constants;


/**
 * MyEclipse Struts Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * 
 * @struts.action path="/roomDeptEdit" name="roomDeptEditForm"
 *                input="/admin/roomDeptEdit.jsp" parameter="doit"
 *                scope="request" validate="true"
 * @struts.action-forward name="showEdit" path="roomDeptEditTile"
 * @struts.action-forward name="showRoomDeptList" path="/roomDeptList.do"
 */
public class RoomDeptEditAction extends LookupDispatchAction {

	// --------------------------------------------------------- Instance
	// Variables

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	protected Map getKeyMethodMap() {
		Map map = new HashMap();
		map.put("editRoomDept", "editRoomDept");
		map.put("button.editRoomSharing", "editRoomDept");
		map.put("button.update", "updateRoomDept");
		map.put("button.returnToRoomDeptList", "cancelRoomDept");
		return map;
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 * @throws Exception
	 */
	public ActionForward editRoomDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws HibernateException, Exception {

		RoomDeptEditForm roomDeptEditForm = (RoomDeptEditForm) form;
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();

		// get department from session
		Department d = new Department();
		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
			String deptCode = webSession.getAttribute(
					Constants.DEPT_CODE_ATTR_ROOM_NAME).toString();
			d = Department.findByDeptCode(deptCode, sessionId);
			roomDeptEditForm.setId(d.getUniqueId().toString());
		}

		// get department from request
		if (request.getParameter("id") != null) {
			Long id = Long.valueOf(request.getParameter("id"));
			roomDeptEditForm.setId(id.toString());
			DepartmentDAO ddao = new DepartmentDAO();
			d = ddao.get(id);
		}

		roomDeptEditForm.setDeptAbbv(d.getName());
		roomDeptEditForm.setDeptCode(d.getDeptCode());

		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(
				user);
		Collection assigned = getAssignedRooms(rooms, d);
		Collection available = getAvailableRooms(rooms, d);

		// TreeSet sortedAssignedRooms = new TreeSet(assigned);
		roomDeptEditForm.setAssignedRooms(assigned);

		// TreeSet sortedAvailableRooms = new TreeSet(available);
		roomDeptEditForm.setNotAssignedRooms(available);

		roomDeptEditForm.setRooms();

		return mapping.findForward("showEdit");
	}

	/**
	 * 
	 * @param user
	 * @param d
	 * @return
	 * @throws Exception
	 */
	private Collection getAvailableRooms(Collection rooms, Department d)
			throws Exception {
		// get rooms owned by user
		ArrayList available = new ArrayList();
		for (Iterator iter = rooms.iterator(); iter.hasNext();) {
			Location location = (Location) iter.next();
			if (location instanceof Room && !((Room) location).hasRoomDept(d))
				available.add(location);
		}
		Collections.sort(available, new RoomTypeComparator());
		return available;
	}

	/**
	 * 
	 * @param user
	 * @param rooms
	 * @param d
	 * @return
	 * @throws Exception
	 */
	private Collection getAssignedRooms(Collection rooms, Department d)
			throws Exception {
		ArrayList assigned = new ArrayList();
		for (Iterator iter = rooms.iterator(); iter.hasNext();) {
			Location location = (Location) iter.next();
			Room r = (location instanceof Room ? (Room) location : null);
			if (location instanceof Room && ((Room) location).hasRoomDept(d))
				assigned.add(location);
		}
		Collections.sort(assigned, new RoomTypeComparator());
		return assigned;
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward cancelRoomDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward("showRoomList");
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateRoomDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		RoomDeptEditForm roomDeptEditForm = (RoomDeptEditForm) form;
		ActionMessages errors = new ActionMessages();

		// Validate input prefs
		errors = roomDeptEditForm.validate(mapping, request);
		if (errors.size() == 0) {
			update(mapping, roomDeptEditForm, request, response);
		} else {
			saveErrors(request, errors);
			editRoomDept(mapping, form, request, response);
			return mapping.findForward("showEdit");
		}

		return mapping.findForward("showRoomList");
	}

	/**
	 * 
	 * @param mapping
	 * @param roomGroupEditForm
	 * @param request
	 * @param response
	 */
	private void update(ActionMapping mapping,
			RoomDeptEditForm roomDeptEditForm, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		Long id = new Long(roomDeptEditForm.getId());
		DepartmentDAO ddao = new DepartmentDAO();
		Department d = ddao.get(id);

		// update rooms
		String[] selectedAssigned = roomDeptEditForm.getAssignedSelected();
		String[] selectedNotAssigned = roomDeptEditForm
				.getNotAssignedSelected();

		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(
				user);
		Collection assignedRooms = getAssignedRooms(rooms, d);
		Collection notAssignedRooms = getAvailableRooms(rooms, d);

		String s1 = null;
		String s2 = null;
		if (selectedAssigned.length != 0)
			s1 = Constants.arrayToStr(selectedAssigned, "", ",");
		else
			s1 = new String();
		if (selectedNotAssigned.length != 0)
			s2 = Constants.arrayToStr(selectedNotAssigned, "", ",");
		else
			s2 = new String();

		RoomDeptDAO rddao = new RoomDeptDAO();
		LocationDAO ldao = new LocationDAO();
		org.hibernate.Session hibSession = rddao.getSession();
		Transaction tx = null;

		try {
			// move room from assignedRooms to notAssignedRooms
			if (selectedAssigned.length != assignedRooms.size()) {
				tx = hibSession.beginTransaction();
				List list = hibSession.createCriteria(RoomDept.class).add(
						Restrictions.eq("department", d)).addOrder(
						Order.asc("room")).list();

				for (Iterator iter = list.iterator(); iter.hasNext();) {
					RoomDept rd = (RoomDept) iter.next();
					Location location = rd.getRoom();
					if (s1.indexOf(location.getUniqueId().toString()) == -1) {
						ChangeLog.addChange(hibSession, request, location,
								ChangeLog.Source.ROOM_DEPT_EDIT,
								ChangeLog.Operation.DELETE, null, rd
										.getDepartment());
						d.getRoomDepts().remove(rd);
						location.getRoomDepts().remove(rd);
						hibSession.saveOrUpdate(rd.getRoom());
						hibSession.delete(rd);
						location.removedFromDepartment(d, hibSession);
					}
				}

				hibSession.flush();
				tx.commit();
			}

			// move room from notAssignedRooms to assignedRooms
			if (selectedNotAssigned.length != 0) {
				tx = hibSession.beginTransaction();
				for (int i = 0; i < selectedNotAssigned.length; i++) {
					Location location = ldao.get(Long
							.valueOf(selectedNotAssigned[i]));
					Room room = (location instanceof Room ? (Room) location
							: null);
					if (room != null) {
						RoomDept rd = new RoomDept();
						rd.setDepartment(d);
						rd.setRoom(room);
						rd.setControl(Boolean.FALSE);
						d.getRoomDepts().add(rd);
						room.getRoomDepts().add(rd);
						hibSession.saveOrUpdate(room);
						hibSession.saveOrUpdate(rd);
						ChangeLog.addChange(hibSession, request, location,
								ChangeLog.Source.ROOM_DEPT_EDIT,
								ChangeLog.Operation.CREATE, null, rd
										.getDepartment());
					}
				}

				hibSession.saveOrUpdate(d);
				tx.commit();
			}
		} catch (Exception e) {
			Debug.error(e);
			try {
				if (tx != null && tx.isActive())
					tx.rollback();
			} catch (Exception e1) {
			}
			throw e;
		}
		hibSession.refresh(d);
	}

}
