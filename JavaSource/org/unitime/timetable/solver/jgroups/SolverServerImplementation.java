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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.MergeView;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.Receiver;
import org.jgroups.SuspectedException;
import org.jgroups.Message.Flag;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.mux.MuxRpcDispatcher;
import org.jgroups.blocks.mux.MuxUpHandler;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.commons.jgroups.UniTimeChannelLookup;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.RoomAvailabilityInterface;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.RoomAvailability;

/**
 * @author Tomas Muller
 */
public class SolverServerImplementation implements MessageListener, MembershipListener, SolverServer, Receiver {
	private static Log sLog = LogFactory.getLog(SolverServerImplementation.class);
	private static SolverServerImplementation sInstance = null;
	public static final RequestOptions sFirstResponse = new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
	public static final RequestOptions sAllResponses = new RequestOptions(ResponseMode.GET_ALL, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB);
	
	private JChannel iChannel;
	private RpcDispatcher iDispatcher;
	
	private CourseSolverContainerRemote iCourseSolverContainer;
	private ExaminationSolverContainerRemote iExamSolverContainer;
	private StudentSolverContainerRemote iStudentSolverContainer;
	private OnlineStudentSchedulingContainerRemote iOnlineStudentSchedulingContainer;
	private RemoteRoomAvailability iRemoteRoomAvailability;
	private OnlineStudentSchedulingGenericUpdater iUpdater;
	
	protected int iUsageBase = 0;
	protected Date iStartTime = new Date();
	protected boolean iActive = false;
	protected boolean iLocal = false;
	
	public SolverServerImplementation(boolean local, JChannel channel) {
		iLocal = local;
		iChannel = channel;
		// iChannel.setReceiver(this);
		iChannel.setUpHandler(new MuxUpHandler());
		iDispatcher = new MuxRpcDispatcher(SCOPE_SERVER, channel, this, this, this);
		
		iCourseSolverContainer = new CourseSolverContainerRemote(channel, SCOPE_COURSE);
		iExamSolverContainer = new ExaminationSolverContainerRemote(channel, SCOPE_EXAM);
		iStudentSolverContainer = new StudentSolverContainerRemote(channel, SCOPE_STUDENT);
		iOnlineStudentSchedulingContainer = new OnlineStudentSchedulingContainerRemote(channel, SCOPE_ONLINE);
		iRemoteRoomAvailability = new RemoteRoomAvailability(channel, SCOPE_AVAILABILITY);
		iUpdater = new OnlineStudentSchedulingGenericUpdater(iDispatcher, iOnlineStudentSchedulingContainer);
	}
	
	public JChannel getChannel() { return iChannel; }
	
	public RpcDispatcher getDispatcher() { return iDispatcher; }
	
	public void start() throws Exception {
		iCourseSolverContainer.start();
		iExamSolverContainer.start();
		iStudentSolverContainer.start();
		iOnlineStudentSchedulingContainer.start();
		iUpdater.start();

		iActive = true;
		sLog.info("Solver server is up and running.");
	}
	
	public void stop() throws Exception {
		iActive = false;
		
		iCourseSolverContainer.stop();
		iExamSolverContainer.stop();
		iStudentSolverContainer.stop();
		iOnlineStudentSchedulingContainer.stop();
		iUpdater.stopUpdating();
	}
	
	@Override
	public boolean isLocal() {
		return iLocal;
	}
	
	@Override
	public Address getAddress() {
		return iChannel.getAddress();
	}
	
