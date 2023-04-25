/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.action;

import java.util.HashSet;
import java.util.List;

import org.hibernate.query.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InstructorEditForm;
import org.unitime.timetable.interfaces.ExternalUidLookup;
import org.unitime.timetable.interfaces.ExternalUidLookup.UserInfo;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.StaffDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.util.NameFormat;


/**
 * Methods common to Instructor Add and Edit
 *
 * @author Tomas Muller, Zuzana Mullerova, Heston Fernandes, Stephanie Schluttenhofer
 */
public class InstructorAction extends UniTimeAction<InstructorEditForm> {
	private static final long serialVersionUID = -3849156971109264456L;

	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	
	public String execute() throws Exception {
		if (form == null)
			form = new InstructorEditForm();

		LookupTables.setupPositionTypes(request);

		form.setNameFormat(NameFormat.fromReference(sessionContext.getUser().getProperty(UserProperty.NameFormat)));
		
		form.setLookupEnabled(ApplicationProperty.InstructorExternalIdLookup.isTrue() && ApplicationProperty.InstructorExternalIdLookupClass.value() != null);
		
		if (op == null) op = form.getOp();
		else form.setOp(op);

		return null;
	}
	
    protected void fillStaffInfo() throws Exception {
        Staff staff = StaffDAO.getInstance().get(Long.valueOf(form.getSearchSelect()));
        form.setPuId(staff.getExternalUniqueId());
        form.setFname(staff.getFirstName()!=null ? staff.getFirstName().trim() : "");
        form.setMname(staff.getMiddleName()!=null ? staff.getMiddleName().trim() : "");
        form.setLname(staff.getLastName()!=null ? staff.getLastName().trim() : "");
        form.setTitle(staff.getAcademicTitle() != null ? staff.getAcademicTitle().trim() : "");
        form.setEmail(staff.getEmail());
        if (staff.getPositionType() != null &&  (form.getPosType() == null || form.getPosType().trim().length() == 0))
        	form.setPosType(staff.getPositionType().getUniqueId().toString());
    }

    /**
     * Fills form with selected i2a2 info
     */
    protected void fillI2A2Info() throws Exception {
	    String login = form.getCareerAcct();
	    if (login!=null && login.trim().length()>0 && form.getLookupEnabled()) {
	    	UserInfo results = lookupInstructor();
	    	if (results != null) {
				form.setPuId(results.getExternalId());
				form.setCareerAcct(results.getUserName());
				form.setFname(results.getFirstName());
				form.setMname(results.getMiddleName());
				form.setLname(results.getLastName());
	    		form.setEmail(results.getEmail());
	    		form.setTitle(results.getAcademicTitle());
	    	}
	    }	    
    }

    /**
	 * Searches STAFF for matches on name / career account
	 * Searches I2A2 for matching career account
	 */
    protected void findMatchingInstructor() throws Exception {
	    
	    form.setMatchFound(Boolean.valueOf(false));
	    String fname = form.getFname();
	    String lname = form.getLname();
	    String login = form.getCareerAcct();

	    // Check I2A2
	    if (login!=null && login.trim().length()>0 && form.getLookupEnabled()) {
	    	UserInfo results = lookupInstructor();
	    	if (results!=null) {
				form.setI2a2Match(results);
	    		form.setMatchFound(Boolean.TRUE);
	    	}
	    }
	    
	    // Check Staff
	    List staffList = Staff.findMatchingName(fname, lname);
	    form.setStaffMatch(staffList);

	    if (staffList!=null && staffList.size()>0)
            form.setMatchFound(Boolean.valueOf(true));
	}

