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

import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

/** 
 * MyEclipse Struts
 * Creation date: 10-17-2005
 * 
 * XDoclet definition:
 * @struts:form name="managerSettingsForm"
 *
 * @author Tomas Muller
 */
public class ManagerSettingsForm extends ActionForm {

	private static final long serialVersionUID = -5955499033542263250L;

    // --------------------------------------------------------- Instance Variables

	private String op;

    /** key property */
    private String key;
    
    private String name;

    /** defaultValue property */
    private String value;
    
    private String defaultValue;

    /** allowedValues property */
    private String[] allowedValues;

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
        
        if(value==null || value.trim().length()==0)
            errors.add("value", new ActionMessage("errors.required", ""));
        
        return errors;
    }

    /** 
     * Method reset
     * @param mapping
     * @param request
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = null;
        key = "";
        name = "";
        value = "";
        defaultValue = "";
        allowedValues = null;
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
     * @param defaultValue The value to set
     */
    public void setValue(String value) {
        this.value = value;
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
     * @return Returns the allowedValues.
     */
    public String[] getAllowedValues() {
        return allowedValues;
    }
    /**
     * @param allowedValues The allowedValues to set.
     */
    public void setAllowedValues(String[] allowedValues) {
        this.allowedValues = allowedValues;
    }

    /**
     * @param allowedValues The allowedValues to set.
     */
    public void setAllowedValues(String allowedValues) {
        StringTokenizer strTok = new StringTokenizer(allowedValues, ",");
        this.allowedValues = new String[strTok.countTokens()];
        int i =0;
        
        while(strTok.hasMoreElements()) {
            this.allowedValues[i++] = strTok.nextElement().toString().trim();
        }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
}
