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
import org.unitime.timetable.form.SolverInfoDefForm;
import org.unitime.timetable.model.SolverInfoDef;
import org.unitime.timetable.model.dao.SolverInfoDefDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/solverInfoDef")
public class SolverInfoDefAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverInfoDefForm myForm = (SolverInfoDefForm) form;
        // Check Access
		sessionContext.checkPermission(Right.SolutionInformationDefinitions);
        
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
                mapping.findForward("showSolverInfoDef");
            } else {
            	SolverInfoDefDAO dao = new SolverInfoDefDAO();
            	SolverInfoDef info = null;

                if(op.equals("Add New"))
                	info = new SolverInfoDef();
                else 
                	info = dao.get(myForm.getUniqueId());
                
                info.setName(myForm.getName());
                info.setDescription(myForm.getDescription());                
                info.setImplementation(myForm.getImplementation());
                dao.saveOrUpdate(info);
                
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
                mapping.findForward("showSolverInfoDef");
            }
            else {
            	SolverInfoDefDAO dao = new SolverInfoDefDAO();
            	SolverInfoDef info = dao.get(new Long(id));
                if(info==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                    mapping.findForward("showSolverInfoDef");
                }
                else {
                    myForm.setUniqueId(info.getUniqueId());
                    myForm.setName(info.getName());
                    myForm.setDescription(info.getDescription());
                    myForm.setImplementation(info.getImplementation());
                    myForm.setOp("Update");
                }                
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	SolverInfoDefDAO dao = new SolverInfoDefDAO();
            	org.hibernate.Session hibSession = dao.getSession();
        		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
        			tx = hibSession.beginTransaction();
    			
    			SolverInfoDef info = dao.get(myForm.getUniqueId(), hibSession);
    			dao.delete(info, hibSession);
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
            myForm.reset(mapping, request);
            myForm.setOp("Add New");
        }
        
        // Read all existing settings and store in request
        getSolverInfoDefs(request);        
        return mapping.findForward("showSolverInfoDef");
	}
	
    private void getSolverInfoDefs(HttpServletRequest request) throws Exception {
		Transaction tx = null;
		
		WebTable.setOrder(sessionContext,"solverInfoDef.ord",request.getParameter("ord"),1);
		// Create web table instance 
        WebTable webTable = new WebTable( 3,
			    "Solution Info Definitions", "solverInfoDef.do?ord=%%",
			    new String[] {"Name", "Description", "Implementation"},
			    new String[] {"left", "left", "left"},
			    null );

        try {
        	SolverInfoDefDAO dao = new SolverInfoDefDAO();
        	org.hibernate.Session hibSession = dao.getSession();
    		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    			tx = hibSession.beginTransaction();
            
			List list = hibSession.createCriteria(SolverInfoDef.class).list();
			
			if(list.isEmpty()) {
			    webTable.addLine(null, new String[] {"No solution info defined."}, null, null );			    
			} else {
				for (Iterator i=list.iterator();i.hasNext();) {
					SolverInfoDef info = (SolverInfoDef)i.next();
					String onClick = "onClick=\"document.location='solverInfoDef.do?op=Edit&id=" + info.getUniqueId() + "';\"";
					webTable.addLine(onClick, new String[] {
							info.getName(), 
							info.getDescription(), 
							info.getImplementation()},
						new Comparable[] {
							info.getName(), 
							info.getDescription(), 
							info.getImplementation()});
				}
			}
			
			if (tx!=null) tx.commit();
	    }
	    catch (Exception e) {
	    	if (tx!=null) tx.rollback();
	    	throw e;
	    }

	    request.setAttribute("SolverInfoDef.table", webTable.printTable(WebTable.getOrder(sessionContext,"solverInfoDef.ord")));
    }	

}

