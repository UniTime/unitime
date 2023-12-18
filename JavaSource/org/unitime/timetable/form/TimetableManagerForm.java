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
package org.unitime.timetable.form;

import java.util.ArrayList;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.IdValue;


/**
 * Represents the Timetable Manager
 * 
 * @author Heston Fernandes, Stephanie Schluttenhofer, Tomas Muller
 */
public class TimetableManagerForm implements UniTimeForm {
	private static final long serialVersionUID = 3763101881496582452L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
    
	private String op;
	private Long uniqueId;
    private String externalId;
    private String lookupResult;
    private String firstName;
    private String middleName;
    private String lastName;
    private String title;
    private Long primaryRole;
    private String email;
    private Long role;
    private Long dept;
    private Long solverGr;
    private List<Long> depts;
    private List<String> deptLabels;
    private List<Long> roles;
    private List<String> roleRefs;
    private List<Boolean> roleReceiveEmailFlags;
    private List<Long> solverGrs;
    private List<String> solverGrLabels;
    private Boolean lookupEnabled;
    private List<IdValue> otherSessions;
    private List<Long> updateSessions;
    
    public TimetableManagerForm() {
    	reset();
    }

    @Override
    public void reset() {
        op="";
    	uniqueId = null;
        externalId = "";
        firstName = "";
        middleName = "";
        lastName = "";
        title = "";
        primaryRole = null;
        role=null;
        dept=null;
        solverGr = null;
        lookupEnabled = null;
        depts = new ArrayList<Long>();
        deptLabels = new ArrayList<String>();
        roles= new ArrayList<Long>();
        roleRefs= new ArrayList<String>();
        roleReceiveEmailFlags = new ArrayList<Boolean>();
        solverGrs = new ArrayList<Long>();
        solverGrLabels = new ArrayList<String>();
        updateSessions = new ArrayList<Long>();
    }

    public void validate(UniTimeAction action) {
        if (MSG.actionAddRole().equals(op) && (role == null || role < 0))
        	action.addFieldError("form.role", MSG.errorRequiredField(MSG.fieldRole()));

        if (MSG.actionAddDepartment().equals(op) && (dept == null || dept < 0))
        	action.addFieldError("form.dept", MSG.errorRequiredField(MSG.columnDepartment()));

        if (MSG.actionAddSolverGroup().equals(op) && (solverGr == null || solverGr < 0))
        	action.addFieldError("form.solverGr", MSG.errorRequiredField(MSG.columnSolverGroup()));

        if (MSG.actionSaveManager().equals(op) || MSG.actionUpdateManager().equals(op)) {
        	if (externalId==null || externalId.trim().isEmpty())
        		action.addFieldError("form.externalId", MSG.errorRequiredField(MSG.columnExternalId()));
            
            if (email==null || email.trim().isEmpty())
            	action.addFieldError("form.email", MSG.errorRequiredField(MSG.columnEmailAddress()));
            
            if (primaryRole==null || primaryRole < 0)
            	action.addFieldError("form.primaryRole", MSG.errorRequiredField(MSG.fieldPrimaryRole()));

            if (roles.size()==0)
            	action.addFieldError("form.roles", MSG.errorManagerHasNoRoles());

            if (externalId!=null && externalId.trim().length()>0) {
            	TimetableManager mgr = TimetableManager.findByExternalId(externalId);
            	if (mgr!=null && !mgr.getUniqueId().equals(getUniqueId()))
            		action.addFieldError("form.externalId", MSG.errorManagerDuplicate());
            }
            
            if (!lookupEnabled) {
            	if (firstName == null || firstName.isEmpty()) {
            		action.addFieldError("form.firstName", MSG.errorRequiredField(MSG.fieldFirstName()));
            	}

            	if (lastName == null || lastName.isEmpty()) {
            		action.addFieldError("form.lastName", MSG.errorRequiredField(MSG.fieldLastName()));
            	}
            }
        }
    }

    public void addToRoles (Roles role) {
        roles.add(role.getRoleId());
        roleRefs.add(role.getAbbv());
     }
    
    public void removeFromRoles (int index) {
        roles.remove(index);
        roleRefs.remove(index);
        if (index < roleReceiveEmailFlags.size()){
        	roleReceiveEmailFlags.remove(index);
        }
    }
    
    public void addToDepts (Department dept) {
        depts.add(dept.getUniqueId());
        deptLabels.add(dept.getLabel());
    }
    
