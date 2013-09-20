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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.SuspectedException;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.SolverProxy;

public class OnlineStudentSchedulingContainerRemote extends OnlineStudentSchedulingContainer implements RemoteSolverContainer<OnlineSectioningServer> {
	private static Log sLog = LogFactory.getLog(OnlineStudentSchedulingContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
	private OnlineStudentSchedulingGenericUpdater iUpdater;
		
	public OnlineStudentSchedulingContainerRemote(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
		iUpdater = new OnlineStudentSchedulingGenericUpdater(channel, this);
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	@Override
	public void start() {
		super.start();
		iUpdater.start();
	}
	
	@Override
	public void stop() {
		super.stop();
		iUpdater.stopUpdating();
	}
	
	@Override
	public boolean createRemoteSolver(String sessionId, DataProperties config, Address caller) {
		super.createSolver(sessionId, config);
        return true;
	}
	
	@Override
	public Object invoke(String method, String sessionId, Class[] types, Object[] args) throws Exception {
		try {
			OnlineSectioningServer solver = iInstances.get(Long.valueOf(sessionId));
			if ("exists".equals(method) && types.length == 0)
				return solver != null;
			if (solver == null)
				throw new Exception("Server " + sessionId + " does not exist.");
			return solver.getClass().getMethod(method, types).invoke(solver, args);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public Object dispatch(Address address, String sessionId, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), sessionId, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (Exception e) {
			if ("exists".equals(method.getName()) && e instanceof SuspectedException) return false;
			sLog.error("Excution of " + method + " on server " + sessionId + " failed: " + e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public OnlineSectioningServer createProxy(Address address, String user) {
		ServerInvocationHandler handler = new ServerInvocationHandler(address, user);
		OnlineSectioningServer px = (OnlineSectioningServer)Proxy.newProxyInstance(
				SolverProxy.class.getClassLoader(),
				new Class[] {OnlineSectioningServer.class, RemoteSolver.class, },
				handler);
    	return px;
	}
    
    public class ServerInvocationHandler implements InvocationHandler {
    	private Address iAddress;
    	private String iUser;
    	
    	private ServerInvocationHandler(Address address, String user) {
    		iAddress = address;
    		iUser = user;
    	}
    	
    	public String getHost() {
    		return iAddress.toString();
    	}
    	
    	public String getUser() {
    		return iUser;
    	}
    	
    	@Override
    	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		try {
    			return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    		} catch (NoSuchMethodException e) {}
    		return dispatch(iAddress, iUser, method, args);
        }
    }

}
