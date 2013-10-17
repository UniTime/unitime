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
import org.unitime.timetable.form.SolverParamDefForm;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverParameterGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/solverParamDef")
public class SolverParamDefAction extends Action {

	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		SolverParamDefForm myForm = (SolverParamDefForm) form;

        // Check Access
		sessionContext.checkPermission(Right.SolverParameters);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");

        if (op==null) {
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            if (myForm.getUniqueId()!=null)
                request.setAttribute("hash", myForm.getUniqueId());
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
        }
        
        if ("Add Solver Parameter".equals(op)) {
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
            myForm.setOp("Save");
            myForm.setGroup(request.getParameter("group"));
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
            		SolverParameterDefDAO dao = new SolverParameterDefDAO();
            		org.hibernate.Session hibSession = dao.getSession();
            		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            			tx = hibSession.beginTransaction();
            		
            		SolverParameterDef def = null;
            		if(op.equals("Save"))
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
                    
                    hibSession.refresh(def);
                    request.setAttribute("hash", def.getUniqueId().toString());
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        			Debug.error(e);
        	    }
            	myForm.reset(mapping, request);
            	myForm.setVisible(Boolean.TRUE);
            }
        }

        // Edit
        if(op.equals("Edit")) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
            	SolverParameterDefDAO dao = new SolverParameterDefDAO();
            	SolverParameterDef def = dao.get(new Long(id));
                if(def==null) {
                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
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
            if (myForm.getGroup()!=null) request.setAttribute("hash", myForm.getGroup());
            myForm.reset(mapping, request);
            myForm.setVisible(Boolean.TRUE);
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
    			
    			if (myForm.getUniqueId()!=null) request.setAttribute("hash", myForm.getUniqueId());
    			
    			if (tx!=null) tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    			Debug.error(e);
    	    }
        }
        if ("List".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            getSolverParameterDefs(request, myForm.getUniqueId());        
            return mapping.findForward("list");
        }
            
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
	}

    private void getSolverParameterDefs(HttpServletRequest request, Long uniqueId) throws Exception {
		Transaction tx = null;
		
		WebTable.setOrder(sessionContext,"solverParamDef.ord",request.getParameter("ord"),1);
		
		StringBuffer tables = new StringBuffer();
        try {
        	SolverParameterGroupDAO dao = new SolverParameterGroupDAO();
        	org.hibernate.Session hibSession = dao.getSession();
    		if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
    			tx = hibSession.beginTransaction();
            
			List groups = dao.findAll(hibSession, Order.asc("order"));
			
			if (groups.isEmpty()) {
				// Create web table instance 
		        WebTable webTable = new WebTable( 5,
					    "Solver Parameters", "solverParamDef.do?ord=%%",
					    new String[] {"Order", "Name", "Description", "Type", "Default"},
					    new String[] {"left", "left", "left", "left", "left"},
					    null );
				webTable.addLine(null, new String[] {"No solver parameter group defined."}, null, null );
				tables.append(webTable.printTable(WebTable.getOrder(sessionContext,"solverParamDef.ord")));
			}
			
			for (Iterator i=groups.iterator();i.hasNext();) {
			    SolverParameterGroup group = (SolverParameterGroup)i.next();
			    if (tables.length()>0) tables.append("<TR><TD colspan='5'>&nbsp;</TD></TR>");
				List parameters = hibSession.createCriteria(SolverParameterDef.class).add(Restrictions.eq("group",group)).addOrder(Order.asc("order")).list();
				if (parameters.isEmpty()) continue;
				String name = "<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>"+
				    "<a name='"+group.getName()+"'>"+
				    "<DIV class='WelcomeRowHeadNoLine'>"+group.getDescription()+"</DIV>"+
				    "</a>"+
				    "</td><td style='padding-bottom: 3px' nowrap>"+
				    "<input type=\"submit\" name=\"op\" accesskey=\"A\" value=\"Add Solver Parameter\" title=\"Create New Solver Parameter (Alt+A)\"" +
				    "onclick=\"solverParamDefForm.group.value='"+group.getName()+"';\">"+
				    "</td></tr></table>";
		        WebTable webTable = new WebTable( 5,
		                name, "solverParamDef.do?ord=%%",
					    new String[] {"Order", "Name", "Description", "Type", "Default"},
					    new String[] {"left", "left", "left", "left",  "left"},
					    null );
		        if (parameters.isEmpty()) {
		        	webTable.addLine(null, new String[] {"No parameter defined in group <i>"+group.getDescription()+"</i>."}, null, null );
		        }
		        for (Iterator j=parameters.iterator();j.hasNext();) {
		        	SolverParameterDef def= (SolverParameterDef)j.next();
                    String ops = "";
                    if (def.getOrder().intValue()>0) {
                        ops += "<img src='images/arrow_u.gif' border='0' align='absmiddle' title='Move Up' " +
                                "onclick=\"solverParamDefForm.op2.value='Move Up';solverParamDefForm.uniqueId.value='"+def.getUniqueId()+"';solverParamDefForm.submit(); event.cancelBubble=true;\">";
                    } else
                        ops += "<img src='images/blank.gif' border='0' align='absmiddle'>";
                    if (j.hasNext()) {
                        ops += "<img src='images/arrow_d.gif' border='0' align='absmiddle' title='Move Down' " +
                                "onclick=\"solverParamDefForm.op2.value='Move Down';solverParamDefForm.uniqueId.value='"+def.getUniqueId()+"';solverParamDefForm.submit(); event.cancelBubble=true;\">";
                    } else
                        ops += "<img src='images/blank.gif' border='0' align='absmiddle'>";
					String onClick = "onClick=\"document.location='solverParamDef.do?op=Edit&id=" + def.getUniqueId() + "';\"";
					webTable.addLine(onClick, new String[] {
							ops,
							(def.isVisible()?"":"<font color='gray'>")+
							    "<a name='"+def.getUniqueId()+"'>"+
							        def.getName()+
							    "</a>"+
							(def.isVisible()?"":"</font>"),
							(def.isVisible()?"":"<font color='gray'>")+
							    def.getDescription()+
							(def.isVisible()?"":"</font>"),
							(def.isVisible()?"":"<font color='gray'>")+
							    def.getType().replaceAll(",", ", ")+
							(def.isVisible()?"":"</font>"),
							(def.isVisible()?"":"<font color='gray'>")+
							    (def.getDefault()==null?
							        "":
							        def.getDefault().length()>50?
							            "<span title='"+def.getDefault()+"'>"+def.getDefault().substring(0,50)+"...</span>":
							            def.getDefault())+
							(def.isVisible()?"":"</font>")
					},
						new Comparable[] {
							def.getOrder(), 
							def.getName(), 
							def.getDescription(),
							def.getType(),
							def.getDefault()});
					if (def.getUniqueId().equals(uniqueId))
						request.setAttribute("SolverParameterDef.last", new Integer(parameters.size()-1));
		        }
		        tables.append(webTable.printTable(WebTable.getOrder(sessionContext,"solverParamDef.ord")));
			}
			
			if (!groups.isEmpty()) {
			    tables.append("\n<TR><TD colspan='5'><DIV class='WelcomeRowHeadBlank'>&nbsp;</DIV></TD></TR>");
			    tables.append("\n<TR><TD colspan='5' align='right'>"+
			            "<input type=\"submit\" name=\"op\" accesskey=\"A\" value=\"Add Solver Parameter\" title=\"Create New Solver Parameter (Alt+A)\">"+
			            "</TD></TR>");
			}
			
			if (tx!=null) tx.commit();
	    } catch (Exception e) {
	    	if (tx!=null) tx.rollback();
    		throw e;
	    }
	    request.setAttribute("SolverParameterDef.table",tables.toString());
    }	
}

