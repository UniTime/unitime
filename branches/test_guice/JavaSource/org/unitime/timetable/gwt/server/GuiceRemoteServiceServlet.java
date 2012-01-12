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
package org.unitime.timetable.gwt.server;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class GuiceRemoteServiceServlet extends RemoteServiceServlet {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private Injector iInjector;

	@Override
	public String processCall(String payload) throws SerializationException {
		try {
			RPCRequest rpcRequest = RPC.decodeRequest(payload, null, this);
			RemoteService service = getServiceInstance(rpcRequest.getMethod().getDeclaringClass());
			return RPC.invokeAndEncodeResponse(service, rpcRequest.getMethod(), rpcRequest.getParameters(), rpcRequest.getSerializationPolicy());
		} catch (IncompatibleRemoteServiceException ex) {
			return RPC.encodeResponseForFailure(null, ex);
	    }
	}
	
	@SuppressWarnings({"unchecked"})
	private RemoteService getServiceInstance(Class serviceClass) {
		return (RemoteService) iInjector.getInstance(serviceClass);
	}
}