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
            sLog.info("Solver passivation thread finished.");
		} catch (Exception e) {
			sLog.error("Solver passivation thread failed, reason: "+e.getMessage(), e);
		}
	}

}
