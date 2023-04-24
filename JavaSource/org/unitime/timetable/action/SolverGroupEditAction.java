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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.springframework.web.util.HtmlUtils;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.SolverGroupEditForm;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.webutil.PdfWebTable;


/** 
 * @author Tomas Muller
 */
@Action(value="solverGroupEdit", results = {
		@Result(name = "list", type = "tiles", location = "solverGroups.tiles"),
		@Result(name = "add", type = "tiles", location = "solverGroupAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "solverGroupEdit.tiles")
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "solverGroups.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Solver Groups"),
				@TilesPutAttribute(name = "body", value = "/admin/solverGroups.jsp")
		}),
		@TilesDefinition(name = "solverGroupAdd.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Add Solver Group"),
				@TilesPutAttribute(name = "body", value = "/admin/solverGroups.jsp")
		}),
		@TilesDefinition(name = "solverGroupEdit.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Edit Solver Group"),
				@TilesPutAttribute(name = "body", value = "/admin/solverGroups.jsp")
		})
	})
public class SolverGroupEditAction extends UniTimeAction<SolverGroupEditForm> {
	private static final long serialVersionUID = -5571247262159758529L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new SolverGroupEditForm();
		
        // Check Access
		sessionContext.checkPermission(Right.SolverGroups);
        	        
        // Read operation to be performed
		if (op == null) op = form.getOp();

        if (op==null || MSG.actionBackToSolverGroups().equals(op)) {
            form.setOp("List");
            if (form.getUniqueId()!=null)
                request.setAttribute("hash", form.getUniqueId());
        }
        
