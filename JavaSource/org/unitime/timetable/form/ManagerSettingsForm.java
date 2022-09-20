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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;

/** 
 * @author Tomas Muller
 */
public class ManagerSettingsForm implements UniTimeForm {
	private static final long serialVersionUID = -5955499033542263250L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

    // --------------------------------------------------------- Instance Variables

	private String op;

    /** key property */
    private String key;
    
    private String name;

    /** defaultValue property */
    private String value;
    
    private String defaultValue;

    /** allowedValues property */
    private List<String> allowedValues;
    private Map<String, String> labels;

    // --------------------------------------------------------- Methods

    @Override
    public void validate(UniTimeAction action) {
    	if (value == null || value.isEmpty())
    		action.addFieldError("form.value", MSG.errorRequiredField(MSG.columnManagerSettingValue()));
    }

    @Override
    public void reset() {
        op = null;
        key = "";
        name = "";
        value = "";
        defaultValue = "";
        allowedValues = null;
        labels = null;
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
    public List<String> getAllowedValues() {
        return allowedValues;
    }
    /**
     * @param allowedValues The allowedValues to set.
     */
    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    /**
     * @param allowedValues The allowedValues to set.
     */
    public void setAllowedValues(String allowedValues) {
        this.allowedValues = new ArrayList<String>();
        labels = new HashMap<String, String>();
        for (StringTokenizer strTok = new StringTokenizer(allowedValues, ","); strTok.hasMoreTokens(); ) {
        	String value = strTok.nextToken().trim();
        	if (value.indexOf(':') >= 0) {
        		labels.put(value.substring(0, value.indexOf(':')), value.substring(value.indexOf(':') + 1));
        		value = value.substring(0, value.indexOf(':'));
        	}
        	this.allowedValues.add(value);
        }
    }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    
    public String getLabel(String value) {
    	if (labels == null) return value;
    	String label = labels.get(value);
    	if (label != null && !label.isEmpty()) return label;
    	return value;
    }
}
