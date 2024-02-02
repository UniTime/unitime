package org.unitime.timetable.server.access;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
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
public class AccessControllBackend implements GwtRpcImplementation<PingRequest, PingResponse>, InitializingBean, DisposableBean {
	private static Log sLog = LogFactory.getLog(AccessControllBackend.class);
	private ConcurrentMap<String, ConcurrentMap<String, PingData>> iData = new ConcurrentHashMap<>();
	private Updater iUpdater;
	
	private @Autowired Tracker unitimeBusySessions;

	@Override
	public PingResponse execute(final PingRequest request, final  SessionContext context) {
		final long t0 = System.currentTimeMillis();
		ConcurrentMap<String, PingData> data = iData.computeIfAbsent(request.getPage(), (key) -> {
			return new ConcurrentHashMap<>();
		});
		PingData pd = data.compute(context.getHttpSessionId(), (key, current) -> {
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
				pd.setAccess(true, t0);
			} else {
				CheckQueue cq = new CheckQueue(request.getPage(), context.getHttpSessionId(), pd, t0);
				data.forEach(cq);
				if (cq.getUsersWithAccess() + cq.getUsersInQueueBeforeMe() + 1 <= maxActiveUsers) {
					pd.setAccess(true, t0);
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
	
	protected ConcurrentMap<String, ConcurrentMap<String, PingData>> getData() {
		return iData;
	}
	
	private static class PingData implements Comparable<PingData> {
		private String iPage;
		private String iUser;
		private String iSession;
		private String iAutority;
		private long iFirstUse;
		private long iLastPing;
		private long iLastActive;
		private long iGotAccess;
		private boolean iAccess = false;
		private boolean iLoggedOut = false;
		private boolean iCountedIn = false, iCountedOut = false;
		
		PingData(long t0) {
			iFirstUse = t0;
			iLastPing = iFirstUse;
			iLastActive = iLastPing;
		}
		
		void update(PingRequest request, SessionContext context, long t0) {
			if (request.getOperation() == Operation.PING) {
				if (!iAccess) { iGotAccess = t0; iCountedIn = false; }
				iAccess = true;
			} else if (request.getOperation() == Operation.LOGOUT) {
				if (iAccess) iCountedOut = false;
				iAccess = false;
			}
			if (request.getOperation() == Operation.CHECK_ACCESS && !iAccess && !isOpened(t0)) {
				iFirstUse = t0;
				iGotAccess = 0;
			}
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
		
		public void logout() {
			if (iAccess) iCountedOut = false;
			iAccess = false;
			iLoggedOut = true;
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
		
		public boolean hadAccess() {
			return iGotAccess > 0;
		}
		
		public long getAccessTime() {
			return (iLastPing - iGotAccess) / 1000;
		}
		
		public long getWaitingTime() {
			if (iGotAccess > 0)
				return (iGotAccess - iFirstUse) / 1000;
			else
				return (iLastPing - iFirstUse) / 1000;
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
		
		public void setAccess(boolean access, long t0) {
			if (!iAccess && access) { iGotAccess = t0; iCountedIn = false; }
			if (iAccess && !access) { iCountedOut = false; }
			iAccess = access;
		}
		
		public boolean isLoggedOut() { return iLoggedOut; }
		
		public boolean countIn() {
			if (!iCountedIn) {
				iCountedIn = true;
				return true;
			}
			return false;
		}
		
		public boolean countOut() {
			if (!iCountedOut) {
				iCountedOut = true;
				return true;
			}
			return false;
		}
		
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
	
	public static class Average {
		private long iTotal;
		private int iCount;
		
		Average() {
			iTotal = 0l;
			iCount = 0;
		}
		
		private void add(long value) {
			iTotal += value;
			iCount ++;
		}
		
		public int getCount() {
			return iCount;
		}
		
		public long getTotal() {
			return iTotal;
		}
		
		private long getAverage() {
			return (iCount == 0 ? 0 : iTotal / iCount);
		}
	}
	
	private class PingCounter {
		private String iPage;
		private Integer iActiveLimitInSeconds;
		private Integer iMaxActiveUsers;
		private long iT0;
		
		private int iWaiting = 0;
		private int iActive1 = 0;
		private int iActive2 = 0;
		private int iActive5 = 0;
		private int iActive10 = 0;
		private int iActive15 = 0;
		private int iActive = 0;
		private int iAccess = 0;
		private int iOpened = 0;
		private int iTotal = 0;
		private int iGaveUp = 0;
		private int iLeft = 0;
		private int iGotIn = 0;
		
		private Average iWaitTime = new Average();
		private Average iWaitTimeWhenGotIn = new Average();
		private Average iAccessTime = new Average();
		private Average iGaveUpTime = new Average();
		private Average iAccessTimeWhenLeft = new Average();
		
		PingCounter(String page, long t0) {
			iPage = page;
			iActiveLimitInSeconds = getActiveLimitInSeconds(page);
			iMaxActiveUsers = getMaxActiveUsers(page);
			iT0 = t0;
		}
		
		public void inc(PingData u) {
			iTotal ++;
			if (u.isAccess()) {
				iAccess ++;
				if (u.getActiveAge(iT0) <=  1 * 60)  iActive1 ++;
				if (u.getActiveAge(iT0) <=  2 * 60)  iActive2 ++;
				if (u.getActiveAge(iT0) <=  5 * 60)  iActive5 ++;
				if (u.getActiveAge(iT0) <= 10 * 60) iActive10 ++;
				if (u.getActiveAge(iT0) <= 15 * 60) iActive15 ++;
				if (u.isActive(iT0, iActiveLimitInSeconds)) iActive ++;
				iAccessTime.add(u.getAccessTime());
				if (u.countIn()) {
					// got access during the last minute
					iGotIn ++;
					iWaitTimeWhenGotIn.add(u.getWaitingTime());
				}
			} else if (u.isLoggedOut()) {
				if (u.hadAccess()) {
					iLeft ++;
					iAccessTimeWhenLeft.add(u.getAccessTime());
				} else {
					iGaveUp ++;
					iGaveUpTime.add(u.getWaitingTime());
				}
			} else if (u.isActive(iT0, iActiveLimitInSeconds)) {
				iWaiting ++;
				iWaitTime.add(u.getWaitingTime());
			} else if (u.countOut()) {
				if (u.hadAccess()) {
					iLeft ++;
					iAccessTimeWhenLeft.add(u.getAccessTime());
				} else {
					iGaveUp ++;
					iGaveUpTime.add(u.getWaitingTime());
				}
			}
			if (u.isOpened(iT0))
				iOpened ++;
		}
		
		@Override
		public String toString() {
			DecimalFormat df = new DecimalFormat("0.0");
			return iPage + "{" +
					"access: " + iAccess + (iMaxActiveUsers == null ? "" : " of " + iMaxActiveUsers) +
					", active: " + iActive + 
					" (avg: " + df.format(iAccessTime.getAverage() / 60.0) + "m"
						+ ", <1m: " + iActive1
						+ ", <2m: " + iActive2
						+ ", <5m: " + iActive5
						+ ", <10m: " + iActive10
						+ ", <15m: " + iActive15 + ")" +
					", waiting: " + iWaiting +
						(iWaiting > 0 ? " (avg: " + df.format(iWaitTime.getAverage() / 60.0) + "m)" : "") +
					", gotin: " + iGotIn +
						(iGotIn > 0 && iWaitTimeWhenGotIn.getTotal() > 0l ? " (avg: " + df.format(iWaitTimeWhenGotIn.getAverage() / 60.0) + "m)" : "") +
					", left: " + iLeft +
						(iLeft > 0 ? " (avg: " + df.format(iAccessTimeWhenLeft.getAverage() / 60.0) + "m)" : "") +
					", gaveup: " + iGaveUp +
						(iGaveUp > 0 ? " (avg: " + df.format(iGaveUpTime.getAverage() / 60.0) + "m)" : "") +
					", opened: " + iOpened +
					", tracking: " + iTotal + "}";
		}
		
		public AccessStatistics generateAccessStatisticsRecord() {
			AccessStatistics stat = new AccessStatistics();
			stat.setTimeStamp(new Date(iT0));
			stat.setPage(iPage);
			stat.setAccess(iAccess);
			stat.setActive(iActive);
			stat.setOpened(iOpened);
			stat.setWaiting(iWaiting);
			stat.setTracking(iTotal);
			stat.setActive1m(iActive1);
			stat.setActive2m(iActive2);
			stat.setActive5m(iActive5);
			stat.setActive10m(iActive10);
			stat.setActive15m(iActive15);
			stat.setGotIn(iGotIn);
			stat.setGaveUp(iGaveUp);
			stat.setLeft(iLeft);
			stat.setAvgAccessTime(iAccessTime.getAverage());
			stat.setAvgAccessTimeWhenLeft(iAccessTimeWhenLeft.getAverage());
			stat.setAvgWaitTime(iWaitTime.getAverage());
			stat.setAvgWaitTimeWhenGotIn(iWaitTimeWhenGotIn.getAverage());
			return stat;
		}
		
		public boolean accept(String t, PingData u) {
			if (sLog.isDebugEnabled())
				sLog.debug(t + ": " + u.toString(iT0));
			if (u.getPingAge(iT0) > 30 * 60) return false;
			if (u.isAccess() && !u.isOpened(iT0))
				u.setAccess(false, iT0);
			inc(u);
			return !u.isLoggedOut();
		}
		
		public boolean isEmpty() {
			return iTotal == 0;
		}
	}
	
	private class Counter implements BiConsumer<String, ConcurrentMap<String, PingData>> {
		private long iT0 = System.currentTimeMillis();
		List<PingCounter> iCounters = new ArrayList<>();

		@Override
		public void accept(String page, ConcurrentMap<String, PingData> u) {
			PingCounter pc = new PingCounter(page, iT0);
			for (Iterator<Map.Entry<String, PingData>> i = u.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, PingData> e = i.next();
				if (!pc.accept(e.getKey(), e.getValue())) {
					if (sLog.isTraceEnabled())
						sLog.trace(e.getKey() + ": removed");
					i.remove();
				}
			}
			if (!pc.isEmpty())
				iCounters.add(pc);
		}
		
		@Override
		public String toString() {
			String ret = "";
			for (PingCounter pc: iCounters)
				ret = (ret.isEmpty() ? "" : ret + "\n") + pc;
			return ret;
		}
		
		protected void record(String host) {
			org.hibernate.Session hibSession = AccessStatisticsDAO.getInstance().createNewSession();
			try {
				for (PingCounter pc: iCounters) {
					AccessStatistics stat = pc.generateAccessStatisticsRecord();
					stat.setHost(host);
					hibSession.persist(stat);
				}
				hibSession.flush();
			} finally {
				hibSession.close();
			}
		}
		
		public boolean isEmpty() {
			return iCounters.isEmpty();
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
	
	public static class Listener implements HttpSessionListener {
		private AccessControllBackend iBackend;
		
		private AccessControllBackend getBackend(HttpSessionEvent event) {
			if (iBackend == null) {
				WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getSession().getServletContext());
				iBackend = (AccessControllBackend)applicationContext.getBean(PingRequest.class.getName());
			}
			return iBackend;
		}
		
		protected ConcurrentMap<String, ConcurrentMap<String, PingData>> getData(HttpSessionEvent event) {
			return getBackend(event).getData();
		}
		
		@Override
		public void sessionCreated(HttpSessionEvent event) {
		}

		@Override
		public void sessionDestroyed(HttpSessionEvent event) {
			getData(event).forEach((page, data) -> {
				PingData pd = data.get(event.getSession().getId());
				if (pd != null) {
					pd.logout();
					if (sLog.isTraceEnabled())
						sLog.trace(event.getSession().getId() + ": REMOVE " + pd.toString(System.currentTimeMillis()));
				}
			});
		}
	}
}
