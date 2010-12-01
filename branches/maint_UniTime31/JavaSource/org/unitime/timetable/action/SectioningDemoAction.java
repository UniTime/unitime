/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
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
import java.io.FileOutputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.SectioningDemoForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.test.StudentSectioningTest;


/** 
 * @author Tomas Muller
 */
public class SectioningDemoAction extends Action {

    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        HttpSession httpSession = request.getSession();
		if(!Web.isLoggedIn( httpSession ) || !Web.isAdmin(httpSession)) {
            throw new Exception ("Access Denied.");
        }
		
        MessageResources rsc = getResources(request);
        User user = Web.getUser(request.getSession());  
        Session session = Session.getCurrentAcadSession(user);
        SectioningDemoForm frm = (SectioningDemoForm) form;
	    ActionMessages errors = null;
	    
	    String op = frm.getOp();
		if(op==null || op.trim().length()==0)
		    op = rsc.getMessage("op.view");
        if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
            op = request.getParameter("op2");
		
		frm.setOp(op);

		// First access
        if ( op.equals(rsc.getMessage("op.view")) ) {
            doLoad(request, frm);
            request.getSession().removeAttribute("LastSolution");
        }
        
        Element studentEl = (Element)request.getSession().getAttribute("LastSolution");
        if (studentEl!=null) {
            frm.load(session, studentEl, false, request);
        }
        
        if ("Add Request".equals(op)) {
            frm.setNrRequests(frm.getNrRequests()+1);
        }
        
        if ("Add Alternative Request".equals(op)) {
            frm.setNrAltRequests(frm.getNrAltRequests()+1);
        }

        if ("Delete".equals(op)) {
            frm.removeRequest(Integer.parseInt(request.getParameter("reqIdx")));
        }

        if ("Move Up".equals(op)) {
            frm.moveRequest(Integer.parseInt(request.getParameter("reqIdx")),-1);
        }

        if ("Move Down".equals(op)) {
            frm.moveRequest(Integer.parseInt(request.getParameter("reqIdx")),+1);
        }
        
        if ("Clear".equals(op)) {
            frm.getCourseAssignments().clear();
            frm.getMessages().clear();
        }
        
        if ("Unload".equals(op)) {
            frm.reset(mapping, request);
        }

        if ("Load".equals(op)) {
            errors = frm.validate(mapping, request);
            if (errors==null || errors.size()==0) {
                if (frm.getStudentId()==null || frm.getStudentId().length()==0) {
                    errors.add("studentId", new ActionMessage("errors.generic", "No student id provided."));
                    saveErrors(request, errors);
                } else {
                    Student student = Student.findByExternalId(session.getUniqueId(), frm.getStudentId());
                    if (student==null) {
                        frm.setStudentLoaded(false);
                        errors.add("studentId", new ActionMessage("errors.generic", "Student with id "+frm.getStudentId()+" does not exists."));
                        saveErrors(request, errors);
                        frm.reset(mapping, request);
                    } else {
                        Document requestXml = DocumentHelper.createDocument();
                        Element requestEl = requestXml.addElement("request");
                        requestEl.addAttribute("campus", session.getAcademicInitiative());
                        requestEl.addAttribute("year", session.getAcademicYear());
                        requestEl.addAttribute("term", session.getAcademicTerm());
                        requestEl.addAttribute("version", "1.1");
                        requestEl.addAttribute("timestamp", new Date().toString());
                        
                        studentEl = requestEl.addElement("student");
                        studentEl.addAttribute("key", frm.getStudentId());

                        studentEl.addElement("retrieveCourseRequests");
                        
                        Document responseXml = StudentSectioningTest.testSectioning(requestXml);
                        studentEl = responseXml.getRootElement().element("student");
                        
                        frm.load(session, studentEl, true, null);
                        
                        if (studentEl!=null) {
                            request.getSession().setAttribute("LastSolution", studentEl);
                        } else {
                            request.getSession().removeAttribute("LastSolution");
                        }
                        
                        File requestFile = ApplicationProperties.getTempFile("request", "xml");
                        XMLWriter out = new XMLWriter(new FileOutputStream(requestFile),OutputFormat.createPrettyPrint());
                        out.write(requestXml);
                        out.flush(); out.close();
                        frm.setRequestFile("temp/"+requestFile.getName());
                        
                        File responseFile = ApplicationProperties.getTempFile("response", "xml");
                        out = new XMLWriter(new FileOutputStream(responseFile),OutputFormat.createPrettyPrint());
                        out.write(responseXml);
                        out.flush(); out.close();
                        frm.setResponseFile("temp/"+responseFile.getName());
                        
                        frm.setStudentLoaded(true);
                    }                
                }
            } else {
                saveErrors(request, errors);
            }
        }

        // Submit request
        if ( op.equals(rsc.getMessage("button.submitStudentRequest")) || op.equals("Save") ) {
            errors = frm.validate(mapping, request);
            if (errors==null || errors.size()==0) {

                Document requestXml = DocumentHelper.createDocument();
                Element requestEl = requestXml.addElement("request");
                requestEl.addAttribute("campus", session.getAcademicInitiative());
                requestEl.addAttribute("year", session.getAcademicYear());
                requestEl.addAttribute("term", session.getAcademicTerm());
                requestEl.addAttribute("version", "1.1");
                requestEl.addAttribute("timestamp", new Date().toString());
                
                studentEl = requestEl.addElement("student");
                studentEl.addAttribute("key", (frm.getStudentId()==null || frm.getStudentId().length()==0?"-1":frm.getStudentId()));
                
                frm.save(session, studentEl, op.equals("Save"));
                
                Document responseXml = StudentSectioningTest.testSectioning(requestXml);
                studentEl = responseXml.getRootElement().element("student");
                
                frm.load(session, studentEl, false, null);
                
                if (studentEl!=null) {
                    request.getSession().setAttribute("LastSolution", studentEl);
                } else {
                    request.getSession().removeAttribute("LastSolution");
                }
                
                File requestFile = ApplicationProperties.getTempFile("request", "xml");
                XMLWriter out = new XMLWriter(new FileOutputStream(requestFile),OutputFormat.createPrettyPrint());
                out.write(requestXml);
                out.flush(); out.close();
                frm.setRequestFile("temp/"+requestFile.getName());
                
                File responseFile = ApplicationProperties.getTempFile("response", "xml");
                out = new XMLWriter(new FileOutputStream(responseFile),OutputFormat.createPrettyPrint());
                out.write(responseXml);
                out.flush(); out.close();
                frm.setResponseFile("temp/"+responseFile.getName());
                
            } else {
                saveErrors(request, errors);
            }
        }
        
        return mapping.findForward("displayForm");
	    
    }

    /**
     * @param request
     * @param frm
     */
    private void doLoad(
            HttpServletRequest request, 
            SectioningDemoForm frm) {
        
    }

}
