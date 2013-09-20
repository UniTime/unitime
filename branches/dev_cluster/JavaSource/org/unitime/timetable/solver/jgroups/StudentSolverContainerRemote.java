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
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

public class StudentSolverContainerRemote extends StudentSolverContainer implements RemoteSolverContainer<StudentSolverProxy> {
	private static Log sLog = LogFactory.getLog(StudentSolverContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
		
	public StudentSolverContainerRemote(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	@Override
	public boolean createRemoteSolver(String user, DataProperties config, Address caller) {
		super.createSolver(user, config);
        return true;
	}
	
	@Override
	public Object invoke(String method, String user, Class[] types, Object[] args) throws Exception {
		try {
			StudentSolverProxy solver = iStudentSolvers.get(user);
			if ("exists".equals(method) && types.length == 0)
				return solver != null;
			if (solver == null)
				throw new Exception("Solver " + user + " does not exist.");
			return solver.getClass().getMethod(method, types).invoke(solver, args);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), user, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (Exception e) {
			sLog.error("Excution of " + method + " on solver " + user + " failed: " + e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public StudentSolverProxy createProxy(Address address, String user) {
		SolverInvocationHandler handler = new SolverInvocationHandler(address, user);
		return (StudentSolverProxy)Proxy.newProxyInstance(
				StudentSolverProxy.class.getClassLoader(),
				new Class[] {StudentSolverProxy.class, RemoteSolver.class, },
				handler);
	}
    
    public class SolverInvocationHandler implements InvocationHandler {
    	private Address iAddress;
    	private String iUser;
    	
    	private SolverInvocationHandler(Address address, String user) {
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