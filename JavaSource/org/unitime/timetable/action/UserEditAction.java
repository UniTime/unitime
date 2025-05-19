/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.action;

import java.util.Iterator;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.api.ApiToken;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.UserEditForm;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.UserDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.SpringApplicationContextHolder;


/** 
 * @author Tomas Muller
 */
@Action(value = "userEdit", results = {
		@Result(name = "list", type = "tiles", location = "userEditList.tiles"),
		@Result(name = "add", type = "tiles", location = "userEditAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "userEditEdit.tiles")
	})
@TilesDefinitions({
	@TilesDefinition(name = "userEditList.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Users (Database Authentication)"),
			@TilesPutAttribute(name = "body", value = "/admin/userEdit.jsp")
		}),
	@TilesDefinition(name = "userEditAdd.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Add User"),
			@TilesPutAttribute(name = "body", value = "/admin/userEdit.jsp")
		}),
	@TilesDefinition(name = "userEditEdit.tiles", extend = "baseLayout", putAttributes =  {
			@TilesPutAttribute(name = "title", value = "Edit User"),
			@TilesPutAttribute(name = "body", value = "/admin/userEdit.jsp")
		})
})
public class UserEditAction extends UniTimeAction<UserEditForm> {
	private static final long serialVersionUID = -1707528693336547809L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	private String id;
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String execute() throws Exception {
		if (form == null)
			form = new UserEditForm();
		
        // Check Access
		sessionContext.checkPermission(Right.Users);
        
        // Read operation to be performed
		if (op == null) op = form.getOp();
        else form.setOp(op);

        if (op==null)
            form.reset();
        
        // Reset Form
        if (MSG.actionBackToUsers().equals(op)) {
            form.reset();
        }
        
        if (MSG.actionAddUser().equals(op)) {
            form.load(null);
        }
        
        if (MSG.actionRequestPasswordChange().equals(op)) {
        	response.sendRedirect("password?reset=1");
        	return null;
        }

        // Add / Update
        if (MSG.actionSaveUser().equals(op) || MSG.actionUpdateUser().equals(op)) {
            // Validate input
        	form.validate(this);
        	if (!hasFieldErrors()) {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (UserDAO.getInstance()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	form.saveOrUpdate(hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

        	    form.reset();
            }
        }

        // Edit
        if ("Edit".equals(op)) {
            if (id==null || id.trim().isEmpty()) {
            	addFieldError("form.externalId", MSG.errorRequiredField(MSG.columnExternalId()));
            } else {
                org.unitime.timetable.model.User u = org.unitime.timetable.model.User.findByExternalId(id);
            	
                if( u==null) {
                	addFieldError("form.externalId", MSG.errorDoesNotExists(id));
                } else {
                	form.load(u);
                	if (ApplicationProperty.ApiCanUseAPIToken.isTrue())
                		form.setToken(getApiToken().getToken(u.getExternalUniqueId(), u.getPassword()));
                }
            }
        }

        // Delete 
        if (MSG.actionDeleteUser().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (UserDAO.getInstance()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	form.delete(hibSession);
            	
            	if (tx != null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    form.reset();
        }
        
        if ("List".equals(form.getOp())) {
            return "list";
        }
        
        return (MSG.actionSaveUser().equals(form.getOp()) ? "add" : "edit");
	}
	
    public String getUsersTable() {
		WebTable.setOrder(sessionContext,"users.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = null;
        boolean showTokents = ApplicationProperty.ApiCanUseAPIToken.isTrue();
        if (showTokents) {
        	webTable = new WebTable( 4,
    			    null, "userEdit.action?ord=%%",
    			    new String[] {
    			    		MSG.columnExternalId(),
    			    		MSG.columnUserName(),
    			    		MSG.columnTimetableManager(),
    			    		MSG.columnAPIKey()},
    			    new String[] {"left", "left", "left", "left"},
    			    null );	
        } else {
        	webTable = new WebTable( 3,
    			    null, "userEdit.action?ord=%%",
    			    new String[] {
    			    		MSG.columnExternalId(),
    			    		MSG.columnUserName(),
    			    		MSG.columnTimetableManager()
    			    },
    			    new String[] {"left", "left", "left"},
    			    null );
        }
        
        List users = UserDAO.getInstance().findAll();
		if(users.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.messageNoUsers()}, null);			    
		}
		
        for (Iterator i=users.iterator();i.hasNext();) {
            org.unitime.timetable.model.User user = (org.unitime.timetable.model.User)i.next();
        	String onClick = "onClick=\"document.location='userEdit.action?op=Edit&id=" + user.getExternalUniqueId() + "';\"";
            TimetableManager mgr = TimetableManager.findByExternalId(user.getExternalUniqueId());
            if (showTokents) {
            	String token = getApiToken().getToken(user.getExternalUniqueId(), user.getPassword());
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
        
	    return webTable.printTable(WebTable.getOrder(sessionContext,"users.ord"));
    }
    
    public ApiToken getApiToken() {
    	return (ApiToken)SpringApplicationContextHolder.getBean("apiToken");
    }
}