    /**
     * Lookup instructor details 
     */
    private UserInfo lookupInstructor() throws Exception {
        String id = form.getCareerAcct();
        if (id!=null && id.trim().length()>0 && form.getLookupEnabled().booleanValue()) {
        	String className = ApplicationProperty.InstructorExternalIdLookupClass.value(); 
        	ExternalUidLookup lookup = (ExternalUidLookup) (Class.forName(className).getDeclaredConstructor().newInstance());
       		return lookup.doLookup(id);
        }
        
	    return null;
    }

    
	/**
	 * Inserts / Updates Instructor Info
	 */
	protected void doUpdate() throws Exception {
	    
		DepartmentalInstructorDAO idao = DepartmentalInstructorDAO.getInstance();
		org.hibernate.Session hibSession = idao.getSession();
		Transaction tx = null;
		
		try {	
			tx = hibSession.beginTransaction();
			
			DepartmentalInstructor inst = null;
			String instrId = form.getInstructorId();
			
			if (instrId!=null && instrId.trim().length()>0) {
			    inst = DepartmentalInstructorDAO.getInstance().get(Long.valueOf(instrId));
			}
			else {    
			    inst = new DepartmentalInstructor();
			    inst.setAttributes(new HashSet<InstructorAttribute>());
			}

			if (form.getFname() != null && form.getFname().trim().length()>0) {
				inst.setFirstName(form.getFname().trim());
			} else {
				inst.setFirstName(null);
			}
			
			if (form.getMname() != null && form.getMname().trim().length()>0) {
				inst.setMiddleName(form.getMname().trim());
			} else {
				inst.setMiddleName(null);
			}
			
			inst.setLastName(form.getLname().trim());
			
			if (form.getTitle() != null && form.getTitle().trim().length()>0) {
				inst.setAcademicTitle(form.getTitle().trim());
			} else {
				inst.setAcademicTitle(null);
			}
			
			if (form.getPuId() != null && form.getPuId().trim().length()>0 && !form.getPuId().equalsIgnoreCase("null")) {
				inst.setExternalUniqueId(form.getPuId().trim());
			} else {
				inst.setExternalUniqueId(null);
			}

			if (form.getCareerAcct() != null && form.getCareerAcct().trim().length()>0) {
				inst.setCareerAcct(form.getCareerAcct().trim());
			} else {
				inst.setCareerAcct(null);
			}
			
			inst.setEmail(form.getEmail());
						
			if (form.getPosType() != null && form.getPosType().trim().length()>0) {
				PositionType pt = PositionType.findById(Long.valueOf(form.getPosType().trim()));
				if (pt != null) {
					inst.setPositionType(pt);
				}
			}
			
			if (form.getNote() != null && !form.getNote().isEmpty()) {
				if (form.getNote().length() > 2048)
					inst.setNote(form.getNote().substring(0, 2048));
				else
					inst.setNote(form.getNote());
			} else 
				inst.setNote(null);
			
			Department d = null;
			//get department
			if (sessionContext.getAttribute(SessionAttribute.DepartmentId) != null) {
				String deptId = (String) sessionContext.getAttribute(SessionAttribute.DepartmentId);
				d = DepartmentDAO.getInstance().get(Long.valueOf(deptId));
				inst.setDepartment(d);
				d.getInstructors().add(inst);
			}
			else
			    throw new Exception("Department Id could not be retrieved from session");
            
            inst.setIgnoreToFar(Boolean.valueOf(form.getIgnoreDist()));
            
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
     */
    protected boolean isDeptInstructorUnique() {
        
        String query = "from DepartmentalInstructor " +
        				"where externalUniqueId=:puid and department.uniqueId=:deptId";
        if (form.getInstructorId()!=null && form.getInstructorId().trim().length()>0) {
            query += " and uniqueId!=:uniqueId";
        }
        
        DepartmentalInstructorDAO ddao = DepartmentalInstructorDAO.getInstance();
        org.hibernate.Session hibSession = ddao.getSession();
        
		String deptId = (String) request.getSession().getAttribute(Constants.DEPT_ID_ATTR_NAME);
       
        Query q = hibSession.createQuery(query);
        q.setParameter("puid", form.getPuId().trim());
        q.setParameter("deptId", Long.parseLong(deptId));
        if (form.getInstructorId()!=null && form.getInstructorId().trim().length()>0) {
            q.setParameter("uniqueId", Long.parseLong(form.getInstructorId().trim()));
        }
        
        return (q.list().size()==0);
    }
}
