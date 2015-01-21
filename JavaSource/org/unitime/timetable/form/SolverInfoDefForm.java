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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.SolverInfoDef;


/** 
 * @author Tomas Muller
 */
public class SolverInfoDefForm extends ActionForm {
	private static final long serialVersionUID = -1859258580363895286L;
	private Long uniqueId;
	private String op;
	private String description;
	private String implementation;
	private String name;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
        
        if(name==null || name.trim().length()==0)
            errors.add("name", new ActionMessage("errors.required", ""));
        else {
        	if ("Add New".equals(op)) {
        		SolverInfoDef info = SolverInfoDef.findByName(name);
        		if (info!=null)
        			errors.add("name", new ActionMessage("errors.exists", name));
        	}
        }
        
        if(description==null || description.trim().length()==0)
            errors.add("description", new ActionMessage("errors.required", ""));
        
        if(implementation==null || implementation.trim().length()==0)
            errors.add("implementation", new ActionMessage("errors.required", ""));

        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = null;
        uniqueId = null;
        name = "";
        description = "";
        implementation = "";
	}
	
	public String getOp() { return op; }
	public void setOp(String op) { this.op = op; }
	public Long getUniqueId() { return uniqueId; }
	public void setUniqueId(Long uniqueId) { this.uniqueId = uniqueId; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getImplementation() { return implementation; }
	public void setImplementation(String implementation) { this.implementation = implementation; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}

