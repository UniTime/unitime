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
import org.unitime.timetable.defaults.ApplicationProperty;

/**
 * @author Stephanie Schluttenhofer
 *
 */
public class LoginManager {
	private static HashMap<String, FailedLoginAttempt> failedLoginAttempts = new HashMap<String, FailedLoginAttempt>();

	public static boolean isUserLockedOut(String user, Date attemptDateTime){
		FailedLoginAttempt fla = failedLoginAttempts.get(user);
		if (fla != null) {
			boolean lockedOut = fla.isUserLockedOut(user, attemptDateTime);
			if (lockedOut) {
				Debug.info("Too many failed login attempts - User: " + user + " temporarily locked out.");
				if (fla.getCount() > ApplicationProperty.LoginMaxFailedAttempts.intValue() + 3) {
					// If user has exceed his max failed attempts by 3 do not respond as quickly
					// This helps to prevent users from flooding the system with failed login attempts.
					try {
						Thread.sleep(ApplicationProperty.LoginFailedAttemptDelay.intValue());
					} catch (InterruptedException e) {}
				}
			}
			return lockedOut;
		} else {
			return false;
		}
	}
	
	public static void addFailedLoginAttempt(String user, Date attemptDateTime){
		FailedLoginAttempt fla = failedLoginAttempts.get(user);
		if (fla == null){
			failedLoginAttempts.put(user, new FailedLoginAttempt(user, 1, attemptDateTime));
		} else {
			if (fla.getCount() < ApplicationProperty.LoginMaxFailedAttempts.intValue()){
				fla.setLastFailedAttempt(attemptDateTime);
			}
			fla.setCount(fla.getCount() + 1);
		}
	}
	
	public static void loginSuceeded(String user){
		failedLoginAttempts.remove(user);
	}
	
}
