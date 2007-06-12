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
package org.unitime.timetable.form;

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Session;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.util.ReferenceList;


public class DepartmentEditForm extends ActionForm {
	public Long iId = null;
	public String iName = null;
	public String iDeptCode = null;
	public String iStatusType = null;
	public String iOp = null;
	public String iAbbv = null;
	public String iExternalId = null;
	public Boolean canDelete = Boolean.TRUE;
	public int iDistPrefPriority = 0;
	public boolean iIsExternal = false;
	public String iExtAbbv = null;
	public String iExtName = null;
    public boolean iAllowReqTime = false;
    public boolean iAllowReqRoom = false;
    
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		
        if(iName==null || iName.equalsIgnoreCase("")) {
        	errors.add("name", new ActionMessage("errors.required", "Name") );
        }

        
        if (iAbbv==null || iAbbv.equalsIgnoreCase("")) {
        	errors.add("abbv", new ActionMessage("errors.required", "Abbreviation") );
        }

        if (iDeptCode==null || iDeptCode.equalsIgnoreCase("")) {
        	errors.add("deptCode", new ActionMessage("errors.required", "Number") );
        }
        
        if (iIsExternal && (iExtName==null || iExtName.trim().length()==0)) {
        	errors.add("extName", new ActionMessage("errors.required", "External Manager Name") );
        }

        if (iIsExternal && (iExtAbbv==null || iExtAbbv.trim().length()==0)) {
        	errors.add("extAbbv", new ActionMessage("errors.required", "External Manager Abbreviation") );
        }

