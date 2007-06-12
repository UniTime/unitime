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
package org.unitime.timetable.action;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.unitime.timetable.form.SolverParameterDefEditForm;
import org.unitime.timetable.model.SolverParameterDef;


/** 
 * @author Tomas Muller
 */
public class SolverParameterDefEditAction extends LookupDispatchAction {

	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods
	
	/**
	 * 
	 */
	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("editSolverParameterDef", "edit");
	      map.put("button.addSolverParameterDef", "add");
	      map.put("button.saveSolverParameterDef", "save");
	      map.put("button.deleteSolverParameterDef", "delete");
	      map.put("button.cancelSolverParameterDef", "cancel");
	      return map;
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 */
	public ActionForward edit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws HibernateException {
		SolverParameterDefEditForm solverParameterDefEditForm = (SolverParameterDefEditForm) form;
		Long id =  new Long(Long.parseLong(request.getParameter("uniqueId")));
		solverParameterDefEditForm.setSolverParameterDef(SolverParameterDef.getSolverParameterDefById(id));
		return mapping.findForward("showEdit");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 */
	public ActionForward add(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws HibernateException {
		return mapping.findForward("showAdd");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 * @throws SQLException
	 */
	public ActionForward save(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws HibernateException, SQLException {
		SolverParameterDefEditForm solverParameterDefEditForm = (SolverParameterDefEditForm) form;
		SolverParameterDef solverParameterDef = solverParameterDefEditForm.getSolverParameterDef();
		if (solverParameterDef.getUniqueId().intValue() == 0 ) {
			SolverParameterDef solverParameterDefNew = new SolverParameterDef();
			solverParameterDefNew.setName(solverParameterDefEditForm.getKey());
			solverParameterDefNew.setDescription(solverParameterDefEditForm.getDesc());
			solverParameterDefNew.setDefault(solverParameterDefEditForm.getDefaultValue());
			solverParameterDefNew.saveOrUpdate();
		}
		else{
			solverParameterDef.saveOrUpdate();
		}
		return mapping.findForward("showSolverParameterDefList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws HibernateException
	 */
	public ActionForward delete(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws HibernateException {
		SolverParameterDefEditForm solverParameterDefEditForm = (SolverParameterDefEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("uniqueId")));
		SolverParameterDef.deleteSolverParameterDefById(id);
		return mapping.findForward("showSolverParameterDefList");
	}
	
	/**
	 * 
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 */
	public ActionForward cancel(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showSolverParameterDefList");
	}

}

