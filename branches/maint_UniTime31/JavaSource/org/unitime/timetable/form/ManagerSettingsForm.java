/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
 */
public class ManagerSettingsForm extends ActionForm {

    // --------------------------------------------------------- Instance Variables

    private String op;

    /** keyId  property */
    private Long keyId;

    /** settingId  property */
    private Long settingId;

    /** key property */
    private String key;

    /** defaultValue property */
    private String value;

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
        keyId = null;
        key = "";
        value = "";
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
     * @return Returns the keyId.
     */
    public Long getKeyId() {
        return keyId;
    }
    /**
     * @param keyId The keyId to set.
     */
    public void setKeyId(Long keyId) {
        this.keyId = keyId;
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
    
    /**
     * @return Returns the settingId.
     */
    public Long getSettingId() {
        return settingId;
    }
    /**
     * @param settingId The settingId to set.
     */
    public void setSettingId(Long settingId) {
        this.settingId = settingId;
    }
}
