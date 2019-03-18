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
package org.unitime.timetable.events;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcServlet;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.EventServiceProviderInterface;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.gwt.shared.EventInterface.StandardEventNoteInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.EventServiceProvider;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.Staff;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.EventContactDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StandardEventNoteDepartmentDAO;
import org.unitime.timetable.model.dao.StandardEventNoteGlobalDAO;
import org.unitime.timetable.model.dao.StandardEventNoteSessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(EventPropertiesRpcRequest.class)
public class EventPropertiesBackend extends EventAction<EventPropertiesRpcRequest, EventPropertiesRpcResponse>{

	private @Autowired ApplicationContext applicationContext;
	
	@Override
	public EventPropertiesRpcResponse execute(EventPropertiesRpcRequest request, EventContext context) {
		EventPropertiesRpcResponse response = new EventPropertiesRpcResponse();
		
		Session session = SessionDAO.getInstance().get(request.getSessionId());
		
		response.setCanLookupPeople(context.hasPermission(Right.EventLookupSchedule));
		response.setCanLookupMainContact(context.hasPermission(Right.EventLookupContact));
		response.setCanLookupAdditionalContacts(response.isCanLookupMainContact() || context.hasPermission(Right.EventLookupContactAdditional));
		
		response.setCanAddSpecialEvent(context.hasPermission(Right.EventAddSpecial));
		response.setCanAddCourseEvent(context.hasPermission(Right.EventAddCourseRelated));
		response.setCanAddUnavailableEvent(context.hasPermission(Right.EventAddUnavailable));
		response.setCanSetExpirationDate(context.hasPermission(Right.EventSetExpiration));
		response.setCanEditAcademicTitle(context.hasPermission(Right.EventCanEditAcademicTitle));
		response.setCanViewMeetingContacts(context.hasPermission(Right.EventCanViewMeetingContacts));
		response.setCanEditMeetingContacts(context.hasPermission(Right.EventCanEditMeetingContacts));
		
		response.setCanExportCSV(true);// rights.canSeeSchedule(null) || rights.canLookupContacts());
		
		if (response.isCanLookupMainContact() && ApplicationProperty.EmailConfirmationEvents.isTrue()) {
			// email confirmations are enabled and user has enough permissions
			// use unitime.email.confirm.default to determine the default value of the "Send email confirmation" toggle
			response.setEmailConfirmation("true".equalsIgnoreCase(context.getUser().getProperty("unitime.email.confirm.default", "true")));
		}
		
		setupSponsoringOrganizations(session,  response);
		
		setupEventServiceProviders(session, response);
		
		if (context.getUser() != null)
			response.setMainContact(lookupMainContact(request.getSessionId(), context));
		
		setupStandardNotes(request.getSessionId(), context.getUser(), response);
		
		response.setCanSaveFilterDefaults(context.hasPermission(Right.HasRole));
		if (context.isAuthenticated() && response.isCanSaveFilterDefaults() && request.getPageName() != null) {
			response.setFilterDefault("rooms", context.getUser().getProperty("Default[" + request.getPageName() + ".rooms]"));
			response.setFilterDefault("events", context.getUser().getProperty("Default[" + request.getPageName() + ".events]"));
			response.setFilterDefault("emails", context.getUser().getProperty("Defaults[AddEvent.emails]"));
		}
		
		int tooEarly = ApplicationProperty.EventTooEarlySlot.intValue();
		if (tooEarly > 0)
			response.setTooEarlySlot(tooEarly);
		
		response.setGridDisplayTitle(ApplicationProperty.EventGridDisplayTitle.isTrue());
		response.setStudent(context.getUser() != null && context.getUser().hasRole(Roles.ROLE_STUDENT));
		response.setFirstDayOfWeek(ApplicationProperty.EventGridStartDay.intValue());
		response.setCourseEventDefaultStudentAttendance(ApplicationProperty.EventCourseEventsDefaultStudentAttendance.isTrue());
		
		return response;
	}
	
	public void setupSponsoringOrganizations(Session session, EventPropertiesRpcResponse response) {
		for (SponsoringOrganization s: SponsoringOrganization.findAll()) {
			SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
			sponsor.setUniqueId(s.getUniqueId());
			sponsor.setName(s.getName());
			sponsor.setEmail(s.getEmail());
			response.addSponsoringOrganization(sponsor);
		}
	}
	
