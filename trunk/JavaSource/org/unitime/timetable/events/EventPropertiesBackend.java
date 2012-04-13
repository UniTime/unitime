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

import java.util.List;

import org.unitime.commons.User;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.server.LookupServlet;
import org.unitime.timetable.gwt.shared.PersonInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EventPropertiesRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.SponsoringOrganizationInterface;
import org.unitime.timetable.model.EventContact;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SponsoringOrganization;

public class EventPropertiesBackend implements GwtRpcImplementation<EventPropertiesRpcRequest, EventPropertiesRpcResponse>{

	@Override
	public EventPropertiesRpcResponse execute(EventPropertiesRpcRequest request, GwtRpcHelper helper) {
		EventPropertiesRpcResponse response = new EventPropertiesRpcResponse();
		
		response.setCanLookupPeople(canLookupPeople(helper.getUser()));
		
		response.setCanAddEvent(canAddEvent(helper.getUser()));
		
		setupSponsoringOrganizations(response);
		
		setupMainContact(response, helper.getUser());
		
		return response;
	}
	
	public boolean canLookupPeople(User user) {
		return user != null && (Roles.ADMIN_ROLE.equals(user.getRole()) || Roles.STUDENT_ADVISOR.equals(user.getRole()) || Roles.DEPT_SCHED_MGR_ROLE.equals(user.getRole()));
	}
	
	public boolean canAddEvent(User user) {
		return user != null;
	}

	public void setupSponsoringOrganizations(EventPropertiesRpcResponse response) {
		for (SponsoringOrganization s: SponsoringOrganization.findAll()) {
			SponsoringOrganizationInterface sponsor = new SponsoringOrganizationInterface();
			sponsor.setUniqueId(s.getUniqueId());
			sponsor.setName(s.getName());
			sponsor.setEmail(s.getEmail());
			response.addSponsoringOrganization(sponsor);
		}
	}
	
	public void setupMainContact(EventPropertiesRpcResponse response, User user) {
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
			List<PersonInterface> people = new LookupServlet().lookupPeople(user.getName(), "mustHaveExternalId");
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

}
