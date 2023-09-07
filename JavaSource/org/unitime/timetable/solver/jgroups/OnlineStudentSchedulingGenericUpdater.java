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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.ToolBox;
import org.jgroups.Address;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;

/**
 * @author Tomas Muller
 */
public class OnlineStudentSchedulingGenericUpdater extends Thread {
	private Log iLog;
	private long iSleepTimeInSeconds = 5;
	private boolean iRun = true, iPause = false;
	
	private RpcDispatcher iDispatcher;
	private OnlineStudentSchedulingContainerRemote iContainer;
	private Long iLastViewChange = null;

	public OnlineStudentSchedulingGenericUpdater(RpcDispatcher dispatcher, OnlineStudentSchedulingContainerRemote container) {
		super();
		iDispatcher = dispatcher;
		iContainer = container;
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
					checkForNewServers(false);
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
	
	public void viewChanged() {
		iLastViewChange = System.currentTimeMillis();
	}
	
	public synchronized void checkForNewServers(boolean reload) {
		if (!isCoordinator()) return;
		if (!HibernateUtil.isConfigured()) {
			iLog.info("Hibernate is not configured yet, will check for new servers later...");
			return;
		}
		boolean create = (iLastViewChange == null || (System.currentTimeMillis() - iLastViewChange) >= (iSleepTimeInSeconds * 500));
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			Map<String, Set<Address>> solvers = new HashMap<String, Set<Address>>();
			try {
				RspList<Set<String>> ret = iContainer.getDispatcher().callRemoteMethods(
						null, "getSolvers", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
				for (Map.Entry<Address, Rsp<Set<String>>> entry : ret.entrySet()) {
					Address sender = entry.getKey();
					Rsp<Set<String>> rsp = entry.getValue();
					if (rsp.getValue() == null) continue;
					for (String solver: rsp.getValue()) {
						Set<Address> members = solvers.get(solver);
						if (members == null) {
							members = new HashSet<Address>();
							solvers.put(solver, members);
						}
						members.add(sender);
					}
				}
			} catch (Exception e) {
				iLog.error("Failed to retrieve servers: " + e.getMessage(), e);
				return;
			}
			
			for (Iterator<Session> i = SessionDAO.getInstance().findAll(hibSession).iterator(); i.hasNext(); ) {
				Session session = i.next();
				if (solvers.containsKey(session.getUniqueId().toString())) {
					try {
						Set<Address> members = solvers.get(session.getUniqueId().toString());
						if (members.size() > 1) {
							iLog.warn(members.size() + " members for " + session.getLabel() + " detected.");
							// pick a master
							Address master = ToolBox.random(members); members.remove(master);
							// reload master
							iLog.warn("Reloading " + master + " for " + session.getLabel() + ".");
							iContainer.getDispatcher().callRemoteMethod(master, "invoke",
									new Object[] { "reload", session.getUniqueId().toString(), new Class[] {}, new Object[] {}},
									new Class[] { String.class, String.class, Class[].class, Object[].class },
									SolverServerImplementation.sFirstResponse);
							// unload all others
							for (Address member: members) {
								if (!member.equals(master)) {
									iLog.warn("Unloading " + member + " for " + session.getLabel() + ".");
									iContainer.getDispatcher().callRemoteMethod(member, "unloadSolver",
											new Object[] { session.getUniqueId().toString() },
											new Class[] { String.class },
											SolverServerImplementation.sFirstResponse);
								}
							}
						} else if (reload) {
							for (Address member: members) {
								iLog.warn("Reloading " + member + " for " + session.getLabel() + ".");
								iContainer.getDispatcher().callRemoteMethod(member, "invoke",
										new Object[] { "reload", session.getUniqueId().toString(), new Class[] {}, new Object[] {}},
										new Class[] { String.class, String.class, Class[].class, Object[].class },
										SolverServerImplementation.sFirstResponse);
								break;
							}
						}
					} catch (Exception e) {
						iLog.error("Failed to release master locks for " + session.getLabel() + ": " + e.getMessage(), e);
						continue;
					}
					continue;
				}
				if (session.getStatusType().isTestSession()) continue;
				if (!session.getStatusType().canSectionAssistStudents() && !session.getStatusType().canOnlineSectionStudents()) continue;
				
				if (!create) {
					iLog.info("Waiting with " + session.getLabel() + " -- too soon after a vew change, will load later.");
					continue;
				}
				
				int nrSolutions = (hibSession.createQuery(
						"select count(s) from Solution s where s.owner.session.uniqueId=:sessionId", Number.class)
						.setParameter("sessionId", session.getUniqueId()).uniqueResult()).intValue();
				if (nrSolutions == 0) continue;
				
				List<Address> available = new ArrayList<Address>();
				try {
					RspList<Boolean> ret = iDispatcher.callRemoteMethods(null, "isAvailable", new Object[] {}, new Class[] {}, SolverServerImplementation.sAllResponses);
					for (Map.Entry<Address, Rsp<Boolean>> entry : ret.entrySet()) {
						Address sender = entry.getKey();
						Rsp<Boolean> rsp = entry.getValue();
						if (Boolean.TRUE.equals(rsp.getValue()))
							available.add(sender);
					}
				} catch (Exception e) {
					iLog.fatal("Unable to update session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
				}
				
				if (available.isEmpty()) {
					iLog.fatal("Unable to update session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: no server available.");
					continue;
				}
				
				
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
						usages.remove(bestAddress);
						
						iLog.info("Loading " + bestAddress + " for " + session.getLabel() + ".");
						Boolean created = iContainer.getDispatcher().callRemoteMethod(
								bestAddress,
								"createRemoteSolver", new Object[] { session.getUniqueId().toString(), null, iDispatcher.getChannel().getAddress() },
								new Class[] { String.class, DataProperties.class, Address.class },
								SolverServerImplementation.sFirstResponse);
						if (created) break;
						else iLog.info("Unable to load " + bestAddress + " for " + session.getLabel() + ".");
					}
				} catch (Exception e) {
					iLog.fatal("Unable to update session " + session.getAcademicTerm() + " " + session.getAcademicYear() + " (" + session.getAcademicInitiative() + "), reason: "+ e.getMessage(), e);
				}
			}
		} finally {
			hibSession.close();
		}
	}
	
	public boolean isCoordinator() {
		return iDispatcher.getChannel().getView().getMembers().get(0).equals(iDispatcher.getChannel().getAddress());
	}
}
