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
package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.fork.ForkChannel;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class RemoteRoomAvailability {
	private static Log sLog = LogFactory.getLog(RemoteRoomAvailability.class);
	
	private RpcDispatcher iDispatcher;
	private ForkChannel iChannel;
		
	public RemoteRoomAvailability(JChannel channel, short scope) throws Exception {
		iChannel = new ForkChannel(channel, String.valueOf(scope), "fork-" + scope);
		iDispatcher = new UniTimeRpcDispatcher(iChannel, this);
	}
	
	public void setChannel(JChannel channel, short scope) throws Exception {
		ForkChannel oldChannel = iChannel; 
		if (channel != null) {
			iChannel = new ForkChannel(channel, String.valueOf(scope), "fork-" + scope);
			iChannel.connect("UniTime:RPC:Remote");
			iDispatcher = new UniTimeRpcDispatcher(iChannel, this);
		}
		if (oldChannel != null && oldChannel.isConnected()) {
			oldChannel.disconnect();
		}
	}
	
	public void start() throws Exception {
		iChannel.connect("UniTime:RPC:Availability");
	}
	
	public void stop() throws Exception {
		iChannel.disconnect();
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
			HibernateUtil.closeCurrentThreadSessions();
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
