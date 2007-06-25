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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.SolverGroupEditForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
public class SolverGroupEditAction extends Action {
	private static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			SolverGroupEditForm myForm = (SolverGroupEditForm) form;
			
	        // Check Access
	        if (!Web.isLoggedIn( request.getSession() )
	               || !Web.hasRole(request.getSession(), Roles.getAdminRoles()) ) {
	            throw new Exception ("Access Denied.");
	        }
	        
	        User user = Web.getUser(request.getSession());
	        Session session = Session.getCurrentAcadSession(user);
	        
	        // Read operation to be performed
	        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

	        if (op==null || "Back".equals(op)) {
	            myForm.setOp("List");
	        }
	        
	        // Add / Update
	        if ("Update".equals(op) || "Create".equals(op)) {
	            // Validate input
	            ActionMessages errors = myForm.validate(mapping, request);
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	            } else {
	        		Transaction tx = null;
	        		
	                try {
	                	org.hibernate.Session hibSession = (new SolverGroupDAO()).getSession();
	                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	                		tx = hibSession.beginTransaction();
	                	
	                	myForm.saveOrUpdate(hibSession, session.getUniqueId(), request);
	                	
	        			if (tx!=null) tx.commit();
	        	    } catch (Exception e) {
	        	    	if (tx!=null) tx.rollback();
	        	    	throw e;
	        	    }

	                myForm.setOp("List");
	            }
	        }

	        // Edit
	        if("Edit".equals(op)) {
	            String id = request.getParameter("id");
	            ActionMessages errors = new ActionMessages();
	            if(id==null || id.trim().length()==0) {
	                errors.add("key", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                saveErrors(request, errors);
	                return mapping.findForward("showSolverGroups");
	            } else {
	            	SolverGroup group = (new SolverGroupDAO()).get(Long.valueOf(id));
	            	
	                if(group==null) {
	                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                    saveErrors(request, errors);
	                    return mapping.findForward("showSolverGroups");
	                } else {
	                	myForm.load(group, session);
	                }
	            }
	        }

	        // Delete 
	        if("Delete".equals(op)) {
	    		Transaction tx = null;
	    		
	            try {
	            	org.hibernate.Session hibSession = (new SolverGroupDAO()).getSession();
	            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
	            		tx = hibSession.beginTransaction();
	            	
	            	myForm.delete(hibSession, request);
	            	
	    			tx.commit();
	    	    } catch (Exception e) {
	    	    	if (tx!=null) tx.rollback();
	    	    	throw e;
	    	    }

	            myForm.setOp("List");
	        }
	        
	        if ("Add New".equals(op)) {
	        	myForm.load(null, session);
	        }
	        
	        if ("Delete All".equals(op)) {
	        	if ("1".equals(request.getParameter("sure"))) {
		    		Transaction tx = null;
		    		
		            try {
		            	org.hibernate.Session hibSession = (new SolverGroupDAO()).getSession();
		            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
		            		tx = hibSession.beginTransaction();
		            	
		            	for (Iterator i=SolverGroup.findBySessionId(session.getUniqueId()).iterator();i.hasNext();) {
		            		SolverGroup group = (SolverGroup)i.next();
		            		if (!group.getSolutions().isEmpty()) continue;
		            		for (Iterator j=group.getDepartments().iterator();j.hasNext();) {
		            			Department dept = (Department)j.next();
		            			dept.setSolverGroup(null);
		            			hibSession.saveOrUpdate(dept);
		            		}
		            		for (Iterator j=group.getTimetableManagers().iterator();j.hasNext();) {
		            			TimetableManager mgr = (TimetableManager)j.next();
		            			mgr.getSolverGroups().remove(group);
		            			hibSession.saveOrUpdate(mgr);
		            		}
		            		hibSession.delete(group);
		            	}
		            	
		    			tx.commit();
		    	    } catch (Exception e) {
		    	    	if (tx!=null) tx.rollback();
		    	    	throw e;
		    	    }
	        	}

	            myForm.setOp("List");
	        	
	        }
	        
	        if ("Auto Setup".equals(op)) {
	        	if ("1".equals(request.getParameter("sure"))) {
		    		Transaction tx = null;
		    		
		            try {
		            	org.hibernate.Session hibSession = (new SolverGroupDAO()).getSession();
		            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
		            		tx = hibSession.beginTransaction();
		            	
		            	TreeSet allDepts = new TreeSet(new Comparator() {
		            		public int compare(Object o1, Object o2) {
		            			Department d1 = (Department)o1;
		            			Department d2 = (Department)o2;
		            			int cmp = -Double.compare(d1.getTimetableManagers().size(),d2.getTimetableManagers().size());
		            			if (cmp!=0) return cmp;
		            			return d1.getUniqueId().compareTo(d2.getUniqueId());
		            		}
		            	});
		            	allDepts.addAll(session.getDepartments());
		            	
		            	for (Iterator i=allDepts.iterator();i.hasNext();) {
		            		Department d = (Department)i.next();
		            		if (d.getSolverGroup()!=null) continue;
		            		if (d.isExternalManager().booleanValue()) {
		            			SolverGroup sg = new SolverGroup();
		            			sg.setAbbv(d.getExternalMgrAbbv());
		            			sg.setName(d.getDeptCode()+" - "+d.getExternalMgrLabel().replaceAll(" Manager", ""));
		            			sg.setSession(session);
		            			hibSession.saveOrUpdate(sg);
		            			d.setSolverGroup(sg);
		            			hibSession.saveOrUpdate(d);
	            				for (Iterator j=d.getTimetableManagers().iterator();j.hasNext();) {
	            					TimetableManager mgr = (TimetableManager)j.next();
	            					mgr.getSolverGroups().add(sg);
	            					hibSession.saveOrUpdate(mgr);
	            				}
		            		} else if (!d.getSubjectAreas().isEmpty() && !d.getTimetableManagers().isEmpty()) {
		            			Set depts = null;
		            			for (Iterator j=d.getTimetableManagers().iterator();j.hasNext();) {
		            				TimetableManager mgr = (TimetableManager)j.next();
		            				Set myDepts = mgr.departmentsForSession(session.getUniqueId());
		            				if (depts==null) 
		            					depts = new HashSet(myDepts);
		            				else
		            					depts.retainAll(myDepts);
		            			}
	            				for (Iterator j=depts.iterator();j.hasNext();) {
	            					Department x = (Department)j.next();
	            					if (x.getSolverGroup()!=null || x.getSubjectAreas().isEmpty())
	            						j.remove();
	            				}
		            			if (!depts.isEmpty()) {
		            				StringBuffer abbv = new StringBuffer();
		            				StringBuffer name = new StringBuffer();
		            				HashSet mgrs = new HashSet();
		            				for (Iterator j=depts.iterator();j.hasNext();) {
		            					Department x = (Department)j.next();
		            					mgrs.addAll(x.getTimetableManagers());
		            					abbv.append(x.getShortLabel().trim());
		            					if (name.length()>0) name.append(", ");
		            					name.append(x.getLabel());
		            				}
		            				SolverGroup sg = new SolverGroup();
		            				sg.setAbbv(abbv.length()<=10?abbv.toString():abbv.toString().substring(0,10));
		            				sg.setName(name.length()<=50?name.toString():name.toString().substring(0,47)+"...");
		            				sg.setSession(session);
		            				hibSession.saveOrUpdate(sg);
		            				for (Iterator j=depts.iterator();j.hasNext();) {
		            					Department x = (Department)j.next();
		            					x.setSolverGroup(sg);
		            					hibSession.saveOrUpdate(x);
		            				}
		            				for (Iterator j=mgrs.iterator();j.hasNext();) {
		            					TimetableManager mgr = (TimetableManager)j.next();
		            					mgr.getSolverGroups().add(sg);
		            					hibSession.saveOrUpdate(mgr);
		            				}
		            			}
		            		}
		            	}
		            	
		    			tx.commit();
		    	    } catch (Exception e) {
		    	    	if (tx!=null) tx.rollback();
		    	    	throw e;
		    	    }
	        	}
	        	
	            myForm.setOp("List");
	        }
            
            if ("Export PDF".equals(op)) {
                PdfWebTable table = getSolverGroups(request, session.getUniqueId(), false);
                File file = ApplicationProperties.getTempFile("solverGroups", "pdf");
                table.exportPdf(file, WebTable.getOrder(request.getSession(), "solverGroups.ord"));
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
                myForm.setOp("List");
            }

	        // Read all existing settings and store in request
	        if ("List".equals(myForm.getOp())) {
                PdfWebTable table = getSolverGroups(request, session.getUniqueId(), true);
                request.setAttribute("SolverGroups.table", table.printTable(WebTable.getOrder(request.getSession(),"solverGroups.ord")));
            }
	        
	        return mapping.findForward("showSolverGroups");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private PdfWebTable getSolverGroups(HttpServletRequest request, Long sessionId, boolean html) throws Exception {
		WebTable.setOrder(request.getSession(),"solverGroups.ord",request.getParameter("ord"),1);
		// Create web table instance 
        PdfWebTable webTable = new PdfWebTable( 5,
			    (html?null:"Solver Groups - "+Web.getUser(request.getSession()).getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME)),
                "solverGroupEdit.do?ord=%%",
			    new String[] {"Abbv", "Name", "Departments", "Managers", "Committed"},
			    new String[] {"left", "left", "left", "left", "left"},
			    null );
        
        Set solverGroups = SolverGroup.findBySessionId(sessionId);
		if(solverGroups.isEmpty()) {
		    webTable.addLine(null, new String[] {"No time pattern defined for this academic initiative and term."}, null, null );			    
		}

		org.hibernate.Session hibSession = (new SolverGroupDAO()).getSession();
        for (Iterator i=solverGroups.iterator();i.hasNext();) {
        	SolverGroup group = (SolverGroup)i.next();
        	if (group.getDepartments()==null || group.getTimetableManagers()==null)
        		hibSession.refresh(group);
        	String onClick = "onClick=\"document.location='solverGroupEdit.do?op=Edit&id=" + group.getUniqueId() + "';\"";
        	String deptStr = "";
        	String deptCmp = "";
        	for (Iterator j=(new TreeSet(group.getDepartments())).iterator();j.hasNext();) {
        		Department d = (Department)j.next();
        		deptStr += (html?"<span title='"+d.getDeptCode()+" - "+d.getName()+"'>"+d.getDeptCode()+"</span>":d.getDeptCode());
        		deptCmp += d.getDeptCode();
        		if (j.hasNext()) { deptStr += ", "; deptCmp += ","; }
        	}
        	String mgrStr = "";
        	String mgrCmp = "";
        	for (Iterator j=(new TreeSet(group.getTimetableManagers())).iterator();j.hasNext();) {
        		TimetableManager mgr = (TimetableManager)j.next();
        		mgrStr += (html?"<span title='"+mgr.getName()+"'>"+mgr.getShortName()+"</span>":mgr.getShortName());
        		mgrCmp += mgr.getLastName();
        		if (j.hasNext()) { mgrStr += ", "; mgrCmp += ","; }
        	}
        	
        	Date commitDate = null;
        	if (group.getCommittedSolution()!=null)
        		commitDate = group.getCommittedSolution().getCommitDate();
        	
        	webTable.addLine(onClick, new String[] {
        			(html?group.getAbbv().replaceAll(" ","&nbsp;"):group.getAbbv()),
        			(html?group.getName().replaceAll(" ","&nbsp;"):group.getName()),
        			deptStr,
        			mgrStr,
        			(commitDate==null?"":sDF.format(commitDate))
        		},new Comparable[] {
        			group.getAbbv(),
        			group.getName(),
        			deptCmp,
        			mgrCmp,
        			new Long(commitDate==null?-1:commitDate.getTime())
        		});
        }
        
	    return webTable;
    }	
}
