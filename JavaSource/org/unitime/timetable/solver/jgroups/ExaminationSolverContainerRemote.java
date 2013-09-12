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
import org.jgroups.Message.Flag;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

public class ExaminationSolverContainerRemote extends ExaminationSolverContainer implements RemoteSolverContainer<ExamSolverProxy> {
	private static Log sLog = LogFactory.getLog(ExaminationSolverContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
	private RequestOptions iResponseOptions;
		
	public ExaminationSolverContainerRemote(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
		iResponseOptions = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
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
		ExamSolverProxy solver = iExamSolvers.get(user);
		if ("exists".equals(method) && types.length == 0)
			return solver != null;
		if (solver == null)
			throw new Exception("Solver " + user + " does not exist.");
		return solver.getClass().getMethod(method, types).invoke(solver, args);
	}
	
	@Override
	public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), user, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, iResponseOptions);
		} catch (Exception e) {
			sLog.error("Excution of " + method + " on solver " + user + " failed: " + e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	public ExamSolverProxy createProxy(Address address, String user) {
		SolverInvocationHandler handler = new SolverInvocationHandler(address, user);
		return (ExamSolverProxy)Proxy.newProxyInstance(
				ExamSolverProxy.class.getClassLoader(),
				new Class[] {ExamSolverProxy.class, RemoteSolver.class, },
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
    	
    	public String getHostLabel() {
    		return iAddress.toString();
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
