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
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.form.DeptStatusTypeEditForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.security.rights.Right;

/** 
 * @author Tomas Muller
 */
@Action(value="deptStatusTypeEdit", results = {
		@Result(name = "list", type = "tiles", location = "deptStatusTypes.tiles"),
		@Result(name = "add", type = "tiles", location = "deptStatusTypeAdd.tiles"),
		@Result(name = "edit", type = "tiles", location = "deptStatusTypeEdit.tiles")
	})
@TilesDefinitions(value = {
		@TilesDefinition(name = "deptStatusTypes.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Status Types"),
				@TilesPutAttribute(name = "body", value = "/admin/deptStatusTypeEdit.jsp")
		}),
		@TilesDefinition(name = "deptStatusTypeAdd.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Add Status Type"),
				@TilesPutAttribute(name = "body", value = "/admin/deptStatusTypeEdit.jsp")
		}),
		@TilesDefinition(name = "deptStatusTypeEdit.tiles", extend = "baseLayout", putAttributes =  {
				@TilesPutAttribute(name = "title", value = "Edit Status Type"),
				@TilesPutAttribute(name = "body", value = "/admin/deptStatusTypeEdit.jsp")
		})
	})
public class DeptStatusTypeEditAction extends UniTimeAction<DeptStatusTypeEditForm> {
	private static final long serialVersionUID = -8502240944652360463L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	private Long id;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new DeptStatusTypeEditForm();
		
        // Check Access
		sessionContext.checkPermission(Right.StatusTypes);
        
        // Read operation to be performed
		if (op == null) op = form.getOp();

        if (op==null) {
            form.reset();
        }
        
        // Reset Form
        if (MSG.actionBackToStatusTypes().equals(op)) {
            form.reset();
        }
        
        if (MSG.actionAddStatusType().equals(op)) {
            form.load(null);
        }

