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

import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.SubjectAreaEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * MyEclipse Struts
 * Creation date: 05-15-2007
 * 
 * XDoclet definition:
 * @struts.action path="/subjectAreaEdit" name="subjectAreaEditForm" input="/admin/subjectAreaEdit.jsp" scope="request"
 * @struts.action-forward name="editSubjectArea" path="SubjectAreaEditTile"
 * @struts.action-forward name="addSubjectArea" path="SubjectAreaAddTile"
 */
public class SubjectAreaEditAction extends Action {
	/*
	 * Generated Methods
	 */

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
			ActionMapping mapping, 
			ActionForm form,
			HttpServletRequest request, 
			HttpServletResponse response) throws Exception {
		
		// Check Access
		if (!Web.isLoggedIn(request.getSession()) 
				|| !Web.hasRole(request.getSession(),Roles.getAdminRoles()))
			throw new Exception ("Access Denied.");
		
		SubjectAreaEditForm frm = (SubjectAreaEditForm) form;
		MessageResources rsc = getResources(request);
		User user = Web.getUser(request.getSession());
		
		// Read operation to be performed
		String op = (frm.getOp()!=null
						? frm.getOp()
						: request.getParameter("op"));
		
        // Add
        if(op.equals(rsc.getMessage("button.addSubjectArea"))) {
    		LookupTables.setupDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
        	return mapping.findForward("addSubjectArea");
        }
        
        // Edit
        if(op.equals(rsc.getMessage("op.edit"))) {
            doLoad(request, frm);
    		LookupTables.setupDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
        	return mapping.findForward("editSubjectArea");
        }
        
        // Update
        if (op.equals(rsc.getMessage("button.updateSubjectArea"))
        		|| op.equals(rsc.getMessage("button.saveSubjectArea")) ) {
            // Validate input
            ActionMessages errors = frm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
        		LookupTables.setupDepts(request, (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME));
                if (frm.getUniqueId()!=null)
                	return mapping.findForward("editSubjectArea");
                else
                	return mapping.findForward("addSubjectArea");
            } 
            else {
            	doUpdate(request, frm);
            }
        }
        
        // Delete
        if(op.equals(rsc.getMessage("button.deleteSubjectArea"))) {
        	doDelete(request, frm);
        }
        
    	if (frm.getUniqueId()!=null)
       		request.setAttribute(Constants.JUMP_TO_ATTR_NAME, frm.getUniqueId().toString());
    	
		return mapping.findForward("back");
	}

	/**
	 * Load the subject area into the form
	 * @param request
	 * @param frm
	 */
	private void doLoad(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Long id = null;
        
        try { 
            id = Long.parseLong(request.getParameter("id"));
        }
        catch (Exception e) {
        	throw new Exception ("Invalid Subject Area IDencountered");
        }
        
        SubjectArea sa = new SubjectAreaDAO().get(id);
        frm.setUniqueId(id);
        frm.setAbbv(sa.getSubjectAreaAbbreviation()!=null ? sa.getSubjectAreaAbbreviation() : "");        
        frm.setDepartment(sa.getDepartment()!=null ? sa.getDepartment().getUniqueId() : null);
        frm.setExternalId(sa.getExternalUniqueId() !=null ? sa.getExternalUniqueId() : "");
        frm.setShortTitle(sa.getShortTitle() !=null ? sa.getShortTitle() : "");
        frm.setLongTitle(sa.getLongTitle() !=null ? sa.getLongTitle() : "");
        frm.setPseudo(sa.isPseudoSubjectArea() !=null ? sa.isPseudoSubjectArea() : null);
        frm.setScheduleBkOnly(sa.isScheduleBookOnly() !=null ? sa.isScheduleBookOnly() : null);		
	}
	
	/**
	 * Delete Subject Area
	 * @param request
	 * @param frm
	 */
	private void doDelete(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		try {
			SubjectAreaDAO sdao = new SubjectAreaDAO();
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();
			
			SubjectArea sa = sdao.get(frm.getUniqueId());
			Set s = sa.getInstructionalOfferings();
			for (Iterator i = s.iterator(); i.hasNext();) {
				InstructionalOffering io = (InstructionalOffering) i.next();
				io.deleteAllDistributionPreferences(hibSession);
				io.deleteAllReservations(hibSession);
				io.deleteAllClasses(hibSession);
				io.deleteAllCourses(hibSession);
				hibSession.delete(io);
			}

	        for (Iterator i = sa.getCourseOfferings().iterator(); i.hasNext(); ) {
	        	CourseOffering co = (CourseOffering) i.next();
	        	hibSession.delete(co);
	        }
			
			hibSession.delete(sa);
			
			tx.commit();
			hibSession.flush();
			hibSession.refresh(org.unitime.timetable.model.Session.getCurrentAcadSession( Web.getUser(request.getSession()) ));
		}
		catch (Exception e) {
			if (tx!=null)
				tx.rollback();
			
			throw (e);
		}
	}

	/**
	 * Update Subject Area
	 * @param request
	 * @param frm
	 */
	private void doUpdate(HttpServletRequest request, SubjectAreaEditForm frm) throws Exception {
		Session hibSession = null;
		Transaction tx = null;
		
		try {
			SubjectAreaDAO sdao = new SubjectAreaDAO();
			DepartmentDAO ddao = new DepartmentDAO();
			
			SubjectArea sa = null;
			
			hibSession = sdao.getSession();
			tx = hibSession.beginTransaction();
			
			if (frm.getUniqueId()!=null) 
				sa = sdao.get(frm.getUniqueId());
			else 
				sa = new SubjectArea();
			
			Department dept = ddao.get(frm.getDepartment());
			
			sa.setSession(
					org.unitime.timetable.model.Session.getCurrentAcadSession(
							Web.getUser( request.getSession() )));
	        sa.setSubjectAreaAbbreviation(frm.getAbbv());        
	        sa.setDepartment(dept);
	        sa.setExternalUniqueId(frm.getExternalId());
	        sa.setShortTitle(frm.getShortTitle());
	        sa.setLongTitle(frm.getLongTitle());
	        sa.setPseudoSubjectArea(frm.getPseudo()==null ? Boolean.FALSE : frm.getPseudo());
	        sa.setScheduleBookOnly(frm.getScheduleBkOnly()==null ? Boolean.FALSE : frm.getScheduleBkOnly());
	        
	        hibSession.saveOrUpdate(sa);			
			
	        tx.commit();			
			hibSession.refresh(sa);
            ChangeLog.addChange(
            		hibSession, 
                    request, 
                    sa, 
                    ChangeLog.Source.SUBJECT_AREA_EDIT, 
                    (frm.getUniqueId()==null?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    sa, 
                    dept);
			hibSession.flush();
			hibSession.refresh(org.unitime.timetable.model.Session.getCurrentAcadSession( Web.getUser(request.getSession()) ));
		}
		catch (Exception e) {
			if (tx!=null)
				tx.rollback();
			
			throw (e);
		}
	}
}