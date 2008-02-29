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
import java.util.List;

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
import org.unitime.timetable.form.UserEditForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.UserDAO;


/** 
 * @author Tomas Muller
 */
public class UserEditAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		UserEditForm myForm = (UserEditForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.reset(mapping, request);
        }
        
    	User user = Web.getUser(request.getSession());
    	Long sessionId = Session.getCurrentAcadSession(user).getSessionId();

        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        if ("Add User".equals(op)) {
            myForm.load(null);
        }

        // Add / Update
        if ("Update".equals(op) || "Save".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new UserDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

        	    myForm.reset(mapping, request);
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("externalId", new ActionMessage("errors.invalid", id));
                saveErrors(request, errors);
            } else {
                org.unitime.timetable.model.User u = org.unitime.timetable.model.User.findByExternalId(id);
            	
                if(u==null) {
                    errors.add("externalId", new ActionMessage("errors.invalid", id));
                    saveErrors(request, errors);
                } else {
                	myForm.load(u);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new UserDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.reset(mapping, request);
        }
        
        if ("List".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            getUserList(request, sessionId);    
            return mapping.findForward("list");
        }
        
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getUserList(HttpServletRequest request, Long sessionId) throws Exception {
		WebTable.setOrder(request.getSession(),"users.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 4,
			    null, "userEdit.do?ord=%%",
			    new String[] {"External ID", "User Name", "Manager"},
			    new String[] {"left", "left", "left"},
			    null );
        
        List users = new UserDAO().findAll();
		if(users.isEmpty()) {
		    webTable.addLine(null, new String[] {"No users defined."}, null, null );			    
		}
		
        for (Iterator i=users.iterator();i.hasNext();) {
            org.unitime.timetable.model.User user = (org.unitime.timetable.model.User)i.next();
        	String onClick = "onClick=\"document.location='userEdit.do?op=Edit&id=" + user.getExternalUniqueId() + "';\"";
            TimetableManager mgr = TimetableManager.findByExternalId(user.getExternalUniqueId());
            webTable.addLine(onClick, new String[] {
                    user.getExternalUniqueId(),
                    user.getUsername(),
                    (mgr==null?"":mgr.getName())
        		},new Comparable[] {
        			user.getExternalUniqueId(),
                    user.getUsername(),
                    (mgr==null?"":mgr.getName())
        		});
        }
        
	    request.setAttribute("Users.table", webTable.printTable(WebTable.getOrder(request.getSession(),"users.ord")));
    }	
}