    public void removeFromDepts (int index) {
        depts.remove(index);
        deptLabels.remove(index);
    }
    
    public void addToSolverGrs (SolverGroup sg) {
        solverGrs.add(sg.getUniqueId());
        solverGrLabels.add(sg.getName());
    }
    
    public void removeFromSolverGrs (int index) {
    	solverGrs.remove(index);
    	solverGrLabels.remove(index);
    }

    public String getLookupResult() {
        return lookupResult;
    }
    public void setLookupResult(String lookupResult) {
        this.lookupResult = lookupResult;
    }
    
    public Long getDept() {
        return dept;
    }
    public void setDept(Long dept) {
        this.dept = dept;
    }
    
    public Long getSolverGr() {
        return solverGr;
    }
    public void setSolverGr(Long solverGr) {
        this.solverGr = solverGr;
    }

    public List getDepts() {
        return depts;
    }
    public Long getDepts(int key) {
        return depts.get(key);
    }
    public void setDepts(int key, Long value) {
        this.depts.set(key, value);
    }
    public void setDepts(List<Long> depts) {
        this.depts = depts;
    }
    
    public List<String> getDeptLabels() {
        return deptLabels;
    }
    public String getDeptLabels(int key) {
        return deptLabels.get(key);
    }
    public void setDeptLabels(int key, String value) {
        this.deptLabels.set(key, value);
    }
    public void setDeptLabels(List<String> deptLabels) {
        this.deptLabels = deptLabels;
    }
    
    public List<Long> getSolverGrs() {
        return solverGrs;
    }
    public Long getSolverGrs(int key) {
        return solverGrs.get(key);
    }
    public void setSolverGrs(int key, Long value) {
        this.solverGrs.set(key, value);
    }
    public void setSolverGrs(List<Long> solverGrs) {
        this.solverGrs = solverGrs;
    }

    public List<String> getSolverGrLabels() {
        return solverGrLabels;
    }
    public String getSolverGrLabels(int key) {
        return solverGrLabels.get(key);
    }
    public void setSolverGrLabels(int key, String value) {
        this.solverGrLabels.set(key, value);
    }
    public void setSolverGrLabels(List<String> solverGrLabels) {
        this.solverGrLabels = solverGrLabels;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getMiddleName() {
        return middleName;
    }
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    
    public Long getPrimaryRole() {
        return primaryRole;
    }
    public void setPrimaryRole(Long primaryRole) {
        this.primaryRole = primaryRole;
    }
    
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public List<Long> getRoles() {
        return roles;
    }
    public Long getRoles(int key) {
        return roles.get(key);
    }
    public void setRoles(int key, Long value) {
        this.roles.set(key, value);
    }
    public void setRoles(List<Long> roles) {
        this.roles = roles;
    }
    
    public List<String> getRoleRefs() {
        return roleRefs;
    }
    public String getRoleRefs(int key) {
        return roleRefs.get(key);
    }
    public void setRoleRefs(int key, String value) {
        this.roleRefs.set(key, value);
    }
    public void setRoleRefs(List<String> roleRefs) {
        this.roleRefs = roleRefs;
    }

    public Long getRole() {
        return role;
    }
    public void setRole(Long role) {
        this.role = role;
    }
    
    public Long getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

	public Boolean getLookupEnabled() {
		return lookupEnabled;
	}

	public void setLookupEnabled(Boolean lookupEnabled) {
		this.lookupEnabled = lookupEnabled;
	}

	public List<Boolean> getRoleReceiveEmailFlags() {
		return this.roleReceiveEmailFlags;
	}
	
	public Boolean getRoleReceiveEmailFlags(int key) {
		return this.roleReceiveEmailFlags.get(key);
	}
	
    public void setRoleReceiveEmailFlags(int key, Boolean value) {
        this.roleReceiveEmailFlags.set(key, value);
    }

	public void setRoleReceiveEmailFlags(List<Boolean> roleReceiveEmailFlags) {
		this.roleReceiveEmailFlags = roleReceiveEmailFlags;
	}
	
	public List<IdValue> getOtherSessions() {
		return otherSessions;
	}
	public void setOtherSessions(List<IdValue> otherSessions) {
		this.otherSessions = otherSessions;
	}
	
	public List<Long> getUpdateSessions() {
		return updateSessions;
	}
	public void setUpdateSessions(List<Long> updateSessions) {
		this.updateSessions = updateSessions;
	}
}
