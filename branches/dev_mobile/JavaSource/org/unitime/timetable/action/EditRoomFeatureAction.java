/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.form.EditRoomFeatureForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.dao.DepartmentRoomFeatureDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoomFeature" name="editRoomFeatureForm" input="/admin/editRoomFeature.jsp" scope="request"
 * @struts.action-forward name="showRoomDetail" path="/roomDetail.do"
 *
 * @author Tomas Muller
 */
@Service("/editRoomFeature")
public class EditRoomFeatureAction extends Action {
	
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
		EditRoomFeatureForm editRoomFeatureForm = (EditRoomFeatureForm) form;
		
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
			errors.add("editRoomGroup", new ActionMessage("errors.lookup.notFound", "Room Group") );
			saveErrors(request, errors);
		}
		
		sessionContext.checkPermission(location, Right.RoomEditFeatures);
		
		boolean editGlobalFeatures = sessionContext.hasPermission(location, Right.RoomEditGlobalFeatures);
		for (GlobalRoomFeature grf: RoomFeature.getAllGlobalRoomFeatures(location.getSession())) {
			if (!editRoomFeatureForm.getGlobalRoomFeatureIds().contains(grf.getUniqueId().toString())) {
				editRoomFeatureForm.addToGlobalRoomFeatures(grf, editGlobalFeatures, location.hasFeature(grf));
			}
		}
		
		Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
		for (Department department: departments) {
			for (DepartmentRoomFeature drf: RoomFeature.getAllDepartmentRoomFeatures(department)) {
				editRoomFeatureForm.addToDepartmentRoomFeatures(drf, true, location.hasFeature(drf));
			}
		}
		
		for (Department department: Department.findAllExternal(location.getSession().getUniqueId())) {
			if (departments.contains(department)) continue;
			for (DepartmentRoomFeature drf: RoomFeature.getAllDepartmentRoomFeatures(department)) {
				editRoomFeatureForm.addToDepartmentRoomFeatures(drf, false, location.hasFeature(drf));
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

		org.hibernate.Session hibSession = LocationDAO.getInstance().getSession();
		
		Transaction tx = null;
		try{
			tx = hibSession.beginTransaction();
			
			Location location = LocationDAO.getInstance().get(Long.valueOf(request.getParameter("id")), hibSession);
			
			sessionContext.checkPermission(location, Right.RoomEditFeatures);

			boolean editGlobalFeatures = sessionContext.hasPermission(location, Right.RoomEditGlobalFeatures);
			
			if (editGlobalFeatures && editRoomFeatureForm.getGlobalRoomFeaturesAssigned() != null) {
				List globalSelected = editRoomFeatureForm.getGlobalRoomFeaturesAssigned();
				List globalRf = editRoomFeatureForm.getGlobalRoomFeatureIds();
				if (globalSelected.size() == 0) {
					for (Iterator iter = globalRf.iterator(); iter.hasNext();) {
						String rfId = (String)iter.next();
						RoomFeature rf = RoomFeatureDAO.getInstance().get(Long.valueOf(rfId), hibSession);
						location.getFeatures().remove(rf);
						rf.getRooms().remove(location);
						hibSession.saveOrUpdate(rf);
					}
				} else {	
					int i = 0;
					for (Iterator iter = globalRf.iterator(); iter.hasNext();){
						String rfId = (String) iter.next();	
						String selected = (String)globalSelected.get(i);
						RoomFeature rf = RoomFeatureDAO.getInstance().get(Long.valueOf(rfId), hibSession);
						if (selected==null) continue;
						
						if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
							if (!rf.hasLocation(location)) {
								location.getFeatures().add(rf);
								rf.getRooms().add(location);
							}
						} else {
							if (rf.hasLocation(location)) {
								location.getFeatures().remove(rf);
								rf.getRooms().remove(location);
							}
						}
						hibSession.saveOrUpdate(rf);
						i++;
					}
				}
			}

			Set<Department> departments = Department.getUserDepartments(sessionContext.getUser());
			if (!departments.isEmpty() && editRoomFeatureForm.getDepartmentRoomFeaturesAssigned() != null) {
				
				List managerSelected = editRoomFeatureForm.getDepartmentRoomFeaturesAssigned();
				List managerRf = editRoomFeatureForm.getDepartmentRoomFeatureIds();
				
				if (managerSelected.size() == 0) {
					for (Iterator iter = managerRf.iterator(); iter.hasNext();) {
						String rfId = (String)iter.next();
						DepartmentRoomFeature rf = DepartmentRoomFeatureDAO.getInstance().get(Long.valueOf(rfId), hibSession);
						if (!departments.contains(rf.getDepartment())) continue;
						rf.getRooms().remove(location);
						hibSession.saveOrUpdate(rf);
					}
				} else {	
					int i = 0;
					for (Iterator iter = managerRf.iterator(); iter.hasNext();){
						String rfId = (String) iter.next();	
						String selected = (String)managerSelected.get(i);
						if (selected==null) continue;

						DepartmentRoomFeature rf = DepartmentRoomFeatureDAO.getInstance().get(Long.valueOf(rfId), hibSession);
						if (!departments.contains(rf.getDepartment())) continue;
						
						if (selected.equalsIgnoreCase("on") || selected.equalsIgnoreCase("true")) {
							if (!rf.hasLocation(location)) {
								rf.getRooms().add(location);
								location.getFeatures().add(rf);
							}
						} else {
							if (rf.hasLocation(location)) {
								rf.getRooms().remove(location);
								location.getFeatures().remove(rf);
							}
						}
						hibSession.saveOrUpdate(rf);
						i++;
					}
				}
			}
			
			hibSession.saveOrUpdate(location);
	        
	        ChangeLog.addChange(
	                hibSession, 
	                sessionContext, 
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

