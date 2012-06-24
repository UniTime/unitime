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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.Settings;


/** 
 * MyEclipse Struts
 * Creation date: 10-17-2005
 * 
 * XDoclet definition:
 * @struts:form name="settingsForm"
 */
public class SettingsForm extends ActionForm {
	private static final long serialVersionUID = -7290264236456861985L;

    // --------------------------------------------------------- Instance Variables

	private String op;

    /** uniqueId  property */
    private Long uniqueId;

    /** key property */
    private String key;

    /** defaultValue property */
    private String defaultValue;

    /** allowedValues property */
    private String allowedValues;

    /** description property */
    private String description;

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
        else {
            Settings setting = Settings.getSetting(key);
            if(op.equals("Save") && setting!=null && setting.getDefaultValue().toString().trim().length()>0)
                errors.add("key", new ActionMessage("errors.exists", key));
        }
        
        if(defaultValue==null || defaultValue.trim().length()==0)
            errors.add("defaultValue", new ActionMessage("errors.required", ""));
        
        if(allowedValues==null || allowedValues.trim().length()==0)
            errors.add("allowedValues", new ActionMessage("errors.required", ""));
        
        if(description==null || description.trim().length()==0)
            errors.add("description", new ActionMessage("errors.required", ""));
        
        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = "List";
        uniqueId = null;
        key = "";
        defaultValue = "";
        allowedValues = "";
        description = "";
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
     * Returns the defaultValue.
     * @return String
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /** 
     * Set the defaultValue.
     * @param defaultValue The defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return Returns the op.
     */
    public String getOp() {
        return op;
    }
    /**
     * @param op The op to set.
     */
    public void setOp(String op) {
        this.op = op;
    }
    /**
     * @return Returns the uniqueId.
     */
    public Long getUniqueId() {
        return uniqueId;
    }
    /**
     * @param uniqueId The uniqueId to set.
     */
    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }
    /**
     * @return Returns the allowedValues.
     */
    public String getAllowedValues() {
        return allowedValues;
    }
    /**
     * @param allowedValues The allowedValues to set.
     */
    public void setAllowedValues(String allowedValues) {
        this.allowedValues = allowedValues;
    }
    
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
