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
package org.unitime.timetable.onlinesectioning.server;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.TravelTime;
import org.unitime.timetable.model.StudentSchedulingRule.Mode;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentSchedulingRuleDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.CacheElement;
import org.unitime.timetable.onlinesectioning.HasCacheMode;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningActionFactory;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLogger;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServerContext;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XCredit;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XEnrollments;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRule;
import org.unitime.timetable.onlinesectioning.model.XSchedulingRules;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.onlinesectioning.status.FindStudentInfoAction.MinMaxCredit;
import org.unitime.timetable.onlinesectioning.model.XClassEnrollment;
import org.unitime.timetable.onlinesectioning.updates.CheckAllOfferingsAction;
import org.unitime.timetable.onlinesectioning.updates.PersistExpectedSpacesAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.MemoryCounter;

/**
 * @author Tomas Muller
 */
public abstract class AbstractServer implements OnlineSectioningServer {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	protected Log iLog = LogFactory.getLog(AbstractServer.class);
	private DistanceMetric iDistanceMetric = null;
	private DistanceMetric iUnavailabilityDistanceMetric = null;
	private DataProperties iConfig = null;
	protected XSchedulingRules iRules = null;
	private OnlineSectioningActionFactory iActionFactory = null;
	
	protected List<AsyncExecutor> iExecutors = new ArrayList<AsyncExecutor>();
	private Queue<Runnable> iExecutorQueue = new LinkedList<Runnable>();
	private HashSet<CacheElement<Long>> iOfferingsToPersistExpectedSpaces = new HashSet<CacheElement<Long>>();
	private static ThreadLocal<LinkedList<OnlineSectioningHelper>> sHelper = new ThreadLocal<LinkedList<OnlineSectioningHelper>>();
	protected Map<String, Object> iProperties = new HashMap<String, Object>();
	
	public AbstractServer(OnlineSectioningServerContext context) throws SectioningException {
		iConfig = new ServerConfig();
		iDistanceMetric = new DistanceMetric(iConfig);
		TravelTime.populateTravelTimes(iDistanceMetric, context.getAcademicSessionId());
		int unavailabilityMaxTravelTime = iConfig.getPropertyInteger("Distances.UnavailabilityMaxTravelTimeInMinutes", iDistanceMetric.getMaxTravelDistanceInMinutes());
        if (unavailabilityMaxTravelTime != iDistanceMetric.getMaxTravelDistanceInMinutes()) {
        	iUnavailabilityDistanceMetric = new DistanceMetric(iDistanceMetric);
        	iUnavailabilityDistanceMetric.setMaxTravelDistanceInMinutes(unavailabilityMaxTravelTime);
        	iUnavailabilityDistanceMetric.setComputeDistanceConflictsBetweenNonBTBClasses(true);
        }
		try {
			iActionFactory = ((OnlineSectioningActionFactory)Class.forName(ApplicationProperty.CustomizationOnlineSectioningActionFactory.value()).getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			LogFactory.getLog(OnlineSectioningServer.class).warn("Failed to initialize online sectioning action factory, using the default one.", e);
			iActionFactory = new SimpleActionFactory();
		}
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			Session session = SessionDAO.getInstance().get(context.getAcademicSessionId(), hibSession);
			if (session == null)
				throw new SectioningException(MSG.exceptionSessionDoesNotExist(context.getAcademicSessionId() == null ? "null" : context.getAcademicSessionId().toString()));
			
    		Date firstDay = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear());
    		iConfig.setProperty("DatePattern.DayOfWeekOffset", Integer.toString(Constants.getDayOfWeek(firstDay)));
    		
			AcademicSessionInfo academicSession = new AcademicSessionInfo(session);
			iLog = LogFactory.getLog(OnlineSectioningServer.class.getName() + ".server[" + academicSession.toCompactString() + "]");
			iProperties.put("AcademicSession", academicSession);
			int asncPoolSize = ApplicationProperty.OnlineSchedulingServerAsyncPoolSize.intValue();
			for (int i = 0; i < asncPoolSize; i++)
				new AsyncExecutor(academicSession, 1 + i).start();
		} finally {
			hibSession.close();
		}
		iLog.info("Config: " + ToolBox.dict2string(iConfig, 2));
		