	public void setupEventServiceProviders(Session session, EventPropertiesRpcResponse response) {
		for (EventServiceProvider p: EventServiceProvider.findAll(session.getUniqueId())) {
			EventServiceProviderInterface provider = new EventServiceProviderInterface();
			provider.setId(p.getUniqueId());
			provider.setReference(p.getReference());
			provider.setLabel(p.getLabel());
			provider.setMessage(p.getNote());
			provider.setEmail(p.getEmail());
			provider.setLocationIds(p.getLocationIds(session.getUniqueId()));
			response.addEventServiceProvider(provider);
		}
	}
	
	public ContactInterface lookupMainContact(Long sessionId, SessionContext context) {
		UserContext user = context.getUser();
		String nameFormat = user.getProperty(UserProperty.NameFormat);
		org.hibernate.Session hibSession = EventContactDAO.getInstance().getSession();
		EventContact contact = (EventContact)hibSession.createQuery(
				"from EventContact where externalUniqueId = :userId"
				).setString("userId", user.getExternalUserId()).setMaxResults(1).uniqueResult();
		if (contact != null) {
			ContactInterface c = new ContactInterface();
			c.setFirstName(contact.getFirstName());
			c.setMiddleName(contact.getMiddleName());
			c.setLastName(contact.getLastName());
			c.setAcademicTitle(contact.getAcademicTitle());
			c.setEmail(contact.getEmailAddress());
			c.setPhone(contact.getPhone());
			c.setExternalId(contact.getExternalUniqueId());
			c.setFormattedName(contact.getName(nameFormat));
			return c;
		}
		TimetableManager manager = (TimetableManager)hibSession.createQuery(
				"from TimetableManager where externalUniqueId = :userId"
				).setString("userId", user.getExternalUserId()).setMaxResults(1).uniqueResult();
		if (manager != null) {
			ContactInterface c = new ContactInterface();
			c.setExternalId(manager.getExternalUniqueId());
			c.setFirstName(manager.getFirstName());
			c.setMiddleName(manager.getMiddleName());
			c.setLastName(manager.getLastName());
			c.setAcademicTitle(manager.getAcademicTitle());
			c.setEmail(manager.getEmailAddress());
			c.setFormattedName(manager.getName(nameFormat));
			return c;
		}
		DepartmentalInstructor instructor = (DepartmentalInstructor)hibSession.createQuery(
				"from DepartmentalInstructor where department.session.uniqueId = :sessionId and externalUniqueId = :userId"
				).setLong("sessionId", sessionId).setString("userId", user.getExternalUserId()).setMaxResults(1).uniqueResult();
		if (instructor != null) {
			ContactInterface c = new ContactInterface();
			c.setExternalId(instructor.getExternalUniqueId());
			c.setFirstName(instructor.getFirstName());
			c.setMiddleName(instructor.getMiddleName());
			c.setLastName(instructor.getLastName());
			c.setAcademicTitle(instructor.getAcademicTitle());
			c.setEmail(instructor.getEmail());
			c.setFormattedName(instructor.getName(nameFormat));
			return c;
		}
		Staff staff = (Staff)hibSession.createQuery(
				"from Staff where externalUniqueId = :userId"
				).setString("userId", user.getExternalUserId()).setMaxResults(1).uniqueResult();
		if (staff != null) {
			ContactInterface c = new ContactInterface();
			c.setExternalId(staff.getExternalUniqueId());
			c.setFirstName(staff.getFirstName());
			c.setMiddleName(staff.getMiddleName());
			c.setLastName(staff.getLastName());
			c.setAcademicTitle(staff.getAcademicTitle());
			c.setEmail(staff.getEmail());
			c.setFormattedName(staff.getName(nameFormat));
			return c;
		}
		Student student = (Student)hibSession.createQuery(
				"from Student where session.uniqueId = :sessionId and externalUniqueId = :userId"
				).setLong("sessionId", sessionId).setString("userId", user.getExternalUserId()).setMaxResults(1).uniqueResult();
		if (student != null) {
			ContactInterface c = new ContactInterface();
			c.setExternalId(student.getExternalUniqueId());
			c.setFirstName(student.getFirstName());
			c.setMiddleName(student.getMiddleName());
			c.setLastName(student.getLastName());
			c.setAcademicTitle(student.getAcademicTitle());
			c.setEmail(student.getEmail());
			c.setFormattedName(student.getName(nameFormat));
			return c;
		}
		if (user.getName() != null && !user.getName().isEmpty()) {
			List<PersonInterface> people = GwtRpcServlet.execute(new PersonInterface.LookupRequest(user.getName(), "mustHaveExternalId,session=" + sessionId), applicationContext, null);
			if (people != null) {
				for (PersonInterface person: people) {
					if (user.getExternalUserId().equals(person.getId())) {
						ContactInterface c = new ContactInterface();
						c.setFirstName(person.getFirstName());
						c.setMiddleName(person.getMiddleName());
						c.setLastName(person.getLastName());
						c.setAcademicTitle(person.getAcademicTitle());
						c.setEmail(person.getEmail());
						c.setPhone(person.getPhone());
						c.setExternalId(person.getId());
						return c;
					}
				}
			}			
		}

		ContactInterface c = new ContactInterface();
		if (user.getName() != null && !user.getName().isEmpty()) {
			String name[] = user.getName().split(" ");
			if (name.length == 1) {
				c.setLastName(name[0]);
			} else if (name.length == 2) {
				c.setFirstName(name[0]);
				c.setLastName(name[1]);
			} else {
				c.setFirstName(name[0]);
				String mName = "";
				for (int i = 1; i < name.length - 1; i++)
					mName += (mName.isEmpty() ? "" : " ") + name[i];
				c.setFirstName(mName);
				c.setLastName(name[name.length - 1]);
			}
		} else {
			c.setLastName(user.getUsername());
		}
		c.setExternalId(user.getExternalUserId());
		
		return c;
	}
	
