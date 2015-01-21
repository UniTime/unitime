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
