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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.util.Constants;


/**
 * Methods common to Instructor Add and Edit
 */
public class InstructorAction extends Action {

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
		
        String uidLookupEnabled = ApplicationProperties.getProperty("tmtbl.instructor.external_id.lookup.enabled");
        if (uidLookupEnabled!=null && uidLookupEnabled.equalsIgnoreCase("true")) {
        	frm.setLookupEnabled(Boolean.TRUE);
        }
        else {
        	frm.setLookupEnabled(Boolean.FALSE);
        }


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
        if (staff.getPositionCode()!=null && staff.getPositionCode().getPositionType()!=null &&
        		(frm.getPosType()==null || frm.getPosType().trim().length()==0))
        	frm.setPosType(staff.getPositionCode().getPositionType().getUniqueId().toString());
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
	    	Map results = lookupInstructor(frm);
	    	if (results!=null && results.size()>0) {
				frm.setPuId((String)results.get(ExternalUidLookup.EXTERNAL_ID));
				frm.setCareerAcct((String)results.get(ExternalUidLookup.USERNAME));
				frm.setFname((String)results.get(ExternalUidLookup.FIRST_NAME));
				frm.setMname((String)results.get(ExternalUidLookup.MIDDLE_NAME));
				frm.setLname((String)results.get(ExternalUidLookup.LAST_NAME));
	    		
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
	    	Map results = lookupInstructor(frm);
	    	if (results!=null && results.size()>0) {

				String fname1 = ((String)results.get(ExternalUidLookup.FIRST_NAME));
				String mname1 = ((String)results.get(ExternalUidLookup.MIDDLE_NAME));
				String lname1 = ((String)results.get(ExternalUidLookup.LAST_NAME));				

				User user = new User();
				user.setId((String)results.get(ExternalUidLookup.EXTERNAL_ID));
				user.setLogin((String)results.get(ExternalUidLookup.USERNAME));
				user.setName(Constants.toInitialCase(
							((lname1==null?"":lname1.trim())+", "+
							 (fname1==null?"":fname1.trim())+
							 (mname1==null?"":" "+mname1.trim()))));
				
				frm.setI2a2Match(user);
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
    private Map lookupInstructor(InstructorEditForm frm) throws Exception{
    	Map results = null;
        String id = frm.getCareerAcct();
        if (id!=null && id.trim().length()>0 && frm.getLookupEnabled().booleanValue()) {
            
        	HashMap attributes = new HashMap();
        	attributes.put(ExternalUidLookup.SEARCH_ID, id);
        	
        	String className = ApplicationProperties.getProperty("tmtbl.instructor.external_id.lookup.class");        	
        	ExternalUidLookup lookup = (ExternalUidLookup) (Class.forName(className).newInstance());
       		results = lookup.doLookup(attributes);
        }
        
        return results;
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
			
			HttpSession httpSession = request.getSession();
			User user = Web.getUser(httpSession);	
			Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
			
			DepartmentalInstructor inst = null;
			String instrId = frm.getInstructorId();
			
			if (instrId!=null && instrId.trim().length()>0) {
			    inst = new DepartmentalInstructorDAO().get(new Long(instrId));
			}
			else {    
			    inst = new DepartmentalInstructor();
			}

			if (frm.getFname() != null && frm.getFname().trim().length()>0) {
				inst.setFirstName(frm.getFname().trim().toUpperCase());
			} else {
				inst.setFirstName(null);
			}
			
			if (frm.getMname() != null && frm.getMname().trim().length()>0) {
				inst.setMiddleName(frm.getMname().trim().toUpperCase());
			} else {
				inst.setMiddleName(null);
			}
			
			inst.setLastName(frm.getLname().trim().toUpperCase());
			
			if (frm.getPuId() != null && frm.getPuId().trim().length()>0) {
				inst.setExternalUniqueId(frm.getPuId().trim());
			}

			if (frm.getCareerAcct() != null && frm.getCareerAcct().trim().length()>0) {
				inst.setCareerAcct(frm.getCareerAcct().trim());
			}
						
			if (frm.getPosType() != null && frm.getPosType().trim().length()>0) {
				PositionType pt = PositionType.findById(new Long(frm.getPosType().trim()));
				if (pt != null) {
					inst.setPositionType(pt);
				}
			}
			
			if (frm.getNote() != null && frm.getNote().trim().length()>0) {
				inst.setNote(frm.getNote().trim());
			} else 
				inst.setNote(null);
			
			Department d = null;
			//get department
			if (httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME) != null) {
				String deptId = (String) httpSession.getAttribute(Constants.DEPT_ID_ATTR_NAME);
				d = new DepartmentDAO().get(new Long(deptId));
				inst.setDepartment(d);
			}
			else
			    throw new Exception("Department Id could not be retrieved from session");
            
            inst.setIgnoreToFar(new Boolean(frm.getIgnoreDist()));
            
			hibSession.saveOrUpdate(inst);

            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    inst, 
                    ChangeLog.Source.INSTRUCTOR_EDIT, 
                    (instrId==null || instrId.trim().length()<=0?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    null, 
                    inst.getDepartment());

            tx.commit();			
			hibSession.clear();
			hibSession.refresh(inst);
			if (d!=null)
				hibSession.refresh(d);
			
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
