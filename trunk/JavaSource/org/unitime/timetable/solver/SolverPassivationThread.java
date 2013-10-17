/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
public class SolverPassivationThread extends Thread {
	private static Log sLog = LogFactory.getLog(SolverPassivationThread.class);
	private File iFolder = null;
	private Map<String, ? extends SolverProxy> iSolvers = null;
	private Map<String, ? extends ExamSolverProxy> iExamSolvers = null;
	private Map<String, ? extends StudentSolverProxy> iStudentSolvers = null;
	public static long sDelay = 30000;
	
	public SolverPassivationThread(File folder, Map<String, ? extends SolverProxy> solvers, Map<String, ? extends ExamSolverProxy> examSolvers, Map<String, ? extends StudentSolverProxy> studentSolvers) {
		iFolder = folder;
		iSolvers = solvers;
		iExamSolvers = examSolvers;
		iStudentSolvers = studentSolvers;
		setName("SolverPasivationThread");
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
	}
	
	public void run() {
		try {
            sLog.info("Solver passivation thread started.");
			while (true) {
				for (Iterator i=iSolvers.entrySet().iterator();i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					String puid = (String)entry.getKey();
					SolverProxy solver = (SolverProxy)entry.getValue();
					solver.passivateIfNeeded(iFolder, puid);
				}
                for (Iterator i=iExamSolvers.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    String puid = (String)entry.getKey();
                    ExamSolverProxy solver = (ExamSolverProxy)entry.getValue();
                    solver.passivateIfNeeded(iFolder, puid);
                }
                for (Iterator i=iStudentSolvers.entrySet().iterator();i.hasNext();) {
                    Map.Entry entry = (Map.Entry)i.next();
                    String puid = (String)entry.getKey();
                    StudentSolverProxy solver = (StudentSolverProxy)entry.getValue();
                    solver.passivateIfNeeded(iFolder, puid);
                }
				try {
					sleep(sDelay);
				} catch (InterruptedException e) {
				    break;
                }
			}
            System.out.println("Solver passivation thread finished.");
		} catch (Exception e) {
			System.err.print("Solver passivation thread failed, reason: "+e.getMessage());
			e.printStackTrace();
		}
	}

}
