/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2009 - 2013, UniTime LLC, and individual contributors
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

import java.util.Date;
import java.util.HashMap;

import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class LoginManager {

	private static HashMap<String, FailedLoginAttempt> failedLoginAttempts = new HashMap<String, FailedLoginAttempt>();
	private final static int DEFAULT_MAX_FAILED_ATTEMPTS = 7;
	private final static int DEFAULT_DELAY_MILLISECONDS = 15000;
	/**
	 * 
	 */
	public LoginManager() {
		// TODO Auto-generated constructor stub
	}
	public static int getMaxFailedAttempts(){
		String maxFailedAttemptsStr = ApplicationProperties.getProperty("tmtbl.login.max.failed.attempts", Integer.toString(DEFAULT_MAX_FAILED_ATTEMPTS));
		int maxFailedAttempts;
		try {
			maxFailedAttempts = Integer.parseInt(maxFailedAttemptsStr);
		} catch (NumberFormatException e) {			
			maxFailedAttempts = DEFAULT_MAX_FAILED_ATTEMPTS;
		}
		if (maxFailedAttempts < 0){
			maxFailedAttempts = DEFAULT_MAX_FAILED_ATTEMPTS;
		}
		return(maxFailedAttempts);
	}
	
	public static int getDelayMilliseconds(){
		String delayMillisecondsStr = ApplicationProperties.getProperty("tmtbl.login.failed.delay.milliseconds", Integer.toString(DEFAULT_DELAY_MILLISECONDS));
		int delayMilliseconds;
		try {
			delayMilliseconds = Integer.parseInt(delayMillisecondsStr);
		} catch (NumberFormatException e) {			
			delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
		}
		if (delayMilliseconds < 0){
			delayMilliseconds = DEFAULT_DELAY_MILLISECONDS;
		}
		return(delayMilliseconds);
	}
	
	public static boolean isUserLockedOut(String user, Date attemptDateTime){
		FailedLoginAttempt fla = failedLoginAttempts.get(user);
		if (fla != null){
			boolean lockedOut = fla.isUserLockedOut(user, attemptDateTime);
			if (lockedOut) {
				Debug.info("Too many failed login attempts - User: " + user + " temporarily locked out.");
				if (fla.getCount() > (getMaxFailedAttempts() + 3)){
					// If user has exceed his max failed attempts by 3 do not respond as quickly
					// This helps to prevent users from flooding the system with failed login attempts.
					try {
						Thread.sleep(getDelayMilliseconds());
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return(lockedOut);
		} else {
			return(false);
		}
	}
	
	public static void addFailedLoginAttempt(String user, Date attemptDateTime){
		FailedLoginAttempt fla = failedLoginAttempts.get(user);
		if (fla == null){
			failedLoginAttempts.put(user, new FailedLoginAttempt(user, 1, attemptDateTime));
		} else {
			if (fla.getCount() < getMaxFailedAttempts()){
				fla.setLastFailedAttempt(attemptDateTime);
			}
			fla.setCount(fla.getCount() + 1);
		}
	}
	
	public static void loginSuceeded(String user){
		failedLoginAttempts.remove(user);
	}
	
}
