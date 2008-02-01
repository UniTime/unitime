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

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamPeriodEditForm;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.Constants;

/** 
 * @author Tomas Muller
 */
public class ExamPeriodEditAction extends Action {
	
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
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			ExamPeriodEditForm myForm = (ExamPeriodEditForm) form;
			
	        // Check Access
	        if (!Web.isLoggedIn(request.getSession()) || !Web.hasRole(request.getSession(),Roles.getAdminRoles())) {
	            throw new Exception ("Access Denied.");
	        }
	        
	        // Read operation to be performed
	        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
	        
	        if (op==null) {
	            myForm.load(null, request);
	            myForm.setOp("List");
	        }
	        
	    	User user = Web.getUser(request.getSession());
	    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();

	        // Reset Form
	        if ("Back".equals(op)) {
	            if (myForm.getUniqueId()!=null)
	                request.setAttribute("hash", myForm.getUniqueId());
	            myForm.load(null, request);
	            myForm.setOp("List");
	        }
	        
            if ("Add Period".equals(op)) {
                myForm.load(null, request);
                myForm.setOp("Save");
            }

	        // Add / Update
	        if ("Update".equals(op) || "Save".equals(op)) {
	            // Validate input
	            ActionMessages errors = myForm.validate(mapping, request);
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	                myForm.setOp(myForm.getUniqueId().longValue()<0?"Save":"Update");
	            } else {
	        		Transaction tx = null;
	        		
	                try {
	                	org.hibernate.Session hibSession = (new ExamPeriodDAO()).getSession();
	                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	                		tx = hibSession.beginTransaction();
	                	
	                	ExamPeriod ep = myForm.saveOrUpdate(request, hibSession);
	                	
                        ChangeLog.addChange(
                                hibSession, 
                                request, 
                                ep, 
                                ChangeLog.Source.EXAM_PERIOD_EDIT, 
                                ("Save".equals(op)?ChangeLog.Operation.CREATE:ChangeLog.Operation.UPDATE), 
                                null, 
                                null);

                        if (tx!=null) tx.commit();
	        	    } catch (Exception e) {
	        	    	if (tx!=null) tx.rollback();
	        	    	throw e;
	        	    }

	                myForm.setOp("List");
	                if (myForm.getUniqueId()!=null)
	                    request.setAttribute("hash", myForm.getUniqueId());
	            }
	        }

	        // Edit
	        if("Edit".equals(op)) {
	            String id = request.getParameter("id");
	            ActionMessages errors = new ActionMessages();
	            if(id==null || id.trim().length()==0) {
	                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                saveErrors(request, errors);
	                return mapping.findForward("list");
	            } else {
	            	ExamPeriod ep = (new ExamPeriodDAO()).get(new Long(id));
	            	
	                if(ep==null) {
	                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                    saveErrors(request, errors);
	                    return mapping.findForward("list");
	                } else {
	                	myForm.load(ep, request);
	                }
	            }
	        }

	        // Delete 
	        if("Delete".equals(op)) {
	    		Transaction tx = null;
	    		
	            try {
	            	org.hibernate.Session hibSession = (new ExamPeriodDAO()).getSession();
	            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	            		tx = hibSession.beginTransaction();
	            	
                    ExamPeriod ep = (new ExamPeriodDAO()).get(myForm.getUniqueId(), hibSession);
                    ChangeLog.addChange(
                            hibSession, 
                            request, 
                            ep, 
                            ChangeLog.Source.EXAM_PERIOD_EDIT, 
                            ChangeLog.Operation.DELETE, 
                            null, 
                            null);

                    myForm.delete(hibSession);
	            	
	    			tx.commit();
	    	    } catch (Exception e) {
	    	    	if (tx!=null) tx.rollback();
	    	    	throw e;
	    	    }

	    	    myForm.load(null, request);
	            myForm.setOp("List");
	        }
	        
	        if ("List".equals(myForm.getOp())) {
	            // Read all existing settings and store in request
	            getExamPeriods(request);
	            return mapping.findForward("list");
	        } 
	        
	        return mapping.findForward(myForm.getUniqueId().longValue()<0?"add":"edit");
	        
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}

    private void getExamPeriods(HttpServletRequest request) throws Exception {
		WebTable.setOrder(request.getSession(),"examPeriods.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "examPeriodEdit.do?ord=%%",
			    new String[] {"Date", "Start Time", "End Time", "Length"},
			    new String[] {"left", "left", "left", "left"},
			    null );
        
        TreeSet periods = ExamPeriod.findAll(request);
		if(periods.isEmpty()) {
		    webTable.addLine(null, new String[] {"No exan period defined for this session."}, null, null );			    
		}
		
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MM/dd/yyyy");
        SimpleDateFormat stf = new SimpleDateFormat("hh:mm aa");

        for (Iterator i=periods.iterator();i.hasNext();) {
        	ExamPeriod ep = (ExamPeriod)i.next();
        	String onClick = "onClick=\"document.location='examPeriodEdit.do?op=Edit&id=" + ep.getUniqueId() + "';\"";
        	String deptStr = "";
        	String deptCmp = "";
        	webTable.addLine(onClick, new String[] {
        	        "<a name='"+ep.getUniqueId()+"'>"+sdf.format(ep.getStartDate())+"</a>",
        	        stf.format(ep.getStartTime()),
        	        stf.format(ep.getEndTime()),
        	        String.valueOf(Constants.SLOT_LENGTH_MIN*ep.getLength())},
        	        new Comparable[] {
        	        ep.getStartDate(), ep.getStartSlot(), ep.getStartSlot()+ep.getLength(), ep.getLength()});
        }
        
	    request.setAttribute("ExamPeriods.table", webTable.printTable(WebTable.getOrder(request.getSession(),"examPeriods.ord")));
    }	
}

