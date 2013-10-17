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

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/** 
 * 
 * @author Tomas Muller
 * 
 */
public class UnassignedForm extends ActionForm {
	private static final long serialVersionUID = -3419420049976489695L;
	private String iOp = null;
	private Long iSubjectArea = null;
	private Collection iSubjectAreas = null;


	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null;
		iSubjectArea = null;
		iSubjectAreas = null;
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }

	public Long getSubjectArea() { return iSubjectArea; }
	public String getSubjectAreaAbbv() { return new SubjectAreaDAO().get(iSubjectArea).getSubjectAreaAbbreviation(); }
	public void setSubjectArea(Long subjectArea) { iSubjectArea = subjectArea; } 
	public Collection getSubjectAreas() { return iSubjectAreas; }
	public void setSubjectAreas(Collection subjectAreas) { iSubjectAreas = subjectAreas; }
}

