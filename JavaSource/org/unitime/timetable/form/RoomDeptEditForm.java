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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;


/** 
 * MyEclipse Struts
 * Creation date: 05-05-2006
 * 
 * XDoclet definition:
 * @struts.form name="roomDeptEditForm"
 */
public class RoomDeptEditForm extends ActionForm {

    private String iOp = null;
    private String iTable = null;
    private String iName = null;
	private Long iId = null;
	private Long iExamType = null;
	private Set<Long> iAssigned = new HashSet<Long>();

	private static final long serialVersionUID = 5225503750129395914L;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		return errors;
	}
	
	public void reset(ActionMapping mapping, HttpServletRequest request) {
	    iOp = null; iId = null; iExamType = null; iAssigned.clear();
	}

	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
	public Long getId() { return iId; }
	public void setId(Long id) { iId = id;}
	public Long getExamType() { return iExamType; }
	public void setExamType(Long examType) { iExamType = examType; }
	public Set<Long> getAssignedSet() { return iAssigned; }
	public String[] getAssigned() { 
	    String[] ret = new String[iAssigned.size()];
	    int idx = 0;
	    for (Iterator i=iAssigned.iterator();i.hasNext();) {
	        ret[idx]=i.next().toString();
	    }
	    return ret;
	}
	public void setAssigned(String[] assigned) {
	    iAssigned.clear();
	    for (int i=0;i<assigned.length;i++)
	        iAssigned.add(Long.valueOf(assigned[i]));
	}
	public void setTable(String table) { iTable = table; }
	public String getTable() { return iTable; }
	public void setName(String name) { iName = name; }
	public String getName() { return iName; }
}

