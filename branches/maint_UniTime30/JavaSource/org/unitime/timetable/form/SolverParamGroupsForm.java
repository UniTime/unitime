/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.timetable.model.SolverParameterGroup;


/** 
 * @author Tomas Muller
 */
public class SolverParamGroupsForm extends ActionForm {
    private String op;
    private Long uniqueId;
	private String description;
	private String name;
	private int order;

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
}

