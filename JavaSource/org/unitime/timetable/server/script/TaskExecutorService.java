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
package org.unitime.timetable.server.script;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.TaskInterface.ExecutionStatus;
import org.unitime.timetable.model.TaskExecution;
import org.unitime.timetable.model.dao.PeriodicTaskDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.security.evaluation.PermissionCheck;
import org.unitime.timetable.solver.service.SolverServerService;

/**
 * @author Tomas Muller
 */
@Service("periodicTaskExecutor")
@DependsOn({"startupService", "solverServerService"})
public class TaskExecutorService implements InitializingBean, DisposableBean {
	private static Logger sLog = Logger.getLogger(TaskExecutorService.class);
	private TaskExecutor iExecutor = null;
	
	@Autowired SolverServerService solverServerService;
	
	@Autowired PermissionCheck unitimePermissionCheck;

	@Override
	public void destroy() throws Exception {
		iExecutor.interrupt();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		iExecutor = new TaskExecutor();
		iExecutor.start();
	}
	
	public void checkForQueuedTasks() throws Exception {
		List<TaskExecutionItem> items = new ArrayList<TaskExecutionItem>();
		org.hibernate.Session hibSession = PeriodicTaskDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			List<TaskExecution> executions = (List<TaskExecution>)hibSession.createQuery(
					"from TaskExecution e where e.executionStatus = :status and e.scheduledDate <= :now"
					).setTimestamp("now", new Date()).setInteger("status", ExecutionStatus.QUEUED.ordinal()).list();
			for (TaskExecution execution: executions) {
				if (solverServerService.getQueueProcessor().getByExecutionId(execution.getUniqueId()) != null) continue;
				try {
					TaskExecutionItem item = new TaskExecutionItem(execution, unitimePermissionCheck);
					item.setTaskExecutionId(execution.getUniqueId());
					items.add(item);
					execution.setExecutionStatus(ExecutionStatus.QUEUED.ordinal());
					execution.setQueuedDate(new Date());
				} catch (Exception e) {
					execution.setExecutionStatus(ExecutionStatus.FAILED.ordinal());
					execution.setStatusMessageCheckLength("Failed to execute: " + e.getMessage());
					sLog.warn("Failed to execute " + execution.getTask().getName() + ": " + e.getMessage(), e);
				}
				
				hibSession.update(execution);
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			hibSession.close();
			_RootDAO.closeCurrentThreadSessions();
		}
		for (TaskExecutionItem item: items)
			solverServerService.getQueueProcessor().add(item);
	}
	
	public void checkForTasks() throws Exception {
		List<TaskExecutionItem> items = new ArrayList<TaskExecutionItem>();
		org.hibernate.Session hibSession = PeriodicTaskDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			List<TaskExecution> executions = (List<TaskExecution>)hibSession.createQuery(
					"from TaskExecution e where e.executionStatus = :status and e.scheduledDate <= :now"
					).setTimestamp("now", new Date()).setInteger("status", ExecutionStatus.CREATED.ordinal()).list();
			for (TaskExecution execution: executions) {
				try {
					TaskExecutionItem item = new TaskExecutionItem(execution, unitimePermissionCheck);
					item.setTaskExecutionId(execution.getUniqueId());
					items.add(item);
					execution.setExecutionStatus(ExecutionStatus.QUEUED.ordinal());
					execution.setQueuedDate(new Date());
				} catch (Exception e) {
					execution.setExecutionStatus(ExecutionStatus.FAILED.ordinal());
					execution.setStatusMessageCheckLength("Failed to execute: " + e.getMessage());
					sLog.warn("Failed to execute " + execution.getTask().getName() + ": " + e.getMessage(), e);
				}
				
				hibSession.update(execution);
			}
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			hibSession.close();
			_RootDAO.closeCurrentThreadSessions();
		}
		for (TaskExecutionItem item: items)
			solverServerService.getQueueProcessor().add(item);
	}
	
	public class TaskExecutor extends Thread {
		private int iSleepTimeInMinutes;
		private boolean iActive = true;
		
		public TaskExecutor() {
			setName("TaskExecutorService");
			setDaemon(true);
			iSleepTimeInMinutes = ApplicationProperty.TaskSchedulerCheckIntervalInMinutes.intValue();
		}
		
		protected boolean isEnabled() {
			return iActive && solverServerService.getLocalServer() != null && solverServerService.getLocalServer().isActive() && solverServerService.getLocalServer().isLocalCoordinator();
		}
		
		@Override
		public void run() {
			try {
				sLog.info("Task executor service started.");
				long iteration = 0;
				while (iActive) {
					try {
						sleep(iSleepTimeInMinutes * 60000);
					} catch (InterruptedException e) {}
					try {
						if (isEnabled()) {
							if (iteration == 0)
								checkForQueuedTasks();
							else
								checkForTasks();
							iteration ++;
						}
					} catch (Exception e) {
						sLog.error("Failed to check for tasks: " + e.getMessage(), e);
					}
				}
				sLog.info("Task executor service stopped.");
			} catch (Exception e) {
				sLog.info("Task executor service failed, " + e.getMessage(), e);
			}
		}

		@Override
		public void interrupt() {
			iActive = false;
			super.interrupt();
			try { join(); } catch (InterruptedException e) {}
		}
		
	}
}
