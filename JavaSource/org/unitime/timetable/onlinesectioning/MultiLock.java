/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.cpsolver.ifs.util.ToolBox;

/**
 * @author Tomas Muller
 */
public class MultiLock {
    private Log iLog = LogFactory.getLog(MultiLock.class);
    private Lock iLock = new ReentrantLock();
    private Condition iAllLocked = null;
    private Map<Long, Condition> iIndividualLocks = new HashMap<Long, Condition>();

    public MultiLock() {
    	iLog = LogFactory.getLog(MultiLock.class.getName() + ".lock");
    }
    
    public MultiLock(AcademicSessionInfo session) {
    	iLog = LogFactory.getLog(MultiLock.class.getName() + ".lock[" + session.toCompactString() + "]");
    }
    
	private Condition hasLock(Collection<Long> ids) {
		if (iAllLocked != null) return iAllLocked;
		for (Long id: ids) {
			Condition c = iIndividualLocks.get(id);
			if (c != null) return c;
		}
		return null;
	}
	
	public Unlock lock(Long... ids) {
		List<Long> list = new ArrayList<Long>(ids.length);
		for (Long id: ids)
			list.add(id);
		return lock(list);
	}
	
	public UnlockAll lockAll() {
		iLock.lock();
		try {
			iLog.debug("Locking all ...");
			while (iAllLocked != null)
				iAllLocked.awaitUninterruptibly();
			iAllLocked = iLock.newCondition();
			while (!iIndividualLocks.isEmpty()) {
				Condition otherCondition = iIndividualLocks.values().iterator().next();
				otherCondition.awaitUninterruptibly();
			}
			iLog.debug("Locked: all");
			return new UnlockAll();
		} finally {
			iLock.unlock();
		}
	}
	
	public void unlockAll() {
		iLock.lock();
		try {
			iLog.debug("Unlocking all ...");
			Condition allLocked = iAllLocked;
			iAllLocked = null;
			allLocked.signalAll();
			iLog.debug("Unlocked: all");
		} finally {
			iLock.unlock();
		}
	}
	
	public Unlock lock(Collection<Long> ids) {
		iLock.lock();
		try {
			if (ids == null || ids.isEmpty()) return new Unlock(ids);
			iLog.debug("Locking " + ids + " ...");
			Condition otherCondition = null;
			while ((otherCondition = hasLock(ids)) != null)
				otherCondition.awaitUninterruptibly();
			Condition myCondition = iLock.newCondition();
			for (Long id: ids)
				iIndividualLocks.put(id, myCondition);
			iLog.debug("Locked: " + ids);
			return new Unlock(ids);
		} finally {
			iLock.unlock();
		}
	}
	
	private void unlock(Collection<Long> ids) {
		iLock.lock();
		try {
			if (ids == null || ids.isEmpty()) return;
			iLog.debug("Unlocking " + ids + " ...");
			Condition myCondition = null;
			for (Long id: ids)
				myCondition = iIndividualLocks.remove(id);
			if (myCondition != null)
				myCondition.signalAll();
			iLog.debug("Unlocked: " + ids);
		} finally {
			iLock.unlock();
		}
	}
	
	public Set<Long> locked() {
		iLock.lock();
		try {
			return new TreeSet<Long>(iIndividualLocks.keySet());
		} finally {
			iLock.unlock();
		}
	}
	
	public boolean isLocked(Long id) {
		iLock.lock();
		try {
			return iIndividualLocks.containsKey(id);
		} finally {
			iLock.unlock();
		}
	}
	
	public class Unlock implements OnlineSectioningServer.Lock {
		private Collection<Long> iIds;
		
		private Unlock(Collection<Long> ids) {
			iIds = ids;
		}
		
		public void release() {
			unlock(iIds);
		}
	}

	public class UnlockAll implements OnlineSectioningServer.Lock {
		
		private UnlockAll() {
		}
		
		public void release() {
			unlockAll();
		}
	}

	public static void main(String[] args) {
		try {
			final MultiLock lock = new MultiLock();
			for (int i = 1; i <= 1000; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							while (true) {
								int nrCourses = 2 + ToolBox.random(9);
								Set<Long> courses = new HashSet<Long>();
								String s = "";
								for (int i = 0; i < nrCourses; i++) {
									long courseId;
									do {
										courseId = ToolBox.random(10000);
									} while (!courses.add(courseId));
									s += (i > 0 ? ", " : "") + courseId;
								}
								System.out.println(Thread.currentThread().getName() + "Locking: [" + s + "]");
								Unlock l = lock.lock(courses);
								System.out.println(Thread.currentThread().getName() + "Locked: [" + s + "]");
								try {
									Thread.sleep(ToolBox.random(1000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "Unlocking: [" + s + "]");
								l.release();
								System.out.println(Thread.currentThread().getName() + "Unlocked: [" + s + "]");
							}
						} catch (Exception e) {
							System.err.println(Thread.currentThread().getName() + e.getMessage());
							e.printStackTrace();
						}
					}
				});
				t.setName("[T" + i + "]: ");
				t.start();
			}
			for (int i = 1; i <= 3; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							while (true) {
								try {
									Thread.sleep(ToolBox.random(5000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "Locking all...");
								lock.lockAll();
								System.out.println(Thread.currentThread().getName() + "All locked.");
								try {
									Thread.sleep(ToolBox.random(1000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "Unlocking all.");
								lock.unlockAll();
								System.out.println(Thread.currentThread().getName() + "All unlocked.");
							}
						} catch (Exception e) {
							System.err.println(Thread.currentThread().getName() + e.getMessage());
							e.printStackTrace();
						}
					}
				});
				t.setName("[A" + i + "]: ");
				t.start();		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
