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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.SuspectedException;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
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
	private EmbeddedCacheManager iCacheManager = null;
	private LockService iLockService;

	public OnlineStudentSchedulingContainerRemote(JChannel channel, short scope) {
		iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
		iLockService = new LockService(channel);
	}
	
	@Override
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	public LockService getLockService() { return iLockService; }
	
	@Override
	public void start() {
		super.start();
		GlobalConfiguration global = GlobalConfigurationBuilder.defaultClusteredBuilder()
				.transport().addProperty("channelLookup", "org.unitime.commons.jgroups.SectioningChannelLookup").clusterName("UniTime:sectioning")
				.globalJmxStatistics().cacheManagerName("OnlineSchedulingCacheManager").disable()
				.build();
		Configuration config = new ConfigurationBuilder()
				.clustering().cacheMode(CacheMode.REPL_ASYNC)
				.storeAsBinary().enable().defensive(true)
				.build();
		iCacheManager = new DefaultCacheManager(global, config);
	}
	
	@Override
	public void stop() {
		super.stop();
		iCacheManager.stop();
	}
	
	@Override
	public boolean hasMaster(String sessionId) {
		OnlineSectioningServer server = getInstance(Long.valueOf(sessionId));
		return server != null && server.isMaster();
	}
	
	@Override
	public boolean createRemoteSolver(String sessionId, DataProperties config, Address caller) {
		if (!canCreateSolver(Long.valueOf(sessionId))) return false;
		return super.createSolver(sessionId, config) != null;
	}
	
	protected boolean canCreateSolver(Long sessionId) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(sessionId, hibSession);
			if (session == null) return false;
			
			String year = ApplicationProperties.getProperty("unitime.enrollment.year");
			if (year != null && !session.getAcademicYear().matches(year)) return false;

			String term = ApplicationProperties.getProperty("unitime.enrollment.term");
			if (term != null && !session.getAcademicTerm().matches(term)) return false;

			String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
			if (campus != null && !session.getAcademicInitiative().matches(campus)) return false;

			return true;
		} finally {
			hibSession.close();
		}
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
			throw (Exception)e.getTargetException();
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public Object dispatch(Address address, String sessionId, Method method, Object[] args) throws Exception {
		try {
			return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), sessionId, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
		} catch (InvocationTargetException e) {
			throw (Exception)e.getTargetException();
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
					for (Rsp<Boolean> rsp : ret) {
						if (rsp != null && rsp.getValue()) {
							address = rsp.getSender();
							break;
						}
					}
				} else {
					List<Address> slaves = new ArrayList<Address>();
					for (Rsp<Boolean> rsp : ret) {
						if (rsp != null && !rsp.getValue()) {
							slaves.add(rsp.getSender());
						}
					}
					if (!slaves.isEmpty())
						address = ToolBox.random(slaves);
				}
				return dispatch(address, sessionId, method, args);
			}
		} catch (InvocationTargetException e) {
			throw (Exception)e.getTargetException();
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
			public EmbeddedCacheManager getCacheManager() {
				return OnlineStudentSchedulingContainerRemote.this.getCacheManager();
			}

			@Override
			public LockService getLockService() {
				return iLockService;
			}
		};
	}
    
	public EmbeddedCacheManager getCacheManager() {
		return iCacheManager;
	}
}
