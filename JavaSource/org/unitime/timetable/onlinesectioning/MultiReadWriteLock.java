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
package org.unitime.timetable.onlinesectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.cpsolver.ifs.util.ToolBox;


/**
 * @author Tomas Muller
 */
public class MultiReadWriteLock {
	protected Lock iLock = new ReentrantLock();
	protected Condition iLockNotAvailable = iLock.newCondition(), iGlobalLockNotAvailable = iLock.newCondition();
	protected Map<Long, ReadWriteLock> iIndividualLocks = new HashMap<Long, ReadWriteLock>();
	protected ReadWriteLock iGlobalLock = new ReentrantReadWriteLock(true);
	protected int iGlobalLockRequests = 0;
	
	public Unlock lock(boolean write, Long... ids) {
		List<Long> list = new ArrayList<Long>(ids.length);
		for (Long id: ids)
			list.add(id);
		return lock(write, list);
	}
	
	public Unlock tryLock(boolean write, Long... ids) {
		List<Long> list = new ArrayList<Long>(ids.length);
		for (Long id: ids)
			list.add(id);
		return tryLock(write, list);
	}

	public Unlock lock(boolean write, Collection<Long> ids) {
		iLock.lock();
		try {
			if (ids == null || ids.isEmpty()) return new Unlock();
			while (true) {
				Unlock unlock = tryLock(write, ids);
				if (unlock != null) return unlock;
				iLockNotAvailable.awaitUninterruptibly();
			}
		} finally {
			iLock.unlock();
		}
	}
	
	public Unlock tryLock(boolean write, Collection<Long> ids) {
		iLock.lock();
		try {
			if (ids == null || ids.isEmpty()) return null;
			List<Lock> acquiredLocks = new ArrayList<Lock>();
			if (iGlobalLock.readLock().tryLock()) {
				acquiredLocks.add(iGlobalLock.readLock()); 
			} else {
				return null;
			}
			for (Long courseId: ids) {
				ReadWriteLock courseLock = iIndividualLocks.get(courseId);
				if (courseLock == null) {
					courseLock = new ReentrantReadWriteLock(false);
					iIndividualLocks.put(courseId, courseLock);
				}
				Lock lock =  (write ? courseLock.writeLock() : courseLock.readLock());
				if (lock.tryLock()) {
					acquiredLocks.add(lock);
				} else {
					for (Lock undo: acquiredLocks) undo.unlock();
					return null;
				}
			}
			return new Unlock(acquiredLocks);
		} finally {
			iLock.unlock();
		}
	}
	
	public Unlock lockAll() {
		iLock.lock();
		try {
			while (true) {
				if (iGlobalLock.writeLock().tryLock())
					return new Unlock(iGlobalLock.writeLock());
				else {
					iGlobalLockRequests ++;
					iGlobalLockNotAvailable.awaitUninterruptibly();
					iGlobalLockRequests --;
				}
			}
		} finally {
			iLock.unlock();
		}
	}
	
	public Unlock empty() {
		return new Unlock();
	}
	
	public class Unlock implements OnlineSectioningServer.Lock {
		private List<Lock> iAcquiredLocks;
		
		private Unlock(List<Lock> acquiredLocks) {
			iAcquiredLocks = acquiredLocks;
		}
		
		private Unlock(Lock... acquiredLocks) {
			iAcquiredLocks = new ArrayList<Lock>();
			for (Lock lock: acquiredLocks)
				iAcquiredLocks.add(lock);
		}
		
		public void release() {
			iLock.lock();
			try {
				for (Lock lock: iAcquiredLocks)
					lock.unlock();
				if (iGlobalLockRequests > 0)
					iGlobalLockNotAvailable.signal();
				else
					iLockNotAvailable.signalAll();
			} finally {
				iLock.unlock();
			}
		}
	}

	public void remove(Long id) {
		iLock.lock();
		try {
			iIndividualLocks.remove(id);
		} finally {
			iLock.unlock();
		}
	}
	
	public void removeAll() {
		iLock.lock();
		try {
			iIndividualLocks.clear();
		} finally {
			iLock.unlock();
		}
	}

	public static void main(String[] args) {
		try {
			final MultiReadWriteLock lock = new MultiReadWriteLock();
			for (int i = 1; i <= 1000; i++) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							for (int x = 1; x <= 10; x++) {
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
								boolean write = (ToolBox.random(10) == 0);
								System.out.println(Thread.currentThread().getName() + "(" + x +") " + (write ? "Write " : "Read ") + "locking: [" + s + "]");
								Unlock unlock = lock.lock(write, courses);
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +(write ? "Write " : "Read ") + "locked: [" + s + "]");
								try {
									Thread.sleep(ToolBox.random(1000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +(write ? "Write " : "Read ") + "unlocking: [" + s + "]");
								unlock.release();
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +(write ? "Write " : "Read ") + "unlocked: [" + s + "]");
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
							for (int x = 1; x <= 10; x++) {
								try {
									Thread.sleep(ToolBox.random(5000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +"Locking all...");
								Unlock unlock = lock.lockAll();
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +"All locked.");
								try {
									Thread.sleep(ToolBox.random(1000));
								} catch (InterruptedException e) {}
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +"Unlocking all.");
								unlock.release();
								System.out.println(Thread.currentThread().getName() + "(" + x +") " +"All unlocked.");
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
