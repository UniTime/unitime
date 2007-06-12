/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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

/**
 * @author Tomas Muller
 */
public class SolverPassivationThread extends Thread {
	private static Log sLog = LogFactory.getLog(SolverPassivationThread.class);
	private File iFolder = null;
	private Hashtable iSolvers = null;
	public static long sDelay = 30000;
	
	public SolverPassivationThread(File folder, Hashtable solvers) {
		iFolder = folder;
		iSolvers = solvers;
		setName("SolverPasivationThread");
		setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
	}
	
	public void run() {
		try {
			while (true) {
				for (Iterator i=iSolvers.entrySet().iterator();i.hasNext();) {
					Map.Entry entry = (Map.Entry)i.next();
					String puid = (String)entry.getKey();
					SolverProxy solver = (SolverProxy)entry.getValue();
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
			sLog.error("Solver passivation thread failed, reason: "+e.getMessage(),e);
		}
	}

}
