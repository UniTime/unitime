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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.log4j.Logger;
import org.cpsolver.ifs.util.DataProperties;
import org.jgroups.Address;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

/**
 * @author Tomas Muller
 */
public class OnlineStudentSchedulingGenericUpdater extends Thread {
	private Logger iLog;
	private long iSleepTimeInSeconds = 5;
	private boolean iRun = true, iPause = false;
	
	private RpcDispatcher iDispatcher;
	private OnlineStudentSchedulingContainerRemote iContainer;

	public OnlineStudentSchedulingGenericUpdater(RpcDispatcher dispatcher, OnlineStudentSchedulingContainerRemote container) {
		super();
		iDispatcher = dispatcher;
		iContainer = container;
		setDaemon(true);
		setName("Updater[generic]");
		iSleepTimeInSeconds = ApplicationProperty.OnlineSchedulingQueueLoadInterval.intValue(); 
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
		if (!isCoordinator()) return;
		Lock lock = iContainer.getLockService().getLock("updater[generic].check");
		lock.lock();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			boolean replicate = ApplicationProperty.OnlineSchedulingServerReplicated.isTrue();
			Map<String, Set<Address>> solvers = new HashMap<String, Set<Address>>();
			try {
				RspList<Set<String>> ret = iContainer.getDispatcher().callRemoteMethods(
						null, "getSolvers", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
				for (Rsp<Set<String>> rsp : ret) {
					if (rsp.getValue() == null) continue;
					for (String solver: rsp.getValue()) {
						Set<Address> members = solvers.get(solver);
						if (members == null) {
							members = new HashSet<Address>();
							solvers.put(solver, members);
						}
						members.add(rsp.getSender());
					}
				}
			} catch (Exception e) {
				iLog.error("Failed to retrieve servers: " + e.getMessage(), e);
				return;
			}
			
			for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
				Session session = i.next();
				
				if (!replicate && solvers.containsKey(session.getUniqueId().toString())) continue;
				if (session.getStatusType().isTestSession()) continue;
				if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;
				
				int nrSolutions = ((Number)hibSession.createQuery(
						"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId")
						.setLong("sessionId", session.getUniqueId()).uniqueResult()).intValue();
				if (nrSolutions == 0) continue;
				
				List<Address> available = new ArrayList<Address>();
				try {
					RspList<Boolean> ret = iDispatcher.callRemoteMethods(null, "isAvailable", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
					for (Rsp<Boolean> rsp : ret) {
						if (Boolean.TRUE.equals(rsp.getValue()))
							available.add(rsp.getSender());
					}
				} catch (Exception e) {
					iLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
				}
				
				if (available.isEmpty()) {
					iLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: no server available.");
					continue;
				}
				
				if (replicate) {
					Collections.shuffle(available);
					Set<Address> members = solvers.get(session.getUniqueId().toString());
					if (members != null) {
						boolean ready = false;
						for (Address address: members) {
							OnlineSectioningServer server = iContainer.createProxy(address, session.getUniqueId().toString());
							if (server.isReady()) { ready = true; break; }
						}
						if (!ready) continue;
					}
					try {
						for (Address address: available) {
							if (members != null && members.contains(address)) continue;
							Boolean created = iContainer.getDispatcher().callRemoteMethod(
									address,
									"createRemoteSolver", new Object[] { session.getUniqueId().toString(), null, iDispatcher.getChannel().getAddress() },
									new Class[] { String.class, DataProperties.class, Address.class },
									SolverServerImplementation.sFirstResponse);
							// startup only one server first
							if (members == null && created) break;
						}
					} catch (Exception e) {
						iLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				} else {
					try {
						// retrieve usage of the available serves
						Map<Address, Integer> usages = new HashMap<Address, Integer>();
						for (Address address: available) {
							Integer usage = iDispatcher.callRemoteMethod(address, "getUsage", new Object[] {}, new Class[] {}, SolverServerImplementation.sFirstResponse);
							usages.put(address, usage);
						}
						
						// while there is a server available, pick one with the lowest usage and try to create the solver there
						while (!usages.isEmpty()) {
							Address bestAddress = null;
							int bestUsage = 0;
							for (Map.Entry<Address, Integer> entry: usages.entrySet()) {
								if (bestAddress == null || bestUsage > entry.getValue()) {
									bestAddress = entry.getKey();
									bestUsage = entry.getValue();
								}
							}
							
							Boolean created = iContainer.getDispatcher().callRemoteMethod(
									bestAddress,
									"createRemoteSolver", new Object[] { session.getUniqueId().toString(), null, iDispatcher.getChannel().getAddress() },
									new Class[] { String.class, DataProperties.class, Address.class },
									SolverServerImplementation.sFirstResponse);
							if (created) break;
						}
					} catch (Exception e) {
						iLog.fatal("Unable to upadte session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
					}
				}
			}
		} finally {
			hibSession.close();
			lock.unlock();
		}
	}
	
	public boolean isCoordinator() {
		return iDispatcher.getChannel().getView().getMembers().get(0).equals(iDispatcher.getChannel().getAddress());
	}
}
