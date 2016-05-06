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
package org.unitime.timetable.solver.service;

import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.unitime.commons.jgroups.UniTimeChannelLookup;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.jgroups.LocalSolverServer;
import org.unitime.timetable.solver.jgroups.RemoteSolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.jgroups.SolverContainerWrapper;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.SolverServerImplementation;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@Service("solverServerService")
@DependsOn({"startupService"})
public class SolverServerService implements InitializingBean, DisposableBean {
	private static Log sLog = LogFactory.getLog(SolverServerService.class);
	private JChannel iChannel = null;
	private SolverServer iServer = null;
	
	private SolverContainer<SolverProxy> iCourseSolverContainer;
	private SolverContainer<ExamSolverProxy> iExamSolverContainer;
	private SolverContainer<StudentSolverProxy> iStudentSolverContainer;
	private SolverContainer<InstructorSchedulingProxy> iInstructorSchedulingContainer;
	private SolverContainer<OnlineSectioningServer> iOnlineStudentSchedulingContainer;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			if (ApplicationProperty.SolverClusterEnabled.isFalse()) {
				iServer = new LocalSolverServer();
				
				iServer.start();
				
				iCourseSolverContainer = iServer.getCourseSolverContainer();
				iExamSolverContainer = iServer.getExamSolverContainer();
				iStudentSolverContainer = iServer.getStudentSolverContainer();
				iInstructorSchedulingContainer = iServer.getInstructorSchedulingContainer();
				iOnlineStudentSchedulingContainer = iServer.getOnlineStudentSchedulingContainer();
			} else {
				iChannel = (JChannel) new UniTimeChannelLookup().getJGroupsChannel(null);
				
				iServer = new SolverServerImplementation(true, iChannel);
				
				iChannel.connect("UniTime:rpc");
				
				iServer.start();
				
				iCourseSolverContainer = new SolverContainerWrapper<SolverProxy>(
						((SolverServerImplementation)iServer).getDispatcher(),
						(RemoteSolverContainer<SolverProxy>) iServer.getCourseSolverContainer(), true);
				iExamSolverContainer = new SolverContainerWrapper<ExamSolverProxy>(
						((SolverServerImplementation)iServer).getDispatcher(),
						(RemoteSolverContainer<ExamSolverProxy>) iServer.getExamSolverContainer(), true);
				iStudentSolverContainer = new SolverContainerWrapper<StudentSolverProxy>(
						((SolverServerImplementation)iServer).getDispatcher(),
						(RemoteSolverContainer<StudentSolverProxy>) iServer.getStudentSolverContainer(), true);
				iInstructorSchedulingContainer = new SolverContainerWrapper<InstructorSchedulingProxy>(
						((SolverServerImplementation)iServer).getDispatcher(),
						(RemoteSolverContainer<InstructorSchedulingProxy>) iServer.getInstructorSchedulingContainer(), true);
				iOnlineStudentSchedulingContainer = new SolverContainerWrapper<OnlineSectioningServer>(
						((SolverServerImplementation)iServer).getDispatcher(),
						(RemoteSolverContainer<OnlineSectioningServer>) iServer.getOnlineStudentSchedulingContainer(), false);
			}
		} catch (Exception e) {
			sLog.fatal("Failed to start solver server: " + e.getMessage(), e);
		}
	}
	
	private RpcDispatcher getDispatcher() {
		if (iServer instanceof SolverServerImplementation)
			return ((SolverServerImplementation)iServer).getDispatcher();
		return null;
	}

	@Override
	public void destroy() throws Exception {
		try {
			sLog.info("Server is going down...");
			iServer.stop();
			
			if (iChannel != null) {
				sLog.info("Disconnecting from the channel...");
				iChannel.disconnect();
			
				sLog.info("Closing the channel...");
				iChannel.close();
			}
			
			iServer = null; 
		} catch (Exception e) {
			sLog.fatal("Failed to stop solver server: " + e.getMessage(), e);
		}
	}

	public List<SolverServer> getServers(boolean onlyAvailable) {
		return iServer.getServers(onlyAvailable);
	}
	
	public SolverServer getLocalServer() {
		return iServer;
	}
	
	public SolverContainer<SolverProxy> getCourseSolverContainer() {
		return iCourseSolverContainer;
	}
	
	public SolverProxy createCourseSolver(String host, String user, DataProperties properties) {
	    if (host != null) {
	    	if ("local".equals(host)) {
	    		SolverProxy solver = iServer.getCourseSolverContainer().createSolver(user, properties);
    			return solver;
	    	}	
	    	for (SolverServer server: iServer.getServers(true)) {
	    		if (server.getHost().equals(host)) {
	    			SolverProxy solver = server.getCourseSolverContainer().createSolver(user, properties);
	    			return solver;
	    		}
	    	}
	    }
	    SolverProxy solver = iCourseSolverContainer.createSolver(user, properties);
	    return solver;
	}
	
	public SolverContainer<ExamSolverProxy> getExamSolverContainer() {
		return iExamSolverContainer;
	}
	
	public ExamSolverProxy createExamSolver(String host, String user, DataProperties properties) {
	    if (host != null) {
	    	if ("local".equals(host)) {
	    		ExamSolverProxy solver = iServer.getExamSolverContainer().createSolver(user, properties);
    			return solver;
	    	}	
	    	for (SolverServer server: iServer.getServers(true)) {
	    		if (server.getHost().equals(host)) {
	    			ExamSolverProxy solver = server.getExamSolverContainer().createSolver(user, properties);
	    			return solver;
	    		}
	    	}
	    }
	    ExamSolverProxy solver = iExamSolverContainer.createSolver(user, properties);
	    return solver;
	}
	
	public SolverContainer<StudentSolverProxy> getStudentSolverContainer() {
		return iStudentSolverContainer;
	}
	
	public StudentSolverProxy createStudentSolver(String host, String user, DataProperties properties) {
	    if (host != null) {
	    	if ("local".equals(host)) {
	    		StudentSolverProxy solver = iServer.getStudentSolverContainer().createSolver(user, properties);
    			return solver;
	    	}	
	    	for (SolverServer server: iServer.getServers(true)) {
	    		if (server.getHost().equals(host)) {
	    			StudentSolverProxy solver = server.getStudentSolverContainer().createSolver(user, properties);
	    			return solver;
	    		}
	    	}
	    }
	    StudentSolverProxy solver = iStudentSolverContainer.createSolver(user, properties);
	    return solver;
	}
	
	public SolverContainer<InstructorSchedulingProxy> getInstructorSchedulingContainer() {
		return iInstructorSchedulingContainer;
	}
	
	public InstructorSchedulingProxy createInstructorScheduling(String host, String user, DataProperties properties) {
	    if (host != null) {
	    	if ("local".equals(host)) {
	    		InstructorSchedulingProxy solver = iServer.getInstructorSchedulingContainer().createSolver(user, properties);
    			return solver;
	    	}	
	    	for (SolverServer server: iServer.getServers(true)) {
	    		if (server.getHost().equals(host)) {
	    			InstructorSchedulingProxy solver = server.getInstructorSchedulingContainer().createSolver(user, properties);
	    			return solver;
	    		}
	    	}
	    }
	    InstructorSchedulingProxy solver = iInstructorSchedulingContainer.createSolver(user, properties);
	    return solver;
	}
	
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
		return iOnlineStudentSchedulingContainer;
	}
	
	public SolverServer getServer(String host) {
		if ("local".equals(host) || host == null)
			return iServer;
		if (iChannel != null)
			for (Address address: iChannel.getView().getMembers()) {
				if (host.equals(address.toString()))
					return iServer.crateServerProxy(address);
			}
		return null;
	}
	
	public boolean isOnlineStudentSchedulingEnabled() {
		return !getOnlineStudentSchedulingContainer().getSolvers().isEmpty();
	}
	
	public boolean isStudentRegistrationEnabled() {
        for (Session session: SessionDAO.getInstance().findAll()) {
                if (session.getStatusType().isTestSession()) continue;
                if (!session.getStatusType().canOnlineSectionStudents() && !session.getStatusType().canSectionAssistStudents() && session.getStatusType().canPreRegisterStudents()) return true;
        }
        return false;
	}
	
	public void setApplicationProperty(Long sessionId, String key, String value) {
		try {
			RpcDispatcher dispatcher = getDispatcher();
			if (dispatcher != null)
				dispatcher.callRemoteMethods(null, "setApplicationProperty", new Object[] { sessionId, key, value }, new Class[] { Long.class, String.class, String.class }, SolverServerImplementation.sAllResponses);
			else
				iServer.setApplicationProperty(sessionId, key, value);
		} catch (Exception e) {
			sLog.error("Failed to update the application property " + key + " along the cluster: " + e.getMessage(), e);
		}
	}
	
	public void setLoggingLevel(String name, Integer level) {
		try {
			RpcDispatcher dispatcher = getDispatcher();
			if (dispatcher != null)
				dispatcher.callRemoteMethods(null, "setLoggingLevel", new Object[] { name, level }, new Class[] { String.class, Integer.class }, SolverServerImplementation.sAllResponses);
			else
				iServer.setLoggingLevel(name, level);
		} catch (Exception e) {
			sLog.error("Failed to update the logging level for " + name + " along the cluster: " + e.getMessage(), e);
		}
	}
}
