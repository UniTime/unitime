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
package org.unitime.timetable.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.ReferenceList;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class DepartmentEditForm extends ActionForm {
	private static final long serialVersionUID = -6614766002463228171L;
	public Long iId = null;
	public Long iSessionId = null;
	public String iName = null;
	public String iDeptCode = null;
	public String iStatusType = null;
	public String iOp = null;
	public String iAbbv = null;
	public String iExternalId = null;
	public int iDistPrefPriority = 0;
	public boolean iIsExternal = false;
	public String iExtAbbv = null;
	public String iExtName = null;
    public boolean iAllowReqTime = false;
    public boolean iAllowReqRoom = false;
    public boolean iAllowReqDist = false;
    public boolean iAllowEvents = false;
    
	
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		
        if(iName==null || iName.trim().equalsIgnoreCase("")) {
        	errors.add("name", new ActionMessage("errors.required", "Name") );
        }
        if(iName!=null && iName.trim().length() > 100) {
        	errors.add("name", new ActionMessage("errors.maxlength", "Name", "100") );
        }
        
        if (iAbbv==null || iAbbv.trim().equalsIgnoreCase("")) {
        	errors.add("abbv", new ActionMessage("errors.required", "Abbreviation") );
        }
        
        if (iAbbv!=null && iAbbv.trim().length() > 20) {
        	errors.add("abbv", new ActionMessage("errors.maxlength", "Abbreviation", "20") );
        }

        if (iDeptCode==null || iDeptCode.trim().equalsIgnoreCase("")) {
        	errors.add("deptCode", new ActionMessage("errors.required", "Code") );
        }
        
        if (iDeptCode!=null && iDeptCode.trim().length() > 50) {
        	errors.add("deptCode", new ActionMessage("errors.maxlength", "Code", "50") );
        }

        if (iIsExternal && (iExtName==null || iExtName.trim().length()==0)) {
        	errors.add("extName", new ActionMessage("errors.required", "External Manager Name") );
        }
        
        if (!iIsExternal && iExtName!=null && iExtName.trim().length() > 0){ 	
        	errors.add("extName", new ActionMessage("errors.generic", "External Manager Name should only be used when the department is marked as 'External Manager'") );
        }
        
        if (iIsExternal && (iExtName!=null && iExtName.trim().length() > 30)) {
        	errors.add("extName", new ActionMessage("errors.maxlength", "External Manager Name", "30") );
        }

        if (iIsExternal && (iExtAbbv==null || iExtAbbv.trim().length()==0)) {
        	errors.add("extAbbv", new ActionMessage("errors.required", "External Manager Abbreviation") );
        }
        
        if (!iIsExternal && iExtAbbv!=null && iExtAbbv.trim().length() > 0){
        	errors.add("extName", new ActionMessage("errors.generic", "External Manager Abbreviation should only be used when the department is marked as 'External Manager'") );      	
        }
        
        if (iIsExternal && (iExtAbbv!=null && iExtAbbv.trim().length() > 10)) {
        	errors.add("extAbbv", new ActionMessage("errors.maxlength", "External Manager Abbreviation", "10") );
        }

        try {
			Department dept = Department.findByDeptCode(iDeptCode, iSessionId);
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
		iId = null; iSessionId = null; iName = null; iDeptCode = null; iStatusType = null; iAbbv=null; iDistPrefPriority = 0;
		iIsExternal = false; iExtName = null; iExtAbbv = null;
        iAllowReqTime = false; iAllowReqRoom = false; iAllowReqDist = false; iAllowEvents = false;
	}

	public Long getId() { return iId; }
	public void setId(Long id) { iId = id; }

	public Long getSessionId() { return iSessionId; }
	public void setSessionId(Long sessionId) { iSessionId = sessionId; }

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

    public boolean getIsExternal() { return iIsExternal; }
	public void setIsExternal(boolean isExternal) { iIsExternal = isExternal; }
    public boolean getAllowReqTime() { return iAllowReqTime; }
    public void setAllowReqTime(boolean allowReqTime) { iAllowReqTime = allowReqTime; }
    public boolean getAllowReqRoom() { return iAllowReqRoom; }
    public void setAllowReqRoom(boolean allowReqRoom) { iAllowReqRoom = allowReqRoom; }
    public boolean getAllowReqDist() { return iAllowReqDist; }
    public void setAllowReqDist(boolean allowReqDist) { iAllowReqDist = allowReqDist; }
    public boolean getAllowEvents() { return iAllowEvents; }
    public void setAllowEvents(boolean allowEvents) { iAllowEvents = allowEvents; }
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
		setSessionId(department.getSessionId());
		setName(department.getName());
		setAbbv(department.getAbbreviation());
		setDistPrefPriority(department.getDistributionPrefPriority()==null?0:department.getDistributionPrefPriority().intValue());
		setDeptCode(department.getDeptCode());
		setStatusType(department.getStatusType()==null?null:department.getStatusType().getReference());
		setExternalId(department.getExternalUniqueId());
		setIsExternal(department.isExternalManager().booleanValue());
		setExtAbbv(department.getExternalMgrAbbv());
		setExtName(department.getExternalMgrLabel());
        setAllowReqRoom(department.isAllowReqRoom()!=null && department.isAllowReqRoom().booleanValue());
        setAllowReqTime(department.isAllowReqTime()!=null && department.isAllowReqTime().booleanValue());
        setAllowReqDist(department.isAllowReqDistribution()!=null && department.isAllowReqDistribution().booleanValue());
        setAllowEvents(department.isAllowEvents());
	}

	public void save(SessionContext context) throws Exception {
		DepartmentDAO dao = new DepartmentDAO();
		org.hibernate.Session session = dao.getSession();
		Department department;
		Session acadSession = null;
		
		if( getId().equals(new Long(0))) {
			department = new Department();
			acadSession = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId()); 
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
            department.setAllowReqDistribution(new Boolean(getAllowReqDist()));
            department.setAllowEvents(getAllowEvents());

			dao.saveOrUpdate(department);
//			if( acadSession != null) {
//				session.saveOrUpdate(acadSession);
//			}
            ChangeLog.addChange(
                    session, 
                    context, 
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
