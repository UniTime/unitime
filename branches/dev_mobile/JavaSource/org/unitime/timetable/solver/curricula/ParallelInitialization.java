/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.timetable.solver.curricula;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cpsolver.ifs.util.Progress;

public class ParallelInitialization {
	private Lock iLock = new ReentrantLock();
	private List<? extends Task> iTasks = null;
	private int iNrThreads;
	private String iPhase;
	
	public ParallelInitialization(String phase, int nrThreads, List<? extends Task> tasks) {
		iPhase = phase;
        iNrThreads = nrThreads;
		iTasks = tasks;
	}
	
	public void execute(org.hibernate.Session hibSession, Progress progress) {
		progress.setPhase(iPhase, iTasks.size());
		if (iNrThreads <= 1) {
			for (Task task: iTasks) {
				task.setup(hibSession);
				task.execute();
				task.teardown(hibSession);
				progress.incProgress();
			}
		} else {
			Iterator<? extends Task> iterator = iTasks.iterator();
	        List<Executor> executors = new ArrayList<Executor>();
	        for (int i = 0; i < iNrThreads; i++) {
	        	Executor executor = new Executor(i, iterator, progress, hibSession);
	        	executor.start();
	        	executors.add(executor);
	        }
	        for (Executor executor: executors) {
	        	try {
	        		executor.join();
	        	} catch (InterruptedException e) {}
	        }
	        for (Executor executor: executors)
	        	if (executor.getException() != null) 
	        		throw new ParallelInitializationException(executor.getException());
		}
	}
	
	public static interface Task {
		public void setup(org.hibernate.Session hibSession);
		public void execute();
		public void teardown(org.hibernate.Session hibSession);
	}
	
    public class Executor extends Thread {
    	private Iterator<? extends Task> iIterator;
    	private Progress iProgress;
    	private org.hibernate.Session iHibSession;
    	private Exception iException;
		
		public Executor(int index, Iterator<? extends Task> iterator, Progress progress, org.hibernate.Session hibSession) {
			setName("Initialization-" + (1 + index));
			iIterator = iterator;
			iProgress = progress;
			iHibSession = hibSession;
		}
		
		public Exception getException() { return iException; }
		
		@Override
		public void run() {
			try {
				for (;;) {
					Task task = iIterator.next();
					
					// setup task (one at a time)
					iLock.lock();
					try {
						task.setup(iHibSession);
					} finally {
						iLock.unlock();
					}
					
					// execute task (in parallel)
					task.execute();
					
					// tear down task (one at a time)
					iLock.lock();
					try {
						task.teardown(iHibSession);
						iProgress.incProgress();
					} finally {
						iLock.unlock();
					}
				}
			} catch (NoSuchElementException e) {
			} catch (Exception e) {
				iException = e;
			}
		}
	}
    
    public static class ParallelInitializationException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ParallelInitializationException(Exception e) {
    		super(e.getMessage(), e);
    	}
    }


}
