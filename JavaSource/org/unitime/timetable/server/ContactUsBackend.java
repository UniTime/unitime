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
package org.unitime.timetable.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.fileupload2.core.FileItem;
import org.unitime.commons.Email;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.page.ContactUsPage.AttachedFile;
import org.unitime.timetable.gwt.client.page.ContactUsPage.Category;
import org.unitime.timetable.gwt.client.page.ContactUsPage.ContactUsRequest;
import org.unitime.timetable.gwt.client.page.ContactUsPage.ContactUsResponse;
import org.unitime.timetable.gwt.client.page.ContactUsPage.Manager;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ContactCategory;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.ManagerRole;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ContactCategoryDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

import jakarta.activation.DataSource;

@GwtRpcImplements(ContactUsRequest.class)
public class ContactUsBackend implements GwtRpcImplementation<ContactUsRequest, ContactUsResponse> {
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final GwtMessages GWT = Localization.create(GwtMessages.class);

	@Override
	public ContactUsResponse execute(ContactUsRequest request, SessionContext context) {
		context.checkPermission(Right.Inquiry);
		
		switch (request.getOperation()) {
		case LOAD:
			return load(request, context);
		case UPLOAD:
			return attachFile(request, context);
		case SUBMIT:
			try {
				return sendEmail(request, context);
			} catch (Exception e) {
				throw new GwtRpcException(GWT.failedEmail(e.getMessage()), e);
			}
		default:
			return null;
		}
	}
	
	protected ContactUsResponse load(ContactUsRequest request, SessionContext context) {
		context.removeAttribute("ContactUsFiles");
		ContactUsResponse response = new ContactUsResponse();
		
		String nameFormat = UserProperty.NameFormat.get(context.getUser());
		
		boolean hasRole = context.getUser().getCurrentAuthority().hasRight(Right.HasRole);
		
		if (hasRole) {
			for (TimetableManager manager: TimetableManager.getManagerList()) {
				if (manager.getEmailAddress() == null || manager.getEmailAddress().isEmpty()) continue;
				for (ManagerRole role: manager.getManagerRoles()) {
					
					// Check if can be used
					if (!Boolean.TRUE.equals(role.isReceiveEmails())) continue;
					if (role.getRole().hasRight(Right.SessionIndependent) || (manager.getDepartments().isEmpty() && role.getRole().hasRight(Right.SessionIndependentIfNoSessionGiven))) {
						// can be used
					} else {
						boolean hasThisSession = false;
						for (Department department: manager.getDepartments())
							if (department.getSessionId().equals(context.getUser().getCurrentAcademicSessionId())) {
								hasThisSession = true;
								break;
							}
						if (!hasThisSession) continue;
					}
					
					Manager m = new Manager();
					m.setId(manager.getUniqueId());
					m.setName(manager.getName(nameFormat));
					m.setEmail(manager.getEmailAddress());
					response.addManager(m);
				}
			}
		}
		
		for (ContactCategory cc: ContactCategoryDAO.getInstance().getSession().createQuery(
				"from ContactCategory order by reference", ContactCategory.class).setCacheable(true).list()) {
			if (cc.getHasRole() && !hasRole) continue;
			Category c = new Category();
			c.setId(cc.getUniqueId());
			c.setSubject(cc.getLabel());
			c.setMessage(cc.getMessage());
			response.addCategory(c);
		}
		
		TableInterface contactInfo = new TableInterface();
		contactInfo.setName(MSG.sectionContactInformation());
		addContactInfo(contactInfo, MSG.propContactAddress(), ApplicationProperty.ContactUsAddress);
		addContactInfo(contactInfo, MSG.propContactPhone(), ApplicationProperty.ContactUsPhone);
		addContactInfo(contactInfo, MSG.propContactOfficeHours(), ApplicationProperty.ContactUsOfficeHours);
		addContactInfo(contactInfo, MSG.propContactEmail(), ApplicationProperty.ContactUsEmail);
		response.setContactInformation(contactInfo);
		
		return response;
	}
	
	protected void addContactInfo(TableInterface contactInfo, String label, ApplicationProperty prop) {
		String value = prop.value();
		if (value != null && !value.isEmpty()) {
			if (prop == ApplicationProperty.ContactUsEmail) {
				String mailTo = ApplicationProperty.ContactUsMailTo.value();
				if (mailTo == null || mailTo.isEmpty())
					mailTo = value;
				contactInfo.addProperty(label).setHtml("<a href='mailto:" + mailTo + "'>" + value + "</a>");
			} else {
				contactInfo.addProperty(label).setHtml(value).addStyle("white-space: pre-wrap;");
			}
		}
	}
	
	protected ContactUsResponse attachFile(ContactUsRequest request, SessionContext context) {
		Map<Long, Attachment> files = (Map<Long, Attachment>)context.getAttribute("ContactUsFiles");
    	if (files == null) {
    		files = new HashMap<Long, Attachment>();
    		context.setAttribute("ContactUsFiles", files);
    	}
    	FileItem item = (FileItem)context.getAttribute(SessionAttribute.LastUploadedFile);
    	if (item == null) return null;
    	
    	context.removeAttribute(SessionAttribute.LastUploadedFile);

    	try {
        	Attachment a = new Attachment(item);
    		files.put(a.getId(), a);
    		
        	AttachedFile f = new AttachedFile();
        	f.setId(a.getId());
        	f.setName(a.getName());
        	f.setSize(a.getSize());
        	
        	ContactUsResponse response = new ContactUsResponse();
        	response.setAttachedFile(f);
        	return response;
    	} catch (IOException e) {
    		throw new GwtRpcException(e.getMessage(), e);
    	}
	}
	
