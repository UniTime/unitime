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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomFeatureEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomFeature;
import org.unitime.timetable.model.RoomFeaturePref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.RoomFeatureDAO;
import org.unitime.timetable.model.dao.RoomFeatureTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;
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
 *
 * @author Tomas Muller
 */
@Service("/roomFeatureEdit")
public class RoomFeatureEditAction extends SpringAwareLookupDispatchAction {
	
	@Autowired SessionContext sessionContext;

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
		
		//get roomFeature from request
		Long id =  new Long(Long.parseLong(request.getParameter("id")));
		roomFeatureEditForm.setId(id.toString());
		RoomFeatureDAO rdao = new RoomFeatureDAO();
		RoomFeature rf = rdao.get(id);
		
		sessionContext.checkPermission(rf, rf instanceof GlobalRoomFeature ? Right.GlobalRoomFeatureEdit : Right.DepartmenalRoomFeatureEdit);
		
		roomFeatureEditForm.setSessionId(sessionContext.getUser().getCurrentAcademicSessionId());
		
		roomFeatureEditForm.setFeatureTypeId(rf.getFeatureType() == null ? -1 : rf.getFeatureType().getUniqueId());
		
		//set global
		if (rf instanceof GlobalRoomFeature) {
			roomFeatureEditForm.setGlobal(true);
			roomFeatureEditForm.setDeptCode(null);
			roomFeatureEditForm.setDeptName(null);
			String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (dept != null && dept.matches("Exam[0-9]*")) {
				roomFeatureEditForm.setDeptName(ExamTypeDAO.getInstance().get(Long.valueOf(dept.substring(4))).getLabel() + " Examination Rooms");
			} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
				Department department = Department.findByDeptCode(dept, sessionContext.getUser().getCurrentAcademicSessionId());
				if (department != null)
					roomFeatureEditForm.setDeptName(department.getDeptCode() + " - " + department.getName());
			}
		}
		
		if (rf instanceof DepartmentRoomFeature){
			roomFeatureEditForm.setGlobal(false);
			Department dept = ((DepartmentRoomFeature)rf).getDepartment();
			roomFeatureEditForm.setDeptCode(dept.getDeptCode());
			roomFeatureEditForm.setDeptName(dept.getDeptCode() + " - " + dept.getName());
		}
		
		if (roomFeatureEditForm.getName()==null || roomFeatureEditForm.getName().isEmpty())
			roomFeatureEditForm.setName(rf.getLabel());
        
        if (roomFeatureEditForm.getAbbv()==null || roomFeatureEditForm.getAbbv().isEmpty())
            roomFeatureEditForm.setAbbv(rf.getAbbv());
		
        //get rooms		
		Collection assigned = getAssignedRooms(rf);
		Collection available = getAvailableRooms(rf);

		TreeSet sortedAssignedRooms = new TreeSet(assigned);
		roomFeatureEditForm.setAssignedRooms(sortedAssignedRooms);
		
		TreeSet sortedAvailableRooms = new TreeSet(available);
		roomFeatureEditForm.setNotAssignedRooms(sortedAvailableRooms);
		
		roomFeatureEditForm.setRooms();
		
		request.setAttribute("featureTypes", RoomFeatureTypeDAO.getInstance().findAll(Order.asc("label")));
		
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
				
				sessionContext.checkPermission(rf, rf instanceof GlobalRoomFeature ? Right.GlobalRoomFeatureDelete : Right.DepartmenalRoomFeatureDelete);
                
                ChangeLog.addChange(
                        hibSession, 
                        sessionContext, 
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
                
				for (RoomFeaturePref p: (List<RoomFeaturePref>)hibSession.createQuery("from RoomFeaturePref p where p.roomFeature.uniqueId = :id")
						.setLong("id", id).list()) {
					p.getOwner().getPreferences().remove(p);
					hibSession.delete(p);
					hibSession.saveOrUpdate(p.getOwner());
				}
                
				hibSession.delete(rf);
			}
            
			tx.commit();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
			throw e;
		}
			
		roomFeatureEditForm.setDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
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
				
        roomFeatureEditForm.setDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
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
		
		Long id = new Long(roomFeatureEditForm.getId());
		
		org.hibernate.Session hibSession = (new RoomFeatureDAO()).getSession();
		Transaction tx = null;
		
		try {
			tx = hibSession.beginTransaction();
			
			RoomFeature roomFeature = (new RoomFeatureDAO()).get(id, hibSession);
			
			sessionContext.checkPermission(roomFeature, roomFeature instanceof GlobalRoomFeature ? Right.GlobalRoomFeatureEdit : Right.DepartmenalRoomFeatureEdit);
		
			roomFeature.setLabel(roomFeatureEditForm.getName());
            roomFeature.setAbbv(roomFeatureEditForm.getAbbv());
			
			String[] selectedAssigned = roomFeatureEditForm.getAssignedSelected();
			String[] selectedNotAssigned = roomFeatureEditForm.getNotAssignedSelected();
			Collection assignedRooms = getAssignedRooms(roomFeature);
			Collection notAssignedRooms = getAvailableRooms(roomFeature);
		
            if (roomFeatureEditForm.getFeatureTypeId() != null && roomFeatureEditForm.getFeatureTypeId() >= 0)
            	roomFeature.setFeatureType(RoomFeatureTypeDAO.getInstance().get(roomFeatureEditForm.getFeatureTypeId()));
            else
            	roomFeature.setFeatureType(null);
			
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
					if (assignedRooms.contains(r) && s1.indexOf(r.getUniqueId().toString()) == -1) {
						iter.remove();
						m.add(r);
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
                    sessionContext, 
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
		roomFeatureEditForm.setDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom));
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
	private Collection getAvailableRooms(RoomFeature rf) throws Exception {		
		List<Location> rooms = null;
		
		if (rf instanceof DepartmentRoomFeature) {
			Department dept = ((DepartmentRoomFeature)rf).getDepartment();
			rooms = new ArrayList<Location>();
			for (RoomDept rd: dept.getRoomDepts())
				rooms.add(rd.getRoom());
		} else {
			Session session = ((GlobalRoomFeature)rf).getSession();
			String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (dept != null && dept.matches("Exam[0-9]*")) {
				rooms = new ArrayList<Location>(Location.findAllExamLocations(session.getUniqueId(), Long.valueOf(dept.substring(4))));
			} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
				Department department = Department.findByDeptCode(dept, session.getUniqueId());
				if (department != null) {
					rooms = new ArrayList<Location>();
					for (RoomDept rd: department.getRoomDepts())
						rooms.add(rd.getRoom());
				} else {
					rooms = new ArrayList<Location>(Location.findAll(session.getUniqueId()));	
				}
			} else {
				rooms = new ArrayList<Location>(Location.findAll(session.getUniqueId()));
			}
		}
		
		Collections.sort(rooms);
		
		rooms.removeAll(rf.getRooms());
		
		return rooms;
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
	private Collection getAssignedRooms(RoomFeature rf) throws Exception {
		List<Location> rooms = new ArrayList<Location>(rf.getRooms());
		
		String dept = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
		if (dept != null && dept.matches("Exam[0-9]*")) {
			Long examType = Long.valueOf(dept.substring(4));
			for (Iterator<Location> i = rooms.iterator(); i.hasNext(); ) {
				if (!i.next().isExamEnabled(examType)) i.remove();
			}
		} else if (dept != null && !dept.isEmpty() && !"All".equals(dept)) {
			Department department = Department.findByDeptCode(dept, sessionContext.getUser().getCurrentAcademicSessionId());
			if (department != null) {
				rooms: for (Iterator<Location> i = rooms.iterator(); i.hasNext(); ) {
					Location location = i.next();
					for (RoomDept rd: location.getRoomDepts())
						if (rd.getDepartment().equals(department)) continue rooms;
					i.remove();
				}
			}
		}

		return rooms;
	}

}
