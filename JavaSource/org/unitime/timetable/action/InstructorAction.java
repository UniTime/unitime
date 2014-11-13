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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;


/**
 * Methods common to Instructor Add and Edit
 *
 * @author Tomas Muller, Zuzana Mullerova, Heston Fernandes, Stephanie Schluttenhofer
 */
public class InstructorAction extends Action {

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired SessionContext sessionContext;
	
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
	
		InstructorEditForm frm = (InstructorEditForm) form;
		
		frm.setLookupEnabled(ApplicationProperty.InstructorExternalIdLookup.isTrue() && ApplicationProperty.InstructorExternalIdLookupClass.value() != null);

		return null;
	}
	
	/**
	 * Fills form with selected staff record info
     * @param frm
     * @param request
     */
    protected void fillStaffInfo(
            InstructorEditForm frm, 
            HttpServletRequest request) throws Exception {
        
        Staff staff = new StaffDAO().get(new Long(frm.getSearchSelect()));
        frm.setPuId(staff.getExternalUniqueId());
        frm.setFname(staff.getFirstName()!=null ? staff.getFirstName().trim() : "");
        frm.setMname(staff.getMiddleName()!=null ? staff.getMiddleName().trim() : "");
        frm.setLname(staff.getLastName()!=null ? staff.getLastName().trim() : "");
        frm.setTitle(staff.getAcademicTitle() != null ? staff.getAcademicTitle().trim() : "");
        frm.setEmail(staff.getEmail());
        if (staff.getPositionType() != null &&  (frm.getPosType() == null || frm.getPosType().trim().length() == 0))
        	frm.setPosType(staff.getPositionType().getUniqueId().toString());
    }

    /**
     * Fills form with selected i2a2 info
     * @param frm
     * @param request
     */
    protected void fillI2A2Info(
            InstructorEditForm frm, 
            HttpServletRequest request) throws Exception {
        
	    String login = frm.getCareerAcct();
	    if (login!=null && login.trim().length()>0 && frm.getLookupEnabled()) {
	    	UserInfo results = lookupInstructor(frm);
	    	if (results != null) {
				frm.setPuId(results.getExternalId());
				frm.setCareerAcct(results.getUserName());
				frm.setFname(results.getFirstName());
				frm.setMname(results.getMiddleName());
				frm.setLname(results.getLastName());
	    		frm.setEmail(results.getEmail());
	    		frm.setTitle(results.getAcademicTitle());
	    	}
	    }	    
    }

    /**
	 * Searches STAFF for matches on name / career account
	 * Searches I2A2 for matching career account
	 * @param frm
	 * @param request
	 * @throws Exception
	 */
    protected void findMatchingInstructor(
	        InstructorEditForm frm, 
	        HttpServletRequest request) throws Exception {
	    
	    frm.setMatchFound(new Boolean(false));
	    String fname = frm.getFname();
	    String lname = frm.getLname();
	    String login = frm.getCareerAcct();

	    // Check I2A2
	    if (login!=null && login.trim().length()>0 && frm.getLookupEnabled()) {
	    	UserInfo results = lookupInstructor(frm);
	    	if (results!=null) {
				frm.setI2a2Match(results);
	    		frm.setMatchFound(Boolean.TRUE);
	    	}
	    }
	    
	    // Check Staff
	    List staffList = Staff.findMatchingName(fname, lname);
	    frm.setStaffMatch(staffList);

	    if (staffList!=null && staffList.size()>0)
            frm.setMatchFound(new Boolean(true));
	}

    /**
     * Lookup instructor details 
     * @param frm
     */
    private UserInfo lookupInstructor(InstructorEditForm frm) throws Exception {
        String id = frm.getCareerAcct();
        if (id!=null && id.trim().length()>0 && frm.getLookupEnabled().booleanValue()) {
        	String className = ApplicationProperty.InstructorExternalIdLookupClass.value(); 
        	ExternalUidLookup lookup = (ExternalUidLookup) (Class.forName(className).newInstance());
       		return lookup.doLookup(id);
        }
        return null;
    }

    
	/**
	 * Inserts / Updates Instructor Info
	 * @param frm
	 * @param request
	 */
	protected void doUpdate(
	        InstructorEditForm frm, 
	        HttpServletRequest request) throws Exception {
	    
		DepartmentalInstructorDAO idao = new DepartmentalInstructorDAO();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
			
			DepartmentalInstructor inst = null;
			String instrId = frm.getInstructorId();
			
			if (instrId!=null && instrId.trim().length()>0) {
			    inst = new DepartmentalInstructorDAO().get(new Long(instrId));
			}
			else {    
			    inst = new DepartmentalInstructor();
			}

			if (frm.getFname() != null && frm.getFname().trim().length()>0) {
				inst.setFirstName(frm.getFname().trim());
			} else {
				inst.setFirstName(null);
			}
			
			if (frm.getMname() != null && frm.getMname().trim().length()>0) {
				inst.setMiddleName(frm.getMname().trim());
			} else {
				inst.setMiddleName(null);
			}
			
			inst.setLastName(frm.getLname().trim());
			
			if (frm.getTitle() != null && frm.getTitle().trim().length()>0) {
				inst.setAcademicTitle(frm.getTitle().trim());
			} else {
				inst.setAcademicTitle(null);
			}
			
			if (frm.getPuId() != null && frm.getPuId().trim().length()>0 && !frm.getPuId().equalsIgnoreCase("null")) {
				inst.setExternalUniqueId(frm.getPuId().trim());
			}

			if (frm.getCareerAcct() != null && frm.getCareerAcct().trim().length()>0) {
				inst.setCareerAcct(frm.getCareerAcct().trim());
			}
			
			inst.setEmail(frm.getEmail());
						
			if (frm.getPosType() != null && frm.getPosType().trim().length()>0) {
				PositionType pt = PositionType.findById(new Long(frm.getPosType().trim()));
				if (pt != null) {
					inst.setPositionType(pt);
				}
			}
			
			if (frm.getNote() != null && !frm.getNote().isEmpty()) {
				if (frm.getNote().length() > 2048)
					inst.setNote(frm.getNote().substring(0, 2048));
				else
					inst.setNote(frm.getNote());
			} else 
				inst.setNote(null);
			
			Department d = null;
			//get department
			if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
				String deptId = (String) sessionContext.getAttribute(SessionAttribute.DepartmentId);
				d = new DepartmentDAO().get(new Long(deptId));
				inst.setDepartment(d);
				d.getInstructors().add(inst);
			}
			else
			    throw new Exception("Department Id could not be retrieved from session");
            
            inst.setIgnoreToFar(new Boolean(frm.getIgnoreDist()));
            
			hibSession.saveOrUpdate(inst);

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    (instrId==null || instrId.trim().length()<=0?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    null, 
                    inst.getDepartment());

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
     * Checks that combination of Instructor/Dept 
     * does not already exist
     * @param frm
     * @return
     */
    protected boolean isDeptInstructorUnique(
            InstructorEditForm frm, 
	        HttpServletRequest request ) {
        
        String query = "from DepartmentalInstructor " +
        				"where externalUniqueId=:puid and department.uniqueId=:deptId";
        if (frm.getInstructorId()!=null && frm.getInstructorId().trim().length()>0) {
            query += " and uniqueId!=:uniqueId";
        }
        
        DepartmentalInstructorDAO ddao = new DepartmentalInstructorDAO();
        org.hibernate.Session hibSession = ddao.getSession();
        
		HttpSession httpSession = request.getSession();
		String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
       
        Query q = hibSession.createQuery(query);
        q.setString("puid", frm.getPuId().trim());
        q.setLong("deptId", Long.parseLong(deptId));
        if (frm.getInstructorId()!=null && frm.getInstructorId().trim().length()>0) {
            q.setString("uniqueId", frm.getInstructorId().trim());
        }
        
        return (q.list().size()==0);
    }

	
}