	@Override
	public Address getLocalAddress() {
		if (isLocal()) return getAddress();
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
	public int getUsage() {
		int ret = iUsageBase;
		if (isLocal()) ret += 500;
		ret += iCourseSolverContainer.getUsage();
		ret += iExamSolverContainer.getUsage();
		ret += iStudentSolverContainer.getUsage();
		ret += iOnlineStudentSchedulingContainer.getUsage();
		return ret;
	}
	
	@Override
	public void setUsageBase(int base) {
		iUsageBase = base;
	}
	
	@Override
	public long getAvailableMemory() {
		return Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory();
	}
	
	@Override
	public int getAvailableProcessors() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	@Override
	public long getMemoryLimit() {
		return 1024l * 1024l * Long.parseLong(ApplicationProperties.getProperty(ApplicationProperty.SolverMemoryLimit));
	}
	
	@Override
	public String getVersion() {
		return Constants.getVersion();
	}
	
	public Date getStartTime() {
		return iStartTime;
	}
	
	@Override
	public boolean isActive() {
		return iActive;
	}
	
	@Override
	public boolean isAvailable() {
		if (!isActive()) return false;
		if (getMemoryLimit() > getAvailableMemory()) System.gc();
		return getMemoryLimit() <= getAvailableMemory();
	}
	
	public List<SolverServer> getServers(boolean onlyAvailable) {
		List<SolverServer> servers = new ArrayList<SolverServer>();
		if (!onlyAvailable || isActive()) servers.add(this);
		for (Address address: iChannel.getView().getMembers()) {
			if (address.equals(iChannel.getAddress())) continue;
			SolverServer server = crateServerProxy(address);
			if (onlyAvailable && !server.isAvailable()) continue;
			servers.add(server);
		}
		return servers;
	}
	
	public SolverServer crateServerProxy(Address address) {
		ServerInvocationHandler handler = new ServerInvocationHandler(address);
		SolverServer px = (SolverServer)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {SolverServer.class},
				handler
				);
		return px;
	}
	
	@Override
	public SolverContainer<SolverProxy> getCourseSolverContainer() {
		return iCourseSolverContainer;
	}
	
