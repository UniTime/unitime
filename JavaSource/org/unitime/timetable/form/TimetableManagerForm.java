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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.User;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * Represents the Timetable Manager
 * 
 * @author Heston Fernandes
 */
public class TimetableManagerForm extends ActionForm {

	private static final long serialVersionUID = 3763101881496582452L;

    // --------------------------------------------------------- Instance Variables
    
	private String op;
	private String op1;
	private String uniqueId;
    private String externalId;
    private String lookupResult;
    private String firstName;
    private String middleName;
    private String lastName;
    private String primaryRole;
    private String email;
    private String role;
    private String dept;
    private String solverGr;
    private List depts;
    private List deptLabels;
    private List roles;
    private List roleRefs;
    private List solverGrs;
    private List solverGrLabels;
    private Boolean isExternalManager;
    private Boolean lookupEnabled;
    
    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for departments */
    protected DynamicListObjectFactory factoryDepts = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for department labels */
    protected DynamicListObjectFactory factoryDeptLabels = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for roles */
    protected DynamicListObjectFactory factoryRoles = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for role refs */
    protected DynamicListObjectFactory factoryRoleRefs = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for solver groups */
    protected DynamicListObjectFactory factorySolverGrs = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    /** Factory to create dynamic list element for solver group labels */
    protected DynamicListObjectFactory factorySolverGrLabels = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };

    // --------------------------------------------------------- Methods

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op1 = "1";
        op="";
    	uniqueId = "";
        externalId = "";
        firstName = "";
        middleName = "";
        lastName = "";
        primaryRole = "";
        role="";
        dept="";
        solverGr = "";
        isExternalManager=null;
        lookupEnabled = null;
        
        depts = DynamicList.getInstance(new ArrayList(), factoryDepts);
        deptLabels = DynamicList.getInstance(new ArrayList(), factoryDeptLabels);
        roles= DynamicList.getInstance(new ArrayList(), factoryRoles);
        roleRefs= DynamicList.getInstance(new ArrayList(), factoryRoleRefs);
        solverGrs = DynamicList.getInstance(new ArrayList(), factorySolverGrs);
        solverGrLabels = DynamicList.getInstance(new ArrayList(), factorySolverGrLabels);
        
        super.reset(mapping, request);
    }

    public ActionErrors validate(
            ActionMapping mapping,
            HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        // Get Message Resources
        MessageResources rsc = 
            (MessageResources) super.getServlet()
            	.getServletContext().getAttribute(Globals.MESSAGES_KEY);
        
        if (op.equalsIgnoreCase(rsc.getMessage("button.addRole"))) {
            if (role.equals(Constants.BLANK_OPTION_VALUE))
                errors.add("role", 
                        new ActionMessage("errors.invalid", "Role"));
        }        
        
        if (op.equalsIgnoreCase(rsc.getMessage("button.addDepartment"))) {
            if (dept.equals(Constants.BLANK_OPTION_VALUE))
                errors.add("dept", 
                        new ActionMessage("errors.invalid", "Department"));
        }        
        
        if (op.equalsIgnoreCase(rsc.getMessage("button.addSolverGroup"))) {
            if (solverGr.equals(Constants.BLANK_OPTION_VALUE))
                errors.add("solverGr", 
                        new ActionMessage("errors.invalid", "Solver Group"));
        }        

        if (op.equalsIgnoreCase(rsc.getMessage("button.insertTimetableManager"))
                || op.equalsIgnoreCase(rsc.getMessage("button.updateTimetableManager")) ) {
            
            if (externalId==null || externalId.trim().length()==0)
                errors.add("externalId", 
                        new ActionMessage("errors.required", "PuID / Career Account"));
                
            if (User.canIdentify()) {
                User user = User.identify(externalId);
                if (user==null)
                    errors.add("externalId", 
                        new ActionMessage("errors.generic", "Manager '" + externalId + "' cannot be identified"));
            }
            
            if (email==null || email.trim().length()==0)
                errors.add("email", 
                        new ActionMessage("errors.required", "Email Address"));
            
            if (primaryRole==null || primaryRole.trim().length()==0)
                errors.add("primaryRole", 
                        new ActionMessage("errors.required", "Primary Role"));
            
            if(depts.size()==0)
                errors.add("depts", 
                        new ActionMessage("errors.generic", "At least one department must be assigned"));

            if(roles.size()==0)
                errors.add("roles", 
                        new ActionMessage("errors.generic", "At least one role must be assigned"));

            if (op.equalsIgnoreCase(rsc.getMessage("button.insertTimetableManager"))
            		&& externalId!=null && externalId.trim().length()>0) {
            	TimetableManager mgr = TimetableManager.findByExternalId(externalId);
            	if (mgr!=null)
                    errors.add("roles", 
                            new ActionMessage("errors.generic", "Duplicate Record - This manager already exists"));
            }
        }
        
		return errors;
    }

    public void addToRoles (Roles role) {
        roles.add(role.getRoleId());
        roleRefs.add(role.getReference());
    }
    
    public void removeFromRoles (int index) {
        roles.remove(index);
        roleRefs.remove(index);
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
    
    public String getDept() {
        return dept;
    }
    public void setDept(String dept) {
        this.dept = dept;
    }
    
    public String getSolverGr() {
        return solverGr;
    }
    public void setSolverGr(String solverGr) {
        this.solverGr = solverGr;
    }

    public List getDepts() {
        return depts;
    }
    public String getDepts(int key) {
        return depts.get(key).toString();
    }
    public void setDepts(int key, Object value) {
        this.depts.set(key, value);
    }
    public void setDepts(List depts) {
        this.depts = depts;
    }
    
    public List getDeptLabels() {
        return deptLabels;
    }
    public String getDeptLabels(int key) {
        return deptLabels.get(key).toString();
    }
    public void setDeptLabels(int key, Object value) {
        this.deptLabels.set(key, value);
    }
    public void setDeptLabels(List deptLabels) {
        this.deptLabels = deptLabels;
    }
    
    public List getSolverGrs() {
        return solverGrs;
    }
    public String getSolverGrs(int key) {
        return solverGrs.get(key).toString();
    }
    public void setSolverGrs(int key, Object value) {
        this.solverGrs.set(key, value);
    }
    public void setSolverGrs(List solverGrs) {
        this.solverGrs = solverGrs;
    }

    public List getSolverGrLabels() {
        return solverGrLabels;
    }
    public String getSolverGrLabels(int key) {
        return solverGrLabels.get(key).toString();
    }
    public void setSolverGrLabels(int key, Object value) {
        this.solverGrLabels.set(key, value);
    }
    public void setSolverGrLabels(List solverGrLabels) {
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
    
    public Boolean getIsExternalManager() {
        return isExternalManager;
    }
    public void setIsExternalManager(Boolean isExternalManager) {
        this.isExternalManager = isExternalManager;
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
    
    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
    
    public String getOp1() {
        return op1;
    }
    public void setOp1(String op1) {
        this.op1 = op1;
    }
    
    public String getPrimaryRole() {
        return primaryRole;
    }
    public void setPrimaryRole(String primaryRole) {
        this.primaryRole = primaryRole;
    }
    
    public String getExternalId() {
        return externalId;
    }
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }
    
    public List getRoles() {
        return roles;
    }
    public String getRoles(int key) {
        return roles.get(key).toString();
    }
    public void setRoles(int key, Object value) {
        this.roles.set(key, value);
    }
    public void setRoles(List roles) {
        this.roles = roles;
    }
    
    public List getRoleRefs() {
        return roleRefs;
    }
    public String getRoleRefs(int key) {
        return roleRefs.get(key).toString();
    }
    public void setRoleRefs(int key, Object value) {
        this.roleRefs.set(key, value);
    }
    public void setRoleRefs(List roleRefs) {
        this.roleRefs = roleRefs;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getUniqueId() {
        return uniqueId;
    }
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

	public Boolean getLookupEnabled() {
		return lookupEnabled;
	}

	public void setLookupEnabled(Boolean lookupEnabled) {
		this.lookupEnabled = lookupEnabled;
	}
    
}
