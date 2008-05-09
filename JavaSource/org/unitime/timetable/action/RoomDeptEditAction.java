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

import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.RoomDeptEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.util.Constants;

public class RoomDeptEditAction extends Action {

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		RoomDeptEditForm myForm = (RoomDeptEditForm)form;
		
		HttpSession webSession = request.getSession();
		User user = Web.getUser(webSession);
		Long sessionId = Session.getCurrentAcadSession(user).getUniqueId();
		
		Department d = null;
		if (webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME) != null) {
			String deptCode = webSession.getAttribute(Constants.DEPT_CODE_ATTR_ROOM_NAME).toString();
			if ("Exam".equalsIgnoreCase(deptCode)) {
			    myForm.setId(null);
			    myForm.setExamType(Exam.sExamTypeFinal);
			} else if ("EExam".equalsIgnoreCase(deptCode)) { 
                myForm.setId(null);
                myForm.setExamType(Exam.sExamTypeMidterm);
			} else {
			    d = Department.findByDeptCode(deptCode, sessionId);
			    myForm.setId(d.getUniqueId());
			    myForm.setExamType(-1);
			}
		}

		if (request.getParameter("deptId") != null) {
			String id = request.getParameter("deptId");
            if ("Exam".equalsIgnoreCase(id)) {
                myForm.setId(null);
                myForm.setExamType(Exam.sExamTypeFinal);
            } else if ("EExam".equalsIgnoreCase(id)) { 
                myForm.setId(null);
                myForm.setExamType(Exam.sExamTypeMidterm);
            } else {
                d = new DepartmentDAO().get(Long.valueOf(id));
                myForm.setId(d.getUniqueId());
                myForm.setExamType(-1);
            }
		}
		
		TreeSet rooms = null;
		if (myForm.getId()==null && myForm.getExamType()>=0) {
		    rooms = new TreeSet(Room.findAll(Session.getCurrentAcadSession(user).getUniqueId()));
		} else {
		    rooms = new TreeSet(Session.getCurrentAcadSession(user).getRoomsFast(user));
		    for (Iterator i=rooms.iterator();i.hasNext();) {
		        Location location = (Location)i.next();
		        if (!(location instanceof Room)) i.remove();
		    }
		}
		
        int examType = myForm.getExamType();
        if (myForm.getId()!=null && myForm.getExamType()<0) d = new DepartmentDAO().get(myForm.getId());
        
        if (d!=null)
            myForm.setName(d.getDeptCode()+" "+d.getName());
        else if (examType==Exam.sExamTypeFinal)
            myForm.setName("Final Examination Rooms");
        else if (examType==Exam.sExamTypeMidterm)
            myForm.setName("Midterm Examination Rooms");
        else
            myForm.setName("Unknown");

        String op = myForm.getOp();
        if (request.getParameter("op")!=null) op = request.getParameter("op");
        if (request.getParameter("ord")!=null && request.getParameter("ord").length()>0) op = "ord";
        
        if (op==null) {
            myForm.getAssignedSet().clear();
		    if (d==null) {
		        for (Iterator i=Location.findAllExamLocations(sessionId, examType).iterator();i.hasNext();) {
		            Location location = (Location)i.next();
		            myForm.getAssignedSet().add(location.getUniqueId());
		        }
		    } else {
		        for (Iterator i=d.getRoomDepts().iterator();i.hasNext();) {
		            RoomDept rd = (RoomDept)i.next();
		            myForm.getAssignedSet().add(rd.getRoom().getUniqueId());
		        }
		    }
		}
        
        if ("Back".equals(op)) {
            return mapping.findForward("back");
        }
        
        if ("Update".equals(op)) {
            ActionMessages errors = myForm.validate(mapping, request);
            if (errors.size() == 0) {
                Transaction tx = null;
                try {
                    org.hibernate.Session hibSession = new RoomDeptDAO().getSession();
                    tx = hibSession.beginTransaction();
                    
                    for (Iterator i=rooms.iterator();i.hasNext();) {
                        Location location = (Location)i.next();
                        boolean checked = myForm.getAssignedSet().contains(location.getUniqueId());
                        boolean current = (d==null?location.isExamEnabled(examType):location.hasRoomDept(d));
                        if (current!=checked) {
                            if (d==null) {
                                location.setExamEnabled(examType, checked);
                                hibSession.update(location);
                                ChangeLog.addChange(hibSession, request, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, null);
                            } else if (checked) {
                                RoomDept rd = new RoomDept();
                                rd.setDepartment(d);
                                rd.setRoom(location);
                                rd.setControl(Boolean.FALSE);
                                d.getRoomDepts().add(rd);
                                location.getRoomDepts().add(rd);
                                hibSession.saveOrUpdate(location);
                                hibSession.saveOrUpdate(rd);
                                ChangeLog.addChange(hibSession, request, location,
                                        ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.CREATE, null, d);
                            } else {
                                RoomDept rd = null;
                                for (Iterator j=location.getRoomDepts().iterator();rd==null && j.hasNext();) {
                                    RoomDept x = (RoomDept)j.next();
                                    if (x.getDepartment().equals(d)) rd=x;
                                }
                                ChangeLog.addChange(hibSession, request, location,
                                        ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.DELETE, null, d);
                                d.getRoomDepts().remove(rd);
                                location.getRoomDepts().remove(rd);
                                hibSession.saveOrUpdate(rd.getRoom());
                                hibSession.delete(rd);
                                location.removedFromDepartment(d, hibSession);
                            }
                        }
                    }
                    
                    if (d!=null) hibSession.saveOrUpdate(d);
                    tx.commit();
                    if (d!=null) hibSession.refresh(d);
                    
                    return mapping.findForward("back");
                } catch (Exception e) {
                    if (tx!=null) tx.rollback();
                    throw e;
                }
            } else {
                saveErrors(request, errors);
            }
        }

        WebTable table =
            (d==null?
                    new WebTable(7, null, "javascript:document.getElementsByName('ord')[0].value=%%;roomDeptEditForm.submit();", 
                            new String[] {"Use", "Room", "Capacity", "Exam Capacity", "Type", "Global<br>Groups", "Global<br>Features"}, 
                            new String[] {"left", "left", "right", "right", "left", "left", "left", "left"}, 
                            new boolean[] {true, true, true, true, true, true, true})
                    :
            new WebTable(6, null, "javascript:document.getElementsByName('ord')[0].value=%%;roomDeptEditForm.submit();", 
                new String[] {"Use", "Room", "Capacity", "Type", "Global<br>Groups", "Global<br>Features"}, 
                new String[] {"left", "left", "right", "left", "left", "left"}, 
                new boolean[] {true, true, true, true, true, true}));

        for (Iterator i=rooms.iterator();i.hasNext();) {
		    Location location = (Location)i.next();
		    boolean checked = myForm.getAssignedSet().contains(location.getUniqueId());
		    String g = "", f = "";
		    for (Iterator j=location.getGlobalRoomFeatures().iterator();j.hasNext();) {
		        GlobalRoomFeature grf = (GlobalRoomFeature)j.next();
		        f += grf.getAbbv();
		        if (j.hasNext()) f += ", ";
		    }
		    for (Iterator j=location.getRoomGroups().iterator();j.hasNext();) {
		        RoomGroup rg = (RoomGroup)j.next();
		        if (rg.isGlobal()) {
		            if (g.length()>0) g += ", ";
		            g += rg.getAbbv();
		        }
		    }
		    if (d==null)
		        table.addLine(
                    new String[] {
                            "<input type='checkbox' name='assigned' value='"+location.getUniqueId()+"' "+(checked?"checked='checked'":"")+">",
                            location.getLabel(),
                            String.valueOf(location.getCapacity()),
                            String.valueOf(location.getExamCapacity()),
                            Room.getSchedulingRoomTypeName(location.getSchedulingRoomTypeInteger()),
                            g,f
                    },
                    new Comparable[] {
                            (!checked?1:0),
                            location.getLabel(),
                            location.getCapacity(),
                            location.getExamCapacity(),
                            location.getSchedulingRoomTypeInteger(),
                            null, null
                    });
		    else
	                table.addLine(
	                    new String[] {
	                            "<input type='checkbox' name='assigned' value='"+location.getUniqueId()+"' "+(checked?"checked='checked'":"")+">",
	                            location.getLabel(),
	                            String.valueOf(location.getCapacity()),
	                            Room.getSchedulingRoomTypeName(location.getSchedulingRoomTypeInteger()),
	                            g,f
	                    },
	                    new Comparable[] {
	                            (!checked?1:0),
	                            location.getLabel(),
	                            location.getCapacity(),
	                            location.getSchedulingRoomTypeInteger(),
	                            null, null
	                    });
		}
        
        
        WebTable.setOrder(webSession, "RoomDeptEdit.ord", request.getParameter("ord"), (d==null?4:5));
        myForm.setTable(table.printTable(WebTable.getOrder(webSession, "RoomDeptEdit.ord")));

		return mapping.findForward("show");
	}
    
}
