/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.events;

import java.util.Collections;
import java.util.List;

import org.unitime.commons.User;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.server.LookupServlet;
import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SponsoringOrganization;
import org.unitime.timetable.model.StandardEventNote;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StandardEventNoteDAO;

public class EventPropertiesBackend extends EventAction<EventPropertiesRpcRequest, EventPropertiesRpcResponse>{

	@Override
	public EventPropertiesRpcResponse execute(EventPropertiesRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		EventPropertiesRpcResponse response = new EventPropertiesRpcResponse();
		
		Session session = SessionDAO.getInstance().get(request.getSessionId());
		
		response.setCanLookupPeople(rights.canSeeSchedule(null));
		response.setCanLookupContacts(rights.canLookupContacts());
		
		response.setCanAddEvent(rights.canAddEvent(EventType.Special, null));
		response.setCanAddCourseEvent(rights.canAddEvent(EventType.Course, null));
		
		setupSponsoringOrganizations(session,  response);
		
		setupMainContact(session, response, helper.getUser());
		
		setupStandardNotes(session, response);
		
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
	
	public void setupMainContact(Session session, EventPropertiesRpcResponse response, User user) {
		if (user == null) return;
		EventContact contact = EventContact.findByExternalUniqueId(user.getId());
		if (contact != null) {
			ContactInterface c = new ContactInterface();
			c.setFirstName(contact.getFirstName());
			c.setMiddleName(contact.getMiddleName());
			c.setLastName(contact.getLastName());
			c.setEmail(contact.getEmailAddress());
			c.setPhone(contact.getPhone());
			c.setExternalId(contact.getExternalUniqueId());
			response.setMainContact(c);
		} else {
			List<PersonInterface> people = new LookupServlet().lookupPeople(user.getName(), "mustHaveExternalId" + (session == null ? "" : ",session=" + session.getUniqueId()));
			if (people != null) {
				for (PersonInterface person: people) {
					if (user.getId().equals(person.getId())) {
						ContactInterface c = new ContactInterface();
						c.setFirstName(person.getFirstName());
						c.setMiddleName(person.getMiddleName());
						c.setLastName(person.getLastName());
						c.setEmail(person.getEmail());
						c.setPhone(person.getPhone());
						c.setExternalId(person.getId());
						response.setMainContact(c);
						break;
					}
				}
			}
			if (!response.hasMainContact()) {
				ContactInterface c = new ContactInterface();
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
				c.setExternalId(user.getId());
				
			}
		}
	}
	
	public void setupStandardNotes(Session session, EventPropertiesRpcResponse response) {
		for (StandardEventNote note: StandardEventNoteDAO.getInstance().findAll())
			response.addStandardNote(note.getNote());
		if (response.hasStandardNotes())
			Collections.sort(response.getStandardNotes());
	}

}
