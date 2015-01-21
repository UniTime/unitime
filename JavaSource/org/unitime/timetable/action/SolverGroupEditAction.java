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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.SolverGroupEditForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Service("/solverGroupEdit")
public class SolverGroupEditAction extends Action {
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SessionContext sessionContext;
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			SolverGroupEditForm myForm = (SolverGroupEditForm) form;
			
	        // Check Access
			sessionContext.checkPermission(Right.SolverGroups);
	        	        
	        // Read operation to be performed
	        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));

	        if (op==null || "Back".equals(op)) {
	            myForm.setOp("List");
	            if (myForm.getUniqueId()!=null)
                    request.setAttribute("hash", myForm.getUniqueId());
	        }
	        
	        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
	        
	        // Add / Update
	        if ("Update".equals(op) || "Save".equals(op)) {
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
	                	
	                	myForm.saveOrUpdate(hibSession, sessionContext);
	                	
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
	                return mapping.findForward("edit");
	            } else {
	            	SolverGroup group = (new SolverGroupDAO()).get(Long.valueOf(id));
	            	
	                if(group==null) {
	                    errors.add("name", new ActionMessage("errors.invalid", "Unique Id : " + id));
	                    saveErrors(request, errors);
	                    return mapping.findForward("edit");
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
	            	
	            	myForm.delete(hibSession, sessionContext);
	            	
	    			tx.commit();
	    	    } catch (Exception e) {
	    	    	if (tx!=null) tx.rollback();
	    	    	throw e;
	    	    }

	            myForm.setOp("List");
	        }
	        
	        if ("Add Solver Group".equals(op)) {
	        	myForm.load(null, session);
	        }
	        
	        if ("Delete All".equals(op)) {
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

	            myForm.setOp("List");
	        	
	        }
	        
	        if ("Auto Setup".equals(op)) {
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
		            			sg.setTimetableManagers(new HashSet<TimetableManager>());
		            			hibSession.saveOrUpdate(sg);
		            			d.setSolverGroup(sg);
		            			hibSession.saveOrUpdate(d);
	            				for (Iterator j=d.getTimetableManagers().iterator();j.hasNext();) {
	            					TimetableManager mgr = (TimetableManager)j.next();
	            					mgr.getSolverGroups().add(sg);
	            					sg.getTimetableManagers().add(mgr);
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
		            				sg.setTimetableManagers(new HashSet<TimetableManager>());
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
		            					sg.getTimetableManagers().add(mgr);
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
	        	
	            myForm.setOp("List");
	        }
            
            if ("Export PDF".equals(op)) {
            	ExportUtils.exportPDF(
            			getSolverGroups(request, session, false),
            			WebTable.getOrder(sessionContext, "solverGroups.ord"),
            			response, "solverGroups");
            	return null;
            }

	        // Read all existing settings and store in request
	        if ("List".equals(myForm.getOp())) {
                PdfWebTable table = getSolverGroups(request, session, true);
                request.setAttribute("SolverGroups.table", table.printTable(WebTable.getOrder(sessionContext,"solverGroups.ord")));
                return mapping.findForward("list");
            }
	        
	        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
	        
	        
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private PdfWebTable getSolverGroups(HttpServletRequest request, Session session, boolean html) throws Exception {
		WebTable.setOrder(sessionContext,"solverGroups.ord",request.getParameter("ord"),1);
		// Create web table instance 
        PdfWebTable webTable = new PdfWebTable( 5,
			    (html?null:"Solver Groups - " + session.getLabel()),
                "solverGroupEdit.do?ord=%%",
			    new String[] {"Abbv", "Name", "Departments", "Managers", "Committed"},
			    new String[] {"left", "left", "left", "left", "left"},
			    null );
        
        Set solverGroups = SolverGroup.findBySessionId(session.getUniqueId());
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
        	        (html?"<a name='"+group.getUniqueId()+"'>"+(html?group.getAbbv().replaceAll(" ","&nbsp;"):group.getAbbv())+"</a>":group.getAbbv()),
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
