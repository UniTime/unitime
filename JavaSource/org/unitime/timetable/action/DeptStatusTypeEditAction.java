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
import org.unitime.timetable.form.DeptStatusTypeEditForm;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.dao.DepartmentStatusTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;


/** 
 * @author Tomas Muller
 */
@Service("/deptStatusTypeEdit")
public class DeptStatusTypeEditAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
		DeptStatusTypeEditForm myForm = (DeptStatusTypeEditForm) form;
		
        // Check Access
		sessionContext.checkPermission(Right.StatusTypes);
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");

        if (op==null) {
            myForm.reset(mapping, request);
        }
        
        // Reset Form
        if ("Back".equals(op)) {
            myForm.reset(mapping, request);
        }
        
        if ("Add Status Type".equals(op)) {
            myForm.load(null);
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
                	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
                	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                		tx = hibSession.beginTransaction();
                	
                	myForm.saveOrUpdate(hibSession);
                	
        			if (tx!=null) tx.commit();
        	    } catch (Exception e) {
        	    	if (tx!=null) tx.rollback();
        	    	throw e;
        	    }

                myForm.reset(mapping, request);
            }
        }

        // Edit
        if("Edit".equals(op)) {
            String id = request.getParameter("id");
            ActionMessages errors = new ActionMessages();
            if(id==null || id.trim().length()==0) {
                errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                saveErrors(request, errors);
            } else {
                DepartmentStatusType s = (new DepartmentStatusTypeDAO()).get(new Long(id));
            	
                if(s==null) {
                    errors.add("reference", new ActionMessage("errors.invalid", "Unique Id : " + id));
                    saveErrors(request, errors);
                } else {
                	myForm.load(s);
                }
            }
        }

        // Delete 
        if("Delete".equals(op)) {
    		Transaction tx = null;
    		
            try {
            	org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
            	if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
            		tx = hibSession.beginTransaction();
            	
            	myForm.delete(hibSession);
            	
    			tx.commit();
    	    } catch (Exception e) {
    	    	if (tx!=null) tx.rollback();
    	    	throw e;
    	    }

    	    myForm.reset(mapping, request);
        }
        
        // Move Up or Down
        if("Move Up".equals(op) || "Move Down".equals(op)) {
            Transaction tx = null;
            
            try {
                org.hibernate.Session hibSession = (new DepartmentStatusTypeDAO()).getSession();
                if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
                    tx = hibSession.beginTransaction();
                
                DepartmentStatusType curStatus = (new DepartmentStatusTypeDAO()).get(myForm.getUniqueId());
                
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
                        myForm.setOrder(curStatus.getOrd());
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
                        myForm.setOrder(curStatus.getOrd());
                        hibSession.saveOrUpdate(curStatus);
                    }
                }
                
                if (tx!=null) tx.commit();
            } catch (Exception e) {
                if (tx!=null) tx.rollback();
                Debug.error(e);
            }
            myForm.reset(mapping, request);
        }

        if ("List".equals(myForm.getOp())) {
            // Read all existing settings and store in request
            getDeptStatusList(request, sessionContext.getUser().getCurrentAcademicSessionId());
            return mapping.findForward("list");
        }
        
        return mapping.findForward("Save".equals(myForm.getOp())?"add":"edit");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
    private void getDeptStatusList(HttpServletRequest request, Long sessionId) throws Exception {
		WebTable.setOrder(sessionContext,"deptStatusTypes.ord",request.getParameter("ord"),2);
		// Create web table instance 
        WebTable webTable = new WebTable( 5,
			    null, "deptStatusTypeEdit.do?ord=%%",
			    new String[] {
                "","Reference", "Label", "Apply", "Rights"},
			    new String[] {"left","left", "left","left", "left"},
			    null );
        
        TreeSet statuses = DepartmentStatusType.findAll();
		if(statuses.isEmpty()) {
		    webTable.addLine(null, new String[] {"No status defined."}, null, null );			    
		}
		
		int ord = 0;
        for (Iterator i=statuses.iterator();i.hasNext();) {
        	DepartmentStatusType s = (DepartmentStatusType)i.next();
        	if (ord != s.getOrd()) {
        		s.setOrd(ord);
        		DepartmentStatusTypeDAO.getInstance().saveOrUpdate(s);
        	}
        	ord ++;
        	String onClick = "onClick=\"document.location='deptStatusTypeEdit.do?op=Edit&id=" + s.getUniqueId() + "';\"";
        	String rights = "";
            String apply = "";
            if (s.applyDepartment()) {
                if (s.applySession())
                    apply = "Both";
                else
                    apply = "Department";
            } else if (s.applySession())
                apply = "Session";
            if (s.isAllowRollForward()) {
            	if (rights.length()>0) rights+="; ";
                rights += "roll-forward";
            }
            if (s.canOwnerView() || s.canOwnerLimitedEdit() || s.canOwnerEdit()) {
                if (rights.length()>0) rights+="; ";
                rights += "owner can ";
                if (s.canOwnerView() && s.canOwnerEdit())
                    rights += "do all";
                else {
                    if (s.canOwnerView())
                        rights += "view";
                    if (s.canOwnerEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "edit";
                    } else if (s.canOwnerLimitedEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "limited edit";
                    }
                }
            }
            if (s.canManagerView() || s.canManagerLimitedEdit() || s.canManagerEdit()) {
                if (rights.length()>0) rights+="; ";
                rights += "manager can ";
                if (s.canManagerView() && s.canManagerEdit())
                    rights += "do all";
                else {
                    if (s.canManagerView())
                        rights += "view";
                    if (s.canManagerEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "edit";
                    } else if (s.canManagerLimitedEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "limited edit";
                    }
                }
            }
            if (s.canAudit()) {
                if (rights.length()>0) rights+="; ";
                rights += "audit";
            }
            if (s.canTimetable()) {
                if (rights.length()>0) rights+="; ";
                rights += "timetable";
            } 
            if (s.canCommit()) {
                if (rights.length()>0) rights+="; ";
                rights += "commit";
            }
            if (s.canExamView() || s.canExamEdit() || s.canExamTimetable()) {
                if (rights.length()>0) rights+="; ";
                rights += "exam ";
                if (s.canExamEdit() && s.canExamTimetable())
                    rights += "do all";
                else {
                    if (s.canExamView())
                        rights += "view";
                    if (s.canExamEdit()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "edit";
                    } else if (s.canExamTimetable()) {
                        if (!rights.endsWith(" ")) rights+=" and ";
                        rights += "timetable";
                    }
                }
            }
            if (s.canOnlineSectionStudents()) {
            	if (rights.length()>0) rights+="; ";
            	rights += "sectioning";
            } else if (s.canSectionAssistStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += "assistant";
            } else if (s.canPreRegisterStudents()) {
                if (rights.length()>0) rights+="; ";
                rights += "registration";
            }
            if (s.isAllowNoRole() || s.canNoRoleReportExamFinal() || s.canNoRoleReportExamMidterm() || s.canNoRoleReportClass()) {
                if (rights.length()>0) rights+="; ";
                rights += "no-role";
                if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm() && s.canNoRoleReportClass())
                    rights += " all";
                else {
                    if (s.canNoRoleReportClass()) rights += " classes";
                    if (s.canNoRoleReportExamFinal() && s.canNoRoleReportExamMidterm()) rights += " exams";
                    else {
                        if (s.canNoRoleReportExamFinal()) rights += " final exams";
                        if (s.canNoRoleReportExamMidterm()) rights += " midterm exams";
                    }
                }
            }
            if (s.isTestSession()) {
            	if (rights.length()>0) rights+="; ";
                rights += "test session";
            }
            String ops = "";
            if (s.getOrd().intValue()>0) {
                ops += "<img src='images/arrow_u.gif' border='0' align='absmiddle' title='Move Up' " +
                		"onclick=\"deptStatusTypeEditForm.op2.value='Move Up';deptStatusTypeEditForm.uniqueId.value='"+s.getUniqueId()+"';deptStatusTypeEditForm.submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.gif' border='0' align='absmiddle'>";
            if (i.hasNext()) {
                ops += "<img src='images/arrow_d.gif' border='0' align='absmiddle' title='Move Down' " +
                		"onclick=\"deptStatusTypeEditForm.op2.value='Move Down';deptStatusTypeEditForm.uniqueId.value='"+s.getUniqueId()+"';deptStatusTypeEditForm.submit(); event.cancelBubble=true;\">";
            } else
                ops += "<img src='images/blank.gif' border='0' align='absmiddle'>";
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
        
        request.setAttribute("DeptStatusType.last", new Integer(statuses.size()-1));
	    request.setAttribute("DeptStatusType.table", webTable.printTable(WebTable.getOrder(sessionContext,"deptStatusTypes.ord")));
    }	
}

