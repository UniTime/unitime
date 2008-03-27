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
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.EditRoomForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.EveningPeriodPreferenceModel;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LocationPermIdGenerator;
import org.unitime.timetable.webutil.RequiredTimeTable;


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
            if (editRoomForm.getId()==null || editRoomForm.getId().length()==0)
                response.sendRedirect("roomList.do");
            else
                response.sendRedirect("roomDetail.do?id="+editRoomForm.getId());
			return null;
			//the following call cannot be used since doit is has the same value as for return to room list (Back)
			//return mapping.findForward("showRoomDetail");
		}
        
        User user = Web.getUser(webSession);
        Session s = Session.getCurrentAcadSession(user);
        String mgrId = (String)user.getAttribute(Constants.TMTBL_MGR_ID_ATTR_NAME);
        TimetableManagerDAO tdao = new TimetableManagerDAO();
        TimetableManager owner = tdao.get(new Long(mgrId));
        boolean admin = Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE});
        
        //update location
		if(doit != null && (doit.equals(rsc.getMessage("button.update")) || doit.equals(rsc.getMessage("button.save")))) {
			ActionMessages errors = new ActionMessages();
			errors = editRoomForm.validate(mapping, request);
			if (errors.size() == 0) {
                if (editRoomForm.getId()==null || editRoomForm.getId().length()==0) {
                    doSave(editRoomForm, request);
                } else {
                    doUpdate(editRoomForm,request);
                }
				response.sendRedirect("roomDetail.do?id="+editRoomForm.getId());
				return null;
			} else {
				saveErrors(request, errors);
                setupDepartments(user, owner, s.getUniqueId(), request);
                setBldgs(s, request);
                return mapping.findForward(editRoomForm.getId()==null || editRoomForm.getId().length()==0?"showAddRoom":"showEditRoom");
			}
		}	
        
        if (request.getParameter("id")!=null && request.getParameter("id").length()>0) {
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
            if (location.getExamCapacity() != null){
            	editRoomForm.setExamCapacity(location.getExamCapacity().toString());           
            }
            editRoomForm.setExamEnabled(location.isExamEnabled(Exam.sExamTypeFinal));
            editRoomForm.setExamEEnabled(location.isExamEnabled(Exam.sExamTypeEvening));
            editRoomForm.setIgnoreTooFar(location.isIgnoreTooFar());
            editRoomForm.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
            editRoomForm.setCoordX(location.getCoordinateX()==null || location.getCoordinateX().intValue()<0?null:location.getCoordinateX().toString());
            editRoomForm.setCoordY(location.getCoordinateY()==null || location.getCoordinateY().intValue()<0?null:location.getCoordinateY().toString());
            editRoomForm.setControlDept(null);
            
            PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), Exam.sExamTypeFinal);
            px.load(location);
            px.setAllowRequired(false);
            RequiredTimeTable rttPx = new RequiredTimeTable(px);
            rttPx.setName("PeriodPrefs");
            request.setAttribute("PeriodPrefs", rttPx.print(true, RequiredTimeTable.getTimeGridVertical(user))); 

            if (Exam.hasEveningExams(location.getSession().getUniqueId())) {
                EveningPeriodPreferenceModel epx = new EveningPeriodPreferenceModel(location.getSession());
                if (epx.canDo()) {
                    epx.load(location);
                    request.setAttribute("PeriodEPrefs", epx.print(true));
                } else {
                    px = new PeriodPreferenceModel(location.getSession(), Exam.sExamTypeEvening);
                    px.load(location);
                    px.setAllowRequired(false);
                    rttPx = new RequiredTimeTable(px);
                    rttPx.setName("PeriodEPrefs");
                    request.setAttribute("PeriodEPrefs", rttPx.print(true, RequiredTimeTable.getTimeGridVertical(user)));
                }
            }

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
        } else {
            editRoomForm.reset(mapping, request);
            
            Set departments = owner.departmentsForSession(s.getUniqueId());
            if (!admin && (departments.size() == 1)) {
                Department d = (Department) departments.iterator().next();
                editRoomForm.setControlDept(d.getUniqueId().toString());
            } else if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
                Department d = Department.findByDeptCode(webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString(), s.getUniqueId());
                if (d!=null)
                    editRoomForm.setControlDept(d.getUniqueId().toString());
            }

            setupDepartments(user, owner, s.getUniqueId(), request);
            setBldgs(s, request);
        }
		
		return mapping.findForward(editRoomForm.getId()==null || editRoomForm.getId().length()==0?"showAddRoom":"showEditRoom");
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
    
    public static void setupDepartments(User user, TimetableManager manager, Long sessionId, HttpServletRequest request) throws Exception {
        Set departments = new TreeSet();
        if (user.getRole().equals(Roles.ADMIN_ROLE)) {
            departments = Department.findAllBeingUsed(sessionId);
        } else {
            departments = manager.departmentsForSession(sessionId);
        }
        
        Collection availableDepts = new Vector();
        
        for (Iterator i=departments.iterator();i.hasNext();) {
            Department d = (Department)i.next();
            availableDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
        }
        
        request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
    }
    
    private void setBldgs(Session session, HttpServletRequest request) throws Exception {
        Collection bldgs = session.getBldgsFast(null);
        
        ArrayList list = new ArrayList();
        for (Iterator iter = bldgs.iterator(); iter.hasNext();) {
            Building b = (Building) iter.next();
            list.add(new LabelValueBean(
                    b.getAbbreviation() + "-" + b.getName(), 
                    b.getUniqueId().toString()));
        }
            
        request.setAttribute(Building.BLDG_LIST_ATTR_NAME, list);
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
		Session session = Session.getCurrentAcadSession(user);
		Long sessionId = session.getSessionId();
        
        Long id = Long.valueOf(editRoomForm.getId());
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
			
            if (editRoomForm.getExamCapacity() != null && !editRoomForm.getExamCapacity().trim().equalsIgnoreCase("")) {
                location.setExamCapacity(Integer.valueOf(editRoomForm.getExamCapacity().trim()));
            }

            location.setExamEnabled(Exam.sExamTypeFinal,editRoomForm.getExamEnabled());
            location.setExamEnabled(Exam.sExamTypeEvening,editRoomForm.getExamEEnabled());
				
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
			
			if (location.isExamEnabled(Exam.sExamTypeFinal)) {
			    PeriodPreferenceModel px = new PeriodPreferenceModel(session, Exam.sExamTypeFinal);
			    RequiredTimeTable rttPx = new RequiredTimeTable(px);
			    rttPx.setName("PeriodPrefs");
			    rttPx.update(request);
			    px.save(location);
			} else {
			    location.clearExamPreferences(Exam.sExamTypeFinal);
			}
            
            if (Exam.hasEveningExams(location.getSession().getUniqueId()) && location.isExamEnabled(Exam.sExamTypeEvening)) {
                EveningPeriodPreferenceModel epx = new EveningPeriodPreferenceModel(location.getSession());
                if (epx.canDo()) {
                    epx.load(request);
                    request.setAttribute("PeriodEPrefs", epx.print(true));
                    epx.save(location);
                } else {
                    PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), Exam.sExamTypeEvening);
                    RequiredTimeTable rttPx = new RequiredTimeTable(px);
                    rttPx.setName("PeriodEPrefs");
                    rttPx.update(request);
                    px.save(location);
                }
            } else {
                location.clearExamPreferences(Exam.sExamTypeEvening);
            }
			
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
    
    private void doSave(EditRoomForm editRoomForm, HttpServletRequest request) throws Exception {
        HttpSession webSession = request.getSession();
        User user = Web.getUser(webSession);
        Session session = Session.getCurrentAcadSession(user);

        LocationDAO ldao = new LocationDAO();
        org.hibernate.Session hibSession = ldao.getSession();
        Transaction tx = null;
        try {
            tx = hibSession.beginTransaction();

            Room room = new Room();
                        
            room.setRoomNumber(editRoomForm.getName());
            Building building = new BuildingDAO().get(Long.valueOf(editRoomForm.getBldgId()));
            room.setBuilding(building);
            if (building!=null) room.setBuildingAbbv(building.getAbbreviation());
            room.setRoomDepts(new HashSet());
            RoomDept rd = new RoomDept();
            rd.setRoom(room); rd.setDepartment(new DepartmentDAO().get(Long.valueOf(editRoomForm.getControlDept()))); rd.setControl(Boolean.TRUE);
            room.getRoomDepts().add(rd);
            room.setCapacity(Integer.valueOf(editRoomForm.getCapacity().trim()));
            room.setExamCapacity(Integer.valueOf(editRoomForm.getExamCapacity().trim()));
            room.setExamEnabled(Exam.sExamTypeFinal,editRoomForm.getExamEnabled());
            room.setExamEnabled(Exam.sExamTypeEvening,editRoomForm.getExamEEnabled());
            room.setIgnoreTooFar(Boolean.FALSE);
            room.setIgnoreRoomCheck(editRoomForm.isIgnoreRoomCheck()!=null && editRoomForm.isIgnoreRoomCheck().booleanValue());
            room.setExternalUniqueId(editRoomForm.getExternalId());
            room.setScheduledRoomType(editRoomForm.getType());
            room.setCoordinateX(editRoomForm.getCoordX()==null || editRoomForm.getCoordX().length()==0 ? new Integer(-1) : Integer.valueOf(editRoomForm.getCoordX()));
            room.setCoordinateY(editRoomForm.getCoordY()==null || editRoomForm.getCoordY().length()==0 ? new Integer(-1) : Integer.valueOf(editRoomForm.getCoordY()));
            room.setSession(session);
            
            LocationPermIdGenerator.setPermanentId(room);

            PeriodPreferenceModel px = new PeriodPreferenceModel(session, Exam.sExamTypeFinal);
            RequiredTimeTable rttPx = new RequiredTimeTable(px);
            rttPx.setName("PeriodPrefs");
            rttPx.update(request);
            px.save(room);

            hibSession.saveOrUpdate(room);
            
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    (Location)room, 
                    ChangeLog.Source.ROOM_EDIT, 
                    ChangeLog.Operation.CREATE, 
                    null, 
                    room.getControllingDepartment());

            hibSession.flush();
            tx.commit();
            
            editRoomForm.setId(room.getUniqueId().toString());
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
    }    

}