        try {
			Department dept = Department.findByDeptCode(iDeptCode, org.unitime.timetable.model.Session.getCurrentAcadSession(Web.getUser(request.getSession())).getSessionId());
			if (dept!=null && !dept.getUniqueId().equals(iId)) {
				errors.add("deptCode", new ActionMessage("errors.exists", iDeptCode));
			}
			
		} catch (Exception e) {
			Debug.error(e);
			errors.add("deptCode", new ActionMessage("errors.generic", e.getMessage()));
		}
		

		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		setCanDelete(Boolean.TRUE);
		iId = null; iName = null; iDeptCode = null; iStatusType = null; iAbbv=null; iDistPrefPriority = 0;
		iIsExternal = false; iExtName = null; iExtAbbv = null;
        iAllowReqTime = false; iAllowReqRoom = false;
	}

	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }

	public String getExternalId() {
		return iExternalId;
	}

	public void setExternalId(String externalId) {
		iExternalId = externalId;
	}

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }
	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }
	public int getDistPrefPriority() { return iDistPrefPriority; }
	public void setDistPrefPriority(int distPrefPriority) { iDistPrefPriority = distPrefPriority; }
	public String getDeptCode() { return iDeptCode; }
	public void setDeptCode(String deptCode) { iDeptCode = deptCode; }
	public String getStatusType() { return iStatusType; }
	public void setStatusType(String statusType) { iStatusType = statusType; }
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

	public Boolean getCanDelete() {
		return canDelete;
	}

	public void setCanDelete(Boolean canDelete) {
		this.canDelete = canDelete;
	}

	public boolean getIsExternal() { return iIsExternal; }
	public void setIsExternal(boolean isExternal) { iIsExternal = isExternal; }
    public boolean getAllowReqTime() { return iAllowReqTime; }
    public void setAllowReqTime(boolean allowReqTime) { iAllowReqTime = allowReqTime; }
    public boolean getAllowReqRoom() { return iAllowReqRoom; }
    public void setAllowReqRoom(boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }
	public String getExtAbbv() { return iExtAbbv; }
	public void setExtAbbv(String extAbbv) { iExtAbbv = extAbbv; }
	public String getExtName() { return iExtName; }
	public void setExtName(String extName) { iExtName = extName; }
	
	public ReferenceList getStatusOptions() { 
		ReferenceList ref = new ReferenceList();
		ref.addAll(DepartmentStatusType.findAllForDepartment());
		return ref;
	}
	
	public void load(Department department) {
		setId(department.getUniqueId());
		setName(department.getName());
		setAbbv(department.getAbbreviation());
		setDistPrefPriority(department.getDistributionPrefPriority()==null?0:department.getDistributionPrefPriority().intValue());
		setDeptCode(department.getDeptCode());
		setStatusType(department.getStatusType()==null?null:department.getStatusType().getReference());
		setExternalId(department.getExternalUniqueId());
		setIsExternal(department.isExternalManager().booleanValue());
		setExtAbbv(department.getExternalMgrAbbv());
		setExtName(department.getExternalMgrLabel());
		setCanDelete(Boolean.TRUE);
        setAllowReqRoom(department.isAllowReqRoom()!=null && department.isAllowReqRoom().booleanValue());
        setAllowReqTime(department.isAllowReqTime()!=null && department.isAllowReqTime().booleanValue());
	}

	/**
	 * Test to see if the department can be deleted.
	 * All of its InstructionalOfferings must be not offered.
	 * @param department
	 */
	private boolean deptHasOfferedCourses(Department department) {
		Iterator it = department.getSubjectAreas().iterator();
		while(it.hasNext()) {
			 SubjectArea subject = (SubjectArea) it.next();
			 if(subjectAreaHasOfferedCourses(department, subject)) {
				 return true;
			 }
		}
		return false;
	}

	/**
	 * Test whether any InstructionalOfferings for the SubjectArea are offered. 
	 * @param department
	 * @param subject
	 */
	private boolean subjectAreaHasOfferedCourses(Department department, SubjectArea subject) {
		Iterator it = subject.getCourseOfferings().iterator();
		while(it.hasNext()) {
			CourseOffering courseOffer = (CourseOffering) it.next(); 
			InstructionalOffering instrOffer = courseOffer.getInstructionalOffering();
			if(instrOffer.getNotOffered().equals(Boolean.FALSE)) {
				return true;
			}
			if(instrOffer.getCourseOfferings().size() > 1) {
				checkCourseOfferings(department, instrOffer);
			}
		}
		return false;
	}

	/**
	 * If the InstructionalOffering has multiple CourseOfferings, check to see
	 *  that all the CourseOfferings' SubjectAreas are owned by the Department
	 *  to be deleted.
	 * @param department
	 * @param instrOffer
	 */
	private void checkCourseOfferings(Department department, InstructionalOffering instrOffer) {
		Iterator it = instrOffer.getCourseOfferings().iterator();
		while(it.hasNext()) {
			CourseOffering courseOffer = (CourseOffering) it.next();
			SubjectArea area = courseOffer.getSubjectArea();
			if(!area.getDepartment().equals(department)) {
				setCanDelete(Boolean.FALSE);
				return;
			}
		}
	}

	public void save(User user, HttpServletRequest request) throws Exception {
		DepartmentDAO dao = new DepartmentDAO();
		Session session = dao.getSession();
		Department department;
		org.unitime.timetable.model.Session acadSession = null;
		
		if( getId().equals(new Long(0))) {
			department = new Department();
			acadSession = org.unitime.timetable.model.Session.getCurrentAcadSession(user); 
			department.setSession(acadSession);
			department.setDistributionPrefPriority(new Integer(0));
			acadSession.addTodepartments(department);
		}
		else {
			department = dao.get(getId(), session);
		}
		if (department!=null) {
			department.setStatusType(getStatusType()==null || getStatusType().length()==0 ? null : DepartmentStatusType.findByRef(getStatusType()));
			department.setName(getName());
			department.setDeptCode(getDeptCode());
			department.setAbbreviation(getAbbv());
			department.setExternalUniqueId(getExternalId());
			department.setDistributionPrefPriority(new Integer(getDistPrefPriority()));
			department.setExternalManager(new Boolean(getIsExternal()));
			department.setExternalMgrLabel(getExtName());
			department.setExternalMgrAbbv(getExtAbbv());
            department.setAllowReqRoom(new Boolean(getAllowReqRoom()));
            department.setAllowReqTime(new Boolean(getAllowReqTime()));
			dao.saveOrUpdate(department);
//			if( acadSession != null) {
//				session.saveOrUpdate(acadSession);
//			}
            ChangeLog.addChange(
                    session, 
                    request, 
                    department, 
                    ChangeLog.Source.DEPARTMENT_EDIT, 
                    (getId().equals(new Long(0))?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    null, 
                    department);

			session.flush();
			if( acadSession != null){
				session.refresh(acadSession);
			}
		}
	}
}
