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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.RoomDetailForm;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 05-12-2006
 * 
 * XDoclet definition:
 * @struts.action path="/roomDetail" name="roomDetailForm" input="/admin/roomDetail.jsp" scope="request"
 * @struts.action-forward name="showEditRoomFeaure" path="/editRoomFeature.do"
 * @struts.action-forward name="showRoomList" path="/roomList.do"
 * @struts.action-forward name="showEditRoomGroup" path="/editRoomGroup.do"
 * @struts.action-forward name="showEditRoomPref" path="/editRoomPref.do"
 * @struts.action-forward name="showEditRoomDept" path="/editRoomDept.do"
 */
public class RoomDetailAction extends Action {

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
		RoomDetailForm roomDetailForm = (RoomDetailForm) form;
		
		HttpSession webSession = request.getSession();
		if (!Web.isLoggedIn(webSession)) {
			throw new Exception("Access Denied.");
		}
			
		MessageResources rsc = getResources(request);
		String doit = roomDetailForm.getDoit();
		
		if (doit != null) {
			//delete location
			if(doit.equals(rsc.getMessage("button.delete"))) {
				if ("y".equals(request.getParameter("confirm"))) {
					doDelete(roomDetailForm, request);
					return mapping.findForward("showRoomList");
				}
			}
			
			//return to room list
			if(doit.equals(rsc.getMessage("button.returnToRoomList"))) {
				if (roomDetailForm.getId()!=null)
					request.setAttribute("hash", "A"+roomDetailForm.getId());
				return mapping.findForward("showRoomList");
			}
			
			//modify room
			if(doit.equals(rsc.getMessage("button.modifyRoom"))) {
				return mapping.findForward("showEditRoom");
			}
			
			//modify room departments
			if(doit.equals(rsc.getMessage("button.modifyRoomDepts"))) {
				return mapping.findForward("showEditRoomDept");
			}
			
			//modify room groups
			if(doit.equals(rsc.getMessage("button.modifyRoomGroups"))) {
				return mapping.findForward("showEditRoomGroup");
			}
			
			//modify room features
			if(doit.equals(rsc.getMessage("button.modifyRoomFeatures"))) {
				return mapping.findForward("showEditRoomFeature");
			}
			
			//modify room preferences
			if(doit.equals(rsc.getMessage("button.modifyRoomPreference"))
					|| doit.equals(rsc.getMessage("button.addRoomPreference"))) {
				return mapping.findForward("showEditRoomPref");
			}
			
            //modify room departments
            if(doit.equals(rsc.getMessage("button.modifyRoomPeriodPreferences"))) {
                return mapping.findForward("showEditRoomPeriodPref");
            }
		}
		
		if (request.getParameter("id")==null && roomDetailForm.getId()==null)
		    return mapping.findForward("showRoomList");
				
		//get location
		Long id = Long.valueOf(request.getParameter("id")!=null?request.getParameter("id"):roomDetailForm.getId());
		LocationDAO ldao = new LocationDAO();
		Location location = ldao.get(id);
		if (location instanceof Room) {
			roomDetailForm.setNonUniv(false);
		} else {
			roomDetailForm.setNonUniv(true);
		}
		
		//set roomSharingTable and user preference on location in form
		User user = Web.getUser(webSession);
		Session s = Session.getCurrentAcadSession(user);
		String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
		TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
        
        boolean timeVertical = RequiredTimeTable.getTimeGridVertical(user);
        RequiredTimeTable rtt = location.getRoomSharingTable(s, user);
        rtt.getModel().setDefaultSelection(RequiredTimeTable.getTimeGridSize(user));
        roomDetailForm.setSharingTable(rtt.print(false, timeVertical));
			
