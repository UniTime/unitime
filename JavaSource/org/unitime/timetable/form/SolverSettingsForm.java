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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.util.ComboBoxLookup;


/** 
 * @author Tomas Muller
 */
public class SolverSettingsForm implements UniTimeForm {
	private static final long serialVersionUID = -9205033432561871308L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	private String op;
	private Long uniqueId;
	private String name;
	private String description;
	private String appearance;
	private Map<Long, String> params;
	private Map<Long, Boolean> useDefaults;
	
	public SolverSettingsForm() {
		reset();
	}

	public void validate(UniTimeAction action) {
        if (name==null || name.isEmpty()) {
        	action.addFieldError("form.name", MSG.errorRequiredField(MSG.fieldReference()));
        } else {
        	SolverPredefinedSetting set = SolverPredefinedSetting.findByName(name);
        	if (uniqueId != null) { // update
        		if (set != null && !set.getUniqueId().equals(uniqueId))
        			action.addFieldError("form.name", MSG.errorAlreadyExists(name));
        	} else { // save
        		if (set!=null)
        			action.addFieldError("form.name", MSG.errorAlreadyExists(name));
        	}
        }
        
        if (description==null || description.isEmpty())
        	action.addFieldError("form.description", MSG.errorRequiredField(MSG.fieldName()));
        
        if(appearance==null || appearance.isEmpty())
        	action.addFieldError("form.appearance", MSG.errorRequiredField(MSG.fieldAppearance()));

        for (Map.Entry<Long, String> entry: params.entrySet()) {
            Long parm = entry.getKey();
        	String val = entry.getValue();
        	Boolean useDefault = useDefaults.get(parm);
        	if (!useDefault.booleanValue() && (val==null || val.isEmpty()))
        		action.addFieldError("form.parameter["+parm+"]", MSG.errorRequiredField(SolverParameterDefDAO.getInstance().get(parm).getDescription()));
        }
	}

	public void reset() {
		name=""; description="";
		op=null; uniqueId=null;
		appearance = SolverPredefinedSetting.Appearance.SOLVER.name();
		params = new HashMap<Long, String>();
		useDefaults = new HashMap<Long, Boolean>();
	}

	public void loadDefaults() {
		params.clear(); useDefaults.clear();
		for (SolverParameterDef def: SolverParameterDefDAO.getInstance().findAll()) {
			if (!def.isVisible().booleanValue()) continue;
			params.put(def.getUniqueId(), def.getDefault());
			useDefaults.put(def.getUniqueId(), Boolean.TRUE);
		}
	}

    public void loadDefaults(HttpServletRequest request) {
    	params.clear(); useDefaults.clear();
    	for (SolverParameterDef def: SolverParameterDefDAO.getInstance().findAll()) {
			if (!def.isVisible().booleanValue()) continue;
            params.put(def.getUniqueId(), (request.getParameter("parameter["+def.getUniqueId()+"]")==null?def.getDefault():request.getParameter("parameter["+def.getUniqueId()+"]")));
            useDefaults.put(def.getUniqueId(), (request.getParameter("useDefault["+def.getUniqueId()+"]")==null || "false".equals(request.getParameter("useDefault["+def.getUniqueId()+"]"))?Boolean.FALSE:Boolean.TRUE));
        }
    }

	public String getOp() { return op; }
	public void setOp(String op) { this.op = op;}
	public Long getUniqueId() { return uniqueId;}
	public void setUniqueId(Long uniqueId) { this.uniqueId = uniqueId;}
	public String getDescription() { return description;}
	public void setDescription(String description) { this.description = description;}
	public String getAppearance() { return appearance;}
	public void setAppearance(String appearance) { this.appearance = appearance;}
	public SolverPredefinedSetting.Appearance getAppearanceType() {
		for (SolverPredefinedSetting.Appearance a: SolverPredefinedSetting.Appearance.values())
			if (a.name().equals(appearance)) return a;
		return null;
	}
	public void setAppearanceType(SolverPredefinedSetting.Appearance appearance) {
		if (appearance == null) this.appearance="";
		this.appearance = appearance.name();
	}
	public String getName() { return name;}
	public void setName(String name) { this.name = name;}
	public Boolean getUseDefault(Long id) { return useDefaults.get(id); }
	public void setUseDefault(Long id, Boolean useDefault) { useDefaults.put(id,useDefault); }

	public String getParameter(Long id) { return params.get(id); }
	public void setParameter(Long id, String value) { params.put(id, value); }
	public List<ComboBoxLookup> getAppearances() {
		List<ComboBoxLookup> ret = new ArrayList<ComboBoxLookup>();
		for (SolverPredefinedSetting.Appearance a: SolverPredefinedSetting.Appearance.values())
			ret.add(new ComboBoxLookup(a.getLabel(), a.name()));
		return ret;
	}
	public Collection getEnum(String type) {
		Vector options = new Vector();
		StringTokenizer stk = new StringTokenizer(type,",");
		while (stk.hasMoreTokens()) options.add(stk.nextToken());
		return options;
	}
}

