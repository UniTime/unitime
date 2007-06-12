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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.DistributionTypeEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DistributionType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.DistributionTypeDAO;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;


/** 
 * MyEclipse Struts
 * Creation date: 02-18-2005
 * 
 * XDoclet definition:
 * @struts:action path="/distributionTypeEdit" name="distributionTypeEditForm" parameter="do" scope="request" validate="true"
 * @struts:action-forward name="showEdit" path="/admin/distributionTypeEdit.jsp"
 * @struts:action-forward name="showDistributionTypeList" path="/distributionTypeList.do" redirect="true"
 */
public class DistributionTypeEditAction extends Action {
	// --------------------------------------------------------- Instance Variables

	// --------------------------------------------------------- Methods

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
    public ActionForward execute(
            ActionMapping mapping,
            ActionForm form,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
    	
    		DistributionTypeEditForm myForm = (DistributionTypeEditForm) form;
		
			// Check Access
			if (!Web.isLoggedIn(request.getSession()) || !Web.hasRole(request.getSession(),Roles.getAdminRoles())) {
				throw new Exception ("Access Denied.");
			}
			
	    	User user = Web.getUser(request.getSession());
	    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();
        
			// Read operation to be performed
			String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
			if (op==null) {
				Long id =  new Long(Long.parseLong(request.getParameter("id")));
				myForm.setRefTableEntry((new DistributionTypeDAO()).get(id), sessionId);
				
			}
			
	        if (request.getParameterValues("depts")!=null) {
	        	String[] depts = request.getParameterValues("depts");
	        	for (int i=0;i<depts.length;i++)
	        		myForm.getDepartmentIds().add(new Long(depts[i]));
	        }

	        List list = (new DepartmentDAO()).getSession()
	    		.createCriteria(Department.class)
	    		.add(Restrictions.eq("session.uniqueId", sessionId))
	    		.addOrder(Order.asc("deptCode"))
	    		.list();
	    	Vector availableDepts = new Vector();
	    	for (Iterator iter = list.iterator();iter.hasNext();) {
	    		Department d = (Department) iter.next();
	    		availableDepts.add(new LabelValueBean(d.getDeptCode() + "-" + d.getName(), d.getUniqueId().toString()));
	    	}
	    	request.setAttribute(Department.DEPT_ATTR_NAME, availableDepts);

	    	if ("Save".equals(op)) {
				DistributionTypeDAO dao = new DistributionTypeDAO();
				org.hibernate.Session hibSession = dao.getSession();
				DistributionType distType = dao.get(myForm.getUniqueId());
				DistributionType x = (DistributionType) myForm.getRefTableEntry();
				distType.setAbbreviation(x.getAbbreviation());
				distType.setAllowedPref(x.getAllowedPref());
				distType.setDescr(x.getDescr());
				distType.setInstructorPref(x.isInstructorPref()==null?Boolean.FALSE:x.isInstructorPref());
				distType.setLabel(x.getLabel());
				HashSet oldDepts = new HashSet(distType.getDepartments());
				for (Enumeration e=myForm.getDepartmentIds().elements();e.hasMoreElements();) {
					Long departmentId = (Long)e.nextElement();
					Department d = (new DepartmentDAO()).get(departmentId,hibSession);
					if (d==null) continue;
					if (oldDepts.remove(d)) {
						//not changed -> do nothing
					} else {
						distType.getDepartments().add(d);
					}
				}
				for (Iterator i=oldDepts.iterator();i.hasNext();) {
					Department d = (Department)i.next();
					if (!d.getSessionId().equals(sessionId)) continue;
					distType.getDepartments().remove(d);
				}
				hibSession.saveOrUpdate(distType);
                ChangeLog.addChange(
                        hibSession, 
                        request, 
                        distType, 
                        ChangeLog.Source.DIST_TYPE_EDIT, 
                        ChangeLog.Operation.UPDATE, 
                        null, 
                        null);
                
				hibSession.flush();
				return mapping.findForward("showDistributionTypeList");
			}
			if ("Back".equals(op)) {
				return mapping.findForward("showDistributionTypeList");
			}
	        if ("Add Department".equals(op)) {
	            ActionMessages errors = new ActionErrors();
				if (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0)
					errors.add("department", new ActionMessage("errors.generic", "No department selected."));
				else {
					boolean contains = myForm.getDepartmentIds().contains(myForm.getDepartmentId());
					if (contains)
						errors.add("department", new ActionMessage("errors.generic", "Department already present in the list of departments."));
				}
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	            } else {
	            	myForm.getDepartmentIds().add(myForm.getDepartmentId());
	            }
	        }

	        if ("Remove Department".equals(op)) {
	            ActionMessages errors = new ActionErrors();
				if (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0)
					errors.add("department", new ActionMessage("errors.generic", "No department selected."));
				else {
					boolean contains = myForm.getDepartmentIds().contains(myForm.getDepartmentId());
					if (!contains)
						errors.add("department", new ActionMessage("errors.generic", "Department not present in the list of departments."));
				}
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	            } else {
	            	myForm.getDepartmentIds().remove(myForm.getDepartmentId());
	            }	
	        }
			return mapping.findForward("showEdit");
			
	}
}