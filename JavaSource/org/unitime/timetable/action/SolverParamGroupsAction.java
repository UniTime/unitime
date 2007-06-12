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
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolverParamGroupsForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;


/** 
 * @author Tomas Muller
 */
public class SolverParamGroupsAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SolverParamGroupsForm myForm = (SolverParamGroupsForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.setOp("Add New");
	        op = "list";
        }
        
        // Reset Form
        if ("Clear".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }

        // Add / Update
        if ("Update".equals(op) || "Add New".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSolverParamGroups");
            } else {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
                SolverParameterGroup group = null;

                if(op.equals("Add New"))
                	group = new SolverParameterGroup();
                else 
                	group = dao.get(myForm.getUniqueId());
                
                group.setName(myForm.getName());
                group.setDescription(myForm.getDescription());                
                group.setCondition(myForm.getCondition());
                if (myForm.getOrder()<0) {
                	group.setOrder(new Integer(dao.findAll().size()));
                }
                dao.saveOrUpdate(group);
                
                myForm.reset(mapping, request);
                myForm.setOp("Add New");
            }
        }

        // Edit
        if(op.equals("Edit")) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
                mapping.findForward("showSolverParamGroups");
            }
            else {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
            	SolverParameterGroup group = dao.get(new Long(id));
                if(group==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    mapping.findForward("showSolverParamGroups");
                }
                else {
                    myForm.setUniqueId(group.getUniqueId());
                    myForm.setName(group.getName());
                    myForm.setOrder(group.getOrder().intValue());
                    myForm.setDescription(group.getDescription());
                    myForm.setCondition(group.getCondition());
                    myForm.setOp("Update");
                }                
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
            	org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
    			
    			SolverParameterGroup group = dao.get(myForm.getUniqueId(), hibSession);

    			List list = hibSession.createCriteria(SolverParameterGroup.class).add(Restrictions.gt("order", group.getOrder())).list();
    			
    			for (Iterator i=list.iterator();i.hasNext();) {
    				SolverParameterGroup g = (SolverParameterGroup)i.next();
    				g.setOrder(new Integer(g.getOrder().intValue()-1));
    				dao.save(g,hibSession);
    			}
    			
    			dao.delete(group, hibSession);
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }
        
        // Move Up or Down
        if("Move Up".equals(op) || "Move Down".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
            	org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
    			
    			SolverParameterGroup group = dao.get(myForm.getUniqueId(), hibSession);
    			if ("Move Up".equals(op)) {
    				List list = hibSession.createCriteria(SolverParameterGroup.class).add(Restrictions.eq("order", new Integer(group.getOrder().intValue()-1))).list();
    				if (!list.isEmpty()) {
    					SolverParameterGroup prior = (SolverParameterGroup)list.get(0);
    					prior.setOrder(new Integer(prior.getOrder().intValue()+1));
    					dao.save(prior,hibSession);
        				group.setOrder(new Integer(group.getOrder().intValue()-1));
        				dao.save(group,hibSession);
    				}
    			} else {
    				List list = hibSession.createCriteria(SolverParameterGroup.class).add(Restrictions.eq("order", new Integer(group.getOrder().intValue()+1))).list();
    				if (!list.isEmpty()) {
    					SolverParameterGroup next = (SolverParameterGroup)list.get(0);
    					next.setOrder(new Integer(next.getOrder().intValue()-1));
    					dao.save(next,hibSession);
        				group.setOrder(new Integer(group.getOrder().intValue()+1));
        				dao.save(group,hibSession);
    				}
    			}
    			myForm.setOrder(group.getOrder().intValue());
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
    	    myForm.setOp("Update");
        }
        // Read all existing settings and store in request
        getSolverParameterGroups(request);        
        return mapping.findForward("showSolverParamGroups");
	}
	
    private void getSolverParameterGroups(HttpServletRequest request) throws Exception {
		Transaction tx = null;
		
		WebTable.setOrder(request.getSession(),"solverParamGroups.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 3,
			    "Solver Parameter Groups", "solverParamGroups.do?ord=%%",
			    new String[] {"Ord", "Name", "Description"},
			    new String[] {"left", "left", "left"},
			    null );
        int size = 0;

        try {
        	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
			org.hibernate.Session hibSession = dao.getSession();
    		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    			tx = hibSession.beginTransaction();
            
			List list = hibSession.createCriteria(SolverParameterGroup.class).list();
			size = list.size();
			
			if(list.isEmpty()) {
			    webTable.addLine(null, new String[] {"No solver parameter group defined."}, null, null );			    
			} else {
				for (Iterator i=list.iterator();i.hasNext();) {
					SolverParameterGroup group = (SolverParameterGroup)i.next();
					String onClick = "onClick=\"document.location='solverParamGroups.do?op=Edit&id=" + group.getUniqueId() + "';\"";
					webTable.addLine(onClick, new String[] {group.getOrder().toString(), group.getName(), group.getDescription()},
							new Comparable[] {group.getOrder(), group.getName(), group.getDescription()});
				}
			}
			
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;
	    }

	    request.setAttribute("SolverParameterGroup.table", webTable.printTable(WebTable.getOrder(request.getSession(),"solverParamGroups.ord")));
	    request.setAttribute("SolverParameterGroup.last", new Integer(size-1));
    }	

}

