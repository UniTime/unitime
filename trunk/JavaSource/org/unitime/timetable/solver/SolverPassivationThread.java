/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.solver;

import java.io.File;
import java.util.Hashtable;
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
	private Hashtable iSolvers = null;
	private Hashtable iExamSolvers = null;
	private Hashtable iStudentSolvers = null;
	public static long sDelay = 30000;
	
	public SolverPassivationThread(File folder, Hashtable solvers, Hashtable examSolvers, Hashtable studentSolvers) {
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
