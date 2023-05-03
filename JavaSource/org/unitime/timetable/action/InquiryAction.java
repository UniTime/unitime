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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.activation.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.InquiryForm;
import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ContactCategoryDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Action(value = "inquiry", results = {
		@Result(name = "display", type = "tiles", location = "inquiry.tiles"),
		@Result(name = "submit", type = "redirect", location = "/selectPrimaryRole.action")
	})
@TilesDefinition(name = "inquiry.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Contact Us"),
		@TilesPutAttribute(name = "body", value = "/user/inquiry.jsp"),
		@TilesPutAttribute(name = "checkRole", value = "false")
	})
public class InquiryAction extends UniTimeAction<InquiryForm> {
	private static final long serialVersionUID = -3667385823477363659L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	protected String op2;
	protected String deleteFile;
	protected Integer deleteId;
	
	public String getOp2() { return op2; }
	public void setOp2(String op2) { this.op2 = op2; }
	public String getDeleteFile() { return deleteFile; }
	public void setDeleteFile(String deleteFile) { this.deleteFile = deleteFile; }
	public Integer getDeleteId() { return deleteId; }
	public void setDeleteId(Integer deleteId) { this.deleteId = deleteId; }


	public String execute() throws Exception {
		if (form == null) {
			form = new InquiryForm();
		}

        // Check Access
        sessionContext.checkPermission(Right.Inquiry);
		
        // Read operation to be performed
        if (op == null) op = form.getOp();
        if (op == null || op.isEmpty()) op = op2;
        
        form.setNoRole(!sessionContext.getUser().getCurrentAuthority().hasRight(Right.HasRole));
        
        if (op == null) {
        	request.getSession().removeAttribute("ContactUsFiles");
        }
        
        if (MSG.actionInquiryCancel().equals(op) || MSG.actionInquiryBack().equals(op)) {
        	request.getSession().removeAttribute("ContactUsFiles");
        	return "submit";
        }
        
        if (MSG.actionAddRecipient().equals(op)) {
        	if (form.getPuid()!=null && form.getPuid().length()>0) {
        		TimetableManager mgr = TimetableManager.findByExternalId(form.getPuid());
        		if (mgr != null && mgr.getEmailAddress() != null && !mgr.getEmailAddress().isEmpty()) {
        			form.addToCarbonCopyName(mgr.getEmailAddress(), mgr.getName(getNameFormat()));
        		} else {
        			addFieldError("form.puid", MSG.errorInquiryInvalidRecipientAddress());	
        		}
        		form.setPuid(null);
        	} else {
        		addFieldError("form.puid", MSG.errorInquiryInvalidRecipientAddress());
        	}
        }
        
        if (form.getFile() != null && form.getFileFileName() != null) {
        	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
        	if (files == null) {
        		files = new HashMap<String, Attachment>();
        		request.getSession().setAttribute("ContactUsFiles", files);
        	}
        	files.put(form.getFileFileName(), new Attachment(form.getFile(), form.getFileFileName(), form.getFileContentType()));
        }
        
        if (deleteFile!=null && !deleteFile.isEmpty()) {
        	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
        	if (files != null) files.remove(request.getParameter("deleteFile"));
        }
        
        if (deleteId!=null) {
        	try {
        		form.removeCarbonCopy(deleteId);
        		form.setPuid(null);
        	} catch (Exception e) {
        		addFieldError("form.puid", MSG.errorInquiryInvalidAddress());
        	}
        }
        
        if (MSG.actionInquirySubmit().equals(op)) {
            form.validate(this);
            if (!hasFieldErrors()) {
            	String mail = form.getMessage();;
            	mail += "\r\n";
            	mail += "\r\n";
            	mail += MSG.emailInquiryUserInfoSection() + "\r\n";
            	mail += MSG.propUserName() + " "+sessionContext.getUser().getName()+"\r\n";
            	mail += MSG.propLogin() + " "+sessionContext.getUser().getUsername()+"\r\n";
            	mail += MSG.propertyEmail() + " "+sessionContext.getUser().getEmail()+"\r\n";
            	mail += MSG.propertyRole() + " "+sessionContext.getUser().getCurrentAuthority()+"\r\n";
            	List<? extends Qualifiable> sessions = sessionContext.getUser().getCurrentAuthority().getQualifiers("Session");
            	if (!sessions.isEmpty())
            		mail += MSG.propAcademicSession() + " " + sessions.get(0).getQualifierLabel()+"\r\n";
            	List<? extends Qualifiable> depts = sessionContext.getUser().getCurrentAuthority().getQualifiers("Department");
            	if (!depts.isEmpty())
            		mail += MSG.propDepartments() + " "+depts+"\r\n";
            	List<? extends Qualifiable> sg = sessionContext.getUser().getCurrentAuthority().getQualifiers("SolverGroup");
            	if (!sg.isEmpty())
            		mail += MSG.propSolverGroups() +  " "+sg+"\r\n";
            	
            	mail += "\r\n";
            	
            	mail += "\r\n";
            	mail += MSG.emailInquiryApplicationInfoSection() + "\r\n";
            	mail += MSG.propVersion() + " " + Constants.getVersion()+" ("+Constants.getReleaseDate()+")\r\n";
            	mail += MSG.propTimeStamp() + " " + (new Date());
            	
                EventContact c = EventContact.findByExternalUniqueId(sessionContext.getUser().getExternalUserId());
            	
                Email email = Email.createEmail();
                email.setSubject(MSG.emailInquirySubject(form.getTypeMsg(form.getType()), form.getSubject()));
                email.setText(mail);
                
            	if (!form.getCarbonCopy().isEmpty()) {
            		for (int i = 0; i < form.getCarbonCopy().size(); i++)
            			email.addRecipientCC(form.getCarbonCopy(i), form.getCarbonCopyName(i));
            	}
            	
            	ContactCategory cc = ContactCategoryDAO.getInstance().get(form.getType());
            	String replyTo = null, replyToName = null;
            	if (cc != null && cc.getEmail() != null && !cc.getEmail().isEmpty()) {
    				String suffix = ApplicationProperty.EmailDefaultAddressSuffix.value();
    				for (String address: cc.getEmail().split("[\n,]")) {
    					if (!address.trim().isEmpty()) {
    						if (suffix != null && address.indexOf('@') < 0) {
    							email.addRecipient(address.trim() + suffix, null);
    							if (replyTo == null) replyTo = address.trim() + suffix;
    						} else {
    							email.addRecipient(address.trim(), null);
    							if (replyTo == null) replyTo = address.trim();
    						}
    					}
    				}
            	} else if (ApplicationProperty.EmailInquiryAddress.value() != null) {
                	email.addRecipient(ApplicationProperty.EmailInquiryAddress.value(), ApplicationProperty.EmailInquiryAddressName.value());
                	replyTo = ApplicationProperty.EmailInquiryAddress.value();
                	replyToName = ApplicationProperty.EmailInquiryAddressName.value();
            	} else
                	email.addNotify();
            	if (sessionContext != null && sessionContext.isAuthenticated() && sessionContext.getUser().getEmail() != null)
            		email.setReplyTo(sessionContext.getUser().getEmail(), sessionContext.getUser().getName());
            	else if (replyTo != null)
            		email.setReplyTo(replyTo, replyToName);
                
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
	            		mail = MSG.emailInquiryCofirmation() + "\r\n\r\n";
                    
	            		if (ApplicationProperty.EmailSenderName.value() != null) {
                            mail += MSG.emailInquiryCofirmationThankYou(ApplicationProperty.EmailSenderName.value())+"\r\n\r\n";
                        }
                        
                        mail +=
                            MSG.emailInquiryBeginSection(form.getTypeMsg(form.getType()), form.getSubject()) + "\r\n"+
            				form.getMessage()+"\r\n"+
                            MSG.emailInquiryEndSection();
                        
                        email = Email.createEmail();
                        email.setSubject("RE: " + MSG.emailInquirySubject(form.getTypeMsg(form.getType()), form.getSubject()));
                        email.setText(mail);
                        
                        if (sessionContext.getUser().getEmail() != null && !sessionContext.getUser().getEmail().isEmpty()) {
                            email.addRecipient(sessionContext.getUser().getEmail(), sessionContext.getUser().getName());
                        } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
                        	email.addRecipient(c.getEmailAddress(), c.getName());
                        } else {
                        	email.addRecipient(sessionContext.getUser().getUsername() + ApplicationProperty.EmailInquiryAddressSuffix.value(), sessionContext.getUser().getName());
                        }
    	            	if (replyTo != null)
    	            		email.setReplyTo(replyTo, replyToName);
                        
                        email.send();
                	} catch (Exception e) {}
            	}
            	
