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

import java.io.Serializable;
import java.util.Date;

import org.unitime.timetable.gwt.shared.EventInterface.EventType;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Meeting;

public interface EventRights extends Serializable {
	/**
	 * Creates a default page access exception
	 * @return Authentication required if not authenticated, insufficient rights if authenticated
	 */
	public PageAccessException getException();
	
	/**
	 * Throws an exception if access to Event Management features is disabled
	 * @throws PageAccessException when not authenticated but the authentication is required to access events
	 */
	public void checkAccess() throws PageAccessException;
	
	/**
	 * Check whether an event can be created by the current user.
	 * @param type event type, Special Event if null
	 * @param userId event main contact (current user if null)
	 * @return true if the user can create an event 
	 */
	public boolean canAddEvent(EventType type, String userId);
	
	/**
	 * Check if people lookup can be enabled while creating a new event
	 * @return true if the user can lookup people for event contacts
	 */
	public boolean canLookupContacts();
	
	/**
	 * Check if the user can see a schedule of the given user
	 * @param userId user external id, any user if null 
	 * @return true if a schedule of given user can be displayed
	 */
	public boolean canSeeSchedule(String userId);

	/**
	 * Check if the details of an event can be seen by the user.
	 * @param event an event
	 * @return true if the user can open Event Detail page for an event
	 */
	public boolean canSee(Event event);
	
	/**
	 * Check if the given event can be edited (e.g., a new meeting can be added to it).
	 * @param event an event
	 * @return true if the user can open Edit Event page for an event
	 */
	public boolean canEdit(Event event);
	
	/**
	 * Check if the given time is in the past or outside of the term (to be used with {@link Meeting#getStartTime()}).
	 * @param date start time of a meeting
	 * @return true if the meeting is in the past (e.g., it should not be tinkered with)
	 */
	public boolean isPastOrOutside(Date date);
	
	/**
	 * Check if the given meeting can be edited (e.g., removed).
	 * Both {@link EventRights#canEdit(Event)} and {@link EventRights#canEdit(Meeting)} should be true.
	 * @param meeting a meeting
	 * @return true if the user can edit the given meeting
	 */
	public boolean canEdit(Meeting meeting);
	
	/**
	 * Check if the given meeting can be approved / rejected / inquired
	 * @param meeting a meeting
	 * @return true if the user can approve / reject / inquire the given meeting
	 */
	public boolean canApprove(Meeting meeting);

	
	/**
	 * Check if the given location is an event location
	 * @param location a location (null to check if there is at least one such location)
	 * @return if the given location is event location (UniTime is maintaining events for this location) 
	 */
	public boolean isEventLocation(Long locationId);
	
	/**
	 * Check if the user can create a meeting using the given location.
	 * @param location a location
	 * @return true, if a meeting can be created using the given location
	 */
	public boolean canCreate(Long locationId);
		
	/**
	 * Check if the user can approve meetings in the given location.
	 * @param location a location
	 * @return true if a newly created meeting by the user should get automatically approved
	 */
	public boolean canApprove(Long locationId);
	
	/**
	 * Check if the user can overbook meetings in the given location.
	 * @param location a location
	 * @return true if the user can create a meeting in the room that is conflicting with some other meeting
	 */
	public boolean canOverbook(Long locationId);
}