        // Add / Update
        if (MSG.actionUpdateStatusType().equals(op) || MSG.actionSaveStatusType().equals(op)) {
            // Validate input
            form.validate(this);
            if (hasFieldErrors()) {
            	form.setOp(op);
            } else {
        		Transaction tx = null;
        		
                try {
                	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
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
        	if (id==null ) {
            	addFieldError("form.uniqueId", MSG.errorRequiredField(MSG.fieldId()));
            } else {
                DepartmentStatusType s = DepartmentStatusTypeDAO.getInstance().get(id);
                if(s == null) {
                	addFieldError("form.uniqueId", MSG.errorDoesNotExists(id.toString()));
                } else {
                	form.load(s);
                }
            }
        }

        // Delete 
        if(MSG.actionDeleteStatusType().equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	form.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    form.reset();
        }
        
        // Move Up or Down
        if ("Move Up".equals(op) || "Move Down".equals(op)) {
            Transaction tx = null;
            
            try {
                org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    tx = hibSession.beginTransaction();
                
                DepartmentStatusType curStatus = (new DepartmentStatusTypeDAO()).get(id);
                
                if ("Move Up".equals(op)) {
                    boolean found = false;
                    for (Iterator i=DepartmentStatusType.findAll().iterator();i.hasNext();) {
                        DepartmentStatusType s = (DepartmentStatusType)i.next();
                        if (s.getOrd()+1==curStatus.getOrd()) {
                            s.setOrd(s.getOrd()+1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curStatus.setOrd(curStatus.getOrd()-1);
                        form.setOrder(curStatus.getOrd());
                        hibSession.saveOrUpdate(curStatus);
                    }
                } else {
                    boolean found = false;
                    for (Iterator i=DepartmentStatusType.findAll().iterator();i.hasNext();) {
                        DepartmentStatusType s = (DepartmentStatusType)i.next();
                        if (s.getOrd()-1==curStatus.getOrd()) {
                            s.setOrd(s.getOrd()-1); 
                            hibSession.saveOrUpdate(s);
                            found = true;
                        }
                    }
                    if (found) {
                        curStatus.setOrd(curStatus.getOrd()+1);
                        form.setOrder(curStatus.getOrd());
                        hibSession.saveOrUpdate(curStatus);
                    }
                }
                
                if (tx!=null) tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                Debug.error(e);
            }
            form.reset();
        }

        if ("List".equals(form.getOp())) {
            // Read all existing settings and store in request
            return "list";
        }
        
        return (form.getUniqueId() == null || form.getUniqueId() < 0 ? "add" : "edit");
	}
	
    public String getTable() {
		WebTable.setOrder(sessionContext,"deptStatusTypes.ord",request.getParameter("ord"),2);
		// Create web table instance 
        WebTable webTable = new WebTable( 5,
			    null, "deptStatusTypeEdit.action?ord=%%",
			    new String[] {
                "",
                MSG.fieldReference(),
                MSG.fieldLabel(),
                MSG.fieldApply(),
                MSG.fieldRights()},
			    new String[] {"left","left", "left","left", "left"},
			    null );
        
        TreeSet statuses = DepartmentStatusType.findAll();
		if(statuses.isEmpty()) {
		    webTable.addLine(null, new String[] {MSG.infoNoStatusTypes()}, null, null );			    
		}
		
		int ord = 0;
        for (Iterator i=statuses.iterator();i.hasNext();) {
        	DepartmentStatusType s = (DepartmentStatusType)i.next();
        	if (ord != s.getOrd()) {
        		s.setOrd(ord);
        		DepartmentStatusTypeDAO.getInstance().saveOrUpdate(s);
        	}
        	ord ++;
        	String onClick = "onClick=\"document.location='deptStatusTypeEdit.action?op=Edit&id=" + s.getUniqueId() + "';\"";
        	String rights = "";
            String apply = "";
            if (s.applyDepartment()) {
                if (s.applySession()) {
                	if (s.applyExamStatus())
                		apply = MSG.applyToAll();
                	else
                		apply = MSG.applyToSessionAndDepartment().replace("&", "&amp;");
                } else
                    apply = MSG.applyToDepartment();
            } else if (s.applySession())
                apply = MSG.applyToSession();
            else if (s.applyExamStatus())
            	apply = MSG.applyToExaminations();
            if (s.isAllowRollForward()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightRollFoward();
            }
            if (s.isInstructorSurveyEnabled()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightInstructorSurvey();
            }
            if (s.canOwnerView() || s.canOwnerLimitedEdit() || s.canOwnerEdit()) {
                if (rights.length()>0) rights+="; ";
                if (s.canOwnerView() && s.canOwnerEdit())
                    rights += MSG.rightOwnerCan(MSG.rightViewAndEdit()); 
                else {
                	String r = null;
                    if (s.canOwnerView())
                    	r = MSG.rightView();
                    if (s.canOwnerEdit()) {
                        if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canOwnerLimitedEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightLimitedEdit());
                    	else r = MSG.rightLimitedEdit();
                    }
                    rights += MSG.rightOwnerCan(r);
                }
            }
            if (s.canManagerView() || s.canManagerLimitedEdit() || s.canManagerEdit()) {
                if (rights.length()>0) rights+="; ";
                if (s.canManagerView() && s.canManagerEdit())
                    rights +=  MSG.rightManagerCan(MSG.rightViewAndEdit());
                else {
                	String r = null;
                    if (s.canManagerView())
                    	r = MSG.rightView();
                    if (s.canManagerEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canManagerLimitedEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightLimitedEdit());
                    	else r = MSG.rightLimitedEdit();
                    }
                    rights += MSG.rightManagerCan(r);
                }
            }
            if (s.canAudit()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightAudit();
            }
            if (s.canTimetable()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightTimetable();
            } 
            if (s.canCommit()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightCommit();
            }
            if (s.canExamView() || s.canExamEdit() || s.canExamTimetable()) {
                if (rights.length()>0) rights+="; ";
                if (s.canExamEdit() && s.canExamTimetable())
                    rights += MSG.rightExam(MSG.rightEditAndTimetable());
                else {
                	String r = null;
                    if (s.canExamView())
                        r = MSG.rightView();
                    if (s.canExamEdit()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightEdit());
                        else r = MSG.rightEdit();
                    } else if (s.canExamTimetable()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightTimetable());
                        else r = MSG.rightTimetable();
                    }
                    rights += MSG.rightExam(r);
                }
            }
            if (s.canOnlineSectionStudents()) {
            	if (rights.length()>0) rights+="; ";
            	rights += MSG.rightSectioning();
            } else if (s.canSectionAssistStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightAssitant();
            } else if (s.canPreRegisterStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += MSG.rightRegistration();
            }
            if (s.isEventManagement()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightEvents();
            }
            if (s.isAllowNoRole() || s.canNoRoleReportExamFinal() || s.canNoRoleReportExamMidterm() || s.canNoRoleReportClass()) {
                if (rights.length()>0) rights+="; ";
                if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm() && s.canNoRoleReportClass())
                    rights += MSG.rightNoRoleCan(MSG.rightSeeAllEvents()) ;
                else {
                	String r = null;
                    if (s.canNoRoleReportClass()) r = MSG.rightSeeClasses();
                    if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm()) {
                    	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeExams());
                        else r = MSG.rightSeeExams();
                    } else {
                        if (s.canNoRoleReportExamFinal()) {
                        	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeFinalExams());
                            else r = MSG.rightSeeFinalExams();
                        }
                        if (s.canNoRoleReportExamMidterm()) {
                        	if (r != null) r = MSG.rightAnd(r, MSG.rightSeeMidtermExams());
                            else r = MSG.rightSeeMidtermExams();
                        }
                    }
                    if (r == null) r = "";
                    rights += MSG.rightNoRoleCan(r).trim();
                }
            }
            if (s.isTestSession()) {
            	if (rights.length()>0) rights+="; ";
                rights += MSG.rightTestSession();
            }
            String ops = "";
            if (s.getOrd().intValue()>0) {
                ops += "<img src='images/arrow_up.png' border='0' align='absmiddle' title='Move Up' " +
                		"onclick=\"document.getElementById('op').value='Move Up';document.getElementById('id').value='"+s.getUniqueId()+"'; submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
            if (i.hasNext()) {
                ops += "<img src='images/arrow_down.png' border='0' align='absmiddle' title='Move Down' " +
                		"onclick=\"document.getElementById('op').value='Move Down';document.getElementById('id').value='"+s.getUniqueId()+"'; submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.png' border='0' align='absmiddle'>";
            webTable.addLine(onClick, new String[] {
                    ops,
                    s.getReference(),
                    s.getLabel(),
                    apply,
                    rights,
        		},new Comparable[] {
                    s.getOrd(),
        			s.getOrd(),
                    s.getLabel(),
                    s.getApply(),
                    s.getStatus(),
                    
        		});
        }
        
        // request.setAttribute("DeptStatusType.last", Integer.valueOf(statuses.size()-1));
	    return webTable.printTable(WebTable.getOrder(sessionContext,"deptStatusTypes.ord"));
    }
    
    public int getLast() {
    	return DepartmentStatusType.findAll().size() - 1;    	
    }
}

