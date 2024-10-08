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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.jgroups.Address;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.model.XClassEnrollment;
import org.unitime.timetable.onlinesectioning.server.InMemoryServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
public class LocalSolverServer extends AbstractSolverServer {
	private CourseSolverContainer iCourseSolverContainer;
	private ExaminationSolverContainer iExamSolverContainer;
	private StudentSolverContainer iStudentSolverContainer;
	private InstructorSchedulingContainer iInstructorSchedulingContainer;
	private OnlineStudentSchedulingContainer iOnlineStudentSchedulingContainer;
	private Updater iUpdater;
	
	public LocalSolverServer() {
		super();
		
		iCourseSolverContainer = new CourseSolverContainer();
		iExamSolverContainer = new ExaminationSolverContainer();
		iStudentSolverContainer = new StudentSolverContainer();
		iInstructorSchedulingContainer = new InstructorSchedulingContainer();
		iOnlineStudentSchedulingContainer = new OnlineStudentSchedulingContainer();
		iUpdater = new Updater();
	}
	
	@Override
	public void start() throws Exception {
		iCourseSolverContainer.start();
		iExamSolverContainer.start();
		iStudentSolverContainer.start();
		iInstructorSchedulingContainer.start();
		iOnlineStudentSchedulingContainer.start();
		iUpdater.start();
		
		super.start();
	}
	
	@Override
	public void stop() throws Exception {
		super.stop();

		iCourseSolverContainer.stop();
		iExamSolverContainer.stop();
		iStudentSolverContainer.stop();
		iInstructorSchedulingContainer.stop();
		iOnlineStudentSchedulingContainer.stop();
		iUpdater.stopUpdating();
	}
	
	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void reconnect() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUsage() {
		int ret = super.getUsage();
		ret += iCourseSolverContainer.getUsage();
		ret += iExamSolverContainer.getUsage();
		ret += iStudentSolverContainer.getUsage();
		ret += iInstructorSchedulingContainer.getUsage();
		ret += iOnlineStudentSchedulingContainer.getUsage();
		return ret;
	}

	@Override
	public List<SolverServer> getServers(boolean onlyAvailable) {
		List<SolverServer> servers = new ArrayList<SolverServer>();
		if (!onlyAvailable || isActive()) servers.add(this);
		return servers;
	}
	
	@Override
	public SolverServer crateServerProxy(Address address) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public SolverContainer<SolverProxy> getCourseSolverContainer() {
		return iCourseSolverContainer;
	}
	
	@Override
	public SolverContainer<ExamSolverProxy> getExamSolverContainer() {
		return iExamSolverContainer;
	}
	
	@Override
	public SolverContainer<StudentSolverProxy> getStudentSolverContainer() {
		return iStudentSolverContainer;
	}
	
	@Override
	public SolverContainer<InstructorSchedulingProxy> getInstructorSchedulingContainer() {
		return iInstructorSchedulingContainer;
	}
	
	@Override
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
		return iOnlineStudentSchedulingContainer;
	}
	
	public class Updater extends Thread {
		private Log iLog;
		private long iSleepTimeInSeconds = 5;
		private boolean iRun = true, iPause = false;
		
		public Updater() {
			super();
			setDaemon(true);
			setName("Updater[generic]");
			iSleepTimeInSeconds = ApplicationProperty.OnlineSchedulingQueueLoadInterval.intValue();
			iLog = LogFactory.getLog(OnlineStudentSchedulingGenericUpdater.class.getName() + ".updater[generic]");
		}
		
		@Override
		public void run() {
			try {
				iLog.info("Generic updater started.");
				while (iRun) {
					try {
						sleep(iSleepTimeInSeconds * 1000);
					} catch (InterruptedException e) {}
					if (iRun && !iPause)
						checkForNewServers();
				}
				iLog.info("Generic updater stopped.");
			} catch (Exception e) {
				iLog.error("Generic updater failed, " + e.getMessage(), e);
			}
		}
		
		public synchronized void pauseUpading() {
			iPause = true;
			iLog.info("Generic updater paused.");
		}
		
		public synchronized void resumeUpading() {
			interrupt();
			iPause = false;
			iLog.info("Generic updater resumed.");
		}
		
		public void stopUpdating() {
			iRun = false;
			
			interrupt();
			try {
				this.join();
			} catch (InterruptedException e) {}
		}
		
		public synchronized void checkForNewServers() {
			if (!HibernateUtil.isConfigured()) {
				iLog.info("Hibernate is not yet configured, waiting...");
				return;
			}
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				Set<String> solvers = new HashSet<String>(iOnlineStudentSchedulingContainer.getSolvers());
				
				for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
					Session session = i.next();
					
					if (solvers.contains(session.getUniqueId().toString())) continue;
					if (session.getStatusType().isTestSession()) continue;
					if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;
					
					int nrSolutions = (hibSession.createQuery(
							"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId", Number.class)
							.setParameter("sessionId", session.getUniqueId()).uniqueResult()).intValue();
					if (nrSolutions == 0) continue;
					
					Properties properties = ApplicationProperties.getConfigProperties();
					if (ApplicationProperty.OnlineSchedulingServerClass.value() == null)
						properties.setProperty(ApplicationProperty.OnlineSchedulingServerClass.key(), InMemoryServer.class.getName());

					try {
						iOnlineStudentSchedulingContainer.createSolver(session.getUniqueId().toString(), null);
					} catch (Exception e) {
						iLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				}
			} finally {
				hibSession.close();
			}
		}
	}

	@Override
	public void unloadSolver(SolverType type, String id) {
		switch (type) {
		case COURSE:
			getCourseSolverContainer().unloadSolver(id);
			break;
		case EXAM:
			getExamSolverContainer().unloadSolver(id);
			break;
		case INSTRUCTOR:
			getInstructorSchedulingContainer().unloadSolver(id);
			break;
		case STUDENT:
			getStudentSolverContainer().unloadSolver(id);
			break;
		}
	}
	
	@Override
	public Collection<XClassEnrollment> getUnavailabilitiesFromOtherSessions(AcademicSessionInfo session, String studentExternalId) {
		return iOnlineStudentSchedulingContainer.getUnavailabilitiesFromOtherSessions(session, studentExternalId);
	}
	
	@Override
	public float[] getCreditRangeFromOtherSessions(AcademicSessionInfo session, String studentExternalId) {
		return iOnlineStudentSchedulingContainer.getCreditRangeFromOtherSessions(session, studentExternalId);
	}
}
