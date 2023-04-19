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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.SuspectedException;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.fork.ForkChannel;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.solver.SolverProxy;

/**
 * @author Tomas Muller
 */
public class OnlineStudentSchedulingContainerRemote extends OnlineStudentSchedulingContainer implements ReplicatedSolverContainer<OnlineSectioningServer> {
	private static Log sLog = LogFactory.getLog(OnlineStudentSchedulingContainerRemote.class);
	
	private RpcDispatcher iDispatcher;
	private LockService iLockService;
	private ForkChannel iChannel;

	public OnlineStudentSchedulingContainerRemote(JChannel channel, short scope) throws Exception {
		iChannel = new ForkChannel(channel, String.valueOf(scope), "fork-" + scope);
		iDispatcher = new RpcDispatcher(iChannel, this);
		iLockService = null;//new LockService(channel);
	}
	
	@Override
	public void start() throws Exception {
		iChannel.connect("UniTime:RPC:Online");
		super.start();
	}
	
	@Override
	public void stop() throws Exception {
		iChannel.disconnect();
		super.stop();
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	public LockService getLockService() { return iLockService; }
	
	@Override
	public boolean hasMaster(String sessionId) {
		OnlineSectioningServer server = getInstance(Long.valueOf(sessionId));
		return server != null && server.isMaster();
	}

	@Override
	public boolean createRemoteSolver(String sessionId, DataProperties config, Address caller) {
		return super.createSolver(sessionId, config) != null;
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
	public Object dispatch(Address address, String sessionId, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), sessionId, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null && e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
		} catch (Exception e) {
			if ("exists".equals(method.getName()) && e instanceof SuspectedException) return false;
			sLog.debug("Excution of " + method.getName() + " on server " + sessionId + " failed: " + e.getMessage(), e);
			throw e;
		}
	}
	
	@Override
	public Object dispatch(Collection<Address> addresses, String sessionId, Method method, Object[] args) throws Exception {
		try {
			if (addresses.size() == 1) {
				return dispatch(ToolBox.random(addresses), sessionId, method, args);
			} else {
				Address address = ToolBox.random(addresses);
				CheckMaster ch = method.getAnnotation(CheckMaster.class);
				if (ch == null && "execute".equals(method.getName()))
					ch = args[0].getClass().getAnnotation(CheckMaster.class);
				RspList<Boolean> ret = iDispatcher.callRemoteMethods(addresses, "hasMaster", new Object[] { sessionId }, new Class[] { String.class }, SolverServerImplementation.sAllResponses);
				if (ch != null && ch.value() == Master.REQUIRED) {
					for (Map.Entry<Address, Rsp<Boolean>> entry : ret.entrySet()) {
						Address sender = entry.getKey();
						Rsp<Boolean> rsp = entry.getValue();
						if (rsp != null && rsp.getValue()) {
							address = sender;
							break;
						}
					}
				} else {
					List<Address> slaves = new ArrayList<Address>();
					for (Map.Entry<Address, Rsp<Boolean>> entry : ret.entrySet()) {
						Address sender = entry.getKey();
						Rsp<Boolean> rsp = entry.getValue();
						if (rsp != null && !rsp.getValue()) {
							slaves.add(sender);
						}
					}
					if (!slaves.isEmpty())
						address = ToolBox.random(slaves);
				}
				return dispatch(address, sessionId, method, args);
			}
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null && e.getTargetException() instanceof Exception)
				throw (Exception)e.getTargetException();
			else
				throw e;
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
	
	@Override
	public OnlineSectioningServer createProxy(Collection<Address> addresses, String user) {
		ReplicatedServerInvocationHandler handler = new ReplicatedServerInvocationHandler(addresses, user);
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

    
    public class ReplicatedServerInvocationHandler implements InvocationHandler {
    	private Collection<Address> iAddresses;
    	private String iUser;
    	
    	private ReplicatedServerInvocationHandler(Collection<Address> addresses, String user) {
    		iAddresses = addresses;
    		iUser = user;
    	}
    	
    	public String getHost() {
    		return iAddresses.toString();
    	}
    	
    	public String getUser() {
    		return iUser;
    	}
    	
    	@Override
    	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		try {
    			return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    		} catch (NoSuchMethodException e) {}
    		return dispatch(iAddresses, iUser, method, args);
        }
    }
    
    @Override
    public OnlineSectioningServerContext getServerContext(final Long academicSessionId) {
		return new OnlineSectioningServerContext() {
			@Override
			public Long getAcademicSessionId() {
				return academicSessionId;
			}

			@Override
			public boolean isWaitTillStarted() {
				return false;
			}

			@Override
			public LockService getLockService() {
				return iLockService;
			}
		};
	}
}
