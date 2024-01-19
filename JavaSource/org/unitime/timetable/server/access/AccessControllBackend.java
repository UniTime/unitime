package org.unitime.timetable.server.access;

import java.util.ArrayList;
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
		PingData pd = iData.compute(context.getHttpSessionId(), (key, current) -> {
			if (current == null) current = new PingData();
			current.update(request, context);
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
				CheckQueue cq = new CheckQueue(request.getPage(), context.getHttpSessionId(), pd);
				iData.forEach(cq);
				if (cq.getUsersWithAccess() + cq.getUsersInQueueBeforeMe() + 1 <= maxActiveUsers) {
					pd.setAccess(true);
				} else {
					ret.setQueue(1 + cq.getUsersInQueueBeforeMe());
				}
			}
		} else {
			Integer activeLimitInSeconds = getActiveLimitInSeconds(request.getPage());
			if (activeLimitInSeconds != null && pd.getActiveAge() > activeLimitInSeconds && !unitimeBusySessions.isWorking(context.getHttpSessionId()))
				ret.setInactive(activeLimitInSeconds / 60);
		}
		if (sLog.isTraceEnabled())
			sLog.trace(context.getHttpSessionId() + ": " + request.getOperation() + " " + pd);
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
		
		PingData() {
			iFirstUse = System.currentTimeMillis();
			iLastPing = iFirstUse;
			iLastActive = iLastPing;
		}
		
		void update(PingRequest request, SessionContext context) {
			if (request.getOperation() == Operation.PING)
				iAccess = true;
			else if (request.getOperation() == Operation.LOGOUT)
				iAccess = false;
			if (request.getOperation() == Operation.CHECK_ACCESS && !iAccess && !isOpened())
				iFirstUse = System.currentTimeMillis();
			iLastPing = System.currentTimeMillis();
			iLoggedOut = (request.getOperation() == Operation.LOGOUT);
			if (request.isActive()) iLastActive = System.currentTimeMillis();
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
		
		public long getActiveAge() {
			return (System.currentTimeMillis() - iLastActive) / 1000;
		}
		
		public long getPingAge() {
			return (System.currentTimeMillis() - iLastPing) / 1000;
		}
		
		public long getAge() {
			return (System.currentTimeMillis() - iFirstUse) / 1000;
		}
		
		public long getFirstUse() { return iFirstUse; }
		
		public String getPage() {
			return iPage;
		}
		
		public boolean isOpened() {
			return getPingAge() <= 60 && !isLoggedOut();
		}
		
		public boolean isActive(Integer activeLimitInSeconds) {
			return isOpened() && (activeLimitInSeconds == null || getActiveAge() <= activeLimitInSeconds + 120);
		}
		
		public boolean isAccess() { return iAccess; }
		
		public void setAccess(boolean access) { iAccess = access; }
		
		public boolean isLoggedOut() { return iLoggedOut; }
		
		@Override
		public String toString() {
			return "PingData{" + (isLoggedOut() || !isOpened() ? "expired, " : isAccess() ? "access, " : "waiting, ") + (getAge()/60) + "m old, " + (getActiveAge()/60) + "m inactive, " + (getPingAge()/60) + "m unchecked" +
					", page=" + iPage + ", user=" + iUser + ", session=" + iSession + ", role=" + iAutority + "}";
		}

		@Override
		public int compareTo(PingData pd) {
			return Long.compare(getActiveAge(), pd.getActiveAge());
		}
	}
	
	private class CheckQueue implements BiConsumer<String, PingData> {
		private String iPage;
		private String iSession;
		private PingData iPingData;
		private int iAccess;
		private int iQueue;
		private Integer iActiveLimitInSeconds;
		
		private CheckQueue(String myPage, String mySession, PingData myData) {
			iPage = myPage;
			iSession = mySession;
			iPingData = myData;
			iActiveLimitInSeconds = getActiveLimitInSeconds(myPage);
		}
		
		@Override
		public void accept(String t, PingData u) {
			if (!iSession.equals(t) && iPage.equals(u.getPage()) && u.isActive(iActiveLimitInSeconds)) {
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
		private Map<String, Integer> iActive = new HashedMap<>();
		private Map<String, Integer> iAccess = new HashedMap<>();
		private Map<String, Integer> iOpened = new HashedMap<>();
		private Map<String, Integer> iTotal = new HashedMap<>();
		private List<String> iInactive = new ArrayList<>();
		
		@Override
		public void accept(String t, PingData u) {
			if (sLog.isDebugEnabled())
				sLog.debug(t + ": " + u);
			if (!unitimeBusySessions.isActive(t)) {
				iInactive.add(t);
			} else {
				if (u.isAccess() && !u.isOpened()) u.setAccess(false);
				iTotal.merge(u.getPage(), 1, (a, b) -> a + b);
				if (u.isActive(getActiveLimitInSeconds(u.getPage()))) {
					iActive.merge(u.getPage(), 1, (a, b) -> a + b);
					if (!u.isAccess())
						iWaiting.merge(u.getPage(), 1, (a, b) -> a + b);
				}
				if (u.isAccess())
					iAccess.merge(u.getPage(), 1, (a, b) -> a + b);
				if (u.isOpened())
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
					"active: " + iActive.getOrDefault(page, 0) +
					", access: " + iAccess.getOrDefault(page, 0) + (limit == null ? "" : " of " + limit) +
					", waiting: " + iWaiting.getOrDefault(page, 0) +
					", opened: " + iOpened.getOrDefault(page, 0) +
					", tracking: " + iTotal.getOrDefault(page, 0) + "}";
			}
			return ret;
		}
	}
	
	private class Updater extends Thread {
		private boolean iActive = true;

		public Updater() {
			super("PingBackend.Updater");
			setDaemon(true);
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
					if (!c.isEmpty()) sLog.info(c);
					if (!iActive) break;
				} catch (Exception e) {
					sLog.error(e.getMessage(), e);
				}
			}
			sLog.debug("Access Controll Updater is down.");
		}
	}


}