	protected ContactUsResponse sendEmail(ContactUsRequest request, SessionContext context) throws Exception {
		String mail = request.getMessage();
		mail += "\r\n";
    	mail += "\r\n";
    	mail += MSG.emailInquiryUserInfoSection() + "\r\n";
    	mail += MSG.propUserName() + " "+context.getUser().getName()+"\r\n";
    	mail += MSG.propLogin() + " "+context.getUser().getUsername()+"\r\n";
    	mail += MSG.propertyEmail() + " "+context.getUser().getEmail()+"\r\n";
    	mail += MSG.propertyRole() + " "+context.getUser().getCurrentAuthority()+"\r\n";
    	List<? extends Qualifiable> sessions = context.getUser().getCurrentAuthority().getQualifiers("Session");
    	if (!sessions.isEmpty())
    		mail += MSG.propAcademicSession() + " " + sessions.get(0).getQualifierLabel()+"\r\n";
    	List<? extends Qualifiable> depts = context.getUser().getCurrentAuthority().getQualifiers("Department");
    	if (!depts.isEmpty())
    		mail += MSG.propDepartments() + " "+depts+"\r\n";
    	List<? extends Qualifiable> sg = context.getUser().getCurrentAuthority().getQualifiers("SolverGroup");
    	if (!sg.isEmpty())
    		mail += MSG.propSolverGroups() +  " "+sg+"\r\n";
    	
    	mail += "\r\n";
    	
    	mail += "\r\n";
    	mail += MSG.emailInquiryApplicationInfoSection() + "\r\n";
    	mail += MSG.propVersion() + " " + Constants.getVersion()+" ("+Constants.getReleaseDate()+")\r\n";
    	mail += MSG.propTimeStamp() + " " + (new Date());
    	
        EventContact c = EventContact.findByExternalUniqueId(context.getUser().getExternalUserId());
    	
        Email email = Email.createEmail();
    	ContactCategory cc = (request.getCategoryId() == null ? null : ContactCategoryDAO.getInstance().get(request.getCategoryId()));
        
        email.setSubject(MSG.emailInquirySubject(cc == null ? "" : cc.getReference(), request.getSubject()));
        email.setText(mail);
        
        if (request.hasManagersToCC()) {
        	String nameFormat = UserProperty.NameFormat.get(context.getUser());
        	for (Long managerId: request.getManagersToCC()) {
        		TimetableManager manager = TimetableManagerDAO.getInstance().get(managerId);
        		if (manager != null)
        			email.addRecipientCC(manager.getEmailAddress(), manager.getName(nameFormat));
        	}
        }
    	
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
    	if (context != null && context.isAuthenticated() && context.getUser().getEmail() != null && !context.getUser().getEmail().isEmpty())
    		email.setReplyTo(context.getUser().getEmail(), context.getUser().getName());
    	else if (replyTo != null)
    		email.setReplyTo(replyTo, replyToName);
        
        boolean autoreply = ApplicationProperty.EmailInquiryAutoreply.isTrue();
        
        if (!autoreply) {
            if (context.getUser().getEmail() != null && !context.getUser().getEmail().isEmpty()) {
                email.addRecipientCC(context.getUser().getEmail(), context.getUser().getName());
            } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
            	email.addRecipientCC(c.getEmailAddress(), c.getName());
            } else if (context.getUser().getUsername() != null && !context.getUser().getUsername().isEmpty()) {
            	email.addRecipientCC(context.getUser().getUsername() + ApplicationProperty.EmailInquiryAddressSuffix.value(), context.getUser().getName());
            }
        }
        
        Map<Long, Attachment> files = (Map<Long, Attachment>)context.getAttribute("ContactUsFiles");
        if (files != null && request.hasAttachments()) {
        	for (Long id: request.getAttachments()) {
        		Attachment att = files.get(id);
        		if (att != null) email.addAttachment(att);
        	}
        }
        email.send();

        context.removeAttribute("ContactUsFiles");

        
        if (autoreply) {
		
        	try {
        		mail = MSG.emailInquiryCofirmation() + "\r\n\r\n";
            
        		if (ApplicationProperty.EmailSenderName.value() != null) {
                    mail += MSG.emailInquiryCofirmationThankYou(ApplicationProperty.EmailSenderName.value())+"\r\n\r\n";
                }
                
                mail +=
                	MSG.emailInquirySubject(cc == null ? "" : cc.getReference(), request.getSubject()) + "\r\n"+
    				request.getMessage()+"\r\n"+
                    MSG.emailInquiryEndSection();
                
                email = Email.createEmail();
                email.setSubject("RE: " + MSG.emailInquirySubject(cc == null ? "" : cc.getReference(), request.getSubject()));
                email.setText(mail);
                
                if (context.getUser().getEmail() != null && !context.getUser().getEmail().isEmpty()) {
                    email.addRecipient(context.getUser().getEmail(), context.getUser().getName());
                } else if (c != null && c.getEmailAddress() != null && !c.getEmailAddress().isEmpty()) {
                	email.addRecipient(c.getEmailAddress(), c.getName());
                } else {
                	email.addRecipient(context.getUser().getUsername() + ApplicationProperty.EmailInquiryAddressSuffix.value(), context.getUser().getName());
                }
            	if (replyTo != null)
            		email.setReplyTo(replyTo, replyToName);
                
                email.send();
        	} catch (Exception e) {}
    	}
        return null;
	}

	static class Attachment implements DataSource, Serializable {
		private static final long serialVersionUID = 1L;
		private Long iId;
		private String iName;
		private byte[] iData;
		private String iContentType;
		
		public Attachment(FileItem file) throws IOException {
			iId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
			iData = file.get();
			iName = file.getName();
			iContentType = file.getContentType();
		}
		
		public Long getId() { return iId; }

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
}
