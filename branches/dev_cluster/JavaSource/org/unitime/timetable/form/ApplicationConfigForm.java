/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

/** 
 * MyEclipse Struts
 * Creation date: 08-28-2006
 * 
 * XDoclet definition:
 * @struts:form name="applicationConfigForm"
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
}
