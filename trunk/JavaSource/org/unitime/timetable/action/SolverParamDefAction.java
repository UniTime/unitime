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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.unitime.commons.Debug;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolverParamDefForm;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;


/** 
 * @author Tomas Muller
 */
public class SolverParamDefAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverParamDefForm myForm = (SolverParamDefForm) form;

        // Check Access
        if (!Web.isLoggedIn( request.getSession() )
               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (op==null) {
            myForm.setOp("Add New");
            myForm.setVisible(Boolean.TRUE);
	        op = "list";
        }
        
        // Reset Form
        if ("Clear".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
            myForm.setOp("Add New");
        }

        // Add / Update
        if ("Update".equals(op) || "Add New".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
                mapping.findForward("showSolverParamDef");
            } else {
            	Transaction tx = null;
            	try {
            		SolverParameterDefDAO dao = new SolverParameterDefDAO();
            		org.hibernate.Session hibSession = dao.getSession();
            		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            			tx = hibSession.beginTransaction();
            		
            		SolverParameterDef def = null;
            		if(op.equals("Add New"))
            			def = new SolverParameterDef();
            		else
            			def = dao.get(myForm.getUniqueId(), hibSession);
            		
            		def.setName(myForm.getName());
            		def.setDescription(myForm.getDescription());                
            		def.setDefault(myForm.getDefault());
            		def.setType(myForm.getType());
            		def.setVisible(myForm.getVisible());
            		SolverParameterGroup group = null;
            		List groups = hibSession.createCriteria(SolverParameterGroup.class).add(Restrictions.eq("name", myForm.getGroup())).list();
            		if (!groups.isEmpty())
            			group = (SolverParameterGroup)groups.get(0);
            		if (def.getGroup()!=null && !def.getGroup().equals(group)) {
            			List list = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",def.getGroup())).add(Restrictions.gt("order", def.getOrder())).list();
            			for (Iterator i=list.iterator();i.hasNext();) {
            				SolverParameterDef d = (SolverParameterDef)i.next();
            				d.setOrder(new Integer(d.getOrder().intValue()-1));
            				dao.save(d,hibSession);
            			}
            			myForm.setOrder(-1);
            		}
            		if (myForm.getOrder()<0) {
            			def.setOrder(new Integer(group==null?0:group.getParameters().size()));
            		}
                	def.setGroup(group);
                	dao.saveOrUpdate(def,hibSession);
                	
                	if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        			Debug.error(e);
        	    }
            	myForm.reset(mapping, request);
            	myForm.setVisible(Boolean.TRUE);
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
                mapping.findForward("showSolverParamDef");
            }
            else {
            	SolverParameterDefDAO dao = new SolverParameterDefDAO();
            	SolverParameterDef def = dao.get(new Long(id));
                if(def==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    mapping.findForward("showSolverParamDef");
                }
                else {
                    myForm.setUniqueId(def.getUniqueId());
                    myForm.setName(def.getName());
                    myForm.setOrder(def.getOrder().intValue());
                    myForm.setDescription(def.getDescription());
                    myForm.setGroup(def.getGroup().getName());
                    myForm.setType(def.getType());
                    myForm.setDefault(def.getDefault());
                    myForm.setVisible(def.isVisible());
                    myForm.setOp("Update");
                }                
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	SolverParameterDefDAO dao = new SolverParameterDefDAO();
            	org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
    			
    			SolverParameterDef def = dao.get(myForm.getUniqueId(), hibSession);

    			List list = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",def.getGroup())).add(Restrictions.gt("order", def.getOrder())).list();
    			
    			for (Iterator i=list.iterator();i.hasNext();) {
    				SolverParameterDef d = (SolverParameterDef)i.next();
    				d.setOrder(new Integer(d.getOrder().intValue()-1));
    				dao.save(d,hibSession);
    			}
    			
    			dao.delete(def, hibSession);
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
            myForm.setOp("Add New");
        }
        
        // Move Up or Down
        if("Move Up".equals(op) || "Move Down".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	SolverParameterDefDAO dao = new SolverParameterDefDAO();
            	org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
    			
    			SolverParameterDef def = dao.get(myForm.getUniqueId(), hibSession);
    			if ("Move Up".equals(op)) {
    				List list = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",def.getGroup())).add(Restrictions.eq("order", new Integer(def.getOrder().intValue()-1))).list();
    				if (!list.isEmpty()) {
    					SolverParameterDef prior = (SolverParameterDef)list.get(0);
    					prior.setOrder(new Integer(prior.getOrder().intValue()+1));
    					dao.save(prior,hibSession);
    					def.setOrder(new Integer(def.getOrder().intValue()-1));
        				dao.save(def,hibSession);
    				}
    			} else {
    				List list = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",def.getGroup())).add(Restrictions.eq("order", new Integer(def.getOrder().intValue()+1))).list();
    				if (!list.isEmpty()) {
    					SolverParameterDef next = (SolverParameterDef)list.get(0);
    					next.setOrder(new Integer(next.getOrder().intValue()-1));
    					dao.save(next,hibSession);
    					def.setOrder(new Integer(def.getOrder().intValue()+1));
        				dao.save(def,hibSession);
    				}
    			}
    			myForm.setOrder(def.getOrder().intValue());
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
    	    myForm.setOp("Update");
        }
        // Read all existing settings and store in request
        getSolverParameterDefs(request, myForm.getUniqueId());        
        return mapping.findForward("showSolverParamDef");
	}

    private void getSolverParameterDefs(HttpServletRequest request, Long uniqueId) throws Exception {
		Transaction tx = null;
		
		WebTable.setOrder(request.getSession(),"solverParamDef.ord",request.getParameter("ord"),1);
		
		StringBuffer tables = new StringBuffer();
        try {
        	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
        	org.hibernate.Session hibSession = dao.getSession();
    		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    			tx = hibSession.beginTransaction();
            
			List groups = dao.findAll(hibSession, Order.asc("order"));
			
			if (groups.isEmpty()) {
				// Create web table instance 
		        WebTable webTable = new WebTable( 6,
					    "Solver Parameters", "solverParamDef.do?ord=%%",
					    new String[] {"Ord", "Name", "Description", "Type", "Visible", "Default"},
					    new String[] {"left", "left", "left", "left", "left", "left"},
					    null );
				webTable.addLine(null, new String[] {"No solver parameter group defined."}, null, null );
				tables.append(webTable.printTable(WebTable.getOrder(request.getSession(),"solverParamDef.ord")));
			}
			
			for (Iterator i=groups.iterator();i.hasNext();) {
				SolverParameterGroup group = (SolverParameterGroup)i.next();
				List parameters = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",group)).list();
				if (parameters.isEmpty()) continue;
		        WebTable webTable = new WebTable( 6,
					    group.getDescription(), "solverParamDef.do?ord=%%",
					    new String[] {"Ord", "Name", "Description", "Type", "Visible", "Default"},
					    new String[] {"left", "left", "left", "left", "left", "left"},
					    null );
		        if (parameters.isEmpty()) {
		        	webTable.addLine(null, new String[] {"No parameter defined in group <i>"+group.getDescription()+"</i>."}, null, null );
		        }
		        for (Iterator j=parameters.iterator();j.hasNext();) {
		        	SolverParameterDef def= (SolverParameterDef)j.next();
					String onClick = "onClick=\"document.location='solverParamDef.do?op=Edit&id=" + def.getUniqueId() + "';\"";
					webTable.addLine(onClick, new String[] {
							def.getOrder().toString(), 
							def.getName(), 
							def.getDescription(),
							def.getType(),
							def.isVisible().toString(),
							def.getDefault()},
						new Comparable[] {
							def.getOrder(), 
							def.getName(), 
							def.getDescription(),
							def.getType(),
							def.isVisible().toString(),
							def.getDefault()});
					if (def.getUniqueId().equals(uniqueId))
						request.setAttribute("SolverParameterDef.last", new Integer(parameters.size()-1));
		        }
		        if (tables.length()>0) 
		        	tables.append("<TR><TD colspan='6'>&nbsp;</TD></TR>");
				tables.append(webTable.printTable(WebTable.getOrder(request.getSession(),"solverParamDef.ord")));
			}
			
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
    		throw e;
	    }
	    request.setAttribute("SolverParameterDef.table",tables.toString());
    }	
}

