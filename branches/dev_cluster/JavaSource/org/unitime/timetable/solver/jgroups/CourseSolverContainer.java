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

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.TimetableSolver;
import org.unitime.timetable.solver.TimetableSolver.SolverDisposeListener;
import org.unitime.timetable.solver.remote.BackupFileFilter;

public class CourseSolverContainer implements SolverContainer<SolverProxy> {
	private static Log sLog = LogFactory.getLog(CourseSolverContainer.class);
	
	protected Map<String,TimetableSolver> iCourseSolvers = new Hashtable<String, TimetableSolver>();
	private PassivationThread iPassivation = null;

	@Override
	public Set<String> getSolvers() {
		return new HashSet<String>(iCourseSolvers.keySet());
	}
	
	@Override
	public SolverProxy getSolver(String user) {
		return iCourseSolvers.get(user);
	}
	
	@Override
	public SolverProxy createSolver(String user, DataProperties config) {
		TimetableSolver solver = new TimetableSolver(config, new SolverOnDispose(user));
        iCourseSolvers.put(user, solver);
        return solver;
	}
	
	@Override
	public boolean hasSolver(String user) {
		return iCourseSolvers.containsKey(user);
	}
	
	@Override
	public int getUsage() {
		int ret = 0;
		for (SolverProxy solver: iCourseSolvers.values()) {
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
		sLog.info("Restore folder: " + folder);
		if (!folder.exists() || !folder.isDirectory()) return;
		
		File[] files = folder.listFiles(new BackupFileFilter(true, false, SolverParameterGroup.sTypeCourse));
		for (int i=0;i<files.length;i++) {
			File file = files[i];
			String user = file.getName().substring(0,file.getName().indexOf('.'));
            TimetableSolver solver = new TimetableSolver(new DataProperties(), new SolverOnDispose(user));
			if (solver.restore(folder, user)) {
				if (ApplicationProperties.getPassivationFolder() != null)
					solver.passivate(ApplicationProperties.getPassivationFolder(), user);
				iCourseSolvers.put(user, solver);
			}
		}
	}
	
	@Override
	public void stop() {
		File folder = ApplicationProperties.getRestoreFolder();
		if (folder.exists() && !folder.isDirectory()) return;
		
		folder.mkdirs();
		File[] old = folder.listFiles(new BackupFileFilter(true, true, SolverParameterGroup.sTypeCourse));
		for (int i = 0; i < old.length; i++)
			old[i].delete();
		
		for (Map.Entry<String, TimetableSolver> entry: iCourseSolvers.entrySet()) {
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
            iCourseSolvers.remove(iUser);
        }
    }
    
	private class PassivationThread extends Thread {
		private File iFolder = null;
		public long iDelay = 30000;
		public boolean iContinue = true;
		
		public PassivationThread(File folder) {
			iFolder = folder;
			setName("Passivation[CourseTimetabling]");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}
		
		public void run() {
			try {
				sLog.info("Solver passivation thread started.");
				while (iContinue) {
					for (Map.Entry<String, TimetableSolver> entry: iCourseSolvers.entrySet())
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
