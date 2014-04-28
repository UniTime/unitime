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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.InMemoryServer;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.jgroups.AbstractSolverServer;
import org.unitime.timetable.solver.jgroups.CourseSolverContainer;
import org.unitime.timetable.solver.jgroups.ExaminationSolverContainer;
import org.unitime.timetable.solver.jgroups.OnlineStudentSchedulingContainer;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.jgroups.StudentSolverContainer;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
public class LocalSolverServer extends AbstractSolverServer {
	private CourseSolverContainer iCourseSolverContainer;
	private ExaminationSolverContainer iExamSolverContainer;
	private StudentSolverContainer iStudentSolverContainer;
	private OnlineStudentSchedulingContainer iOnlineStudentSchedulingContainer;
	private Updater iUpdater;
	
	public LocalSolverServer() {
		super();
		
		iCourseSolverContainer = new CourseSolverContainer();
		iExamSolverContainer = new ExaminationSolverContainer();
		iStudentSolverContainer = new StudentSolverContainer();
		iOnlineStudentSchedulingContainer = new OnlineStudentSchedulingContainer();
		iUpdater = new Updater();
	}
	
	@Override
	public void start() {
		iCourseSolverContainer.start();
		iExamSolverContainer.start();
		iStudentSolverContainer.start();
		iOnlineStudentSchedulingContainer.start();
		iUpdater.start();
		
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();

		iCourseSolverContainer.stop();
		iExamSolverContainer.stop();
		iStudentSolverContainer.stop();
		iOnlineStudentSchedulingContainer.stop();
		iUpdater.stopUpdating();
	}
	
	@Override
	public void shutdown() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getUsage() {
		int ret = super.getUsage();
		ret += iCourseSolverContainer.getUsage();
		ret += iExamSolverContainer.getUsage();
		ret += iStudentSolverContainer.getUsage();
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
	public SolverContainer<OnlineSectioningServer> getOnlineStudentSchedulingContainer() {
		return iOnlineStudentSchedulingContainer;
	}
	
	public class Updater extends Thread {
		private Logger iLog;
		private long iSleepTimeInSeconds = 5;
		private boolean iRun = true, iPause = false;
		
		public Updater() {
			super();
			setDaemon(true);
			setName("Updater[generic]");
			iSleepTimeInSeconds = Long.parseLong(ApplicationProperties.getProperty("unitime.sectioning.queue.loadInterval", "300"));
			iLog = Logger.getLogger(OnlineStudentSchedulingGenericUpdater.class + ".updater[generic]");
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
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			try {
				Set<String> solvers = new HashSet<String>(iOnlineStudentSchedulingContainer.getSolvers());
				
				for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
					Session session = i.next();
					
					if (solvers.contains(session.getUniqueId().toString())) continue;
					if (session.getStatusType().isTestSession()) continue;
					if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;
					
					int nrSolutions = ((Number)hibSession.createQuery(
							"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId")
							.setLong("sessionId", session.getUniqueId()).uniqueResult()).intValue();
					if (nrSolutions == 0) continue;
					
					Properties properties = ApplicationProperties.getConfigProperties();
					if (properties.getProperty("unitime.enrollment.server.class") == null)
						properties.setProperty("unitime.enrollment.server.class", InMemoryServer.class.getName());

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
}
