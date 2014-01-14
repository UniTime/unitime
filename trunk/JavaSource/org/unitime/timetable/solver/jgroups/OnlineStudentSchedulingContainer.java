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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jgroups.blocks.locking.LockService;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.server.ReplicatedServerWithMaster;

/**
 * @author Tomas Muller
 */
public class OnlineStudentSchedulingContainer implements SolverContainer<OnlineSectioningServer> {
	private static Log sLog = LogFactory.getLog(OnlineStudentSchedulingContainer.class);
	
	protected Hashtable<Long, OnlineSectioningServer> iInstances = new Hashtable<Long, OnlineSectioningServer>();
	private Hashtable<Long, OnlineStudentSchedulingUpdater> iUpdaters = new Hashtable<Long, OnlineStudentSchedulingUpdater>();
	
	private ReentrantReadWriteLock iGlobalLock = new ReentrantReadWriteLock();

	@Override
	public Set<String> getSolvers() {
		iGlobalLock.readLock().lock();
		try {
			TreeSet<String> ret = new TreeSet<String>();
			for (Map.Entry<Long, OnlineSectioningServer> entry: iInstances.entrySet()) {
				try {
					ret.add(entry.getValue().getAcademicSession().getUniqueId().toString());
				} catch (IllegalStateException e) {
					sLog.error("Server " + entry.getKey() + " appears to be in an inconsistent state: " + e.getMessage());
				}
			}
			return ret;
		} finally {
			iGlobalLock.readLock().unlock();
		}
	}

	@Override
	public OnlineSectioningServer getSolver(String sessionId) {
		return getInstance(Long.valueOf(sessionId));
	}
	
	@Override
	public long getMemUsage(String user) {
		OnlineSectioningServer solver = getSolver(user);
		return solver == null ? 0 : solver.getMemUsage();
	}
	
	public OnlineSectioningServer getInstance(Long sessionId) {
		iGlobalLock.readLock().lock();
		try {
			OnlineSectioningServer instance = iInstances.get(sessionId);
			try {
				instance.getAcademicSession();
			} catch (IllegalStateException e) {
				sLog.error("Server " + sessionId + " appears to be in an inconsistent state: " + e.getMessage(), e);
				return null;
			}
			return instance;
		} finally {
			iGlobalLock.readLock().unlock();
		}
	}

	@Override
	public boolean hasSolver(String sessionId) {
		iGlobalLock.readLock().lock();
		try {
			return iInstances.containsKey(Long.valueOf(sessionId));
		} finally {
			iGlobalLock.readLock().unlock();
		}
	}

	@Override
	public OnlineSectioningServer createSolver(String sessionId, DataProperties config) {
		return createInstance(Long.valueOf(sessionId), config);
	}
	
	public OnlineSectioningServer createInstance(final Long academicSessionId, DataProperties config) {
		unload(academicSessionId, true);
		iGlobalLock.writeLock().lock();
		try {
			ApplicationProperties.setSessionId(academicSessionId);
			Class serverClass = Class.forName(ApplicationProperties.getProperty("unitime.enrollment.server.class", ReplicatedServerWithMaster.class.getName()));
			OnlineSectioningServer server = (OnlineSectioningServer)serverClass.getConstructor(OnlineSectioningServerContext.class).newInstance(getServerContext(academicSessionId));
			iInstances.put(academicSessionId, server);
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				OnlineStudentSchedulingUpdater updater = new OnlineStudentSchedulingUpdater(this, server.getAcademicSession(), StudentSectioningQueue.getLastTimeStamp(hibSession, academicSessionId));
				iUpdaters.put(academicSessionId, updater);
				updater.start();
			} finally {
				hibSession.close();
			}
			return server;
		} catch (SectioningException e) {
			throw e;
		} catch (Exception e) {
			throw new SectioningException(e.getMessage(), e);
		} finally {
			iGlobalLock.writeLock().unlock();
			ApplicationProperties.setSessionId(null);
		}
	}
	
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
				return null;
			}

			@Override
			public LockService getLockService() {
				return null;
			}
		};
	}
	
	@Override
	public void unloadSolver(String sessionId) {
		unload(Long.valueOf(sessionId), true);
	}
	
	public void unload(Long academicSessionId, boolean interrupt) {
		iGlobalLock.writeLock().lock();
		try {
			OnlineStudentSchedulingUpdater u = iUpdaters.get(academicSessionId);
			if (u != null)
				u.stopUpdating(interrupt);
			OnlineSectioningServer s = iInstances.get(academicSessionId);
			if (s != null) {
				sLog.info("Unloading " + u.getAcademicSession() + "...");
				s.unload(true);
			}
			iInstances.remove(academicSessionId);
			iUpdaters.remove(academicSessionId);
		} finally {
			iGlobalLock.writeLock().unlock();
		}
	}

	@Override
	public int getUsage() {
		iGlobalLock.readLock().lock();
		int ret = 0;
		try {
			for (OnlineSectioningServer s: iInstances.values())
				if (s.isMaster())
					ret += 200;
				else
					ret += 100;
		} finally {
			iGlobalLock.readLock().unlock();
		}
		return ret;
	}

	@Override
	public void start() {
		sLog.info("Student Sectioning Service is starting up ...");
		OnlineSectioningLogger.startLogger();
	}
	
	public boolean isEnabled() {
		// if autostart is enabled, just check whether there are some instances already loaded in
		if ("true".equals(ApplicationProperties.getProperty("unitime.enrollment.autostart", "false")))
			return !iInstances.isEmpty();
		
		// quick check for existing instances
		if (!iInstances.isEmpty()) return true;
		
		// otherwise, look for a session that has sectioning enabled
		String year = ApplicationProperties.getProperty("unitime.enrollment.year");
		String term = ApplicationProperties.getProperty("unitime.enrollment.term");
		String campus = ApplicationProperties.getProperty("unitime.enrollment.campus");
		for (Iterator<Session> i = SessionDAO.getInstance().findAll().iterator(); i.hasNext(); ) {
			final Session session = i.next();
			
			if (year != null && !year.equals(session.getAcademicYear())) continue;
			if (term != null && !term.equals(session.getAcademicTerm())) continue;
			if (campus != null && !campus.equals(session.getAcademicInitiative())) continue;
			if (session.getStatusType().isTestSession()) continue;

			if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;

			return true;
		}
		return false;
	}
	
	public boolean isRegistrationEnabled() {
		for (Session session: SessionDAO.getInstance().findAll()) {
			if (session.getStatusType().isTestSession()) continue;
			if (!session.getStatusType().canOnlineSectionStudents() && !session.getStatusType().canSectionAssistStudents() && session.getStatusType().canPreRegisterStudents()) return true;
		}
		return false;
	}
	
	public void unloadAll(boolean remove) {
		iGlobalLock.writeLock().lock();
		try {
			for (OnlineStudentSchedulingUpdater u: iUpdaters.values()) {
				u.stopUpdating(true);
				if (u.getAcademicSession() != null) {
					OnlineSectioningServer s = iInstances.get(u.getAcademicSession().getUniqueId());
					if (s != null) s.unload(remove);
				}
			}
			iInstances.clear();
			iUpdaters.clear();
		} finally {
			iGlobalLock.writeLock().unlock();
		}		
	}

	@Override
	public void stop() {
		sLog.info("Student Sectioning Service is going down ...");
		unloadAll(false);
		OnlineSectioningLogger.stopLogger();
	}

}
