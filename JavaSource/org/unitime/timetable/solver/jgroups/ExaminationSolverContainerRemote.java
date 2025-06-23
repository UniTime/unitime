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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.SuspectedException;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.fork.ForkChannel;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.solver.exam.ExamSolverProxy;

/**
 * @author Tomas Muller
 */
public class ExaminationSolverContainerRemote extends ExaminationSolverContainer implements RemoteSolverContainer<ExamSolverProxy> {
	private static Log sLog = LogFactory.getLog(ExaminationSolverContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
	private ForkChannel iChannel;
		
	public ExaminationSolverContainerRemote(JChannel channel, short scope) throws Exception {
		iChannel = new ForkChannel(channel, String.valueOf(scope), "fork-" + scope);
		iDispatcher = new UniTimeRpcDispatcher(iChannel, this);
	}
	
	@Override
	public void setChannel(JChannel channel, short scope) throws Exception {
		ForkChannel oldChannel = iChannel; 
		if (channel != null) {
			iChannel = new ForkChannel(channel, String.valueOf(scope), "fork-" + scope);
			iChannel.connect("UniTime:RPC:Exams");
			iDispatcher = new UniTimeRpcDispatcher(iChannel, this);
		}
		if (oldChannel != null && oldChannel.isConnected()) {
			oldChannel.disconnect();
		}
	}
	
	@Override
	public void start() throws Exception {
		iChannel.connect("UniTime:RPC:Exams");
		super.start();
	}
	
	@Override
	public void stop() throws Exception {
		iChannel.disconnect();
		super.stop();
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	@Override
	public boolean createRemoteSolver(String user, DataProperties config, Address caller) {
		super.createSolver(user, config);
        return true;
	}
	
	@Override
	public Object invoke(String method, String user, String locale, Class[] types, Object[] args) throws Exception {
		try {
			ExamSolverProxy solver = iExamSolvers.get(user);
			if ("exists".equals(method) && types.length == 0)
				return solver != null;
			if (solver == null)
				throw new Exception("Solver " + user + " does not exist.");
			if (locale != null) Localization.setLocale(locale);
			return solver.getClass().getMethod(method, types).invoke(solver, args);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null && e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
		} finally {
			HibernateUtil.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",
					new Object[] { method.getName(), user, Localization.getLocale(), method.getParameterTypes(), args },
					new Class[] { String.class, String.class, String.class, Class[].class, Object[].class },
					SolverServerImplementation.sFirstResponse);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null && e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
		} catch (Exception e) {
			if ("exists".equals(method.getName()) && e instanceof SuspectedException) return false;
			sLog.error("Excution of " + method.getName() + " on solver " + user + " failed: " + e.getMessage(), e);
			throw e;
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
