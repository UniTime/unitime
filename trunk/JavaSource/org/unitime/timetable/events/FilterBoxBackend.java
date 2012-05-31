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

import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

public abstract class FilterBoxBackend extends EventAction<FilterRpcRequest, FilterRpcResponse> {

	@Override
	public FilterRpcResponse execute(FilterRpcRequest request, GwtRpcHelper helper, EventRights rights) {
		if (helper.getUser() != null) {
			request.addOption("user", helper.getUser().getId());
			if (helper.getUser().getCurrentRole() != null)
				request.addOption("role", helper.getUser().getCurrentRole());
		}
		
		FilterRpcResponse response = new FilterRpcResponse();
		
		switch (request.getCommand()) {
			case LOAD:
				load(request, response, rights);
				break;
			case SUGGESTIONS:
				suggestions(request, response, rights);
				break;
			case ENUMERATE:
				enumarate(request, response, rights);
				break;
		}
		
		return response;
	}
	
	public abstract void load(FilterRpcRequest request, FilterRpcResponse response, EventRights rights);
	
	public abstract void suggestions(FilterRpcRequest request, FilterRpcResponse response, EventRights rights);
	
	public abstract void enumarate(FilterRpcRequest request, FilterRpcResponse response, EventRights rights);	

}