	public SolverContainer<SolverProxy> createCourseSolverContainerProxy(Address address) {
		ContainerInvocationHandler<RemoteSolverContainer<SolverProxy>> handler = new ContainerInvocationHandler<RemoteSolverContainer<SolverProxy>>(address, iCourseSolverContainer);
		SolverContainer<SolverProxy> px = (SolverContainer<SolverProxy>)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {SolverContainer.class},
				handler
				);
		return px;
	}
	
	@Override
	public SolverContainer<ExamSolverProxy> getExamSolverContainer() {
		return iExamSolverContainer;
	}
	
	public SolverContainer<ExamSolverProxy> createExamSolverContainerProxy(Address address) {
		ContainerInvocationHandler<RemoteSolverContainer<ExamSolverProxy>> handler = new ContainerInvocationHandler<RemoteSolverContainer<ExamSolverProxy>>(address, iExamSolverContainer);
		SolverContainer<ExamSolverProxy> px = (SolverContainer<ExamSolverProxy>)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {SolverContainer.class},
				handler
				);
		return px;
	}
	
	@Override
	public SolverContainer<StudentSolverProxy> getStudentSolverContainer() {
		return iStudentSolverContainer;
	}
	
	public SolverContainer<StudentSolverProxy> createStudentSolverContainerProxy(Address address) {
		ContainerInvocationHandler<RemoteSolverContainer<StudentSolverProxy>> handler = new ContainerInvocationHandler<RemoteSolverContainer<StudentSolverProxy>>(address, iStudentSolverContainer);
		SolverContainer<StudentSolverProxy> px = (SolverContainer<StudentSolverProxy>)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {SolverContainer.class},
				handler
				);
		return px;
	}
	
	@Override
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
		return iOnlineStudentSchedulingContainer;
	}
	
	public SolverContainer<OnlineSectioningServer> createOnlineStudentSchedulingContainerProxy(Address address) {
		ContainerInvocationHandler<RemoteSolverContainer<OnlineSectioningServer>> handler = new ContainerInvocationHandler<RemoteSolverContainer<OnlineSectioningServer>>(address, iOnlineStudentSchedulingContainer);
		SolverContainer<OnlineSectioningServer> px = (SolverContainer<OnlineSectioningServer>)Proxy.newProxyInstance(
				SolverServerImplementation.class.getClassLoader(),
				new Class[] {SolverContainer.class},
				handler
				);
		return px;
	}
	
	@Override
	public RoomAvailabilityInterface getRoomAvailability() {
		if (isLocal())
			return RoomAvailability.getInstance();

		Address local = getLocalAddress();
		if (local != null)
			return (RoomAvailabilityInterface)Proxy.newProxyInstance(
					SolverServerImplementation.class.getClassLoader(),
					new Class[] {RoomAvailabilityInterface.class},
					new RoomAvailabilityInvocationHandler(local, iRemoteRoomAvailability));

		return null;
	}
	
	public void refreshCourseSolutionLocal(Long... solutionIds) {
		try {
			for (Long solutionId: solutionIds)
				Solution.refreshSolution(solutionId);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public void refreshCourseSolution(Long... solutionIds) {
		if (isLocal()) {
			refreshCourseSolutionLocal(solutionIds);
		} else {
			try {
				Address local = getLocalAddress();
				if (local != null)
					iDispatcher.callRemoteMethod(local, "refreshCourseSolutionLocal", new Object[] { solutionIds }, new Class[] { Long[].class }, sFirstResponse);
			} catch (Exception e) {
				sLog.error("Failed to refresh solution: " + e.getMessage(), e);
			}
		}
	}
	
	public void refreshExamSolutionLocal(Long sessionId, Long examTypeId) {
		try {
			ExamType.refreshSolution(sessionId, examTypeId);
		} finally {
			_RootDAO.closeCurrentThreadSessions();
		}
	}
	
	@Override
	public void refreshExamSolution(Long sessionId, Long examTypeId) {
		if (isLocal()) {
			refreshExamSolutionLocal(sessionId, examTypeId);
		} else {
			try {
				Address local = getLocalAddress();
				if (local != null)
					iDispatcher.callRemoteMethod(local, "refreshExamSolution", new Object[] { sessionId, examTypeId }, new Class[] { Long.class, Long.class }, sFirstResponse);
			} catch (Exception e) {
				sLog.error("Failed to refresh solution: " + e.getMessage(), e);
			}
		}
	}
	
	public static class RoomAvailabilityInvocationHandler implements InvocationHandler {
		private Address iAddress;
		private RemoteRoomAvailability iAvailability;
		
		private RoomAvailabilityInvocationHandler(Address address, RemoteRoomAvailability availability) {
			iAddress = address;
			iAvailability = availability;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		return iAvailability.dispatch(iAddress, method, args);
		}
    }
	
	@Override
	public void viewAccepted(View view) {
		sLog.info("viewAccepted(" + view + ")");
		if (view instanceof MergeView) {
			reset();
		}
	}


	@Override
	public void suspect(Address suspected_mbr) {
		sLog.warn("suspect(" + suspected_mbr + ")");
	}


	@Override
	public void block() {
		sLog.info("block");
	}


	@Override
	public void unblock() {
		sLog.info("unblock");
	}


	@Override
	public void receive(Message msg) {
		sLog.info("receive(" + msg + ", " + msg.getObject() + ")");
	}


	@Override
	public void getState(OutputStream output) throws Exception {
	}


	@Override
	public void setState(InputStream input) throws Exception {
	}
	
	public class ServerInvocationHandler implements InvocationHandler {
		private Address iAddress;
		
		public ServerInvocationHandler(Address address) {
			iAddress = address;
		}
		
		public SolverContainer<SolverProxy> getCourseSolverContainer() {
			return createCourseSolverContainerProxy(iAddress);
		}
		
		public SolverContainer<ExamSolverProxy> getExamSolverContainer() {
			return createExamSolverContainerProxy(iAddress);
		}
		
		public SolverContainer<StudentSolverProxy> getStudentSolverContainer() {
			return createStudentSolverContainerProxy(iAddress);
		}
		
		public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
			return createOnlineStudentSchedulingContainerProxy(iAddress);
		}
		
		public Address getAddress() {
			return iAddress;
		}
		
		public String getHost() {
			return iAddress.toString();
		}

		public boolean isActive() throws Exception {
			try {
				Boolean active = iDispatcher.callRemoteMethod(iAddress, "isActive", new Object[] {}, new Class[] {},
						new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB));
				return active;
			} catch (SuspectedException e) {
				return false;
			}
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		try {
    			return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    		} catch (NoSuchMethodException e) {}
    		return iDispatcher.callRemoteMethod(iAddress, method.getName(), args, method.getParameterTypes(),
    				new RequestOptions(ResponseMode.GET_FIRST, 0).setFlags(Flag.DONT_BUNDLE, Flag.OOB));
		}

	}
	
	public class ContainerInvocationHandler<T extends RemoteSolverContainer> implements InvocationHandler {
		private Address iAddress;
		private T iContainer;
		
		private ContainerInvocationHandler(Address address, T container) {
			iAddress = address;
			iContainer = container;
		}
		
		public Object createSolver(String user, DataProperties config) throws Throwable {
			iContainer.getDispatcher().callRemoteMethod(iAddress, "createRemoteSolver", new Object[] { user, config, iChannel.getAddress() }, new Class[] { String.class, DataProperties.class, Address.class}, sFirstResponse);
			return iContainer.createProxy(iAddress, (String)user);
		}
		
		public Address getAddress() {
			return iAddress;
		}
		
		public String getHost() {
			return iAddress.toString();
		}
		
		public Object getSolver(String user) throws Exception {
			Boolean ret = iContainer.getDispatcher().callRemoteMethod(iAddress, "hasSolver", new Object[] { user }, new Class[] { String.class }, sFirstResponse);
			if (ret)
				return iContainer.createProxy(iAddress, user);
			return null;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    		try {
    			return getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
    		} catch (NoSuchMethodException e) {}
    		return iContainer.getDispatcher().callRemoteMethod(iAddress, method.getName(), args, method.getParameterTypes(), sFirstResponse);
		}
    }
	
	@Override
	public void shutdown() {
		iActive = false;
		new ShutdownThread().start();
	}
	
	public static SolverServer getInstance() {
		return sInstance;
	}
	
	private class ShutdownThread extends Thread {
		ShutdownThread() {
			setName("SolverServer:Shutdown");
		}
		
		@Override
		public void run() {
			try {
				try {
					sleep(500);
				} catch (InterruptedException e) {}
				
				sLog.info("Server is going down...");
				
				SolverServerImplementation.this.stop();
				
				sLog.info("Disconnecting from the channel...");
				getChannel().disconnect();
				
				sLog.info("This is the end.");
				System.exit(0);
			} catch (Exception e) {
				sLog.error("Failed to stop the server: " + e.getMessage(), e);
			}
		}
	}
	
    public static void main(String[] args) {
    	try {
    		if (ApplicationProperties.getProperty("unitime.data.dir") == null)
    			ApplicationProperties.getDefaultProperties().setProperty("unitime.data.dir", ".");
    		
    		if (System.getProperty("catalina.base") == null)
    			ApplicationProperties.getDefaultProperties().setProperty("catalina.base", ".");
    		
			ToolBox.configureLogging();
    		
			HibernateUtil.configureHibernate(ApplicationProperties.getProperties());
			
			ToolBox.configureLogging(System.getProperty("unitime.solver.log", ApplicationProperties.getDataFolder() + File.separator + "logs"), ApplicationProperties.getProperties());
			
			final JChannel channel = (JChannel) new UniTimeChannelLookup().getJGroupsChannel(null);
			
			sInstance = new SolverServerImplementation(false, channel);
			
			channel.connect("UniTime:rpc");
			
			channel.getState(null, 0);
			
			sInstance.start();
			
    		Runtime.getRuntime().addShutdownHook(new Thread() {
    			public void run() {
    				try {
        				sInstance.iActive = false;

        				sLog.info("Server is going down...");
    					
    					sInstance.stop();
    					
    					sLog.info("Disconnecting from the channel...");
    					channel.disconnect();
    					
    					sLog.info("Closing the channel...");
    					channel.close();
    					
    					sLog.info("Closing hibernate...");
    					HibernateUtil.closeHibernate();
    					
    					sLog.info("This is the end.");
    				} catch (Exception e) {
    					sLog.error("Failed to stop the server: " + e.getMessage(), e);
    				}
    			}
    		});
    		
    	} catch (Exception e) {
    		sLog.error("Failed to start the server: " + e.getMessage(), e);
    	}
    }

	@Override
	public boolean isCoordinator() {
		return (iUpdater != null && iUpdater.isCoordinator());
	}

	@Override
	public synchronized void reset() {
		sLog.info(iOnlineStudentSchedulingContainer.getLockService().printLocks());
		
		// For each of my online student sectioning solvers
		for (String session: iOnlineStudentSchedulingContainer.getSolvers()) {
			OnlineSectioningServer server = iOnlineStudentSchedulingContainer.getSolver(session);
			if (server == null) continue;
			
			// mark server for reload and release the lock
			if (server.isMaster()) {
				sLog.info("Marking " + server.getAcademicSession() + " for reload");
				server.setProperty("ReadyToServe", Boolean.FALSE);
				server.setProperty("ReloadIsNeeded", Boolean.TRUE);

				sLog.info("Releasing master lock for " + server.getAcademicSession() + " ...");
				server.releaseMasterLockIfHeld();
			}
		}
	}
}