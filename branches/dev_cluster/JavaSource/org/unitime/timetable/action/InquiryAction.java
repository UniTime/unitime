/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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

import java.util.Date;
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
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.form.InquiryForm;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Service("/inquiry")
public class InquiryAction extends Action {
	
	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			
			InquiryForm myForm = (InquiryForm) form;
	        MessageResources rsc = getResources(request);
			
	        // Check Access
	        sessionContext.checkPermission(Right.Inquiry);
			
	        // Read operation to be performed
	        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
	        
            myForm.setNoRole(!sessionContext.getUser().getCurrentAuthority().hasRight(Right.HasRole));
	        
	        if ("Cancel".equals(op) || "Back".equals(op)) {
	        	return mapping.findForward("submit");
	        }
	        
    		ActionMessages errors = null;
    		
	        if (op!=null && op.equals(rsc.getMessage("button.insertAddress"))) {
	        	if (myForm.getPuid()!=null && myForm.getPuid().length()>0) {
	        		myForm.addToCarbonCopy(myForm.getPuid());
	        		myForm.setPuid(null);
	        	}
	        	else {
	        		errors = new ActionMessages();
	        		errors.add("puid", 
	        					new ActionMessage("errors.generic",	"Recipient has an invalid email address."));
	                saveErrors(request, errors);
	        	}
	        }
	        
	        if (request.getParameter("deleteId")!=null && request.getParameter("deleteId").length()>0) {
	        	try {
	        		int deleteId = Integer.parseInt(request.getParameter("deleteId"));
	        		myForm.removeCarbonCopy(deleteId);
	        		myForm.setPuid(null);
	        	}
	        	catch(Exception e) {
	        		errors = new ActionMessages();
	        		errors.add("puid", 
	        					new ActionMessage("errors.generic",	"Invalid email address."));
	                saveErrors(request, errors);
	        	}
	        }
	        
	        if ("Submit".equals(op)) {
	            errors = myForm.validate(mapping, request);
	            if(errors.size()>0) {
	                saveErrors(request, errors);
	            } else {
	            	String mail = myForm.getMessage();;
	            	mail += "\r\n";
	            	mail += "\r\n";
	            	mail += "User info -------------- \r\n";
	            	mail += "User: "+sessionContext.getUser().getName()+"\r\n";
	            	mail += "Login: "+sessionContext.getUser().getUsername()+"\r\n";
	            	mail += "Email: "+sessionContext.getUser().getEmail()+"\r\n";
	            	mail += "Role: "+sessionContext.getUser().getCurrentAuthority()+"\r\n";
	            	List<? extends Qualifiable> sessions = sessionContext.getUser().getCurrentAuthority().getQualifiers("Session");
	            	if (!sessions.isEmpty())
	            		mail += "Academic Session: " + sessions.get(0).getQualifierLabel()+"\r\n";
	            	List<? extends Qualifiable> depts = sessionContext.getUser().getCurrentAuthority().getQualifiers("Department");
	            	if (!depts.isEmpty())
	            		mail += "Departments: "+depts+"\r\n";
	            	List<? extends Qualifiable> sg = sessionContext.getUser().getCurrentAuthority().getQualifiers("SolverGroup");
	            	if (!sg.isEmpty())
	            		mail += "Solver Groups: "+sg+"\r\n";
	            	
	            	mail += "\r\n";
	            	
	            	mail += "\r\n";
	            	mail += "Application info -------------- \r\n";
	            	mail += "Version: " + Constants.getVersion()+" ("+Constants.getReleaseDate()+")\r\n";
	            	mail += "TimeStamp: " + (new Date());
	            	
                    EventContact c = EventContact.findByExternalUniqueId(sessionContext.getUser().getExternalUserId());
	            	
                    Email email = Email.createEmail();
                    email.setSubject("UniTime ("+myForm.getTypeMsg(myForm.getType())+"): "+myForm.getSubject());
                    email.setText(mail);
                    
	            	if (!myForm.getCarbonCopy().isEmpty()) {
	            		for (Iterator i=myForm.getCarbonCopy().iterator(); i.hasNext(); ) {
	            			email.addRecipientCC((String)i.next(), null);
	            		}
	            	}
                    
                    if (ApplicationProperties.getProperty("unitime.email.inquiry") != null)
                    	email.addRecipient(ApplicationProperties.getProperty("unitime.email.inquiry"), ApplicationProperties.getProperty("unitime.email.inquiry.name"));
                    else
                    	email.addNotify();
                    
                    boolean autoreply = "true".equals(ApplicationProperties.getProperty("unitime.email.inquiry.autoreply", ApplicationProperties.getProperty("tmtbl.inquiry.autoreply", "false")));
                    
                    if (!autoreply) {
                        if (sessionContext.getUser().getEmail() != null && !sessionContext.getUser().getEmail().isEmpty()) {
                            email.addRecipientCC(sessionContext.getUser().getEmail(), sessionContext.getUser().getName());
                        } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
                        	email.addRecipientCC(c.getEmailAddress(), c.getName());
                        } else {
                        	email.addRecipientCC(sessionContext.getUser().getUsername() + ApplicationProperties.getProperty("unitime.email.inquiry.suffix", ApplicationProperties.getProperty("tmtbl.inquiry.email.suffix","@unitime.org")), sessionContext.getUser().getName());
                        }
                    }
                    email.send();
                    
                    
                    if (autoreply) {
            		
                    	try {
    	            		mail = "The following inquiry was submitted on your behalf. " +
	    	            		"We will contact you soon. "+
	            				"This email was automatically generated, please do not reply.\n\n";
                        
    	            		if (ApplicationProperties.getProperty("tmtbl.inquiry.sender.name")!=null) {
                                mail += "Thank you, \n\n"+ApplicationProperties.getProperty("tmtbl.inquiry.sender.name")+"\n\n";
                            }
                            
                            mail +=
                                "-- INQUIRY ("+myForm.getTypeMsg(myForm.getType())+"): "+myForm.getSubject()+" ---------- \n\n"+
                				myForm.getMessage()+"\n"+
                				"-- END INQUIRY -------------------------------------------";
                            
                            email = Email.createEmail();
                            email.setSubject("RE: UniTime ("+myForm.getTypeMsg(myForm.getType())+"): "+myForm.getSubject());
                            email.setText(mail);
                            
                            if (sessionContext.getUser().getEmail() != null && !sessionContext.getUser().getEmail().isEmpty()) {
                                email.addRecipient(sessionContext.getUser().getEmail(), sessionContext.getUser().getName());
                            } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
                            	email.addRecipient(c.getEmailAddress(), c.getName());
                            } else {
                            	email.addRecipient(sessionContext.getUser().getUsername() + ApplicationProperties.getProperty("unitime.email.inquiry.suffix", ApplicationProperties.getProperty("tmtbl.inquiry.email.suffix","@unitime.org")), sessionContext.getUser().getName());
                            }
                            
                            email.send();
                    	} catch (Exception e) {}
	            	}
	            	
	            	myForm.setOp("Sent");
	            	return mapping.findForward("display");
	            }
	        }
	        
            LookupTables.setupTimetableManagers(request);
	        myForm.updateMessage();
	        return mapping.findForward("display");
		} catch (Exception e) {
			Debug.error(e);
			throw e;
		}
	}
	
}
