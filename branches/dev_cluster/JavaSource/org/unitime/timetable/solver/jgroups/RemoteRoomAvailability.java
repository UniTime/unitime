package org.unitime.timetable.solver.jgroups;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message.Flag;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.util.RoomAvailability;

public class RemoteRoomAvailability {
	private static Log sLog = LogFactory.getLog(RemoteRoomAvailability.class);
	
	private RpcDispatcher iDispatcher;
	private RequestOptions iResponseOptions;
		
	public RemoteRoomAvailability(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
		iResponseOptions = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
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
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, iResponseOptions);
		} catch (Exception e) {
			sLog.error("Excution of room availability method " + method + " failed: " + e.getMessage(), e);
			return null;
		}
	}

}
