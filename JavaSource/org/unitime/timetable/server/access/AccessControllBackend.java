package org.unitime.timetable.server.access;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.filter.BusySessions.Tracker;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.Operation;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.PingRequest;
import org.unitime.timetable.gwt.client.access.AccessControlInterface.PingResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.model.AccessStatistics;
import org.unitime.timetable.model.dao.AccessStatisticsDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserQualifier;

@GwtRpcImplements(PingRequest.class)
@GwtRpcLogging(Level.DISABLED)
public class AccessControllBackend implements GwtRpcImplementation<PingRequest, PingResponse>, InitializingBean, DisposableBean{
	private static Log sLog = LogFactory.getLog(AccessControllBackend.class);
	private ConcurrentMap<String, PingData> iData = new ConcurrentHashMap<String, PingData>();
	private Updater iUpdater;
	
	private @Autowired Tracker unitimeBusySessions;

	@Override
	public PingResponse execute(final PingRequest request, final  SessionContext context) {
		final long t0 = System.currentTimeMillis();
		PingData pd = iData.compute(context.getHttpSessionId(), (key, current) -> {
			if (current == null) current = new PingData(t0);
			current.update(request, context, t0);
			return current;
		});
		PingResponse ret = new PingResponse();
		if (request.getOperation() == Operation.LOGOUT) {
			// do nothing
		} else if (!pd.isAccess()) {
			// check queue
			Integer maxActiveUsers = getMaxActiveUsers(request.getPage());
			if (maxActiveUsers == null) {
				pd.setAccess(true);
			} else {
				CheckQueue cq = new CheckQueue(request.getPage(), context.getHttpSessionId(), pd, t0);
				iData.forEach(cq);
				if (cq.getUsersWithAccess() + cq.getUsersInQueueBeforeMe() + 1 <= maxActiveUsers) {
					pd.setAccess(true);
				} else {
					ret.setQueue(1 + cq.getUsersInQueueBeforeMe());
				}
			}
		} else {
			Integer activeLimitInSeconds = getActiveLimitInSeconds(request.getPage());
			if (activeLimitInSeconds != null && pd.getActiveAge(t0) > activeLimitInSeconds && !unitimeBusySessions.isWorking(context.getHttpSessionId()))
				ret.setInactive(activeLimitInSeconds / 60);
		}
		if (sLog.isTraceEnabled())
			sLog.trace(context.getHttpSessionId() + ": " + request.getOperation() + " " + pd.toString(t0));
		ret.setAccess(pd.isAccess());
		return ret;
	}
	
	public Integer getMaxActiveUsers(String page) {
		Integer maxActiveUsers = ApplicationProperty.AccessControlMaxActiveUsers.intValue(page);
		return (maxActiveUsers == null || maxActiveUsers <= 0 ? null : maxActiveUsers);
	}
	
	public Integer getActiveLimitInSeconds(String page) {
		Integer limitInMinutes = ApplicationProperty.AccessControlActiveLimitInMinutes.intValue(page);
		return (limitInMinutes == null || limitInMinutes <= 0 ? null : limitInMinutes * 60);
	}
	
