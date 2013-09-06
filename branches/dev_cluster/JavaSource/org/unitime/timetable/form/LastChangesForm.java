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

/** 
 * @author Tomas Muller
 */
public class LastChangesForm extends ActionForm {
	private static final long serialVersionUID = 3633681949556250656L;
	private String iOp;
    private int iN;
    private Long iDepartmentId, iSubjAreaId, iManagerId;

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
        
		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iOp = null; 
        iN = 100;
        iDepartmentId = new Long(-1);
        iSubjAreaId = new Long(-1);
        iManagerId = new Long(-1);
	}
	
	public String getOp() { return iOp; }
	public void setOp(String op) { iOp = op; }
    public int getN() { return iN; }
    public void setN(int n) { iN = n; }
    public Long getDepartmentId() { return iDepartmentId; }
    public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
    public Long getSubjAreaId() { return iSubjAreaId; }
    public void setSubjAreaId(Long subjAreaId) { iSubjAreaId = subjAreaId; }
    public Long getManagerId() { return iManagerId; }
    public void setManagerId(Long managerId) { iManagerId = managerId; }
    
    public void load(HttpServletRequest request) {
        Integer n = (Integer)request.getSession().getAttribute("LastChanges.N");
        setN(n==null?100:n.intValue());
        setDepartmentId((Long)request.getSession().getAttribute("LastChanges.DepartmentId"));
        setSubjAreaId((Long)request.getSession().getAttribute("LastChanges.SubjAreaId"));
        setManagerId((Long)request.getSession().getAttribute("LastChanges.ManagerId"));
    }
    
    public void save(HttpServletRequest request) {
        request.getSession().setAttribute("LastChanges.N", new Integer(getN()));
        if (getDepartmentId()==null)
            request.getSession().removeAttribute("LastChanges.DepartmentId");
        else
            request.getSession().setAttribute("LastChanges.DepartmentId", getDepartmentId());
        if (getSubjAreaId()==null)
            request.getSession().removeAttribute("LastChanges.SubjAreaId");
        else
            request.getSession().setAttribute("LastChanges.SubjAreaId", getSubjAreaId());
        if (getManagerId()==null)
            request.getSession().removeAttribute("LastChanges.ManagerId");
        else
            request.getSession().setAttribute("LastChanges.ManagerId", getManagerId());
    }
}

