/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.services;

import java.util.List;

import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.shared.EventException;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.IdValueInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.PageAccessException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Tomas Muller
 */
public interface EventServiceAsync {
	public void findResource(String session, ResourceType type, String name, AsyncCallback<ResourceInterface> callback) throws EventException, PageAccessException;
	public void findEvents(ResourceInterface resource, UniTimeFilterBox.FilterRpcRequest events, AsyncCallback<List<EventInterface>> callback) throws EventException, PageAccessException;
	public void findSessions(String session, AsyncCallback<List<IdValueInterface>> callback)  throws EventException, PageAccessException;
	public void findResources(String session, ResourceType type, String query, int limit, AsyncCallback<List<ResourceInterface>> callback) throws EventException, PageAccessException;
	public void canLookupPeople(AsyncCallback<Boolean> callback) throws EventException, PageAccessException;
}
