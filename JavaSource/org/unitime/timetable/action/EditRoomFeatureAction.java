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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomFeatureForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomFeature" name="editRoomFeatureForm" input="/admin/editRoomFeature.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 */
public class EditRoomFeatureAction extends Action {

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
		EditRoomFeatureForm editRoomFeatureForm = (EditRoomFeatureForm) form;
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}

		MessageResources rsc = getResources(request);
		String doit = editRoomFeatureForm.getDoit();
		
		//return to room list
		if(doit!= null && doit.equals(rsc.getMessage("button.returnToRoomDetail"))) {
			response.sendRedirect("roomDetail.do?id="+editRoomFeatureForm.getId());
			return null;
			//return mapping.findForward("showRoomDetail");
		}
		
		//update location
		if(doit != null && doit.equals(rsc.getMessage("button.update"))) {
			doUpdate(editRoomFeatureForm,request);				
			return mapping.findForward("showRoomDetail");
		}
		
		User user = Web.getUser(webSession);
		Long sessionId = org.unitime.timetable.model.Session.getCurrentAcadSession(user).getSessionId();
		
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));

        //get location information
        Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		if (location instanceof Room) {
			Room r = (Room) location;
			editRoomFeatureForm.setRoomLabel(r.getLabel());
		} else if (location instanceof NonUniversityLocation) {
				NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
				editRoomFeatureForm.setRoomLabel(nonUnivLocation.getName());
		} else {
			ActionMessages errors = new ActionMessages();
			errors.add("editRoomGroup", 
	                   new ActionMessage("errors.lookup.notFound", "Room Group") );
			saveErrors(request, errors);
		}
		
		//get features
		ArrayList globalRoomFeatures = new ArrayList();
		ArrayList deptRoomFeatures = new ArrayList();
		
		org.hibernate.Session hibSession = null;
		try {
			RoomFeatureDAO d = new RoomFeatureDAO();
			hibSession = d.getSession();
			
			List list = hibSession
						.createCriteria(GlobalRoomFeature.class)
						.addOrder(Order.asc("label"))
						.list();
			
			for (Iterator iter = list.iterator();iter.hasNext();) {
				GlobalRoomFeature rf = (GlobalRoomFeature) iter.next();
				globalRoomFeatures.add(rf);
			}
			
			list = hibSession
			.createQuery("select distinct f from DepartmentRoomFeature f where f.department.session=:sessionId order by label")
			.setLong("sessionId", sessionId.longValue())
			.list();
				
			for (Iterator i1 = list.iterator();i1.hasNext();) {
				DepartmentRoomFeature rf = (DepartmentRoomFeature) i1.next();
				if (rf.getDeptCode()==null) continue;
				Department dept = Department.findByDeptCode(rf.getDeptCode(), sessionId);				
				for (Iterator i2=location.getRoomDepts().iterator();i2.hasNext();) {
					RoomDept rd = (RoomDept)i2.next();
					if (dept.getUniqueId().equals(rd.getDepartment().getUniqueId())) {
						deptRoomFeatures.add(rf); break;
					}
				}
			}

			
		} catch (Exception e) {
			Debug.error(e);
		}
		
		for (Iterator iter = globalRoomFeatures.iterator(); iter.hasNext();) {
			GlobalRoomFeature grf = (GlobalRoomFeature) iter.next();
			if (!editRoomFeatureForm.getGlobalRoomFeatureIds().contains(grf.getUniqueId().toString())) {
				if (user.getRole().equals(Roles.ADMIN_ROLE) || user.getRole().equals(Roles.EXAM_MGR_ROLE)) {
					editRoomFeatureForm.addToGlobalRoomFeatures(grf,Boolean.TRUE, Boolean.valueOf(location.hasFeature(grf)));
				} else {
					editRoomFeatureForm.addToGlobalRoomFeatures(grf,Boolean.FALSE,Boolean.valueOf(location.hasFeature(grf)));
				}
			}
		}
		
		for (Iterator iter = deptRoomFeatures.iterator();iter.hasNext();) {
			DepartmentRoomFeature drf = (DepartmentRoomFeature) iter.next();
			if (!editRoomFeatureForm.getDepartmentRoomFeatureIds().contains(drf.getUniqueId().toString())){
				if (user.getRole().equals(Roles.ADMIN_ROLE) || (drf.getDeptCode() != null && owner.getDepartments().contains(Department.findByDeptCode(drf.getDeptCode(), sessionId)))) {
					editRoomFeatureForm.addToDepartmentRoomFeatures(drf, Boolean.TRUE, Boolean.valueOf(location.hasFeature(drf)));
				} else {
					editRoomFeatureForm.addToDepartmentRoomFeatures(drf, Boolean.FALSE, Boolean.valueOf(location.hasFeature(drf)));
				}
			}
		}

		return mapping.findForward("showEditRoomFeature");
	}

	/**
	 * 
	 * @param editRoomFeatureForm
	 * @param request
	 * @throws Exception
	 */
	private void doUpdate(
			EditRoomFeatureForm editRoomFeatureForm, 
			HttpServletRequest request) throws Exception {

		//get location information
		Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		Collection features = location.getFeatures();
		RoomFeatureDAO rfdao = new RoomFeatureDAO();
		Set rfs = new HashSet();
		
		//update room features
		Session hibSession = ldao.getSession();
		Transaction tx = null;
		try{
			tx = hibSession.beginTransaction();
			
		if (editRoomFeatureForm.getGlobalRoomFeaturesAssigned() != null) {
			List globalSelected = editRoomFeatureForm.getGlobalRoomFeaturesAssigned();
			List globalRf = editRoomFeatureForm.getGlobalRoomFeatureIds();
	
			if (globalSelected.size() == 0) {
				for (Iterator iter = globalRf.iterator(); iter.hasNext();) {
					String rfId = (String)iter.next();
					RoomFeature rf = rfdao.get(Long.valueOf(rfId));
					rf.getRooms().remove(location);
					hibSession.saveOrUpdate(rf);
				}
			} else {	
				int i = 0;
				for (Iterator iter = globalRf.iterator(); iter.hasNext();){
					String rfId = (String) iter.next();	
					String selected = (String)globalSelected.get(i);
					RoomFeature rf = rfdao.get(Long.valueOf(rfId));
					if (selected==null) continue;
					
					if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
						rfs.add(rf);
						if (!rf.hasLocation(location)) {
							rf.getRooms().add(location);
						}
					} else {
						if (rf.hasLocation(location)) {
							rf.getRooms().remove(location);
						}
					}
					hibSession.saveOrUpdate(rf);
					i++;
				}
			}
		}
		
		if (editRoomFeatureForm.getDepartmentRoomFeaturesAssigned() != null){
			List managerSelected = editRoomFeatureForm.getDepartmentRoomFeaturesAssigned();
			List managerRf = editRoomFeatureForm.getDepartmentRoomFeatureIds();
			
			if (managerSelected.size() == 0) {
				for (Iterator iter = managerRf.iterator(); iter.hasNext();) {
					String rfId = (String)iter.next();
					RoomFeature rf = rfdao.get(Long.valueOf(rfId));
					rf.getRooms().remove(location);
					hibSession.saveOrUpdate(rf);
				}
			} else {	
				int i = 0;
				for (Iterator iter = managerRf.iterator(); iter.hasNext();){
					String rfId = (String) iter.next();	
					String selected = (String)managerSelected.get(i);
					RoomFeature rf = rfdao.get(Long.valueOf(rfId));
					if (selected==null) continue;
					
					if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
						rfs.add(rf);
						if (!rf.hasLocation(location)) {
							rf.getRooms().add(location);
						}
					} else {
						if (rf.hasLocation(location)) {
							rf.getRooms().remove(location);
						}
					}
					hibSession.saveOrUpdate(rf);
					i++;
				}
			}
		}
		
		location.setFeatures(rfs);
		hibSession.saveOrUpdate(location);
        
        ChangeLog.addChange(
                hibSession, 
                request, 
                location, 
                ChangeLog.Source.ROOM_FEATURE_EDIT, 
                ChangeLog.Operation.UPDATE, 
                null, 
                location.getControllingDepartment());
        
		tx.commit();
		hibSession.refresh(location);
			
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

}

