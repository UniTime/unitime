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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

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
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomFeatureEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:action path="/roomFeatureEdit" name="roomFeatureEditForm" parameter="do" scope="request" validate="true"
 * @struts:action-forward name="showEdit" path="/admin/roomFeatureEdit.jsp"
 * @struts:action-forward name="showAdd" path="/admin/roomFeatureAdd.jsp"
 * @struts:action-forward name="showRoomFeatureList" path="/roomFeatureList.do" redirect="true"
 */
public class RoomFeatureEditAction extends LookupDispatchAction {

	// --------------------------------------------------------- Methods

	/**
	 * 
	 */
	protected Map getKeyMethodMap() {
	     Map map = new HashMap();
	     map.put("editRoomFeature", "editRoomFeature");
	     map.put("button.delete", "deleteRoomFeature");
	     map.put("button.update", "saveRoomFeature");
	     map.put("button.addNew", "saveRoomFeature");
	     map.put("button.returnToRoomFeatureList", "cancelRoomFeature");
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
	public ActionForward editRoomFeature(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws HibernateException, Exception {	
		
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
				
		//get roomFeature from request
		Long id =  new Long(Long.parseLong(request.getParameter("id")));	
		roomFeatureEditForm.setId(id.toString());
		RoomFeatureDAO rdao = new RoomFeatureDAO();
		RoomFeature rf = rdao.get(id);
		
		//set global
		if (rf instanceof GlobalRoomFeature) {
			roomFeatureEditForm.setGlobal(true);
			roomFeatureEditForm.setDeptCode(null);
		}
		
		if (rf instanceof DepartmentRoomFeature){
			roomFeatureEditForm.setGlobal(false);
			Department dept = ((DepartmentRoomFeature)rf).getDepartment();
			roomFeatureEditForm.setDeptCode(dept.getDeptCode());
		}
		
		if (roomFeatureEditForm.getName()==null || roomFeatureEditForm.getName().length()==0)
			roomFeatureEditForm.setName(rf.getLabel());
		
		//get rooms		
		Collection assigned = getAssignedRooms(user, rf, roomFeatureEditForm);
		Collection available = getAvailableRooms(user, rf, roomFeatureEditForm);

		TreeSet sortedAssignedRooms = new TreeSet(assigned);
		roomFeatureEditForm.setAssignedRooms(sortedAssignedRooms);
		
		TreeSet sortedAvailableRooms = new TreeSet(available);
		roomFeatureEditForm.setNotAssignedRooms(sortedAvailableRooms);
		
		roomFeatureEditForm.setRooms();
		
		return mapping.findForward("showEdit");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 */
	public ActionForward deleteRoomFeature(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		Long id = new Long(roomFeatureEditForm.getId());
		RoomFeatureDAO rdao = new RoomFeatureDAO();
		org.hibernate.Session hibSession = rdao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			
			RoomFeature rf = rdao.get(id, hibSession);
			if (rf != null) {
                
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        rf, 
                        ChangeLog.Source.ROOM_FEATURE_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        (rf instanceof DepartmentRoomFeature?((DepartmentRoomFeature)rf).getDepartment():null));

                for (Iterator i=rf.getRooms().iterator();i.hasNext();) {
					Location loc = (Location)i.next();
					loc.getFeatures().remove(rf);
					hibSession.save(loc);
				}
				hibSession.delete(rf);
			}
            
			tx.commit();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
			throw e;
		}
			
		roomFeatureEditForm.setDeptCode((String)request.getSession().getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		return mapping.findForward("showRoomFeatureList");
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
	public ActionForward saveRoomFeature(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		ActionMessages errors = new ActionMessages();
		
		//Validate input prefs
        errors = roomFeatureEditForm.validate(mapping, request);
        if(errors.size()==0) {
			update(mapping, roomFeatureEditForm, request, response);
        }else {
        	saveErrors(request, errors);
        	editRoomFeature(mapping, form, request, response);
        	return mapping.findForward("showEdit");
        }
				
        roomFeatureEditForm.setDeptCode((String)request.getSession().getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		if (roomFeatureEditForm.getId()!=null)
			request.setAttribute("hash", "A"+roomFeatureEditForm.getId());
		return mapping.findForward("showRoomFeatureList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param roomFeatureEditForm
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	private void update(
			ActionMapping mapping, 
			RoomFeatureEditForm roomFeatureEditForm, 
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
		Long id = new Long(roomFeatureEditForm.getId());
		
		org.hibernate.Session hibSession = (new RoomFeatureDAO()).getSession();
		Transaction tx = null;
		
		try {
			tx = hibSession.beginTransaction();
			
			RoomFeature roomFeature = (new RoomFeatureDAO()).get(id, hibSession);
		
			roomFeature.setLabel(roomFeatureEditForm.getName());
			
			String[] selectedAssigned = roomFeatureEditForm.getAssignedSelected();
			String[] selectedNotAssigned = roomFeatureEditForm.getNotAssignedSelected();
			Collection assignedRooms = getAssignedRooms(user, roomFeature, roomFeatureEditForm);
			Collection notAssignedRooms = getAvailableRooms(user, roomFeature, roomFeatureEditForm);
		
			
			String s1 = null;
			String s2 = null;
			if (selectedAssigned.length != 0)
				s1 = Constants.arrayToStr(selectedAssigned,"",",");
			else
				s1 = new String();
			if (selectedNotAssigned.length != 0)
				s2 = Constants.arrayToStr(selectedNotAssigned,"",",");
			else 
				s2 = new String();
			
			Collection rooms = roomFeature.getRooms();
			
			//move room from assignedRooms to notAssignedRooms
			if (selectedAssigned.length != assignedRooms.size()) {
				
				Collection m = new HashSet();
				
				//remove room from feature
				for (Iterator iter = rooms.iterator();iter.hasNext();) {
					Location r = (Location) iter.next();
					if (r.getSession().getUniqueId().equals(sessionId)) {
						if (s1.indexOf(r.getUniqueId().toString()) == -1) {
							iter.remove();
							m.add(r);
						}
					}
				}
					
				//remove feature from room
				for (Iterator iter = m.iterator(); iter.hasNext();) {
					Location r = (Location) iter.next();
					Collection features = r.getFeatures();
					for (Iterator innerIter = features.iterator(); innerIter.hasNext();) {
						RoomFeature innerRf = (RoomFeature) innerIter.next();
						if (roomFeature.equals(innerRf)) {
							innerIter.remove();
						}
					}
					hibSession.saveOrUpdate(r);
				}
			}
			
			//move room from notAssignedRooms to assignedRooms
			if (selectedNotAssigned.length != 0) {
				Collection m = new HashSet();
				
				//add room to feature
				for (Iterator iter = notAssignedRooms.iterator(); iter.hasNext();) {
					Location r = (Location) iter.next();
					if (s2.indexOf(r.getUniqueId().toString()) != -1) {
						rooms.add(r);
						m.add(r);
					}
				}
				
				//add feature to room
				for (Iterator iter = m.iterator(); iter.hasNext();) {
					Location r = (Location) iter.next();
					Collection features = r.getFeatures();
					features.add(roomFeature);
					hibSession.saveOrUpdate(r);
				}
			}
			
			hibSession.saveOrUpdate(roomFeature);

            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    (RoomFeature)roomFeature, 
                    ChangeLog.Source.ROOM_FEATURE_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    (roomFeature instanceof DepartmentRoomFeature?((DepartmentRoomFeature)roomFeature).getDepartment():null));

            tx.commit();
		} catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        } 
	}

	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward cancelRoomFeature(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		RoomFeatureEditForm roomFeatureEditForm = (RoomFeatureEditForm) form;
		roomFeatureEditForm.setDeptCode((String)request.getSession().getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString());
		if (roomFeatureEditForm.getId()!=null)
			request.setAttribute("hash", "A"+roomFeatureEditForm.getId());
		return mapping.findForward("showRoomFeatureList");
	}
	
	/**
	 * 
	 * @param user
	 * @param roomFeatureEditForm 
	 * @param d
	 * @return
	 * @throws Exception 
	 */
	private Collection getAvailableRooms(User user, RoomFeature rf, RoomFeatureEditForm roomFeatureEditForm) throws Exception {		
		//get depts owned by user
		String depts[] = null;
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		if (rf instanceof DepartmentRoomFeature) {
			Department dept = ((DepartmentRoomFeature)rf).getDepartment();
			depts = new String[] { dept.getDeptCode() };
		}

		//get rooms owned by user
		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(depts);	
		Collection available = new HashSet();
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();)  {
			Location r = (Location) iter.next();
			if (!r.hasFeature(rf))  available.add(r);
		}
		return available;
	}

	/**
	 * 
	 * @param user 
	 * @param roomFeatureEditForm 
	 * @param rooms
	 * @param d 
	 * @return
	 * @throws Exception 
	 */
	private Collection getAssignedRooms(User user, RoomFeature rf, RoomFeatureEditForm roomFeatureEditForm) throws Exception {
		//get depts owned by user
		String depts[] = null;
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		if (rf instanceof DepartmentRoomFeature) {
			Department dept = ((DepartmentRoomFeature)rf).getDepartment();
			depts = new String[] { dept.getDeptCode() };
		}

		//get rooms owned by user
		Collection rooms = Session.getCurrentAcadSession(user).getRoomsFast(depts);	
		Collection assigned = new HashSet();
		
		for (Iterator iter = rooms.iterator(); iter.hasNext();)  {
			Location r = (Location) iter.next();
			if (r.hasFeature(rf))  assigned.add(r);
		}
		return assigned;
	}

}