        Session session = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId());
        
        // Add / Update
        if (MSG.actionUpdateSolverGroup().equals(op) || MSG.actionSaveSolverGroup().equals(op)) {
            // Validate input
            form.validate(this);
            if (!hasFieldErrors()) {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (SolverGroupDAO.getInstance()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	form.saveOrUpdate(hibSession, sessionContext);
                	
        			if (tx!=null) tx.commit();
        			
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                form.setOp("List");
                if (form.getUniqueId()!=null)
                    request.setAttribute("hash", form.getUniqueId());
            }
        }

        // Edit
        if ("Edit".equals(op)) {
            if (id==null ) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField(MSG.fieldId()));
                return "edit";
            } else {
            	SolverGroup group = SolverGroupDAO.getInstance().get(id);
                if (group==null) {
                	addFieldError("form.uniqueId", MSG.errorDoesNotExists(id.toString()));
                    return "edit";
                } else {
                	form.load(group, session, getNameFormat());
                }
            }
        }

        // Delete 
        if (MSG.actionDeleteSolverGroup().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (SolverGroupDAO.getInstance()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	form.delete(hibSession, sessionContext);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

            form.setOp("List");
        }
        
        if (MSG.actionAddSolverGroup().equals(op)) {
        	form.load(null, session, getNameFormat());
        }
        
        if (MSG.actionDeleteAllSolverGroups().equals(op)) {
	    		Transaction tx = null;
	    		
	            try {
	            	org.hibernate.Session hibSession = (SolverGroupDAO.getInstance()).getSession();
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

            form.setOp("List");
        	
        }
        
        if (MSG.actionAutoSetupSolverGroups().equals(op)) {
	    		Transaction tx = null;
	    		
	            try {
	            	org.hibernate.Session hibSession = (SolverGroupDAO.getInstance()).getSession();
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
        	
            form.setOp("List");
        }
        
        if (MSG.actionExportPdf().equals(op)) {
        	ExportUtils.exportPDF(
        			getSolverGroups(session, false),
        			WebTable.getOrder(sessionContext, "solverGroups.ord"),
        			response, "solverGroups");
        	return null;
        }
        
        if (MSG.actionExportCsv().equals(op)) {
        	ExportUtils.exportCSV(
        			getSolverGroups(session, false),
        			WebTable.getOrder(sessionContext, "solverGroups.ord"),
        			response, "solverGroups");
        	return null;
        }

        // Read all existing settings and store in request
        if ("List".equals(form.getOp())) {
            PdfWebTable table = getSolverGroups(session, true);
            request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"solverGroups.ord")));
            return "list";
        }
        
        return (form.getUniqueId() == null || form.getUniqueId() < 0 ? "add" : "edit");
	}
	
    private PdfWebTable getSolverGroups(Session session, boolean html) throws Exception {
		WebTable.setOrder(sessionContext,"solverGroups.ord",request.getParameter("ord"),1);
		// Create web table instance 
        PdfWebTable webTable = new PdfWebTable( 5,
			    (html?null:MSG.sectSolverGroupsForSession(session.getLabel())),
                "solverGroupEdit.action?ord=%%",
			    new String[] {
			    		MSG.fieldAbbv(),
			    		MSG.fieldName(),
			    		MSG.fieldDepartments(),
			    		MSG.fieldManagers(),
			    		MSG.fieldCommitted()},
			    new String[] {"left", "left", "left", "left", "left"},
			    null );
        
        Set<SolverGroup> solverGroups = SolverGroup.findBySessionId(session.getUniqueId());
		if(solverGroups.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.infoNoSolverGroupInThisSession()}, null, null );			    
		}

		org.hibernate.Session hibSession = (SolverGroupDAO.getInstance()).getSession();
        for (Iterator<SolverGroup> i=solverGroups.iterator();i.hasNext();) {
        	SolverGroup group = i.next();
        	if (group.getDepartments()==null || group.getTimetableManagers()==null)
        		hibSession.refresh(group);
        	String onClick = "onClick=\"document.location='solverGroupEdit.action?op=Edit&id=" + group.getUniqueId() + "';\"";
        	String deptStr = "";
        	String deptCmp = "";
        	for (Iterator j=(new TreeSet(group.getDepartments())).iterator();j.hasNext();) {
        		Department d = (Department)j.next();
        		deptStr += (html?"<span title='"+HtmlUtils.htmlEscape(d.getLabel())+"'>"+d.getDeptCode()+"</span>":d.getDeptCode());
        		deptCmp += d.getDeptCode();
        		if (j.hasNext()) { deptStr += ", "; deptCmp += ","; }
        	}
        	String mgrStr = "";
        	String mgrCmp = "";
        	List<TimetableManager> managers = new ArrayList<TimetableManager>(group.getTimetableManagers());
            Collections.sort(managers, new Comparator<TimetableManager>() {
    			@Override
    			public int compare(TimetableManager m1, TimetableManager m2) {
    				int cmp = m1.getName(nameFormat).compareToIgnoreCase(m2.getName(nameFormat));
    				if (cmp != 0) return cmp;
    				return m1.compareTo(m2);
    			}
    		});
        	for (Iterator<TimetableManager> j=managers.iterator(); j.hasNext();) {
        		TimetableManager mgr = j.next();
        		String name = mgr.getName(getNameFormat());
        		String depts = "";
	        	for (Department d: new TreeSet<Department>(mgr.departmentsForSession(session.getUniqueId()))) {
	        		depts += (depts.isEmpty() ? "" : ", ") + d.getDeptCode();
	        	}
        		mgrStr += (html ? "<span title=\"" + HtmlUtils.htmlEscape(name + (depts.isEmpty() ? "" : " (" + depts + ")")) + "\">" + name + "</span>" : name);
        		mgrCmp += name;
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
        			Long.valueOf(commitDate==null?-1:commitDate.getTime())
        		});
        }
        
	    return webTable;
    }
    
    private String nameFormat = null;
    public String getNameFormat() {
    	if (nameFormat == null)
    		nameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
    	return nameFormat;
    }
    
    public String getTitle() {
    	return MSG.sectSolverGroupsForSession(sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel()); 
    }
}
