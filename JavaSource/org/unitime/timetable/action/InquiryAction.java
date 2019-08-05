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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.Debug;
import org.unitime.commons.Email;
import org.unitime.timetable.defaults.ApplicationProperty;
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
            
            if (op == null && request.getParameter("op2") == null) {
            	request.getSession().removeAttribute("ContactUsFiles");
            }
            
	        if ("Cancel".equals(op) || "Back".equals(op)) {
            	request.getSession().removeAttribute("ContactUsFiles");
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
	        
	        if (myForm.getFile() != null && myForm.getFile().getFileSize() > 0) {
	        	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
	        	if (files == null) {
	        		files = new HashMap<String, Attachment>();
	        		request.getSession().setAttribute("ContactUsFiles", files);
	        	}
	        	files.put(myForm.getFile().getFileName(), new Attachment(myForm.getFile()));
	        }
	        
	        if (request.getParameter("deleteFile")!=null && !request.getParameter("deleteFile").isEmpty()) {
	        	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
	        	if (files != null) files.remove(request.getParameter("deleteFile"));
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
                    
                    if (ApplicationProperty.EmailInquiryAddress.value() != null)
                    	email.addRecipient(ApplicationProperty.EmailInquiryAddress.value(), ApplicationProperty.EmailInquiryAddressName.value());
                    else
                    	email.addNotify();
                    
                    boolean autoreply = ApplicationProperty.EmailInquiryAutoreply.isTrue();
                    
                    if (!autoreply) {
                        if (sessionContext.getUser().getEmail() != null && !sessionContext.getUser().getEmail().isEmpty()) {
                            email.addRecipientCC(sessionContext.getUser().getEmail(), sessionContext.getUser().getName());
                        } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
                        	email.addRecipientCC(c.getEmailAddress(), c.getName());
                        } else {
                        	email.addRecipientCC(sessionContext.getUser().getUsername() + ApplicationProperty.EmailInquiryAddressSuffix.value(), sessionContext.getUser().getName());
                        }
                    }
                    
                    Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
                    if (files != null) {
                    	for (Attachment attachment: files.values())
                    		email.addAttachment(attachment);
                    	request.getSession().removeAttribute("ContactUsFiles");
                    }
                    
                    email.send();
                    
                    
                    if (autoreply) {
            		
                    	try {
    	            		mail = "The following inquiry was submitted on your behalf. " +
	    	            		"We will contact you soon. "+
	            				"This email was automatically generated, please do not reply.\n\n";
                        
    	            		if (ApplicationProperty.EmailSenderName.value() != null) {
                                mail += "Thank you, \n\n"+ApplicationProperty.EmailSenderName.value()+"\n\n";
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
                            	email.addRecipient(sessionContext.getUser().getUsername() + ApplicationProperty.EmailInquiryAddressSuffix.value(), sessionContext.getUser().getName());
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
	
	static class Attachment implements DataSource, Serializable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private byte[] iData;
		private String iContentType;
		
		public Attachment(FormFile file) throws IOException {
			iName = file.getFileName();
			iData = file.getFileData();
			iContentType = file.getContentType();
		}

		@Override
		public String getContentType() { return iContentType; }
		@Override
		public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(iData); }
		@Override
		public String getName() { return iName; }
		@Override
		public OutputStream getOutputStream() throws IOException { return null; }
	}
	
}
