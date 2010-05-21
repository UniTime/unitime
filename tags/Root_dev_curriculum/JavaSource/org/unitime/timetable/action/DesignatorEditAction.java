/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.DesignatorEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Designator;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.DesignatorDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;


/** 
 * MyEclipse Struts
 * Creation date: 07-26-2006
 * 
 * XDoclet definition:
 * @struts:action path="/designatorEdit" name="designatorEditForm" input="/user/designatorEdit.jsp" scope="request"
 */
public class DesignatorEditAction extends Action {

    // --------------------------------------------------------- Instance Variables

    // --------------------------------------------------------- Methods

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
        
	    HttpSession httpSession = request.getSession();
        if(!Web.isLoggedIn( httpSession )) {
            throw new Exception ("Access Denied.");
        }
        
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());        
        DesignatorEditForm frm = (DesignatorEditForm) form;
	    ActionMessages errors = null;
        
        // Get operation
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
        			        
        if(op==null)
            op = request.getParameter("hdnOp");
        
        if(op==null || op.trim().length()==0)
            throw new Exception ("Invalid operation");

        if (op.equals(rsc.getMessage("button.backToPrevious"))) {
            if (BackTracker.doBack(request, response))
                return null;
        }            
        
        // Set up Lists
        frm.setOp(op);
        Set subjectAreas = TimetableManager.getSubjectAreas(user);
        frm.setSubjectAreas(subjectAreas);
        
        // Add New Designator - from subject area
        if (op.equals(rsc.getMessage("button.addDesignator"))) {
            String subjectAreaId = (String) request.getAttribute("subjectAreaId");
            if (subjectAreaId==null || subjectAreaId.trim().length()==0)
                throw new Exception ("Invalid Subject Area Id");
            
            frm.setSubjectAreaId(new Long(subjectAreaId));
            frm.setReadOnly("subject");            

            setupInstructors(request, frm);
            return mapping.findForward("displayDesignatorDetail");
        }            
        
        // Add New Designator - from instructor
        if (op.equals(rsc.getMessage("button.addDesignator2"))) {
            String instructorId = (String) request.getAttribute("instructorId");
            if (instructorId==null || instructorId.trim().length()==0)
                throw new Exception ("Invalid Instructor Id");
            
            frm.setInstructorId(new Long(instructorId));
            frm.setReadOnly("instructor");
            frm.setSubjectAreas(new DepartmentalInstructorDAO().get(new Long(instructorId)).getDepartment().getSubjectAreas());
            setupInstructors(request, frm);
            return mapping.findForward("displayDesignatorDetail");
        }            
        
        // Edit Designator - load details
        if (op.equalsIgnoreCase(rsc.getMessage("op.edit"))) {
            doLoad(request, frm);
            setupInstructors(request, frm);
            return mapping.findForward("displayDesignatorDetail");
        }
        
        // Save/Update Designator
        if (op.equals(rsc.getMessage("button.saveDesignator"))
                || op.equals(rsc.getMessage("button.updateDesignator")) ) {
            
            // Validate
            errors = frm.validate(mapping, request);
            if (errors.size()!=0 || !isDesignatorUnique(frm)) {
                if (errors.size()==0) {
                    errors.add( "uniqueId", 
                        	new ActionMessage("errors.generic", "This combination of Subject / Instructor / Code already exists."));
            	}
                setupInstructors(request, frm);
                saveErrors(request, errors);
                return mapping.findForward("displayDesignatorDetail");                
            }
            
            
            doSaveOrUpdate(request, frm, rsc, op);
            request.setAttribute("subjectAreaId", frm.getSubjectAreaId().toString());
            if (BackTracker.doBack(request, response))
                return null;
        }
        
        // Delete Designator
        if (op.equals(rsc.getMessage("button.deleteDesignator"))) {
            doDelete(request, frm);
            request.setAttribute("subjectAreaId", frm.getSubjectAreaId().toString());
            if (BackTracker.doBack(request, response))
                return null;
        }
        
        return mapping.findForward("displayDesignatorList");
    }

    /**
     * Checks that combination of Subject/Instructor/Code 
     * does not already exist
     * @param frm
     * @return
     */
    private boolean isDesignatorUnique(DesignatorEditForm frm) {
        
        String query = "from Designator " +
        				"where subjectArea=:subjectArea and instructor=:instructor and code=:code";
        if (frm.getUniqueId()!=null && frm.getUniqueId().longValue()>0L) {
            query += " and uniqueId!=:uniqueId";
        }
        
        DesignatorDAO ddao = new DesignatorDAO();
        org.hibernate.Session hibSession = ddao.getSession();
        
        Query q = hibSession.createQuery(query);
        q.setInteger("subjectArea", frm.getSubjectAreaId().intValue());
        q.setLong("instructor", frm.getInstructorId().longValue());
        q.setString("code", frm.getCode().trim());
        if (frm.getUniqueId()!=null && frm.getUniqueId().longValue()>0L) {
            q.setLong("uniqueId", frm.getUniqueId().longValue());
        }
        
        return (q.list().size()==0);
    }

    /**
     * @param request
     * @param frm
     */
    private void doLoad(
            HttpServletRequest request, 
            DesignatorEditForm frm) throws Exception {
        
        String id = request.getParameter("id");
        if (id==null || id.trim().length()==0)
            throw new Exception ("Designator Unique Id was not supplied");
        
        DesignatorDAO ddao = new DesignatorDAO();
        Designator d = ddao.get(new Long(id));

        frm.setUniqueId(new Long(id));
        frm.setSubjectAreaId(d.getSubjectArea().getUniqueId());        
        frm.setInstructorId(d.getInstructor().getUniqueId());
        frm.setCode(d.getCode());
        frm.setReadOnly("both");
    }

    /**
     * @param request
     * @param frm
     */
    private void setupInstructors(
            HttpServletRequest request, 
            DesignatorEditForm frm) throws Exception {
        
        if (frm.getUniqueId()!=null && frm.getUniqueId().longValue()>0L) {
            frm.setReadOnly("both");
        }
        
        if ((frm.getReadOnly().equals("subject") || frm.getReadOnly().equals("both")) 
                && (frm.getSubjectAreaAbbv()==null || frm.getSubjectAreaAbbv().length()==0) ) {
            
            SubjectAreaDAO sdao = new SubjectAreaDAO();
            SubjectArea sa = sdao.get(frm.getSubjectAreaId());
            frm.setSubjectAreaAbbv(sa.getSubjectAreaAbbreviation());
        }
        
        if ((frm.getReadOnly().equals("instructor") || frm.getReadOnly().equals("both"))
                && (frm.getInstructorName()==null || frm.getInstructorName().length()==0) ) {
            
            DepartmentalInstructorDAO ddao = new DepartmentalInstructorDAO();
            DepartmentalInstructor di = ddao.get(frm.getInstructorId());

            HttpSession httpSession = request.getSession();
            User user = Web.getUser(httpSession);            
            String nameFormat = Settings.getSettingValue(user, Constants.SETTINGS_INSTRUCTOR_NAME_FORMAT);
            frm.setInstructorName(di.getName(nameFormat));
        }
        
        if ( frm.getSubjectAreaId()!=null && frm.getSubjectAreaId().intValue()>0) {
            SubjectAreaDAO sdao = new SubjectAreaDAO();
            SubjectArea sa = sdao.get(frm.getSubjectAreaId());
            LookupTables.setupInstructors(request, sa.getDepartment().getUniqueId());
        }
        else {
	        if (frm.getInstructorId()!=null && frm.getInstructorId().longValue()>0L) {
	            DepartmentalInstructorDAO ddao = new DepartmentalInstructorDAO();
	            DepartmentalInstructor di = ddao.get(frm.getInstructorId());
	            LookupTables.setupInstructors(request, di.getDepartment().getUniqueId());
	        }
        }        
    }

    /**
     * @param request
     * @param frm
     */
    private void doSaveOrUpdate(
            HttpServletRequest request, 
            DesignatorEditForm frm,
            MessageResources rsc,
            String op ) throws Exception {
        
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        
        try {
            DesignatorDAO ddao = new DesignatorDAO();
            SubjectAreaDAO sdao = new SubjectAreaDAO();
            DepartmentalInstructorDAO didao = new DepartmentalInstructorDAO();

            hibSession = ddao.getSession();
            tx = hibSession.beginTransaction();
            
            SubjectArea sa = sdao.get(frm.getSubjectAreaId());
            DepartmentalInstructor di = didao.get(frm.getInstructorId());
            
            Designator d = null;
            if ( op.equals(rsc.getMessage("button.saveDesignator")) ) {
                d = new Designator();
            }
            else {
                d = ddao.get(frm.getUniqueId());
            }
            
            d.setInstructor(di);
            d.setSubjectArea(sa);
            d.setCode(frm.getCode().trim());
            
            di.addTodesignatorSubjectAreas(d);
            sa.addTodesignatorInstructors(d);

            hibSession.saveOrUpdate(d);
            hibSession.saveOrUpdate(di);
            hibSession.saveOrUpdate(sa);
            
            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    di,
                    d.toString(),
                    ChangeLog.Source.DESIGNATOR_EDIT, 
                    (op.equals(rsc.getMessage("button.saveDesignator"))?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                    sa, 
                    sa.getDepartment());
            
            hibSession.flush();
            tx.commit();
            
            hibSession.refresh(d);            
            hibSession.refresh(di);            
            hibSession.refresh(sa);            
        }
        catch (Exception e ){
            if (tx!=null) tx.rollback();
            Debug.error(e);
            throw (e);
        }
    }        

     /**
     * @param request
     * @param frm
     */
    private void doDelete(
            HttpServletRequest request, 
            DesignatorEditForm frm) throws Exception {
        
        org.hibernate.Session hibSession = null;
        Transaction tx = null;
        
        try {
            DesignatorDAO ddao = new DesignatorDAO();
            hibSession = ddao.getSession();
            tx = hibSession.beginTransaction();
            
            Designator d = ddao.get(frm.getUniqueId());
            SubjectArea sa = d.getSubjectArea();
            DepartmentalInstructor di = d.getInstructor();
            
            sa.getDesignatorInstructors().remove(d);
            di.getDesignatorSubjectAreas().remove(d);
            
            hibSession.saveOrUpdate(di);
            hibSession.saveOrUpdate(sa);

            ChangeLog.addChange(
                    hibSession, 
                    request, 
                    di, 
                    d.toString(),
                    ChangeLog.Source.DESIGNATOR_EDIT, 
                    ChangeLog.Operation.DELETE, 
                    sa, 
                    sa.getDepartment());

            hibSession.delete(d);
            
            hibSession.flush();
            
            tx.commit();
            
            hibSession.refresh(di);            
            hibSession.refresh(sa);            
        }
        catch (Exception e ){
            if (tx!=null) tx.rollback();
            Debug.error(e);
            throw (e);
        }
    }        
}