	@Override
	public void destroy() throws Exception {
		if (iUpdater != null) {
			iUpdater.interrupt();
			iUpdater = null;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		iUpdater = new Updater();
		iUpdater.start();
	}
	
	private static class PingData implements Comparable<PingData> {
		private String iPage;
		private String iUser;
		private String iSession;
		private String iAutority;
		private long iFirstUse;
		private long iLastPing;
		private long iLastActive;
		private boolean iAccess = false;
		private boolean iLoggedOut = false;
		
		PingData(long t0) {
			iFirstUse =t0;
			iLastPing = iFirstUse;
			iLastActive = iLastPing;
		}
		
		void update(PingRequest request, SessionContext context, long t0) {
			if (request.getOperation() == Operation.PING)
				iAccess = true;
			else if (request.getOperation() == Operation.LOGOUT)
				iAccess = false;
			if (request.getOperation() == Operation.CHECK_ACCESS && !iAccess && !isOpened(t0))
				iFirstUse = t0;
			iLastPing = t0;
			iLoggedOut = (request.getOperation() == Operation.LOGOUT);
			if (request.isActive()) iLastActive = t0;
			iPage = request.getPage();
			if (context.isAuthenticated()) {
				iUser = context.getUser().getExternalUserId();
				UserAuthority auth = context.getUser().getCurrentAuthority(); 
				if (auth != null) {
					iAutority = auth.getRole();
					UserQualifier session = auth.getAcademicSession();
					iSession = (session == null ? null : session.getQualifierReference());
				} else {
					iAutority = null;
					iSession = null;
				}
			} else {
				iUser = null;
				iAutority = null;
				iSession = null;
			}
		}
		
		public long getActiveAge(long t0) {
			return (t0 - iLastActive) / 1000;
		}
		
		public long getPingAge(long t0) {
			return (t0 - iLastPing) / 1000;
		}
		
		public long getAge(long t0) {
			return (t0 - iFirstUse) / 1000;
		}
		
		public long getFirstUse() { return iFirstUse; }
		
		public String getPage() {
			return iPage;
		}
		
		public boolean isOpened(long t0) {
			return getPingAge(t0) <= 60 && !isLoggedOut();
		}
		
		public boolean isActive(long t0, Integer activeLimitInSeconds) {
			return isOpened(t0) && (activeLimitInSeconds == null || getActiveAge(t0) <= activeLimitInSeconds + 120);
		}
		
		public boolean isAccess() { return iAccess; }
		
		public void setAccess(boolean access) { iAccess = access; }
		
		public boolean isLoggedOut() { return iLoggedOut; }
		
		public String toString(long t0) {
			return "PingData{" + (isLoggedOut() || !isOpened(t0) ? "expired, " : isAccess() ? "access, " : "waiting, ") + (getAge(t0)/60) + "m old, " + (getActiveAge(t0)/60) + "m inactive, " + (getPingAge(t0)/60) + "m unchecked" +
					", page=" + iPage + ", user=" + iUser + ", session=" + iSession + ", role=" + iAutority + "}";
		}

		@Override
		public String toString() {
			return toString(System.currentTimeMillis());
		}

		@Override
		public int compareTo(PingData pd) {
			return Long.compare(pd.iLastActive, iLastActive);
		}
	}
	
	private class CheckQueue implements BiConsumer<String, PingData> {
		private String iPage;
		private String iSession;
		private PingData iPingData;
		private int iAccess;
		private int iQueue;
		private Integer iActiveLimitInSeconds;
		private long iT0;
		
		private CheckQueue(String myPage, String mySession, PingData myData, long t0) {
			iPage = myPage;
			iSession = mySession;
			iPingData = myData;
			iActiveLimitInSeconds = getActiveLimitInSeconds(myPage);
			iT0 = t0;
		}
		
		@Override
		public void accept(String t, PingData u) {
			if (!iSession.equals(t) && iPage.equals(u.getPage()) && u.isActive(iT0, iActiveLimitInSeconds)) {
				if (u.isAccess())
					iAccess ++;
				else if (u.getFirstUse() < iPingData.getFirstUse())
					iQueue ++;
			}
		}
		
		public int getUsersWithAccess() { return iAccess; }
		public int getUsersInQueueBeforeMe() { return iQueue; }
		
		@Override
		public String toString() {
			return "access: " + iAccess + ", queue: " + iQueue;
		}
	}
	
	private class Counter implements BiConsumer<String, PingData> {
		private Map<String, Integer> iWaiting = new HashedMap<>();
		private Map<String, Integer> iActive1 = new HashedMap<>();
		private Map<String, Integer> iActive2 = new HashedMap<>();
		private Map<String, Integer> iActive5 = new HashedMap<>();
		private Map<String, Integer> iActive10 = new HashedMap<>();
		private Map<String, Integer> iActive15 = new HashedMap<>();
		private Map<String, Integer> iActive = new HashedMap<>();
		private Map<String, Integer> iAccess = new HashedMap<>();
		private Map<String, Integer> iOpened = new HashedMap<>();
		private Map<String, Integer> iTotal = new HashedMap<>();
		private List<String> iInactive = new ArrayList<>();
		private long iT0 = System.currentTimeMillis();
		
		@Override
		public void accept(String t, PingData u) {
			if (sLog.isDebugEnabled())
				sLog.debug(t + ": " + u);
			if (!unitimeBusySessions.isActive(t)) {
				iInactive.add(t);
			} else {
				if (u.isAccess() && !u.isOpened(iT0)) u.setAccess(false);
				iTotal.merge(u.getPage(), 1, (a, b) -> a + b);
				if (u.isAccess()) {
					iAccess.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.getActiveAge(iT0) <=  1 * 60)  iActive1.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.getActiveAge(iT0) <=  2 * 60)  iActive2.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.getActiveAge(iT0) <=  5 * 60)  iActive5.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.getActiveAge(iT0) <= 10 * 60) iActive10.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.getActiveAge(iT0) <= 15 * 60) iActive15.merge(u.getPage(), 1, (a, b) -> a + b);
					if (u.isActive(iT0, getActiveLimitInSeconds(u.getPage()))) iActive.merge(u.getPage(), 1, (a, b) -> a + b); 
				} else if (u.isActive(iT0, getActiveLimitInSeconds(u.getPage()))) {
					iWaiting.merge(u.getPage(), 1, (a, b) -> a + b);
				}
				if (u.isOpened(iT0))
					iOpened.merge(u.getPage(), 1, (a, b) -> a + b);
			}
		}
		
		public List<String> getInactive() { return iInactive; }
		
		public boolean isEmpty() {
			return iTotal.isEmpty();
		}

		@Override
		public String toString() {
			String ret = "";
			for (String page: iTotal.keySet()) {
				Integer limit = getMaxActiveUsers(page);
				ret = (ret.isEmpty() ? "" : ret + "\n") + page + "{" +
					"access: " + iAccess.getOrDefault(page, 0) + (limit == null ? "" : " of " + limit) +
					", active: " + iActive.getOrDefault(page, 0) + 
					" (<1m: " + iActive1.getOrDefault(page, 0)
						+ ", <2m: " + iActive2.getOrDefault(page, 0)
						+ ", <5m: " + iActive5.getOrDefault(page, 0)
						+ ", <10m: " + iActive10.getOrDefault(page, 0)
						+ ", <15m: " + iActive15.getOrDefault(page, 0) + ")" +
					", waiting: " + iWaiting.getOrDefault(page, 0) +
					", opened: " + iOpened.getOrDefault(page, 0) +
					", tracking: " + iTotal.getOrDefault(page, 0) + "}";
			}
			return ret;
		}
		
		protected void record(String host) {
			org.hibernate.Session hibSession = AccessStatisticsDAO.getInstance().createNewSession();
			try {
				for (String page: iTotal.keySet()) {
					AccessStatistics stat = new AccessStatistics();
					stat.setTimeStamp(new Date(iT0));
					stat.setHost(host);
					stat.setPage(page);
					stat.setAccess(iAccess.getOrDefault(page, 0));
					stat.setActive(iActive.getOrDefault(page, 0));
					stat.setOpened(iOpened.getOrDefault(page, 0));
					stat.setWaiting(iWaiting.getOrDefault(page, 0));
					stat.setTracking(iTotal.getOrDefault(page, 0));
					stat.setActive1m(iActive1.getOrDefault(page, 0));
					stat.setActive2m(iActive2.getOrDefault(page, 0));
					stat.setActive5m(iActive5.getOrDefault(page, 0));
					stat.setActive10m(iActive10.getOrDefault(page, 0));
					stat.setActive15m(iActive15.getOrDefault(page, 0));
					hibSession.persist(stat);
				}
				hibSession.flush();
			} finally {
				hibSession.close();
			}
		}
	}
	
	private class Updater extends Thread {
		private boolean iActive = true;
		private String iHost = "localhost";

		public Updater() {
			super("PingBackend.Updater");
			setDaemon(true);
			try {
				iHost = InetAddress.getLocalHost().getHostName();
				if (iHost.indexOf('.') > 0)
					iHost = iHost.substring(0, iHost.indexOf('.'));
			} catch (UnknownHostException e) {}
		}
		
		@Override
		public void interrupt() {
			iActive = false;
			super.interrupt();
			try { join(); } catch (InterruptedException e) {}
		}
		
		@Override
		public void run() {
			sLog.debug("Access Controll Updater is up.");
			while (true) {
				try {
					try {
						sleep(60000);
					} catch (InterruptedException e) {}
					Counter c = new Counter();
					iData.forEach(c);
					for (String k: c.getInactive()) {
						if (sLog.isTraceEnabled())
							sLog.trace(k + ": removed");
						iData.remove(k);
					}
					if (!c.isEmpty()) {
						sLog.info(c);
						try {
							c.record(iHost);
						} catch (Exception e) {
							sLog.warn("Failed to record statistics: " + e.getMessage(), e);
						}
					}
					if (!iActive) break;
				} catch (Exception e) {
					sLog.error(e.getMessage(), e);
				}
			}
			sLog.debug("Access Controll Updater is down.");
		}
	}


}
