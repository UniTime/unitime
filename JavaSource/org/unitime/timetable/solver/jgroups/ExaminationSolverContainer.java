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

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.ifs.util.DataProperties;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.solver.SolverDisposeListener;
import org.unitime.timetable.solver.exam.ExamSolver;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.util.MemoryCounter;

/**
 * @author Tomas Muller
 */
public class ExaminationSolverContainer implements SolverContainer<ExamSolverProxy> {
	private static Log sLog = LogFactory.getLog(ExaminationSolverContainer.class);

	protected Map<String,ExamSolver> iExamSolvers = new Hashtable<String, ExamSolver>();
	private PassivationThread iPassivation = null;

	@Override
	public Set<String> getSolvers() {
		return new HashSet<String>(iExamSolvers.keySet());
	}
	
	@Override
	public ExamSolverProxy getSolver(String user) {
		return iExamSolvers.get(user);
	}
	
	@Override
	public long getMemUsage(String user) {
		ExamSolverProxy solver = getSolver(user);
		return solver == null ? 0 : new MemoryCounter().estimate(solver);
	}
	
	@Override
	public ExamSolverProxy createSolver(String user, DataProperties config) {
		ExamSolver solver = new ExamSolver(config, new SolverOnDispose(user));
		iExamSolvers.put(user, solver);
        return solver;
	}
	
	@Override
	public void unloadSolver(String user) {
		ExamSolver solver = iExamSolvers.get(user);
		if (solver != null)
			solver.dispose();
	}
	
	@Override
	public boolean hasSolver(String user) {
		return iExamSolvers.containsKey(user);
	}
	
	@Override
	public int getUsage() {
		int ret = 0;
		for (ExamSolverProxy solver: iExamSolvers.values()) {
			ret++;
			if (!solver.isPassivated()) ret++;
			try {
				if (solver.isWorking()) ret++;
			} catch (Exception e) {};
		}
		return ret;		
	}
	
	@Override
	public void start() {
		iPassivation = new PassivationThread(ApplicationProperties.getPassivationFolder());
		iPassivation.start();
		File folder = ApplicationProperties.getRestoreFolder();
		if (!folder.exists() || !folder.isDirectory()) return;
		
		BackupFileFilter filter = new BackupFileFilter(SolverParameterGroup.SolverType.EXAM);
		File[] files = folder.listFiles(filter);
		for (int i=0;i<files.length;i++) {
			File file = files[i];
			String user = filter.getUser(file);
			ExamSolver solver = new ExamSolver(new DataProperties(), new SolverOnDispose(user));
			if (solver.restore(folder,user)) {
				if (ApplicationProperties.getPassivationFolder() != null)
					solver.passivate(ApplicationProperties.getPassivationFolder(), user);
				iExamSolvers.put(user, solver);
			}

		}
	}
	
	@Override
	public void stop() {
		File folder = ApplicationProperties.getRestoreFolder();
		if (folder.exists() && !folder.isDirectory()) return;
		
		folder.mkdirs();
		File[] old = folder.listFiles(new BackupFileFilter(SolverParameterGroup.SolverType.EXAM));
		for (int i = 0; i < old.length; i++)
			old[i].delete();
		
		for (Map.Entry<String, ExamSolver> entry: iExamSolvers.entrySet()) {
			entry.getValue().backup(folder, entry.getKey());
		}
		iPassivation.destroy();
	}
	
    protected class SolverOnDispose implements SolverDisposeListener {
        String iUser = null;
        public SolverOnDispose(String user) {
        	iUser = user;
        }
        @Override
        public void onDispose() {
            iExamSolvers.remove(iUser);
        }
    }
    
	private class PassivationThread extends Thread {
		private File iFolder = null;
		public long iDelay = 30000;
		public boolean iContinue = true;
		
		public PassivationThread(File folder) {
			iFolder = folder;
			setName("Passivation[Examination]");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}
		
		public void run() {
			try {
				sLog.info("Solver passivation thread started.");
				while (iContinue) {
					for (Map.Entry<String, ExamSolver> entry: iExamSolvers.entrySet())
						entry.getValue().passivateIfNeeded(iFolder, entry.getKey());
					try {
						sleep(iDelay);
					} catch (InterruptedException e) {
					    break;
	                }
				}
				sLog.info("Solver passivation thread finished.");
			} catch (Exception e) {
				sLog.warn("Solver passivation thread failed, reason: " + e.getMessage(), e);
			}
		}
		
		public void destroy() {
			iContinue = false;
			if (isAlive()) interrupt();
		}
	}

}