	public void setupStandardNotes(Long sessionId, UserContext user, EventPropertiesRpcResponse response) {
		for (StandardEventNote note: StandardEventNoteGlobalDAO.getInstance().findAll()) {
			StandardEventNoteInterface n = new StandardEventNoteInterface();
			n.setId(note.getUniqueId()); n.setReference(note.getReference()); n.setNote(note.getNote());
			response.addStandardNote(n);
		}
		for (StandardEventNote note: (List<StandardEventNote>)StandardEventNoteSessionDAO.getInstance().getSession().createQuery(
				"from StandardEventNoteSession where session.uniqueId = :sessionId").setLong("sessionId", sessionId).setCacheable(true).list()) {
			StandardEventNoteInterface n = new StandardEventNoteInterface();
			n.setId(note.getUniqueId()); n.setReference(note.getReference()); n.setNote(note.getNote());
			response.addStandardNote(n);
		}
		if (user != null) {
			String departments = ""; boolean allDepartments = false;
			for (UserAuthority auth: user.getAuthorities(user.getCurrentAuthority() != null ? user.getCurrentAuthority().getRole() : Roles.ROLE_ANONYMOUS, new SimpleQualifier("Session", sessionId))) {
				if (auth.hasRight(Right.DepartmentIndependent)) {
					allDepartments = true; break;
				} else {
					for (Qualifiable q: auth.getQualifiers("Department"))
						departments += (departments.isEmpty() ? "" : ",") + q.getQualifierId();
				}
			}
			if (allDepartments) {
				for (StandardEventNote note: (List<StandardEventNote>)StandardEventNoteDepartmentDAO.getInstance().getSession().createQuery(
						"from StandardEventNoteDepartment where department.session.uniqueId = :sessionId").setLong("sessionId", sessionId).setCacheable(true).list()) {
					StandardEventNoteInterface n = new StandardEventNoteInterface();
					n.setId(note.getUniqueId()); n.setReference(note.getReference()); n.setNote(note.getNote());
					response.addStandardNote(n);
				}
			} else if (!departments.isEmpty()) {
				for (StandardEventNote note: (List<StandardEventNote>)StandardEventNoteDepartmentDAO.getInstance().getSession().createQuery(
						"from StandardEventNoteDepartment where department.uniqueId in (" + departments + ")").setCacheable(true).list()) {
					StandardEventNoteInterface n = new StandardEventNoteInterface();
					n.setId(note.getUniqueId()); n.setReference(note.getReference()); n.setNote(note.getNote());
					response.addStandardNote(n);
				}
			}
		}
	}

}
