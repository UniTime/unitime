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
package org.unitime.timetable.util;

import java.util.Calendar;
import java.util.Date;

import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Stephanie Schluttenhofer
 */
public class FailedLoginAttempt {
	private String userId;
	private int count;
	private Date lastFailedAttempt;
	
	public FailedLoginAttempt (String userId, int count, Date lastFailedAttempt) {
		this.userId = userId;
		this.count = count;
		this.lastFailedAttempt = lastFailedAttempt;
	}		

	public boolean isUserLockedOut(String user, Date attemptTime){
		if (userId != null && user != null && userId.equals(user) && count >= ApplicationProperty.LoginMaxFailedAttempts.intValue()) {
			Calendar checkTime = Calendar.getInstance();
			checkTime.setTime(lastFailedAttempt);
			Calendar attempt = Calendar.getInstance();
			attempt.setTime(attemptTime);
			checkTime.add(Calendar.MINUTE, ApplicationProperty.LoginFailedLockout.intValue());
			boolean lockedOut = attempt.before(checkTime);
			if (!lockedOut){
				count = 0;
			}
			return(lockedOut);
		} else {
			return(false);
		}
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public Date getLastFailedAttempt() {
		return lastFailedAttempt;
	}
	public void setLastFailedAttempt(Date lastFailedAttempt) {
		this.lastFailedAttempt = lastFailedAttempt;
	}

}
