/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.EditRoomForm;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.MidtermPeriodPreferenceModel;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PeriodPreferenceModel;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.dao.BuildingDAO;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.RoomTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.LocationPermIdGenerator;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.RequiredTimeTable;


/** 
 * MyEclipse Struts
 * Creation date: 07-05-2006
 * 
 * XDoclet definition:
 * @struts.action path="/editRoom" name="editRoomForm" input="/admin/editRoom.jsp" scope="request"
 * @struts.action-forward name="showEditRoom" path="editRoomTile"
 */
@Service("/editRoom")
public class EditRoomAction extends Action {

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
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception{
		EditRoomForm editRoomForm = (EditRoomForm) form;
		
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
		
        //update location
		if(doit != null && (doit.equals(rsc.getMessage("button.update")) || doit.equals(rsc.getMessage("button.save")))) {
			ActionMessages errors = new ActionMessages();
			errors = editRoomForm.validate(mapping, request);
			if (errors.size() == 0) {
                if (editRoomForm.getId()==null || editRoomForm.getId().isEmpty()) {
                    doSave(editRoomForm, request);
                } else {
                    doUpdate(editRoomForm,request);
                }
				response.sendRedirect("roomDetail.do?id="+editRoomForm.getId());
				return null;
			} else {
				saveErrors(request, errors);
				if (editRoomForm.getId()==null || editRoomForm.getId().isEmpty())
					setupDepartments(request, sessionContext);
				else
					setupDepartments(request, sessionContext, LocationDAO.getInstance().get(Long.valueOf(editRoomForm.getId())));
                setupBuildings(request);
        		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
                return mapping.findForward(editRoomForm.getId()==null || editRoomForm.getId().length()==0?"showAddRoom":"showEditRoom");
			}
		}	
        
        if (request.getParameter("id")!=null && request.getParameter("id").length() > 0) {
            //get location information
            Long id = Long.valueOf(request.getParameter("id"));
            LocationDAO ldao = new LocationDAO();
            Location location = ldao.get(id);
            if (location instanceof Room) {
                Room r = (Room)location;
                sessionContext.checkPermission(r, Right.RoomEdit);
                editRoomForm.setName(r.getRoomNumber());
                editRoomForm.setBldgName(r.getBuildingAbbv());
                editRoomForm.setRoom(true);
            } else {
            	sessionContext.checkPermission((NonUniversityLocation)location, Right.NonUniversityLocationEdit);
                editRoomForm.setName(((NonUniversityLocation)location).getName());
                editRoomForm.setBldgName("");
                editRoomForm.setRoom(false);
            }
            editRoomForm.setExternalId(location.getExternalUniqueId());
            editRoomForm.setType(location.getRoomType().getUniqueId());
            editRoomForm.setCapacity(location.getCapacity().toString());
            if (location.getExamCapacity() != null && (location.hasAnyExamsEnabled() || location.getExamCapacity() != 0)) {
            	editRoomForm.setExamCapacity(location.getExamCapacity().toString());           
            }
            for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId()))
            	editRoomForm.setExamEnabled(type.getUniqueId().toString(), location.getExamTypes().contains(type));
            editRoomForm.setIgnoreTooFar(location.isIgnoreTooFar());
            editRoomForm.setIgnoreRoomCheck(location.isIgnoreRoomCheck());
            editRoomForm.setCoordX(location.getCoordinateX()==null ? null : location.getCoordinateX().toString());
            editRoomForm.setCoordY(location.getCoordinateY()==null ? null : location.getCoordinateY().toString());
            editRoomForm.setArea(location.getArea() == null ? null : new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00")).format(location.getArea()));
            editRoomForm.setControlDept(location.getControllingDepartment() == null ? null : location.getControllingDepartment().getUniqueId().toString());
            editRoomForm.setEventDepartment(location.getEventDepartment() == null ? null : location.getEventDepartment().getUniqueId().toString());
            
            editRoomForm.setEventStatus(location.getEventStatus() == null ? -1 : location.getEventStatus());
            editRoomForm.setBreakTime(location.getBreakTime() == null ? "" : location.getBreakTime().toString());
            editRoomForm.setNote(location.getNote() == null ? "" : location.getNote());
            
            if (sessionContext.hasPermission(location, Right.RoomEditChangeExaminationStatus)) {
            	for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId())) {
            		if (type.getType() == ExamType.sExamTypeMidterm) {
                        MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(location.getSession(), type);
                        epx.load(location);
                        epx.setName("mp" + type.getUniqueId());
                        request.setAttribute("PeriodPrefs" + type.getUniqueId(), epx.print(true));
            		} else {
                        PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), type.getUniqueId());
                        px.load(location);
                        px.setAllowRequired(false);
                        RequiredTimeTable rttPx = new RequiredTimeTable(px);
                        rttPx.setName("PeriodPrefs" +  type.getUniqueId());
                        request.setAttribute("PeriodPrefs" + type.getUniqueId(), rttPx.print(true, CommonValues.VerticalGrid.eq(UserProperty.GridOrientation.get(sessionContext.getUser())))); 
            		}
            	}
            }

            EditRoomAction.setupDepartments(request, sessionContext, location);
    		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
        } else {
        	sessionContext.checkPermission(Right.AddRoom);
            editRoomForm.reset(mapping, request);
            
            if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null) {
            	Department d = Department.findByDeptCode((String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom), sessionContext.getUser().getCurrentAcademicSessionId());
                if (d!=null) editRoomForm.setControlDept(d.getUniqueId().toString());
            }
            editRoomForm.setEventDepartment(null);

            setupDepartments(request, sessionContext);
            setupBuildings(request);
    		LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
        }
		
		return mapping.findForward(editRoomForm.getId()==null || editRoomForm.getId().length()==0?"showAddRoom":"showEditRoom");
	}
	
    public static void setupDepartments(HttpServletRequest request, SessionContext context, Location location) throws Exception {
    	Collection availableDepts = new Vector();

        for (Iterator i=new TreeSet(location.getRoomDepts()).iterator();i.hasNext();) {
            RoomDept rd = (RoomDept)i.next();
            Department d = rd.getDepartment();
            availableDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
        }
		
		request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
		
    	Collection eventDepts = new Vector();
    	
    	TreeSet<Department>  userDepartments = Department.getUserDepartments(context.getUser());
    	if (location.getEventDepartment() != null) userDepartments.add(location.getEventDepartment());
		for (Department d: userDepartments)
			if (d.isAllowEvents())
				eventDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
		
		request.setAttribute("eventDepts", eventDepts);
    }
    
    public static void setupDepartments(HttpServletRequest request, SessionContext context) throws Exception {
    	Collection availableDepts = new Vector();

        for (Department d: Department.getUserDepartments(context.getUser()))
            availableDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
		
		request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);
		
		Collection eventDepts = new Vector();
		
		for (Department d: Department.getUserDepartments(context.getUser()))
			if (d.isAllowEvents())
				eventDepts.add(new LabelValueBean(d.getDeptCode() + " - " + d.getName(), d.getUniqueId().toString()));
		
		request.setAttribute("eventDepts", eventDepts);
    }
    
    private void setupBuildings(HttpServletRequest request) throws Exception {
        ArrayList list = new ArrayList();
        for (Building b: Building.findAll(sessionContext.getUser().getCurrentAcademicSessionId())) {
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

		Long id = Long.valueOf(editRoomForm.getId());
        LocationDAO ldao = new LocationDAO();
		org.hibernate.Session hibSession = ldao.getSession();
		Transaction tx = null;
		try {
			tx = hibSession.beginTransaction();
			
			Location location = ldao.get(id, hibSession);
			
			if (location instanceof Room) {
				sessionContext.checkPermission((Room)location, Right.RoomEdit);
			} else {
				sessionContext.checkPermission((NonUniversityLocation)location, Right.NonUniversityLocationEdit);
			}
					        
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
            
            location.setExternalUniqueId(editRoomForm.getExternalId());
            location.setRoomType(RoomTypeDAO.getInstance().get(editRoomForm.getType()));
			
			location.setCoordinateX(editRoomForm.getCoordX()==null || editRoomForm.getCoordX().length()==0 ? null : Double.valueOf(editRoomForm.getCoordX()));
			location.setCoordinateY(editRoomForm.getCoordY()==null || editRoomForm.getCoordY().length()==0 ? null : Double.valueOf(editRoomForm.getCoordY()));
            Double area = null;
            if (editRoomForm.getArea() != null && !editRoomForm.getArea().isEmpty()) {
            	try {
            		area = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00")).parse(editRoomForm.getArea()).doubleValue();
            	} catch (NumberFormatException e) {
            		area = location.getArea();
            	}
            }
            location.setArea(area);
            
            if (sessionContext.hasPermission(location, Right.RoomEditChangeEventProperties)) {
    			location.setEventDepartment(editRoomForm.getEventDepartment() == null || editRoomForm.getEventDepartment().isEmpty() ? null : new DepartmentDAO().get(Long.valueOf(editRoomForm.getEventDepartment())));
            	location.setBreakTime(editRoomForm.getBreakTime() == null || editRoomForm.getBreakTime().isEmpty() ? null : Integer.parseInt(editRoomForm.getBreakTime()));
            	location.setEventStatus(editRoomForm.getEventStatus() == null || editRoomForm.getEventStatus() < 0 ? null : editRoomForm.getEventStatus());
            }
            
            String oldNote = location.getNote();
        	location.setNote(editRoomForm.getNote() == null ? "" : editRoomForm.getNote().length() > 2048 ? editRoomForm.getNote().substring(0, 2048) : editRoomForm.getNote());
			
			if (sessionContext.hasPermission(location, Right.RoomEditChangeExaminationStatus)) {
	            if (editRoomForm.getExamCapacity() != null && !editRoomForm.getExamCapacity().trim().equalsIgnoreCase("")) {
	                location.setExamCapacity(Integer.valueOf(editRoomForm.getExamCapacity().trim()));
	            }
	            
	            boolean examTypesChanged = false;
	            for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId())) {
	            	if (editRoomForm.getExamEnabled(type.getUniqueId().toString()) != location.getExamTypes().contains(type)) {
	            		examTypesChanged = true;
	            		break;
	            	}
	            }
	            if (examTypesChanged) {
	        		// Examination types has changed -- apply brute force to avoid unique constraint (PK_ROOM_EXAM_TYPE) violation
	            	if (!location.getExamTypes().isEmpty()) {
	            		location.getExamTypes().clear();
	            		hibSession.update(location); hibSession.flush();
	            	}
	            	for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId()))
	            		if (editRoomForm.getExamEnabled(type.getUniqueId().toString()))
	            			location.getExamTypes().add(type);
	            }

            	for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId())) {
            		if (location.getExamTypes().contains(type)) {
                		if (type.getType() == ExamType.sExamTypeMidterm) {
                            MidtermPeriodPreferenceModel epx = new MidtermPeriodPreferenceModel(location.getSession(), type);
                            epx.setName("mp" + type.getUniqueId());
                            epx.load(request);
                            epx.save(location);
                		} else {
                            PeriodPreferenceModel px = new PeriodPreferenceModel(location.getSession(), type.getUniqueId());
                            RequiredTimeTable rttPx = new RequiredTimeTable(px);
                            rttPx.setName("PeriodPrefs" +  type.getUniqueId());
        				    rttPx.update(request);
        				    px.save(location);
                		}
            		} else {
            			location.clearExamPreferences(type);
            		}
            	}
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
			
        	if (!ToolBox.equals(oldNote, location.getNote()))
        		ChangeLog.addChange(hibSession, sessionContext, location, (location.getNote() == null || location.getNote().isEmpty() ? "-" : location.getNote()), ChangeLog.Source.ROOM_EDIT, ChangeLog.Operation.NOTE, null, location.getControllingDepartment());
			
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
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
    	
    	sessionContext.checkPermission(editRoomForm.getControlDept(), "Department", Right.AddRoom);
    	
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
            room.setEventDepartment(editRoomForm.getEventDepartment() == null || editRoomForm.getEventDepartment().isEmpty() ? null : new DepartmentDAO().get(Long.valueOf(editRoomForm.getEventDepartment())));
            room.getRoomDepts().add(rd);
            room.setCapacity(Integer.valueOf(editRoomForm.getCapacity().trim()));
            room.setExamCapacity(editRoomForm.getExamCapacity() == null || editRoomForm.getExamCapacity().trim().isEmpty() ? 0 : Integer.valueOf(editRoomForm.getExamCapacity().trim()));
            room.setExamTypes(new HashSet<ExamType>());
            for (ExamType type: ExamType.findAllUsed(sessionContext.getUser().getCurrentAcademicSessionId()))
            	if (editRoomForm.getExamEnabled(type.getUniqueId().toString()))
            		room.getExamTypes().add(type);
            room.setIgnoreTooFar(Boolean.FALSE);
            room.setIgnoreRoomCheck(editRoomForm.isIgnoreRoomCheck()!=null && editRoomForm.isIgnoreRoomCheck().booleanValue());
            room.setExternalUniqueId(editRoomForm.getExternalId());
            room.setRoomType(RoomTypeDAO.getInstance().get(editRoomForm.getType()));
            room.setCoordinateX(editRoomForm.getCoordX()==null || editRoomForm.getCoordX().length()==0 ? null : Double.valueOf(editRoomForm.getCoordX()));
            room.setCoordinateY(editRoomForm.getCoordY()==null || editRoomForm.getCoordY().length()==0 ? null : Double.valueOf(editRoomForm.getCoordY()));
            Double area = null;
            if (editRoomForm.getArea() != null && !editRoomForm.getArea().isEmpty()) {
            	try {
            		area = new DecimalFormat(ApplicationProperties.getProperty("unitime.room.area.units.format", "#,##0.00")).parse(editRoomForm.getArea()).doubleValue();
            	} catch (NumberFormatException e) {
            	}
            }
            room.setArea(area);
            room.setSession(room.getControllingDepartment() == null ?
            		SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId(), hibSession) : room.getControllingDepartment().getSession());
            
            room.setBreakTime(editRoomForm.getBreakTime() == null || editRoomForm.getBreakTime().isEmpty() ? null : Integer.parseInt(editRoomForm.getBreakTime()));
            room.setEventStatus(editRoomForm.getEventStatus() == null || editRoomForm.getEventStatus() < 0 ? null : editRoomForm.getEventStatus());
            room.setNote(editRoomForm.getNote() == null ? "" : editRoomForm.getNote().length() > 2048 ? editRoomForm.getNote().substring(0, 2048) : editRoomForm.getNote());
            
            LocationPermIdGenerator.setPermanentId(room);

            hibSession.saveOrUpdate(room);
            
            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
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

