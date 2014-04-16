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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class DummySolverServer implements SolverServer, MessageListener {
	private static Log sLog = LogFactory.getLog(DummySolverServer.class);
	public static final RequestOptions sFirstResponse = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
	public static final RequestOptions sAllResponses = new RequestOptions(ResponseMode.GET_ALL, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
	
	private JChannel iChannel = null;
	private RpcDispatcher iDispatcher;
	protected Date iStartTime = new Date();
	private RpcDispatcher iRoomAvailabilityDispatcher;
	protected Properties iProperties = null;
	
	private DummyContainer<SolverProxy> iCourseSolverContainer;
	private DummyContainer<ExamSolverProxy> iExamSolverContainer;
	private DummyContainer<StudentSolverProxy> iStudentSolverContainer;
	private DummyContainer<OnlineSectioningServer> iOnlineStudentSchedulingContainer;

	private SolverContainerWrapper<SolverProxy> iCourseSolverContainerWrapper;
	private SolverContainerWrapper<ExamSolverProxy> iExamSolverContainerWrapper;
	private SolverContainerWrapper<StudentSolverProxy> iStudentSolverContainerWrapper;
	private SolverContainerWrapper<OnlineSectioningServer> iOnlineStudentSchedulingContainerWrapper;

	public DummySolverServer(JChannel channel) {
		iChannel = channel;
		iDispatcher = new MuxRpcDispatcher(SCOPE_SERVER, channel, null, null, this);
		iCourseSolverContainer = new DummyContainer<SolverProxy>(channel, SCOPE_COURSE, SolverProxy.class);
		iExamSolverContainer = new DummyContainer<ExamSolverProxy>(channel, SCOPE_EXAM, ExamSolverProxy.class);
		iStudentSolverContainer = new DummyContainer<StudentSolverProxy>(channel, SCOPE_STUDENT, StudentSolverProxy.class);
		iOnlineStudentSchedulingContainer = new ReplicatedDummyContainer<OnlineSectioningServer>(channel, SCOPE_ONLINE, OnlineSectioningServer.class);
		iRoomAvailabilityDispatcher = new MuxRpcDispatcher(SCOPE_AVAILABILITY, channel, null, null, this);
		
		iCourseSolverContainerWrapper = new SolverContainerWrapper<SolverProxy>(iDispatcher, iCourseSolverContainer, false);
		iExamSolverContainerWrapper = new SolverContainerWrapper<ExamSolverProxy>(iDispatcher, iExamSolverContainer, false);
		iStudentSolverContainerWrapper = new SolverContainerWrapper<StudentSolverProxy>(iDispatcher, iStudentSolverContainer, false);
		iOnlineStudentSchedulingContainerWrapper = new SolverContainerWrapper<OnlineSectioningServer>(iDispatcher, iOnlineStudentSchedulingContainer, false);
	}
	
	public Properties getProperties() {
		if (iProperties == null)
			iProperties = ApplicationProperties.getProperties();
		return iProperties;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public Address getAddress() {
		return iChannel.getAddress();
	}

	@Override
	public Address getLocalAddress() {
		try {
			RspList<Boolean> ret = iDispatcher.callRemoteMethods(null, "isLocal", new Object[] {}, new Class[] {}, sAllResponses);
			for (Rsp<Boolean> local: ret) {
				if (Boolean.TRUE.equals(local.getValue()))
					return local.getSender();
			}
			return null;
		} catch (Exception e) {
			sLog.error("Failed to retrieve local address: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public String getHost() {
		return iChannel.getAddressAsString();
	}

	@Override
	public Date getStartTime() {
		return iStartTime;
	}

	@Override
	public int getUsage() {
		return 0;
	}

	@Override
	public String getVersion() {
		return Constants.getVersion();
	}

	@Override
	public void setUsageBase(int usage) {
	}

	@Override
	public long getAvailableMemory() {
		return 0;
	}
	
	@Override
	public int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}

	@Override
	public long getMemoryLimit() {
		return 0;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public SolverContainer<SolverProxy> getCourseSolverContainer() {
		return iCourseSolverContainerWrapper;
	}
	
	@Override
	public SolverContainer<ExamSolverProxy> getExamSolverContainer() {
		return iExamSolverContainerWrapper;
	}
	
	@Override
	public SolverContainer<StudentSolverProxy> getStudentSolverContainer() {
		return iStudentSolverContainerWrapper;
	}
	
	@Override
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
		return iOnlineStudentSchedulingContainerWrapper;
	}

	@Override
	public RoomAvailabilityInterface getRoomAvailability() {
		Address local = getLocalAddress();
		if (local == null) return null;
		
		return (RoomAvailabilityInterface)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {RoomAvailabilityInterface.class},
				new RoomAvailabilityInvocationHandler(local));
	}
	
	public class RoomAvailabilityInvocationHandler implements InvocationHandler {
		private Address iAddress;
		
		private RoomAvailabilityInvocationHandler(Address address) {
			iAddress = address;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				return iRoomAvailabilityDispatcher.callRemoteMethod(iAddress, "invoke",  new Object[] { method.getName(), method.getParameterTypes(), args }, new Class[] { String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
			} catch (Exception e) {
				sLog.error("Excution of room availability method " + method + " failed: " + e.getMessage(), e);
				return null;
			}
		}
    }

	@Override
	public void refreshCourseSolution(Long... solutionId) {
		try {
			Address local = getLocalAddress();
			if (local != null)
				iDispatcher.callRemoteMethod(local, "refreshCourseSolutionLocal", new Object[] { solutionId }, new Class[] { Long[].class }, sFirstResponse);
		} catch (Exception e) {
			sLog.error("Failed to refresh solution: " + e.getMessage(), e);
		}
	}

	@Override
	public void refreshExamSolution(Long sessionId, Long examTypeId) {
		try {
			Address local = getLocalAddress();
			if (local != null)
				iDispatcher.callRemoteMethod(local, "refreshExamSolution", new Object[] { sessionId, examTypeId }, new Class[] { Long.class, Long.class }, sFirstResponse);
		} catch (Exception e) {
			sLog.error("Failed to refresh solution: " + e.getMessage(), e);
		}
	}
	
	public class DummyContainer<T> implements RemoteSolverContainer<T> {
		protected RpcDispatcher iDispatcher;
		protected Class<T> iClazz;
		
		public DummyContainer(JChannel channel, short scope, Class<T> clazz) {
			iDispatcher = new MuxRpcDispatcher(scope, channel, null, null, this);
			iClazz = clazz;
		}
		
		@Override
		public Set<String> getSolvers() {
			return new HashSet<String>();
		}

		@Override
		public T getSolver(String user) {
			return null;
		}

		@Override
		public long getMemUsage(String user) {
			return 0;
		}
		
		@Override
		public boolean hasSolver(String user) {
			return false;
		}

		@Override
		public T createSolver(String user, DataProperties config) {
			return null;
		}

		@Override
		public void unloadSolver(String user) {
		}

		@Override
		public int getUsage() {
			return 0;
		}

		@Override
		public void start() {
		}

		@Override
		public void stop() {
		}

		@Override
		public boolean createRemoteSolver(String user, DataProperties config, Address caller) {
			return false;
		}

		@Override
		public RpcDispatcher getDispatcher() {
			return iDispatcher;
		}

		@Override
		public Object dispatch(Address address, String user, Method method, Object[] args) throws Exception {
			try {
				return iDispatcher.callRemoteMethod(address, "invoke",  new Object[] { method.getName(), user, method.getParameterTypes(), args }, new Class[] { String.class, String.class, Class[].class, Object[].class }, SolverServerImplementation.sFirstResponse);
			} catch (Exception e) {
				sLog.debug("Excution of " + method.getName() + " on solver " + user + " failed: " + e.getMessage(), e);
				throw e;
			}
		}

		@Override
		public Object invoke(String method, String user, Class[] types, Object[] args) throws Exception {
			throw new Exception("Method " + method + " not implemented.");
		}

		@Override
		public T createProxy(Address address, String user) {
			SolverInvocationHandler handler = new SolverInvocationHandler(address, user);
			return (T)Proxy.newProxyInstance(
					iClazz.getClassLoader(),
					new Class[] {iClazz, RemoteSolver.class, },
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
	
	public class ReplicatedDummyContainer<T> extends DummyContainer<T> implements ReplicatedSolverContainer<T> {
		
		public ReplicatedDummyContainer(JChannel channel, short scope, Class<T> clazz) {
			super(channel, scope, clazz);
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
		public T createProxy(Collection<Address> addresses, String user) {
			ReplicatedServerInvocationHandler handler = new ReplicatedServerInvocationHandler(addresses, user);
			T px = (T)Proxy.newProxyInstance(
					iClazz.getClassLoader(),
					new Class[] {iClazz, RemoteSolver.class, },
					handler);
	    	return px;
		}

		@Override
		public boolean hasMaster(String user) {
			return false;
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
	}
	
	@Override
	public void receive(Message msg) {
		sLog.info("receive(" + msg + ", " + msg.getObject() + ")");
	}


	@Override
	public void getState(OutputStream output) throws Exception {
		getProperties().store(output, "UniTime Application Properties");
	}


	@Override
	public void setState(InputStream input) throws Exception {
		if (iProperties == null) {
			iProperties = new Properties();
		} else {
			iProperties.clear();
		}
		iProperties.load(input);
	}

	@Override
	public boolean isCoordinator() {
		return false;
	}

	@Override
	public void reset() {}

	@Override
	public void setApplicationProperty(Long sessionId, String key, String value) {
	}

	@Override
	public void setLoggingLevel(String name, Integer level) {
	}
}