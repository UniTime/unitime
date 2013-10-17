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
package org.unitime.timetable.onlinesectioning.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.DistanceMetric;
import net.sf.cpsolver.ifs.util.JProf;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.CacheElement;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.solver.StudentSchedulingAssistantWeights;
import org.unitime.timetable.onlinesectioning.updates.CheckAllOfferingsAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public abstract class AbstractServer implements OnlineSectioningServer {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected Log iLog = LogFactory.getLog(AbstractServer.class);
	private AcademicSessionInfo iAcademicSession = null;
	private DistanceMetric iDistanceMetric = null;
	private DataProperties iConfig = null;
	
	private AsyncExecutor iExecutor;
	private Queue<Runnable> iExecutorQueue = new LinkedList<Runnable>();
	private HashSet<CacheElement<Long>> iOfferingsToPersistExpectedSpaces = new HashSet<CacheElement<Long>>();
	private static ThreadLocal<LinkedList<OnlineSectioningHelper>> sHelper = new ThreadLocal<LinkedList<OnlineSectioningHelper>>();
	
	private MasterAcquiringThread iMasterThread;
	
	public AbstractServer(OnlineSectioningServerContext context) throws SectioningException {
		iConfig = new ServerConfig();
		iDistanceMetric = new DistanceMetric(iConfig);
		TravelTime.populateTravelTimes(iDistanceMetric, context.getAcademicSessionId());
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(context.getAcademicSessionId(), hibSession);
			if (session == null)
				throw new SectioningException(MSG.exceptionSessionDoesNotExist(context.getAcademicSessionId() == null ? "null" : context.getAcademicSessionId().toString()));
			iAcademicSession = new AcademicSessionInfo(session);
			iLog = LogFactory.getLog(OnlineSectioningServer.class.getName() + ".server[" + iAcademicSession.toCompactString() + "]");
			iExecutor = new AsyncExecutor();
			iExecutor.start();
		} finally {
			hibSession.close();
		}
		iLog.info("Config: " + ToolBox.dict2string(iConfig, 2));
		
		load(context);
	}
	
	protected void load(OnlineSectioningServerContext context) throws SectioningException {
		if (context.getLockService() != null) {
			iMasterThread = new MasterAcquiringThread(context);
			iMasterThread.start();
		}
	}
		
	protected void loadOnMaster(OnlineSectioningServerContext context) throws SectioningException {
		try {
			final OnlineSectioningLog.Entity user = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
					.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
			if (context.isWaitTillStarted()) {
				try {
					execute(new ReloadAllData(), user);
				} catch (Throwable exception) {
					iLog.error("Failed to load server: " + exception.getMessage(), exception);
					throw exception;
				}
				if (iAcademicSession.isSectioningEnabled()) {
					try {
						execute(new CheckAllOfferingsAction(), user);
					} catch (Throwable exception) {
						iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
						throw exception;
					}
				}
			} else {
				execute(new ReloadAllData(), user, new ServerCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (iAcademicSession.isSectioningEnabled())
							execute(new CheckAllOfferingsAction(), user, new ServerCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {}
								@Override
								public void onFailure(Throwable exception) {
									iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
								}
							});
					}
					@Override
					public void onFailure(Throwable exception) {
						iLog.error("Failed to load server: " + exception.getMessage(), exception);
					}
				});
			}
		} catch (Throwable t) {
			if (t instanceof SectioningException) throw (SectioningException)t;
			throw new SectioningException(MSG.exceptionUnknown(t.getMessage()), t);
		}
	}
	
	@Override
	public boolean isMaster() {
		return (iMasterThread != null ? iMasterThread.isMaster() : true);
	}
	
	@Override
	public void releaseMasterLockIfHeld() {
		if (iMasterThread != null)
			iMasterThread.release();
	}
	
	@Override
	public DistanceMetric getDistanceMetric() { return iDistanceMetric; }
	
	@Override
	public AcademicSessionInfo getAcademicSession() { return iAcademicSession; }
	
	@Override
	public String getCourseDetails(Long courseId, CourseDetailsProvider provider) {
		XCourse course = getCourse(courseId);
		return course == null ? null : course.getDetails(getAcademicSession(), provider);
	}
	
	protected OnlineSectioningHelper getCurrentHelper() {
		LinkedList<OnlineSectioningHelper> h = sHelper.get();
		if (h == null || h.isEmpty())
			return new OnlineSectioningHelper(null);
		return h.peek();
	}
	
	protected void setCurrentHelper(OnlineSectioningHelper helper) {
		LinkedList<OnlineSectioningHelper> h = sHelper.get();
		if (h == null) {
			h = new LinkedList<OnlineSectioningHelper>();
			sHelper.set(h);
		}
		h.push(helper);
	}
	
	protected void releaseCurrentHelper() {
		LinkedList<OnlineSectioningHelper> h = sHelper.get();
		h.poll();
		if (h.isEmpty())
			sHelper.remove();
	}

	@Override
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException {
		long c0 = OnlineSectioningHelper.getCpuTime();
		String cacheMode = getConfig().getProperty(action.name() + ".CacheMode", getConfig().getProperty("CacheMode"));
		OnlineSectioningHelper h = new OnlineSectioningHelper(user, cacheMode != null ? CacheMode.valueOf(cacheMode) : action instanceof HasCacheMode ? ((HasCacheMode)action).getCacheMode() : null);
		
		try {
			setCurrentHelper(h);
			h.addMessageHandler(new OnlineSectioningHelper.DefaultMessageLogger(LogFactory.getLog(OnlineSectioningServer.class.getName() + "." + action.name() + "[" + getAcademicSession().toCompactString() + "]")));
			h.addAction(action, getAcademicSession());
			E ret = action.execute(this, h);
			if (h.getAction() != null) {
				if (ret == null)
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.NULL);
				else if (ret instanceof Boolean)
					h.getAction().setResult((Boolean)ret ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
				else
					h.getAction().setResult(OnlineSectioningLog.Action.ResultType.SUCCESS);
			}
			return ret;
		} catch (Exception e) {
			if (e instanceof SectioningException) {
				if (e.getCause() == null) {
					h.info("Execution failed: " + e.getMessage());
				} else {
					h.warn("Execution failed: " + e.getMessage(), e.getCause());
				}
			} else {
				h.error("Execution failed: " + e.getMessage(), e);
			}
			if (h.getAction() != null) {
				h.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
				if (e.getCause() != null && e instanceof SectioningException)
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getCause().getClass().getName() + ": " + e.getCause().getMessage()));
				else
					h.getAction().addMessage(OnlineSectioningLog.Message.newBuilder()
							.setLevel(OnlineSectioningLog.Message.Level.FATAL)
							.setText(e.getMessage() == null ? "null" : e.getMessage()));
			}
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		} finally {
			if (h.getAction() != null) {
				h.getAction().setEndTime(System.currentTimeMillis()).setCpuTime(OnlineSectioningHelper.getCpuTime() - c0);
				if ((!h.getAction().hasStudent() || !h.getAction().getStudent().hasExternalId()) &&
					user != null && user.hasExternalId() &&
					user.hasType() && user.getType() == OnlineSectioningLog.Entity.EntityType.STUDENT) {
					if (h.getAction().hasStudent()) {
						h.getAction().getStudentBuilder().setExternalId(user.getExternalId());
					} else {
						h.getAction().setStudent(OnlineSectioningLog.Entity.newBuilder().setExternalId(user.getExternalId()));
					}
				}
			}
			iLog.debug("Executed: " + h.getLog() + " (" + h.getLog().toByteArray().length + " bytes)");
			OnlineSectioningLogger.getInstance().record(h.getLog());
			releaseCurrentHelper();
		}
	}
	
	@Override
	public <E> void execute(final OnlineSectioningAction<E> action, final OnlineSectioningLog.Entity user, final ServerCallback<E> callback) throws SectioningException {
		final String locale = Localization.getLocale();
		synchronized (iExecutorQueue) {
			iExecutorQueue.offer(new Runnable() {
				@Override
				public void run() {
					Localization.setLocale(locale);
					try {
						callback.onSuccess(execute(action, user));
					} catch (Throwable t) {
						callback.onFailure(t);
					}
				}
				
				@Override
				public String toString() {
					return action.name();
				}
			});
			iExecutorQueue.notify();
		}
	}
	
	public class AsyncExecutor extends Thread {
		private boolean iStop = false;
		
		public AsyncExecutor() {
			setName("AsyncExecutor[" + getAcademicSession() + "]");
			setDaemon(true);
		}
		
		public void run() {
			try {
				ApplicationProperties.setSessionId(getAcademicSession().getUniqueId());
				Runnable job;
				while (!iStop) {
					synchronized (iExecutorQueue) {
						job = iExecutorQueue.poll();
						if (job == null) {
							try {
								iLog.info("Executor is waiting for a new job...");
								iExecutorQueue.wait();
							} catch (InterruptedException e) {}
							continue;
						}		
					}
					job.run();
					if (_RootDAO.closeCurrentThreadSessions())
						iLog.debug("Job " + job + " did not close current-thread hibernate session.");
				}
				iLog.info("Executor stopped.");
			} finally {
				ApplicationProperties.setSessionId(null);
				Localization.removeLocale();
				Formats.removeFormats();
			}
		}
		
	}
	
	@Override
	public void unload(boolean remove) {
		if (iExecutor != null) {
			iExecutor.iStop = true;
			synchronized (iExecutorQueue) {
				iExecutorQueue.notify();
			}
		}
		if (iMasterThread != null)
			iMasterThread.dispose();
	}

	@Override
	public DataProperties getConfig() {
		return iConfig;
	}

	@Override
	public void persistExpectedSpaces(Long offeringId) {
		synchronized(iOfferingsToPersistExpectedSpaces) {
			iOfferingsToPersistExpectedSpaces.add(new CacheElement<Long>(offeringId));
		}
	}
	
	@Override
	public List<Long> getOfferingsToPersistExpectedSpaces(long minimalAge) {
		List<Long> offeringIds = new ArrayList<Long>();
		long current = JProf.currentTimeMillis();
		synchronized (iOfferingsToPersistExpectedSpaces) {
			for (Iterator<CacheElement<Long>> i = iOfferingsToPersistExpectedSpaces.iterator(); i.hasNext(); ) {
				CacheElement<Long> c = i.next();
				if (current - c.created() >= minimalAge) {
					offeringIds.add(c.element());
					i.remove();
				}
			}
		}
		return offeringIds;
	}
	
	@Override
	public boolean needPersistExpectedSpaces(Long offeringId) {
		synchronized(iOfferingsToPersistExpectedSpaces) {
			return iOfferingsToPersistExpectedSpaces.remove(offeringId);
		}
	}

	@Override
	public boolean checkDeadline(Long courseId, XTime sectionTime, Deadline type) {
		if (!"true".equals(ApplicationProperties.getProperty("unitime.enrollment.deadline", "true"))) return true;
		
		XCourse info = getCourse(courseId);
		int deadline = 0;
		switch (type) {
		case NEW:
			if (info != null && info.getLastWeekToEnroll() != null)
				deadline = info.getLastWeekToEnroll();
			else
				deadline = getAcademicSession().getLastWeekToEnroll();
			break;
		case CHANGE:
			if (info != null && info.getLastWeekToChange() != null)
				deadline = info.getLastWeekToChange();
			else
				deadline = getAcademicSession().getLastWeekToChange();
			break;
		case DROP:
			if (info != null && info.getLastWeekToDrop() != null)
				deadline = info.getLastWeekToDrop();
			else
				deadline = getAcademicSession().getLastWeekToDrop();
			break;
		}
		long start = getAcademicSession().getSessionBeginDate().getTime();
		long now = new Date().getTime();
		int week = 0;
		if (now >= start) {
			week = (int)((now - start) / (1000 * 60 * 60 * 24 * 7)) + 1;
		} else {
			week = -(int)((start - now) / (1000 * 60 * 60 * 24 * 7));
		}

		if (sectionTime == null)
			return week <= deadline; // no time, just compare week and the deadline
		
		int offset = 0;
		long time = getAcademicSession().getDatePatternFirstDate().getTime() + (long) sectionTime.getWeeks().nextSetBit(0) * (1000l * 60l * 60l * 24l);
		if (time >= start) {
			offset = (int)((time - start) / (1000 * 60 * 60 * 24 * 7));
		} else {
			offset = -(int)((start - time) / (1000 * 60 * 60 * 24 * 7)) - 1;
		}
		
		return week <= deadline + offset;
	}
	
	private static class ServerConfig extends DataProperties {
		private static final long serialVersionUID = 1L;

		private ServerConfig() {
			super();
			setProperty("Neighbour.BranchAndBoundTimeout", "1000");
			setProperty("Suggestions.Timeout", "1000");
			setProperty("Extensions.Classes", DistanceConflict.class.getName() + ";" + TimeOverlapsCounter.class.getName());
			setProperty("StudentWeights.Class", StudentSchedulingAssistantWeights.class.getName());
			setProperty("StudentWeights.PriorityWeighting", "true");
			setProperty("StudentWeights.LeftoverSpread", "true");
			setProperty("StudentWeights.BalancingFactor", "0.0");
			setProperty("StudentWeights.MultiCriteria", "true");
			setProperty("Reservation.CanAssignOverTheLimit", "true");
			setProperty("General.SaveDefaultProperties", "false");
			setProperty("General.StartUpDate", String.valueOf(new Date().getTime()));
			org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
			try {
				for (SolverParameterDef def: (List<SolverParameterDef>)hibSession.createQuery(
						"from SolverParameterDef x where x.group.type = :type and x.default is not null")
						.setInteger("type", SolverParameterGroup.sTypeStudent).list()) {
					setProperty(def.getName(), def.getDefault());
				}
				SolverPredefinedSetting settings = (SolverPredefinedSetting)hibSession.createQuery(
						"from SolverPredefinedSetting x where x.name = :reference")
						.setString("reference", "StudentSct.Online").setMaxResults(1).uniqueResult();
				if (settings != null) {
					for (SolverParameter param: settings.getParameters()) {
						if (!param.getDefinition().isVisible().booleanValue()) continue;
						if (param.getDefinition().getGroup().getType() != SolverParameterGroup.sTypeStudent) continue;
						setProperty(param.getDefinition().getName(), param.getValue());
					}
					setProperty("General.SettingsId", settings.getUniqueId().toString());
				}
				if (getProperty("Distances.Ellipsoid") == null || "DEFAULT".equals(getProperty("Distances.Ellipsoid")))
					setProperty("Distances.Ellipsoid", ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name()));
				if ("Priority".equals(getProperty("StudentWeights.Mode")))
					setProperty("StudentWeights.PriorityWeighting", "true");
				else if ("Equal".equals(getProperty("StudentWeights.Mode")))
					setProperty("StudentWeights.PriorityWeighting", "false");
			} finally {
				hibSession.close();
			}
		}
		
		@Override
		public String getProperty(String key) {
			String value = ApplicationProperties.getProperty("unitime.sectioning.config." + key);
			return value == null ? super.getProperty(key) : value;
		}
		
		@Override
		public String getProperty(String key, String defaultValue) {
			String value = ApplicationProperties.getProperty("unitime.sectioning.config." + key);
			return value == null ? super.getProperty(key, defaultValue) : value;
		}
	}

	@Override
	public String getHost() {
		return "local";
	}

	@Override
	public String getUser() {
		return getAcademicSession().getUniqueId().toString();
	}

	@Override
	public XEnrollments getEnrollments(Long offeringId) {
		return new XEnrollments(offeringId, getRequests(offeringId));
	}
	
	private class MasterAcquiringThread extends Thread {
		private java.util.concurrent.locks.Lock iLock;
		private AtomicBoolean iMaster = new AtomicBoolean(false);
		private boolean iStop = false;
		private OnlineSectioningServerContext iContext;
		
		private MasterAcquiringThread(OnlineSectioningServerContext context) {
			iContext = context;
			setName("AcquiringMasterLock[" + getAcademicSession() + "]");
			setDaemon(true);
			iLock = context.getLockService().getLock(getAcademicSession().toCompactString() + "[master]");
		}
		
		public boolean isMaster() {
			return iMaster.get();
		}
		
		@Override
		public void run() {
			if (iLock.tryLock()) {
				iMaster.set(true);
				loadOnMaster(iContext);
			}
			while (!iStop) {
				try {
					if (!iMaster.get()) {
						iLog.info("Waiting for a master lock...");
						iLock.lockInterruptibly();
					}
					iLog.info("I am the master.");
					synchronized (iMaster) {
						iMaster.set(true);
						iMaster.wait();
					}
					if (!iMaster.get()) {
						iLock.unlock();
						iLog.info("I am no longer the master.");
					}
				} catch (InterruptedException e) {
					
				}
			}
			iLog.info("No longer looking for a master.");
		}
		
		public boolean release() {
			if (iMaster.compareAndSet(true, false)) {
				synchronized (iMaster) {
					iMaster.notify();
				}
				return true;
			}
			return false;
		}
		
		public void dispose() {
			iStop = true;
			if (!release())
				interrupt();
		}
	}
}
