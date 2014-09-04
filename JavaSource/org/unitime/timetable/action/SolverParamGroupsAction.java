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
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolverParamGroupsForm;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/solverParamGroups")
public class SolverParamGroupsAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SolverParamGroupsForm myForm = (SolverParamGroupsForm) form;

        // Check Access
        sessionContext.checkPermission(Right.SolverParameterGroups);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");

        if (op==null) {
            myForm.setOp("List");
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        if ("Add Solver Parameter Group".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setOp("Save");
        }

        // Add / Update
        if ("Update".equals(op) || "Save".equals(op)) {
            // Validate input
            ActionMessages errors = myForm.validate(mapping, request);
            if(errors.size()>0) {
                saveErrors(request, errors);
            } else {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
                SolverParameterGroup group = null;

                if(op.equals("Save"))
                	group = new SolverParameterGroup();
                else 
                	group = dao.get(myForm.getUniqueId());
                
                group.setName(myForm.getName());
                group.setDescription(myForm.getDescription());     
                group.setType(myForm.getType());
                if (myForm.getOrder()<0) {
                	group.setOrder(new Integer(dao.findAll().size()));
                }
                dao.saveOrUpdate(group);
                
                myForm.reset(mapping, request);
            }
        }

        // Edit
        if ("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
            	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
            	SolverParameterGroup group = dao.get(new Long(id));
                if(group==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                } else {
                    myForm.setUniqueId(group.getUniqueId());
                    myForm.setName(group.getName());
                    myForm.setOrder(group.getOrder().intValue());
                    myForm.setType(group.getType());
                    myForm.setDescription(group.getDescription());
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
    	    myForm.reset(mapping, request);
        }
        
        if ("List".equals(myForm.getOp())) {
            //Read all existing settings and store in request
            getSolverParameterGroups(request);
            return mapping.findForward("list");
        }
            
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
	}
	
    private void getSolverParameterGroups(HttpServletRequest request) throws Exception {
		Transaction tx = null;
		
		WebTable.setOrder(sessionContext,"solverParamGroups.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 3,
			    null, "solverParamGroups.do?ord=%%",
			    new String[] {"Order", "Name", "Type", "Description"},
			    new String[] {"left", "left", "left", "left"},
			    null );
        int size = 0;

        try {
        	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
			org.hibernate.Session hibSession = dao.getSession();
    		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    			tx = hibSession.beginTransaction();
            
			List list = hibSession.createCriteria(SolverParameterGroup.class).addOrder(Order.asc("order")).list();
			size = list.size();
			
			if(list.isEmpty()) {
			    webTable.addLine(null, new String[] {"No solver parameter group defined."}, null, null );			    
			} else {
				for (Iterator i=list.iterator();i.hasNext();) {
					SolverParameterGroup group = (SolverParameterGroup)i.next();
					String onClick = "onClick=\"document.location='solverParamGroups.do?op=Edit&id=" + group.getUniqueId() + "';\"";
		            String ops = "";
		            if (group.getOrder().intValue()>0) {
		                ops += "<img src='images/arrow_up.png' border='0' align='absmiddle' title='Move Up' " +
		                        "onclick=\"solverParamGroupsForm.op2.value='Move Up';solverParamGroupsForm.uniqueId.value='"+group.getUniqueId()+"';solverParamGroupsForm.submit(); event.cancelBubble=true;\">";
		            } else
		                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
		            if (i.hasNext()) {
		                ops += "<img src='images/arrow_down.png' border='0' align='absmiddle' title='Move Down' " +
		                        "onclick=\"solverParamGroupsForm.op2.value='Move Down';solverParamGroupsForm.uniqueId.value='"+group.getUniqueId()+"';solverParamGroupsForm.submit(); event.cancelBubble=true;\">";
		            } else
		                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
					webTable.addLine(onClick, new String[] {ops, group.getName(), (group.getType()==SolverParameterGroup.sTypeStudent?"student":group.getType()==SolverParameterGroup.sTypeCourse?"course":"exam"), group.getDescription()},
							new Comparable[] {group.getOrder(), group.getName(), group.getType(), group.getDescription()});
				}
			}
			
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;
	    }

	    request.setAttribute("SolverParameterGroup.table", webTable.printTable(WebTable.getOrder(sessionContext,"solverParamGroups.ord")));
	    request.setAttribute("SolverParameterGroup.last", new Integer(size-1));
    }	

}

