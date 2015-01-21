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

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

/** 
 * MyEclipse Struts
 * Creation date: 08-28-2006
 * 
 * XDoclet definition:
 * @struts:form name="applicationConfigForm"
 *
 * @author Tomas Muller
 */
public class ApplicationConfigForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables

	private static final long serialVersionUID = 4677371360700536609L;

	private String op;

    /** key property */
    private String key;

    /** value property */
    private String value;

    /** description property */
    private String description;
    
    private boolean allSessions;
    
    private Long[] sessions = null;
    
    private boolean showAll = false;

    // --------------------------------------------------------- Methods

    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    public ActionErrors validate(
        ActionMapping mapping,
        HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();
        
        if(key==null || key.trim().length()==0)
            errors.add("key", new ActionMessage("errors.required", ""));

        if(value==null)
            value = "";
        
        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = "list";
        key = "";
        value = "";
        description = "";
        allSessions = false;
        sessions = null;
        showAll = false; 
    }

    /** 
     * Returns the key.
     * @return String
     */
    public String getKey() {
        return key;
    }

    /** 
     * Set the key.
     * @param key The key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /** 
     * Returns the value.
     * @return String
     */
    public String getValue() {
        return value;
    }

    /** 
     * Set the value.
     * @param value The value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /** 
     * Returns the description.
     * @return String
     */
    public String getDescription() {
        return description;
    }

    /** 
     * Set the description.
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getOp() {
        return op;
    }
    
    public void setOp(String op) {
        this.op = op;
    }
    
    public boolean isAllSessions() {
    	return allSessions;
    }
    
    public void setAllSessions(boolean allSessions) {
    	this.allSessions = allSessions;
    }
    
    public Set<Session> getListSessions() {
    	return new TreeSet<Session>(SessionDAO.getInstance().findAll());
    }
    
	public Long[] getSessions() {
		return sessions;
	}

	public void setSessions(Long[] sessions) {
		this.sessions = sessions;
	}
	
	public String getType() {
		ApplicationProperty p = ApplicationProperty.fromKey(key);
		if (p == null) return null;
		Class type = p.type();
		if (type == null || type.equals(String.class)) return null; 
		if (type.equals(Class.class) && p.implementation() != null) {
			if (p.implementation().isInterface())
				return "class implementing " + p.implementation().getSimpleName();
			else
				return "class extending " + p.implementation().getSimpleName();
		}
		return type.getSimpleName().toLowerCase();
	}
	
	public String getValues() {
		ApplicationProperty p = ApplicationProperty.fromKey(key);
		if (p == null) return null;
		String[] vals = p.availableValues();
		if (vals != null && vals.length > 0) {
			String ret = "";
			for (int i = 0; i < vals.length; i++) {
				if (i > 0) ret += ", ";
				ret += vals[i];
			}
			return ret;
		}
		return null;
	}
	
	public String getDefault() {
		ApplicationProperty p = ApplicationProperty.fromKey(key);
		return (p != null ? p.defaultValue() : null);
	}
	
	public boolean getShowAll() { return showAll; }
	public void setShowAll(boolean showAll) { this.showAll = showAll; }
}
