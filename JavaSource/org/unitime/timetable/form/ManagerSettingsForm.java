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
