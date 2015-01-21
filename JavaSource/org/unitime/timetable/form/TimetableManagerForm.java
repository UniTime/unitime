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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.util.MessageResources;
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
 * @author Heston Fernandes, Stephanie Schluttenhofer, Tomas Muller
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
    private String title;
    private String primaryRole;
    private String email;
    private String role;
    private String dept;
    private String solverGr;
    private List depts;
    private List deptLabels;
    private List roles;
    private List roleRefs;
    private List roleReceiveEmailFlags;
    private List solverGrs;
    private List solverGrLabels;
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

    /** Factory to create dynamic list element for role receive email flags */
    protected DynamicListObjectFactory factoryRoleReceiveEmailFlags = new DynamicListObjectFactory() {
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
        title = "";
        primaryRole = "";
        role="";
        dept="";
        solverGr = "";
        lookupEnabled = null;
        
        depts = DynamicList.getInstance(new ArrayList(), factoryDepts);
        deptLabels = DynamicList.getInstance(new ArrayList(), factoryDeptLabels);
        roles= DynamicList.getInstance(new ArrayList(), factoryRoles);
        roleRefs= DynamicList.getInstance(new ArrayList(), factoryRoleRefs);
        roleReceiveEmailFlags = DynamicList.getInstance(new ArrayList(), factoryRoleReceiveEmailFlags);
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
                        new ActionMessage("errors.required", "External ID"));
                
            /*
            if (User.canIdentify()) {
                User user = User.identify(externalId);
                if (user==null)
                    errors.add("externalId", 
                        new ActionMessage("errors.generic", "Manager '" + externalId + "' cannot be identified"));
            }
            */
            
            if (email==null || email.trim().length()==0)
                errors.add("email", 
                        new ActionMessage("errors.required", "Email Address"));
            
            if (primaryRole==null || primaryRole.trim().length()==0)
                errors.add("primaryRole", 
                        new ActionMessage("errors.required", "Primary Role"));
            
            /*
            if(depts.size()==0) {
                Roles deptRole = Roles.getRole(Roles.DEPT_SCHED_MGR_ROLE);
                if (deptRole!=null && roles.contains(deptRole.getRoleId().toString()))
                    errors.add("depts",
                            new ActionMessage("errors.generic", "At least one department must be assigned for role "+deptRole.getAbbv()));
            }
            */

            if(roles.size()==0)
                errors.add("roles", 
                        new ActionMessage("errors.generic", "At least one role must be assigned"));

            if (externalId!=null && externalId.trim().length()>0) {
            	TimetableManager mgr = TimetableManager.findByExternalId(externalId);
            	if (mgr!=null && !mgr.getUniqueId().toString().equals(getUniqueId()))
                    errors.add("roles", 
                            new ActionMessage("errors.generic", "Duplicate Record - This manager already exists"));
            }
            
            if (!lookupEnabled) {
            	if (firstName == null || firstName.isEmpty()) {
            		errors.add("firstName", new ActionMessage("errors.required", "First Name"));
            	}

            	if (lastName == null || lastName.isEmpty()) {
            		errors.add("lastName", new ActionMessage("errors.required", "Last Name"));
            	}
            }
        }
        
		return errors;
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

	public List getRoleReceiveEmailFlags() {
		return roleReceiveEmailFlags;
	}
	
    public void setRoleReceiveEmailFlags(int key, Object value) {
        this.roleReceiveEmailFlags.set(key, value);
    }

	public void setRoleReceiveEmailFlags(List roleReceiveEmailFlags) {
		this.roleReceiveEmailFlags = roleReceiveEmailFlags;
	}
    
}
