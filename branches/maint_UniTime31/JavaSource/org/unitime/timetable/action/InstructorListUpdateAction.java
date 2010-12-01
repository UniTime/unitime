/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.InstructorListUpdateForm;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.DepartmentalInstructorComparator;
import org.unitime.timetable.model.comparators.StaffComparator;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 07-18-2006
 * 
 * XDoclet definition:
 * @struts.action path="/updateInstructorList" name="updateInstructorListForm" input="/user/updateInstructorList.jsp" scope="request"
 */
public class InstructorListUpdateAction extends Action {

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
		
		//Check permissions
		HttpSession httpSession = request.getSession();
		if (!Web.isLoggedIn(httpSession)) {
			throw new Exception("Access Denied.");
		}
		
		InstructorListUpdateForm frm = (InstructorListUpdateForm) form;
		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);		
		MessageResources rsc = getResources(request);
		String op = frm.getOp();
		
        // Cancel - Go back to Instructors Detail Screen
        if(op != null && op.equals(rsc.getMessage("button.backToInstructorList"))) {
        	response.sendRedirect( response.encodeURL("instructorList.do"));
        	return null;
        }
        
        // Update - Update the instructor and go back to Instructor List Screen
        if(op != null && op.equals(rsc.getMessage("button.update")) ) {
	        update(frm, request);
	        response.sendRedirect( response.encodeURL("instructorList.do"));
	        return null;
        }
		
        // Refresh - set filters
        if(op != null && op.equals(rsc.getMessage("button.applyFilter")) ) {
            request.setAttribute("filterApplied", "1");
        }
        
        Collection assigned = getAssigned(frm, request);
		if (assigned != null) {
			frm.setAssignedInstr(assigned);
		} 
		
		Collection available = getAvailable(frm, request);
		if (available != null) {
			frm.setAvailableInstr(available);
		}
		
		// Get Position Types
		LookupTables.setupPositionTypes(request);
		setupFilters(frm, request);
		
		frm.setInstructors();		
		
		return mapping.findForward("showUpdateInstructorList");
	}

	/**
     * @param request
     */
    private void setupFilters(InstructorListUpdateForm frm, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        String[] defaultPosTypes = {"ADMIN_STAFF", "CLERICAL_STAFF", "SERVICE_STAFF", "FELLOWSHIP", "UNDRGRD_TEACH_ASST", "EMERITUS OTHER"};
        boolean filterSet = UserData.getPropertyBoolean(httpSession, "instrListFilter", false);
        String filterApplied = (String) request.getAttribute("filterApplied");
        
        if (filterApplied!=null && !filterApplied.equals("1"))
            filterApplied = null;
        
        if (!filterSet) {
            frm.setDisplayListType("both");
            frm.setDisplayPosType(defaultPosTypes);
        } 
        else if (filterApplied == null) {            
            frm.setDisplayListType(UserData.getProperty(httpSession, "displayListType"));
            String displayPosType = UserData.getProperty(httpSession, "displayPosType");
            if (displayPosType!=null) {
                String[] arr = null;
                if (displayPosType.trim().length()==0) {
                    arr = new String[] {"X"};
                }
                else {
                    StringTokenizer strTok = new StringTokenizer(displayPosType);
                    arr = new String [strTok.countTokens()];
                    int ct = 0;
                    while (strTok.hasMoreTokens()) {
                        arr[ct++] = (String) strTok.nextToken();
                    }
                }    
                frm.setDisplayPosType(arr);
            }
        }
        
        if (frm.getDisplayListType()==null || frm.getDisplayListType().length()==0)
            frm.setDisplayListType("both");
        
        if (frm.getDisplayPosType()==null) {
            if (filterApplied!=null)
                frm.setDisplayPosType(new String[] {"X"});
            else
                frm.setDisplayPosType(defaultPosTypes);
        }
        
        UserData.setProperty(httpSession, "instrListFilter", "1");
        UserData.setProperty(httpSession, "displayListType", frm.getDisplayListType());
        UserData.setProperty(httpSession, "displayPosType", Constants.arrayToStr(frm.getDisplayPosType(), "", " "));
    }

    /**
	 * 
	 * @param frm
	 * @param request
	 */
	private void update(InstructorListUpdateForm frm, HttpServletRequest request) throws Exception {
		String[] selectedAssigned = frm.getAssignedSelected();
		String[] selectedNotAssigned = frm.getAvailableSelected();
		Collection assigned = getAssigned(frm, request);
		Collection available = getAvailable(frm, request);
		
		StringBuffer s1 = new StringBuffer();
		StringBuffer s2 = new StringBuffer();
		if (selectedAssigned.length != 0) {
			s1.append(Constants.arrayToStr(selectedAssigned,"",","));
		}
		if (selectedNotAssigned.length != 0) {
			s2.append(Constants.arrayToStr(selectedNotAssigned,"",","));
		}
		
		DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		StaffDAO sdao = new StaffDAO();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		HashSet<Class_> updatedClasses = new HashSet<Class_>(); 
		try {	
			tx = hibSession.beginTransaction();

			//remove instructor from assigned
			if ( frm.getDisplayListType()!=null 
							&& ( frm.getDisplayListType().equals("assigned") 
									|| frm.getDisplayListType().equals("both")
								) ) { 
			
				for (Iterator iter = assigned.iterator(); iter.hasNext(); ) {
					DepartmentalInstructor inst = (DepartmentalInstructor) iter.next();
					if (s1.indexOf(inst.getUniqueId().toString()) == -1) {
						if (!inst.getExams().isEmpty() || !inst.getClasses().isEmpty()) continue;
						
                        ChangeLog.addChange(
                                hibSession, 
                                request, 
                                inst, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.DELETE, 
                                null, inst.getDepartment());

                        for (Iterator i=inst.getClasses().iterator();i.hasNext();) {
				        	ClassInstructor ci = (ClassInstructor)i.next();
				        	updatedClasses.add(ci.getClassInstructing());
				        	ci.getClassInstructing().getClassInstructors().remove(ci);
				        	hibSession.saveOrUpdate(ci);
				        	hibSession.delete(ci);
				        }
					    
				        for (Iterator i=inst.getAssignments().iterator();i.hasNext();) {
				        	Assignment a = (Assignment)i.next();
				        	a.getInstructors().remove(inst);
				        	hibSession.saveOrUpdate(a);
				        }
				        
				        for (Iterator i=inst.getDesignatorSubjectAreas().iterator();i.hasNext();) {
				            Designator d = (Designator) i.next();
				            SubjectArea sa = d.getSubjectArea();
				            sa.getDesignatorInstructors().remove(d);
				        	hibSession.saveOrUpdate(sa);
				        	hibSession.delete(d);
				        }
                        
						hibSession.delete(inst);
					}
				}
			}
			
			if ( frm.getDisplayListType()!=null 
					&& ( frm.getDisplayListType().equals("available") 
							|| frm.getDisplayListType().equals("both")
						) ) { 
	
				//move instructor from staff to department
				for (Iterator iter = available.iterator(); iter.hasNext(); ) {
					Staff staff = (Staff) iter.next();
					if (s2.indexOf(staff.getUniqueId().toString()) != -1) {
						DepartmentalInstructor inst = new DepartmentalInstructor();
						inst.setLastName(staff.getLastName());
						inst.setEmail(staff.getEmail());
						
						HttpSession httpSession = request.getSession();
						String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
						Department d = new DepartmentDAO().get(new Long(deptId));
						inst.setDepartment(d);
						
						if (staff.getFirstName() != null) {
							inst.setFirstName(staff.getFirstName());
						}
						if (staff.getMiddleName() != null) {
							inst.setMiddleName(staff.getMiddleName());
						}
						if (staff.getExternalUniqueId() != null) {
							inst.setExternalUniqueId(staff.getExternalUniqueId());
						}
						if (staff.getPositionCode() != null) {
						    inst.setPositionType(staff.getPositionCode().getPositionType());
						}
                        
                        inst.setIgnoreToFar(Boolean.FALSE);
                        
						hibSession.save(inst);

                        ChangeLog.addChange(
                                hibSession, 
                                request, 
                                inst, 
                                ChangeLog.Source.INSTRUCTOR_MANAGE, 
                                ChangeLog.Operation.CREATE, 
                                null, inst.getDepartment());
					}
				}
			}
			
			tx.commit();
            String className = ApplicationProperties.getProperty("tmtbl.external.class.edit_action.class");
        	if (className != null && className.trim().length() > 0){
            	ExternalClassEditAction editAction = (ExternalClassEditAction) (Class.forName(className).newInstance());
            	for(Class_ c : updatedClasses){
            		editAction.performExternalClassEditAction(c, hibSession);
            	}
        	}
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
	 * @param frm
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private Collection getAvailable(InstructorListUpdateForm frm, HttpServletRequest request) throws Exception {
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);	
		
		if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
			String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
			Department d = new DepartmentDAO().get(new Long(deptId));
			List available = Staff.getStaffByDept(d.getDeptCode().trim(), sessionId);			
			Collections.sort(available, new StaffComparator(StaffComparator.COMPARE_BY_POSITION));
			return available;
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param frm
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private Collection getAssigned(InstructorListUpdateForm frm, HttpServletRequest request) throws Exception {
		HttpSession httpSession = request.getSession();
		User user = Web.getUser(httpSession);
		Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);	
		
		if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
			String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
			List assigned = DepartmentalInstructor.getInstructorByDept(sessionId, new Long(deptId));
			Collections.sort(assigned, new DepartmentalInstructorComparator(DepartmentalInstructorComparator.COMPARE_BY_POSITION));
			return assigned;
		} else {
			return null;
		}
	}

}

