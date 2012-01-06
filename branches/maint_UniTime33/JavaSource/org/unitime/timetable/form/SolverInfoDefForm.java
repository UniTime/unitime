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