		load(context);
	}
	
	protected AbstractServer(AcademicSessionInfo session, boolean allowAsyncCalls) {
		iConfig = new ServerConfig();
		iDistanceMetric = new DistanceMetric(iConfig);
		TravelTime.populateTravelTimes(iDistanceMetric, session.getUniqueId());
		int unavailabilityMaxTravelTime = iConfig.getPropertyInteger("Distances.UnavailabilityMaxTravelTimeInMinutes", iDistanceMetric.getMaxTravelDistanceInMinutes());
        if (unavailabilityMaxTravelTime != iDistanceMetric.getMaxTravelDistanceInMinutes()) {
        	iUnavailabilityDistanceMetric = new DistanceMetric(iDistanceMetric);
        	iUnavailabilityDistanceMetric.setMaxTravelDistanceInMinutes(unavailabilityMaxTravelTime);
        	iUnavailabilityDistanceMetric.setComputeDistanceConflictsBetweenNonBTBClasses(true);
        }
		try {
			iActionFactory = ((OnlineSectioningActionFactory)Class.forName(ApplicationProperty.CustomizationOnlineSectioningActionFactory.value()).getDeclaredConstructor().newInstance());
		} catch (Exception e) {
			LogFactory.getLog(OnlineSectioningServer.class).warn("Failed to initialize online sectioning action factory, using the default one.", e);
			iActionFactory = new SimpleActionFactory();
		}
		iLog = LogFactory.getLog(OnlineSectioningServer.class.getName() + ".server[" + session.toCompactString() + "]");
		iProperties.put("AcademicSession", session);
		if (allowAsyncCalls) {
			int asncPoolSize = ApplicationProperty.OnlineSchedulingServerAsyncPoolSize.intValue();
			for (int i = 0; i < asncPoolSize; i++)
				new AsyncExecutor(session, 1 + i).start();
		}
	}
	
	protected void load(OnlineSectioningServerContext context) throws SectioningException {
		loadOnMaster(context);
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
				if (Boolean.TRUE.equals(getProperty("ReloadingAllData", Boolean.FALSE))) {
					iLog.info("Already reloading all data.");
					return;
				}
				setProperty("ReloadingAllData", Boolean.TRUE);
				execute(createAction(ReloadAllData.class), user, new ServerCallback<Boolean>() {
					@Override
					public void onSuccess(Boolean result) {
						if (getAcademicSession().isSectioningEnabled())
							execute(createAction(CheckAllOfferingsAction.class), user, new ServerCallback<Boolean>() {
								@Override
								public void onSuccess(Boolean result) {
									setProperty("ReloadingAllData", Boolean.FALSE);
									setReady(true);
									getMemUsage();
								}
								@Override
								public void onFailure(Throwable exception) {
									setProperty("ReloadingAllData", Boolean.FALSE);
									iLog.error("Failed to check all offerings: " + exception.getMessage(), exception);
								}
							});
						else {
							setProperty("ReloadingAllData", Boolean.FALSE);
							setReady(true);
							getMemUsage();
						}
					}
					@Override
					public void onFailure(Throwable exception) {
						setProperty("ReloadingAllData", Boolean.FALSE);
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
	
	protected void setReady(boolean ready) {
		setProperty("ReadyToServe", Boolean.TRUE);
	}
	
	@Override
	public boolean isReady() {
		return Boolean.TRUE.equals(getProperty("ReadyToServe", Boolean.FALSE));
	}
	
	@Override
	public void reload() {
		setProperty("ReadyToServe", Boolean.FALSE);
		setProperty("ReloadIsNeeded", Boolean.TRUE);
		iLog.info("Reloading server...");
		List<Long> offeringIds = getOfferingsToPersistExpectedSpaces(0);
		if (!offeringIds.isEmpty()) {
			iLog.info("There are " + offeringIds.size() + " offerings that need expected spaces persisted.");
			execute(createAction(PersistExpectedSpacesAction.class).forOfferings(offeringIds), getSystemUser());
		}
		final Long sessionId = getAcademicSession().getUniqueId();
		loadOnMaster(new OnlineSectioningServerContext() {
			@Override
			public Long getAcademicSessionId() { return sessionId; }
			@Override
			public boolean isWaitTillStarted() { return false; }
		});
	}
	
	@Override
	public DistanceMetric getDistanceMetric() { return iDistanceMetric; }
	
	@Override
	public DistanceMetric getUnavailabilityDistanceMetric() { return iUnavailabilityDistanceMetric == null ? iDistanceMetric : iUnavailabilityDistanceMetric; }
	
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
			return new OnlineSectioningHelper();
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
		return iActionFactory.createAction(clazz);
	}

	@Override
	public <E> E execute(OnlineSectioningAction<E> action, OnlineSectioningLog.Entity user) throws SectioningException {
		Long oldSessionId = ApplicationProperties.getSessionId();
		ApplicationProperties.setSessionId(getAcademicSession().getUniqueId());
		
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
			if (e instanceof SectioningException || e instanceof PageAccessException) {
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
			if (e instanceof PageAccessException)
				throw (PageAccessException)e;
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
			ApplicationProperties.setSessionId(oldSessionId);
		}
	}
	
	@Override
	public <E> void execute(final OnlineSectioningAction<E> action, final OnlineSectioningLog.Entity user, final ServerCallback<E> callback) throws SectioningException {
		if (iExecutors == null || iExecutors.isEmpty()) {
			try {
				callback.onSuccess(execute(action, user));
			} catch (Throwable t) {
				callback.onFailure(t);
			}
			return;
		}
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
		private int iId;
		
		public AsyncExecutor(AcademicSessionInfo session, int id) {
			iId = id;
			setName("AsyncExecutor[" + session + "-" + id + "]");
			setDaemon(true);
			iExecutors.add(this);
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
								iLog.debug("Executor " + iId + " is waiting for a new job...");
								iExecutorQueue.wait();
							} catch (InterruptedException e) {}
							continue;
						}		
					}
					job.run();
					if (HibernateUtil.closeCurrentThreadSessions())
						iLog.debug("Job " + job + " did not close current-thread hibernate session.");
				}
				iLog.info("Executor " + iId + " stopped.");
			} finally {
				ApplicationProperties.setSessionId(null);
				Localization.removeLocale();
				Formats.removeFormats();
				iExecutors.remove(this);
			}
		}
		
	}
	
	@Override
	public void unload() {
		List<Long> offeringIds = getOfferingsToPersistExpectedSpaces(0);
		if (!offeringIds.isEmpty()) {
			iLog.info("There are " + offeringIds.size() + " offerings that need expected spaces persisted.");
			execute(createAction(PersistExpectedSpacesAction.class).forOfferings(offeringIds), getSystemUser());
		}
		if (iExecutors != null) {
			for (AsyncExecutor ex: iExecutors)
				ex.iStop = true;
			synchronized (iExecutorQueue) {
				iExecutorQueue.notifyAll();
			}
		}
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
			return iOfferingsToPersistExpectedSpaces.remove(new CacheElement<Long>(offeringId));
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
		long time = getAcademicSession().getDatePatternFirstDate().getTime() + (long) sectionTime.getWeeks().nextSetBit(0) * (1000l * 60l * 60l * 24l) + 43200000l;
		if (time >= start) {
			offset = (int)((time - start) / (1000 * 60 * 60 * 24 * 7));
		} else {
			offset = -(int)((start - time) / (1000 * 60 * 60 * 24 * 7)) - 1;
		}
		
		return week <= deadline + offset;
	}
	
	@Override
	public CourseDeadlines getCourseDeadlines(Long courseId) {
		boolean enabled = ApplicationProperty.OnlineSchedulingCheckDeadlines.isTrue();
		XCourse info = getCourse(courseId);
		int newDeadline = 0, changeDeadline = 0, dropDeadline = 0;
		if (info != null && info.getLastWeekToEnroll() != null)
			newDeadline = info.getLastWeekToEnroll();
		else
			newDeadline = getAcademicSession().getLastWeekToEnroll();
		if (info != null && info.getLastWeekToChange() != null)
			changeDeadline = info.getLastWeekToChange();
		else
			changeDeadline = getAcademicSession().getLastWeekToChange();
		if (info != null && info.getLastWeekToDrop() != null)
			dropDeadline = info.getLastWeekToDrop();
		else
			dropDeadline = getAcademicSession().getLastWeekToDrop();

		long start = getAcademicSession().getSessionBeginDate().getTime();
		long now = new Date().getTime();
		int week = 0;
		if (now >= start) {
			week = (int)((now - start) / (1000 * 60 * 60 * 24 * 7)) + 1;
		} else {
			week = -(int)((start - now) / (1000 * 60 * 60 * 24 * 7));
		}
		return new CourseDeadlinesImpl(enabled, newDeadline, changeDeadline, dropDeadline, week, start, getAcademicSession().getDatePatternFirstDate().getTime());
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
				for (SolverParameterDef def: hibSession.createQuery(
						"from SolverParameterDef x where x.group.type = :type and x.default is not null", SolverParameterDef.class)
						.setParameter("type", SolverParameterGroup.SolverType.STUDENT.ordinal()).list()) {
					setProperty(def.getName(), def.getDefault());
				}
				SolverPredefinedSetting settings = hibSession.createQuery(
						"from SolverPredefinedSetting x where x.name = :reference", SolverPredefinedSetting.class)
						.setParameter("reference", "StudentSct.Online").setMaxResults(1).uniqueResult();
				if (settings != null) {
					for (SolverParameter param: settings.getParameters()) {
						if (!param.getDefinition().isVisible().booleanValue()) continue;
						if (param.getDefinition().getGroup().getSolverType() != SolverParameterGroup.SolverType.STUDENT) continue;
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
	
	@Override
	public XCourseId getCourse(Long courseId, String courseName) {
		if (courseId != null) return getCourse(courseId);
		else if (courseName != null) return getCourse(courseName);
		else return null;
	}
	
	public static class CourseDeadlinesImpl implements CourseDeadlines {
		private static final long serialVersionUID = 1L;
		private boolean iEnabled;
		private int iNewDeadline = 0, iChangeDeadline = 0, iDropDeadline = 0;
		private int iWeek = 0;
		private long iStart, iFirstDate;
		
		public CourseDeadlinesImpl(boolean enabled, int newDeadline, int changeDeadline, int dropDeadline, int week, long start, long firstDate) {
			iEnabled = enabled;
			iNewDeadline = newDeadline; iChangeDeadline = changeDeadline; iDropDeadline = dropDeadline;
			iWeek = week;
			iStart = start; iFirstDate = firstDate;
		}
		
		public boolean isEnabled() { return iEnabled; }
		
		protected int getDeadline(Deadline type) {
			switch (type) {
			case NEW: return iNewDeadline;
			case CHANGE: return iChangeDeadline;
			case DROP: return iDropDeadline;
			default: return iNewDeadline;
			}
		}
		
		public boolean checkDeadline(XTime sectionTime, Deadline type) {
			int deadline = getDeadline(type);
			if (sectionTime == null)
				return iWeek <= deadline; // no time, just compare week and the deadline
			
			int offset = 0;
			long time = iFirstDate + (long) sectionTime.getWeeks().nextSetBit(0) * (1000l * 60l * 60l * 24l) + 43200000l;
			if (time >= iStart) {
				offset = (int)((time - iStart) / (1000 * 60 * 60 * 24 * 7));
			} else {
				offset = -(int)((iStart - time) / (1000 * 60 * 60 * 24 * 7)) - 1;
			}
			
			return iWeek <= deadline + offset;
		}
	}
	
	@Override
	public void setSchedulingRules(XSchedulingRules rules) {
		iRules = rules;	
	}

	
	@Override
	public XSchedulingRule getSchedulingRule(XStudent student, Mode mode, boolean isAdvisor, boolean isAdmin) {
		if (iRules != null)
			return iRules.getRule(student, mode, this, isAdvisor, isAdmin);
		StudentSchedulingRule rule = StudentSchedulingRule.getRule(
				new org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher(student, getAcademicSession().getDefaultSectioningStatus(), this, false),
				getAcademicSession(),
				isAdvisor,
				isAdmin,
				mode,
				StudentSchedulingRuleDAO.getInstance().getSession());
		return (rule == null ? null : new XSchedulingRule(rule));
	}
	
	@Override
	public Collection<XClassEnrollment> getStudentSchedule(final String studentExternalId) {
		XStudent student = getStudentForExternalId(studentExternalId);
		if (student == null) return null;
		List<XClassEnrollment> ret = new ArrayList<>();
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment e = cr.getEnrollment();
				if (e != null) {
					XOffering offering = getOffering(e.getOfferingId());
					XEnrollments enrl = getEnrollments(e.getOfferingId());
					for (XSection section: offering.getSections(e)) {
						XClassEnrollment ce = new XClassEnrollment(e, section);
						if (section.getParentId() != null)
							ce.setParentSectionName(offering.getSection(section.getParentId()).getName(e.getCourseId()));
						if (enrl != null) ce.setEnrollment(enrl.countEnrollmentsForSection(section.getSectionId()));
						XSubpart subpart = offering.getSubpart(section.getSubpartId());
						ce.setCredit(subpart.getCredit(e.getCourseId()));
						Float creditOverride = section.getCreditOverride(e.getCourseId());
						if (creditOverride != null) ce.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
						ret.add(ce);
					}
				}
			}
		}
		return ret;
	}
	
	@Override
	public XSchedulingRule getSchedulingRule(Long studentId, StudentSchedulingRule.Mode mode, boolean isAdvisor, boolean isAdmin) {
		XStudent student = getStudent(studentId);
		if (student == null) return null;
		return getSchedulingRule(student, mode, isAdvisor, isAdmin);
	}
	
	@Override
	public float[] getCredits(String studentExternalId) {
		return getCredits(getStudentForExternalId(studentExternalId));
	}
	
	public static XCourse getParentRequest(CourseCache server, XStudent student, XCourse childCourse) {
		if (childCourse == null || childCourse.getParentCourseId() == null) return null;
		for (XRequest cr: student.getRequests())
			if (cr instanceof XCourseRequest)
				for (XCourseId parent: ((XCourseRequest)cr).getCourseIds())
					if (parent.getCourseId().equals(childCourse.getParentCourseId()))
						return server.getCourse(parent.getCourseId());
		return null;
	}
	
	public MinMaxCredit getMinMaxCredit(CourseCache server, XStudent student, XCourse course, Float ec) {
		if (course == null) return null;
		XCredit c = course.getCreditInfo();
		// no course or credit -> no values
		if (c == null && ec == null) return null;
		// has parent course --> count this credit with the parent
		XCourse pc = getParentRequest(server, student, course);
		if (pc != null && pc.getCreditInfo() != null) return new MinMaxCredit(0f, 0f);
		
		float tMin = 0f, tMax = 0f;
		for (XRequest request: student.getRequests())
			if (request instanceof XCourseRequest) {
				Float min = null, max = null;
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment e = cr.getEnrollment();
				if (e != null) {
					XCourse child = server.getCourse(e.getCourseId());
					if (child != null && course.getCourseId().equals(child.getParentCourseId())) {
						float cred = e.getCredit(this);
						if (min == null || min > cred) min = cred;
						if (max == null || max < cred) max = cred;
					}
				} else {
					for (XCourseId cid: cr.getCourseIds()) {
						XCourse child = server.getCourse(cid.getCourseId());
						if (child != null && course.getCourseId().equals(child.getParentCourseId())) {
							XCredit cc = child.getCreditInfo();
							if (cc != null) {
								if (min == null || min > cc.getMinCredit()) min = cc.getMinCredit();
								if (max == null || max < cc.getMaxCredit()) max = cc.getMinCredit();
							}
						}
					}
				}
				if (min != null) {
					tMin += min; tMax += max;
				}
			}
		if (ec != null)
			return new MinMaxCredit(ec + tMin, ec + tMax);
		else
			return new MinMaxCredit(c.getMinCredit() + tMin, c.getMinCredit() + tMax);
	}
	
	@Override
	public float[] getCredits(XStudent student) {
		if (student == null) return null;
		Set<Long> advisorWaitListedCourseIds = student.getAdvisorWaitListedCourseIds(this);
		CourseCache cache = new CourseCache(this);
		List<Float> mins = new ArrayList<Float>();
		List<Float> maxs = new ArrayList<Float>();
		int nrCourses = 0;
		float tMin = 0f, tMax = 0f, tEnrl = 0f;
		for (XRequest request: student.getRequests()) {
			if (request instanceof XCourseRequest) {
				XCourseRequest cr = (XCourseRequest)request;
				XEnrollment e = cr.getEnrollment();
				if (e != null) {
					float cred = e.getCredit(this);
					MinMaxCredit rc = getMinMaxCredit(cache, student, cache.getCourse(e.getCourseId()), cred);
					if (rc != null) {
						// child course added
						tMin += rc.getMinCredit(); tMax += rc.getMaxCredit();
					} else {
						tMin += cred; tMax += cred;
					}
					tEnrl += cred;
					if (cr.isAlternative())
						nrCourses --;
				} else {
					Float min = null, max = null;
					for (XCourseId courseId: cr.getCourseIds()) {
						XCourse course = getCourse(courseId.getCourseId());
						MinMaxCredit rc = getMinMaxCredit(cache, student, course, null);
						if (rc != null) {
							if (min == null || min > rc.getMinCredit()) min = rc.getMinCredit();
							if (max == null || max < rc.getMaxCredit()) max = rc.getMaxCredit();
						}
					}
					if (cr.isAlternative()) {
						if (min != null) {
							mins.add(min); maxs.add(max);
						}
					} else {
						if (min != null) {
							if (cr.isWaitListOrNoSub(WaitListMode.NoSubs, advisorWaitListedCourseIds)) {
								tMin += min; tMax += max;
							} else {
								mins.add(min); maxs.add(max); nrCourses ++;
							}
						}
					}
				}
			}
		}
		Collections.sort(mins);
		Collections.sort(maxs);
		for (int i = 0; i < nrCourses; i++) {
			tMin += mins.get(i);
			tMax += maxs.get(maxs.size() - i - 1);
		}
		return new float[] {tMin, tMax, tEnrl};
	}
}
