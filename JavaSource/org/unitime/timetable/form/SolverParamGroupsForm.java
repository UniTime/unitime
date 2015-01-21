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

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.util.IdValue;


/** 
 * @author Tomas Muller
 */
public class SolverParamGroupsForm extends ActionForm {
	private static final long serialVersionUID = 8095255248431482868L;
	private String op;
    private Long uniqueId;
	private String description;
	private String name;
	private int order;
	private int type;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
        
        if(name==null || name.trim().length()==0)
            errors.add("name", new ActionMessage("errors.required", ""));
        else {
        	if ("Save".equals(op)) {
        		SolverParameterGroup gr = SolverParameterGroup.findByName(name);
        		if (gr!=null)
        			errors.add("name", new ActionMessage("errors.exists", name));
        	}
        }
        
        if(description==null || description.trim().length()==0)
            errors.add("description", new ActionMessage("errors.required", ""));
        
        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
        op = "List";
        uniqueId = null;
        name = "";
        description = "";
        order = -1;
        type = 0;
	}

    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }
    public Long getUniqueId() { return uniqueId; }
    public void setUniqueId(Long uniqueId) { this.uniqueId = uniqueId; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
    public int getOrder() { return this.order; }
    public void setOrder(int order) { this.order = order; }
    public int getType() { return this.type; }
    public void setType(int type) { this.type = type; }
    public Vector getTypes() {
        Vector ret = new Vector(3);
        ret.add(new IdValue(new Long(SolverParameterGroup.sTypeCourse), "Course Timetabling"));
        ret.add(new IdValue(new Long(SolverParameterGroup.sTypeExam), "Examination Timetabling"));
        ret.add(new IdValue(new Long(SolverParameterGroup.sTypeStudent), "Student Sectioning"));
        return ret;
    }
}
