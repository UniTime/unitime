/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class RemoteRoomAvailability {
	private static Log sLog = LogFactory.getLog(RemoteRoomAvailability.class);
	
	private RpcDispatcher iDispatcher;
		
	public RemoteRoomAvailability(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
	}
	
	public RpcDispatcher getDispatcher() {
		return iDispatcher;
	}
	
	public Object invoke(String method, Class[] types, Object[] args) throws Exception {
		try {
			RoomAvailabilityInterface availability = RoomAvailability.getInstance();
			if (availability == null)
				throw new Exception("There is no room availability.");
			return availability.getClass().getMethod(method, types).invoke(availability, args);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	public Object dispatch(Address address, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (Exception e) {
			sLog.error("Excution of room availability method " + method + " failed: " + e.getMessage(), e);
			return null;
		}
	}

}