        //get department
        Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		String deptCode = (String)webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME);
		
		//get room preferences
		Vector depts = new Vector();
		roomDetailForm.setEditable(user.isAdmin());
		for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			depts.add(rd.getDepartment());
			if (rd.getDepartment().isEditableBy(user)) roomDetailForm.setEditable(true);
		}
		Collections.sort(depts);
		Vector prefs = new Vector(depts.size());
		for (Iterator i=depts.iterator();i.hasNext();) {
			Department d = (Department)i.next();
			PreferenceLevel pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sNeutral);
	        Set roomPrefs = d.getEffectiveRoomPreferences();
			for (Iterator j=roomPrefs.iterator();j.hasNext();) {
				RoomPref rp = (RoomPref)j.next();
				if (rp.getRoom().equals(location)) {
					pref = rp.getPrefLevel(); break;
				}
			}
			if (pref.getPrefProlog().equals(PreferenceLevel.sNeutral))
				i.remove();
			else
				prefs.addElement(pref);
		}
		roomDetailForm.setDepts(depts);
		roomDetailForm.setRoomPrefs(prefs);

		LookupTables.setupPrefLevels(request);
		
		//set location information in form
		roomDetailForm.setExamEnabled(location.isExamEnabled());
		roomDetailForm.setExamCapacity(location.getExamCapacity());
		
        PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession());
        px.setAllowRequired(false);
        px.load(location);
        RequiredTimeTable rttPx = new RequiredTimeTable(px);
        rttPx.setName("PeriodPrefs");
        if (!location.getExamPreferences().isEmpty())
            roomDetailForm.setExamPref(rttPx.print(false, timeVertical, true, false));
		
		roomDetailForm.setCapacity(location.getCapacity());
		roomDetailForm.setCoordinateX(location.getCoordinateX());
		roomDetailForm.setCoordinateY(location.getCoordinateY());
		roomDetailForm.setIgnoreTooFar(location.isIgnoreTooFar()==null?false:location.isIgnoreTooFar().booleanValue());
		roomDetailForm.setIgnoreRoomCheck(location.isIgnoreRoomCheck().booleanValue());
		roomDetailForm.setPatterns(location.getPattern());
		roomDetailForm.setGlobalFeatures(new TreeSet(location.getGlobalRoomFeatures()));
		roomDetailForm.setDepartmentFeatures(new TreeSet(location.getDepartmentRoomFeatures()));
		for (Iterator i=roomDetailForm.getDepartmentFeatures().iterator();i.hasNext();) {
		    DepartmentRoomFeature drf = (DepartmentRoomFeature)i.next();
            boolean skip = true;
            for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                RoomDept rd = (RoomDept)j.next();
                if (drf.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
            }
            if (skip) i.remove();
		}
		roomDetailForm.setGroups(new TreeSet(location.getRoomGroups()));
        for (Iterator i=roomDetailForm.getGroups().iterator();i.hasNext();) {
            RoomGroup rg = (RoomGroup)i.next();
            if (rg.isGlobal()) continue;
            boolean skip = true;
            for (Iterator j=location.getRoomDepts().iterator();j.hasNext();) {
                RoomDept rd = (RoomDept)j.next();
                if (rg.getDepartment().equals(rd.getDepartment())) { skip=false; break; }
            }
            if (skip) i.remove();
        }
		if (location instanceof Room) {
			Room r = (Room) location;
			roomDetailForm.setName(r.getLabel());
			roomDetailForm.setType(r.getScheduledRoomType());
            if ("genClassroom".equals(r.getScheduledRoomType())) {
                roomDetailForm.setTypeName("Classroom");
            } else if ("computingLab".equals(r.getScheduledRoomType())) {
                roomDetailForm.setTypeName("Computing Laboratory");
            } else if ("departmental".equals(r.getScheduledRoomType())) {
                roomDetailForm.setTypeName("Additional Instructional Room");
            } else if ("specialUse".equals(r.getScheduledRoomType())) {
                roomDetailForm.setTypeName("Special Use Room");
            } else
                roomDetailForm.setTypeName(r.getScheduledRoomType()); 
			if (r.getScheduledRoomType().trim().equalsIgnoreCase("specialUse")) {
				roomDetailForm.setDeleteFlag(true);
			} else {
				roomDetailForm.setDeleteFlag(false);
			}
            roomDetailForm.setExternalId(user.isAdmin()?r.getExternalUniqueId():null);
		} else if (location instanceof NonUniversityLocation) {
			NonUniversityLocation nonUnivLocation = (NonUniversityLocation) location;
			roomDetailForm.setName(nonUnivLocation.getName());
			roomDetailForm.setType("Non Univeristy Location");		
			roomDetailForm.setDeleteFlag(true);
            roomDetailForm.setExternalId(null);
            roomDetailForm.setTypeName("Non-University Location");
		} else {
			ActionMessages errors = new ActionMessages();
			errors.add("roomDetail", 
                    new ActionMessage("errors.lookup.notFound", "Room") );
			saveErrors(request, errors);
		}
		
		roomDetailForm.setOwner(true);
		roomDetailForm.setControl(null);
		Set ownedDepts = owner.departmentsForSession(s.getUniqueId());
		boolean controls = false;
		boolean allDepts = true;
		for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
			RoomDept rd = (RoomDept)i.next();
			if (rd.isControl().booleanValue())
				roomDetailForm.setControl(rd.getDepartment().getUniqueId().toString());
			if (rd.isControl().booleanValue() && ownedDepts!=null && ownedDepts.contains(rd.getDepartment()))
				controls = true;
			if (ownedDepts==null || !ownedDepts.contains(rd.getDepartment())) {
				allDepts = false;
			}
		}
		roomDetailForm.setOwner(controls || allDepts);
		
		EditRoomAction.setupDepartments(request, location);

		return mapping.findForward("showRoomDetail");
	}
	
	/**
	 * 
	 * @param roomDetailForm
	 * @param request
	 */
    private void doDelete(RoomDetailForm roomDetailForm, HttpServletRequest request) throws Exception {
		//get location
    	Long id = Long.valueOf(request.getParameter("id"));
		LocationDAO ldao = new LocationDAO();
		org.hibernate.Session hibSession = ldao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			Location location = ldao.get(id, hibSession);
			if (location != null){
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        location, 
                        ChangeLog.Source.ROOM_EDIT, 
                        ChangeLog.Operation.DELETE, 
                        null, 
                        location.getControllingDepartment());
				List roomPrefs = hibSession.createCriteria(RoomPref.class).add(Restrictions.eq("room.uniqueId", id)).list();
				for (Iterator i=location.getRoomDepts().iterator();i.hasNext();) {
					RoomDept rd = (RoomDept)i.next();
					Department d = rd.getDepartment();
					d.getRoomDepts().remove(rd);
					hibSession.delete(rd);
					hibSession.saveOrUpdate(d);
				}
				for (Iterator i=roomPrefs.iterator();i.hasNext();) {
					RoomPref rp = (RoomPref)i.next();
					rp.getOwner().getPreferences().remove(rp);
					hibSession.delete(rp);
					hibSession.saveOrUpdate(rp.getOwner());
				}
                for (Iterator i=location.getAssignments().iterator();i.hasNext();) {
                    Assignment a = (Assignment)i.next();
                    a.getRooms().remove(location);
                    hibSession.saveOrUpdate(a);
                    i.remove();
                }
                hibSession.delete(location);
			}
			tx.commit();
		} catch (Exception e) {
			if (tx!=null && tx.isActive()) tx.rollback();
			throw e;
		}
		
	}

}

