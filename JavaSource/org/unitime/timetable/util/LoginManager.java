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
