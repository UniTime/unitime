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
import org.unitime.timetable.solver.remote.BackupFileFilter;
import org.unitime.timetable.solver.studentsct.StudentSolver;
import org.unitime.timetable.solver.studentsct.StudentSolver.StudentSolverDisposeListener;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.MemoryCounter;

/**
 * @author Tomas Muller
 */
public class StudentSolverContainer implements SolverContainer<StudentSolverProxy> {
	private static Log sLog = LogFactory.getLog(StudentSolverContainer.class);
	
	protected Map<String,StudentSolver> iStudentSolvers = new Hashtable<String, StudentSolver>();
	private PassivationThread iPassivation = null;

	@Override
	public Set<String> getSolvers() {
		return new HashSet<String>(iStudentSolvers.keySet());
	}
	
	@Override
	public StudentSolverProxy getSolver(String user) {
		return iStudentSolvers.get(user);
	}
	
	@Override
	public long getMemUsage(String user) {
		StudentSolverProxy solver = getSolver(user);
		return solver == null ? 0 : new MemoryCounter().estimate(solver);
	}
	
	@Override
	public StudentSolverProxy createSolver(String user, DataProperties config) {
		StudentSolver solver = new StudentSolver(config, new SolverOnDispose(user));
        iStudentSolvers.put(user, solver);
        return solver;
	}
	
	@Override
	public void unloadSolver(String user) {
		StudentSolver solver = iStudentSolvers.get(user);
		if (solver != null)
			solver.dispose();
	}
	
	@Override
	public boolean hasSolver(String user) {
		return iStudentSolvers.containsKey(user);
	}
	
	@Override
	public int getUsage() {
		int ret = 0;
		for (StudentSolverProxy solver: iStudentSolvers.values()) {
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
		
		File[] files = folder.listFiles(new BackupFileFilter(true, false, SolverParameterGroup.sTypeStudent));
		for (int i=0;i<files.length;i++) {
			File file = files[i];
			String user = file.getName().substring("sct_".length(), file.getName().indexOf('.'));
			StudentSolver solver = new StudentSolver(new DataProperties(), new SolverOnDispose(user));
			if (solver.restore(folder, user)) {
				if (ApplicationProperties.getPassivationFolder() != null)
					solver.passivate(ApplicationProperties.getPassivationFolder(), user);
				iStudentSolvers.put(user, solver);
			}
		}
	}
	
	@Override
	public void stop() {
		File folder = ApplicationProperties.getRestoreFolder();
		if (folder.exists() && !folder.isDirectory()) return;
		
		folder.mkdirs();
		File[] old = folder.listFiles(new BackupFileFilter(true, true, SolverParameterGroup.sTypeStudent));
		for (int i = 0; i < old.length; i++)
			old[i].delete();
		
		for (Map.Entry<String, StudentSolver> entry: iStudentSolvers.entrySet()) {
			entry.getValue().backup(folder, entry.getKey());
		}
		iPassivation.destroy();
	}
	
    protected class SolverOnDispose implements StudentSolverDisposeListener {
        String iUser = null;
        public SolverOnDispose(String user) {
        	iUser = user;
        }
        @Override
        public void onDispose() {
            iStudentSolvers.remove(iUser);
        }
    }
    
	private class PassivationThread extends Thread {
		private File iFolder = null;
		public long iDelay = 30000;
		public boolean iContinue = true;
		
		public PassivationThread(File folder) {
			iFolder = folder;
			setName("Passivation[StudentSectioning]");
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}
		
		public void run() {
			try {
				sLog.info("Solver passivation thread started.");
				while (iContinue) {
					for (Map.Entry<String, StudentSolver> entry: iStudentSolvers.entrySet())
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