            	form.setOp("Sent");
            	return "display";
            }
        }
        
        LookupTables.setupTimetableManagers(request);
        form.updateMessage();
        return "display";
	}
	
	static class Attachment implements DataSource, Serializable {
		private static final long serialVersionUID = 1L;
		private String iName;
		private byte[] iData;
		private String iContentType;
		
		public Attachment(File file, String name, String contentType) throws IOException {
			iData = FileUtils.readFileToByteArray(file);
			iName = name;
			iContentType = contentType;
		}

		@Override
		public String getContentType() { return iContentType; }
		@Override
		public InputStream getInputStream() throws IOException { return new ByteArrayInputStream(iData); }
		@Override
		public String getName() { return iName; }
		@Override
		public OutputStream getOutputStream() throws IOException { return null; }
		public Long getSize() { return Long.valueOf(iData.length); }
	}
	
	private String nameFormat = null;
    public String getNameFormat() {
    	if (nameFormat == null)
    		nameFormat = UserProperty.NameFormat.get(sessionContext.getUser());
    	return nameFormat;
    }
    
    public Collection<String> getAttachedFiles() {
    	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
    	if (files == null) return null;
    	return files.keySet();
    }
    
    public Long getAttachedFileSize(String name) {
    	Map<String, Attachment> files = (Map<String, Attachment>)request.getSession().getAttribute("ContactUsFiles");
    	if (files == null) return null;
    	Attachment at = files.get(name);
    	if (at == null) return null;
    	return at.getSize();
    }
}
