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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.api.ApiToken;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.UserEditForm;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.UserDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/userEdit")
public class UserEditAction extends Action {
	
	@Autowired SessionContext sessionContext;
	
	@Autowired ApiToken apiToken;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		UserEditForm myForm = (UserEditForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.Users);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.reset(mapping, request);
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        if ("Add User".equals(op)) {
            myForm.load(null);
        }
        
        if ("Request Password Change".equals(op)) {
        	response.sendRedirect("gwt.jsp?page=password&reset=1");
        	return null;
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
                	if (ApplicationProperty.ApiCanUseAPIToken.isTrue())
                		myForm.setToken(apiToken.getToken(u.getExternalUniqueId(), u.getPassword()));
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
            getUserList(request);    
            return mapping.findForward("list");
        }
        
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getUserList(HttpServletRequest request) throws Exception {
		WebTable.setOrder(sessionContext,"users.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = null;
        boolean showTokents = ApplicationProperty.ApiCanUseAPIToken.isTrue();
        if (showTokents) {
        	webTable = new WebTable( 4,
    			    null, "userEdit.do?ord=%%",
    			    new String[] {"External ID", "User Name", "Manager", "API Secret"},
    			    new String[] {"left", "left", "left", "left"},
    			    null );	
        } else {
        	webTable = new WebTable( 3,
    			    null, "userEdit.do?ord=%%",
    			    new String[] {"External ID", "User Name", "Manager"},
    			    new String[] {"left", "left", "left"},
    			    null );
        }
        
        List users = new UserDAO().findAll();
		if(users.isEmpty()) {
		    webTable.addLine(null, new String[] {"No users defined."}, null);			    
		}
		
        for (Iterator i=users.iterator();i.hasNext();) {
            org.unitime.timetable.model.User user = (org.unitime.timetable.model.User)i.next();
        	String onClick = "onClick=\"document.location='userEdit.do?op=Edit&id=" + user.getExternalUniqueId() + "';\"";
            TimetableManager mgr = TimetableManager.findByExternalId(user.getExternalUniqueId());
            if (showTokents) {
            	String token = apiToken.getToken(user.getExternalUniqueId(), user.getPassword());
            	webTable.addLine(onClick, new String[] {
                        user.getExternalUniqueId(),
                        user.getUsername(),
                        (mgr==null?"":mgr.getName()),
                        (token == null ? "" : token)
            		},new Comparable[] {
            			user.getExternalUniqueId(),
                        user.getUsername(),
                        (mgr==null?"":mgr.getName()),
                        null
            		});
            } else {
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
        }
        
	    request.setAttribute("Users.table", webTable.printTable(WebTable.getOrder(sessionContext,"users.ord")));
    }	
}

