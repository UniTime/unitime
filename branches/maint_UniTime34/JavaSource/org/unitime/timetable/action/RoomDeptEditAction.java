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

import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.RoomDeptEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.GlobalRoomFeature;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.model.RoomDept;
import org.unitime.timetable.model.RoomGroup;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.RoomDeptDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@Service("/roomDeptEdit")
public class RoomDeptEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		RoomDeptEditForm myForm = (RoomDeptEditForm)form;
		
		Department d = null;
		if (sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom) != null) {
			String deptCode = (String)sessionContext.getAttribute(SessionAttribute.DepartmentCodeRoom);
			if (deptCode != null && deptCode.matches("Exam[0-9]*")) {
			    myForm.setId(null);
			    myForm.setExamType(Long.valueOf(deptCode.substring(4)));
			    sessionContext.checkPermission(Right.EditRoomDepartmentsExams);
			} else {
				sessionContext.checkPermission(deptCode, "Department", Right.EditRoomDepartments);
			    d = Department.findByDeptCode(deptCode, sessionContext.getUser().getCurrentAcademicSessionId());
			    myForm.setId(d.getUniqueId());
			    myForm.setExamType(null);
			}
		}

		if (request.getParameter("deptId") != null) {
			String id = request.getParameter("deptId");
            if (id != null && id.matches("Exam[0-9]*")) {
            	sessionContext.checkPermission(Right.EditRoomDepartmentsExams);
                myForm.setId(null);
                myForm.setExamType(Long.valueOf(id.substring(4)));
            } else {
                d = new DepartmentDAO().get(Long.valueOf(id));
            	sessionContext.checkPermission(d, Right.EditRoomDepartments);
                myForm.setId(d.getUniqueId());
                myForm.setExamType(null);
            }
		}
		
        if (d == null && myForm.getId() != null && myForm.getExamType() < 0)
        	d = new DepartmentDAO().get(myForm.getId());

		TreeSet<Room> rooms = new TreeSet<Room>();
		if (sessionContext.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent)) {
			rooms.addAll(Location.findAllRooms(sessionContext.getUser().getCurrentAcademicSessionId()));
		} else {
			for (Department department: Department.getUserDepartments(sessionContext.getUser())) {
				for (RoomDept rd: department.getRoomDepts()) {
					if (rd.getRoom() instanceof Room)
						rooms.add((Room)rd.getRoom());
				}
			}
		}
		
        ExamType examType = (myForm.getExamType() == null ? null : ExamTypeDAO.getInstance().get(myForm.getExamType()));
        
        if (d != null)
            myForm.setName(d.getDeptCode()+" "+d.getName());
        else if (examType != null)
            myForm.setName(examType.getLabel() + "Examination Rooms");
        else
            myForm.setName("Unknown");

        String op = myForm.getOp();
        if (request.getParameter("op")!=null) op = request.getParameter("op");
        if (request.getParameter("ord")!=null && request.getParameter("ord").length()>0) op = "ord";
        
        if (op==null) {
            myForm.getAssignedSet().clear();
		    if (d==null) {
		        for (Iterator i=Location.findAllExamLocations(sessionContext.getUser().getCurrentAcademicSessionId(), examType).iterator();i.hasNext();) {
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
                                ChangeLog.addChange(hibSession, sessionContext, location, ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.UPDATE, null, null);
                            } else if (checked) {
                                RoomDept rd = new RoomDept();
                                rd.setDepartment(d);
                                rd.setRoom(location);
                                rd.setControl(Boolean.FALSE);
                                d.getRoomDepts().add(rd);
                                location.getRoomDepts().add(rd);
                                hibSession.saveOrUpdate(location);
                                hibSession.saveOrUpdate(rd);
                                ChangeLog.addChange(hibSession, sessionContext, location,
                                        ChangeLog.Source.ROOM_DEPT_EDIT, ChangeLog.Operation.CREATE, null, d);
                            } else {
                                RoomDept rd = null;
                                for (Iterator j=location.getRoomDepts().iterator();rd==null && j.hasNext();) {
                                    RoomDept x = (RoomDept)j.next();
                                    if (x.getDepartment().equals(d)) rd=x;
                                }
                                ChangeLog.addChange(hibSession, sessionContext, location,
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
                            location.getRoomTypeLabel(),
                            g,f
                    },
                    new Comparable[] {
                            (!checked?1:0),
                            location.getLabel(),
                            location.getCapacity(),
                            location.getExamCapacity(),
                            location.getRoomTypeLabel(),
                            null, null
                    });
		    else
	                table.addLine(
	                    new String[] {
	                            "<input type='checkbox' name='assigned' value='"+location.getUniqueId()+"' "+(checked?"checked='checked'":"")+">",
	                            location.getLabel(),
	                            String.valueOf(location.getCapacity()),
	                            location.getRoomTypeLabel(),
	                            g,f
	                    },
	                    new Comparable[] {
	                            (!checked?1:0),
	                            location.getLabel(),
	                            location.getCapacity(),
	                            location.getRoomTypeLabel(),
	                            null, null
	                    });
		}
        
        
        WebTable.setOrder(sessionContext, "RoomDeptEdit.ord", request.getParameter("ord"), (d==null?4:5));
        myForm.setTable(table.printTable(WebTable.getOrder(sessionContext, "RoomDeptEdit.ord")));

		return mapping.findForward("show");
	}
    
}
