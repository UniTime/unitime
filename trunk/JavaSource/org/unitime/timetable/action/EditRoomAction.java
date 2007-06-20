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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 07-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoom" name="editRoomForm" input="/admin/editRoom.jsp" scope="request"
 * @struts.action-forward name="showEditRoom" path="editRoomTile"
 */
public class EditRoomAction extends Action {

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
		HttpServletResponse response) throws Exception{
		EditRoomForm editRoomForm = (EditRoomForm) form;
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
		
		MessageResources rsc = getResources(request);
		String doit = editRoomForm.getDoit();

		//return to room list
		if(doit!= null && doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
			response.sendRedirect("roomDetail.do?id="+editRoomForm.getId());
			return null;
			//the following call cannot be used since doit is has the same value as for return to room list (Back)
			//return mapping.findForward("showRoomDetail");
		}
		
		//update location
		if(doit != null && doit.equals(rsc.getMessage("button.update"))) {
			ActionMessages errors = new ActionMessages();
			errors = editRoomForm.validate(mapping, request);
			if (errors.size() == 0) {
				doUpdate(editRoomForm,request);
				response.sendRedirect("roomDetail.do?id="+editRoomForm.getId());
				return null;
			} else {
				saveErrors(request, errors);
			}
		}	
		
		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		if (location instanceof Room) {
			Room r = (Room)location;
			editRoomForm.setName(r.getRoomNumber());
            editRoomForm.setType(r.getScheduledRoomType());
			editRoomForm.setBldgName(r.getBuildingAbbv());
			editRoomForm.setRoom(true);
            editRoomForm.setExternalId(r.getExternalUniqueId());
		} else {
			editRoomForm.setName(((NonUniversityLocation)location).getName());
            editRoomForm.setType(null);
			editRoomForm.setBldgName("");
			editRoomForm.setRoom(false);
            editRoomForm.setExternalId(null);
		}
		editRoomForm.setCapacity(location.getCapacity().toString());
		editRoomForm.setIgnoreTooFar(location.isIgnoreTooFar());
		editRoomForm.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
		editRoomForm.setCoordX(location.getCoordinateX()==null || location.getCoordinateX().intValue()<0?null:location.getCoordinateX().toString());
		editRoomForm.setCoordY(location.getCoordinateY()==null || location.getCoordinateY().intValue()<0?null:location.getCoordinateY().toString());
		editRoomForm.setControlDept(null);
		
		User user = Web.getUser(webSession);
		Session s = Session.getCurrentAcadSession(user);
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
    	boolean admin = Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE});

		Set ownedDepts = owner.departmentsForSession(s.getUniqueId());
		boolean controls = false;
		boolean allDepts = true;
		for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			if (rd.isControl().booleanValue())
				editRoomForm.setControlDept(rd.getDepartment().getUniqueId().toString());
			if (rd.isControl().booleanValue() && ownedDepts!=null && ownedDepts.contains(rd.getDepartment()))
				controls = true;
			if (ownedDepts==null || !ownedDepts.contains(rd.getDepartment())) {
				allDepts = false;
			}
		}
		editRoomForm.setOwner(admin || controls || allDepts);
		
		EditRoomAction.setupDepartments(request, location);
		
		return mapping.findForward("showEditRoom");
	}
	
    public static void setupDepartments(HttpServletRequest request, Location location) throws Exception {
    	User user = Web.getUser(request.getSession());
    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();

    	Collection availableDepts = new Vector();

        for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
    		RoomDept rd = (RoomDept)i.next();
			Department d = rd.getDepartment();
			availableDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
		}
		
		request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
    }
	

	/**
	 * 
	 * @param editRoomForm
	 * @param request
	 * @throws Exception 
	 */
	private void doUpdate(EditRoomForm editRoomForm, HttpServletRequest request) throws Exception {
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		org.hibernate.Session hibSession = ldao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			
			Location location = ldao.get(id, hibSession);
					        
			if (editRoomForm.getName() != null && !editRoomForm.getName().trim().equalsIgnoreCase("")) {
				if (location instanceof Room)
					((Room)location).setRoomNumber(editRoomForm.getName());
				else
					((NonUniversityLocation)location).setName(editRoomForm.getName());
			}
				
			if (editRoomForm.getCapacity() != null && !editRoomForm.getCapacity().trim().equalsIgnoreCase("")) {
				location.setCapacity(Integer.valueOf(editRoomForm.getCapacity().trim()));
			}
				
			if (editRoomForm.isIgnoreTooFar() == null || !editRoomForm.isIgnoreTooFar().booleanValue()) {
				location.setIgnoreTooFar(Boolean.FALSE);
			} else {
				location.setIgnoreTooFar(Boolean.TRUE);
			}
			
			if (editRoomForm.isIgnoreRoomCheck() == null || !editRoomForm.isIgnoreRoomCheck().booleanValue()) {
				location.setIgnoreRoomCheck(Boolean.FALSE);
			} else {
				location.setIgnoreRoomCheck(Boolean.TRUE);
			}
            
            if (location instanceof Room) {
                ((Room)location).setExternalUniqueId(editRoomForm.getExternalId());
                ((Room)location).setScheduledRoomType(editRoomForm.getType());
            }
			
			location.setCoordinateX(editRoomForm.getCoordX()==null || editRoomForm.getCoordX().length()==0 ? new Integer(-1) : Integer.valueOf(editRoomForm.getCoordX()));
			location.setCoordinateY(editRoomForm.getCoordY()==null || editRoomForm.getCoordY().length()==0 ? new Integer(-1) : Integer.valueOf(editRoomForm.getCoordY()));
			
			for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
				RoomDept rd = (RoomDept)i.next();
				boolean newControl = editRoomForm.getControlDept()!=null && editRoomForm.getControlDept().equals(rd.getDepartment().getUniqueId().toString());
				if (newControl!=rd.isControl().booleanValue()) {
					rd.setControl(new Boolean(newControl));
					hibSession.saveOrUpdate(rd);
				}
			}

			hibSession.saveOrUpdate(location);
			
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    (Location)location, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    location.getControllingDepartment());

            hibSession.flush();
			tx.commit();
		} catch (Exception e) {
			if (tx!=null) tx.rollback();
			throw e;
		}
	}

}

