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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.ifs.util.JProf;
import org.cpsolver.ifs.util.ToolBox;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.online.expectations.AvoidUnbalancedWhenNoExpectations;
import org.cpsolver.studentsct.online.expectations.OverExpectedCriterion;
import org.cpsolver.studentsct.online.selection.StudentSchedulingAssistantWeights;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
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
import org.unitime.timetable.onlinesectioning.updates.CheckAllOfferingsAction;
import org.unitime.timetable.onlinesectioning.updates.PersistExpectedSpacesAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.MemoryCounter;

/**
 * @author Tomas Muller
 */
public abstract class AbstractServer implements OnlineSectioningServer {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected Log iLog = LogFactory.getLog(AbstractServer.class);
	private DistanceMetric iDistanceMetric = null;
	private DataProperties iConfig = null;
	
	protected AsyncExecutor iExecutor;
	private Queue<Runnable> iExecutorQueue = new LinkedList<Runnable>();
	private HashSet<CacheElement<Long>> iOfferingsToPersistExpectedSpaces = new HashSet<CacheElement<Long>>();
	private static ThreadLocal<LinkedList<OnlineSectioningHelper>> sHelper = new ThreadLocal<LinkedList<OnlineSectioningHelper>>();
	protected Map<String, Object> iProperties = new HashMap<String, Object>();
	
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
			AcademicSessionInfo academicSession = new AcademicSessionInfo(session);
			iLog = LogFactory.getLog(OnlineSectioningServer.class.getName() + ".server[" + academicSession.toCompactString() + "]");
			iProperties.put("AcademicSession", academicSession);
			iExecutor = new AsyncExecutor(academicSession);
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
		} else {
			loadOnMaster(context);
		}
	}
		
	protected void loadOnMaster(OnlineSectioningServerContext context) throws SectioningException {
		try {
			setProperty("ReloadIsNeeded", Boolean.FALSE);
			final OnlineSectioningLog.Entity user = OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
					.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
					.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
			if (context.isWaitTillStarted()) {
				try {
					execute(createAction(ReloadAllData.class), user);
				} catch (Throwable exception) {
					iLog.error("Failed to load server: " + exception.getMessage(), exception);
					throw exception;
				}
				if (getAcademicSession().isSectioningEnabled()) {
					try {
						execute(createAction(CheckAllOfferingsAction.class), user);
					} catch (Throwable exception) {
						iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
						throw exception;
					}
				}
				setReady(true);
				getMemUsage();
			} else {
				execute(createAction(ReloadAllData.class), user, new ServerCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (getAcademicSession().isSectioningEnabled())
							execute(createAction(CheckAllOfferingsAction.class), user, new ServerCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {
									setReady(true);
									getMemUsage();
								}
								@Override
								public void onFailure(Throwable exception) {
									iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
								}
							});
						else {
							setReady(true);
							getMemUsage();
						}
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
	public long getMemUsage() {
		Runtime rt = Runtime.getRuntime();
		MemoryCounter mc = new MemoryCounter();
		DecimalFormat df = new DecimalFormat("#,##0.00");
		long total = 0; // mc.estimate(this);
		Map<String, String> info = new HashMap<String, String>();
		Class clazz = getClass();
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				if (!Modifier.isStatic(fields[i].getModifiers())) {
					if (!fields[i].getType().isPrimitive()) {
						fields[i].setAccessible(true);
						try {
							Object obj = fields[i].get(this);
							if (obj != null) {
								long est = estimate(mc, obj);
								if (est > 1024)
									info.put(clazz.getSimpleName() + "." + fields[i].getName(), df.format(est / 1024.0) + " kB" + (obj instanceof Map ? " (" + ((Map)obj).size() + " records)" : obj instanceof Collection ? "(" + ((Collection)obj).size() + " records)" : ""));
								total += est;
							}
						} catch (IllegalAccessException ex) {
						} catch (ConcurrentModificationException ex) {
						}
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		iLog.info("Total Allocated " + df.format(total / 1024.0) + " kB (of " + df.format((rt.totalMemory() - rt.freeMemory()) / 1048576.0) + " MB), details: " + ToolBox.dict2string(info, 2));
		return total;
	}
	
	private long estimate(MemoryCounter mc, Object obj) {
		if (obj instanceof Map) {
			Map map = (Map)obj;
			if (map.size() <= 1000) return mc.estimate(obj);
			long total = 0;
			int limit = map.size() / 5; Iterator it = map.entrySet().iterator();
			for (int i = 0; i < limit; i++) {
				Map.Entry e = (Map.Entry)it.next();
				total += mc.estimate(e.getKey()) + mc.estimate(e.getValue());
			}
			return map.size() * total / limit;
		} else if (obj instanceof Collection) {
			Collection col = (Collection)obj;
			if (col.size() <= 1000) return mc.estimate(obj);
			long total = 0;
			int limit = col.size() / 5; Iterator it = col.iterator();
			for (int i = 0; i < limit; i++) {
				Object val = it.next();
				total += mc.estimate(val);
			}
			return col.size() * total / limit;
		} else {
			return mc.estimate(obj);
		}
	}
	
	@Override
	public boolean isMaster() {
		return (iMasterThread != null ? iMasterThread.isMaster() : true);
	}
	
	protected void setReady(boolean ready) {
		setProperty("ReadyToServe", Boolean.TRUE);
	}
	
	@Override
	public boolean isReady() {
		return Boolean.TRUE.equals(getProperty("ReadyToServe", Boolean.FALSE));
	}
	
	@Override
	public void releaseMasterLockIfHeld() {
		if (iMasterThread != null)
			iMasterThread.release();
	}
	
	@Override
	public DistanceMetric getDistanceMetric() { return iDistanceMetric; }
	
	@Override
	public OverExpectedCriterion getOverExpectedCriterion() {
		try {
            Class<OverExpectedCriterion> overExpectedCriterionClass = (Class<OverExpectedCriterion>)Class.forName(getConfig().getProperty("OverExpectedCriterion.Class", AvoidUnbalancedWhenNoExpectations.class.getName()));
            return overExpectedCriterionClass.getConstructor(DataProperties.class).newInstance(getConfig());
        } catch (Exception e) {
        	iLog.error("Unable to create custom over-expected criterion (" + e.getMessage() + "), using default.", e);
        	return new AvoidUnbalancedWhenNoExpectations(getConfig());
        }
	}
	
	@Override
	public AcademicSessionInfo getAcademicSession() { return getProperty("AcademicSession", null); }
	
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
	
	protected OnlineSectioningLog.Entity getSystemUser() {
		return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
				.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
				.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
	}
	
	@Override
	public <X extends OnlineSectioningAction> X createAction(Class<X> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new SectioningException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new SectioningException(e.getMessage(), e);
		}
	}

	@Override
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException {
		long c0 = OnlineSectioningHelper.getCpuTime();
		String cacheMode = getConfig().getProperty(action.name() + ".CacheMode", getConfig().getProperty("CacheMode"));
		OnlineSectioningHelper h = new OnlineSectioningHelper(user, cacheMode != null ? CacheMode.valueOf(cacheMode) : action instanceof HasCacheMode ? ((HasCacheMode)action).getCacheMode() : CacheMode.IGNORE);
		
		try {
			setCurrentHelper(h);
			h.addMessageHandler(new OnlineSectioningHelper.DefaultMessageLogger(LogFactory.getLog(action.getClass().getName() + "." + action.name() + "[" + getAcademicSession().toCompactString() + "]")));
			h.addAction(action, getAcademicSession());
			E ret = action.execute(this, h);
			if (h.getAction() != null && !h.getAction().hasResult()) {
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
			if (iLog.isDebugEnabled())
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
		
		public AsyncExecutor(AcademicSessionInfo session) {
			setName("AsyncExecutor[" + session + "]");
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
	public void unload() {
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
		if (!ApplicationProperty.OnlineSchedulingCheckDeadlines.isTrue()) return true;
		
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
			setProperty("check-assignment.ExcludeLockedOfferings", "false");
			setProperty("check-offering.ExcludeLockedOfferings", "false");
			setProperty("approve-enrollments.ExcludeLockedOfferings", "false");
			setProperty("reject-enrollments.ExcludeLockedOfferings", "false");
			setProperty("status-change.LockOfferings", "false");
			setProperty("student-email.LockOfferings", "false");
			setProperty("eligibility.LockOfferings", "false");
			
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
					setProperty("Distances.Ellipsoid", ApplicationProperty.DistanceEllipsoid.value());
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
			String value = ApplicationProperty.OnlineSchedulingParameter.value(key);
			return value == null ? super.getProperty(key) : value;
		}
		
		@Override
		public String getProperty(String key, String defaultValue) {
			String value = ApplicationProperty.OnlineSchedulingParameter.value(key);
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
		
		private void executeLoadOnMaster() {
			synchronized (iExecutorQueue) {
				iExecutorQueue.offer(new Runnable() {
					@Override
					public void run() {
						loadOnMaster(iContext);
					}
					
					@Override
					public String toString() {
						return "load-on-master";
					}
				});
				iExecutorQueue.notify();
			};
		}
		
		@Override
		public void run() {
			if (iLock.tryLock()) {
				iMaster.set(true);
				iLog.info("Loading server...");
				executeLoadOnMaster();
			}
			while (!iStop) {
				try {
					if (!iMaster.get()) {
						iLog.info("Waiting for a master lock...");
						iLock.lockInterruptibly();
					}
					synchronized (iMaster) {
						iLog.info("I am the master.");
						iMaster.set(true);
						if (Boolean.TRUE.equals(getProperty("ReloadIsNeeded", Boolean.FALSE))) {
							iLog.info("Reloading server...");
							executeLoadOnMaster();
						}
						iMaster.wait();
						iMaster.set(false);
						iLock.unlock();
						iLog.info("I am no longer the master.");
					}
				} catch (InterruptedException e) {
				}
			}
			iLog.info("No longer looking for a master.");
		}
		
		public boolean release() {
			synchronized (iMaster) {
				if (iMaster.get()) {
					iLog.info("Releasing master lock...");
					List<Long> offeringIds = getOfferingsToPersistExpectedSpaces(0);
					if (!offeringIds.isEmpty()) {
						iLog.info("There are " + offeringIds.size() + " offerings that need expected spaces persisted.");
						execute(createAction(PersistExpectedSpacesAction.class).forOfferings(offeringIds), getSystemUser());
					}
					iMaster.notify();
					return true;
				}
				return false;
			}
		}
		
		public void dispose() {
			iStop = true;
			if (!release())
				interrupt();
			try {
				this.join();
			} catch (InterruptedException e) {}
		}
	}
	
	@Override
	public <E> E getProperty(String name, E defaultValue) {
		E ret = (E)iProperties.get(name);
		return (ret == null ? defaultValue : ret);
	}

	@Override
	public <E> void setProperty(String name, E value) {
		if (value == null)
			iProperties.remove(name);
		else
			iProperties.put(name,  value);
	}
}
